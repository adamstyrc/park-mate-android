package com.adamstyrc.parkmate

import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions
import kotlinx.android.synthetic.main.activity_maps.*


class MapsActivity : AppCompatActivity(), PermissionsListener {

//    private lateinit var mMap: GoogleMap

    private lateinit var mapboxMap: MapboxMap
    private lateinit var permissionsManager: PermissionsManager
    private var originLocation: Location? = null
    private var destinationMarker: Marker? = null

    lateinit var routeController: RouteController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(this, "pk.eyJ1IjoiYWRhbXN0eXJjIiwiYSI6ImNqb3ZraWhzcDBlcDAzcXJwbjRldGlpNW0ifQ.-2YjpnaUbKADGXwKCBszBA");
        routeController = RouteController(applicationContext)

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
            }

            mapboxMap.setOnMarkerClickListener { marker ->
                val destinationPosition = Point.fromLngLat(marker.position.longitude, marker.position.latitude)
                val originPosition = Point.fromLngLat(originLocation!!.longitude, originLocation!!.latitude)

                routeController.getRoute(originPosition,
                    destinationPosition,
                    mapView,
                    mapboxMap)

                true
            }
        }

        btnFindParking.setOnClickListener {
            val options = NavigationLauncherOptions.builder()
                .directionsRoute(routeController.currentRoute)
                .shouldSimulateRoute(true)
                .build()

            NavigationLauncher.startNavigation(this, options)
        }

//        mapView.on
//        enableLocationComponent()
        //        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
//        mapFragment.getMapAsync { googleMap ->
//            initMap(googleMap)
//        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()

        btnFindParking.visibility = if (routeController.currentRoute != null) View.VISIBLE else View.GONE
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

//    private fun initMap(googleMap: GoogleMap) {
//        mMap = googleMap
//
//        val tampereLatLng = LatLng(61.4972373, 23.7579932)
//        mMap.addMarker(MarkerOptions().position(tampereLatLng).title("Marker in Tampere"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tampereLatLng, 15f))
//    }

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
