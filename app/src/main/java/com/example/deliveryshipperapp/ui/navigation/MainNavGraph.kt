package com.example.deliveryshipperapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.deliveryshipperapp.data.local.DataStoreManager
import com.example.deliveryshipperapp.ui.orders.OrdersListScreen
import com.example.deliveryshipperapp.ui.orders.OrdersViewModel
import com.example.deliveryshipperapp.ui.orders.DeliveryScreen
import com.example.deliveryshipperapp.ui.orders.OrderDetailScreen
import com.example.deliveryshipperapp.ui.profile.ProfileScreen
import com.example.deliveryshipperapp.ui.chat.ChatScreen
import kotlinx.coroutines.delay

@Composable
fun MainNavGraph(rootNavController: NavHostController) {
    val navController = rememberNavController()
    val ordersViewModel: OrdersViewModel = hiltViewModel()
    val currentChatOrder by ordersViewModel.currentChatOrder.collectAsState()
    val isFirstOrderReceived by ordersViewModel.isFirstOrderReceived.collectAsState()

    val context = LocalContext.current
    val dataStore = remember { DataStoreManager(context) }
    val accessToken by dataStore.accessToken.collectAsState(initial = "")

    Scaffold(
        bottomBar = { BottomNavigationBar(navController, showChat = false) }
    ) { padding ->
        NavHost(
            navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(BottomNavItem.Home.route) {
                LaunchedEffect(Unit) { ordersViewModel.loadAvailableOrders() }
                OrdersListScreen(navController, viewModel = ordersViewModel, mode = "processing")
            }
            composable(BottomNavItem.MyOrders.route) {
                LaunchedEffect(Unit, isFirstOrderReceived) {
                    if (isFirstOrderReceived) delay(800) else delay(300)
                    ordersViewModel.loadMyOrders()
                }
                OrdersListScreen(navController, viewModel = ordersViewModel, mode = "shipping")
            }
            composable(
                route = "order/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStack ->
                val id = backStack.arguments?.getLong("id") ?: 0L
                OrderDetailScreen(orderId = id, navController = navController, viewModel = ordersViewModel)
            }
            // 2️⃣ Route đầy đủ thông tin khách hàng
            composable(
                route = "delivery/{orderId}/{customerId}/{customerName}",
                arguments = listOf(
                    navArgument("orderId") { type = NavType.LongType },
                    navArgument("customerId") { type = NavType.LongType },
                    navArgument("customerName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getLong("orderId") ?: 0L
                val customerId = backStackEntry.arguments?.getLong("customerId") ?: 0L
                val customerName = backStackEntry.arguments?.getString("customerName") ?: ""
                DeliveryScreen(
                    orderId = orderId,
                    customerId = customerId,
                    customerName = customerName,
                    navController = navController,
                    viewModel = ordersViewModel
                )
            }

            composable(BottomNavItem.Profile.route) {
                ProfileScreen(navController = rootNavController)
            }

            // 👇 Route mới khi chuyển từ DeliveryScreen có tên khách (duplicate đã xóa)
            composable(
                route = "chat/{orderId}/{customerId}/{customerName}",
                arguments = listOf(
                    navArgument("orderId") { type = NavType.LongType },
                    navArgument("customerId") { type = NavType.LongType },
                    navArgument("customerName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getLong("orderId") ?: 0L
                val customerId = backStackEntry.arguments?.getLong("customerId") ?: 0L
                val customerName = backStackEntry.arguments?.getString("customerName") ?: "Khách hàng"

                ChatScreen(
                    navController = navController,
                    orderId = orderId,
                    customerId = customerId,
                    customerName = customerName
                )
            }
        }
    }
}