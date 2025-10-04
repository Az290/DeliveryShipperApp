package com.example.deliveryshipperapp.di

import android.content.Context
import com.example.deliveryshipperapp.data.local.DataStoreManager
import com.example.deliveryshipperapp.data.remote.ApiClient
import com.example.deliveryshipperapp.data.remote.api.AuthApi
import com.example.deliveryshipperapp.data.remote.api.ShipperApi
import com.example.deliveryshipperapp.data.remote.interceptor.AuthInterceptor
import com.example.deliveryshipperapp.data.repository.AuthRepository
import com.example.deliveryshipperapp.data.repository.ShipperRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import retrofit2.Retrofit
import javax.inject.Singleton
import com.example.deliveryshipperapp.data.remote.api.ProfileApi
import com.example.deliveryshipperapp.data.repository.ProfileRepository

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStoreManager =
        DataStoreManager(context)

    @Provides @Singleton
    fun provideAuthInterceptor(ds: DataStoreManager): Interceptor = AuthInterceptor(ds)

    @Provides @Singleton
    fun provideRetrofit(interceptor: Interceptor): Retrofit =
        ApiClient.create(interceptor)

    @Provides @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides @Singleton
    fun provideShipperApi(retrofit: Retrofit): ShipperApi =
        retrofit.create(ShipperApi::class.java)

    @Provides @Singleton
    fun provideAuthRepository(api: AuthApi, ds: DataStoreManager): AuthRepository =
        AuthRepository(api, ds)

    @Provides @Singleton
    fun provideShipperRepository(api: ShipperApi): ShipperRepository =
        ShipperRepository(api)

    @Provides @Singleton
    fun provideProfileApi(retrofit: Retrofit): ProfileApi =
        retrofit.create(ProfileApi::class.java)

    @Provides @Singleton
    fun provideProfileRepository(api: ProfileApi): ProfileRepository =
        ProfileRepository(api)
}