package com.example.deliveryshipperapp.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.deliveryshipperapp.data.local.DataStoreManager
import com.example.deliveryshipperapp.data.remote.ApiClient
import com.example.deliveryshipperapp.data.remote.api.AuthApi
import com.example.deliveryshipperapp.data.remote.dto.AuthResponseDto
import com.example.deliveryshipperapp.data.remote.dto.RefreshTokenRequestDto
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

    // Animation cho logo
    val infiniteTransition = rememberInfiniteTransition(label = "splash_animation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_animation"
    )

    // Màu gradient
    val gradientColors = listOf(
        Color(0xFF667eea),
        Color(0xFF764ba2),
        Color(0xFFf093fb)
    )

    LaunchedEffect(Unit) {
        // Hiển thị splash ít nhất 1 giây để load hình đẹp hơn
        delay(1000)

        // Lấy token từ bộ nhớ
        val accessToken = dataStore.accessToken.first()
        val refreshToken = dataStore.refreshToken.first()

        // 🛑 FIX 1: KIỂM TRA CHẶT CHẼ
        // Nếu không có Refresh Token (hoặc rỗng do lỗi cũ) -> Bắt đăng nhập lại ngay
        // (Tránh việc gửi chuỗi rỗng lên Server gây lỗi 400 Bad Request)
        if (refreshToken.isNullOrEmpty()) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
            return@LaunchedEffect
        }

        // Tạo API Client
        val authApi = ApiClient.create().create(AuthApi::class.java)

        try {
            // Gửi refresh token lên server (Lúc này chắc chắn refreshToken không rỗng)
            val response = authApi.refreshAccessToken(RefreshTokenRequestDto(refreshToken))

            if (response.isSuccessful && response.body() != null) {
                val tokens: AuthResponseDto = response.body()!!

                scope.launch {
                    // 🛑 FIX 2: XỬ LÝ LỖI CHÍNH TẢ TỪ SERVER
                    // Dùng hàm getRefreshTokenValue() để lấy đúng token dù server trả về 'refersh_token' hay 'refresh_token'
                    val finalRefreshToken = tokens.getRefreshTokenValue() ?: ""

                    dataStore.saveTokens(tokens.accessToken, finalRefreshToken)
                }

                // Vào màn hình chính
                navController.navigate("main") {
                    popUpTo(0) { inclusive = true }
                }
            } else {
                // Token hết hạn hoặc không hợp lệ -> Login
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        } catch (e: IOException) {
            // Lỗi mạng -> Login
            navController.navigate("login") { popUpTo(0) { inclusive = true } }
        } catch (e: HttpException) {
            // Lỗi server -> Login
            navController.navigate("login") { popUpTo(0) { inclusive = true } }
        } catch (e: Exception) {
            // Các lỗi khác
            navController.navigate("login") { popUpTo(0) { inclusive = true } }
        }
    }

    // --- PHẦN GIAO DIỆN (GIỮ NGUYÊN) ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors
                )
            )
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Logo với animation
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(scale),
                contentAlignment = Alignment.Center
            ) {
                // Background circle cho logo
                androidx.compose.foundation.Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.2f)
                    )
                }

                // Icon truck hoặc emoji
                Text(
                    text = "🚚",
                    fontSize = 80.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Tên ứng dụng
            Text(
                text = "Shipper App",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 36.sp,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Slogan
            Text(
                text = "Giao hàng nhanh chóng & tiện lợi",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Progress indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    modifier = Modifier.width(200.dp),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Đang tải...",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                )
            }
        }

        // Footer version
        Text(
            text = "Phiên bản 1.0.0",
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}