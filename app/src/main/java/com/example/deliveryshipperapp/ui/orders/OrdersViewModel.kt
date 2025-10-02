package com.example.deliveryshipperapp.ui.orders

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliveryshipperapp.data.remote.dto.OrderDetailDto
import com.example.deliveryshipperapp.data.remote.dto.OrdersListResponse
import com.example.deliveryshipperapp.domain.usecase.*
import com.example.deliveryshipperapp.utils.NotificationHelper
import com.example.deliveryshipperapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "OrdersViewModel"

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val getAvailable: GetAvailableOrdersUseCase,
    private val getMy: GetMyOrdersUseCase,
    private val getDetail: GetOrderDetailUseCase,
    private val receiveOrder: ReceiveOrderUseCase,
    private val updateOrder: UpdateOrderUseCase,
    @ApplicationContext private val context: Context  // Sửa từ Application thành Context với annotation
) : ViewModel() {

    // Phần code còn lại giữ nguyên
    private val _availableOrders = MutableStateFlow<Resource<OrdersListResponse>>(Resource.Loading())
    val availableOrders: StateFlow<Resource<OrdersListResponse>> = _availableOrders

    private val _myOrders = MutableStateFlow<Resource<OrdersListResponse>>(Resource.Loading())
    val myOrders: StateFlow<Resource<OrdersListResponse>> = _myOrders

    private val _orderDetail = MutableStateFlow<Resource<OrderDetailDto>>(Resource.Loading())
    val orderDetail: StateFlow<Resource<OrderDetailDto>> = _orderDetail

    // Thêm state cho trạng thái nhận đơn
    private val _receiveOrderState = MutableStateFlow<Resource<Unit>?>(null)
    val receiveOrderState: StateFlow<Resource<Unit>?> = _receiveOrderState

    // Thêm state cho trạng thái cập nhật đơn
    private val _updateOrderState = MutableStateFlow<Resource<Unit>?>(null)
    val updateOrderState: StateFlow<Resource<Unit>?> = _updateOrderState

    fun loadAvailableOrders() {
        viewModelScope.launch {
            _availableOrders.value = Resource.Loading()
            try {
                _availableOrders.value = getAvailable()
            } catch (e: Exception) {
                Log.e(TAG, "Error loading available orders: ${e.message}", e)
                _availableOrders.value = Resource.Error("Lỗi tải đơn hàng: ${e.message}")
            }
        }
    }

    fun loadMyOrders() {
        viewModelScope.launch {
            _myOrders.value = Resource.Loading()
            try {
                _myOrders.value = getMy()
            } catch (e: Exception) {
                Log.e(TAG, "Error loading my orders: ${e.message}", e)
                _myOrders.value = Resource.Error("Lỗi tải đơn hàng: ${e.message}")
            }
        }
    }

    fun loadOrderDetail(id: Long) {
        viewModelScope.launch {
            _orderDetail.value = Resource.Loading()
            try {
                _orderDetail.value = getDetail(id)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading order detail: ${e.message}", e)
                _orderDetail.value = Resource.Error("Lỗi tải chi tiết đơn hàng: ${e.message}")
            }
        }
    }

    fun acceptOrder(id: Long) {
        viewModelScope.launch {
            _receiveOrderState.value = Resource.Loading()
            try {
                Log.d(TAG, "Attempting to accept order: $id")
                val result = receiveOrder(id)
                _receiveOrderState.value = result

                if (result is Resource.Success) {
                    // Hiển thị thông báo
                    NotificationHelper.showOrderReceivedNotification(context, id)

                    // Cập nhật trạng thái đơn hàng ngay lập tức mà không cần gọi API lại
                    val currentDetail = orderDetail.value
                    if (currentDetail is Resource.Success && currentDetail.data != null) {
                        val updatedOrder = currentDetail.data.order.copy(order_status = "shipping")
                        val updatedDetail = currentDetail.data.copy(order = updatedOrder)
                        _orderDetail.value = Resource.Success(updatedDetail)
                    } else {
                        // Nếu không có dữ liệu chi tiết đơn hàng, gọi API
                        loadOrderDetail(id)
                    }

                    // Cập nhật danh sách đơn hàng
                    loadAvailableOrders()
                    loadMyOrders()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error accepting order: ${e.message}", e)
                _receiveOrderState.value = Resource.Error("Lỗi nhận đơn: ${e.message}")
            }
        }
    }

    fun markDelivered(id: Long) {
        viewModelScope.launch {
            _updateOrderState.value = Resource.Loading()
            try {
                Log.d(TAG, "Marking order as delivered: $id")
                val result = updateOrder(id, "paid", "delivered")
                _updateOrderState.value = result

                if (result is Resource.Success) {
                    // Hiển thị thông báo
                    NotificationHelper.showOrderDeliveredNotification(context, id)

                    // Cập nhật UI
                    val currentDetail = orderDetail.value
                    if (currentDetail is Resource.Success && currentDetail.data != null) {
                        val updatedOrder = currentDetail.data.order.copy(
                            order_status = "delivered",
                            payment_status = "paid"
                        )
                        val updatedDetail = currentDetail.data.copy(order = updatedOrder)
                        _orderDetail.value = Resource.Success(updatedDetail)
                    } else {
                        loadOrderDetail(id)
                    }

                    loadMyOrders()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error marking order as delivered: ${e.message}", e)
                _updateOrderState.value = Resource.Error("Lỗi cập nhật đơn hàng: ${e.message}")
            }
        }
    }

    // Reset các state
    fun resetReceiveOrderState() {
        _receiveOrderState.value = null
    }

    fun resetUpdateOrderState() {
        _updateOrderState.value = null
    }
}