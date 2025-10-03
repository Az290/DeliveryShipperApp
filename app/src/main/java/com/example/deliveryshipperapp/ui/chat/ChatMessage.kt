data class ChatMessage(
    val fromUserId: Long,
    val toUserId: Long,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()  // ✅ an toàn, không crash
)