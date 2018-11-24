package com.adamstyrc.parkmate

import android.location.Location
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.mapbox.geojson.Point

class ParkingManager {

    companion object {
        fun getClosestParking(parkings: QuerySnapshot, point: Point) : QueryDocumentSnapshot {
            var currentMinDistance : Float = Float.MAX_VALUE
            var currentClosestParking : QueryDocumentSnapshot? = null

            val resultDistance = FloatArray(1)
            parkings.forEach {
                val parkingLocation = it.getLocation()
                Location.distanceBetween(
                    point.latitude(), point.longitude(),
                    parkingLocation.latitude, parkingLocation.longitude,
                    resultDistance)

                if (currentMinDistance > resultDistance[0]) {
                    currentMinDistance = resultDistance[0]
                    currentClosestParking = it
                }
            }

            return currentClosestParking!!

        }
    }
}