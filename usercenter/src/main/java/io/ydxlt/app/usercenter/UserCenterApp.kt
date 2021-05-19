package io.ydxlt.app.usercenter

import android.util.Log
import io.ydxlt.app.lifecycle.api.AppLifecycle
import io.ydxlt.app.lifecycle.api.AppLifecycleCallback

@AppLifecycle(priority = AppLifecycle.MIN_PRIORITY)
class UserCenterApp : AppLifecycleCallback {

    override fun onCreate() {
        Log.d("AppLifecycle","UserCenterApp onCreate()")
    }

    override fun onTerminate() {
    }

    override fun onLowMemory() {
    }

    override fun onTrimMemory() {
    }
}