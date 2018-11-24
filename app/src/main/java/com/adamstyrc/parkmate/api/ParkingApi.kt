package com.adamstyrc.parkmate.api

import com.google.firebase.firestore.FirebaseFirestore

class ParkingApi {

    var db = FirebaseFirestore.getInstance()

    fun getParkings() = db.collection("parklots").get()

    fun getAvailableParkings() = db.collection("parklots")
        .whereGreaterThan("free_spaces", 0)
        .get()

    fun scanParking(id: String) = db.collection("parklots")
        .document(id)
}