package com.adamstyrc.parkmate

import android.content.Context
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RouteController(val applicationContext: Context) {

    var currentRoute: DirectionsRoute? = null
    var navigationMapRoute: NavigationMapRoute? = null

    fun getRoute(origin: Point, destination: Point, mapView: MapView, mapboxMap: MapboxMap) {
        MapboxApi.getRoute(
            applicationContext,
            origin,
            destination,
            object : Callback<DirectionsResponse> {
                override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
                    // You can get the generic HTTP info about the response
                    Logger.log("Response code: " + response.code())
                    val body = response.body()
                    if (body == null) {
                        Logger.log("No routes found, make sure you set the right user and access token.")
                        return
                    } else if (body.routes().size < 1) {
                        Logger.log("No routes found")
                        return
                    }

                    currentRoute = body.routes()[0]

                    if (navigationMapRoute != null) {
                        navigationMapRoute!!.removeRoute()
                    } else {
                        navigationMapRoute =
                                NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute)
                    }
                    navigationMapRoute!!.addRoute(currentRoute)
                }

                override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                    Logger.log("Error: " + t.message) }

            }
        )
    }
}