package com.example.deliveryshipperapp.ui.orders

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.deliveryshipperapp.data.remote.dto.OrderSummaryDto
import com.example.deliveryshipperapp.data.remote.dto.OrdersListResponse
import com.example.deliveryshipperapp.utils.Resource
import com.example.deliveryshipperapp.utils.getAddressFromLatLng // Đảm bảo bạn đã có hàm này ở Utils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    LaunchedEffect(mode) {
        if (mode == "processing") {
            viewModel.loadAvailableOrders()
        } else {
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
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
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
                        Text(text = "⚠️", fontSize = 64.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Có lỗi xảy ra",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = state.message ?: "Vui lòng thử lại",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
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
                            Text(text = if (mode == "processing") "📭" else "🎉", fontSize = 64.sp)
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "Không có đơn hàng nào",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (mode == "processing") "Chưa có đơn hàng mới để nhận" else "Bạn chưa có đơn hàng nào đang giao",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
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
    val scope = rememberCoroutineScope()
    var isAccepting by remember { mutableStateOf(false) } // Loading state cho nút nhận

    // Lấy địa chỉ hiển thị
    var addressInfo by remember { mutableStateOf("Đang xác định vị trí...") }
    LaunchedEffect(order.latitude, order.longitude) {
        if (order.latitude != 0.0 && order.longitude != 0.0) {
            // Đảm bảo bạn đã import hàm getAddressFromLatLng
            addressInfo = getAddressFromLatLng(context, order.latitude, order.longitude)
        } else {
            addressInfo = "Chưa có thông tin vị trí"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                if (isLocked) {
                    stateDescription = "Đơn hàng bị khóa"
                }
            }
            .clickable(enabled = !isLocked && mode != "processing") {
                // Chỉ cho click xem chi tiết nếu KHÔNG phải tab "processing"
                if (mode != "processing") {
                    handleOrderClick(order, mode, navController, viewModel)
                }
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .alpha(if (isLocked) 0.6f else 1f)
            ) {
                // Header + Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OrderThumbnail(thumbnailUrl = order.thumbnail)

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = if (order.thumbnail.isNullOrEmpty()) 0.dp else 16.dp)
                    ) {
                        OrderHeader(orderId = order.id)
                        Spacer(Modifier.height(4.dp))

                        // Nếu đang ở tab Processing -> Hiện địa chỉ thay vì status badge
                        if (mode == "processing") {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color.Red,
                                    modifier = Modifier.size(14.dp).padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = addressInfo,
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else {
                            // Tab Shipping -> Hiện Status Badge như cũ
                            OrderStatusBadge(status = order.order_status)
                        }

                        Spacer(Modifier.height(6.dp))
                        OrderAmount(amount = order.total_amount)
                    }

                    // Nếu tab Shipping -> Hiện mũi tên như cũ
                    if (mode != "processing") {
                        OrderArrowIcon(isLocked = isLocked)
                    }
                }

                // Nếu là tab Processing -> Thêm nút NHẬN ĐƠN NGAY
                if (mode == "processing") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (!isAccepting) {
                                isAccepting = true
                                scope.launch {
                                    val success = viewModel.acceptOrderAndWaitForUpdate(order.id, order.user_id ?: 0)
                                    if (success) {
                                        Toast.makeText(context, "Đã nhận đơn #${order.id}", Toast.LENGTH_SHORT).show()
                                        navController.navigate("myOrders") { // Chuyển sang tab "Của tôi"
                                            popUpTo("home") { inclusive = true }
                                        }
                                    } else {
                                        Toast.makeText(context, "Nhận đơn thất bại", Toast.LENGTH_SHORT).show()
                                        isAccepting = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF667eea)),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isAccepting
                    ) {
                        if (isAccepting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Đang xử lý...")
                        } else {
                            Text("NHẬN ĐƠN NGAY", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (isLocked) {
                LockedOrderWarning()
            }
        }
    }
}

// --- CÁC HÀM NHỎ DƯỚI ĐÂY GIỮ NGUYÊN ---

@Composable
private fun OrderThumbnail(thumbnailUrl: String?) {
    if (!thumbnailUrl.isNullOrEmpty()) {
        Surface(
            modifier = Modifier.size(70.dp),
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
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, color = statusStyle.textColor),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun OrderAmount(amount: Double) {
    Text(
        text = "${formatCurrency(amount)} đ",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF667eea), fontSize = 18.sp)
    )
}

@Composable
private fun OrderArrowIcon(isLocked: Boolean) {
    Icon(
        imageVector = Icons.Default.ArrowForward,
        contentDescription = if (isLocked) "Đơn hàng bị khóa" else "Xem chi tiết",
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
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF856404), fontWeight = FontWeight.Medium)
        )
    }
}

private fun shouldLockOrder(mode: String, index: Int, orders: List<OrderSummaryDto>): Boolean {
    return mode == OrderMode.SHIPPING && orders.size > 1 && index > 0 && orders[index - 1].order_status != OrderStatus.DELIVERED
}

private fun handleOrderClick(order: OrderSummaryDto, mode: String, navController: NavController, viewModel: OrdersViewModel) {
    viewModel.selectOrder(order.id)
    val route = when (mode) {
        OrderMode.PROCESSING -> "order/${order.id}" // Sẽ không gọi nếu sửa ở trên
        else -> "delivery/${order.id}"
    }
    navController.navigate(route)
}

private fun getOrderStatusStyle(status: String): OrderStatusStyle {
    return when (status) {
        OrderStatus.PROCESSING -> OrderStatusStyle("🔄", "Đang xử lý", Color(0xFFFFA726), Color(0xFFF57C00))
        OrderStatus.SHIPPING -> OrderStatusStyle("🚚", "Đang giao", Color(0xFF42A5F5), Color(0xFF1976D2))
        OrderStatus.DELIVERED -> OrderStatusStyle("✅", "Đã giao", Color(0xFF66BB6A), Color(0xFF2E7D32))
        else -> OrderStatusStyle("", status, Color.Gray, Color.Gray)
    }
}

private fun formatCurrency(amount: Double): String {
    return String.format("%,.0f", amount)
}

private data class OrderStatusStyle(val emoji: String, val label: String, val backgroundColor: Color, val textColor: Color)

object OrderStatus {
    const val PROCESSING = "processing"
    const val SHIPPING = "shipping"
    const val DELIVERED = "delivered"
}

object OrderMode {
    const val PROCESSING = "processing"
    const val SHIPPING = "shipping"
}