package com.example.deliveryshipperapp.ui.orders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.deliveryshipperapp.data.remote.dto.OrderSummaryDto
import com.example.deliveryshipperapp.data.remote.dto.OrdersListResponse
import com.example.deliveryshipperapp.utils.Resource

@OptIn(ExperimentalMaterial3Api::class) // Thêm annotation này để bỏ qua cảnh báo
@Composable
fun OrdersListScreen(navController: NavController, viewModel: OrdersViewModel = hiltViewModel()) {
    var selectedTab by remember { mutableStateOf(0) }
    val availableState by viewModel.availableOrders.collectAsState()
    val myOrdersState by viewModel.myOrders.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAvailableOrders()
        viewModel.loadMyOrders()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shipper App") },
                actions = {
                    IconButton(onClick = {
                        // Đăng xuất
                    }) {
                        Icon(Icons.Default.ExitToApp, "Đăng xuất")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Tab cho "Đơn có thể nhận" và "Đơn của tôi"
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Đơn có thể nhận") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Đơn của tôi") }
                )
            }

            when (selectedTab) {
                0 -> OrdersList(availableState, navController, "Đơn có thể nhận", viewModel)
                1 -> OrdersList(myOrdersState, navController, "Đơn của tôi", viewModel)
            }
        }
    }
}

// Phần code còn lại giữ nguyên

@Composable
fun OrdersList(
    state: Resource<OrdersListResponse>,
    navController: NavController,
    title: String,
    viewModel: OrdersViewModel
) {
    when(state) {
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("❌ ${(state as Resource.Error).message}")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        if (title == "Đơn có thể nhận") {
                            viewModel.loadAvailableOrders()
                        } else {
                            viewModel.loadMyOrders()
                        }
                    }) {
                        Text("Thử lại")
                    }
                }
            }
        }
        is Resource.Success -> {
            val orders = (state as Resource.Success).data?.orders ?: emptyList()

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
        else -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Đang tải dữ liệu...")
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
            // Hiển thị ảnh thumbnail nếu có
            if (!order.thumbnail.isNullOrEmpty()) {
                AsyncImage(
                    model = order.thumbnail,
                    contentDescription = "Thumbnail",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Đơn #${order.id}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Hiển thị trạng thái đơn hàng
                Surface(
                    color = when(order.order_status) {
                        "processing" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        "shipping" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                        "delivered" -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                        else -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = when(order.order_status) {
                            "processing" -> "Đang chuẩn bị"
                            "shipping" -> "Đang giao"
                            "delivered" -> "Đã giao"
                            else -> order.order_status
                        },
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${order.total_amount} đ",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Chi tiết",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}