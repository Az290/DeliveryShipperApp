package com.example.deliveryshipperapp.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class CryptoManager {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    private val encryptCipher = Cipher.getInstance(TRANSFORMATION)
    private val decryptCipher = Cipher.getInstance(TRANSFORMATION)

    // Tạo hoặc lấy key từ Android Keystore
    private fun getKey(): SecretKey {
        val existingKey = keyStore.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey()
    }

    private fun createKey(): SecretKey {
        return KeyGenerator.getInstance(ALGORITHM, "AndroidKeyStore").apply {
            init(
                KeyGenParameterSpec.Builder(
                    ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(BLOCK_MODE)
                    .setEncryptionPaddings(PADDING)
                    .setUserAuthenticationRequired(false) // Set true nếu muốn yêu cầu vân tay/mã pin
                    .setRandomizedEncryptionRequired(true)
                    .build()
            )
        }.generateKey()
    }

    // 🔒 HÀM MÃ HÓA: String -> String (Encrypted Base64)
    fun encrypt(data: String): String {
        if (data.isBlank()) return ""
        try {
            val bytes = data.toByteArray(Charsets.UTF_8)
            encryptCipher.init(Cipher.ENCRYPT_MODE, getKey())
            val encryptedBytes = encryptCipher.doFinal(bytes)
            val iv = encryptCipher.iv // Vector khởi tạo (cần để giải mã)

            // Kết hợp IV và Dữ liệu đã mã hóa
            val combined = ByteArray(iv.size + encryptedBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)

            return Base64.encodeToString(combined, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    // 🔓 HÀM GIẢI MÃ: String (Encrypted Base64) -> String
    fun decrypt(encryptedData: String?): String? {
        if (encryptedData.isNullOrBlank()) return null
        try {
            val combined = Base64.decode(encryptedData, Base64.DEFAULT)

            // Tách IV ra (AES GCM thường dùng IV 12 bytes)
            val iv = ByteArray(12)
            System.arraycopy(combined, 0, iv, 0, 12)

            // Phần còn lại là dữ liệu
            val encryptedBytes = ByteArray(combined.size - 12)
            System.arraycopy(combined, 12, encryptedBytes, 0, encryptedBytes.size)

            val spec = GCMParameterSpec(128, iv)
            decryptCipher.init(Cipher.DECRYPT_MODE, getKey(), spec)

            val decoded = decryptCipher.doFinal(encryptedBytes)
            return String(decoded, Charsets.UTF_8)
        } catch (e: Exception) {
            // Nếu giải mã lỗi (do key thay đổi hoặc dữ liệu cũ chưa mã hóa), trả về null
            e.printStackTrace()
            return null
        }
    }

    companion object {
        private const val ALIAS = "secret_token_key"
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
    }
}