package com.example.deliveryshipperapp.data.repository

import com.example.deliveryshipperapp.data.local.DataStoreManager
import com.example.deliveryshipperapp.data.remote.api.AuthApi
import com.example.deliveryshipperapp.data.remote.dto.AuthResponseDto
import com.example.deliveryshipperapp.data.remote.dto.LoginRequestDto
import com.example.deliveryshipperapp.utils.Resource
import retrofit2.Response

class AuthRepository(
    private val api: AuthApi,
    private val dataStore: DataStoreManager
) {
    suspend fun login(email:String,password:String): Resource<AuthResponseDto> =
        safeCall { api.login(LoginRequestDto(email,password)) }

    private inline fun <T> safeCall(apiCall:()->Response<T>):Resource<T>{
        return try {
            val resp= apiCall()
            if(resp.isSuccessful){
                resp.body()?.let { Resource.Success(it) } ?: Resource.Error("Empty body")
            } else Resource.Error(resp.errorBody()?.string()?:"Unknown error")
        } catch (e:Exception){
            Resource.Error(e.message?:"Unexpected error")
        }
    }

    suspend fun saveTokens(access:String,refresh:String){
        dataStore.saveTokens(access,refresh)
    }
    suspend fun logout(){
        dataStore.clearTokens()
    }
}