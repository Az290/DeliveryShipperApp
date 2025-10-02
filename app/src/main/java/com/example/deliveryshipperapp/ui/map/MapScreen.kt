package com.example.deliveryshipperapp.ui.map

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.deliveryshipperapp.R
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager

@Composable
fun MapScreen(
    userLat: Double,
    userLng: Double,
    driverLat: Double,
    driverLng: Double,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current

    // Tạo MapView đơn giản
    val mapView = remember {
        try {
            MapView(context)
        } catch (e: Exception) {
            null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // MapView hoặc fallback UI
        if (mapView != null) {
            AndroidView(
                factory = { mapView },
                modifier = Modifier.fillMaxSize()
            )

            // Hiển thị marker và camera trong LaunchedEffect
            LaunchedEffect(userLat, userLng, driverLat, driverLng) {
                try {
                    mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
                        val annotationApi = mapView.annotations
                        val manager = annotationApi.createPointAnnotationManager()
                        manager.deleteAll()

                        // Marker cho khách hàng
                        val userPoint = Point.fromLngLat(userLng, userLat)
                        manager.create(PointAnnotationOptions().withPoint(userPoint))

                        // Marker cho shipper
                        val driverPoint = Point.fromLngLat(driverLng, driverLat)
                        manager.create(PointAnnotationOptions().withPoint(driverPoint))

                        // Di chuyển camera đến vị trí khách hàng
                        mapView.getMapboxMap().setCamera(
                            CameraOptions.Builder()
                                .center(userPoint)
                                .zoom(12.0)
                                .build()
                        )
                    }
                } catch (e: Exception) {
                    // Xử lý lỗi nếu có
                }
            }

            // Cleanup khi component bị hủy
            DisposableEffect(Unit) {
                onDispose {
                    try {
                        mapView.onStop()
                        mapView.onDestroy()
                    } catch (e: Exception) {
                        // Xử lý lỗi nếu có
                    }
                }
            }
        } else {
            // Fallback UI khi không thể tạo MapView
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Không thể hiển thị bản đồ")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Vị trí khách hàng: $userLat, $userLng")
                    Text("Vị trí shipper: $driverLat, $driverLng")
                }
            }
        }

        // Nút Back (luôn hiển thị, bất kể có MapView hay không)
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                tonalElevation = 4.dp
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Quay lại",
                    modifier = Modifier.padding(8.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}