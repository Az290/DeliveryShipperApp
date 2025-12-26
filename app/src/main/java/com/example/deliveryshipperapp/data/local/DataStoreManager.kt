package com.example.deliveryshipperapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.deliveryshipperapp.utils.Constants
import com.example.deliveryshipperapp.utils.CryptoManager // ✅ Thêm import này
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

// Khởi tạo DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.PREFS_NAME)

@Singleton
class DataStoreManager @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context
) {
    // ✅ THÊM: Khởi tạo CryptoManager để mã hóa/giải mã
    private val cryptoManager = CryptoManager()

    companion object {
        val ACCESS_TOKEN_KEY = stringPreferencesKey(Constants.KEY_ACCESS_TOKEN)
        val REFRESH_TOKEN_KEY = stringPreferencesKey(Constants.KEY_REFRESH_TOKEN)
        val USER_ID_KEY = longPreferencesKey("user_id") // Key cho UserId
    }

    // 🔥 CACHE RAM: Biến lưu tạm để truy cập siêu nhanh (Tránh ANR)
    @Volatile private var cachedAccessToken: String? = null
    @Volatile private var cachedRefreshToken: String? = null

    // Scope riêng để chạy việc nạp cache ngầm
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        // 🚀 TỰ ĐỘNG NẠP CACHE NGAY KHI APP KHỞI ĐỘNG
        scope.launch {
            context.dataStore.data.collect { prefs ->
                // ✅ SỬA: Giải mã token từ đĩa trước khi đưa vào RAM
                cachedAccessToken = cryptoManager.decrypt(prefs[ACCESS_TOKEN_KEY])
                cachedRefreshToken = cryptoManager.decrypt(prefs[REFRESH_TOKEN_KEY])
            }
        }
    }

    // --- CÁC FLOW (Giữ nguyên cho các màn hình khác observe) ---
    // ✅ SỬA: Giải mã khi stream dữ liệu ra
    val accessToken: Flow<String?> = context.dataStore.data.map { cryptoManager.decrypt(it[ACCESS_TOKEN_KEY]) }
    val refreshToken: Flow<String?> = context.dataStore.data.map { cryptoManager.decrypt(it[REFRESH_TOKEN_KEY]) }

    // ✅ Giữ nguyên phần UserID của bạn
    val userId: Flow<Long> = context.dataStore.data.map { it[USER_ID_KEY] ?: 0L }

    // --- CÁC HÀM LẤY NHANH (CHO INTERCEPTOR) ---

    // ⚡ Lấy Access Token 0ms
    fun getAccessTokenInstant(): String? {
        if (cachedAccessToken != null) return cachedAccessToken
        return runBlocking {
            // ✅ SỬA: Giải mã nếu phải đọc từ đĩa (fallback)
            val encryptedToken = context.dataStore.data.map { it[ACCESS_TOKEN_KEY] }.first()
            val token = cryptoManager.decrypt(encryptedToken)
            cachedAccessToken = token
            token
        }
    }

    // ⚡ Lấy Refresh Token 0ms
    fun getRefreshTokenInstant(): String? {
        if (cachedRefreshToken != null) return cachedRefreshToken
        return runBlocking {
            // ✅ SỬA: Giải mã nếu phải đọc từ đĩa (fallback)
            val encryptedToken = context.dataStore.data.map { it[REFRESH_TOKEN_KEY] }.first()
            val token = cryptoManager.decrypt(encryptedToken)
            cachedRefreshToken = token
            token
        }
    }

    // --- CÁC HÀM LƯU / XÓA ---

    // ✅ Giữ nguyên hàm lưu UserID
    suspend fun saveUserId(id: Long) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID_KEY] = id
        }
    }

    suspend fun saveTokens(access: String, refresh: String) {
        // Cập nhật RAM ngay lập tức (Lưu ý: RAM giữ bản KHÔNG mã hóa để dùng luôn)
        cachedAccessToken = access
        cachedRefreshToken = refresh

        // Lưu xuống đĩa
        context.dataStore.edit { prefs ->
            // ✅ SỬA: Mã hóa trước khi lưu xuống file
            prefs[ACCESS_TOKEN_KEY] = cryptoManager.encrypt(access)
            prefs[REFRESH_TOKEN_KEY] = cryptoManager.encrypt(refresh)
        }
    }

    suspend fun clearTokens() {
        // Xóa RAM
        cachedAccessToken = null
        cachedRefreshToken = null
        // Xóa đĩa
        context.dataStore.edit { it.clear() }
    }
}