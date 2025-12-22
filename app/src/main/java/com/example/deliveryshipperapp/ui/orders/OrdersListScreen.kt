package com.example.deliveryshipperapp.ui.orders

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
                    // Nút refresh (GIỮ LẠI)
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
                    // ❌ ĐÃ XÓA NÚT LOGOUT TẠI ĐÂY
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
                // ✅ FIX: Use Elvis operator to default to emptyList() if null
                val orders: List<OrderSummaryDto> = state.data?.safeOrders() ?: emptyList()

                if (orders.isEmpty()) {
                    // ✅ Hiển thị empty state giống nhau cho cả 2 tab
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
                                ),
                                textAlign = TextAlign.Center
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
                                        // ✅ Safe to use .size here because 'orders' is guaranteed not null
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
                        itemsIndexed(orders) { index, order ->
                            OrderCard(
                                order = order,
                                navController = navController,
                                mode = mode,
                                viewModel = viewModel,
                                index = index,
                                orders = orders
                            )
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
    viewModel: OrdersViewModel,
    index: Int,
    orders: List<OrderSummaryDto>
) {
    val isLocked = shouldLockOrder(mode, index, orders)
    val cardAlpha = if (isLocked) 0.5f else 1f
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                if (isLocked) {
                    stateDescription = "Đơn hàng bị khóa, cần giao đơn trước"
                }
            }
            .clickable(enabled = !isLocked) {
                handleOrderClick(order, mode, navController, viewModel)
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .alpha(if (isLocked) 0.6f else 1f)
            ) {
                OrderCardContent(order = order, isLocked = isLocked)
            }

            if (isLocked) {
                LockedOrderWarning()
            }
        }
    }
}

@Composable
private fun OrderCardContent(
    order: OrderSummaryDto,
    isLocked: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ✅ ĐÃ SỬA: Thay thumbnail_id bằng thumbnail
        OrderThumbnail(thumbnailUrl = order.thumbnail)

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = if (order.thumbnail.isNullOrEmpty()) 0.dp else 16.dp)
        ) {
            OrderHeader(orderId = order.id)
            Spacer(Modifier.height(4.dp))
            OrderStatusBadge(status = order.order_status)
            Spacer(Modifier.height(6.dp))
            OrderAmount(amount = order.total_amount)
        }

        OrderArrowIcon(isLocked = isLocked)
    }
}

@Composable
private fun OrderThumbnail(thumbnailUrl: String?) {
    if (!thumbnailUrl.isNullOrEmpty()) {
        Surface(
            modifier = Modifier
                .size(70.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF0F0F0)
        ) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = "Ảnh sản phẩm",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun OrderHeader(orderId: Long) {
    Text(
        text = "Đơn hàng #$orderId",
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    )
}

@Composable
private fun OrderStatusBadge(status: String) {
    val statusStyle = getOrderStatusStyle(status)

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = statusStyle.backgroundColor.copy(alpha = 0.2f)
    ) {
        Text(
            text = "${statusStyle.emoji} ${statusStyle.label}",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                color = statusStyle.textColor
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun OrderAmount(amount: Double) {
    Text(
        text = "${formatCurrency(amount)} đ",
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = Color(0xFF667eea),
            fontSize = 18.sp
        )
    )
}

@Composable
private fun OrderArrowIcon(isLocked: Boolean) {
    Icon(
        imageVector = Icons.Default.ArrowForward,
        contentDescription = if (isLocked) "Đơn hàng bị khóa" else "Xem chi tiết đơn hàng",
        tint = if (isLocked) Color.Gray else Color(0xFF667eea),
        modifier = Modifier.size(24.dp)
    )
}

@Composable
private fun LockedOrderWarning() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color(0xFFFFF3CD))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = Color(0xFFF57C00),
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "Đơn chưa được mở khóa giao",
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFF856404),
                fontWeight = FontWeight.Medium
            )
        )
    }
}

private fun shouldLockOrder(
    mode: String,
    index: Int,
    orders: List<OrderSummaryDto>
): Boolean {
    return mode == OrderMode.SHIPPING &&
            orders.size > 1 &&
            index > 0 &&
            orders[index - 1].order_status != OrderStatus.DELIVERED
}

private fun handleOrderClick(
    order: OrderSummaryDto,
    mode: String,
    navController: NavController,
    viewModel: OrdersViewModel
) {
    viewModel.selectOrder(order.id)

    val route = when (mode) {
        OrderMode.PROCESSING -> "order/${order.id}"
        else -> "delivery/${order.id}"
    }

    navController.navigate(route)
}

private fun getOrderStatusStyle(status: String): OrderStatusStyle {
    return when (status) {
        OrderStatus.PROCESSING -> OrderStatusStyle(
            emoji = "🔄",
            label = "Đang xử lý",
            backgroundColor = Color(0xFFFFA726),
            textColor = Color(0xFFF57C00)
        )
        OrderStatus.SHIPPING -> OrderStatusStyle(
            emoji = "🚚",
            label = "Đang giao",
            backgroundColor = Color(0xFF42A5F5),
            textColor = Color(0xFF1976D2)
        )
        OrderStatus.DELIVERED -> OrderStatusStyle(
            emoji = "✅",
            label = "Đã giao",
            backgroundColor = Color(0xFF66BB6A),
            textColor = Color(0xFF2E7D32)
        )
        else -> OrderStatusStyle(
            emoji = "",
            label = status,
            backgroundColor = Color.Gray,
            textColor = Color.Gray
        )
    }
}

private fun formatCurrency(amount: Double): String {
    return String.format("%,.0f", amount)
}

private data class OrderStatusStyle(
    val emoji: String,
    val label: String,
    val backgroundColor: Color,
    val textColor: Color
)

object OrderStatus {
    const val PROCESSING = "processing"
    const val SHIPPING = "shipping"
    const val DELIVERED = "delivered"
}

object OrderMode {
    const val PROCESSING = "processing"
    const val SHIPPING = "shipping"
}