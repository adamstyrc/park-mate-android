package com.adamstyrc.parkmate

import android.content.Context
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.adamstyrc.parkmate.R.id.mapView
import com.mapbox.geojson.Point
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MapboxApi {

    companion object {
        fun getRoute(context: Context, origin: Point, destination: Point, callback: Callback<DirectionsResponse>) {
            NavigationRoute.builder(context)
                .accessToken(Mapbox.getAccessToken()!!)
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(callback)
        }
    }

}