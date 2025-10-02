package com.example.deliveryshipperapp.domain.usecase

import com.example.deliveryshipperapp.data.repository.AuthRepository
import com.example.deliveryshipperapp.utils.Resource
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val repo:AuthRepository){
    suspend operator fun invoke(email:String,password:String)= repo.login(email,password)
}