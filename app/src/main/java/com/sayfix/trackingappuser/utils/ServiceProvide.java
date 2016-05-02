package com.sayfix.trackingappuser.utils;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by MohamedBahaaEddine on 18/03/2016.
 */
public class ServiceProvide extends Service implements LocationListener {

    LocationManager mlocationManager;
    private final long REQUEST_LOCATION_MIN_TIME = 5000;
    private final float REQUEST_LOCATION_MIN_DISTANCE = 0;

    private final IBinder mBinder = new MyBinder();
    private LocationChangeListener myLocListener;

    public interface LocationChangeListener {

        void onLocationChanged(Location currentLocation);
    }

    public class MyBinder extends Binder {

        public ServiceProvide getService() {
            return ServiceProvide.this;
        }
    }

    @Override
    public void onCreate() {
        //startListening();
        Log.d("TrackApp", "on create service");
        super.onCreate();
        initialize();
    }

    private void initialize() {

        setUpLocationManager();
    }


    public void setLocationListener(LocationChangeListener myLocListener) {

        this.myLocListener = myLocListener;
    }

    private void setUpLocationManager() {

        mlocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mlocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, REQUEST_LOCATION_MIN_TIME,
                REQUEST_LOCATION_MIN_DISTANCE, this);
        Log.d("TrackApp", "location listener set");

    }

    public void killService() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mlocationManager.removeUpdates(this);
        stopSelf();
    }

    public LatLng getCurrentLocation() {

        /*Criteria criteria = new Criteria();
        String provider = mlocationManager.getBestProvider(criteria, true);*/
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        Location location = mlocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        LatLng userLatLng = null;
        if (location != null)
            userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        return userLatLng;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("TrackApp", "on start service");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onLocationChanged(Location location) {

        Log.d("TrackApp", "on location changed: " + location.getLatitude() + " & " + location.getLongitude());
        //toastLocation(location);
        if (myLocListener != null)
            myLocListener.onLocationChanged(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("TrackApp", "in onBind method services");
        return mBinder;
    }

    private void toastLocation(final Location location) {

        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(ServiceProvide.this.getApplicationContext(), "Location latitude: " + location.getLatitude()
                        + "longitude: " + location.getLongitude(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
