package com.example.deliveryshipperapp.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    orderId: Long,
    customerId: Long,
    accessToken: String,
    customerName: String = "Khách hàng",   // 👈 thêm mặc định
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    var input by remember { mutableStateOf("") }

    LaunchedEffect(accessToken) {
        if (accessToken.isNotBlank()) {
            viewModel.connectWebSocket(accessToken)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Chat với $customerName (Đơn #$orderId)") }) },
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    if (input.isNotBlank()) {
                        viewModel.sendMessage(orderId, customerId, input)
                        input = ""
                    }
                }) { Text("Gửi") }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(messages) { msg ->
                val mine = (msg.fromUserId == -1L)
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (mine)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(Modifier.padding(8.dp)) {
                        Text(
                            text = if (mine) "Tôi" else customerName,
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(msg.content)
                    }
                }
            }
        }
    }
}