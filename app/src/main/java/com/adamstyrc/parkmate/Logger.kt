package com.adamstyrc.parkmate

import android.util.Log

class Logger {
    companion object {
        fun log(message: String) {
            Log.d("ParkMate", message)
        }
    }
}