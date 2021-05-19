package io.ydxlt.app.lifecycler;

import android.app.Application;
import android.util.Log;

import io.ydxlt.app.lifecycle.api.AppLifecycles;

public class MyApp extends Application  {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("AppLifecycle","MyApp onCreate()");
        AppLifecycles.INSTANCE.onCreate();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        AppLifecycles.INSTANCE.onTerminate();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        AppLifecycles.INSTANCE.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        AppLifecycles.INSTANCE.onTrimMemory();
    }
}
