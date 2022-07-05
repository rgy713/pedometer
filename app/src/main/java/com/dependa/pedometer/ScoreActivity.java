package com.dependa.pedometer;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dependa.pedometer.base.Constants;
import com.dependa.pedometer.base.HttpPostRequest;
import com.dependa.pedometer.base.StepBaseWithMenu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

public class ScoreActivity extends StepBaseWithMenu {
    private GetScoreDataTask mGetScoreDataTask = null;
    private JSONObject scoreData;
    private RadarChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        TextView title = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.action_bar_title);
        title.setText(R.string.score_page);

        mChart = (RadarChart) findViewById(R.id.radarchart);

        getScoreData();

        Button goToBMIButton = (Button) findViewById(R.id.goto_bmi_btn);
        goToBMIButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
                String email = sp1.getString(Constants.SHARE_EMAIL, null);
                String pwd = sp1.getString(Constants.SHARE_PWD, null);

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.GOTO_DASHBOARD_URL + "?id=weight_content" + "&email=" + email + "&password=" + pwd));
                startActivity(intent);
            }
        });

        Button goToStepButton = (Button) findViewById(R.id.goto_step_btn);
        goToStepButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
                String email = sp1.getString(Constants.SHARE_EMAIL, null);
                String pwd = sp1.getString(Constants.SHARE_PWD, null);

                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(Constants.GOTO_DASHBOARD_URL + "?id=bar_content" + "&email=" + email + "&password=" + pwd));
                startActivity(intent);
            }
        });

        Button goToStepSizeButton = (Button) findViewById(R.id.goto_stepsize_btn);
        goToStepSizeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
                String email = sp1.getString(Constants.SHARE_EMAIL, null);
                String pwd = sp1.getString(Constants.SHARE_PWD, null);

                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(Constants.GOTO_DASHBOARD_URL + "?id=stepsize_content" + "&email=" + email + "&password=" + pwd));
                startActivity(intent);
            }
        });

        Button goToMealButton = (Button) findViewById(R.id.goto_meal_btn);
        goToMealButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
                String email = sp1.getString(Constants.SHARE_EMAIL, null);
                String pwd = sp1.getString(Constants.SHARE_PWD, null);

                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(Constants.GOTO_HEALTH_URL + "?id=mealAdd" + "&email=" + email + "&password=" + pwd));
                startActivity(intent);
            }
        });

        Button goToSleepButton = (Button) findViewById(R.id.goto_sleep_btn);
        goToSleepButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
                String email = sp1.getString(Constants.SHARE_EMAIL, null);
                String pwd = sp1.getString(Constants.SHARE_PWD, null);

                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(Constants.GOTO_HEALTH_URL + "?id=sleepAdd" + "&email=" + email + "&password=" + pwd));
                startActivity(intent);
            }
        });

        Button goToSleepTimeButton = (Button) findViewById(R.id.goto_sleeptime_btn);
        goToSleepTimeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
                String email = sp1.getString(Constants.SHARE_EMAIL, null);
                String pwd = sp1.getString(Constants.SHARE_PWD, null);

                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(Constants.GOTO_HEALTH_URL + "?id=sleepAdd" + "&email=" + email + "&password=" + pwd));
                startActivity(intent);
            }
        });
    }

    private void getScoreData() {
        if (mGetScoreDataTask != null) {
            return;
        }

        SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
        String email = sp1.getString(Constants.SHARE_EMAIL, null);

        mGetScoreDataTask = new GetScoreDataTask(email);
        mGetScoreDataTask.execute((Void) null);
    }

    private class GetScoreDataTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private String mErrorMsg;

        GetScoreDataTask(String email) {
            this.mEmail = email;
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
                    result = httpPostRequest.GET(Constants.GET_SCORE_DATA, getParams);

                    if (result == null) return false;
                    if (!result.getString("type").equals("success")) {
                        mErrorMsg = result.getString("content");
                        return false;
                    }

                    scoreData = result.getJSONObject("content");
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
            mGetScoreDataTask = null;

            if (success) {

            } else{
                AlertDialog.Builder builder = new AlertDialog.Builder(ScoreActivity.this);
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

            viewRadarChart();
        }

        @Override
        protected void onCancelled() {
            mGetScoreDataTask = null;
        }
    }

    private void viewRadarChart(){

        mChart.setWebLineWidth(1.5f);
        mChart.setWebLineWidthInner(0.75f);
        mChart.setWebAlpha(100);
        mChart.getLegend().setEnabled(false);
        mChart.setDescription("");
        mChart.setRotationEnabled(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setTextSize(9f);
        xAxis.setTextColor(R.color.mainColor);

        YAxis yAxis = mChart.getYAxis();
        yAxis.setLabelCount(6, true);
        yAxis.setTextSize(9f);
        yAxis.setAxisMinValue(0f);
        yAxis.setAxisMaxValue(10f);

        Legend l = mChart.getLegend();
        l.setPosition(LegendPosition.RIGHT_OF_CHART);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(5f);

        setData();
    }

    private String[] mParties = new String[]{
            "BMI", "歩幅伸展", "習慣性", "継続性", "バランス", "習慣性", "継続性", "睡眠の質",
            "習慣性", "継続性"
    };

    private String[] mCategories = new String[]{
            "BMI", "step_size_score", "step_habit_score", "step_continuity_score", "meal_balance_score", "meal_habit_score", "meal_continuity_score", "sleep_quality_score",
            "sleep_std_score", "sleep_continuity_score"
    };

    public void setData() {

        ArrayList<Entry> yVals0 = new ArrayList<Entry>();
        ArrayList<Entry> yVals1 = new ArrayList<Entry>();
        ArrayList<Entry> yVals2 = new ArrayList<Entry>();
        ArrayList<Entry> yVals = new ArrayList<Entry>();

        ArrayList<String> xVals = new ArrayList<String>();

        float score_sum = 0;

        for (int i = 0; i < 10; i++) {
            xVals.add(mParties[i]);

            float score = 0;

            try {
                score = (float) scoreData.getDouble(mCategories[i]);
                score_sum += score;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            yVals.add(new Entry(score, i));
            yVals0.add(new Entry(10, i));
            yVals1.add(new Entry(8, i));
            yVals2.add(new Entry(4, i));
        }

        RadarDataSet set0 = new RadarDataSet(yVals0, "");
        set0.setColor(Color.rgb(52, 235, 152));
        set0.setFillColor(Color.rgb(52, 235, 152));
        set0.setDrawFilled(true);
        set0.setLineWidth(1f);

        RadarDataSet set1 = new RadarDataSet(yVals1, "");
        set1.setColor(Color.rgb(235, 253, 53));
        set1.setFillColor(Color.rgb(235, 253, 53));
        set1.setDrawFilled(true);
        set1.setLineWidth(1f);

        RadarDataSet set2 = new RadarDataSet(yVals2, "");
        set2.setColor(Color.rgb(235, 70, 52));
        set2.setFillColor(Color.rgb(235, 70, 52));
        set2.setDrawFilled(true);
        set2.setLineWidth(1f);

        RadarDataSet set = new RadarDataSet(yVals, "Score");
        set.setColor(Color.rgb(21, 126, 140));
        set.setFillColor(Color.rgb(21, 126, 140));
        set.setDrawFilled(false);
        set.setLineWidth(3f);
        set.setDrawHighlightCircleEnabled(true);
        set.setHighlightCircleStrokeWidth(5f);

        ArrayList<IRadarDataSet> sets = new ArrayList<IRadarDataSet>();
        sets.add(set0);
        sets.add(set1);
        sets.add(set2);
        sets.add(set);

        RadarData data = new RadarData(xVals, sets);
        data.setValueTextSize(8f);
        data.setDrawValues(false);

        mChart.setData(data);

        mChart.invalidate();

        TextView title = (TextView) findViewById(R.id.score_sum);
        title.setText(String.format("%.2f", score_sum));
    }
}
