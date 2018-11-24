package com.adamstyrc.parkmate.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.adamstyrc.parkmate.R
import com.adamstyrc.parkmate.RouteController
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions
import com.mapbox.services.android.navigation.ui.v5.listeners.NavigationListener
import kotlinx.android.synthetic.main.activity_navigation.*


class NavigationActivity : AppCompatActivity() {

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
                        finish()
                    }

                    override fun onNavigationRunning() {
                    }

                    override fun onCancelNavigation() {
                    }
                })
                .build()

            vNavigation.startNavigation(options)
        }
    }

    override fun onStart() {
        super.onStart()
        vNavigation.onStart()
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
}
