package io.ydxlt.app.lifecycle.api;

public interface AppLifecycleCallback {

    void onCreate();

    void onTerminate();

    void onLowMemory();

    void onTrimMemory();

    default int getPriority() {
        return 0;
    }
}
