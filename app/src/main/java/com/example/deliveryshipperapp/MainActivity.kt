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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    val rootNavController: NavHostController = rememberNavController()
                    NavHost(
                        rootNavController,
                        startDestination = "login"
                    ) {
                        composable("login") {
                            LoginScreen(rootNavController)
                        }
                        composable("main") {
                            MainNavGraph(rootNavController)
                        }
                    }
                }
            }
        }
    }
}