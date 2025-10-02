package com.example.deliveryshipperapp.data.repository

import com.example.deliveryshipperapp.data.remote.api.ShipperApi
import com.example.deliveryshipperapp.data.remote.dto.*
import com.example.deliveryshipperapp.utils.Resource
import retrofit2.Response

class ShipperRepository(private val api: ShipperApi) {

    suspend fun getAvailableOrders(): Resource<OrdersListResponse> = handleOrdersResponse { api.getAvailableOrders() }

    suspend fun getMyOrders(): Resource<OrdersListResponse> = handleOrdersResponse { api.getMyOrders() }

    suspend fun getOrderDetail(id: Long): Resource<OrderDetailDto> = handleDetailResponse { api.getOrderDetail(id) }

    suspend fun receiveOrder(orderId: Long): Resource<Unit> = handleUnitResponse { api.receiveOrder(ReceiveOrderRequestDto(orderId)) }

    suspend fun updateOrder(orderId: Long, payment: String, status: String): Resource<Unit> =
        handleUnitResponse { api.updateOrder(UpdateOrderRequestDto(orderId, payment, status)) }

    private inline fun handleOrdersResponse(apiCall: () -> Response<OrdersListResponse>): Resource<OrdersListResponse> {
        return try {
            val resp = apiCall()
            if (resp.isSuccessful) {
                val body = resp.body()
                if (body != null) {
                    Resource.Success(body)
                } else {
                    Resource.Success(OrdersListResponse(emptyList()))
                }
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
                val body = resp.body()
                if (body != null) {
                    Resource.Success(body)
                } else {
                    Resource.Error("Order details not found")
                }
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