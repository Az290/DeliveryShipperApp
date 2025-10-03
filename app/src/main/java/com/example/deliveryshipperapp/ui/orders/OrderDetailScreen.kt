package com.example.deliveryshipperapp.ui.orders

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.deliveryshipperapp.ui.map.MapScreen
import com.example.deliveryshipperapp.utils.Resource
import kotlinx.coroutines.delay

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

    // Feedback cho Accept
    LaunchedEffect(receiveOrderState) {
        when (receiveOrderState) {
            is Resource.Success -> {
                Toast.makeText(context, "Đã nhận đơn hàng thành công!", Toast.LENGTH_SHORT).show()
                delay(600)
                viewModel.resetReceiveOrderState()
                // 👉 Quay về Home
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                }
            }
            is Resource.Error -> {
                Toast.makeText(context, (receiveOrderState as Resource.Error).message ?: "Lỗi nhận đơn", Toast.LENGTH_LONG).show()
                viewModel.resetReceiveOrderState()
            }
            else -> {}
        }
    }

    // Feedback cho Delivered
    LaunchedEffect(updateOrderState) {
        when (updateOrderState) {
            is Resource.Success -> {
                Toast.makeText(context, "Đã cập nhật trạng thái đơn!", Toast.LENGTH_SHORT).show()
                delay(600)
                viewModel.resetUpdateOrderState()
                navController.popBackStack()
            }
            is Resource.Error -> {
                Toast.makeText(context, (updateOrderState as Resource.Error).message ?: "Cập nhật lỗi", Toast.LENGTH_LONG).show()
                viewModel.resetUpdateOrderState()
            }
            else -> {}
        }
    }

    LaunchedEffect(orderId) { viewModel.loadOrderDetail(orderId) }

    Scaffold { padding ->
        when (state) {
            is Resource.Loading -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }
            is Resource.Error -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Text("❌ ${(state as Resource.Error).message}")
            }
            is Resource.Success -> {
                val dto = (state as Resource.Success).data!!
                val order = dto.order
                Column(Modifier.fillMaxSize().padding(padding)) {
                    // Card thông tin
                    Card(
                        Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Đơn hàng #${order.id}", style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(6.dp))
                            Text("Trạng thái: ${order.order_status}")
                            Text("Thanh toán: ${order.payment_status}")
                            Text("Tổng tiền: ${order.total_amount} đ")
                        }
                    }

                    // Map preview
                    Box(Modifier.fillMaxWidth().height(200.dp)) {
                        MapScreen(
                            userLat = order.latitude,
                            userLng = order.longitude,
                            driverLat = order.latitude,
                            driverLng = order.longitude,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Card sản phẩm
                    Card(
                        Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Sản phẩm", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(6.dp))
                            dto.items.forEach { item ->
                                Row(
                                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${item.product_name} x${item.quantity}")
                                    Text("${item.subtotal} đ")
                                }
                                Divider()
                            }
                        }
                    }

                    // Action buttons
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        OrderActionButtons(
                            orderStatus = order.order_status,
                            orderId = order.id,
                            customerId = order.user_id,   // ✅ Thêm customerId để gọi acceptOrder đúng
                            viewModel = viewModel,
                            receiveOrderState = receiveOrderState,
                            updateOrderState = updateOrderState,
                            navController = navController
                        )
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun OrderActionButtons(
    orderStatus: String,
    orderId: Long,
    customerId: Long,
    viewModel: OrdersViewModel,
    receiveOrderState: Resource<Unit>?,
    updateOrderState: Resource<Unit>?,
    navController: NavController
) {
    when (orderStatus) {
        "processing" -> {
            Button(
                onClick = { viewModel.acceptOrder(orderId, customerId) },   // ✅ truyền cả customerId
                modifier = Modifier.fillMaxWidth(),
                enabled = receiveOrderState !is Resource.Loading
            ) {
                if (receiveOrderState is Resource.Loading) {
                    CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(8.dp))
                }
                Text("📦 Nhận đơn hàng")
            }
        }
        "shipping" -> {
            Button(
                onClick = { viewModel.markDelivered(orderId) },
                modifier = Modifier.fillMaxWidth(),
                enabled = updateOrderState !is Resource.Loading
            ) {
                if (updateOrderState is Resource.Loading) {
                    CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(8.dp))
                }
                Text("✅ Đánh dấu đã giao")
            }
        }
        "delivered" -> {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CheckCircle, "Đã giao", tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Đơn hàng đã giao thành công", color = MaterialTheme.colorScheme.primary)
            }
        }
        else -> {
            Text("Đơn hàng trạng thái: $orderStatus", Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        }
    }
}