package com.example.deliveryshipperapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "Home", Icons.Default.Home)
    object MyOrders : BottomNavItem("myOrders", "Đơn của tôi", Icons.Default.List)
    object Profile : BottomNavItem("profile", "Profile", Icons.Default.Person)
    object Chat : BottomNavItem("chat", "Chat", Icons.Default.Chat)
}