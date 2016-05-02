package com.sayfix.trackingappuser;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sayfix.trackingappuser.utils.ConnectionDetector;
import com.sayfix.trackingappuser.utils.MyApplication;
import com.sayfix.trackingappuser.utils.ServiceProvide;

import java.util.EmptyStackException;
import java.util.Stack;

public class MapsDisplayActivity extends AppCompatActivity implements
        OnMapReadyCallback {

    private Stack<Marker> markerStack;
    private final float CAMERA_ZOOM = 17.0f; //2.0 max zoom-out and 21.0 is max zoom-in
    private final long MARKER_MOVEMENT_SPEED = 8000;

    private ServiceProvide.LocationChangeListener myLocListener = new ServiceProvide.LocationChangeListener() {
        @Override
        public void onLocationChanged(Location currentLocation) {

            Toast.makeText(getApplicationContext(), "Location latitude: " + currentLocation.getLatitude() + " "
                    + "longitude: " + currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();

            showUpOnMap(currentLocation);
        }
    };

    private void showUpOnMap(Location currentLocation) {

        try {
            Marker previousMarker = markerStack.pop();
            LatLng newLatlng = getLatLngObj(currentLocation);
            animateMarker(previousMarker, newLatlng, false);
            previousMarker.setPosition(newLatlng);
            markerStack.add(previousMarker);
        } catch (EmptyStackException e) {
            e.printStackTrace();
            checkTracking();
        }
    }


    public void animateMarker(final Marker marker, final LatLng toPosition,
                              final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mGoogleMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = MARKER_MOVEMENT_SPEED;

        final Interpolator interpolator = new LinearInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                LatLng newPosition = new LatLng(lat, lng);
                marker.setPosition(newPosition);
                moveCamera(newPosition);

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }


    //private GPSTracker gpsTracker;
    private ConnectionDetector mConnectionDetector;
    private boolean isKillActive = false;
    private GoogleMap mGoogleMap;
    private View noUseView;

    private ServiceProvide mService;

    private ServiceConnection mServiceConn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {

            Log.d("dj", "component name onServiceDisconnected: " + name);
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            // TODO Auto-generated method stub
            Log.d("dj", "component name onServiceConnected: " + name);
            mService = ((ServiceProvide.MyBinder) binder).getService();
            if (mService != null) {
                if (isKillActive) {
                    askBeforeExit();
                } else {
                    mService.setLocationListener(myLocListener);
                    LatLng locationLatLng = mService.getCurrentLocation();
                    if (locationLatLng != null) {
                        displayOnMap(locationLatLng);
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapNav);
        mapFragment.getMapAsync(this);*/

        isKillActive = getIntent().getBooleanExtra(MyApplication.KEY_KILL_SERVICE, false);
        if (isKillActive) {
            exitInBackground();
        } else {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.mapNav);
            mapFragment.getMapAsync(this);
            initializeViews();
        }

        //gpsTracker = new GPSTracker(this);

    }


    private void exitInBackground() {

        bindMyService();
    }


    private void checkKillStat() {

        if (isKillActive) {
            askBeforeExit();
        }else mService.setLocationListener(myLocListener);
    }


    private void initializeViews() {

        markerStack = new Stack<>();
        noUseView = findViewById(R.id.noUseView);
        mConnectionDetector = MyApplication.getInstance().getConDetectorInstance();
        checkGps();
        /*if (isKillActive) {
            bindMyService();
        }
        if (mService != null) {
            mService.killService();
        }*/
    }


    private void askBeforeExit() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        String title = "GPS turned OFF!";
        String message = "The app will exit";

        dialogBuilder.setTitle(title);
        dialogBuilder.setMessage(message);
        dialogBuilder.setNegativeButton("No wait!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });
        dialogBuilder.setPositiveButton("Go ahead", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                //if okay exit the app
                dialog.dismiss();
                finish();
            }
        });

        dialogBuilder.create();
        dialogBuilder.show();
    }


    private void bindMyService() {

        if (mService == null) {
            Intent intent = new Intent(getBaseContext(), ServiceProvide.class);
            Log.d("dj", "binding location service");
            bindService(intent, mServiceConn, Activity.BIND_AUTO_CREATE);

        }else {
            checkKillStat();
        }
        //finish();
    }


    private void unBindMyService() {

        Log.d("dj", "un binding service");
        getApplicationContext().unbindService(mServiceConn);
    }


    private void checkGps() {

        if (mConnectionDetector.canGetLocation()) {
            //TODO
            onGPSTurnedOn();
        } else {
            requestGPS();
        }

    }

    private void requestGPS() {

        Snackbar tempBar = Snackbar.make(noUseView, "Turn ON GPS and data/WIFI to track", Snackbar.LENGTH_INDEFINITE);
        tempBar.setAction("retry", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);*/
                checkGps();

            }
        });
        tempBar.setActionTextColor(MyApplication.getInstance().getResReader()
                .getColorFromResource(R.color.colorPrimaryDark));
        setSnackBarTextColor(tempBar);
        tempBar.show();
    }


    private void onGPSTurnedOn() {

        /*if (isKillActive) {
            bindMyService();
        } else {

            startMyService();
        }*/
        //finish();

        startMyService();
        bindMyService();

    }


    private void startMyService() {

        Intent intent = new Intent(getApplicationContext(), ServiceProvide.class);
        Log.d("dj", "starting service");
        startService(intent);
    }


    private Snackbar setSnackBarTextColor(Snackbar tempSnBar) {

        View sbView = tempSnBar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(MyApplication.getInstance().getResReader()
                .getColorFromResource(R.color.colorRed));
        return tempSnBar;
    }


    @Override
    protected void onStart() {

        super.onStart();
        /*if (!isKillActive) {
            checkGps();
        }*/

    }


    @Override
    protected void onStop() {

        super.onStop();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mService != null) {
            unbindService(mServiceConn);
            if (isKillActive) {
                mService.killService();
            }
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        isKillActive = intent.getBooleanExtra(MyApplication.KEY_KILL_SERVICE, false);
        checkKillStat();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mGoogleMap = googleMap;
        // loadMapSettings();
    }


    private void checkTracking() {

        if (mConnectionDetector.canGetLocation()) {
            trackMe();
        } else {
            mConnectionDetector.showSettingsAlert();
        }
    }


    private void trackMe() {

        LatLng currentLocation = mService.getCurrentLocation();
        if (currentLocation != null) {
            displayOnMap(currentLocation);
        }
    }


    private LatLng getLatLngObj(Location location) {

        return new LatLng(location.getLatitude(), location.getLongitude());
    }


    private void displayOnMap(final LatLng currentLocLatLng) {

        markerStack.add(addCustomMarker(R.drawable.location_selected, currentLocLatLng));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //positionCamera(currentLocLatLng, null);
                moveCamera(currentLocLatLng);
            }
        }, 300);
    }


    private void positionCamera(final LatLng origin, LatLng dest) {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(origin);
        if (dest != null)
            builder.include(dest);
        LatLngBounds bounds = builder.build();
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 30));
    }


    private void moveCamera(LatLng point) {

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, CAMERA_ZOOM));
    }


    private Marker addCustomMarker(int mapMarkerResId, LatLng markerLatLng) {
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(80, 80, conf);
        Canvas canvas1 = new Canvas(bmp);

// paint defines the text color,
// stroke width, size
        Paint color = new Paint();
        color.setTextSize(35);
        color.setColor(Color.BLACK);

//modify canvas
        canvas1.drawBitmap(BitmapFactory.decodeResource(getResources(),
                mapMarkerResId), 0, 0, color);
        canvas1.drawText("", 30, 40, color);
//add marker to Map
        Marker mCustomMarker = mGoogleMap.addMarker(new MarkerOptions().position(markerLatLng)
                .icon(BitmapDescriptorFactory.fromBitmap(bmp))
                        // Specifies the anchor to be at a particular point in the marker image.
                .anchor(0.5f, 1));
        return mCustomMarker;
    }


    private void loadMapSettings() {

        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
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
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.setOnMyLocationButtonClickListener(getCurrentLocListener);
    }


    public GoogleMap.OnMyLocationButtonClickListener getCurrentLocListener = new GoogleMap.OnMyLocationButtonClickListener() {

        @Override
        public boolean onMyLocationButtonClick() {

            // checkTracking();
            return true;
        }

    };


}
