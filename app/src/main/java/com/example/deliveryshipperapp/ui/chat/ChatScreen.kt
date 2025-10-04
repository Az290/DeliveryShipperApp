package com.example.deliveryshipperapp.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    orderId: Long,
    customerId: Long,
    accessToken: String,
    customerName: String = "Khách hàng",
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Màu sắc tùy chỉnh
    val shipperBubbleColor = Color(0xFF667eea)
    val customerBubbleColor = Color(0xFFF0F0F0)
    val backgroundColor = Color(0xFFFAFAFA)

    LaunchedEffect(accessToken) {
        if (accessToken.isNotBlank()) {
            viewModel.connectWebSocket(accessToken)
        }
    }

    // Auto scroll khi có tin nhắn mới
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Avatar khách hàng
                        Surface(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            color = Color(0xFF667eea).copy(alpha = 0.2f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Avatar khách hàng",
                                    tint = Color(0xFF667eea),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(Modifier.width(12.dp))

                        Column {
                            Text(
                                text = customerName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Đơn hàng #$orderId",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1A1A1A)
                ),
                navigationIcon = {
                    IconButton(onClick = { /* Xử lý quay lại */ }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color(0xFF667eea)
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Input field
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                "Nhập tin nhắn...",
                                color = Color.Gray.copy(alpha = 0.5f)
                            )
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF667eea),
                            unfocusedBorderColor = Color.LightGray,
                            cursorColor = Color(0xFF667eea)
                        ),
                        maxLines = 4
                    )

                    Spacer(Modifier.width(8.dp))

                    // Nút gửi
                    FloatingActionButton(
                        onClick = {
                            if (input.isNotBlank()) {
                                viewModel.sendMessage(orderId, customerId, input)
                                input = ""
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        containerColor = Color(0xFF667eea),
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Gửi tin nhắn",
                            tint = Color.White
                        )
                    }
                }
            }
        },
        containerColor = backgroundColor
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (messages.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "💬",
                        fontSize = 64.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Chưa có tin nhắn nào",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Gửi tin nhắn đầu tiên để bắt đầu trò chuyện",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Gray.copy(alpha = 0.7f)
                        )
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { msg ->
                        val mine = (msg.fromUserId == -1L)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (mine)
                                Arrangement.End
                            else
                                Arrangement.Start
                        ) {
                            // Bubble tin nhắn
                            Surface(
                                modifier = Modifier.widthIn(max = 280.dp),
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (mine) 16.dp else 4.dp,
                                    bottomEnd = if (mine) 4.dp else 16.dp
                                ),
                                color = if (mine) shipperBubbleColor else customerBubbleColor,
                                shadowElevation = 2.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 8.dp
                                    )
                                ) {
                                    // Tên người gửi
                                    Text(
                                        text = if (mine) "Tôi" else customerName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (mine)
                                                Color.White.copy(alpha = 0.9f)
                                            else
                                                Color(0xFF667eea),
                                            fontSize = 11.sp
                                        )
                                    )

                                    Spacer(Modifier.height(4.dp))

                                    // Nội dung tin nhắn
                                    Text(
                                        text = msg.content,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = if (mine) Color.White else Color.Black,
                                            fontSize = 15.sp
                                        )
                                    )

                                    // Thời gian (optional - nếu có timestamp)
                                    // Text(
                                    //     text = "10:30",
                                    //     style = MaterialTheme.typography.labelSmall.copy(
                                    //         color = if (mine)
                                    //             Color.White.copy(alpha = 0.7f)
                                    //         else
                                    //             Color.Gray,
                                    //         fontSize = 10.sp
                                    //     ),
                                    //     modifier = Modifier.align(Alignment.End)
                                    // )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}