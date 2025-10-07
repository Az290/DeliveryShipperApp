package com.example.deliveryshipperapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.deliveryshipperapp.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.longPreferencesKey
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.PREFS_NAME)

class DataStoreManager(private val context: Context) {
    companion object {
        val ACCESS_TOKEN_KEY = stringPreferencesKey(Constants.KEY_ACCESS_TOKEN)
        val REFRESH_TOKEN_KEY = stringPreferencesKey(Constants.KEY_REFRESH_TOKEN)
        val USER_ID_KEY = longPreferencesKey("user_id") // Lấy userid ddeer load laij tin nhawns
    }

    val accessToken: Flow<String?> = context.dataStore.data.map { it[ACCESS_TOKEN_KEY] }
    val refreshToken: Flow<String?> = context.dataStore.data.map { it[REFRESH_TOKEN_KEY] }
    // ✅ thêm flow để đọc userId
    val userId: Flow<Long> = context.dataStore.data.map { it[USER_ID_KEY] ?: 0L }

    // ✅ thêm hàm lưu userId
    suspend fun saveUserId(id: Long) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID_KEY] = id
        }
    }

    suspend fun saveTokens(access:String,refresh:String){
        context.dataStore.edit { prefs->
            prefs[ACCESS_TOKEN_KEY]=access
            prefs[REFRESH_TOKEN_KEY]=refresh
        }
    }

    suspend fun clearTokens(){
        context.dataStore.edit{ it.clear() }
    }
}