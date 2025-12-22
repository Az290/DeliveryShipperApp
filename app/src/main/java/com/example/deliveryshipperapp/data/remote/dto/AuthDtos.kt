package com.example.deliveryshipperapp.data.remote.dto

import com.google.gson.annotations.SerializedName


data class AuthResponseDto(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("refresh_token")
    val refreshToken: String? = null,

    // ⚠️ Backend có typo "refersh_token" trong RefreshTokenResponse
    @SerializedName("refersh_token")
    val refershToken: String? = null
) {
    // Helper để lấy refresh token từ cả 2 field
    fun getRefreshTokenValue(): String? = refreshToken ?: refershToken
}

data class LoginRequestDto(
    val email: String,
    val password: String
)