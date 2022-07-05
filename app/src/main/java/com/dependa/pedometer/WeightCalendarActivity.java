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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dependa.pedometer.base.Constants;
import com.dependa.pedometer.base.HttpPostRequest;
import com.dependa.pedometer.base.StepBaseWithMenu;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class WeightCalendarActivity extends StepBaseWithMenu implements CalendarListener  {

    private GetCalendarDataTask mGetMainDataTask = null;

    private TextView regDate;
    private HashMap<String, Object> reg_data;
    private CustomCalendarView weightCalendar;
    private String yearMonth;
    private Double lastWeight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_calendar);

        TextView title = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.action_bar_title);
        title.setText(R.string.weight_habit);


        regDate = (TextView) findViewById(R.id.reg_date);

        Button weightViewBtn = (Button) findViewById(R.id.weight_view_btn);

        weightViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(regDate.getText()).equals("")) {
                    String selectedDate = regDate.getText().toString();
                    if(reg_data.get(selectedDate)== null){
                        Toast.makeText(getApplicationContext(), "登録されたデータがありません。", Toast.LENGTH_SHORT).show();
                    }
                    else
                        getWeightInfo(selectedDate);
                } else {
                    Toast.makeText(getApplicationContext(), "日付を選択してください。", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button weightAddBtn = (Button) findViewById(R.id.weight_add_btn);

        weightAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(regDate.getText()).equals("")) {
                    String selectedDate = regDate.getText().toString();
                    if(reg_data.get(selectedDate)!= null){
                        Toast.makeText(getApplicationContext(), "登録されたデータがあります。", Toast.LENGTH_SHORT).show();
                    }
                    else
                        addWeight(selectedDate);
                } else {
                    Toast.makeText(getApplicationContext(), "日付を選択してください。", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Initialize CustomCalendarView from layout
        weightCalendar = (CustomCalendarView) findViewById(R.id.weight_calendar);
        //Show Monday as first date of week
        weightCalendar.setFirstDayOfWeek(Calendar.SUNDAY);
        //Show/hide overflow reg_data of a month
        weightCalendar.setShowOverflowDate(false);

        //Handling custom calendar events
        weightCalendar.setCalendarListener(new CalendarListener() {
            @Override
            public void onDateSelected(Date date) {
                SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT_yMd);
                String yearMonth = df.format(date);
                regDate.setText(yearMonth);
            }

            @Override
            public void onMonthChanged(Date date) {
                SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT_yM);
                String dateString  = df.format(date);

                regDate.setText(dateString);

                getCalendarData(dateString);
            }
        });
    }

    public void onStart() {
        super.onStart();

        Calendar currentCalendar = Calendar.getInstance(Locale.getDefault());

        weightCalendar.refreshCalendar(currentCalendar);

        Date nowTime = Calendar.getInstance().getTime();

        SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT_yM);

        yearMonth = df.format(nowTime);

        getCalendarData(yearMonth);

        regDate.setText((new SimpleDateFormat(Constants.DATE_FORMAT_yMd)).format(nowTime));
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

        weightCalendar.setDecorators(decorators);

//call refreshCalendar to update calendar the view
        weightCalendar.refreshCalendar(currentCalendar);

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
                if ((reg_data.get(df.format(dayView.getDate()))) != null) {
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
                    result = httpPostRequest.GET(Constants.GET_WEIGHT_MONTH_DATA, getParams);

                    if (result == null) return false;
                    if (!result.getString("type").equals("success")) {
                        mErrorMsg = result.getString("content");
                        return false;
                    }
                    //TODO
                    reg_data = new HashMap<String, Object>();
                    try {
                        JSONArray jArray = result.getJSONArray("content");

                        for (int i=0;i<jArray.length();i++){
                            JSONObject one = jArray.getJSONObject(i);
                            reg_data.put(one.getString("reg_date"), one);
                        }
                        lastWeight = jArray.getJSONObject(jArray.length() - 1).getDouble("weight");
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

            } else{
                AlertDialog.Builder builder = new AlertDialog.Builder(WeightCalendarActivity.this);
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

    private void getWeightInfo(String date) {
        Intent intent = new Intent(this, WeightInfoActivity.class);
        intent.putExtra("data", reg_data.get(date).toString());
        intent.putExtra("regDate", date);
        startActivity(intent);
        overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
    }

    private void addWeight(String date) {
        Intent intent = new Intent(this, WeightInfoActivity.class);
        intent.putExtra("regDate", date);
        if(lastWeight != null ){
            intent.putExtra("lastWeight", lastWeight);
        }
        startActivity(intent);
        overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
    }
}
