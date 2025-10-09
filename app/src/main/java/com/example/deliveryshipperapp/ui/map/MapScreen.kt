package com.example.deliveryshipperapp.ui.map

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
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

                    val drawable = ContextCompat.getDrawable(context, R.mipmap.ic_customerlocation)
                    val bitmap = Bitmap.createBitmap(
                        drawable!!.intrinsicWidth,
                        drawable.intrinsicHeight,
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = Canvas(bitmap)
                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                    drawable.draw(canvas)
                    style.addImage("customer_icon", bitmap)

                    val userPoint = Point.fromLngLat(userLng, userLat)
                    val driverPoint = Point.fromLngLat(driverLng, driverLat)

                    manager.create(
                        PointAnnotationOptions()
                            .withPoint(userPoint)
                            .withIconImage("customer_icon")
                    )
                    manager.create(PointAnnotationOptions().withPoint(driverPoint))

                    getMapboxMap().setCamera(
                        CameraOptions.Builder()
                            .center(userPoint)
                            .zoom(16.0)
                            .build()
                    )

                    val retrofit = Retrofit.Builder()
                        .baseUrl("https://api.mapbox.com/")
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
                            android.util.Log.e("Mapbox", "Route API error: ${e.message}")
                        }
                    }
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}