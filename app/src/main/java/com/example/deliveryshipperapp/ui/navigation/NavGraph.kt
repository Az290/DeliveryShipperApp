package com.example.deliveryshipperapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.deliveryshipperapp.ui.auth.LoginScreen
import com.example.deliveryshipperapp.ui.orders.OrderDetailScreen
import com.example.deliveryshipperapp.ui.orders.OrdersListScreen

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("orders") { OrdersListScreen(navController) }
        composable(
            "order/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStack ->
            val id = backStack.arguments?.getLong("id") ?: 0L
            OrderDetailScreen(orderId = id, navController = navController)  // Truyền navController
        }
    }
}