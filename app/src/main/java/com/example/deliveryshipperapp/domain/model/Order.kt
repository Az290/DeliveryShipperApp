package com.example.deliveryshipperapp.domain.model

data class Order(
    val id:Long,
    val orderStatus:String,
    val totalAmount:Double,
    val latitude:Double,
    val longitude:Double
)