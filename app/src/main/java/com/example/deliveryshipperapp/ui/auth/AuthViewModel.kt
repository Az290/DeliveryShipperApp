package com.example.deliveryshipperapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliveryshipperapp.data.remote.dto.AuthResponseDto
import com.example.deliveryshipperapp.domain.usecase.LoginUseCase
import com.example.deliveryshipperapp.data.repository.AuthRepository
import com.example.deliveryshipperapp.utils.JwtUtils // ✅ Thêm import này
import com.example.deliveryshipperapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val authRepo: AuthRepository
): ViewModel() {

    private val _loginState = MutableStateFlow<Resource<AuthResponseDto>?>(null)
    val loginState: StateFlow<Resource<AuthResponseDto>?> = _loginState

    fun login(email:String,password:String){
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            val res = loginUseCase(email,password)

            if(res is Resource.Success){
                val data = res.data!!

                // 🔥 SỬA: Lấy role từ Token để kiểm tra
                val role = JwtUtils.getRoleFromToken(data.accessToken)

                // Kiểm tra nếu role là "shipper" (hoặc chứa chữ shipper)
                if (role != null && role.contains("shipper", ignoreCase = true)) {
                    // ✅ Đúng quyền -> Lưu token và báo thành công
                    authRepo.saveTokens(data.accessToken, data.refreshToken ?: "")
                    _loginState.value = res
                } else {
                    // ❌ Sai quyền -> Báo lỗi và KHÔNG lưu token
                    _loginState.value = Resource.Error("Vui lòng sử dụng tài khoản Shipper để đăng nhập!")
                }
            } else {
                // API trả về lỗi (sai pass, lỗi mạng...)
                _loginState.value = res
            }
        }
    }

    fun logout() {
        viewModelScope.launch { authRepo.logout() }
    }
}