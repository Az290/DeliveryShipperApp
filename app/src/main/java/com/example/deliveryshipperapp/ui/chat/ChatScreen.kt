package com.example.deliveryshipperapp.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.deliveryshipperapp.data.local.DataStoreManager
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    orderId: Long,
    customerId: Long,
    customerName: String,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages = viewModel.messages
    val inputText by viewModel.inputText
    val isEnabled by viewModel.isChatEnabled
    val name by viewModel.customerName

    val context = LocalContext.current
    val dataStore = remember { DataStoreManager(context) }
    val token by dataStore.accessToken.map { it ?: "" }.collectAsState(initial = "")

    LaunchedEffect(orderId, customerId, customerName, token) {
        if (token.isNotEmpty()) {
            viewModel.initChat(orderId, customerId, customerName, token)
        }
    }


    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Chat với $name") })

        if (!isEnabled) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Gray.copy(alpha = 0.3f)),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Đơn hàng đã được hoàn thành, không thể tiếp tục nhắn tin",
                        modifier = Modifier.padding(16.dp),
                        color = Color.Gray
                    )
                }
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                Text(
                    text = "${if (msg.fromUserId == customerId) name else "Bạn"}: ${msg.content}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Blue.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                )
            }
        }

        Row(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { viewModel.inputText.value = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Nhập tin nhắn...") }
            )
            IconButton(onClick = { viewModel.sendMessage() }) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}