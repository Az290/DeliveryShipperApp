package com.example.deliveryshipperapp.ui.orders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.deliveryshipperapp.data.remote.dto.OrderSummaryDto
import com.example.deliveryshipperapp.data.remote.dto.OrdersListResponse
import com.example.deliveryshipperapp.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersListScreen(
    navController: NavController,
    viewModel: OrdersViewModel = hiltViewModel()
) {
    val availableState by viewModel.availableOrders.collectAsState()

    // Load data init
    LaunchedEffect(Unit) {
        viewModel.loadAvailableOrders()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shipper App") },
                actions = {
                    IconButton(onClick = { viewModel.loadAvailableOrders() }) {
                        Icon(Icons.Default.Refresh, "Làm mới")
                    }
                    IconButton(onClick = {
                        // TODO: logout, sau đó chuyển màn hình login
                    }) {
                        Icon(Icons.Default.ExitToApp, "Đăng xuất")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Chỉ còn danh sách "Đơn có thể nhận"
            OrdersList(
                state = availableState,
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun OrdersList(
    state: Resource<OrdersListResponse>,
    navController: NavController,
    viewModel: OrdersViewModel
) {
    when (state) {
        is Resource.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is Resource.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("❌ ${state.message}")
                Button(onClick = { viewModel.loadAvailableOrders() }) {
                    Text("Thử lại")
                }
            }
        }
        is Resource.Success -> {
            val orders = state.data?.orders ?: emptyList()
            if (orders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Không có đơn hàng nào")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(orders) { order ->
                        OrderCard(order, navController)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(order: OrderSummaryDto, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { navController.navigate("order/${order.id}") },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!order.thumbnail.isNullOrEmpty()) {
                AsyncImage(
                    model = order.thumbnail,
                    contentDescription = "Thumbnail",
                    modifier = Modifier.size(60.dp),
                )
                Spacer(Modifier.width(16.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text("Đơn #${order.id}", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text("Trạng thái: ${order.order_status}", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(4.dp))
                Text("${order.total_amount} đ", style = MaterialTheme.typography.bodyMedium)
            }

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Chi tiết",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}