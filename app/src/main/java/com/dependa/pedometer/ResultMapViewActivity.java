package com.dependa.pedometer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dependa.pedometer.base.Constants;
import com.dependa.pedometer.base.DBStepData;
import com.dependa.pedometer.base.StepBaseWithMenu;
import com.dependa.pedometer.base.Utils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ResultMapViewActivity extends StepBaseWithMenu implements OnMapReadyCallback{
    private GoogleMap mMap;

    private Button nextButton;
    private TextView resultStepCount;

    private double distance = 0.0;
    private int stepCount = 0;
    private List<LatLng> startToPresentLocations;

    private JSONObject data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_map_view);

        TextView title = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.action_bar_title);
        title.setText(R.string.result_view);

        Bundle bundle = getIntent().getExtras();

        try {
            data = new JSONObject(bundle.getString("data"));
        } catch (JSONException e) {
            e.printStackTrace();
            finish();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.result_map);
        mapFragment.getMapAsync(this);

        nextButton = (Button) findViewById(R.id.result_next_btn);
        nextButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ResultMapViewActivity.this, ResultViewActivity.class);
                intent.putExtra("data", data.toString());
                startActivity(intent);
                overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
            }
        });

        resultStepCount = (TextView) findViewById(R.id.result_step_count);

        try {
            viewMapRoad();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        refreshMap(mMap);
        if (startToPresentLocations.size() > 0) {
            //prepare map drawing.
            List<LatLng> locationPoints = getPoints(startToPresentLocations);
            refreshMap(mMap);
            markStartingLocationOnMap(mMap, locationPoints.get(startToPresentLocations.size() - 1),1);
            markStartingLocationOnMap(mMap, locationPoints.get(0),0);
            drawRouteOnMap(mMap, locationPoints);
        }
    }

    private void markStartingLocationOnMap(GoogleMap mapObject, LatLng location, int startOrEnd) {
        int mark =  R.drawable.marker_start;
        String title = "開始位置";
        if (startOrEnd == 1){
            mark = R.drawable.marker_stop;
            title = "終了位置";
        }
        mapObject.addMarker(
                new MarkerOptions().position(location)
                        .title(title)
                        .alpha(0.85f)
                        .icon(BitmapDescriptorFactory.fromResource(mark))
        );
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
        PolylineOptions options = new PolylineOptions().width(15).color(Color.BLUE).geodesic(true);
        options.add(location);
        Polyline polyline = map.addPolyline(options);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(location)
                .zoom(16)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void drawRouteOnMap(GoogleMap map, List<LatLng> positions) {
        PolylineOptions options = new PolylineOptions().width(12).color(Color.RED).geodesic(true);
        options.addAll(positions);
        Polyline polyline = map.addPolyline(options);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(positions.get(positions.size() - 1).latitude, positions.get(positions.size() - 1).longitude))
                .zoom(16)
//                .bearing(90)
                .tilt(40)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }


    private void refreshMap(GoogleMap mapInstance) {
        mapInstance.clear();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void viewMapRoad() throws ParseException {

        startToPresentLocations = new ArrayList<>();
        distance = 0.0;

        long different = 0;
        JSONArray latitudes = new JSONArray();
        JSONArray longitudes = new JSONArray();
        String startTime = "";

        try {
            different = (Utils.toMilliSeconds( data.getString("end_time"), Constants.DATE_FORMAT) - Utils.toMilliSeconds(data.getString("start_time"), Constants.DATE_FORMAT));
            latitudes = data.getJSONArray("latitudes");
            longitudes = data.getJSONArray("longitudes");
            startTime = Utils.toLocalTime(data.getString("start_time"), Constants.DATE_FORMAT);
            stepCount = data.getInt("step_count");
            distance = data.getDouble("distance");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i=0;i<latitudes.length();i++) {
            try {
            startToPresentLocations.add(new LatLng(latitudes.getDouble(i), longitudes.getDouble(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        String viewText = "";

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long hours = different / hoursInMilli;
        long elapsedMinutes = different % hoursInMilli;
        long minutes = elapsedMinutes / minutesInMilli;
        String[] times = startTime.split("T");
        viewText = "測定日時：" + times[0].split("-")[0] + "年" + times[0].split("-")[1] + "月" + times[0].split("-")[2] + "日 " + times[1] + "\n";
        viewText += "所要時間：" + hours + "時間" + minutes + "分\n";
        viewText += "歩数        ：" + stepCount + "歩\n";
        viewText += "歩行距離：" + String.format("%.3f", (distance / 1000.0)) + "km";
        resultStepCount.setText(viewText);
    }
}
