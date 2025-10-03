package com.example.deliveryshipperapp.ui.chat

data class ChatMessage(
    val fromUserId: Long,
    val toUserId: Long,
    val content: String,
    val createdAt: Long = System.currentTimeMillis() // dùng timestamp thay LocalDateTime
)