package com.dependa.pedometer;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dependa.pedometer.base.Constants;
import com.dependa.pedometer.base.DBStepData;
import com.dependa.pedometer.base.HttpPostRequest;
import com.dependa.pedometer.base.StepBaseWithMenu;
import com.dependa.pedometer.base.Utils;
import com.stacktips.view.CalendarListener;
import com.stacktips.view.CustomCalendarView;
import com.stacktips.view.DayDecorator;
import com.stacktips.view.DayView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ResultSelectActivity extends StepBaseWithMenu implements CalendarListener {

    private ResultSelectActivity.GetCalendarDataTask mGetMainDataTask = null;
    private ResultSelectActivity.GetMeasurementDataTask mGetMeasurementDataTask = null;

    private Spinner mSelectTimeView;
    private TextView resultViewDate;
    private ArrayAdapter<String> mAdapterTime;
    private HashMap<String, ArrayList<String>> measuredDays;
    private ArrayList<String> days;
    private CustomCalendarView calendarView;
    private String email;
    private String yearMonth;
    private String yearMonthUtc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_select);

        TextView title = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.action_bar_title);
        title.setText(R.string.result_view);

        mSelectTimeView = (Spinner) findViewById(R.id.timespinner);
        resultViewDate = (TextView) findViewById(R.id.result_view_date);
        Button startButton = (Button) findViewById(R.id.result_view_btn);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(resultViewDate.getText()).equals("")) {
                    String selectedDate = resultViewDate.getText().toString();
                    if (mSelectTimeView.getSelectedItem() == null) {
                        Toast.makeText(getApplicationContext(), "測定データがありません。", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    String selectedTime = selectedDate + "T" + mSelectTimeView.getSelectedItem().toString();
                    try {
                        selectedTime = Utils.toUTC(selectedTime, Constants.DATE_FORMAT);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    getMeasurementData(selectedTime);
                } else {
                    Toast.makeText(getApplicationContext(), "日付を選択してください。", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Initialize CustomCalendarView from layout
        calendarView = (CustomCalendarView) findViewById(R.id.calendar_view);
        //Show Monday as first date of week
        calendarView.setFirstDayOfWeek(Calendar.SUNDAY);
        //Show/hide overflow days of a month
        calendarView.setShowOverflowDate(false);

        //Handling custom calendar events
        calendarView.setCalendarListener(new CalendarListener() {
            @Override
            public void onDateSelected(Date date) {
                SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT_yMd);
                String yearMonth = df.format(date);
                resultViewDate.setText(yearMonth);

                ArrayList<String> times = null;

                try {
                    times = measuredDays.get(yearMonth);
                } catch (NullPointerException e){

                }

                if(times == null)
                    times = new ArrayList<String>();

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(ResultSelectActivity.this, android.R.layout.simple_spinner_item, times);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSelectTimeView.setAdapter(adapter);
            }

            @Override
            public void onMonthChanged(Date date) {
                SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT_yM);
                String dateString  = df.format(date);

                resultViewDate.setText(dateString);
                ArrayList<String> times = new ArrayList<String>();
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(ResultSelectActivity.this, android.R.layout.simple_spinner_item, times);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSelectTimeView.setAdapter(adapter);

                getCalendarData(dateString);
            }
        });

        Calendar currentCalendar = Calendar.getInstance(Locale.getDefault());

        calendarView.refreshCalendar(currentCalendar);

        SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
        String email = sp1.getString(Constants.SHARE_EMAIL, null);

        Date nowTime = Calendar.getInstance().getTime();

        SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT_yM);

        yearMonth = df.format(nowTime);

        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        yearMonthUtc = df.format(nowTime);

        getCalendarData(yearMonthUtc);

        resultViewDate.setText(yearMonth);
    }

    private void viewCalendar( String yearMonth){
//Initialize calendar with date
        Calendar currentCalendar = Calendar.getInstance(Locale.getDefault());
        SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT_yM);

        Date date = currentCalendar.getTime();
        try {
            date = format.parse(yearMonth);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        currentCalendar.setTime(date);

        List<DayDecorator> decorators = new ArrayList<>();

        decorators.add(new DisabledColorDecorator());

        calendarView.setDecorators(decorators);

//call refreshCalendar to update calendar the view
        calendarView.refreshCalendar(currentCalendar);

    }

    @Override
    public void onDateSelected(Date date) {

    }

    @Override
    public void onMonthChanged(Date date) {

    }

    private class DisabledColorDecorator implements DayDecorator {
        @Override
        public void decorate(DayView dayView) {
            SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT_yMd);
            try{
                if ((measuredDays.get(df.format(dayView.getDate()))) != null) {
                    int color = Color.rgb(123, 209, 217);
                    dayView.setBackgroundColor(color);
                    dayView.setBackgroundResource(R.drawable.shape_circle);
                }}
            catch (NullPointerException e){

            }
        }
    }

    private void getCalendarData(String yearMonth) {
        if (mGetMainDataTask != null) {
            return;
        }

        SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
        String email = sp1.getString(Constants.SHARE_EMAIL, null);

        mGetMainDataTask = new ResultSelectActivity.GetCalendarDataTask(email, yearMonth);
        mGetMainDataTask.execute((Void) null);
    }

    private class GetCalendarDataTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mYearMonth;
        private String mErrorMsg;

        GetCalendarDataTask(String email, String yearMonth) {
            this.mEmail = email;
            this.mYearMonth = yearMonth;
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
                    getParams.put("year_month", mYearMonth);

                    HttpPostRequest httpPostRequest = new HttpPostRequest();
                    result = httpPostRequest.GET(Constants.GET_CALENDAR_DATA, getParams);

                    if (result == null) return false;
                    if (!result.getString("type").equals("success")) {
                        mErrorMsg = result.getString("content");
                        return false;
                    }
                    //TODO
                    days = new ArrayList<>();
                    try {
                        JSONArray jArray = result.getJSONArray("content");

                        for (int i=0;i<jArray.length();i++){
                            days.add(jArray.getString(i));
                        }
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
            mGetMainDataTask = null;

            if (success) {
                //TODO
                measuredDays = new HashMap<String, ArrayList<String>>();
                for(String dateTimeUtc : days){
                    String dateTime = "";
                    try {
                        dateTime = Utils.toLocalTime(dateTimeUtc, Constants.DATE_FORMAT);
                    } catch (ParseException e) {
                        continue;
                    }
                    ArrayList<String> timeArray;
                    if( measuredDays.get(dateTime.split("T")[0]) == null)
                        timeArray = new ArrayList<String>();
                    else{
                        timeArray = measuredDays.get(dateTime.split("T")[0]);
                    }
                    timeArray.add(dateTime.split("T")[1]);
                    measuredDays.put(dateTime.split("T")[0], timeArray);
                }
            } else{
                AlertDialog.Builder builder = new AlertDialog.Builder(ResultSelectActivity.this);
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

            viewCalendar(mYearMonth);
        }

        @Override
        protected void onCancelled() {
            mGetMainDataTask = null;
        }
    }

    private void getMeasurementData(String startTime) {
        if (mGetMeasurementDataTask != null) {
            return;
        }

        SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
        String email = sp1.getString(Constants.SHARE_EMAIL, null);

        mGetMeasurementDataTask = new ResultSelectActivity.GetMeasurementDataTask(email, startTime);
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
                Intent intent = new Intent(ResultSelectActivity.this, ResultMapViewActivity.class);
                //TODO
                intent.putExtra("data", mData);
                startActivity(intent);
                overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
            } else{
                AlertDialog.Builder builder = new AlertDialog.Builder(ResultSelectActivity.this);
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
