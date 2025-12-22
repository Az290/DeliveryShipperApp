// data/remote/api/AuthApi.kt
package com.example.deliveryshipperapp.data.remote.api

import com.example.deliveryshipperapp.data.remote.dto.AuthResponseDto
import com.example.deliveryshipperapp.data.remote.dto.LoginRequestDto
import com.example.deliveryshipperapp.data.remote.dto.RefreshTokenRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("login")
    suspend fun login(@Body req: LoginRequestDto): Response<AuthResponseDto>

    @POST("refresh-access-token")
    suspend fun refreshAccessToken(@Body body: RefreshTokenRequestDto): Response<AuthResponseDto>
}