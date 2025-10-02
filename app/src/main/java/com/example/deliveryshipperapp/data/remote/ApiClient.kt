package com.example.deliveryshipperapp.data.remote

import com.example.deliveryshipperapp.utils.Constants
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    fun create(authInterceptor: Interceptor?=null): Retrofit{
        val logging= HttpLoggingInterceptor().apply {
            level= HttpLoggingInterceptor.Level.BODY
        }
        val builder= OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30,TimeUnit.SECONDS)
            .readTimeout(30,TimeUnit.SECONDS)

        authInterceptor?.let{ builder.addInterceptor(it)}

        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(builder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}