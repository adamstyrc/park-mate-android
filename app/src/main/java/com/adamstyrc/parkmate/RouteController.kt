package com.adamstyrc.parkmate

import android.content.Context
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute
import kotlinx.android.synthetic.main.activity_maps.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RouteController(val applicationContext: Context) {

    companion object {
        var instance : RouteController? = null

        fun getInstance (applicationContext: Context? = null) : RouteController {
            if (instance == null) {
                instance = RouteController(applicationContext!!)
            }

            return instance!!
        }
    }
    var currentRoute: DirectionsRoute? = null
    var navigationMapRoute: NavigationMapRoute? = null

    fun getRoute(origin: Point, destination: Point, callback: Callback<DirectionsResponse>) {
        Logger.log("Mapbox route request.")
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
                    callback.onResponse(call, response)
                }

                override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                    Logger.log("Error: " + t.message)
                    callback.onFailure(call, t)
                }



            }
        )
    }

    fun drawRoute(mapView: MapView, mapboxMap: MapboxMap) {
        if (navigationMapRoute != null) {
            navigationMapRoute!!.removeRoute()
        } else {
            navigationMapRoute =
                    NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute)
        }
        navigationMapRoute!!.addRoute(currentRoute)
    }
}