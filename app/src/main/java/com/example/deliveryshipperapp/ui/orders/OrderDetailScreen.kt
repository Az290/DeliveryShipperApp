package com.example.deliveryshipperapp.ui.orders

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Place  // Thay Map bằng Place icon
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

@OptIn(ExperimentalMaterial3Api::class) // Thêm annotation này để tránh cảnh báo
@Composable
fun OrderDetailScreen(
    orderId: Long,
    navController: NavController? = null,
    viewModel: OrdersViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.orderDetail.collectAsState()
    val receiveOrderState by viewModel.receiveOrderState.collectAsState()
    val updateOrderState by viewModel.updateOrderState.collectAsState()

    // Xử lý trạng thái nhận đơn
    LaunchedEffect(receiveOrderState) {
        when (receiveOrderState) {
            is Resource.Success -> {
                Toast.makeText(context, "Đã nhận đơn hàng thành công!", Toast.LENGTH_SHORT).show()
                delay(1000)
                viewModel.resetReceiveOrderState()
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

    // Xử lý trạng thái cập nhật đơn
    LaunchedEffect(updateOrderState) {
        when (updateOrderState) {
            is Resource.Success -> {
                Toast.makeText(context, "Đã cập nhật trạng thái đơn hàng!", Toast.LENGTH_SHORT).show()
                delay(1000)
                viewModel.resetUpdateOrderState()
                // Quay lại màn hình danh sách đơn hàng
                navController?.popBackStack()
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

    LaunchedEffect(orderId) {
        viewModel.loadOrderDetail(orderId)
    }

    Scaffold { padding ->
        when (state) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
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
                    // Thông tin đơn hàng
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Đơn hàng #${dto.order.id}",
                                style = MaterialTheme.typography.headlineSmall
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Trạng thái đơn hàng
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Trạng thái: ")
                                val statusColor = when (dto.order.order_status) {
                                    "pending" -> MaterialTheme.colorScheme.tertiary
                                    "processing" -> MaterialTheme.colorScheme.primary
                                    "shipping" -> MaterialTheme.colorScheme.secondary
                                    "delivered" -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.error
                                }

                                Surface(
                                    color = statusColor.copy(alpha = 0.1f),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = when (dto.order.order_status) {
                                            "pending" -> "Chờ xử lý"
                                            "processing" -> "Đang chuẩn bị"
                                            "shipping" -> "Đang giao"
                                            "delivered" -> "Đã giao"
                                            else -> dto.order.order_status
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        color = statusColor
                                    )
                                }
                            }

                            Text("Thanh toán: ${dto.order.payment_status}")
                            Text("Tổng tiền: ${dto.order.total_amount} đ")
                        }
                    }

                    // Bản đồ
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
                            onBackClick = { navController?.popBackStack() }
                        )
                    }

                    // Danh sách sản phẩm
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Sản phẩm", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))

                            dto.items.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
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
                            updateOrderState = updateOrderState,
                            latitude = dto.order.latitude,
                            longitude = dto.order.longitude
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
    updateOrderState: Resource<Unit>?,
    latitude: Double,
    longitude: Double
) {
    val context = LocalContext.current

    when (orderStatus) {
        "processing" -> {
            Button(
                onClick = { viewModel.acceptOrder(orderId) },
                modifier = Modifier.fillMaxWidth(),
                enabled = receiveOrderState !is Resource.Loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (receiveOrderState is Resource.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("📦 Nhận đơn hàng")
                }
            }
        }
        "shipping" -> {
            Column {
                Button(
                    onClick = { viewModel.markDelivered(orderId) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = updateOrderState !is Resource.Loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (updateOrderState is Resource.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("✅ Đánh dấu đã giao")
                    }
                }

                // Thêm nút mở bản đồ chỉ đường
                OutlinedButton(
                    onClick = {
                        val gmmIntentUri = Uri.parse("google.navigation:q=$latitude,$longitude")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")

                        // Kiểm tra xem có ứng dụng Maps không
                        if (mapIntent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(mapIntent)
                        } else {
                            // Backup URI nếu không có Google Maps
                            val uri = Uri.parse("https://maps.google.com/?q=$latitude,$longitude")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Place,  // Sử dụng Place thay vì Map
                            contentDescription = "Chỉ đường"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Chỉ đường đến khách hàng")
                    }
                }
            }
        }
        "delivered" -> {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Đã giao",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "✅ Đơn hàng đã giao thành công",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        else -> {
            Text(
                "Đơn hàng đang trong trạng thái $orderStatus",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}