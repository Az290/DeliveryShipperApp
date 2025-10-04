package com.example.deliveryshipperapp.data.remote.interceptor

import android.content.Context
import com.example.deliveryshipperapp.data.local.DataStoreManager
import com.example.deliveryshipperapp.data.remote.ApiClient
import com.example.deliveryshipperapp.data.remote.api.AuthApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.HttpException

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

        // ⚡ Nếu access token hết hạn (backend trả 401) → refresh
        if (response.code == 401) {
            response.close()
            val refreshToken = runBlocking { dataStore.refreshToken.first() }
            if (!refreshToken.isNullOrEmpty()) {
                val authApi = ApiClient.create().create(AuthApi::class.java)
                val refreshResp = runBlocking {
                    try {
                        authApi.refreshAccessToken(mapOf("refresh_token" to refreshToken))
                    } catch (e: Exception) {
                        null
                    }
                }
                if (refreshResp != null && refreshResp.isSuccessful && refreshResp.body() != null) {
                    val newTokens = refreshResp.body()!!
                    runBlocking {
                        dataStore.saveTokens(newTokens.access_token, newTokens.refresh_token)
                    }
                    val newRequest = request.newBuilder()
                        .header("Authorization", "Bearer ${newTokens.access_token}")
                        .build()
                    return chain.proceed(newRequest) // retry request cũ
                } else {
                    runBlocking { dataStore.clearTokens() }
                    throw HttpException(refreshResp!!)
                }
            }
        }

        return response
    }
}