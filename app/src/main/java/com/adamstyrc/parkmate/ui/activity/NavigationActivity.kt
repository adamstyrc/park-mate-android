package com.adamstyrc.parkmate.ui.activity

import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.adamstyrc.parkmate.*
import com.adamstyrc.parkmate.R
import com.adamstyrc.parkmate.api.ParkingApi
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_navigation.*
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress
import retrofit2.Call
import retrofit2.Response
import java.lang.IllegalStateException


class NavigationActivity : AppCompatActivity() {

    lateinit var parkingApi: ParkingApi

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

        parkingApi = ParkingApi()
        routeController = RouteController.getInstance(applicationContext)

        vNavigation.onCreate(savedInstanceState)
        vNavigation.initialize {
//            val origin = Point.fromLngLat(ORIGIN_LONGITUDE, ORIGIN_LATITUDE)
//            val destination = Point.fromLngLat(DESTINATION_LONGITUDE, DESTINATION_LATITUDE)
            val navigationOptions = routeController.prepareCurrentNavigationOptions(this)
            vNavigation.startNavigation(navigationOptions)

            val retrieveMapboxNavigation = vNavigation.retrieveMapboxNavigation()!!

            retrieveMapboxNavigation.addProgressChangeListener(progressChangeListener)
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
        findClosestParkingLot(object : Callback<DocumentSnapshot>() {

            override fun onSuccess(response: DocumentSnapshot) {
                response

            }
            override fun onError(message: String) {
                Logger.log(message)
            }
        })
    }

    private fun navigateToClosestParkingLot() {
        findClosestParkingLot(object : Callback<DocumentSnapshot>() {
            override fun onError(message: String) {
                Logger.log(message)
            }

            override fun onSuccess(response: DocumentSnapshot) {
                val closestParking = response

                routeController.getRoute(routeController.origin!!,
                    closestParking.getLocation().toPoint(),
                    object: retrofit2.Callback<DirectionsResponse> {
                        override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                            Logger.log(t.message!!)
                        }

                        override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
                            val navigationOptions = routeController.prepareCurrentNavigationOptions(this@NavigationActivity)
                            vNavigation.startNavigation(navigationOptions)
                        }
                    })
            }

        })
    }

    private fun findClosestParkingLot(callback: com.adamstyrc.parkmate.Callback<DocumentSnapshot>) {
        ParkingApi().getAvailableParkings()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && !task.result.isEmpty) {
                    val parkings = task.result

                    val closestParkingLot = ParkingManager.getClosestParking(parkings, routeController.destination!!)

                    callback.onSuccess(closestParkingLot)

                    for (document in parkings) {
                        Logger.log(document.id + " => " + document.data)
                    }
                } else if (!task.result.isEmpty) {
                    callback.onError("Empty")
                    displayErrorMessage("Could not find any parking nearby.\nNavigating to final destination.")
                } else {
                    callback.onError("Error response")
                    Logger.log("Error getting documents. ${task.exception}")
                }
            }
    }

    private fun registerParkingUpdates(closestParkingLot: QueryDocumentSnapshot) {
        parklotUpdateRegistration = parkingApi.scanParking(closestParkingLot.id)
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

    val progressChangeListener = object  : ProgressChangeListener {
        override fun onProgressChange(location: Location, routeProgress: RouteProgress) {
            Logger.log("onProgessChange: ${routeProgress.distanceRemaining()}")

            if (routeProgress.distanceRemaining() < 2000) {
                vNavigation.retrieveMapboxNavigation()!!.removeProgressChangeListener(this)

                displayErrorMessage("Navigating to nearest parking lot.")
                navigateToClosestParkingLot()
            }
        }
    }
}
