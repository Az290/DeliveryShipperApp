package com.example.deliveryshipperapp.ui.map

import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.deliveryshipperapp.R
import com.example.deliveryshipperapp.data.remote.api.DirectionsApi
import com.example.deliveryshipperapp.data.remote.api.DirectionsResponseRaw
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Composable
fun MapScreen(
    userLat: Double,
    userLng: Double,
    driverLat: Double,
    driverLng: Double,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val accessToken = context.getString(R.string.MAPBOX_ACCESS_TOKEN)

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) { style ->
                    val annotationApi = annotations
                    val manager = annotationApi.createPointAnnotationManager()

                    val userPoint = Point.fromLngLat(userLng, userLat)
                    val driverPoint = Point.fromLngLat(driverLng, driverLat)

                    // marker khách
                    manager.create(PointAnnotationOptions().withPoint(userPoint))
                    // marker shipper
                    manager.create(PointAnnotationOptions().withPoint(driverPoint))

                    // camera
                    getMapboxMap().setCamera(
                        CameraOptions.Builder()
                            .center(userPoint)
                            .zoom(13.0)
                            .build()
                    )

                    // gọi REST API lấy route
                    val retrofit = Retrofit.Builder()
                        .baseUrl("https://api.mapbox.com/") // base Mapbox API
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

                    val api = retrofit.create(DirectionsApi::class.java)
                    val coordinates = "${driverLng},${driverLat};${userLng},${userLat}"

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response: DirectionsResponseRaw = api.getRoute(
                                coordinates = coordinates,
                                accessToken = accessToken
                            )
                            val route = response.routes.firstOrNull()
                            if (route != null) {
                                val lineString = LineString.fromPolyline(route.geometry, 6)
                                // vẽ lên map trên Main
                                launch(Dispatchers.Main) {
                                    style.addSource(
                                        geoJsonSource("route-source") { geometry(lineString) }
                                    )
                                    style.addLayer(
                                        lineLayer("route-layer", "route-source") {
                                            lineColor("#2196F3")
                                            lineWidth(4.0)
                                            lineJoin(LineJoin.ROUND)
                                        }
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("Mapbox", "Route API error: ${e.message}")
                        }
                    }
                }
            }
        },
        modifier = modifier
    )
}