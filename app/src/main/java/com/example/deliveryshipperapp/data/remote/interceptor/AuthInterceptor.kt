package com.example.deliveryshipperapp.data.remote.interceptor

import com.example.deliveryshipperapp.data.local.DataStoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val dataStore: DataStoreManager
): Interceptor{
    override fun intercept(chain: Interceptor.Chain): Response {
        val token= runBlocking{ dataStore.accessToken.first() }
        val req= if(token!=null){
            chain.request().newBuilder()
                .addHeader("Authorization","Bearer $token").build()
        } else chain.request()
        return chain.proceed(req)
    }
}