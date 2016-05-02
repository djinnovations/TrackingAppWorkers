package com.sayfix.trackingappuser.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;

/**
 * Created by MohamedBahaaEddine on 18/03/2016.
 */
public class ConnectionDetector {

    private static Context mContext;
    private static ConnectionDetector mConnDetector;

    private ConnectionDetector(Context context) {

    }


    public static ConnectionDetector getInstance(Context context){

        mContext = context;
        if(mConnDetector == null){

            mConnDetector = new ConnectionDetector(context);
        }
        return mConnDetector;
    }


    private boolean isNetworkAvailable() {

        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean stat = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        Log.d("TrackApp", "network stat: " + stat);
        return stat;
    }


    public boolean canGetLocation() {
        /*LocationManager locationManager = (LocationManager) mContext
                .getSystemService(mContext.LOCATION_SERVICE);*/

        // getting GPS status
        boolean isGPSEnabled = isGpsTurnedOn();
        Log.d("TrackApp", "gps stat: " + isGPSEnabled);

        if (isGPSEnabled && isNetworkAvailable()) {
            Log.d("TrackApp", "returned from if");
            return true;
        } else {

            Log.d("dj", "returned from else");
            return false;
        }
    }



    public boolean isGpsTurnedOn(){

        LocationManager locationManager = (LocationManager) mContext
                .getSystemService(mContext.LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }



    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");
        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        // Showing Alert Message
        alertDialog.show();
    }


}
