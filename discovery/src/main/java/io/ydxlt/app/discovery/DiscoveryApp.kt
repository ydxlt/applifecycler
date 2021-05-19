package io.ydxlt.app.discovery

import android.util.Log
import android.widget.Toast
import io.ydxlt.app.lifecycle.api.AppLifecycle
import io.ydxlt.app.lifecycle.api.AppLifecycleCallback

@AppLifecycle(priority = AppLifecycle.NORM_PRIORITY)
class DiscoveryApp : AppLifecycleCallback {

    override fun onCreate() {
        Log.d("AppLifecycle","DiscoveryApp onCreate()")
    }

    override fun onTerminate() {
    }

    override fun onLowMemory() {
    }

    override fun onTrimMemory() {
    }
}