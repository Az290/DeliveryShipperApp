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
import com.example.deliveryshipperapp.ui.orders.DeliveryScreen

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
            OrderDetailScreen(orderId = id, navController = navController)
        }
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
            DeliveryScreen(orderId, customerId, customerName, navController)
        }


        // khai bao route = "delivery/{orderId}/{customerId}/{customerName}"
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
            DeliveryScreen(orderId, customerId, customerName, navController)
        }
    }
}