package com.dependa.pedometer;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Binder;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.dependa.pedometer.base.StepValue;
import com.dependa.pedometer.base.Utils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class RouteService extends Service {
    private static final String TAG = RouteService.class.getSimpleName();
    public static final String ACTION = "com.example.pedometer.RouteService";

    private Location mCurrentLocation;

    private LatLng startLatLng = new LatLng(0.0, 0.0);        //polyline 시작점
    private LatLng endLatLng = new LatLng(0.0, 0.0);        //polyline 끝점

    private boolean isServiceRunning = false;

    private int lastStepCount = 0;
    private String stime = null;

    static final String ACTION_BROADCAST = ACTION + ".broadcast";

    static final String EXTRA_LOCATION = ACTION + ".location";
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = ACTION +
            ".started_from_notification";

    private final IBinder mBinder = new LocalBinder();

    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */
    private boolean mChangingConfiguration = false;


    /**
     * Contains parameters used by {@link com.google.android.gms.location.FusedLocationProviderApi}.
     */
    private LocationRequest mLocationRequest;

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Callback for changes in location.
     */
    private LocationCallback mLocationCallback;

    public RouteService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };

        createLocationRequest();
        getLastLocation();

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();

        requestLocationUpdates();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isServiceRunning = true;
        return Service.START_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) comes to the foreground
        // and binds with this service. The service should cease to be a foreground service
        // when that happens.
        Log.i(TAG, "in onBind()");
        stopForeground(true);
        mChangingConfiguration = false;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        Log.i(TAG, "in onRebind()");
        stopForeground(true);
        mChangingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Last client unbound from service");
        return true; // Ensures onRebind() is called when a client re-binds.
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Makes a request for location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    public void requestLocationUpdates() {
        Log.i(TAG, "Requesting location updates");
        Utils.setRequestingLocationUpdates(this, true);
        startService(new Intent(getApplicationContext(), RouteService.class));
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());
        } catch (SecurityException unlikely) {
            Utils.setRequestingLocationUpdates(this, false);
            Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
        }
    }

    /**
     * Removes location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    public void removeLocationUpdates() {
        Log.i(TAG, "Removing location updates");
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            Utils.setRequestingLocationUpdates(this, false);
            stopSelf();
        } catch (SecurityException unlikely) {
            Utils.setRequestingLocationUpdates(this, true);
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
    }

    private void getLastLocation() {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                mCurrentLocation = task.getResult();
                                startLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                                StepValue.curLatitude = mCurrentLocation.getLatitude();
                                StepValue.curLongitude = mCurrentLocation.getLongitude();
                            } else {
                                Log.w(TAG, "Failed to get location.");
                            }
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        RouteService getService() {
            return RouteService.this;
        }
    }

    /**
     * Returns true if this is a foreground service.
     *
     * @param context The {@link Context}.
     */
    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }

    public void onNewLocation(Location location) {
        Log.d(TAG, "Latitude " + location.getLatitude() + " Longitude " + location.getLongitude());
        Log.d(TAG, "SERVICE RUNNING " + isServiceRunning);

        endLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        Log.d(TAG, "Latitude " + location.getLatitude() + " Longitude " + location.getLongitude());
        // insert values to local sqlite database
        int stepCount = StepValue.Step - lastStepCount;

        if (stepCount <= 0) return;
        float distance[] = new float[1];

        Location.distanceBetween(startLatLng.latitude, startLatLng.longitude, endLatLng.latitude, endLatLng.longitude, distance);
        float dist = distance[0];

        /*if (dist * 100.0 / stepCount > 200 || dist * 100.0 / stepCount < 35) {
            return;
        }*/

        lastStepCount = StepValue.Step;
        StepValue.curLatitude = endLatLng.latitude;
        StepValue.curLongitude = endLatLng.longitude;
        startLatLng = endLatLng;
        /*SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
        String startTime = sp1.getString(Constants.SHARE_STARTTIME, null);
        String username = sp1.getString(Constants.SHARE_EMAIL, null);

        if (stime == null) stime = startTime;
        Date currentDate = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
        String etime = sdf.format(currentDate);

        double stepSize = dist * 100 / stepCount;
        DBStepData dbStepData = new DBStepData(getApplicationContext());
        dbStepData.insertStepData(username, startTime, stepCount, dist, stepSize, stime, etime, endLatLng.latitude, endLatLng.longitude);

        startLatLng = endLatLng;
        stime = etime;*/
        // send local broadcast receiver to application components
        /*Intent localBroadcastIntent = new Intent(ACTION);
        String currentPosition = String.valueOf(endLatLng.latitude) + "/" + String.valueOf(endLatLng.longitude);
        localBroadcastIntent.putExtra("RESULT_CODE", currentPosition);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(localBroadcastIntent);*/
    }
}