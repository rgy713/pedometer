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
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dependa.pedometer.base.Constants;
import com.dependa.pedometer.base.HttpPostRequest;
import com.dependa.pedometer.base.StepBaseWithMenu;
import com.dependa.pedometer.base.Utils;
import com.stacktips.view.CalendarListener;
import com.stacktips.view.CustomCalendarView;
import com.stacktips.view.DayDecorator;
import com.stacktips.view.DayView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class SleepCalendarActivity extends StepBaseWithMenu implements CalendarListener {

    private GetCalendarDataTask mGetMainDataTask = null;
    private GetSleepInfoTask mGetSleepInfoTask = null;
    private GetSqInfoTask mGetSqInfoTask = null;

    private Spinner mSelectTimeView;
    private TextView regDateView;
    private HashMap<String, ArrayList<String>> regDays;
    private HashMap<String,JSONObject> regData;
    private CustomCalendarView calendarView;
    private String yearMonth;
    private String yearMonthUtc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_calendar);

        TextView title = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.action_bar_title);
        title.setText(R.string.sleep_habit);

        mSelectTimeView = (Spinner) findViewById(R.id.timespinner);
        regDateView = (TextView) findViewById(R.id.reg_date);
        Button sleepViewBtn = (Button) findViewById(R.id.sleep_view_btn);

        sleepViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(regDateView.getText()).equals("")) {
                    String selectedDate = regDateView.getText().toString();
                    if (mSelectTimeView.getSelectedItem() == null) {
                        Toast.makeText(getApplicationContext(), "登録されたデータがありません。", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    String selectedTime = selectedDate + "T" + mSelectTimeView.getSelectedItem().toString() + "Z";
                    try {
                        selectedTime = Utils.toUTC(selectedTime, Constants.DATE_FORMAT_Z);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    if (regData.get(selectedTime)== null) {
                        Toast.makeText(getApplicationContext(), "登録されたデータがありません。", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    getSleepInfo(selectedTime);

                } else {
                    Toast.makeText(getApplicationContext(), "日付を選択してください。", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button sleepAddBtn = (Button) findViewById(R.id.sleep_add_btn);

        sleepAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSqInfo();
            }
        });

        //Initialize CustomCalendarView from layout
        calendarView = (CustomCalendarView) findViewById(R.id.sleep_calendar);
        //Show Monday as first date of week
        calendarView.setFirstDayOfWeek(Calendar.SUNDAY);
        //Show/hide overflow regData of a month
        calendarView.setShowOverflowDate(false);

        //Handling custom calendar events
        calendarView.setCalendarListener(new CalendarListener() {
            @Override
            public void onDateSelected(Date date) {
                SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT_yMd);
                String yearMonth = df.format(date);
                regDateView.setText(yearMonth);

                ArrayList<String> times = null;

                try {
                    times = regDays.get(yearMonth);
                } catch (NullPointerException e){

                }

                if(times == null)
                    times = new ArrayList<String>();

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(SleepCalendarActivity.this, android.R.layout.simple_spinner_item, times);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSelectTimeView.setAdapter(adapter);
            }

            @Override
            public void onMonthChanged(Date date) {
                SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT_yM);
                String dateString  = df.format(date);

                regDateView.setText(dateString);
                ArrayList<String> times = new ArrayList<String>();
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(SleepCalendarActivity.this, android.R.layout.simple_spinner_item, times);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSelectTimeView.setAdapter(adapter);

                getCalendarData(dateString);
            }
        });
    }

    public void onStart() {
        super.onStart();
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

        regDateView.setText((new SimpleDateFormat(Constants.DATE_FORMAT_yMd)).format(nowTime));
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

        decorators.add(new SleepCalendarActivity.DisabledColorDecorator());

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
                if ((regDays.get(df.format(dayView.getDate()))) != null) {
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

        mGetMainDataTask = new GetCalendarDataTask(email, yearMonth);
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
                    result = httpPostRequest.GET(Constants.GET_SLEEP_DATA, getParams);

                    if (result == null) return false;
                    if (!result.getString("type").equals("success")) {
                        mErrorMsg = result.getString("content");
                        return false;
                    }
                    //TODO
                    regData = new HashMap<String, JSONObject>();
                    try {
                        JSONArray jArray = result.getJSONArray("content");

                        for (int i=0;i<jArray.length();i++){
                            JSONObject one = jArray.getJSONObject(i);
                            regData.put(one.getString("bed_time"), one);
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
                regDays = new HashMap<String, ArrayList<String>>();
                for(String dateTimeUtc : regData.keySet()){
                    String dateTime = "";
                    try {
                        dateTime = Utils.toLocalTime(dateTimeUtc, Constants.DATE_FORMAT);
                    } catch (ParseException e) {
                        continue;
                    }
                    ArrayList<String> timeArray;
                    if( regDays.get(dateTime.split("T")[0]) == null)
                        timeArray = new ArrayList<String>();
                    else{
                        timeArray = regDays.get(dateTime.split("T")[0]);
                    }
                    timeArray.add(dateTime.split("T")[1]);
                    regDays.put(dateTime.split("T")[0], timeArray);
                }
            } else{
                AlertDialog.Builder builder = new AlertDialog.Builder(SleepCalendarActivity.this);
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

    private void getSleepInfo(String mBedTime) {
        if (mGetSleepInfoTask != null) {
            return;
        }
        mGetSleepInfoTask = new GetSleepInfoTask(mBedTime);
        mGetSleepInfoTask.execute((Void) null);
    }

    private class GetSleepInfoTask extends AsyncTask<Void, Void, Boolean> {

        private final String mBedTime;
        private String mErrorMsg;
        private String mData;

        GetSleepInfoTask(String mBedTime) {
            this.mBedTime = mBedTime;
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

                    getParams.put("id", regData.get(mBedTime).getInt("id"));

                    HttpPostRequest httpPostRequest = new HttpPostRequest();
                    result = httpPostRequest.GET(Constants.GET_SLEEP_INFO, getParams);

                    if (result == null) return false;
                    if (!result.getString("type").equals("success")) {
                        mErrorMsg = result.getString("content");
                        return false;
                    }
                    try {
                        JSONArray content = result.getJSONArray("content");
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
            mGetSleepInfoTask = null;

            if (success) {
                Intent intent = new Intent(SleepCalendarActivity.this, SleepInfoActivity.class);
                //TODO
                intent.putExtra("data", mData);
                intent.putExtra("bedTime", mBedTime);
                try {
                    intent.putExtra("id",  regData.get(mBedTime).getInt("id"));
                    intent.putExtra("wakeupTime", regData.get(mBedTime).getString("wakeup_time"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                startActivity(intent);
                overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
            } else{
                AlertDialog.Builder builder = new AlertDialog.Builder(SleepCalendarActivity.this);
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
            mGetSleepInfoTask = null;
        }
    }

    private void getSqInfo() {
        if (mGetSqInfoTask != null) {
            return;
        }

        mGetSqInfoTask = new GetSqInfoTask();
        mGetSqInfoTask.execute((Void) null);
    }

    private class GetSqInfoTask extends AsyncTask<Void, Void, Boolean> {

        private String mErrorMsg;
        private String mData;

        GetSqInfoTask() {
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
                    HttpPostRequest httpPostRequest = new HttpPostRequest();
                    result = httpPostRequest.GET(Constants.GET_SQ_LIST, getParams);

                    if (result == null) return false;
                    if (!result.getString("type").equals("success")) {
                        mErrorMsg = result.getString("content");
                        return false;
                    }
                    try {
                        JSONArray content = result.getJSONArray("content");
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
            mGetSqInfoTask = null;

            if (success) {
                Intent intent = new Intent(SleepCalendarActivity.this, SleepInfoActivity.class);
                //TODO
                intent.putExtra("data", mData);
                startActivity(intent);
                overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
            } else{
                AlertDialog.Builder builder = new AlertDialog.Builder(SleepCalendarActivity.this);
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
            mGetSqInfoTask = null;
        }
    }

}
