package com.example.deliveryshipperapp.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.geojson.Point
import android.location.LocationManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.deliveryshipperapp.R
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager


// Helper function để convert drawable thành bitmap
fun getBitmapFromDrawable(context: Context, drawableId: Int): Bitmap? {
    val drawable = ContextCompat.getDrawable(context, drawableId) ?: return null

    if (drawable is BitmapDrawable) {
        return drawable.bitmap
    }

    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth.takeIf { it > 0 } ?: 1,
        drawable.intrinsicHeight.takeIf { it > 0 } ?: 1,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun MapFullScreen(
    driverLat: Double,
    driverLng: Double,
    userLat: Double,
    userLng: Double
) {
    var shipperLatLng by remember { mutableStateOf<Point?>(null) }

    val context = LocalContext.current
    val locationManager = ContextCompat.getSystemService(context, LocationManager::class.java)

    var polylineAnnotationManager by remember { mutableStateOf<PolylineAnnotationManager?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var manager by remember { mutableStateOf<com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (!granted) {
            Toast.makeText(context, "❌ Cần quyền vị trí để sử dụng chức năng này", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lộ trình giao hàng") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF667eea),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        mapView = this
                        val mapboxMap = getMapboxMap()
                        mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) { style ->
                            manager = annotations.createPointAnnotationManager()
                            polylineAnnotationManager = annotations.createPolylineAnnotationManager()


                            // Đăng ký icon PNG trong mipmap
                            getBitmapFromDrawable(ctx, R.mipmap.ic_customerlocation)
                                ?.let { style.addImage("customer_icon", it) }
                            getBitmapFromDrawable(ctx, R.mipmap.ic_shiperlocation)
                                ?.let { style.addImage("shiper_icon", it) }

                            // Mặc định: zoom tới khách hàng
                            val customerPoint = Point.fromLngLat(userLng, userLat)
                            mapboxMap.setCamera(
                                CameraOptions.Builder()
                                    .center(customerPoint)
                                    .zoom(13.0)
                                    .build()
                            )

                            // Marker khách hàng
                            manager?.create(
                                PointAnnotationOptions()
                                    .withPoint(customerPoint)
                                    .withIconImage("customer_icon")
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // 📍 Nút di chuyển tới vị trí khách hàng
            FloatingActionButton(
                onClick = {
                    val mapboxMap = mapView?.getMapboxMap() ?: return@FloatingActionButton
                    val customerPoint = Point.fromLngLat(userLng, userLat)
                    manager?.deleteAll()
                    manager?.create(
                        PointAnnotationOptions()
                            .withPoint(customerPoint)
                            .withIconImage("customer_icon")
                    )
                    mapboxMap.setCamera(
                        CameraOptions.Builder()
                            .center(customerPoint)
                            .zoom(15.0)
                            .build()
                    )
                    Toast.makeText(context, "📍 Đã đến vị trí khách hàng", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 90.dp),
                containerColor = Color(0xFFFF7043),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Place, contentDescription = "Vị trí khách hàng")
            }
            /*
             🚚 Nút xác định vị trí hiện tại (shipper)
                        FloatingActionButton(
                            onClick = {
                                val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                                val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                                if (fine != PackageManager.PERMISSION_GRANTED && coarse != PackageManager.PERMISSION_GRANTED) {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                    return@FloatingActionButton
                                }

                                val location: Location? =
                                    locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                                        ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                                if (location != null) {
                                    val lat = location.latitude
                                    val lng = location.longitude

                                    val shipperPoint = Point.fromLngLat(lng, lat)

                                    val mapboxMap = mapView?.getMapboxMap() ?: return@FloatingActionButton
                                    manager?.deleteAll()
                                    manager?.create(
                                        PointAnnotationOptions()
                                            .withPoint(shipperPoint)
                                            .withIconImage("shiper_icon")
                                    )
                                    mapboxMap.setCamera(
                                        CameraOptions.Builder()
                                            .center(shipperPoint)
                                            .zoom(15.0)
                                            .build()
                                    )
                                    Toast.makeText(context, "🚚 Di chuyển đến vị trí shipper", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Không thể lấy vị trí hiện tại", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(20.dp),
                            containerColor = Color(0xFF667eea),
                            contentColor = Color.White
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = "Vị trí hiện tại")
                        }
                        */

            FloatingActionButton(
                onClick = {
                    val mapboxMap = mapView?.getMapboxMap() ?: return@FloatingActionButton


                    // Luôn dùng toạ độ cố định 20.981652, 105.791752
                    val lat = 20.981652
                    val lng = 105.791752
                    shipperLatLng = Point.fromLngLat(lng, lat)
                    val shipperPoint = Point.fromLngLat(lng, lat)

                    manager?.deleteAll()
                    manager?.create(
                        PointAnnotationOptions()
                            .withPoint(shipperPoint)
                            .withIconImage("shiper_icon")
                    )
                    mapboxMap.setCamera(
                        CameraOptions.Builder()
                            .center(shipperPoint)
                            .zoom(15.0)
                            .build()
                    )
                    Toast.makeText(context, "🚚 Di chuyển đến vị trí shipper (tọa độ cố định)", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 160.dp),
                containerColor = Color(0xFF667eea),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Vị trí hiện tại")
            }

            //Bị nhảy SANG TRUNG QUỐC CHỈ ĐƯỜNG SANG TRUNG QUỐC
//            FloatingActionButton(
//                onClick = {
//                    val mapboxMap = mapView?.getMapboxMap() ?: return@FloatingActionButton
//
//                    //val shipperPoint = Point.fromLngLat(driverLng, driverLat)
//                    //val shipperPoint = Point.fromLngLat(105.791752, 20.981652)
//
//                    val shipperPoint = shipperLatLng ?: Point.fromLngLat(105.791752, 20.981652)
//
//                    val customerPoint = Point.fromLngLat(userLng, userLat)
//                    val accessToken = context.getString(R.string.mapbox_access_token)
//
//                    Log.d("MAPBOX_DEBUG", "Shipper Point: ${shipperPoint.latitude()}, ${shipperPoint.longitude()}")
//                    Log.d("MAPBOX_DEBUG", "Customer Point: ${customerPoint.latitude()}, ${customerPoint.longitude()}")
//
//                    val url =
//                        "https://api.mapbox.com/directions/v5/mapbox/driving/" +
//                                "${shipperPoint.longitude()},${shipperPoint.latitude()};" +
//                                "${customerPoint.longitude()},${customerPoint.latitude()}?" +
//                                "alternatives=false&geometries=polyline6&overview=full&access_token=$accessToken"
//
//                    Log.d("MAPBOX_DEBUG", "Request URL: $url")
//
//                    Thread {
//                        try {
//                            val client = okhttp3.OkHttpClient()
//                            val request = okhttp3.Request.Builder().url(url).build()
//                            val response = client.newCall(request).execute()
//                            val body = response.body?.string()
//
//                            if (body != null) {
//                                val json = org.json.JSONObject(body)
//                                val routes = json.getJSONArray("routes")
//                                if (routes.length() > 0) {
//                                    val route = routes.getJSONObject(0)
//                                    val geometry = route.getString("geometry")
//
//                                    // ✅ decode polyline6 về LineString hợp lệ
//                                    val lineString = com.mapbox.geojson.LineString.fromPolyline(geometry, 6)
//                                    val coordinates = lineString.coordinates()
//
//                                    Log.d("MAPBOX_DEBUG", "Route points count: ${coordinates.size}")
//                                    Log.d("MAPBOX_DEBUG", "First point: ${coordinates.first().latitude()}, ${coordinates.first().longitude()}")
//                                    Log.d("MAPBOX_DEBUG", "Last point: ${coordinates.last().latitude()}, ${coordinates.last().longitude()}")
//
//                                    (context as android.app.Activity).runOnUiThread {
//                                        mapboxMap.getStyle { style ->
//
//                                            // Xóa route cũ
//                                            if (style.styleLayerExists("route-layer")) style.removeStyleLayer("route-layer")
//                                            if (style.styleSourceExists("route-source")) style.removeStyleSource("route-source")
//
//                                            // Vẽ route mới
//                                            style.addSource(
//                                                com.mapbox.maps.extension.style.sources.generated.geoJsonSource("route-source") {
//                                                    geometry(lineString)
//                                                }
//                                            )
//                                            style.addLayer(
//                                                com.mapbox.maps.extension.style.layers.generated.lineLayer("route-layer", "route-source") {
//                                                    lineColor("#FF0000")
//                                                    lineWidth(4.0)
//                                                    lineOpacity(0.8)
//                                                }
//                                            )
//
//                                            // Zoom vừa khung
//                                            if (coordinates.isNotEmpty()) {
//                                                var minLat = coordinates.first().latitude()
//                                                var maxLat = coordinates.first().latitude()
//                                                var minLng = coordinates.first().longitude()
//                                                var maxLng = coordinates.first().longitude()
//                                                for (p in coordinates) {
//                                                    minLat = minOf(minLat, p.latitude())
//                                                    maxLat = maxOf(maxLat, p.latitude())
//                                                    minLng = minOf(minLng, p.longitude())
//                                                    maxLng = maxOf(maxLng, p.longitude())
//                                                }
//                                                val centerLat = (minLat + maxLat) / 2
//                                                val centerLng = (minLng + maxLng) / 2
//                                                mapboxMap.setCamera(
//                                                    com.mapbox.maps.CameraOptions.Builder()
//                                                        .center(Point.fromLngLat(centerLng, centerLat))
//                                                        .zoom(12.5)
//                                                        .build()
//                                                )
//                                            }
//                                            Toast.makeText(context, "🧭 Đã hiển thị đường đi hợp lý", Toast.LENGTH_SHORT).show()
//                                        }
//                                    }
//                                } else {
//                                    (context as android.app.Activity).runOnUiThread {
//                                        Toast.makeText(context, "Không tìm thấy tuyến đường!", Toast.LENGTH_SHORT).show()
//                                    }
//                                }
//                            }
//                        } catch (e: Exception) {
//                            Log.e("MAPBOX_ERROR", "Lỗi lấy tuyến đường: ${e.message}")
//                            (context as android.app.Activity).runOnUiThread {
//                                Toast.makeText(context, "Lỗi vẽ đường: ${e.message}", Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                    }.start()
//                },
//                modifier = Modifier
//                    .align(Alignment.BottomEnd)
//                    .padding(end = 20.dp, bottom = 20.dp),
//                containerColor = Color(0xFF43A047),
//                contentColor = Color.White
//            ) {
//                Icon(Icons.Default.Route, contentDescription = "Chỉ đường")
//            }


            FloatingActionButton(
                onClick = {
                    val mapboxMap = mapView?.getMapboxMap() ?: return@FloatingActionButton

                    // Lấy tọa độ shipper và khách
                    val shipperPoint = shipperLatLng ?: Point.fromLngLat(105.791752, 20.981652)
                    val customerPoint = Point.fromLngLat(userLng, userLat)

                    Log.d("GEOAPIFY_DEBUG", "Shipper: ${shipperPoint.latitude()}, ${shipperPoint.longitude()}")
                    Log.d("GEOAPIFY_DEBUG", "Customer: ${customerPoint.latitude()}, ${customerPoint.longitude()}")

                    // === Gọi Geoapify Routing API ===
                    val apiKey = "fd15f9c6b4444a64bef013942942bbfe"
                    val url = "https://api.geoapify.com/v1/routing?" +
                            "waypoints=${shipperPoint.latitude()},${shipperPoint.longitude()}|" +
                            "${customerPoint.latitude()},${customerPoint.longitude()}" +
                            "&mode=drive&apiKey=$apiKey"

                    Log.d("GEOAPIFY_DEBUG", "Request URL: $url")

                    Thread {
                        try {
                            val client = okhttp3.OkHttpClient()
                            val request = okhttp3.Request.Builder().url(url).build()
                            val response = client.newCall(request).execute()
                            val body = response.body?.string()

                            if (body != null) {
                                val json = org.json.JSONObject(body)
                                val features = json.getJSONArray("features")
                                if (features.length() > 0) {
                                    val geometry = features.getJSONObject(0).getJSONObject("geometry")
                                    val coords = geometry.getJSONArray("coordinates").getJSONArray(0)

                                    val points = mutableListOf<Point>()
                                    for (i in 0 until coords.length()) {
                                        val coord = coords.getJSONArray(i)
                                        val lng = coord.getDouble(0)
                                        val lat = coord.getDouble(1)
                                        points.add(Point.fromLngLat(lng, lat))
                                    }

                                    Log.d("GEOAPIFY_DEBUG", "Route points: ${points.size}")

                                    (context as android.app.Activity).runOnUiThread {
                                        polylineAnnotationManager?.deleteAll()

                                        val polyline = com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions()
                                            .withPoints(points)
                                            .withLineColor("#FF0000")
                                            .withLineWidth(5.0)

                                        polylineAnnotationManager?.create(polyline)

                                        // Zoom vừa khung
                                        if (points.isNotEmpty()) {
                                            var minLat = points.first().latitude()
                                            var maxLat = points.first().latitude()
                                            var minLng = points.first().longitude()
                                            var maxLng = points.first().longitude()
                                            for (p in points) {
                                                minLat = minOf(minLat, p.latitude())
                                                maxLat = maxOf(maxLat, p.latitude())
                                                minLng = minOf(minLng, p.longitude())
                                                maxLng = maxOf(maxLng, p.longitude())
                                            }
                                            val centerLat = (minLat + maxLat) / 2
                                            val centerLng = (minLng + maxLng) / 2
                                            mapboxMap.setCamera(
                                                com.mapbox.maps.CameraOptions.Builder()
                                                    .center(Point.fromLngLat(centerLng, centerLat))
                                                    .zoom(12.0)
                                                    .build()
                                            )
                                        }

                                        Toast.makeText(context, "🧭 Hiển thị tuyến từ Geoapify thành công", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    (context as android.app.Activity).runOnUiThread {
                                        Toast.makeText(context, "Không có tuyến từ Geoapify", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("GEOAPIFY_ERROR", "Lỗi lấy tuyến: ${e.message}")
                            (context as android.app.Activity).runOnUiThread {
                                Toast.makeText(context, "Lỗi vẽ tuyến: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }.start()
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 20.dp),
                containerColor = Color(0xFF43A047),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Route, contentDescription = "Chỉ đường (Geoapify)")
            }





            //

        }
    }

    BackHandler { }
}

