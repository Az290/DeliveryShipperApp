package com.example.deliveryshipperapp.data.repository

import com.example.deliveryshipperapp.data.remote.api.ProfileApi
import com.example.deliveryshipperapp.data.remote.dto.UserDto
import com.example.deliveryshipperapp.utils.Resource
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val api: ProfileApi
) {
    suspend fun getProfile(): Resource<UserDto> {
        return try {
            val res = api.getProfile()
            if (res.isSuccessful) {
                res.body()?.let { Resource.Success(it) }
                    ?: Resource.Error("Empty profile response")
            } else {
                Resource.Error(res.errorBody()?.string() ?: "Unknown error")
            }
        } catch (e: Exception) {
            Resource.Error("Profile fetch failed: ${e.message}")
        }
    }
}