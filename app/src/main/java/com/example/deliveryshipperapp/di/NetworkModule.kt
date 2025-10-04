package com.example.deliveryshipperapp.di

import android.content.Context
import com.example.deliveryshipperapp.data.local.DataStoreManager
import com.example.deliveryshipperapp.data.remote.ApiClient
import com.example.deliveryshipperapp.data.remote.api.AuthApi
import com.example.deliveryshipperapp.data.remote.api.ProfileApi
import com.example.deliveryshipperapp.data.remote.api.ShipperApi
import com.example.deliveryshipperapp.data.remote.interceptor.AuthInterceptor
import com.example.deliveryshipperapp.data.repository.AuthRepository
import com.example.deliveryshipperapp.data.repository.ProfileRepository
import com.example.deliveryshipperapp.data.repository.ShipperRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStoreManager = DataStoreManager(context)

    // ✅ Truyền cả context và DataStore vào interceptor
    @Provides @Singleton
    fun provideAuthInterceptor(
        @ApplicationContext context: Context,
        ds: DataStoreManager
    ): Interceptor = AuthInterceptor(context, ds)

    @Provides @Singleton
    fun provideRetrofit(interceptor: Interceptor): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)        // gắn interceptor mới
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(com.example.deliveryshipperapp.utils.Constants.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides @Singleton
    fun provideShipperApi(retrofit: Retrofit): ShipperApi =
        retrofit.create(ShipperApi::class.java)

    @Provides @Singleton
    fun provideProfileApi(retrofit: Retrofit): ProfileApi =
        retrofit.create(ProfileApi::class.java)

    @Provides @Singleton
    fun provideAuthRepository(api: AuthApi, ds: DataStoreManager): AuthRepository =
        AuthRepository(api, ds)

    @Provides @Singleton
    fun provideShipperRepository(api: ShipperApi): ShipperRepository =
        ShipperRepository(api)

    @Provides @Singleton
    fun provideProfileRepository(api: ProfileApi): ProfileRepository =
        ProfileRepository(api)
}