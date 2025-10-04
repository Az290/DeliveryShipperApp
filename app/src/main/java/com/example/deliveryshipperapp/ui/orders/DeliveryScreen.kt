package com.example.deliveryshipperapp.ui.orders

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.deliveryshipperapp.ui.map.MapScreen
import com.example.deliveryshipperapp.utils.Resource
import kotlinx.coroutines.delay
import com.example.deliveryshipperapp.ui.navigation.BottomNavItem

@Composable
fun DeliveryScreen(
    orderId: Long,
    navController: NavController,
    viewModel: OrdersViewModel = hiltViewModel()
) {
    val orderDetail by viewModel.orderDetail.collectAsState()
    val updateState by viewModel.updateOrderState.collectAsState()

    LaunchedEffect(orderId) { viewModel.loadOrderDetail(orderId) }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (val res = orderDetail) {
                is Resource.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is Resource.Error -> Text("❌ ${res.message}", Modifier.align(Alignment.Center))

                is Resource.Success -> {
                    val order = res.data!!.order

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Đơn hàng #${order.id}",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Text("Khách hàng: ${order.user_name}")
                        if (!order.phone.isNullOrEmpty())
                            Text("SĐT: ${order.phone}")
                        Text("Tổng tiền: ${order.total_amount} đ")

                        Spacer(Modifier.height(16.dp))

                        MapScreen(
                            userLat = order.latitude,
                            userLng = order.longitude,
                            driverLat = 10.762622,
                            driverLng = 106.660172,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(450.dp)
                        )

                        Spacer(Modifier.height(20.dp))

                        // Nút chat để mở màn Chat
                        Button(
                            onClick = {
                                navController.navigate("chat/${order.id}/${order.user_id}/${order.user_name}")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) { Text("💬 Chat với khách hàng") }

                        Spacer(Modifier.height(12.dp))

                        // Nút đánh dấu đã giao
                        Button(
                            onClick = { viewModel.markDelivered(order.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("✅ Đánh dấu đã giao")
                        }

                        when (updateState) {
                            is Resource.Success -> {
                                LaunchedEffect(Unit) {
                                    Toast.makeText(
                                        navController.context,
                                        "Đơn đã giao thành công",
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
                                Text("❌ ${(updateState as Resource.Error).message}")
                            }
                            is Resource.Loading -> {
                                LinearProgressIndicator(Modifier.fillMaxWidth())
                            }
                            else -> {}
                        }
                    }
                }
                else -> {}
            }
        }
    }
}