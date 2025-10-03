package com.example.deliveryshipperapp.domain.usecase

import com.example.deliveryshipperapp.data.repository.ProfileRepository
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val repo: ProfileRepository
) {
    suspend operator fun invoke() = repo.getProfile()
}