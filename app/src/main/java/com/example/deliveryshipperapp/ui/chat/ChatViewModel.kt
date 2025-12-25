package com.example.deliveryshipperapp.ui.chat

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliveryshipperapp.utils.Constants
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import android.util.Base64
import com.example.deliveryshipperapp.data.remote.api.ChatApi

fun extractUserIdFromToken(token: String): Long? {
    return try {
        val parts = token.split(".")
        if (parts.size < 2) return null
        val payloadJson = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP))
        val payload = JSONObject(payloadJson)
        payload.optLong("user_id", -1L)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

//@HiltViewModel
//class ChatViewModel @Inject constructor(
//    @ApplicationContext private val appContext: Context
//) : ViewModel() {

@HiltViewModel
class ChatViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val chatApi: ChatApi
) : ViewModel() {

    // Map<orderId, danh sách tin nhắn>
    private val _conversations =
        MutableStateFlow<Map<Long, MutableList<ChatMessage>>>(emptyMap())
    val conversations: StateFlow<Map<Long, MutableList<ChatMessage>>> = _conversations

    private var currentOrderId: Long? = null
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()
    private val gson = Gson()
    var shipperId: Long? = null
        private set

    /** Khi khởi tạo, tải các đoạn chat đã lưu tạm */
    init {
        loadCachedConversations()
    }

    //xu ly chuỗi thời gian
    private fun parseCreatedAt(value: String): Long {
        return try {
            val formatter = java.text.SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                java.util.Locale.getDefault()
            )
            formatter.parse(value)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }


    //load tin nhan
    fun loadMessagesFromServer(orderId: Long) {
        viewModelScope.launch {
            try {
                Log.d("ChatVM", "📥 Shipper load DB messages for order=$orderId")

                val res = chatApi.getMessages(orderId, limit = 50)
                if (res.isSuccessful) {
                    //val serverMessages = res.body()?.messages ?: emptyList()
                    val serverMessages = res.body()?.data ?: emptyList()

                    val mapped = serverMessages.map {
                        ChatMessage(
                            fromUserId = it.from_user_id,
                            toUserId = it.to_user_id,
                            content = it.content,
                            createdAt = parseCreatedAt(it.created_at)
                        )
                    }

                    val map = _conversations.value.toMutableMap()
                    map[orderId] = mapped.toMutableList()
                    _conversations.value = map

                    saveConversation(orderId, mapped)

                    Log.d("ChatVM", "✅ Shipper loaded ${mapped.size} messages")
                } else {
                    Log.e("ChatVM", "❌ Load DB failed: ${res.code()}")
                }
            } catch (e: Exception) {
                Log.e("ChatVM", "❌ Load DB exception", e)
            }
        }
    }

    /** Kết nối WebSocket với token thật, cho đơn cụ thể */
    fun connectWebSocket(orderId: Long, accessToken: String) {
        currentOrderId = orderId
        val url = Constants.BASE_URL.replace("http", "ws") + "ws?token=$accessToken"
        val request = Request.Builder().url(url).build()
        shipperId = extractUserIdFromToken(accessToken)
        Log.d("ChatVM", "Current shipperId = $shipperId")
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                Log.d("ChatVM", "✅ WS connected")

            }



            override fun onMessage(ws: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    val msg = ChatMessage(
                        fromUserId = json.optLong("from_user_id", 0L),
                        toUserId = json.optLong("to_user_id", 0L),
                        content = json.optString("content"),
                        createdAt = System.currentTimeMillis()
                    )
                    viewModelScope.launch { appendMessage(orderId, msg) }
                } catch (e: Exception) {
                    Log.e("ChatVM", "Parse error: ${e.message}")
                }
            }

            override fun onMessage(ws: WebSocket, bytes: ByteString) {
                onMessage(ws, bytes.utf8())
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e("ChatVM", "WebSocket failure: ${t.message}")
            }
        })
    }

    fun sendMessage(orderId: Long, toUserId: Long, content: String) {
        val json = JSONObject()
        json.put("type", "chat_message")
        json.put("order_id", orderId)
        json.put("to_user_id", toUserId)
        json.put("content", content)

        webSocket?.send(json.toString())

        // ✅ SỬA Ở ĐÂY: Dùng shipperId thực tế thay vì -1L
        val myId = shipperId ?: -1L

        val msg = ChatMessage(
            fromUserId = myId, // Dùng ID thật để UI hiển thị đúng bên phải
            toUserId = toUserId,
            content = content,
            createdAt = System.currentTimeMillis()
        )
        viewModelScope.launch { appendMessage(orderId, msg) }
    }
    /** Thêm tin nhắn vào map + lưu cache */
//    private fun appendMessage(orderId: Long, msg: ChatMessage) {
//        val map = _conversations.value.toMutableMap()
//        val list = map[orderId] ?: mutableListOf()
//        list.add(msg)
//        map[orderId] = list
//        _conversations.value = map
//        saveConversation(orderId, list)
//    }
    private fun appendMessage(orderId: Long, msg: ChatMessage) {
        val currentMap = _conversations.value.toMutableMap()
        val oldList = currentMap[orderId] ?: emptyList()

        // ⚡ Tạo list mới để Compose recompose
        val newList = oldList + msg

        currentMap[orderId] = newList.toMutableList()
        _conversations.value = currentMap

        saveConversation(orderId, newList)
    }


    /** Xoá toàn bộ chat của 1 đơn (khi giao xong) */
    fun clearConversation(orderId: Long) {
        val map = _conversations.value.toMutableMap()
        map.remove(orderId)
        _conversations.value = map
        deleteConversation(orderId)
    }

    override fun onCleared() {
        webSocket?.close(1000, "closed")
        super.onCleared()
    }

    // ====== Caching bằng SharedPreferences đơn giản ======
    private fun prefs() =
        appContext.getSharedPreferences("chat_cache", Context.MODE_PRIVATE)

    private fun saveConversation(orderId: Long, list: List<ChatMessage>) {
        prefs().edit()
            .putString(orderId.toString(), gson.toJson(list))
            .apply()
    }

    private fun loadCachedConversations() {
        val all = prefs().all
        val map = mutableMapOf<Long, MutableList<ChatMessage>>()
        all.forEach { (orderIdStr, jsonStr) ->
            val id = orderIdStr.toLongOrNull() ?: return@forEach
            val type = object : TypeToken<MutableList<ChatMessage>>() {}.type
            runCatching {
                val list: MutableList<ChatMessage> = gson.fromJson(jsonStr as String, type)
                map[id] = list
            }
        }
        _conversations.value = map
    }

    private fun deleteConversation(orderId: Long) {
        prefs().edit().remove(orderId.toString()).apply()
    }
}