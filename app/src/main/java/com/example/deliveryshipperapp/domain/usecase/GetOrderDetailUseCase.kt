package com.example.deliveryshipperapp.domain.usecase

import com.example.deliveryshipperapp.data.repository.ShipperRepository
import javax.inject.Inject

class GetOrderDetailUseCase @Inject constructor(private val repo:ShipperRepository){
    suspend operator fun invoke(id:Long)= repo.getOrderDetail(id)
}