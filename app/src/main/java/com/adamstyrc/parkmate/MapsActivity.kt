package com.adamstyrc.parkmate

import android.location.Location
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.adamstyrc.parkmate.api.ParkingApi
import com.adamstyrc.parkmate.ui.activity.NavigationActivity
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import kotlinx.android.synthetic.main.activity_maps.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.firebase.firestore.FirebaseFirestore




class MapsActivity : AppCompatActivity(), PermissionsListener {

    lateinit var routeController: RouteController
    lateinit var parkingApi: ParkingApi

    private lateinit var mapboxMap: MapboxMap
    private lateinit var permissionsManager: PermissionsManager
    private var originLocation: Location? = null
    private var destinationMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(this, "pk.eyJ1IjoiYWRhbXN0eXJjIiwiYSI6ImNqb3ZraWhzcDBlcDAzcXJwbjRldGlpNW0ifQ.-2YjpnaUbKADGXwKCBszBA");
        routeController = RouteController.getInstance(applicationContext)
        parkingApi = ParkingApi()

        setContentView(R.layout.activity_maps)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync {
            mapboxMap = it
            enableLocationComponent()

            mapboxMap.addOnMapClickListener { point ->
                if (destinationMarker != null) {
                    mapboxMap.removeMarker(destinationMarker!!)
                }

                destinationMarker = mapboxMap.addMarker(
                    MarkerOptions().position(point)
                )

                val destinationPosition = Point.fromLngLat(point.longitude, point.latitude)
                val originPosition = Point.fromLngLat(originLocation!!.longitude, originLocation!!.latitude)

                routeController.getRoute(originPosition,
                    destinationPosition, object : Callback<DirectionsResponse> {
                        override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
                            routeController.drawRoute(mapView, mapboxMap)
                            updateNavigateButton()
                        }

                        override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                        }
                    })
            }
        }

        btnFindParking.setOnClickListener {
            NavigationActivity.startNavigationActivity(this)
        }


    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()

        if (BuildConfig.DEBUG) {
            parkingApi.getParkings().addOnCompleteListener {
                if (it.isSuccessful) {
                    it.result.documents.forEach { parking ->
                        val location = parking.getLocation()

                        mapboxMap.addMarker(
                            MarkerOptions().position(location.toLatLnt())
                                .title(parking.id)
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()

        updateNavigateButton()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    private fun updateNavigateButton() {
        btnFindParking.visibility = if (routeController.currentRoute != null) View.VISIBLE else View.GONE
    }

    private fun enableLocationComponent() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            val locationComponent = mapboxMap.locationComponent
            locationComponent.activateLocationComponent(this)
            locationComponent.setLocationComponentEnabled(true)
            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING)
            originLocation = locationComponent.getLastKnownLocation()

        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(this, "We need tha, bro.", Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocationComponent()
        } else {
            Toast.makeText(this, "You're not gonna park anywhere!", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
