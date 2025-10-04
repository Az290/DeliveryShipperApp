package com.example.deliveryshipperapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.deliveryshipperapp.ui.auth.LoginScreen
import com.example.deliveryshipperapp.ui.navigation.MainNavGraph
import com.example.deliveryshipperapp.ui.splash.SplashScreen    // 👈 thêm import splash
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    val rootNavController: NavHostController = rememberNavController()

                    // ✅ NavHost thêm Splash là màn đầu tiên,
                    // sau đó điều hướng tới Login hoặc Main tuỳ token
                    NavHost(
                        navController = rootNavController,
                        startDestination = "splash"
                    ) {
                        // 🌟 Màn Splash kiểm tra token
                        composable("splash") {
                            SplashScreen(rootNavController)
                        }

                        // 🌟 Màn Login cũ — giữ nguyên
                        composable("login") {
                            LoginScreen(rootNavController)
                        }

                        // 🌟 Màn Main (OrderList, Delivery ...), giữ nguyên toàn bộ logic
                        composable("main") {
                            MainNavGraph(rootNavController)
                        }
                    }
                }
            }
        }
    }
}