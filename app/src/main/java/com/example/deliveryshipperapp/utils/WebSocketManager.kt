package com.example.deliveryshipperapp.utils

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private const val TAG = "WebSocketManager"

class WebSocketManager(
    private val token: String,
    private val onMessageReceived: (String) -> Unit,  // Callback nhận message
    private val onClosed: () -> Unit  // Callback khi đóng
) {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val listener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "WS Connected")
            this@WebSocketManager.webSocket = webSocket
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d(TAG, "Received: $text")
            onMessageReceived(text)
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WS Closing: $reason")
            webSocket.close(1000, null)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WS Closed")
            onClosed()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "WS Failure: ${t.message}")
            onClosed()
        }
    }

    fun connect(orderId: Long) {
        val request = Request.Builder()
            .url("ws://10.0.2.2:8080/ws?token=$token")
            .build()
        val ws = client.newWebSocket(request, listener)
    }

    fun sendMessage(orderId: Long, toUserId: Long, content: String) {
        val message = JSONObject().apply {
            put("type", "chat_message")
            put("order_id", orderId)
            put("to_user_id", toUserId)
            put("content", content)
        }
        webSocket?.send(message.toString())
    }


    fun close() {
        webSocket?.close(1000, "Order completed")
        webSocket = null
    }
}