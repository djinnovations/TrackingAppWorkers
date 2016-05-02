package com.sayfix.trackingappuser.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sayfix.trackingappuser.MapsDisplayActivity;

/**
 * Created by COMP on 3/9/2016.
 */
public class GpsActionReceiver extends BroadcastReceiver {


    /*public interface KillServiceListener{

        void onKillService();
    }

    private static KillServiceListener mListener;*/

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("dj","onRecieve gps receiver trcker app");
        checkGps(context);
    }


    private void checkGps(Context context){

        ConnectionDetector connectionDetector = MyApplication.getInstance().getConDetectorInstance();
        if(connectionDetector.isGpsTurnedOn()){
            Log.d("dj","kill service false");
            bindMyService(context, false);
        }
        else{
            Log.d("dj","kill service true");
            bindMyService(context, true);
        }

    }


    private void bindMyService(Context context, boolean killservice) {

        Intent requestGpsAct = new Intent(context, MapsDisplayActivity.class);
        requestGpsAct.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        requestGpsAct.putExtra(MyApplication.KEY_KILL_SERVICE, killservice);
        context.startActivity(requestGpsAct);
    }


    /*private void stopGpsTracking(){

        mListener.onKillService();
    }


    public static void setInterface(KillServiceListener mKsListener){

        mListener = mKsListener;
    }*/

}
