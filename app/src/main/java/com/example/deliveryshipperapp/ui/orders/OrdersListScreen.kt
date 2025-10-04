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
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersListScreen(
    navController: NavController,
    viewModel: OrdersViewModel = hiltViewModel(),
    mode: String = "processing" // "processing" = Home, "shipping" = MyOrders
) {
    val state by if (mode == "processing")
        viewModel.availableOrders.collectAsState()
    else
        viewModel.myOrders.collectAsState()

    // khởi động load theo mode với delay
    LaunchedEffect(mode) {
        if (mode == "processing") {
            viewModel.loadAvailableOrders()
        } else {
            // Thêm delay nhỏ để đảm bảo backend đã cập nhật
            delay(300)
            viewModel.loadMyOrders()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (mode == "processing") "Đơn có thể nhận" else "Đơn của tôi") },
                actions = {
                    IconButton(onClick = {
                        if (mode == "processing") viewModel.loadAvailableOrders()
                        else viewModel.loadMyOrders()
                    }) {
                        Icon(Icons.Default.Refresh, "refresh")
                    }
                    IconButton(onClick = { /* TODO: logout */ }) {
                        Icon(Icons.Default.ExitToApp, "logout")
                    }
                }
            )
        }
    ) { padding ->
        OrdersListContent(
            modifier = Modifier.padding(padding),
            state = state,
            navController = navController,
            mode = mode,
            viewModel = viewModel
        )
    }
}

@Composable
fun OrdersListContent(
    modifier: Modifier = Modifier,
    state: Resource<OrdersListResponse>,
    navController: NavController,
    mode: String,
    viewModel: OrdersViewModel
) {
    when (state) {
        is Resource.Loading -> Box(modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }
        is Resource.Error -> Box(modifier.fillMaxSize(), Alignment.Center) {
            Text("❌ ${state.message}")
        }
        is Resource.Success -> {
            val orders = state.data?.orders ?: emptyList()
            if (orders.isEmpty()) {
                Box(modifier.fillMaxSize(), Alignment.Center) {
                    Text("Không có đơn hàng nào")
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(8.dp)) {
                    items(orders) { order ->
                        OrderCard(order, navController, mode, viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(
    order: OrderSummaryDto,
    navController: NavController,
    mode: String,
    viewModel: OrdersViewModel
) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                // Lưu ID của đơn hàng được chọn
                viewModel.selectOrder(order.id)

                if (mode == "processing") {
                    navController.navigate("order/${order.id}")
                } else {
                    navController.navigate("delivery/${order.id}")
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!order.thumbnail.isNullOrEmpty()) {
                AsyncImage(
                    model = order.thumbnail,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp)
                )
                Spacer(Modifier.width(16.dp))
            }
            Column(Modifier.weight(1f)) {
                Text("Đơn #${order.id}", style = MaterialTheme.typography.titleMedium)
                Text("Trạng thái: ${order.order_status}", style = MaterialTheme.typography.bodySmall)
                Text("${order.total_amount} đ", style = MaterialTheme.typography.bodyMedium)
            }
            Icon(Icons.Default.ArrowForward, contentDescription = null)
        }
    }
}