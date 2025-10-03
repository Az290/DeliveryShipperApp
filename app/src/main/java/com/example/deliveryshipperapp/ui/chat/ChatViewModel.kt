package com.example.deliveryshipperapp.ui.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliveryshipperapp.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import javax.inject.Inject

class ChatViewModel @Inject constructor() : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    fun connectWebSocket(accessToken: String) {
        val request = Request.Builder()
            .url(Constants.BASE_URL.replace("http","ws") + "ws?token=$accessToken")
            .build()
        webSocket = client.newWebSocket(request, object: WebSocketListener(){
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("ChatVM","✅ Connected WS")
            }
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    val msg = ChatMessage(
                        fromUserId = json.optLong("from_user_id", 0L),
                        toUserId = json.optLong("to_user_id", 0L),
                        content = json.optString("content")
                    )
                    viewModelScope.launch {
                        _messages.value = _messages.value + msg
                    }
                } catch (e:Exception){ Log.e("ChatVM","Parse err ${e.message}") }
            }
            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                onMessage(webSocket, bytes.utf8())
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, resp: Response?) {
                Log.e("ChatVM","WS err ${t.message}")
            }
        })
    }

    fun sendMessage(orderId: Long, toUserId: Long, content: String) {
        val json = JSONObject()
        json.put("type","chat_message")
        json.put("order_id", orderId)
        json.put("to_user_id", toUserId)
        json.put("content", content)
        webSocket?.send(json.toString())

        val msg = ChatMessage(fromUserId = -1, toUserId = toUserId, content = content) // từ shipper
        _messages.value = _messages.value + msg
    }

    override fun onCleared() {
        webSocket?.close(1000,"closed")
        super.onCleared()
    }
}