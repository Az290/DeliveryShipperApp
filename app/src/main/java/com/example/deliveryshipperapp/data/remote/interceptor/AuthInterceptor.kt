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

    companion object {
        private val LOCK = Any()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        // 1. Lấy token từ RAM (Nhanh)
        val tokenUsedInRequest = dataStore.getAccessTokenInstant()

        if (!tokenUsedInRequest.isNullOrEmpty()) {
            request = request.newBuilder()
                .addHeader("Authorization", "Bearer $tokenUsedInRequest")
                .build()
        }

        val response = chain.proceed(request)

        if (response.code == 401) {
            synchronized(LOCK) {
                // Kiểm tra lại token trong kho
                val currentTokenInStore = dataStore.getAccessTokenInstant()

                if (currentTokenInStore != null && currentTokenInStore != tokenUsedInRequest) {
                    Log.d("AuthInterceptor", "♻️ Dùng token mới từ luồng khác.")
                    response.close()
                    val newRequest = request.newBuilder()
                        .removeHeader("Authorization")
                        .addHeader("Authorization", "Bearer $currentTokenInStore")
                        .build()
                    return chain.proceed(newRequest)
                }

                // 🛑 SỬA LẠI: Dùng hàm lấy nhanh thay vì runBlocking
                // Code cũ gây ANR: val refreshToken = runBlocking { dataStore.refreshToken.first() }

                // ✅ Code mới (Siêu nhanh):
                val refreshToken = dataStore.getRefreshTokenInstant()

                if (!refreshToken.isNullOrEmpty()) {
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
                        response.close()

                        val newRequest = request.newBuilder()
                            .removeHeader("Authorization")
                            .addHeader("Authorization", "Bearer $newAccess")
                            .build()
                        return chain.proceed(newRequest)
                    } else {
                        Log.e("AuthInterceptor", "❌ Refresh thất bại. Logout.")
                        runBlocking { dataStore.clearTokens() }
                        // Để response 401 trôi về UI xử lý logout
                    }
                }
            }
        }
        return response
    }
}