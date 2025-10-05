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

@HiltViewModel
class ChatViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context
) : ViewModel() {
    // Map<orderId, danh sách tin nhắn>
    private val _conversations =
        MutableStateFlow<Map<Long, MutableList<ChatMessage>>>(emptyMap())
    val conversations: StateFlow<Map<Long, MutableList<ChatMessage>>> = _conversations

    private var currentOrderId: Long? = null
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()
    private val gson = Gson()

    /** Khi khởi tạo, tải các đoạn chat đã lưu tạm */
    init {
        loadCachedConversations()
    }

    /** Kết nối WebSocket với token thật, cho đơn cụ thể */
    fun connectWebSocket(orderId: Long, accessToken: String) {
        currentOrderId = orderId
        val url = Constants.BASE_URL.replace("http", "ws") + "ws?token=$accessToken"
        val request = Request.Builder().url(url).build()

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

    /** Gửi tin nhắn cho đơn cụ thể */
    fun sendMessage(orderId: Long, toUserId: Long, content: String) {
        val json = JSONObject()
        json.put("type", "chat_message")
        json.put("order_id", orderId)
        json.put("to_user_id", toUserId)
        json.put("content", content)

        webSocket?.send(json.toString())

        val msg = ChatMessage(
            fromUserId = -1L,
            toUserId = toUserId,
            content = content,
            createdAt = System.currentTimeMillis()
        )
        viewModelScope.launch { appendMessage(orderId, msg) }
    }

    /** Thêm tin nhắn vào map + lưu cache */
    private fun appendMessage(orderId: Long, msg: ChatMessage) {
        val map = _conversations.value.toMutableMap()
        val list = map[orderId] ?: mutableListOf()
        list.add(msg)
        map[orderId] = list
        _conversations.value = map
        saveConversation(orderId, list)
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