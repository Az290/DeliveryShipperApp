package com.example.deliveryshipperapp.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(
    navController: NavController,
    showChat: Boolean
) {
    val items = if (showChat) {
        listOf(BottomNavItem.Home, BottomNavItem.MyOrders, BottomNavItem.Chat, BottomNavItem.Profile)
    } else {
        listOf(BottomNavItem.Home, BottomNavItem.MyOrders, BottomNavItem.Profile)
    }

    NavigationBar {
        val navBackStackEntry = navController.currentBackStackEntryAsState().value
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                label = { Text(item.label) },
                icon = { Icon(item.icon, contentDescription = item.label) }
            )
        }
    }
}