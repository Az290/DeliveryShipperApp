package com.example.deliveryshipperapp.domain.usecase

import com.example.deliveryshipperapp.data.repository.ShipperRepository
import javax.inject.Inject

class ReceiveOrderUseCase @Inject constructor(private val repo:ShipperRepository){
    suspend operator fun invoke(orderId:Long)= repo.receiveOrder(orderId)
}