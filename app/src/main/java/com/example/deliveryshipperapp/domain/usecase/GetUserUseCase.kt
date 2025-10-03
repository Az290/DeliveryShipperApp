package com.example.deliveryshipperapp.domain.usecase

import com.example.deliveryshipperapp.data.repository.UserRepository
import javax.inject.Inject

class GetUserUseCase @Inject constructor(
    private val repo: UserRepository
) {
    suspend operator fun invoke(id: Long) = repo.getUser(id)
}