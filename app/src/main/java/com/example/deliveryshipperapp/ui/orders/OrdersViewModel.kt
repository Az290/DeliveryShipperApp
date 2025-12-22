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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "OrdersViewModel"

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val getAvailableOrders: GetAvailableOrdersUseCase,
    private val getReceivedOrders: GetReceivedOrdersUseCase,
    private val getOrderDetail: GetOrderDetailUseCase,
    private val receiveOrder: ReceiveOrderUseCase,
    private val updateOrder: UpdateOrderUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    // Helper function để xử lý an toàn dữ liệu trả về
    // Nếu orders bị null, hàm này sẽ trả về một object mới với emptyList
    private fun sanitizeResponse(original: OrdersListResponse?): OrdersListResponse {
        val safeOrders = original?.orders ?: emptyList()
        // Sử dụng copy để giữ các trường khác, chỉ thay thế list orders
        // Nếu original null thì tạo mới hoàn toàn
        return original?.copy(orders = safeOrders)
            ?: try {
                // Thử constructor 1 tham số (List)
                OrdersListResponse(orders = emptyList())
            } catch (e: Exception) {
                // Fallback: Nếu class này có constructor khác, bạn cần chỉnh dòng này
                // Ví dụ nếu constructor có 2 tham số: OrdersListResponse(emptyList(), null)
                throw RuntimeException("Vui lòng kiểm tra constructor OrdersListResponse: ${e.message}")
            }
    }

    // ✅ THÊM MỚI: Helper để xử lý Resource trả về an toàn
    private fun safeResource(result: Resource<OrdersListResponse>): Resource<OrdersListResponse> {
        return when (result) {
            is Resource.Success -> {
                val safeData = sanitizeResponse(result.data)
                Resource.Success(safeData)
            }
            is Resource.Error -> result
            is Resource.Loading -> result
        }
    }

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

    private val _isFirstOrderReceived = MutableStateFlow(false)
    val isFirstOrderReceived: StateFlow<Boolean> = _isFirstOrderReceived

    private val _selectedOrderId = MutableStateFlow<Long?>(null)
    val selectedOrderId: StateFlow<Long?> = _selectedOrderId

    init {
        loadAvailableOrders()
        loadMyOrders()
    }

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

                // --- SỬA LỖI CRASH Ở ĐÂY ---
                _availableOrders.value = safeResource(result)

                val orderCount = (_availableOrders.value as? Resource.Success)?.data?.orders?.size ?: 0
                Log.d(TAG, "Tải danh sách đơn có thể nhận thành công: $orderCount đơn")
                // ---------------------------

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

                // --- SỬA LỖI CRASH Ở ĐÂY ---
                _myOrders.value = safeResource(result)

                val orderCount = (_myOrders.value as? Resource.Success)?.data?.orders?.size ?: 0
                Log.d(TAG, "Tải danh sách đơn của tôi thành công: $orderCount đơn")

                // Log chi tiết nếu có đơn
                if (_myOrders.value is Resource.Success) {
                    val orders = (_myOrders.value as Resource.Success).data?.orders ?: emptyList()
                    orders.forEach { order ->
                        Log.d(TAG, "Đơn #${order.id}: ${order.order_status}")
                    }
                }
                // ---------------------------

            } catch (e: Exception) {
                Log.e(TAG, "Error my orders: ${e.message}")
                _myOrders.value = Resource.Error("Không thể tải đơn shipper: ${e.message}")
            }
        }
    }

    fun loadOrderFromAvailableList(orderId: Long) {
        viewModelScope.launch {
            _orderDetail.value = Resource.Loading()

            try {
                if (_availableOrders.value !is Resource.Success) {
                    loadAvailableOrders()
                    delay(500)
                }

                val currentOrders = _availableOrders.value
                if (currentOrders is Resource.Success) {
                    // Cần xử lý null safe ở đây nữa để tránh crash logic
                    val safeList = currentOrders.data?.orders ?: emptyList()
                    val order = safeList.find { it.id == orderId }

                    if (order != null) {
                        Log.d(TAG, "Đã tìm thấy đơn hàng #${orderId} trong danh sách có sẵn")
                        val detailDto = OrderDetailDto(
                            id = order.id,
                            user_id = order.user_id ?: 0,
                            user_name = "Khách hàng",
                            phone = null,
                            order_status = order.order_status,
                            payment_status = order.payment_status ?: "unpaid",
                            latitude = order.latitude,
                            longitude = order.longitude,
                            total_amount = order.total_amount,
                            created_at = null,
                            updated_at = null,
                            items = emptyList()
                        )
                        _orderDetail.value = Resource.Success(detailDto)
                    } else {
                        Log.d(TAG, "Không tìm thấy đơn hàng, gọi API chi tiết")
                        loadOrderDetail(orderId)
                    }
                } else {
                    loadOrderDetail(orderId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error finding order: ${e.message}")
                _orderDetail.value = Resource.Error("Không thể tìm đơn hàng: ${e.message}")
            }
        }
    }

    fun loadOrderDetail(orderId: Long) {
        viewModelScope.launch {
            _orderDetail.value = Resource.Loading()
            try {
                Log.d(TAG, "Đang tải chi tiết đơn hàng #${orderId}...")
                val detail = getOrderDetail(orderId)
                _orderDetail.value = detail
            } catch (e: Exception) {
                Log.e(TAG, "Error loading order detail: ${e.message}")
                _orderDetail.value = Resource.Error("Không thể tải chi tiết đơn: ${e.message}")
            }
        }
    }

    suspend fun acceptOrderAndWaitForUpdate(orderId: Long, customerId: Long): Boolean {
        _receiveOrderState.value = Resource.Loading()
        try {
            Log.d(TAG, "Đang gọi API nhận đơn: $orderId")
            val result = receiveOrder(orderId)
            _receiveOrderState.value = result

            if (result is Resource.Success) {
                Log.d(TAG, "Nhận đơn thành công, cập nhật danh sách")
                NotificationHelper.showOrderReceivedNotification(context, orderId)
                _currentChatOrder.value = Pair(orderId, customerId)

                val isFirst = !_isFirstOrderReceived.value
                if (isFirst) {
                    _isFirstOrderReceived.value = true
                    delay(1200)
                } else {
                    delay(800)
                }

                _myOrders.value = Resource.Loading()
                val myOrdersResult = getReceivedOrders()

                // Xử lý an toàn khi load lại danh sách sau khi nhận đơn
                _myOrders.value = safeResource(myOrdersResult)

                // Logic retry nếu danh sách vẫn rỗng (do server update chậm)
                if (isFirst) {
                    val currentData = (_myOrders.value as? Resource.Success)?.data
                    if (currentData?.orders.isNullOrEmpty()) {
                        delay(1000)
                        val retryResult = getReceivedOrders()
                        _myOrders.value = safeResource(retryResult)
                    }
                }

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

    fun acceptOrder(orderId: Long, customerId: Long) {
        viewModelScope.launch {
            acceptOrderAndWaitForUpdate(orderId, customerId)
        }
    }

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
                    Log.d(TAG, "Đã xoá đoạn chat cho đơn #$orderId sau khi giao thành công")
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