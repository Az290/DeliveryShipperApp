package com.example.deliveryshipperapp.di

import com.example.deliveryshipperapp.data.repository.AuthRepository
import com.example.deliveryshipperapp.data.repository.ShipperRepository
import com.example.deliveryshipperapp.data.repository.UserRepository
import com.example.deliveryshipperapp.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.deliveryshipperapp.data.repository.ProfileRepository

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides @Singleton
    fun provideLoginUseCase(repo: AuthRepository): LoginUseCase = LoginUseCase(repo)

    @Provides @Singleton
    fun provideGetAvailableOrdersUseCase(repo: ShipperRepository) =
        GetAvailableOrdersUseCase(repo)


    @Provides @Singleton
    fun provideGetOrderDetailUseCase(repo: ShipperRepository) =
        GetOrderDetailUseCase(repo)

    @Provides @Singleton
    fun provideReceiveOrderUseCase(repo: ShipperRepository) =
        ReceiveOrderUseCase(repo)

    @Provides @Singleton
    fun provideUpdateOrderUseCase(repo: ShipperRepository) =
        UpdateOrderUseCase(repo)

    @Provides @Singleton
    fun provideGetUserUseCase(repo: UserRepository) =
        GetUserUseCase(repo)
    @Provides @Singleton
    fun provideGetProfileUseCase(repo: ProfileRepository) = GetProfileUseCase(repo)

    @Provides
    @Singleton
    fun provideGetReceivedOrdersUseCase(repo: ShipperRepository) =
        GetReceivedOrdersUseCase(repo)
}