package com.example.deliveryshipperapp.ui.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    // Màu gradient cho header
    val gradientColors = listOf(
        Color(0xFF667eea),
        Color(0xFF764ba2)
    )

    // Khởi động load theo mode với delay
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
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = if (mode == "processing") "📦" else "🚚",
                                    fontSize = 20.sp
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = if (mode == "processing") "Đơn Có Thể Nhận" else "Đơn Của Tôi",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                },
                actions = {
                    // Nút refresh
                    IconButton(
                        onClick = {
                            if (mode == "processing") viewModel.loadAvailableOrders()
                            else viewModel.loadMyOrders()
                        }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Làm mới",
                            tint = Color.White
                        )
                    }
                    // Nút logout
                    IconButton(onClick = { /* TODO: logout */ }) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = "Đăng xuất",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF667eea),
                    titleContentColor = Color.White
                )
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
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        when (state) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF667eea),
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Đang tải đơn hàng...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.Gray
                            )
                        )
                    }
                }
            }
            is Resource.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "⚠️",
                            fontSize = 64.sp
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Có lỗi xảy ra",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = state.message ?: "Vui lòng thử lại",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.Gray
                            )
                        )
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(
                                text = if (mode == "processing") "📭" else "🎉",
                                fontSize = 64.sp
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "Không có đơn hàng nào",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (mode == "processing")
                                    "Chưa có đơn hàng mới để nhận"
                                else
                                    "Bạn chưa có đơn hàng nào đang giao",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.Gray
                                )
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header info
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF667eea).copy(alpha = 0.1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = null,
                                        tint = Color(0xFF667eea),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = "Tổng: ${orders.size} đơn hàng",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF667eea)
                                        )
                                    )
                                }
                            }
                        }

                        // List orders
                        items(orders) { order ->
                            OrderCard(order, navController, mode, viewModel)
                        }
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // Lưu ID của đơn hàng được chọn
                viewModel.selectOrder(order.id)

                if (mode == "processing") {
                    navController.navigate("order/${order.id}")
                } else {
                    navController.navigate("delivery/${order.id}")
                }
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            if (!order.thumbnail.isNullOrEmpty()) {
                Surface(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    color = Color(0xFFF0F0F0)
                ) {
                    AsyncImage(
                        model = order.thumbnail,
                        contentDescription = "Ảnh sản phẩm",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(Modifier.width(16.dp))
            } else {
                // Placeholder nếu không có ảnh
                Surface(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    color = Color(0xFF667eea).copy(alpha = 0.1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "📦",
                            fontSize = 32.sp
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
            }

            // Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Mã đơn
                Text(
                    text = "Đơn hàng #${order.id}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )

                Spacer(Modifier.height(4.dp))

                // Trạng thái
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when(order.order_status) {
                        "processing" -> Color(0xFFFFA726).copy(alpha = 0.2f)
                        "shipping" -> Color(0xFF42A5F5).copy(alpha = 0.2f)
                        "delivered" -> Color(0xFF66BB6A).copy(alpha = 0.2f)
                        else -> Color.Gray.copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = when(order.order_status) {
                            "processing" -> "🔄 Đang xử lý"
                            "shipping" -> "🚚 Đang giao"
                            "delivered" -> "✅ Đã giao"
                            else -> order.order_status
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = when(order.order_status) {
                                "processing" -> Color(0xFFF57C00)
                                "shipping" -> Color(0xFF1976D2)
                                "delivered" -> Color(0xFF2E7D32)
                                else -> Color.Gray
                            }
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(Modifier.height(6.dp))

                // Giá tiền
                Text(
                    text = "${String.format("%,.0f", order.total_amount)} đ",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF667eea),
                        fontSize = 18.sp
                    )
                )

            }

            // Arrow icon
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                color = Color(0xFF667eea).copy(alpha = 0.1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Xem chi tiết",
                        tint = Color(0xFF667eea),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}