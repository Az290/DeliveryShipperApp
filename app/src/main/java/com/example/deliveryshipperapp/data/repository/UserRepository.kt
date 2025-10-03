package com.example.deliveryshipperapp.data.repository

import com.example.deliveryshipperapp.data.remote.api.UserApi
import com.example.deliveryshipperapp.data.remote.dto.UserDto
import com.example.deliveryshipperapp.utils.Resource

class UserRepository(private val api: UserApi) {
    suspend fun getUser(id: Long): Resource<UserDto> {
        return try {
            val resp = api.getUser(id)
            if (resp.isSuccessful) {
                resp.body()?.let { Resource.Success(it) } ?: Resource.Error("Empty body")
            } else {
                Resource.Error(resp.errorBody()?.string() ?: "Unknown error")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unexpected error")
        }
    }
}