package com.adamstyrc.parkmate

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng

fun DocumentSnapshot.getLocation()  = data!!.get("location") as GeoPoint

fun GeoPoint.toLatLnt() = LatLng(latitude, longitude)

fun GeoPoint.toPoint() = Point.fromLngLat(longitude, latitude)

fun LatLng.toPoint() = Point.fromLngLat(longitude, latitude)