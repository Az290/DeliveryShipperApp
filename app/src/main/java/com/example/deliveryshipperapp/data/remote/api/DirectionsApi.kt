package com.example.deliveryshipperapp.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Raw response (chỉ cần lấy geometry polyline)
data class DirectionsResponseRaw(
    val routes: List<Route>
) {
    data class Route(
        val geometry: String
    )
}

interface DirectionsApi {
    @GET("directions/v5/mapbox/driving/{coordinates}")
    suspend fun getRoute(
        @Path("coordinates") coordinates: String,
        @Query("geometries") geometries: String = "polyline",
        @Query("overview") overview: String = "full",
        @Query("access_token") accessToken: String
    ): DirectionsResponseRaw
}