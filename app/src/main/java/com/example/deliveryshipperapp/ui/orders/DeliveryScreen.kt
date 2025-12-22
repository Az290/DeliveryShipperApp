package com.example.deliveryshipperapp.ui.orders

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.deliveryshipperapp.ui.chat.ChatViewModel
import com.example.deliveryshipperapp.ui.map.MapScreen
import com.example.deliveryshipperapp.utils.Resource
import kotlinx.coroutines.delay
import com.example.deliveryshipperapp.ui.navigation.BottomNavItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryScreen(
    orderId: Long,
    navController: NavController,
    viewModel: OrdersViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel()
) {
    val orderDetail by viewModel.orderDetail.collectAsState()
    val updateState by viewModel.updateOrderState.collectAsState()

    LaunchedEffect(orderId) {
        viewModel.loadOrderDetail(orderId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(36.dp).clip(CircleShape),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "🚚", fontSize = 20.sp)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Đang Giao Hàng",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                // ✅ NÚT BACK (Đã có sẵn ở đây)
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
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
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val res = orderDetail) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF667eea))
                    }
                }
                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = res.message ?: "Lỗi tải dữ liệu", color = Color.Red)
                    }
                }
                is Resource.Success -> {
                    val orderData = res.data
                    if (orderData != null) {
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            // Card thông tin đơn hàng
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Đơn hàng #${orderData.id}",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold, color = Color(0xFF667eea)
                                            )
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFF42A5F5).copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "🚚 Đang giao",
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(16.dp))

                                    // 🛑 ĐÃ SỬA: Dùng Divider (thay vì HorizontalDivider) để tương thích bản cũ
                                    Divider(color = Color(0xFFEEEEEE))

                                    Spacer(Modifier.height(16.dp))

                                    InfoRow(icon = "👤", label = "Khách hàng", value = orderData.user_name ?: "N/A")
                                    Spacer(Modifier.height(12.dp))

                                    if (!orderData.phone.isNullOrEmpty()) {
                                        InfoRow(icon = "📞", label = "Số điện thoại", value = orderData.phone)
                                        Spacer(Modifier.height(12.dp))
                                    }

                                    InfoRow(
                                        icon = "💰",
                                        label = "Tổng tiền",
                                        value = "${String.format("%,.0f", orderData.total_amount)} đ",
                                        valueColor = Color(0xFF667eea)
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            // Bản đồ
                            Card(
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                shape = RoundedCornerShape(20.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column {
                                    Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFF667eea).copy(alpha = 0.1f)) {
                                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.LocationOn, null, tint = Color(0xFF667eea))
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = "Vị trí khách hàng",
                                                modifier = Modifier.clickable {
                                                    navController.navigate("map_full/10.762622/106.660172/${orderData.latitude}/${orderData.longitude}")
                                                },
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF667eea))
                                            )
                                        }
                                    }
                                    MapScreen(
                                        userLat = orderData.latitude,
                                        userLng = orderData.longitude,
                                        driverLat = 10.762622,
                                        driverLng = 106.660172,
                                        modifier = Modifier.fillMaxWidth().weight(1f)
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            // Buttons
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        navController.navigate("chat/${orderData.id}/${orderData.user_id}/${orderData.user_name ?: "Customer"}")
                                    },
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Icon(Icons.Default.Chat, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Chat", fontWeight = FontWeight.Bold)
                                }

                                if (!orderData.phone.isNullOrEmpty()) {
                                    OutlinedButton(
                                        onClick = { /* Call logic */ },
                                        modifier = Modifier.weight(1f).height(56.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4CAF50))
                                    ) {
                                        Icon(Icons.Default.Phone, null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Gọi", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            Button(
                                onClick = { viewModel.markDelivered(orderData.id) },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                            ) {
                                Icon(Icons.Default.CheckCircle, null)
                                Spacer(Modifier.width(12.dp))
                                Text("Đã Giao Thành Công", fontWeight = FontWeight.Bold)
                            }

                            // Xử lý trạng thái update
                            when (updateState) {
                                is Resource.Success -> {
                                    LaunchedEffect(Unit) {
                                        chatViewModel.clearConversation(orderData.id)
                                        Toast.makeText(navController.context, "✅ Thành công!", Toast.LENGTH_SHORT).show()
                                        delay(500)
                                        viewModel.resetUpdateOrderState()
                                        navController.navigate(BottomNavItem.Home.route) {
                                            popUpTo(BottomNavItem.Home.route) { inclusive = true }
                                        }
                                    }
                                }
                                is Resource.Error -> {
                                    Text(text = (updateState as Resource.Error).message ?: "Lỗi", color = Color.Red)
                                }
                                else -> {}
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun InfoRow(icon: String, label: String, value: String, valueColor: Color = Color.Black) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = icon, fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
            Text(text = value, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = valueColor))
        }
    }
}