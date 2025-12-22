package com.example.deliveryshipperapp.data.remote.interceptor

import android.content.Context
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

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        val currentAccess = runBlocking { dataStore.accessToken.first() }
        if (!currentAccess.isNullOrEmpty()) {
            request = request.newBuilder()
                .addHeader("Authorization", "Bearer $currentAccess")
                .build()
        }

        val response = chain.proceed(request)

        if (response.code == 401) {
            response.close()
            val refreshToken = runBlocking { dataStore.refreshToken.first() }

            if (!refreshToken.isNullOrEmpty()) {
                val authApi = ApiClient.create().create(AuthApi::class.java)
                val refreshResp = runBlocking {
                    try {
                        // ✅ Dùng DTO thay vì Map
                        authApi.refreshAccessToken(RefreshTokenRequestDto(refreshToken))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }

                if (refreshResp != null && refreshResp.isSuccessful && refreshResp.body() != null) {
                    val newTokens = refreshResp.body()!!

                    // ✅ Dùng helper function để lấy refresh token
                    val newRefreshToken = newTokens.getRefreshTokenValue() ?: refreshToken

                    runBlocking {
                        dataStore.saveTokens(newTokens.accessToken, newRefreshToken)
                    }

                    val newRequest = request.newBuilder()
                        .header("Authorization", "Bearer ${newTokens.accessToken}")
                        .build()
                    return chain.proceed(newRequest)
                } else {
                    runBlocking { dataStore.clearTokens() }
                }
            }
        }

        return response
    }
}