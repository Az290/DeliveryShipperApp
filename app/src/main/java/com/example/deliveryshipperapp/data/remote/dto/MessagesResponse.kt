package com.example.deliveryshipperapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MessagesResponse(
    // Map key "data" từ JSON vào biến này
    @SerializedName("data")
    val data: List<MessageDto>
)