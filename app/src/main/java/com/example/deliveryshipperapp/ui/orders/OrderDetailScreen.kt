package com.example.deliveryshipperapp.ui.orders

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.deliveryshipperapp.ui.map.MapScreen
import com.example.deliveryshipperapp.utils.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: Long,
    navController: NavController,
    viewModel: OrdersViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.orderDetail.collectAsState()
    val receiveOrderState by viewModel.receiveOrderState.collectAsState()
    val updateOrderState by viewModel.updateOrderState.collectAsState()
    val isFirstOrderReceived by viewModel.isFirstOrderReceived.collectAsState()
    val scope = rememberCoroutineScope()

    // Feedback cho Accept
    LaunchedEffect(receiveOrderState) {
        when (receiveOrderState) {
            is Resource.Success -> {
                Toast.makeText(context, "✅ Đã nhận đơn hàng thành công!", Toast.LENGTH_SHORT).show()
                delay(300)
                viewModel.resetReceiveOrderState()
            }
            is Resource.Error -> {
                Toast.makeText(context, "❌ ${(receiveOrderState as Resource.Error).message}", Toast.LENGTH_LONG).show()
                viewModel.resetReceiveOrderState()
            }
            else -> {}
        }
    }

    // Feedback cho Delivered
    LaunchedEffect(updateOrderState) {
        when (updateOrderState) {
            is Resource.Success -> {
                Toast.makeText(context, "✅ Đã cập nhật trạng thái đơn!", Toast.LENGTH_SHORT).show()
                delay(600)
                viewModel.resetUpdateOrderState()
                navController.popBackStack()
            }
            is Resource.Error -> {
                Toast.makeText(context, "❌ ${(updateOrderState as Resource.Error).message}", Toast.LENGTH_LONG).show()
                viewModel.resetUpdateOrderState()
            }
            else -> {}
        }
    }

    LaunchedEffect(orderId) {
        viewModel.loadOrderFromAvailableList(orderId)
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
                                    text = "📦",
                                    fontSize = 20.sp
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Chi Tiết Đơn Hàng",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF667eea),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
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
                                text = "Đang tải chi tiết...",
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
                                text = (state as Resource.Error).message ?: "Vui lòng thử lại",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.Gray
                                )
                            )
                        }
                    }
                }

                is Resource.Success -> {
                    val dto = (state as Resource.Success).data!!
                    val order = dto.order

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Order Info Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                // Header với status badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Đơn hàng #${order.id}",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF667eea),
                                            fontSize = 22.sp
                                        )
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = when(order.order_status) {
                                            "processing" -> Color(0xFFFFA726).copy(alpha = 0.2f)
                                            "shipping" -> Color(0xFF42A5F5).copy(alpha = 0.2f)
                                            "delivered" -> Color(0xFF66BB6A).copy(alpha = 0.2f)
                                            else -> Color.Gray.copy(alpha = 0.2f)
                                        }
                                    ) {
                                        Text(
                                            text = when(order.order_status) {
                                                "processing" -> "🔄 Chờ xử lý"
                                                "shipping" -> "🚚 Đang giao"
                                                "delivered" -> "✅ Đã giao"
                                                else -> order.order_status
                                            },
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = when(order.order_status) {
                                                    "processing" -> Color(0xFFF57C00)
                                                    "shipping" -> Color(0xFF1976D2)
                                                    "delivered" -> Color(0xFF2E7D32)
                                                    else -> Color.Gray
                                                }
                                            ),
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(16.dp))
                                Divider(color = Color(0xFFEEEEEE))
                                Spacer(Modifier.height(16.dp))

                                // Order details
                                DetailInfoRow(
                                    icon = "💳",
                                    label = "Thanh toán",
                                    value = when(order.payment_status) {
                                        "paid" -> "Đã thanh toán"
                                        "pending" -> "Chờ thanh toán"
                                        else -> order.payment_status
                                    }
                                )

                                Spacer(Modifier.height(12.dp))

                                DetailInfoRow(
                                    icon = "💰",
                                    label = "Tổng tiền",
                                    value = "${String.format("%,.0f", order.total_amount)} đ",
                                            valueColor = Color(0xFF667eea)
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Map Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color(0xFF667eea).copy(alpha = 0.1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = Color(0xFF667eea),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "Vị trí giao hàng",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF667eea)
                                            )
                                        )
                                    }
                                }

                                MapScreen(
                                    userLat = order.latitude,
                                    userLng = order.longitude,
                                    driverLat = order.latitude,
                                    driverLng = order.longitude,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Products Card
                        if (dto.items.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ShoppingCart,
                                            contentDescription = null,
                                            tint = Color(0xFF667eea),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "Sản phẩm (${dto.items.size})",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF667eea)
                                            )
                                        )
                                    }

                                    Spacer(Modifier.height(16.dp))

                                    dto.items.forEachIndexed { index, item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(RoundedCornerShape(8.dp)),
                                                    color = Color(0xFF667eea).copy(alpha = 0.1f)
                                                ) {
                                                    Box(
                                                        contentAlignment = Alignment.Center,
                                                        modifier = Modifier.fillMaxSize()
                                                    ) {
                                                        Text(
                                                            text = "📦",
                                                            fontSize = 20.sp
                                                        )
                                                    }
                                                }
                                                Spacer(Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        text = item.product_name,
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    )
                                                    Text(
                                                        text = "x${item.quantity}",
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            color = Color.Gray
                                                        )
                                                    )
                                                }
                                            }

                                            Text(
                                                text = "${String.format("%,d", item.subtotal)} đ",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF667eea)
                                                )
                                            )
                                        }
                                        if (index < dto.items.size - 1) {
                                            Divider(
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                color = Color(0xFFEEEEEE)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))
                        }

                        // Action Buttons
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            when (order.order_status) {
                                "processing" -> {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                val success = viewModel.acceptOrderAndWaitForUpdate(order.id, order.user_id)
                                                if (success) {
                                                    if (!isFirstOrderReceived) {
                                                        delay(500)
                                                    } else {
                                                        delay(300)
                                                    }
                                                    navController.navigate("myOrders") {
                                                        popUpTo("home") { inclusive = true }
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF667eea)
                                        ),
                                        elevation = ButtonDefaults.buttonElevation(
                                            defaultElevation = 4.dp,
                                            pressedElevation = 8.dp
                                        ),
                                        enabled = receiveOrderState !is Resource.Loading
                                    ) {
                                        if (receiveOrderState is Resource.Loading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                color = Color.White,
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(Modifier.width(12.dp))
                                            Text(
                                                "Đang xử lý...",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(Modifier.width(12.dp))
                                            Text(
                                                "Nhận Đơn Hàng",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        }
                                    }
                                }

                                "shipping" -> {
                                    Button(
                                        onClick = { viewModel.markDelivered(order.id) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF4CAF50)
                                        ),
                                        elevation = ButtonDefaults.buttonElevation(
                                            defaultElevation = 4.dp,
                                            pressedElevation = 8.dp
                                        ),
                                        enabled = updateOrderState !is Resource.Loading
                                    ) {
                                        if (updateOrderState is Resource.Loading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                color = Color.White,
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(Modifier.width(12.dp))
                                            Text(
                                                "Đang cập nhật...",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(Modifier.width(12.dp))
                                            Text(
                                                "Đánh Dấu Đã Giao",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        }
                                    }
                                }

                                "delivered" -> {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color(0xFF4CAF50).copy(alpha = 0.15f)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(20.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                modifier = Modifier.size(48.dp),
                                                shape = CircleShape,
                                                color = Color(0xFF4CAF50).copy(alpha = 0.2f)
                                            ) {
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier.fillMaxSize()
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = Color(0xFF4CAF50),
                                                        modifier = Modifier.size(28.dp)
                                                    )
                                                }
                                            }
                                            Spacer(Modifier.width(16.dp))
                                            Column {
                                                Text(
                                                    "Hoàn thành",
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF4CAF50)
                                                    )
                                                )
                                                Text(
                                                    "Đơn hàng đã được giao thành công",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = Color(0xFF2E7D32)
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }

                                else -> {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color.Gray.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            text = "Trạng thái: ${order.order_status}",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(20.dp),
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Medium,
                                                color = Color.Gray
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun DetailInfoRow(
    icon: String,
    label: String,
    value: String,
    valueColor: Color = Color.Black
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 24.sp,
                modifier = Modifier.padding(end = 12.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                color = valueColor,
                fontSize = 16.sp
            )
        )
    }
}