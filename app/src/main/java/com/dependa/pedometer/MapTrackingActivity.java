package com.dependa.pedometer;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dependa.pedometer.base.Constants;
import com.dependa.pedometer.base.DBStepData;
import com.dependa.pedometer.base.HttpPostRequest;
import com.dependa.pedometer.base.PermissionUtils;
import com.dependa.pedometer.base.StepBase;
import com.dependa.pedometer.base.StepValue;
import com.dependa.pedometer.base.Utils;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MapTrackingActivity extends StepBase implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = MapTrackingActivity.class.getSimpleName();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private Location mCurrentLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private LocationSettingsRequest mLocationSettingsRequest;
    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    private Boolean mRequestingLocationUpdates;
    /**
     * Provides access to the Location Settings API.
     */
    private SettingsClient mSettingsClient;
    /**
     * Constant used in the location settings dialog.
     */
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private boolean mPermissionDenied;
    private LocationManager locationManager;
    private Marker mCurrentMarker;
    private LatLng startLatLng = new LatLng(0, 0);        //polyline 시작점
    private LatLng endLatLng = new LatLng(0, 0);        //polyline 끝점

    private boolean walkState = false;                    //걸음 상태

    private Intent pedoService;
    private Intent routeService;
    private PedoReceiver pedoReceiver;
//    private RouteBroadCastReceiver routeReceiver;

    private ImageButton startButton;
    private TextView stepCountView;
    private double distance = 0.0;

    private FileUploadTask mFileUploadTask = null;
    private boolean fileUploadResult = false;
    private GetMeasurementDataTask mGetMeasurementDataTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_tracking);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        pedoService = new Intent(this, StepCheckService.class);
        pedoReceiver = new PedoReceiver();

        routeService = new Intent(this, RouteService.class);
