package com.example.deliveryshipperapp.data.repository

import android.util.Log
import com.example.deliveryshipperapp.data.remote.api.ShipperApi
import com.example.deliveryshipperapp.data.remote.dto.*
import com.example.deliveryshipperapp.utils.Resource
import com.example.deliveryshipperapp.utils.WebSocketManager
import kotlinx.coroutines.delay
import retrofit2.Response

private const val TAG = "ShipperRepository"

class ShipperRepository(private val api: ShipperApi) {

    suspend fun getAvailableOrders(): Resource<OrdersListResponse> =
        handleOrdersResponse {
            Log.d(TAG, "Gọi API getAvailableOrders")
            api.getAvailableOrders()
        }

    suspend fun getReceivedOrders(): Resource<OrdersListResponse> {
        return try {
            Log.d(TAG, "Gọi API getReceivedOrders")
            val response = api.getReceivedOrders()

            if (response.isSuccessful) {
                val orders = response.body()
                if (orders != null) {
                    Log.d(TAG, "API trả về ${orders.orders.size} đơn hàng")

                    // Nếu danh sách trống, đợi một chút và thử lại
                    if (orders.orders.isEmpty()) {
                        Log.d(TAG, "Danh sách trống, thử lại sau 800ms")
                        delay(800)
                        val retryResponse = api.getReceivedOrders()

                        if (retryResponse.isSuccessful) {
                            val retryOrders = retryResponse.body()
                            if (retryOrders != null) {
                                Log.d(TAG, "Retry API trả về ${retryOrders.orders.size} đơn hàng")
                                Resource.Success(retryOrders)
                            } else {
                                Resource.Success(OrdersListResponse(emptyList()))
                            }
                        } else {
                            Resource.Error(retryResponse.errorBody()?.string() ?: "Unknown error")
                        }
                    } else {
                        Resource.Success(orders)
                    }
                } else {
                    Resource.Success(OrdersListResponse(emptyList()))
                }
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Unknown error")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi gọi getReceivedOrders: ${e.message}")
            Resource.Error(e.message ?: "Unexpected error")
        }
    }

    suspend fun getOrderDetail(id: Long): Resource<OrderDetailDto> =
        handleDetailResponse {
            Log.d(TAG, "Gọi API getOrderDetail cho đơn #$id")
            api.getOrderDetail(id)
        }

    suspend fun receiveOrder(orderId: Long): Resource<Unit> =
        handleUnitResponse {
            Log.d(TAG, "Gọi API receiveOrder cho đơn #$orderId")
            api.receiveOrder(ReceiveOrderRequestDto(orderId))
        }

    suspend fun updateOrder(orderId: Long, payment: String, status: String): Resource<Unit> =
        handleUnitResponse {
            Log.d(TAG, "Gọi API updateOrder cho đơn #$orderId: payment=$payment, status=$status")
            api.updateOrder(UpdateOrderRequestDto(orderId, payment, status))
        }

    // Thêm vào ShipperRepository
    private var wsManager: WebSocketManager? = null

    suspend fun openChat(token: String, orderId: Long, toUserId: Long, onMessage: (String) -> Unit) {
        wsManager = WebSocketManager(token, { msg -> onMessage(msg) }, { /* Handle close */ })
        wsManager?.connect(orderId)
    }

    fun closeChat() {
        wsManager?.close()
        wsManager = null
    }

    suspend fun receiveOrderWithChat(orderId: Long, token: String, customerId: Long): Resource<Unit> {
        val result = receiveOrder(orderId)
        if (result is Resource.Success) {
            // Mở WS sau khi nhận thành công
            openChat(token, orderId, customerId, { msg -> /* Xử lý message ở ViewModel */ })
        }
        return result
    }

    // Helpers parse Response
    private inline fun handleOrdersResponse(apiCall: () -> Response<OrdersListResponse>): Resource<OrdersListResponse> {
        return try {
            val resp = apiCall()
            if (resp.isSuccessful) {
                resp.body()?.let { Resource.Success(it) } ?: Resource.Success(OrdersListResponse(emptyList()))
            } else {
                Resource.Error(resp.errorBody()?.string() ?: "Unknown error")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unexpected error")
        }
    }

    private inline fun handleDetailResponse(apiCall: () -> Response<OrderDetailDto>): Resource<OrderDetailDto> {
        return try {
            val resp = apiCall()
            if (resp.isSuccessful) {
                resp.body()?.let { Resource.Success(it) } ?: Resource.Error("Order details not found")
            } else {
                Resource.Error(resp.errorBody()?.string() ?: "Unknown error")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unexpected error")
        }
    }

    private inline fun handleUnitResponse(apiCall: () -> Response<Unit>): Resource<Unit> {
        return try {
            val resp = apiCall()
            if (resp.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error(resp.errorBody()?.string() ?: "Unknown error")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unexpected error")
        }
    }
}