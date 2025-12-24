package com.example.deliveryshipperapp.data.remote

import com.example.deliveryshipperapp.utils.Constants
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val TIMEOUT = 30L

    // 1. Hàm tạo Retrofit linh hoạt (Giữ nguyên logic cũ để không lỗi DI)
    // Nếu truyền authInterceptor vào -> Nó là Authenticated Client
    fun create(authInterceptor: Interceptor? = null): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val builder = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)

        // Chỉ thêm AuthInterceptor nếu được truyền vào
        authInterceptor?.let { builder.addInterceptor(it) }

        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(builder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 2. THÊM HÀM MỚI: Client Public (Không bao giờ có AuthInterceptor)
    // Dùng riêng cho việc Login và Refresh Token để tránh vòng lặp
    fun createPublic(): Retrofit {
        return create(null)
    }
}