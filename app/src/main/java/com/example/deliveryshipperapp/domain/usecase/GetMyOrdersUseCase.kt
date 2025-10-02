package com.example.deliveryshipperapp.domain.usecase

import com.example.deliveryshipperapp.data.repository.ShipperRepository
import javax.inject.Inject

class GetMyOrdersUseCase @Inject constructor(private val repo:ShipperRepository){
    suspend operator fun invoke()= repo.getMyOrders()
}