//        routeReceiver = new RouteBroadCastReceiver();

        startButton = (ImageButton) findViewById(R.id.start_step_btn);
        startButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

            changeWalkState();        //걸음 상태 변경

            }
        });

        stepCountView = (TextView) findViewById(R.id.step_count);

        if (StepValue.StepState) {
            walkState = true;
            startButton.setImageResource(R.drawable.measure_stop);

            IntentFilter pedoFilter = new IntentFilter(StepCheckService.ACTION);
            LocalBroadcastManager.getInstance(this).registerReceiver(pedoReceiver, pedoFilter);

            /*IntentFilter routeFilter = new IntentFilter(RouteService.ACTION);
            LocalBroadcastManager.getInstance(this).registerReceiver(routeReceiver, routeFilter);*/
        }

        mRequestingLocationUpdates = false;
        mSettingsClient = LocationServices.getSettingsClient(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
        enableMyLocation();
        if (!StepValue.StepState) {
            mRequestingLocationUpdates = true;
            startLocationUpdates();
        }

        if(!walkState)
            changeWalkState();
    }

    /**
     * Provides a simple way of getting a device's location and is well suited for
     * applications that do not require a fine-grained location and that do not need location
     * updates. Gets the best and most recent location currently available, which may be null
     * in rare cases when a location is not available.
     * <p>
     * Note: this method should be called after location permission has been granted.
     */
    private void getLastLocation() {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                mCurrentLocation = task.getResult();
                                updateLocationUI();
                            }
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation();
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE, Manifest.permission.ACCESS_FINE_LOCATION, false);
//            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE, Manifest.permission.WRITE_EXTERNAL_STORAGE, false);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            //mMap.setMyLocationEnabled(true);
            getLastLocation();
        }
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(5000);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(3000);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Creates a callback for receiving location events.
     */
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mCurrentLocation = locationResult.getLastLocation();
                updateLocationUI();
            }
        };
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * Sets the value of the UI fields for the location latitude, longitude and last update time.
     */
    private void updateLocationUI() {
        if (mCurrentLocation != null) {
            startLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            Log.d("MapTrackingActivity", "Latitude 4: " + mCurrentLocation.getLatitude() + " Longitude 4: " + mCurrentLocation.getLongitude());
            refreshMap(mMap);
            markStartingLocationOnMap(mMap, startLatLng);
            startPolyline(mMap, startLatLng);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        getLastLocation();
                        break;
                }
                break;
        }
    }

    /**
     * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
     * runtime permission has been granted.
     */
    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {

                        //noinspection MissingPermission
                        if (ActivityCompat.checkSelfPermission(MapTrackingActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapTrackingActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            enableMyLocation();
                            return;
                        }
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        getLastLocation();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(MapTrackingActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(MapTrackingActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                mRequestingLocationUpdates = false;
                        }

                        getLastLocation();
                    }
                });
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    private void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            return;
        }

        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mRequestingLocationUpdates = false;
                    }
                });
    }

    private void markStartingLocationOnMap(GoogleMap mapObject, LatLng location) {
        mapObject.addMarker(new MarkerOptions().position(location).title("現在の位置"));
        mapObject.moveCamera(CameraUpdateFactory.newLatLng(location));
    }

    private List<LatLng> getPoints(List<LatLng> mLocations) {
        List<LatLng> points = new ArrayList<>();
        for (LatLng mLocation : mLocations) {
            points.add(new LatLng(mLocation.latitude, mLocation.longitude));
        }
        return points;
    }

    private void startPolyline(GoogleMap map, LatLng location) {
        if (map == null) {
            Log.d("MapTrackingActivity", "Map object is not null");
            return;
        }
        PolylineOptions options = new PolylineOptions().width(24).color(Color.argb(120, 0, 0, 200)).geodesic(true);
        options.add(location);
        Polyline polyline = map.addPolyline(options);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(location)
                .zoom(16)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void drawRouteOnMap(GoogleMap map, List<LatLng> positions) {
        PolylineOptions options = new PolylineOptions().width(24).color(Color.argb(120, 0, 0, 200)).geodesic(true);
        options.addAll(positions);
        Polyline polyline = map.addPolyline(options);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(positions.get(positions.size() - 1).latitude, positions.get(positions.size() - 1).longitude))
                .zoom(16)
//                .bearing(90)
//                .tilt(40)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void refreshMap(GoogleMap mapInstance) {
        mapInstance.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRequestingLocationUpdates && checkPermissions()) {
            startLocationUpdates();
        }

        enableMyLocation();

        if (StepValue.StepState) {
            /*if (routeReceiver == null) {
                routeReceiver = new RouteBroadCastReceiver();
            }*/
            if (pedoReceiver == null) {
                pedoReceiver = new PedoReceiver();
            }
            /*IntentFilter filter = new IntentFilter(RouteService.ACTION);
            LocalBroadcastManager.getInstance(this).registerReceiver(routeReceiver, filter);*/
            IntentFilter filter = new IntentFilter(StepCheckService.ACTION);
            LocalBroadcastManager.getInstance(this).registerReceiver(pedoReceiver, filter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Remove location updates to save battery.
        stopLocationUpdates();
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(routeReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(pedoReceiver);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void changeWalkState() {
        if (!walkState) {
            try {
                final RelativeLayout countDownBack = (RelativeLayout) findViewById(R.id.cound_down);
                final TextView countDownText = (TextView) findViewById(R.id.count_down_text);
                countDownText.setText(Constants.COUNT_DOWN + "");
                countDownBack.setVisibility(View.VISIBLE);
                startButton.setClickable(false);
                new CountDownTimer(Constants.COUNT_DOWN * 1000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        if ((millisUntilFinished/1000 -1) <= 0)
                            countDownText.setText(R.string.start_step);
                        else
                            countDownText.setText(""+ (millisUntilFinished/1000 -1));
                    }

                    @Override
                    public void onFinish() {
                        stopLocationUpdates();
                        IntentFilter pedoFilter = new IntentFilter(StepCheckService.ACTION);
                        LocalBroadcastManager.getInstance(MapTrackingActivity.this).registerReceiver(pedoReceiver, pedoFilter);
                        startService(pedoService);

                /*IntentFilter routeFilter = new IntentFilter(RouteService.ACTION);
                LocalBroadcastManager.getInstance(this).registerReceiver(routeReceiver, routeFilter);*/
                        startService(routeService);

                        startButton.setClickable(true);
                        startButton.setImageResource(R.drawable.measure_stop);
                        walkState = true;
//                        Toast.makeText(getApplicationContext(), "スタート", Toast.LENGTH_SHORT).show();

                        Date currentDate = Calendar.getInstance().getTime();
                        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
                        String startTime = sdf.format(currentDate);

                        SharedPreferences sp = getSharedPreferences(Constants.SHARE_PREF, 0);
                        SharedPreferences.Editor Ed = sp.edit();
                        Ed.putString(Constants.SHARE_STARTTIME, startTime);
                        Ed.apply();

                        countDownBack.setVisibility(View.INVISIBLE);
                    }
                }.start();

            } catch (Exception e) {
                // TODO: handle exception
                Toast.makeText(getApplicationContext(), e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }

        } else {
            try {
                //startLocationUpdates();

                LocalBroadcastManager.getInstance(this).unregisterReceiver(pedoReceiver);
                stopService(pedoService);

//                LocalBroadcastManager.getInstance(this).unregisterReceiver(routeReceiver);
                stopService(routeService);

//                Toast.makeText(getApplicationContext(), "ストップ", Toast.LENGTH_SHORT).show();
                startButton.setImageResource(R.drawable.measure_start);
                walkState = false;

                pedometerDataUpload();
            } catch (Exception e) {
                // TODO: handle exception
                Toast.makeText(getApplicationContext(), e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private class PedoReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("PlayignReceiver", "IN");
            String stepCount = intent.getStringExtra("stepService");
            String viewText = "";
            assert stepCount != null;
            if (StepValue.StepState && !stepCount.equals("null")) {
                SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
                String startTime = sp1.getString(Constants.SHARE_STARTTIME, null);
                String email = sp1.getString(Constants.SHARE_EMAIL, null);

                DBStepData dbStepData = new DBStepData(getApplicationContext());
                ArrayList<HashMap<String, Object>> stat_data = null;
                stat_data = dbStepData.getStepData(email, startTime);
                List<LatLng> startToPresentLocations = new ArrayList<>();
                distance = 0.0;
                for (HashMap<String, Object> one_data : stat_data) {
                    startToPresentLocations.add(new LatLng((double) one_data.get(Constants.FLD_latitude), (double) one_data.get(Constants.FLD_longitude)));
                    distance += (double) one_data.get(Constants.FLD_distance);
                }

                if (startToPresentLocations.size() > 0) {
                    //prepare map drawing.
                    List<LatLng> locationPoints = getPoints(startToPresentLocations);
                    if(mMap != null){
                        refreshMap(mMap);
                        markStartingLocationOnMap(mMap, locationPoints.get(startToPresentLocations.size() - 1));
                        drawRouteOnMap(mMap, locationPoints);
                    }
                }

                Date currentDate = Calendar.getInstance().getTime();
                SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
                try {
                    Date startDate = sdf.parse(startTime);
                    long different = currentDate.getTime() - startDate.getTime();
                    long secondsInMilli = 1000;
                    long minutesInMilli = secondsInMilli * 60;
                    long hoursInMilli = minutesInMilli * 60;
                    long hours = different / hoursInMilli;
                    long elapsedMinutes = different % hoursInMilli;
                    long minutes = elapsedMinutes / minutesInMilli;
                    viewText = "所要時間: " + hours + "時間" + minutes + "分\n";
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                viewText += "歩数        : " + stepCount + "歩\n";
                viewText += "歩行距離: " + String.format("%.3f", (distance / 1000.0)) + "km";
                stepCountView.setText(viewText);
//            Toast.makeText(getApplicationContext(), "Step:" + serviceData + " Distance", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class RouteBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String local = intent.getStringExtra("RESULT_CODE");
            if (local != null) {
                if (StepValue.StepState && mCurrentLocation != null) {
                    SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
                    String startTime = sp1.getString(Constants.SHARE_STARTTIME, null);
                    String email = sp1.getString(Constants.SHARE_EMAIL, null);

                    DBStepData dbStepData = new DBStepData(getApplicationContext());
                    ArrayList<HashMap<String, Object>> stat_data = null;
                    stat_data = dbStepData.getStepData(email, startTime);
                    List<LatLng> startToPresentLocations = new ArrayList<>();
                    distance = 0.0;
                    for (HashMap<String, Object> one_data : stat_data) {
                        startToPresentLocations.add(new LatLng((double) one_data.get(Constants.FLD_latitude), (double) one_data.get(Constants.FLD_longitude)));
                        distance += (double) one_data.get(Constants.FLD_distance);
                    }

                    if (startToPresentLocations.size() > 0) {
                        //prepare map drawing.
                        List<LatLng> locationPoints = getPoints(startToPresentLocations);
                        refreshMap(mMap);
                        markStartingLocationOnMap(mMap, locationPoints.get(startToPresentLocations.size() - 1));
                        drawRouteOnMap(mMap, locationPoints);
                    }
                }
            }
        }
    }

    private void pedometerDataUpload() {
        SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
        String email = sp1.getString(Constants.SHARE_EMAIL, null);
        DBStepData dbStepData = new DBStepData(getApplicationContext());
        String filePath = MapTrackingActivity.this.getFilesDir() + File.separator + email;
        if (!dbStepData.exportAllStepData(email, filePath)) {
//            dbStepData.updateStepDataFailUpload(email,startTime);
            return;
        }

        /*TelephonyManager telephonyManager;

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = telephonyManager.getDeviceId();*/
        if (mFileUploadTask != null) {
            return;
        }
        mFileUploadTask = new MapTrackingActivity.FileUploadTask(email, Constants.DEVICEID, filePath);
        mFileUploadTask.execute((Void) null);
    }

    private class FileUploadTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mMachineId;
        private final String mFilePath;

        FileUploadTask(String email, String machineId, String filePath) {
            this.mEmail = email;
            this.mMachineId = machineId;
            this.mFilePath = filePath;
        }

        private boolean isConnected() {
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected())
                return true;
            else
                return false;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (isConnected()) {
                ContentValues postParams = new ContentValues();
                JSONObject result = null;
                try {
                    postParams.put("email", this.mEmail);
                    postParams.put("machine_id", this.mMachineId);

                    HttpPostRequest httpPostRequest = new HttpPostRequest();
                    result = httpPostRequest.FileUpload(postParams, this.mFilePath);
                    if (result == null) return false;
                    if (!result.getString("type").equals("success")) {
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mFileUploadTask = null;
            SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
            String email = sp1.getString(Constants.SHARE_EMAIL, null);
            String startTime = sp1.getString(Constants.SHARE_STARTTIME, null);
            DBStepData dbStepData = new DBStepData(getApplicationContext());
            if (success) {
                fileUploadResult = true;
                dbStepData.updatedStepDataRemove(email);
                Toast.makeText(getApplicationContext(), "データがアップロードされました。", Toast.LENGTH_SHORT).show();
                try {
                    startTime = Utils.toUTC(startTime,Constants.DATE_FORMAT);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                getMeasurementData(email, startTime);
            } else {

//                dbStepData.updateStepDataFailUpload(email,startTime);
            }
        }

        @Override
        protected void onCancelled() {
            mFileUploadTask = null;
        }
    }

    private void getMeasurementData(String email, String startTime) {
        if (mGetMeasurementDataTask != null) {
            return;
        }
        mGetMeasurementDataTask = new MapTrackingActivity.GetMeasurementDataTask(email, startTime);
        mGetMeasurementDataTask.execute((Void) null);
    }

    private class GetMeasurementDataTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mStartTime;
        private String mErrorMsg;
        private String mData;

        GetMeasurementDataTask(String email, String dateTime) {
            this.mEmail = email;
            this.mStartTime = dateTime;
        }

        private boolean isConnected() {
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected())
                return true;
            else
                return false;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (isConnected()) {
                ContentValues getParams = new ContentValues();
                JSONObject result = null;
                try {
                    getParams.put("email", mEmail);
                    getParams.put("start_time", mStartTime);

                    HttpPostRequest httpPostRequest = new HttpPostRequest();
                    result = httpPostRequest.GET(Constants.GET_MEASUREMENT_DATA, getParams);

                    if (result == null) return false;
                    if (!result.getString("type").equals("success")) {
                        mErrorMsg = result.getString("content");
                        return false;
                    }
                    try {
                        JSONObject content = result.getJSONObject("content");
                        //TODO
                        mData = content.toString();
                    }catch (Exception e){
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mGetMeasurementDataTask = null;

            if (success) {
                Intent intent = new Intent(MapTrackingActivity.this, ResultViewActivity.class);
                //TODO
                intent.putExtra("data", mData);
                startActivity(intent);
                overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
                finish();
            } else{
                AlertDialog.Builder builder = new AlertDialog.Builder(MapTrackingActivity.this);
                builder.setMessage(mErrorMsg)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }

        }

        @Override
        protected void onCancelled() {
            mGetMeasurementDataTask = null;
        }
    }
}
