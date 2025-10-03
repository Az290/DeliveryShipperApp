package com.example.deliveryshipperapp.data.remote.api

import com.example.deliveryshipperapp.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ShipperApi {
    @GET("shipper/orders")
    suspend fun getAvailableOrders(
        @Query("page") page:Int=1,
        @Query("limit") limit:Int=20
    ): Response<OrdersListResponse>

    @POST("shipper/receive-order")
    suspend fun receiveOrder(@Body req:ReceiveOrderRequestDto):Response<Unit>

    @POST("shipper/update-order")
    suspend fun updateOrder(@Body req:UpdateOrderRequestDto):Response<Unit>

    @GET("orders/{id}")
    suspend fun getOrderDetail(@Path("id") id:Long):Response<OrderDetailDto>

    // Thêm tham số status để phân biệt
    @GET("shipper/orders")
    suspend fun getMyOrders(
        @Query("status") status:String="shipping",
        @Query("page") page:Int=1,
        @Query("limit") limit:Int=20
    ):Response<OrdersListResponse>
}