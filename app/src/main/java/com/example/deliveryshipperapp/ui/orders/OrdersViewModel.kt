package com.example.deliveryshipperapp.ui.orders

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliveryshipperapp.data.remote.dto.OrderDetailDto
import com.example.deliveryshipperapp.data.remote.dto.OrdersListResponse
import com.example.deliveryshipperapp.data.remote.dto.UserDto
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
    private val getAvailableOrders: GetAvailableOrdersUseCase,
    private val getOrderDetail: GetOrderDetailUseCase,
    private val receiveOrder: ReceiveOrderUseCase,
    private val updateOrder: UpdateOrderUseCase,
    private val getUser: GetUserUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // Danh sách đơn có thể nhận
    private val _availableOrders = MutableStateFlow<Resource<OrdersListResponse>>(Resource.Loading())
    val availableOrders: StateFlow<Resource<OrdersListResponse>> = _availableOrders

    // Chi tiết một đơn
    private val _orderDetail = MutableStateFlow<Resource<OrderDetailDto>>(Resource.Loading())
    val orderDetail: StateFlow<Resource<OrderDetailDto>> = _orderDetail

    // Thông tin khách hàng (lấy thêm từ user_id)
    private val _customerInfo = MutableStateFlow<Resource<UserDto>>(Resource.Loading())
    val customerInfo: StateFlow<Resource<UserDto>> = _customerInfo

    // Trạng thái nhận / giao đơn
    private val _receiveOrderState = MutableStateFlow<Resource<Unit>?>(null)
    val receiveOrderState: StateFlow<Resource<Unit>?> = _receiveOrderState

    private val _updateOrderState = MutableStateFlow<Resource<Unit>?>(null)
    val updateOrderState: StateFlow<Resource<Unit>?> = _updateOrderState

    init {
        loadAvailableOrders()
    }

    /** Lấy danh sách đơn có thể nhận (processing) */
    fun loadAvailableOrders() {
        viewModelScope.launch {
            _availableOrders.value = Resource.Loading()
            try {
                val result = getAvailableOrders()
                _availableOrders.value = result
            } catch (e: Exception) {
                Log.e(TAG, "Error loading available orders: ${e.message}")
                _availableOrders.value = Resource.Error("Không thể tải đơn: ${e.message}")
            }
        }
    }

    /** Lấy chi tiết một đơn + từ order.user_id gọi thêm API lấy thông tin khách */
    fun loadOrderDetail(orderId: Long) {
        viewModelScope.launch {
            _orderDetail.value = Resource.Loading()
            try {
                val detail = getOrderDetail(orderId)
                _orderDetail.value = detail

                if (detail is Resource.Success && detail.data != null) {
                    val userId = detail.data.order.user_id
                    loadCustomerInfo(userId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading order detail: ${e.message}")
                _orderDetail.value = Resource.Error("Không thể tải chi tiết đơn: ${e.message}")
            }
        }
    }

    /** Lấy thông tin khách hàng từ user_id */
    private fun loadCustomerInfo(userId: Long) {
        viewModelScope.launch {
            _customerInfo.value = Resource.Loading()
            try {
                _customerInfo.value = getUser(userId)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user info: ${e.message}")
                _customerInfo.value = Resource.Error("Không thể tải thông tin khách: ${e.message}")
            }
        }
    }

    /** Nhận đơn -> sang trạng thái shipping */
    fun acceptOrder(orderId: Long) {
        viewModelScope.launch {
            _receiveOrderState.value = Resource.Loading()
            try {
                val result = receiveOrder(orderId)
                _receiveOrderState.value = result
                if (result is Resource.Success) {
                    NotificationHelper.showOrderReceivedNotification(context, orderId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error accept order: ${e.message}")
                _receiveOrderState.value = Resource.Error("Nhận đơn thất bại: ${e.message}")
            }
        }
    }

    /** Giao đơn -> đánh dấu delivered */
    fun markDelivered(orderId: Long) {
        viewModelScope.launch {
            _updateOrderState.value = Resource.Loading()
            try {
                val result = updateOrder(orderId, "paid", "delivered")
                _updateOrderState.value = result
                if (result is Resource.Success) {
                    NotificationHelper.showOrderDeliveredNotification(context, orderId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error mark delivered: ${e.message}")
                _updateOrderState.value = Resource.Error("Cập nhật đơn thất bại: ${e.message}")
            }
        }
    }

    fun resetReceiveOrderState() { _receiveOrderState.value = null }
    fun resetUpdateOrderState() { _updateOrderState.value = null }
}