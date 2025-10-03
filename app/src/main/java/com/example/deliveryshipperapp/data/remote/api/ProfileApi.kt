package com.example.deliveryshipperapp.data.remote.api
import com.example.deliveryshipperapp.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.GET

interface ProfileApi {
    @GET("profile")
    suspend fun getProfile(): Response<UserDto>
}