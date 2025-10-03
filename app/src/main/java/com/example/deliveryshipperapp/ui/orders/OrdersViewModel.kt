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
    private val getAvailableOrders: GetAvailableOrdersUseCase,   // cho processing
    private val getReceivedOrders: GetReceivedOrdersUseCase,     // cho shipping
    private val getOrderDetail: GetOrderDetailUseCase,
    private val receiveOrder: ReceiveOrderUseCase,
    private val updateOrder: UpdateOrderUseCase,
    private val getUser: GetUserUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _availableOrders = MutableStateFlow<Resource<OrdersListResponse>>(Resource.Loading())
    val availableOrders: StateFlow<Resource<OrdersListResponse>> = _availableOrders

    private val _myOrders = MutableStateFlow<Resource<OrdersListResponse>>(Resource.Loading())
    val myOrders: StateFlow<Resource<OrdersListResponse>> = _myOrders

    private val _orderDetail = MutableStateFlow<Resource<OrderDetailDto>>(Resource.Loading())
    val orderDetail: StateFlow<Resource<OrderDetailDto>> = _orderDetail

    private val _customerInfo = MutableStateFlow<Resource<UserDto>>(Resource.Loading())
    val customerInfo: StateFlow<Resource<UserDto>> = _customerInfo

    private val _receiveOrderState = MutableStateFlow<Resource<Unit>?>(null)
    val receiveOrderState: StateFlow<Resource<Unit>?> = _receiveOrderState

    private val _updateOrderState = MutableStateFlow<Resource<Unit>?>(null)
    val updateOrderState: StateFlow<Resource<Unit>?> = _updateOrderState

    private val _currentChatOrder = MutableStateFlow<Pair<Long, Long>?>(null)
    val currentChatOrder: StateFlow<Pair<Long, Long>?> = _currentChatOrder

    init {
        // khi mở app load cả 2
        loadAvailableOrders()
        loadMyOrders()
    }

    /** ✅ Lấy danh sách đơn processing (có thể nhận) */
    fun loadAvailableOrders() {
        viewModelScope.launch {
            _availableOrders.value = Resource.Loading()
            try {
                val result = getAvailableOrders()
                _availableOrders.value = result
            } catch (e: Exception) {
                Log.e(TAG, "Error available orders: ${e.message}")
                _availableOrders.value = Resource.Error("Không thể tải đơn khả dụng: ${e.message}")
            }
        }
    }

    /** ✅ Lấy danh sách đơn shipping (shipper đã nhận) */
    fun loadMyOrders() {
        viewModelScope.launch {
            _myOrders.value = Resource.Loading()
            try {
                val result = getReceivedOrders()
                _myOrders.value = result
            } catch (e: Exception) {
                Log.e(TAG, "Error my orders: ${e.message}")
                _myOrders.value = Resource.Error("Không thể tải đơn shipper: ${e.message}")
            }
        }
    }

    /** Chi tiết đơn + thông tin khách */
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

    private fun loadCustomerInfo(userId: Long) {
        viewModelScope.launch {
            _customerInfo.value = Resource.Loading()
            try {
                _customerInfo.value = getUser(userId)
            } catch (e: Exception) {
                Log.e(TAG, "Error customer: ${e.message}")
                _customerInfo.value = Resource.Error("Không thể tải thông tin khách: ${e.message}")
            }
        }
    }

    /** Nhận đơn */
    fun acceptOrder(orderId: Long, customerId: Long) {
        viewModelScope.launch {
            _receiveOrderState.value = Resource.Loading()
            try {
                val result = receiveOrder(orderId)
                _receiveOrderState.value = result
                if (result is Resource.Success) {
                    NotificationHelper.showOrderReceivedNotification(context, orderId)
                    _currentChatOrder.value = Pair(orderId, customerId)
                    loadAvailableOrders()
                    loadMyOrders()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error accept: ${e.message}")
                _receiveOrderState.value = Resource.Error("Nhận đơn thất bại: ${e.message}")
            }
        }
    }

    /** Đánh dấu delivered */
    fun markDelivered(orderId: Long) {
        viewModelScope.launch {
            _updateOrderState.value = Resource.Loading()
            try {
                val result = updateOrder(orderId, "paid", "delivered")
                _updateOrderState.value = result
                if (result is Resource.Success) {
                    NotificationHelper.showOrderDeliveredNotification(context, orderId)
                    loadMyOrders()
                    _currentChatOrder.value = null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error delivered: ${e.message}")
                _updateOrderState.value = Resource.Error("Cập nhật thất bại: ${e.message}")
            }
        }
    }

    fun resetReceiveOrderState() { _receiveOrderState.value = null }
    fun resetUpdateOrderState() { _updateOrderState.value = null }
}