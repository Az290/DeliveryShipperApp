package com.example.deliveryshipperapp.data.remote.dto

data class AuthResponseDto(val access_token:String,val refresh_token:String)
data class LoginRequestDto(val email:String,val password:String)