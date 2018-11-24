package com.adamstyrc.parkmate.api

import com.google.firebase.firestore.FirebaseFirestore

class ParkingApi {

    var db = FirebaseFirestore.getInstance()

    fun getParkings() = db.collection("parklots").get()
}