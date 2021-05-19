package io.ydxlt.app.homeapp

import android.util.Log
import android.widget.Toast
import io.ydxlt.app.lifecycle.api.AppLifecycle
import io.ydxlt.app.lifecycle.api.AppLifecycleCallback

@AppLifecycle(priority = AppLifecycle.MAX_PRIORITY)
class HomeApp : AppLifecycleCallback {

    override fun onCreate() {
        Log.d("AppLifecycle","HomeApp onCreate()")
    }

    override fun onTerminate() {
    }

    override fun onLowMemory() {
    }

    override fun onTrimMemory() {
    }
}