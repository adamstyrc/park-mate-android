package com.adamstyrc.parkmate.controller

import android.content.Context
import com.adamstyrc.parkmate.R
import com.adamstyrc.parkmate.getLocation
import com.adamstyrc.parkmate.toLatLnt
import com.google.firebase.firestore.DocumentSnapshot
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.maps.MapboxMap
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import com.mapbox.mapboxsdk.annotations.IconFactory


class ParkingMarkersController private constructor(val applicationContext: Context) {


    companion object {
        private var instance : ParkingMarkersController? = null

        fun getInstance(applicationContext: Context) : ParkingMarkersController {
            if (instance == null) {
                instance = ParkingMarkersController(applicationContext)
            }

            return instance!!
        }
    }

    val markers = ArrayList<Marker>()
    val parkingBitmap : Bitmap

    init {
        parkingBitmap =  BitmapFactory.decodeResource(applicationContext.resources,
            R.drawable.parking)

        applicationContext.getDrawable(R.drawable.parking2)
    }

    fun setMarkers(mapboxMap: MapboxMap, documents: MutableList<DocumentSnapshot>) {
        markers.forEach {
            mapboxMap.removeMarker(it)
        }

        val icon = IconFactory.recreate("parking", parkingBitmap)
        documents.forEach { parking ->
            val location = parking.getLocation()

            markers.add(
                mapboxMap.addMarker(
                    MarkerOptions().position(location.toLatLnt())
                        .setIcon(icon)
                        .title(parking.id)
                )
            )
        }
    }
}