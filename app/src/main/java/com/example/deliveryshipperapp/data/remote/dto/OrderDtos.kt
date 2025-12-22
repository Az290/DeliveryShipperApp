package com.example.deliveryshipperapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OrdersListResponse(
    val orders: List<OrderSummaryDto>
)

data class OrderSummaryDto(
    val id: Long,
    @SerializedName("user_id")
    val user_id: Long?,
    @SerializedName("payment_status")
    val payment_status: String?,
    @SerializedName("order_status")
    val order_status: String,
    val latitude: Double,
    val longitude: Double,
    @SerializedName("total_amount")
    val total_amount: Double,
    @SerializedName("thumbnail")
    val thumbnail: String?,
    // ✅ Thêm lại thumbnail_id để tránh lỗi "Unresolved reference" ở các màn hình danh sách
    @SerializedName("thumbnail_id")
    val thumbnail_id: String? = null
)

data class ReceiveOrderRequestDto(
    @SerializedName("order_id")
    val orderId: Long
)

data class UpdateOrderRequestDto(
    @SerializedName("order_id")
    val orderId: Long,
    @SerializedName("payment_status")
    val payment_status: String,
    @SerializedName("order_status")
    val order_status: String
)

// ✅ Đã sửa: Cấu trúc phẳng để khớp hoàn toàn với JSON 200 OK từ Backend
data class OrderDetailDto(
    val id: Long,
    @SerializedName("user_id")
    val user_id: Long,
    @SerializedName("user_name")
    val user_name: String?,
    @SerializedName("user_phone")
    val phone: String?,
    @SerializedName("order_status")
    val order_status: String,
    @SerializedName("payment_status")
    val payment_status: String,
    val latitude: Double,
    val longitude: Double,
    @SerializedName("total_amount")
    val total_amount: Double,
    @SerializedName("created_at")
    val created_at: String?,
    @SerializedName("updated_at")
    val updated_at: String?,
    // ✅ Bổ sung ShipperInfo vì Backend có trả về trường này
    @SerializedName("shipper_info")
    val shipper_info: ShipperInfoDto? = null,
    val items: List<OrderItemDto>
)

data class ShipperInfoDto(
    val id: Long,
    val name: String,
    val phone: String
)

data class OrderDto(
    val id: Long,
    @SerializedName("user_id")
    val user_id: Long,
    @SerializedName("user_name")
    val user_name: String?,
    @SerializedName("user_phone")
    val phone: String?,
    @SerializedName("order_status")
    val order_status: String,
    @SerializedName("payment_status")
    val payment_status: String,
    val latitude: Double,
    val longitude: Double,
    @SerializedName("total_amount")
    val total_amount: Double,
    @SerializedName("thumbnail_id")
    val thumbnail_id: Long?,
    @SerializedName("created_at")
    val created_at: String?
)

data class OrderItemDto(
    @SerializedName("product_id")
    val product_id: Long,
    @SerializedName("product_name")
    val product_name: String,
    @SerializedName("product_image")
    val product_image: String?,
    val quantity: Long,
    val price: Double,
    val subtotal: Double
)