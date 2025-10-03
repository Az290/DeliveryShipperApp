package com.example.deliveryshipperapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deliveryshipperapp.data.remote.dto.UserDto
import com.example.deliveryshipperapp.domain.usecase.GetProfileUseCase
import com.example.deliveryshipperapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfile: GetProfileUseCase
) : ViewModel() {

    private val _profile = MutableStateFlow<Resource<UserDto>>(Resource.Loading())
    val profile: StateFlow<Resource<UserDto>> = _profile

    fun loadProfile() {
        viewModelScope.launch {
            _profile.value = getProfile()
        }
    }
}