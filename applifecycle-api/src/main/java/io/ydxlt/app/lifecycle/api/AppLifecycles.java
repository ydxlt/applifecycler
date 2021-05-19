package io.ydxlt.app.lifecycle.api;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 目前只通过插件方式注册，不支持通过反射注册（影响效率）
 */
public final class AppLifecycles implements AppLifecycleCallback {

    private final List<AppLifecycleCallback> mAppLifecycleList = new ArrayList<>();

    private AppLifecycles() {
        // register by plugin
    }

    public static AppLifecycles INSTANCE = InstanceHolder.INSTANCE;

    static class InstanceHolder {
        static AppLifecycles INSTANCE = new AppLifecycles();
    }

    /**
     * Call by plugin
     */
    private void sortByPriority(){
        Collections.sort(mAppLifecycleList, (before, after) -> after.getPriority() - before.getPriority());
    }

    /**
     * Call by plugin
     *
     * @param className
     */
    private void register(String className) {
        if(className == null || className.isEmpty()){
            return;
        }
        try {
            Object obj = Class.forName(className).getConstructor().newInstance();
            if(obj instanceof AppLifecycleCallback){
                mAppLifecycleList.add((AppLifecycleCallback) obj);
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        for (AppLifecycleCallback callback : mAppLifecycleList) {
            callback.onCreate();
        }
    }

    @Override
    public void onTerminate() {
        for (AppLifecycleCallback callback : mAppLifecycleList) {
            callback.onTerminate();
        }
    }

    @Override
    public void onLowMemory() {
        for (AppLifecycleCallback callback : mAppLifecycleList) {
            callback.onLowMemory();
        }
    }

    @Override
    public void onTrimMemory() {
        for (AppLifecycleCallback callback : mAppLifecycleList) {
            callback.onTrimMemory();
        }
    }
}
