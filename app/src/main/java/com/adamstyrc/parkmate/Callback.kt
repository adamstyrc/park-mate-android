package com.adamstyrc.parkmate

abstract class Callback<T> {
    abstract fun onSuccess(response: T)
    abstract fun onError(message: String)
}