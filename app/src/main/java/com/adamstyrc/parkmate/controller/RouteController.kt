package com.adamstyrc.parkmate.controller

import android.app.Activity
import android.content.Context
import com.adamstyrc.parkmate.Logger
import com.adamstyrc.parkmate.api.MapboxApi
import com.adamstyrc.parkmate.R
import com.adamstyrc.parkmate.toLocation
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions
import com.mapbox.services.android.navigation.ui.v5.listeners.NavigationListener
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RouteController(val applicationContext: Context) {

    companion object {
        var instance : RouteController? = null

        fun getInstance (applicationContext: Context? = null) : RouteController {
            if (instance == null) {
                instance =
                        RouteController(applicationContext!!)
            }

            return instance!!
        }
    }
    var origin : Point? = null
    var currentLocation : Point? = null
    var destination : Point? = null
    var currentRouteToDestination: DirectionsRoute? = null
    var navigationMapRouteToDestination: NavigationMapRoute? = null

    var currentRouteToParking: DirectionsRoute? = null


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

                    this@RouteController.origin = origin
                    this@RouteController.destination = destination
                    currentRouteToDestination = body.routes()[0]
                    callback.onResponse(call, response)
                }

                override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                    Logger.log("Error: " + t.message)
                    callback.onFailure(call, t)
                }
            }
        )
    }

    fun getRouteToParking(parkingLocation: Point, callback: Callback<DirectionsResponse>) {
        MapboxApi.getRoute(
            applicationContext,
            origin!!,
//            currentLocation!!,
            parkingLocation,
            object : Callback<DirectionsResponse> {
                override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
                    Logger.log("Response code: " + response.code())
                    val body = response.body()
                    if (body == null) {
                        Logger.log("No routes found, make sure you set the right user and access token.")
                        return
                    } else if (body.routes().size < 1) {
                        Logger.log("No routes found")
                        return
                    }

                    currentRouteToParking = body.routes()[0]
                    callback.onResponse(call, response)
                }

                override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                    Logger.log("Error: " + t.message)
                    callback.onFailure(call, t)
                }
            }
        )
    }

    fun prepareNavigationOptionsToDestination(activity: Activity) : NavigationViewOptions {
        return prepareNavigationOptions(activity, currentRouteToDestination!!)

    }

    fun prepareNavigationOptionsToParking(activity: Activity) : NavigationViewOptions {
        return prepareNavigationOptions(activity, currentRouteToParking!!)
    }

    private fun prepareNavigationOptions(activity: Activity, directionsRoute: DirectionsRoute): NavigationViewOptions {
        return NavigationViewOptions.builder()
            .directionsRoute(directionsRoute)
//            .shouldSimulateRoute(true)
            .navigationListener(object : NavigationListener {
                override fun onNavigationFinished() {
                    Logger.log("onNavigationFinished")
                }

                override fun onNavigationRunning() {
                    Logger.log("onNavigationRunning")
                }

                override fun onCancelNavigation() {
                    Logger.log("onCancelNavigation")
                    activity.finish()
                }
            })
            .build()
    }

    fun drawRoute(mapView: MapView, mapboxMap: MapboxMap) {
        if (navigationMapRouteToDestination != null) {
            navigationMapRouteToDestination!!.removeRoute()
        } else {
            navigationMapRouteToDestination =
                    NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute)
        }
        navigationMapRouteToDestination!!.addRoute(currentRouteToDestination)
    }
}