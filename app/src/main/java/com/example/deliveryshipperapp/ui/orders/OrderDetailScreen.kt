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

    // Xử lý khi shipper nhận đơn
    LaunchedEffect(receiveOrderState) {
        when (receiveOrderState) {
            is Resource.Success -> {
                Toast.makeText(context, "Đã nhận đơn hàng thành công!", Toast.LENGTH_SHORT).show()
                delay(500)
                viewModel.resetReceiveOrderState()
                // 👉 Điều hướng sang DeliveryScreen
                navController.navigate("delivery/$orderId") {
                    popUpTo("orders") { inclusive = false }
                }
            }
            is Resource.Error -> {
                Toast.makeText(
                    context,
                    "Lỗi: ${(receiveOrderState as Resource.Error).message}",
                    Toast.LENGTH_LONG
                ).show()
                viewModel.resetReceiveOrderState()
            }
            else -> {}
        }
    }

    // Xử lý khi cập nhật đơn (đánh dấu giao)
    LaunchedEffect(updateOrderState) {
        when (updateOrderState) {
            is Resource.Success -> {
                Toast.makeText(context, "Đã cập nhật trạng thái đơn!", Toast.LENGTH_SHORT).show()
                delay(500)
                viewModel.resetUpdateOrderState()
                navController.popBackStack()
            }
            is Resource.Error -> {
                Toast.makeText(
                    context,
                    "Lỗi: ${(updateOrderState as Resource.Error).message}",
                    Toast.LENGTH_LONG
                ).show()
                viewModel.resetUpdateOrderState()
            }
            else -> {}
        }
    }

    // Load chi tiết đơn
    LaunchedEffect(orderId) {
        viewModel.loadOrderDetail(orderId)
    }

    Scaffold { padding ->
        when (state) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            is Resource.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("❌ ${(state as Resource.Error).message}")
                }
            }

            is Resource.Success -> {
                val dto = (state as Resource.Success).data!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Card thông tin đơn
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Đơn hàng #${dto.order.id}", style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(8.dp))
                            Text("Trạng thái: ${dto.order.order_status}")
                            Text("Thanh toán: ${dto.order.payment_status}")
                            Text("Tổng tiền: ${dto.order.total_amount} đ")
                        }
                    }

                    // Bản đồ (preview khách hàng & shipper)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        MapScreen(
                            userLat = dto.order.latitude,
                            userLng = dto.order.longitude,
                            driverLat = dto.order.latitude,
                            driverLng = dto.order.longitude,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)   // đặt chiều cao tuỳ ý
                        )
                    }

                    // Card sản phẩm
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Sản phẩm", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
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

                    // Nút hành động
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        OrderActionButtons(
                            orderStatus = dto.order.order_status,
                            orderId = dto.order.id,
                            viewModel = viewModel,
                            receiveOrderState = receiveOrderState,
                            updateOrderState = updateOrderState
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
    viewModel: OrdersViewModel,
    receiveOrderState: Resource<Unit>?,
    updateOrderState: Resource<Unit>?
) {
    when (orderStatus) {
        "processing" -> {
            Button(
                onClick = { viewModel.acceptOrder(orderId) },
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
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Đã giao",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text("Đơn hàng đã giao thành công", color = MaterialTheme.colorScheme.primary)
            }
        }
        else -> {
            Text(
                "Đơn hàng đang trong trạng thái $orderStatus",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}