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
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ResultViewActivity extends StepBaseWithMenu implements OnChartValueSelectedListener {

    private JSONObject measureData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_view);

        TextView title = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.action_bar_title);
        title.setText(R.string.result_view);

        Bundle bundle = getIntent().getExtras();

        try {
            measureData = new JSONObject(bundle.getString("data"));
        } catch (JSONException e) {
            e.printStackTrace();
            finish();
        }

        Button startButton = (Button) findViewById(R.id.histogram_view_btn);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ResultViewActivity.this, HistogramActivity.class);
                intent.putExtra("data", measureData.toString());
                startActivity(intent);
                overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
            }
        });


        int[] VORDIPLOM_COLORS = {
                Color.rgb(246, 164, 0), Color.rgb(233, 245, 1), Color.rgb(179, 241, 5), Color.rgb(0, 228, 246), Color.rgb(0, 173, 186),
        };
        String[] LABELS ={"狭い","やや狭い","標準","やや広い","広い"};

        PieChart pieChart = (PieChart) findViewById(R.id.piechart);

        JSONArray stat_data = null;
        try {
            stat_data = measureData.getJSONArray("step_month_distribution");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HashMap<String, Double> piData = getViewData(stat_data);

        ArrayList<Entry> yvalues = new ArrayList<Entry>();
        ArrayList<String> xVals = new ArrayList<String>();

        Integer idx = 0;
        for (String label : LABELS) {
            double value = piData.get(label);
            yvalues.add(new Entry((float) value, idx++));
            xVals.add(label);
        }

        PieDataSet dataSet = new PieDataSet(yvalues, "");
        PieData data = new PieData(xVals, dataSet);
        // In Percentage
        data.setValueFormatter(new PercentFormatter());
        // Default value
        //data.setValueFormatter(new DefaultValueFormatter(0));
        pieChart.setData(data);
        pieChart.setDescription("");
        pieChart.setDrawHoleEnabled(true);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawSliceText(false);
        pieChart.setTransparentCircleRadius(82f);
        pieChart.setHoleRadius(82f);

        dataSet.setColors(VORDIPLOM_COLORS);

        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        pieChart.getLegend().setEnabled(true);
        pieChart.getLegend().setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        pieChart.getLegend().setTextColor(Constants.MAIN_COLOR);
        pieChart.setOnChartValueSelectedListener(this);
        pieChart.animateXY(1000, 1000);
//-----------------------------Inner Chart-----------------------------------------
        PieChart pieChartIn = (PieChart) findViewById(R.id.piechart_in);

        stat_data = null;
        try {
            stat_data = measureData.getJSONArray("step_distribution");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        piData = getViewData(stat_data);

        ArrayList<Entry> yvaluesIn = new ArrayList<Entry>();
        ArrayList<String> xValsIn = new ArrayList<String>();

        idx = 0;
        for (String label : LABELS) {
            double value = piData.get(label);
            yvaluesIn.add(new Entry((float) value, idx++));
            xValsIn.add(label);
        }

        PieDataSet dataSetIn = new PieDataSet(yvaluesIn, "");
        PieData dataIn = new PieData(xValsIn, dataSetIn);
        // In Percentage
        dataIn.setValueFormatter(new PercentFormatter());
        // Default value
        //data.setValueFormatter(new DefaultValueFormatter(0));
        pieChartIn.setData(dataIn);
        pieChartIn.setDescription("");
        pieChartIn.setDrawSliceText(false);
        dataSetIn.setColors(VORDIPLOM_COLORS);
        pieChartIn.setUsePercentValues(true);
        pieChartIn.setDrawHoleEnabled(false);
        dataIn.setValueTextSize(11f);
        dataIn.setValueTextColor(Color.WHITE);
        pieChartIn.getLegend().setEnabled(false);
        pieChartIn.setOnChartValueSelectedListener(this);
        pieChartIn.animateXY(1000, 1000);
    }

    private HashMap<String, Double> getViewData(JSONArray stat_data) {

        HashMap<String, Double> piData = new HashMap<String, Double>();
        try {
            piData.put("狭い", stat_data.getDouble(0) * 100);
            piData.put("やや狭い", stat_data.getDouble(1) * 100);
            piData.put("標準", stat_data.getDouble(2) * 100);
            piData.put("やや広い", stat_data.getDouble(3) * 100);
            piData.put("広い", stat_data.getDouble(4) * 100);
        }catch (JSONException e){

        }
        return piData;
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        if (e == null)
            return;
        Log.i("VAL SELECTED", "Value: " + e.getVal() + ", xIndex: " + e.getXIndex() + ", DataSet index: " + dataSetIndex);
    }

    @Override
    public void onNothingSelected() {
        Log.i("PieChart", "nothing selected");
    }

}
