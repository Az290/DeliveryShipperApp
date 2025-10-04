package com.example.deliveryshipperapp.ui.chat

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliveryshipperapp.data.local.DataStoreManager
import com.example.deliveryshipperapp.utils.WebSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

// Đổi tên để tránh trùng DTO
data class ChatMessageUi(
    val id: Long? = null,
    val fromUserId: Long,
    val toUserId: Long,
    val content: String,
    val createdAt: String,
    val orderId: Long
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val dataStore: DataStoreManager
) : ViewModel() {

    val messages = mutableStateListOf<ChatMessageUi>()
    val inputText = mutableStateOf("")
    val isChatEnabled = mutableStateOf(true)
    val customerName = mutableStateOf("Customer")

    private var wsManager: WebSocketManager? = null
    private var currentOrderId: Long = 0L
    private var toUserId: Long = 0L

    fun initChat(orderId: Long, toUserId: Long, customerName: String, token: String) {
        this.currentOrderId = orderId
        this.toUserId = toUserId
        this.customerName.value = customerName

        viewModelScope.launch {
            val accessToken = token.ifEmpty {
                dataStore.accessToken.firstOrNull() ?: return@launch
            }

            wsManager = WebSocketManager(
                token = accessToken,
                onMessageReceived = { msg ->
                    handleIncomingMessage(msg)
                },
                onClosed = {
                    isChatEnabled.value = false
                }
            )

            wsManager?.connect(orderId)
        }
    }

    private fun handleIncomingMessage(jsonStr: String) {
        try {
            val json = JSONObject(jsonStr)
            if (json.optString("type") == "chat_message") {
                val message = ChatMessageUi(
                    fromUserId = json.optLong("from_user_id", 0L),
                    toUserId = json.optLong("to_user_id", 0L),
                    content = json.optString("content", ""),
                    createdAt = json.optString("created_at", ""),
                    orderId = json.optLong("order_id", 0L)
                )
                messages.add(message)
            }
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Parse message error: ${e.message}")
        }
    }

    fun sendMessage() {
        if (!isChatEnabled.value || inputText.value.isBlank()) return

        // ⚠️ Kiểm tra định nghĩa WebSocketManager.sendMessage()
        // Nếu hàm của bạn là sendMessage(orderId, toUserId, content) thì dùng dòng dưới:
        wsManager?.sendMessage(currentOrderId, toUserId, inputText.value)

        val sentMsg = ChatMessageUi(
            fromUserId = 0L, // Shipper ID thực có thể lấy từ token
            toUserId = toUserId,
            content = inputText.value,
            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date()),
            orderId = currentOrderId
        )
        messages.add(sentMsg)
        inputText.value = ""
    }

    fun onOrderCompleted() {
        isChatEnabled.value = false
        wsManager?.close()
        messages.clear()
    }

    override fun onCleared() {
        wsManager?.close()
        super.onCleared()
    }
}
