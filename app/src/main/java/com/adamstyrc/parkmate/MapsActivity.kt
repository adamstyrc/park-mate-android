package com.adamstyrc.parkmate

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity() {

    private lateinit var mMap: GoogleMap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, "pk.eyJ1IjoiYWRhbXN0eXJjIiwiYSI6ImNqb3ZraWhzcDBlcDAzcXJwbjRldGlpNW0ifQ.-2YjpnaUbKADGXwKCBszBA");
        setContentView(R.layout.activity_maps)
        mapView.onCreate(savedInstanceState)

        //        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
//        mapFragment.getMapAsync { googleMap ->
//            initMap(googleMap)
//        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
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

    private fun initMap(googleMap: GoogleMap) {
        mMap = googleMap

        val tampereLatLng = LatLng(61.4972373, 23.7579932)
        mMap.addMarker(MarkerOptions().position(tampereLatLng).title("Marker in Tampere"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tampereLatLng, 15f))
    }


}
