package com.example.deliveryshipperapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.deliveryshipperapp.data.local.DataStoreManager
import com.example.deliveryshipperapp.ui.orders.OrdersListScreen
import com.example.deliveryshipperapp.ui.orders.OrdersViewModel
import com.example.deliveryshipperapp.ui.orders.DeliveryScreen
import com.example.deliveryshipperapp.ui.orders.OrderDetailScreen
import com.example.deliveryshipperapp.ui.profile.ProfileScreen
import com.example.deliveryshipperapp.ui.chat.ChatScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MainNavGraph(rootNavController: NavHostController) {
    val navController = rememberNavController()
    val ordersViewModel: OrdersViewModel = hiltViewModel()

    // ✅ trạng thái hiện tại của đơn để bật tắt Chat tab
    val currentChatOrder by ordersViewModel.currentChatOrder.collectAsState()
    val isFirstOrderReceived by ordersViewModel.isFirstOrderReceived.collectAsState()

    // ✅ lấy accessToken từ DataStore
    val context = androidx.compose.ui.platform.LocalContext.current
    val dataStore = remember { DataStoreManager(context) }
    val accessToken by dataStore.accessToken.collectAsState(initial = "")

    Scaffold(
        bottomBar = { BottomNavigationBar(navController, showChat = (currentChatOrder != null)) }
    ) { padding ->
        NavHost(
            navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            // Home: đơn đang processing
            composable(BottomNavItem.Home.route) {
                LaunchedEffect(Unit) {
                    // Force refresh khi vào tab Home
                    ordersViewModel.loadAvailableOrders()
                }
                OrdersListScreen(navController, viewModel = ordersViewModel, mode = "processing")
            }
            // MyOrders: đơn shipper đã nhận (shipping)
            composable(BottomNavItem.MyOrders.route) {
                // Sử dụng cả Unit và trạng thái isFirstOrderReceived để trigger LaunchedEffect
                LaunchedEffect(Unit, isFirstOrderReceived) {
                    // Thêm delay dài hơn nếu là đơn đầu tiên
                    if (isFirstOrderReceived) {
                        delay(800)
                    } else {
                        delay(300)
                    }
                    // Force refresh khi vào tab Đơn của tôi
                    ordersViewModel.loadMyOrders()
                }
                OrdersListScreen(navController, viewModel = ordersViewModel, mode = "shipping")
            }
            // Order detail (từ Home)
            composable("order/{id}") { backStack ->
                val id = backStack.arguments?.getString("id")?.toLong() ?: 0L
                OrderDetailScreen(orderId = id, navController = navController, viewModel = ordersViewModel)
            }
            // Delivery screen (vào từ MyOrders)
            composable("delivery/{id}") { backStack ->
                val id = backStack.arguments?.getString("id")?.toLong() ?: 0L
                DeliveryScreen(orderId = id, navController = navController, viewModel = ordersViewModel)
            }
            // Profile
            composable(BottomNavItem.Profile.route) {
                ProfileScreen()
            }
            // Chat screen
            composable(BottomNavItem.Chat.route) {
                if (currentChatOrder != null) {
                    val (orderId, customerId) = currentChatOrder!!
                    if (!accessToken.isNullOrEmpty()) {
                        ChatScreen(orderId = orderId, customerId = customerId, accessToken = accessToken!!)
                    } else {
                        Text("🚫 Không tìm thấy access token, bạn cần đăng nhập lại")
                    }
                } else {
                    Text("Chưa có đơn shipping để chat")
                }
            }
        }
    }
}