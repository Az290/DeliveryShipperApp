package com.example.deliveryshipperapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliveryshipperapp.data.remote.dto.AuthResponseDto
import com.example.deliveryshipperapp.domain.usecase.LoginUseCase
import com.example.deliveryshipperapp.data.repository.AuthRepository
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
                authRepo.saveTokens(data.access_token,data.refresh_token)
            }
            _loginState.value = res
        }
    }

    fun logout() {
        viewModelScope.launch { authRepo.logout() }
    }
}