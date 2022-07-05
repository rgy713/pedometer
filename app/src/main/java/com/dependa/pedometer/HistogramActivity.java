package com.dependa.pedometer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import com.dependa.pedometer.base.Constants;
import com.dependa.pedometer.base.DBStepData;
import com.dependa.pedometer.base.StepBaseWithMenu;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class HistogramActivity extends StepBaseWithMenu {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_histogram);

        TextView title = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.action_bar_title);
        title.setText(R.string.result_view);

        Bundle bundle = getIntent().getExtras();
        JSONObject measurementData = new JSONObject();
        JSONObject histogramData = new JSONObject();
        double distance = 0.;
        int allStepCount = 1;

        try {
            measurementData = new JSONObject(bundle.getString("data"));
            distance = measurementData.getDouble("distance");
            allStepCount = measurementData.getInt("step_count");
            histogramData = measurementData.getJSONObject("step_histogram");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayList<BarEntry> bargroup = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<String>();

        int startIdx = 7;
        int endIdx = 24;
        if(allStepCount>0){
            startIdx = (int)(distance/allStepCount * 100 / 5) - 12;
            endIdx = (int)(distance/allStepCount * 100 / 5) + 12;
        }
        Integer idx = 0;
        for (int i = startIdx; i <= endIdx; i++) {
            String key = String.valueOf(i * 5);
            Integer value = 0;
            try{
                value = histogramData.getInt(key);
            }catch (JSONException e){
            }
            bargroup.add(new BarEntry((int) value, idx++));
            labels.add(key);
        }

        BarChart barChart = (BarChart) findViewById(R.id.histogramchart);
        BarDataSet barDataSet = new BarDataSet(bargroup, "歩幅");
        barDataSet.setColor(Constants.MAIN_COLOR);
// initialize the Bardata with argument labels and dataSet
        BarData data = new BarData(labels, barDataSet);
        data.setValueTextColor(Constants.MAIN_COLOR);
        barChart.setDescription("");
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setTextColor(Constants.MAIN_COLOR);
        barChart.getXAxis().setTextSize(8f);
        barChart.getXAxis().setLabelRotationAngle(45f);
        barChart.getXAxis().setLabelsToSkip(0);
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setAxisMinValue(0f);
        barChart.getAxisLeft().setTextColor(Constants.MAIN_COLOR);
        barChart.getAxisLeft().setTextSize(8f);
//        barChart.getAxisLeft().setAxisMaxValue(100f);
        barChart.getLegend().setEnabled(false);
        barChart.setData(data);

        barChart.animateY(1000);
    }

}
