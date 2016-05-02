package com.sayfix.trackingappuser.utils;

/**
 * Created by Sunny on 12/21/2015.
 */

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

/**
 * Create this Class from tutorial :
 * http://www.androidhive.info/2012/07/android-gps-location-manager-tutorial
 * <p/>
 * For Geocoder read this : http://stackoverflow.com/questions/472313/android-reverse-geocoding-getfromlocation
 */

public class GPSTracker {
    private final Context mContext;
    // flag for GPS status
    boolean isGPSEnabled = false;
    // flag for network status
    boolean isNetworkEnabled = false;
    // flag for GPS status
    //boolean canGetLocation = false;
    //Location location; // location
    double latitude; // latitude
    double longitude; // longitude
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
    // Declaring a Location Manager
    protected LocationManager locationManager;

    public GPSTracker(Context context) {
        this.mContext = context;
    }

    public Location getLocation() {
        Location location = null;
        try {
            if (!isTrackable()) {
                // no network provider is enabled
                Log.d("dj", "returned false");
                //canGetLocation = false;
            } else {
                //this.canGetLocation = true;
                Log.d("dj", "can track");

                    /*locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);*/
                Log.d("Network", "Network");
                if (locationManager != null) {
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return null;
                    }
                    location = locationManager
                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }
                // if GPS Enabled get lat/long using GPS Services

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }


    private boolean isTrackable() {

        boolean canTrack = false;
        locationManager = (LocationManager) mContext
                .getSystemService(mContext.LOCATION_SERVICE);

        // getting GPS status
        isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        // getting network status
        isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!isGPSEnabled && !isNetworkEnabled) {
            // no network provider is enabled
            canTrack = false;
            //canGetLocation = false;
        } else {

            canTrack = true;
        }

        return canTrack;
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     */


    /**
     * Function to get latitude
     */
    /**
     * Function to check GPS/wifi enabled
     *
     * @return boolean
     */
    public boolean canGetLocation() {
        return isTrackable();
    }

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        // Setting Dialog Title
        alertDialog.setTitle("Settings");
        alertDialog.setCancelable(false);
        // Setting Dialog Message
        alertDialog.setMessage("GPS is disabled!. Do you want to go to settings menu?");
        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
                //mContext.startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
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

