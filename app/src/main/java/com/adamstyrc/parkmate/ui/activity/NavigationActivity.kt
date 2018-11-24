package com.adamstyrc.parkmate.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.adamstyrc.parkmate.Logger
import com.adamstyrc.parkmate.ParkingManager
import com.adamstyrc.parkmate.R
import com.adamstyrc.parkmate.RouteController
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions
import com.mapbox.services.android.navigation.ui.v5.listeners.NavigationListener
import kotlinx.android.synthetic.main.activity_navigation.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QueryDocumentSnapshot


class NavigationActivity : AppCompatActivity() {

    lateinit var db: FirebaseFirestore

    var parklotUpdateRegistration : ListenerRegistration? = null

    companion object {
        fun startNavigationActivity(context: Context) {
            val intent = Intent(context, NavigationActivity::class.java)
            context.startActivity(intent)
        }
    }

    lateinit var routeController : RouteController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        db = FirebaseFirestore.getInstance()
        routeController = RouteController.getInstance(applicationContext)

        vNavigation.onCreate(savedInstanceState)
        vNavigation.initialize {
//            val origin = Point.fromLngLat(ORIGIN_LONGITUDE, ORIGIN_LATITUDE)
//            val destination = Point.fromLngLat(DESTINATION_LONGITUDE, DESTINATION_LATITUDE)
            val options = NavigationViewOptions.builder()
                .directionsRoute(routeController.currentRoute)
                .shouldSimulateRoute(true)
                .navigationListener(object  : NavigationListener {
                    override fun onNavigationFinished() {
                        Logger.log("onNavigationFinished")
                    }

                    override fun onNavigationRunning() {
                        Logger.log("onNavigationRunning")
                    }

                    override fun onCancelNavigation() {
                        Logger.log("onCancelNavigation")
                        finish()
                    }
                })
                .build()

            vNavigation.startNavigation(options)
        }
    }

    override fun onStart() {
        super.onStart()
        vNavigation.onStart()

        findClosestParkingLot()
    }

    override fun onResume() {
        super.onResume()
        vNavigation.onResume()
    }

    override fun onPause() {
        super.onPause()
        vNavigation.onPause()
    }

    override fun onStop() {
        super.onStop()
        vNavigation.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        vNavigation.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        vNavigation.onLowMemory()
    }

    private fun findClosestParkingLot() {
        db.collection("parklots")
            .whereGreaterThan("free_spaces", 0)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && !task.result.isEmpty) {
                    val parkings = task.result

                    val closestParkingLot = ParkingManager.getClosestParking(parkings, routeController.destination!!)
                    registerParkingUpdates(closestParkingLot)

                    for (document in parkings) {
                        Logger.log(document.id + " => " + document.data)
                    }
                } else if (!task.result.isEmpty) {
                    displayErrorMessage("Could not find any parking nearby.\nNavigating to final destination.")
                } else {
                    Logger.log("Error getting documents. ${task.exception}")
                }
            }
    }

    private fun registerParkingUpdates(closestParkingLot: QueryDocumentSnapshot) {
        parklotUpdateRegistration = db.collection("parklots")
            .document(closestParkingLot.id)
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                Logger.log("Snapshot update for ${documentSnapshot!!.id} => ${documentSnapshot.data})")

                if (documentSnapshot.getLong("free_spaces")!! <= 0) {
                    displayErrorMessage("Changing parking lot.")

                    unregisterParkingUpdates()
                    findClosestParkingLot()
                }
            }
    }

    private fun displayErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun unregisterParkingUpdates() {
        parklotUpdateRegistration?.remove()
        parklotUpdateRegistration = null
    }
}
