package com.example.deliveryshipperapp.ui.orders

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliveryshipperapp.data.remote.dto.OrderDetailDto
import com.example.deliveryshipperapp.data.remote.dto.OrderDto
import com.example.deliveryshipperapp.data.remote.dto.OrdersListResponse
import com.example.deliveryshipperapp.data.remote.dto.UserDto
import com.example.deliveryshipperapp.domain.usecase.*
import com.example.deliveryshipperapp.utils.NotificationHelper
import com.example.deliveryshipperapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
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

    // Thêm biến mới để theo dõi đã nhận đơn đầu tiên chưa
    private val _isFirstOrderReceived = MutableStateFlow(false)
    val isFirstOrderReceived: StateFlow<Boolean> = _isFirstOrderReceived

    // Thêm state lưu trữ ID đơn hàng đã chọn
    private val _selectedOrderId = MutableStateFlow<Long?>(null)
    val selectedOrderId: StateFlow<Long?> = _selectedOrderId

    init {
        // khi mở app load cả 2
        loadAvailableOrders()
        loadMyOrders()
    }

    // Lưu ID của đơn hàng được chọn
    fun selectOrder(orderId: Long) {
        _selectedOrderId.value = orderId
    }

    /** ✅ Lấy danh sách đơn processing (có thể nhận) */
    fun loadAvailableOrders() {
        viewModelScope.launch {
            _availableOrders.value = Resource.Loading()
            try {
                Log.d(TAG, "Đang tải danh sách đơn có thể nhận...")
                val result = getAvailableOrders()
                _availableOrders.value = result
                if (result is Resource.Success) {
                    Log.d(TAG, "Tải danh sách đơn có thể nhận thành công: ${result.data?.orders?.size ?: 0} đơn")
                }
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
                Log.d(TAG, "Đang tải danh sách đơn của tôi...")
                val result = getReceivedOrders()
                _myOrders.value = result
                if (result is Resource.Success) {
                    Log.d(TAG, "Tải danh sách đơn của tôi thành công: ${result.data?.orders?.size ?: 0} đơn")
                    result.data?.orders?.forEach { order ->
                        Log.d(TAG, "Đơn #${order.id}: ${order.order_status}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error my orders: ${e.message}")
                _myOrders.value = Resource.Error("Không thể tải đơn shipper: ${e.message}")
            }
        }
    }

    /** Phương thức mới - Tìm đơn hàng từ danh sách có sẵn */
    fun loadOrderFromAvailableList(orderId: Long) {
        viewModelScope.launch {
            _orderDetail.value = Resource.Loading()

            try {
                // Đầu tiên, đảm bảo danh sách đã được tải
                if (_availableOrders.value !is Resource.Success) {
                    loadAvailableOrders()
                    // Đợi một chút để đảm bảo API hoàn thành
                    delay(500)
                }

                // Kiểm tra lại kết quả
                val currentOrders = _availableOrders.value
                if (currentOrders is Resource.Success) {
                    // Tìm đơn hàng với ID tương ứng
                    val order = currentOrders.data?.orders?.find { it.id == orderId }

                    if (order != null) {
                        Log.d(TAG, "Đã tìm thấy đơn hàng #${orderId} trong danh sách có sẵn")

                        // Tạo OrderDto giả từ OrderSummaryDto
                        val orderDto = OrderDto(
                            id = order.id,
                            user_id = 0, // Mặc định 0 vì OrderSummaryDto không có trường này
                            order_status = order.order_status,
                            payment_status = "unpaid", // Mặc định
                            latitude = 10.762622, // Vị trí mặc định TPHCM
                            longitude = 106.660172,
                            total_amount = order.total_amount,
                            thumbnail_id = null,
                            created_at = null
                        )

                        // Tạo OrderDetailDto
                        val detailDto = OrderDetailDto(
                            order = orderDto,
                            items = emptyList() // không có thông tin chi tiết
                        )

                        _orderDetail.value = Resource.Success(detailDto)
                    } else {
                        Log.d(TAG, "Không tìm thấy đơn hàng #${orderId} trong danh sách, gọi API chi tiết")
                        // Nếu không tìm thấy trong danh sách, thử gọi API chi tiết
                        loadOrderDetail(orderId)
                    }
                } else {
                    Log.d(TAG, "Danh sách đơn không có sẵn, gọi API chi tiết")
                    // Danh sách không có sẵn, thử gọi API chi tiết
                    loadOrderDetail(orderId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error finding order: ${e.message}")
                _orderDetail.value = Resource.Error("Không thể tìm đơn hàng: ${e.message}")
            }
        }
    }

    /** Chi tiết đơn + thông tin khách */
    fun loadOrderDetail(orderId: Long) {
        viewModelScope.launch {
            _orderDetail.value = Resource.Loading()
            try {
                Log.d(TAG, "Đang tải chi tiết đơn hàng #${orderId}...")
                val detail = getOrderDetail(orderId)
                _orderDetail.value = detail

                if (detail is Resource.Success && detail.data != null) {
                    Log.d(TAG, "Tải chi tiết đơn hàng thành công")
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

    /** Phương thức mới - Nhận đơn và đợi cập nhật hoàn tất */
    suspend fun acceptOrderAndWaitForUpdate(orderId: Long, customerId: Long): Boolean {
        _receiveOrderState.value = Resource.Loading()

        try {
            Log.d(TAG, "Đang gọi API nhận đơn: $orderId")
            val result = receiveOrder(orderId)
            Log.d(TAG, "Kết quả nhận đơn: $result")
            _receiveOrderState.value = result

            if (result is Resource.Success) {
                Log.d(TAG, "Nhận đơn thành công, cập nhật danh sách")
                NotificationHelper.showOrderReceivedNotification(context, orderId)
                _currentChatOrder.value = Pair(orderId, customerId)

                // Kiểm tra nếu là đơn đầu tiên
                val isFirst = !_isFirstOrderReceived.value
                if (isFirst) {
                    _isFirstOrderReceived.value = true
                    // Thêm delay dài hơn cho đơn đầu tiên
                    delay(1200)
                } else {
                    delay(800)
                }

                // Tải lại danh sách với nhiều lần thử nếu là đơn đầu tiên
                _myOrders.value = Resource.Loading()
                val myOrdersResult = getReceivedOrders()
                _myOrders.value = myOrdersResult

                // Kiểm tra và thử lại nếu là đơn đầu tiên và danh sách trống
                if (isFirst && (myOrdersResult is Resource.Success && myOrdersResult.data?.orders?.isEmpty() == true)) {
                    delay(1000)
                    // Thử lại lần nữa
                    val retryResult = getReceivedOrders()
                    _myOrders.value = retryResult
                }

                // Tải lại danh sách đơn có thể nhận
                loadAvailableOrders()

                return true
            }
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Error accept: ${e.message}")
            _receiveOrderState.value = Resource.Error("Nhận đơn thất bại: ${e.message}")
            return false
        }
    }

    /** Nhận đơn - phương thức cũ, giữ lại để tương thích */
    fun acceptOrder(orderId: Long, customerId: Long) {
        viewModelScope.launch {
            _receiveOrderState.value = Resource.Loading()
            try {
                Log.d(TAG, "Đang gọi API nhận đơn: $orderId")
                val result = receiveOrder(orderId)
                Log.d(TAG, "Kết quả nhận đơn: $result")
                _receiveOrderState.value = result
                if (result is Resource.Success) {
                    Log.d(TAG, "Nhận đơn thành công, cập nhật danh sách")
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