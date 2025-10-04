package com.example.deliveryshipperapp.data.remote.api

import com.example.deliveryshipperapp.data.remote.dto.AuthResponseDto
import com.example.deliveryshipperapp.data.remote.dto.LoginRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("login")
    suspend fun login(@Body req: LoginRequestDto): Response<AuthResponseDto>

    // ✅ thêm API refresh token để SplashScreen dùng
    @POST("refresh-access-token")
    suspend fun refreshAccessToken(@Body body: Map<String, String>): Response<AuthResponseDto>
}