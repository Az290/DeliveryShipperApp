package com.example.deliveryshipperapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserDto(
    val id: Long,
    val name: String,
    val email: String,
    val phone: String?,
    val address: String?,
    val role: String?,
    val status: Int
)