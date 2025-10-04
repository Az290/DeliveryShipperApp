package com.example.deliveryshipperapp.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.deliveryshipperapp.R
import com.example.deliveryshipperapp.data.local.DataStoreManager
import com.example.deliveryshipperapp.data.remote.ApiClient
import com.example.deliveryshipperapp.data.remote.api.AuthApi
import com.example.deliveryshipperapp.data.remote.dto.AuthResponseDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

@Composable
fun SplashScreen(navController: NavHostController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val dataStore = remember { DataStoreManager(context) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        // Hiển thị splash ít nhất 1 giây để load hình đẹp hơn
        delay(1000)

        val accessToken = dataStore.accessToken.first()
        val refreshToken = dataStore.refreshToken.first()

        // Nếu chưa có token thì đi thẳng login
        if (accessToken.isNullOrEmpty() && refreshToken.isNullOrEmpty()) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
            return@LaunchedEffect
        }

        val authApi = ApiClient.create().create(AuthApi::class.java)
        try {
            // Gửi refresh token để đảm bảo access token còn hạn
            val response = authApi.refreshAccessToken(mapOf("refresh_token" to (refreshToken ?: "")))

            if (response.isSuccessful && response.body() != null) {
                val tokens: AuthResponseDto = response.body()!!
                scope.launch {
                    dataStore.saveTokens(tokens.access_token, tokens.refresh_token)
                }
                navController.navigate("main") {
                    popUpTo(0) { inclusive = true }
                }
            } else {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        } catch (e: IOException) {
            navController.navigate("login") { popUpTo(0) { inclusive = true } }
        } catch (e: HttpException) {
            navController.navigate("login") { popUpTo(0) { inclusive = true } }
        }
    }

    // Giao diện Splash
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_delivery_truck), // Thêm ảnh này vào drawable
                contentDescription = "Splash Logo",
                modifier = Modifier.size(160.dp)
            )
            Spacer(modifier = Modifier.height(40.dp))
            LinearProgressIndicator(
                modifier = Modifier.width(200.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}