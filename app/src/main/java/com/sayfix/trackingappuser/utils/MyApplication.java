package com.sayfix.trackingappuser.utils;

import android.app.Application;

/**
 * Created by MohamedBahaaEddine on 18/03/2016.
 */
public class MyApplication extends Application {

    private static MyApplication mInstance;
    public static final String KEY_KILL_SERVICE = "GpsTrakerKillService";


    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

    }


    public static synchronized MyApplication getInstance() {
        return mInstance;
    }


    public synchronized ConnectionDetector getConDetectorInstance(){

        return ConnectionDetector.getInstance(getApplicationContext());
    }

    public synchronized ResourceReader getResReader(){

        return ResourceReader.getInstance(getApplicationContext());
    }

}