package com.ho.holive.core.common

import android.util.Log
import com.ho.holive.BuildConfig

object Logger {
    private const val TAG = "Holive"

    fun d(message: String) {
        if (BuildConfig.DEBUG) Log.d(TAG, message)
    }

    fun e(message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, message, throwable)
        } else {
            Log.e(TAG, "unexpected error")
        }
    }
}
