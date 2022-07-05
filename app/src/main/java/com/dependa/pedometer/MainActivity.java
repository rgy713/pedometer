package com.dependa.pedometer;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dependa.pedometer.base.BackPressCloseHandler;
import com.dependa.pedometer.base.Constants;
import com.dependa.pedometer.base.DBStepData;
import com.dependa.pedometer.base.HttpPostRequest;
import com.dependa.pedometer.base.StepBaseWithMenu;
import com.dependa.pedometer.base.StepValue;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;

public class MainActivity extends StepBaseWithMenu {

    private MainActivity.GetMainDataTask mGetMainDataTask = null;
    private BackPressCloseHandler backPressCloseHandler;
    private JSONObject mainData;
    private Button startButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        backPressCloseHandler = new BackPressCloseHandler(this);

        startButton = (Button) findViewById(R.id.start_btn);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MapTrackingActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
            }
        });

        Button resultButton = (Button) findViewById(R.id.result_btn);
        resultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ResultSelectActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
            }
        });

        viewMainData();

        getMainData(true);

        if(StepValue.StepState){
            startButton.setText(R.string.ongoing_step_btn);
            Intent intent = new Intent(MainActivity.this, MapTrackingActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
        }

        Button goToWebButton = (Button) findViewById(R.id.goto_mypage_btn);
        goToWebButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                /*SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
                String email = sp1.getString(Constants.SHARE_EMAIL, null);
                String pwd = sp1.getString(Constants.SHARE_PWD, null);

                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(Constants.GOTO_WEB_URL + "?email=" + email + "&password=" + pwd));
                startActivity(intent);*/

                Intent i;
                i = new Intent(MainActivity.this, MenuActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);

            }
        });
    }

    public void onStart() {
        super.onStart();
        if(StepValue.StepState) {
            startButton.setText(R.string.ongoing_step_btn);
        }else{
            startButton.setText(R.string.start_step_btn);
        }
        getMainData(false);
    }

    private void initMaindata(){
        SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
        String mainDataString = sp1.getString(Constants.SHARE_MAIN_DATA, null);

        if(mainDataString == null){
            mainData = null;
        } else{
            try {
                mainData = new JSONObject(mainDataString);
            } catch (JSONException e) {
                e.printStackTrace();
                mainData = null;
            }
        }
    }

    private void initViewData(){
        initMaindata();
        Integer message = R.string.welcome_msg;
        boolean msgView = false;

        SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
        String email = sp1.getString(Constants.SHARE_EMAIL, null);

        DBStepData dbStepData = new DBStepData(getApplicationContext());

        double meanStepWidth = 0;
        try {
            meanStepWidth = mainData.getDouble("mean_step_size");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        boolean existUserData = dbStepData.existUserData(email);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (meanStepWidth == 0. && !existUserData) {
            msgView = true;
        }
        if (meanStepWidth == 0. && !msgView) {
            msgView = true;
            message = R.string.longtime_msg;
        }
        if (msgView) {
            builder.setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }

        HashMap<String, Object> userinfo = dbStepData.getUserData(email);

        if(String.valueOf(userinfo.get(Constants.FLD_name)).equals("null") || String.valueOf(userinfo.get(Constants.FLD_name)).isEmpty()){
            builder.setMessage(R.string.add_user_info)
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

    private void viewMainData(){
        initMaindata();

        ArrayList<BarEntry> bargroup = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<String>();

        SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
        String email = sp1.getString(Constants.SHARE_EMAIL, null);

        DBStepData dbStepData = new DBStepData(getApplicationContext());

        JSONObject stepData = null;

        try {
            stepData = mainData.getJSONObject("step_count_week");
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 7; i++) {
            Calendar cal = GregorianCalendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_YEAR, -(6 - i));

            Date date = cal.getTime();

            SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
            String startTime = sdf.format(date).split("T")[0];

            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String startTimeUtc = sdf.format(date).split("T")[0];

            String viewTime = startTime.split("-")[1] + "/" + startTime.split("-")[2];
            try {
                if (stepData == null || stepData.get(startTimeUtc) == null) {
                    bargroup.add(new BarEntry(0f, i));
                    labels.add(viewTime);
                } else {
                    bargroup.add(new BarEntry(stepData.getInt(startTimeUtc), i));
                    labels.add(viewTime);
                }
            } catch (Exception e) {
//                e.printStackTrace();
                bargroup.add(new BarEntry(0f, i));
                labels.add(viewTime);
            }
        }

        BarChart barChart = (BarChart) findViewById(R.id.barchart);
        BarDataSet barDataSet = new BarDataSet(bargroup, "Step Count");
        barDataSet.setColor(Constants.MAIN_COLOR);
        barChart.setDescription("");

// initialize the Bardata with argument labels and dataSet
        BarData data = new BarData(labels, barDataSet);
        data.setValueTextColor(Constants.MAIN_COLOR);
        barChart.getXAxis().setPosition(XAxisPosition.BOTTOM);
        barChart.getXAxis().setLabelsToSkip(0);
        barChart.getXAxis().setTextColor(Constants.MAIN_COLOR);
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setAxisMinValue(0f);
        barChart.getAxisLeft().setTextColor(Constants.MAIN_COLOR);
//      barChart.getAxisLeft().setAxisMaxValue(1.2f);
        barChart.getLegend().setEnabled(false);
        barChart.setData(data);

        barChart.animateY(1000);

        TextView avgStepWidth = (TextView) findViewById(R.id.avg_step);

        double meanStepWidth = 0;
        int rateValue = 0;
        try {
            meanStepWidth = mainData.getDouble("mean_step_size");
            rateValue = mainData.getInt("rating_value");
        } catch (Exception e) {
            e.printStackTrace();
        }
        meanStepWidth = ((int)(meanStepWidth * 1000)) / 10.;

        HashMap<String, Object> userinfo = dbStepData.getUserData(email);
        double standardStepWidth = (double) userinfo.get(Constants.FLD_height) * 0.45;
        double diffStepWidth =(int)((meanStepWidth - standardStepWidth) * 10) / 10.;
        avgStepWidth.setText(meanStepWidth + "cm    （標準歩幅 + " + diffStepWidth + "cm）");

        TextView levStepWidth = (TextView) findViewById(R.id.lev_step);

        levStepWidth.setText(rateValue + "");
        RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingBar.setRating(rateValue);
    }

    private void getMainData(boolean isInit) {
        SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
        String email = sp1.getString(Constants.SHARE_EMAIL, null);

        if (mGetMainDataTask != null) {
            return;
        }
        mGetMainDataTask = new MainActivity.GetMainDataTask(email, isInit);
        mGetMainDataTask.execute((Void) null);
    }

    private class GetMainDataTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final boolean mIsInit;
        private String mErrorMsg;

        GetMainDataTask(String email, boolean isInit) {
            this.mEmail = email;
            this.mIsInit = isInit;
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

                    HttpPostRequest httpPostRequest = new HttpPostRequest();
                    result = httpPostRequest.GET(Constants.GET_MAIN_DATA, getParams);

                    if (result == null) return false;
                    if (!result.getString("type").equals("success")) {
                        mErrorMsg = result.getString("content");
                        return false;
                    }
                    //TODO
                    JSONObject content = result.getJSONObject("content");

                    SharedPreferences sp = getSharedPreferences(Constants.SHARE_PREF, 0);
                    SharedPreferences.Editor Ed = sp.edit();
                    Ed.putString(Constants.SHARE_MAIN_DATA, content.toString());
                    Ed.apply();

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
            mGetMainDataTask = null;

            if (success) {
                //TODO
                if(mIsInit){
                    viewMainData();
                    initViewData();
                }
                else
                    viewMainData();
            } else{
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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
            mGetMainDataTask = null;
        }
    }

    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }
}
