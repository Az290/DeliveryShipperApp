package com.example.deliveryshipperapp.data.repository

import android.util.Log
import com.example.deliveryshipperapp.data.remote.api.ShipperApi
import com.example.deliveryshipperapp.data.remote.dto.*
import com.example.deliveryshipperapp.utils.Resource
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
                // ✅ SỬA: Sử dụng safeOrders() thay vì orders.orders
                val safeOrdersList = orders?.safeOrders() ?: emptyList()
                Log.d(TAG, "API trả về ${safeOrdersList.size} đơn hàng")

                // Nếu danh sách trống, đợi một chút và thử lại
                if (safeOrdersList.isEmpty()) {
                    Log.d(TAG, "Danh sách trống, thử lại sau 800ms")
                    delay(800)
                    val retryResponse = api.getReceivedOrders()

                    if (retryResponse.isSuccessful) {
                        val retryOrders = retryResponse.body()
                        // ✅ SỬA: Sử dụng safeOrders()
                        val retrySafeList = retryOrders?.safeOrders() ?: emptyList()
                        Log.d(TAG, "Retry API trả về ${retrySafeList.size} đơn hàng")
                        // ✅ Trả về response với list đã safe
                        Resource.Success(OrdersListResponse(retrySafeList))
                    } else {
                        Resource.Error(retryResponse.errorBody()?.string() ?: "Unknown error")
                    }
                } else {
                    // ✅ Trả về response với list đã safe
                    Resource.Success(OrdersListResponse(safeOrdersList))
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

    // Helpers parse Response
    // ✅ SỬA: Xử lý null-safe trong helper
    private inline fun handleOrdersResponse(apiCall: () -> Response<OrdersListResponse>): Resource<OrdersListResponse> {
        return try {
            val resp = apiCall()
            if (resp.isSuccessful) {
                val body = resp.body()
                // ✅ Đảm bảo luôn trả về list không null
                val safeList = body?.safeOrders() ?: emptyList()
                Resource.Success(OrdersListResponse(safeList))
            } else {
                Resource.Error(resp.errorBody()?.string() ?: "Unknown error")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi handleOrdersResponse: ${e.message}")
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