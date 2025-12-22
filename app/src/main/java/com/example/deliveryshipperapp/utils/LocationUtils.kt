package com.example.deliveryshipperapp.utils

import android.content.Context
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

suspend fun getAddressFromLatLng(context: Context, lat: Double, lng: Double): String {
    return withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale("vi", "VN"))
            // Lấy tối đa 1 kết quả
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                // Ưu tiên lấy dòng địa chỉ đầy đủ, nếu không thì lấy đường + quận
                address.getAddressLine(0) ?: "${address.thoroughfare}, ${address.subAdminArea}"
            } else {
                "Toạ độ: $lat, $lng"
            }
        } catch (e: Exception) {
            "Không xác định được vị trí"
        }
    }
}