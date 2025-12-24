package com.example.deliveryshipperapp.data.remote.interceptor

import android.content.Context
import android.util.Log
import com.example.deliveryshipperapp.data.local.DataStoreManager
import com.example.deliveryshipperapp.data.remote.ApiClient
import com.example.deliveryshipperapp.data.remote.api.AuthApi
import com.example.deliveryshipperapp.data.remote.dto.RefreshTokenRequestDto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val context: Context,
    private val dataStore: DataStoreManager
) : Interceptor {

    // 🔒 Tạo một object để làm khóa đồng bộ
    companion object {
        private val LOCK = Any()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        // 1. Lấy token hiện tại để gửi đi
        // Lưu lại token này vào biến local để lát nữa so sánh
        val tokenUsedInRequest = runBlocking { dataStore.accessToken.first() }

        if (!tokenUsedInRequest.isNullOrEmpty()) {
            request = request.newBuilder()
                .addHeader("Authorization", "Bearer $tokenUsedInRequest")
                .build()
        }

        val response = chain.proceed(request)

        // 2. Nếu lỗi 401 -> Cần Refresh
        if (response.code == 401) {
            response.close() // Đóng response cũ

            // 🛑 BẮT ĐẦU KHÓA ĐỒNG BỘ 🛑
            // Chỉ cho 1 luồng chạy vào đây, các luồng khác phải đợi ở ngoài
            synchronized(LOCK) {

                // 3. Kiểm tra lại token trong DataStore
                val currentTokenInStore = runBlocking { dataStore.accessToken.first() }

                // TRƯỜNG HỢP A: Có luồng khác đã refresh xong rồi!
                // (Token trong kho khác với token mình vừa dùng bị lỗi)
                if (currentTokenInStore != null && currentTokenInStore != tokenUsedInRequest) {
                    Log.d("AuthInterceptor", "♻️ Có luồng khác đã refresh giúp rồi. Dùng luôn token mới.")

                    val newRequest = request.newBuilder()
                        .removeHeader("Authorization")
                        .addHeader("Authorization", "Bearer $currentTokenInStore")
                        .build()
                    return chain.proceed(newRequest)
                }

                // TRƯỜNG HỢP B: Chưa ai refresh cả, mình phải tự làm
                val refreshToken = runBlocking { dataStore.refreshToken.first() }

                if (!refreshToken.isNullOrEmpty()) {
                    Log.d("AuthInterceptor", "🔒 Đang Refresh Token (Độc quyền)...")

                    val authApi = ApiClient.createPublic().create(AuthApi::class.java)
                    val refreshResp = runBlocking {
                        try {
                            authApi.refreshAccessToken(RefreshTokenRequestDto(refreshToken))
                        } catch (e: Exception) {
                            null
                        }
                    }

                    if (refreshResp != null && refreshResp.isSuccessful && refreshResp.body() != null) {
                        val newTokens = refreshResp.body()!!
                        val newAccess = newTokens.accessToken
                        val newRefresh = newTokens.getRefreshTokenValue() ?: refreshToken

                        runBlocking { dataStore.saveTokens(newAccess, newRefresh) }

                        Log.d("AuthInterceptor", "✅ Refresh thành công!")

                        val newRequest = request.newBuilder()
                            .removeHeader("Authorization")
                            .addHeader("Authorization", "Bearer $newAccess")
                            .build()
                        return chain.proceed(newRequest)
                    } else {
                        Log.e("AuthInterceptor", "❌ Refresh thất bại. Logout.")
                        runBlocking { dataStore.clearTokens() }
                    }
                }
            } // 🛑 KẾT THÚC KHÓA
        }

        return response
    }
}