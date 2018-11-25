package com.adamstyrc.parkmate.api

import android.content.Context
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute
import retrofit2.Callback


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