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
import com.example.deliveryshipperapp.ui.profile.ProfileScreen
import com.example.deliveryshipperapp.ui.chat.ChatScreen
import com.example.deliveryshipperapp.ui.map.MapFullScreen
import kotlinx.coroutines.delay

@Composable
fun MainNavGraph(rootNavController: NavHostController) {
    val navController = rememberNavController()
    val ordersViewModel: OrdersViewModel = hiltViewModel()
    val currentChatOrder by ordersViewModel.currentChatOrder.collectAsState()
    val isFirstOrderReceived by ordersViewModel.isFirstOrderReceived.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
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
            composable("delivery/{id}") { backStack ->
                val id = backStack.arguments?.getString("id")?.toLong() ?: 0L
                DeliveryScreen(orderId = id, navController = navController, viewModel = ordersViewModel)
            }
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(navController = rootNavController)
            }


            composable(
                route = "map_full/{driverLat}/{driverLng}/{userLat}/{userLng}"
            ) { backStackEntry ->
                val driverLat = backStackEntry.arguments?.getString("driverLat")!!.toDouble()
                val driverLng = backStackEntry.arguments?.getString("driverLng")!!.toDouble()
                val userLat = backStackEntry.arguments?.getString("userLat")!!.toDouble()
                val userLng = backStackEntry.arguments?.getString("userLng")!!.toDouble()
                MapFullScreen(driverLat, driverLng, userLat, userLng,onBack = {
                    navController.popBackStack()
                })
            }


            // 👇 Route mới khi chuyển từ DeliveryScreen có tên khách
            composable("chat/{orderId}/{customerId}/{customerName}") { backStack ->
                val orderId = backStack.arguments?.getString("orderId")?.toLong() ?: 0L
                val customerId = backStack.arguments?.getString("customerId")?.toLong() ?: 0L
                val customerName = backStack.arguments?.getString("customerName") ?: "Khách hàng"
                ChatScreen(
                    orderId = orderId,
                    customerId = customerId,
                    accessToken = accessToken ?: "",
                    customerName = customerName
                )
            }
        }
    }
}