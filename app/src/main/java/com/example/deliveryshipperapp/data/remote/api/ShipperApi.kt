package com.example.deliveryshipperapp.data.remote.api

import com.example.deliveryshipperapp.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ShipperApi {
    // Lấy danh sách đơn hàng đang ở trạng thái "processing" để Shipper chọn nhận đơn
    // Backend: shipper.GET("/orders", ...) -> /api/v1/shipper/orders
    @GET("shipper/orders")
    suspend fun getAvailableOrders(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<OrdersListResponse>

    // Lấy danh sách đơn hàng mà Shipper này đã nhận và đang đi giao (shipping)
    // Backend: shipper.GET("/orders/received-orders", ...) -> /api/v1/shipper/orders/received-orders
    @GET("shipper/orders/received-orders")
    suspend fun getReceivedOrders(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<OrdersListResponse>

    // Shipper nhấn nút nhận một đơn hàng cụ thể
    // Backend: shipper.POST("/receive-order", ...) -> /api/v1/shipper/receive-order
    @POST("shipper/receive-order")
    suspend fun receiveOrder(@Body req: ReceiveOrderRequestDto): Response<Unit>

    // Shipper cập nhật trạng thái đơn (sang delivered) hoặc trạng thái thanh toán
    // Backend: shipper.POST("/update-order", ...) -> /api/v1/shipper/update-order
    @POST("shipper/update-order")
    suspend fun updateOrder(@Body req: UpdateOrderRequestDto): Response<Unit>

    // Lấy chi tiết một đơn hàng (Dùng để hiển thị trong màn hình Delivery/OrderDetail)
    // ✅ ĐÃ SỬA: Thêm "shipper/" để khớp với Group Route trong backend/routes/routes.go
    @GET("shipper/orders/{id}")
    suspend fun getOrderDetail(@Path("id") id: Long): Response<OrderDetailDto>
}