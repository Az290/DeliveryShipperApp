package com.example.deliveryshipperapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.deliveryshipperapp.ui.orders.OrdersListScreen
import com.example.deliveryshipperapp.ui.orders.OrdersViewModel
import com.example.deliveryshipperapp.ui.orders.DeliveryScreen
import com.example.deliveryshipperapp.ui.orders.OrderDetailScreen
import com.example.deliveryshipperapp.ui.profile.ProfileScreen
import com.example.deliveryshipperapp.ui.chat.ChatScreen
import com.example.deliveryshipperapp.utils.Constants
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.material3.Text


@Composable
fun MainNavGraph(rootNavController: NavHostController) {
    val navController = rememberNavController()
    val ordersViewModel: OrdersViewModel = hiltViewModel()

    // State để toggle tab Chat xuất hiện hay không
    val currentChatOrder by ordersViewModel.currentChatOrder.collectAsState()

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
                OrdersListScreen(navController, viewModel = ordersViewModel, mode = "processing")
            }
            // Shipping: đơn đã nhận
            composable(BottomNavItem.MyOrders.route) {
                OrdersListScreen(navController, viewModel = ordersViewModel, mode = "shipping")
            }
            // Order detail (truy cập từ Home)
            composable("order/{id}") { backStack ->
                val id = backStack.arguments?.getString("id")?.toLong() ?: 0L
                OrderDetailScreen(orderId = id, navController = navController, viewModel = ordersViewModel)
            }
            // Delivery screen (vào khi click đơn shipping)
            composable("delivery/{id}") { backStack ->
                val id = backStack.arguments?.getString("id")?.toLong() ?: 0L
                DeliveryScreen(orderId = id, navController = navController, viewModel = ordersViewModel)
            }
            // Profile screen
            composable(BottomNavItem.Profile.route) {
                ProfileScreen()
            }
            // Chat screen: chỉ hiển thị khi có đơn shipping
            composable(BottomNavItem.Chat.route) {
                if (currentChatOrder != null) {
                    val (orderId, customerId) = currentChatOrder!!
                    // TODO: lấy JWT thật từ DataStore (front bạn đã có DataStoreManager)
                    val fakeAccess = "ACCESS_TOKEN"
                    ChatScreen(orderId = orderId, customerId = customerId, accessToken = fakeAccess)
                } else {
                    Text("Chưa có đơn đang giao để chat")
                }
            }
        }
    }
}