package com.example.deliveryshipperapp.data.remote.api

import com.example.deliveryshipperapp.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface UserApi {
    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: Long): Response<UserDto>
}