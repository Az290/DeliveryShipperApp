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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.deliveryshipperapp.ui.map.MapScreen
import com.example.deliveryshipperapp.utils.Resource
import kotlinx.coroutines.delay
import com.example.deliveryshipperapp.ui.navigation.BottomNavItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryScreen(
    orderId: Long,
    navController: NavController,
    viewModel: OrdersViewModel = hiltViewModel()
) {
    val orderDetail by viewModel.orderDetail.collectAsState()
    val updateState by viewModel.updateOrderState.collectAsState()

    // Màu gradient
    val gradientColors = listOf(
        Color(0xFF667eea),
        Color(0xFF764ba2)
    )

    LaunchedEffect(orderId) { viewModel.loadOrderDetail(orderId) }

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
                                    text = "🚚",
                                    fontSize = 20.sp
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Đang Giao Hàng",
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
            when (val res = orderDetail) {
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
                                text = "Đang tải chi tiết đơn hàng...",
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
                                text = res.message ?: "Vui lòng thử lại",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.Gray
                                )
                            )
                        }
                    }
                }

                is Resource.Success -> {
                    val order = res.data!!.order

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
                                // Header
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
                                        color = Color(0xFF42A5F5).copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "🚚 Đang giao",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1976D2)
                                            ),
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(16.dp))
                                Divider(color = Color(0xFFEEEEEE))
                                Spacer(Modifier.height(16.dp))

                                // Customer Info
                                InfoRow(
                                    icon = "👤",
                                    label = "Khách hàng",
                                    value = order.user_name ?: ""
                                )

                                Spacer(Modifier.height(12.dp))

                                if (!order.phone.isNullOrEmpty()) {
                                    InfoRow(
                                        icon = "📞",
                                        label = "Số điện thoại",
                                        value = order.phone
                                    )
                                    Spacer(Modifier.height(12.dp))
                                }

                                InfoRow(
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
                                .weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column {
                                // Map Header
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
                                            text = "Vị trí khách hàng",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF667eea)
                                            )
                                        )
                                    }
                                }

                                // Map
                                MapScreen(
                                    userLat = order.latitude,
                                    userLng = order.longitude,
                                    driverLat = 10.762622,
                                    driverLng = 106.660172,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Chat Button
                            OutlinedButton(
                                onClick = {
                                    navController.navigate("chat/${order.id}/${order.user_id}/${order.user_name}")
                                },
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF667eea)
                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    width = 2.dp
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Chat",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }

                            // Call Button (optional)
                            if (!order.phone.isNullOrEmpty()) {
                                OutlinedButton(
                                    onClick = { /* TODO: Call customer */ },
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFF4CAF50)
                                    ),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(
                                        width = 2.dp
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Gọi",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Delivered Button
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
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Đã Giao Hàng Thành Công",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        // Update State Handling
                        when (updateState) {
                            is Resource.Loading -> {
                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color(0xFF4CAF50)
                                )
                            }
                            is Resource.Success -> {
                                LaunchedEffect(Unit) {
                                    Toast.makeText(
                                        navController.context,
                                        "✅ Đơn đã giao thành công!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    delay(500)
                                    viewModel.resetUpdateOrderState()
                                    navController.navigate(BottomNavItem.Home.route) {
                                        popUpTo(BottomNavItem.Home.route) { inclusive = true }
                                    }
                                }
                            }
                            is Resource.Error -> {
                                Spacer(Modifier.height(8.dp))
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF44336).copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = "❌ ${(updateState as Resource.Error).message}",
                                        color = Color(0xFFF44336),
                                        modifier = Modifier.padding(12.dp),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            else -> {}
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
fun InfoRow(
    icon: String,
    label: String,
    value: String,
    valueColor: Color = Color.Black
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            fontSize = 24.sp,
            modifier = Modifier.padding(end = 12.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            )
            Spacer(Modifier.height(4.dp))
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
}