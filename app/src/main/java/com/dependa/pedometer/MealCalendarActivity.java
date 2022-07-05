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

public class MealCalendarActivity extends StepBaseWithMenu implements CalendarListener {

    private GetCalendarDataTask mGetMainDataTask = null;
    private GetMealInfoTask mGetMealInfoTask = null;
    private GetFoodInfoTask mGetFoodInfoTask = null;

    private TextView regDate;
    private HashMap<String, Integer> reg_data;
    private CustomCalendarView mealCalendar;
    private String yearMonth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_calendar);

        TextView title = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.action_bar_title);
        title.setText(R.string.meal_habit);

        regDate = (TextView) findViewById(R.id.reg_date);

        Button mealViewBtn = (Button) findViewById(R.id.meal_view_btn);

        mealViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(regDate.getText()).equals("")) {
                    String selectedDate = regDate.getText().toString();
                    if(reg_data.get(selectedDate)== null){
                        Toast.makeText(getApplicationContext(), "登録されたデータがありません。", Toast.LENGTH_SHORT).show();
                    }
                    else
                        getMealInfo(selectedDate);
                } else {
                    Toast.makeText(getApplicationContext(), "日付を選択してください。", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button mealAddBtn = (Button) findViewById(R.id.meal_add_btn);

        mealAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(regDate.getText()).equals("")) {
                    String selectedDate = regDate.getText().toString();
                    if(reg_data.get(selectedDate)!= null){
                        Toast.makeText(getApplicationContext(), "登録されたデータがあります。", Toast.LENGTH_SHORT).show();
                    }
                    else
                        getFoodInfo(selectedDate);
                } else {
                    Toast.makeText(getApplicationContext(), "日付を選択してください。", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Initialize CustomCalendarView from layout
        mealCalendar = (CustomCalendarView) findViewById(R.id.meal_calendar);
        //Show Monday as first date of week
        mealCalendar.setFirstDayOfWeek(Calendar.SUNDAY);
        //Show/hide overflow reg_data of a month
        mealCalendar.setShowOverflowDate(false);

        //Handling custom calendar events
        mealCalendar.setCalendarListener(new CalendarListener() {
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

        mealCalendar.refreshCalendar(currentCalendar);

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

        decorators.add(new MealCalendarActivity.DisabledColorDecorator());

        mealCalendar.setDecorators(decorators);

//call refreshCalendar to update calendar the view
        mealCalendar.refreshCalendar(currentCalendar);

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
                    result = httpPostRequest.GET(Constants.GET_MEAL_DATA, getParams);

                    if (result == null) return false;
                    if (!result.getString("type").equals("success")) {
                        mErrorMsg = result.getString("content");
                        return false;
                    }
                    //TODO
                    reg_data = new HashMap<String, Integer>();
                    try {
                        JSONArray jArray = result.getJSONArray("content");

                        for (int i=0;i<jArray.length();i++){
                            JSONObject one = jArray.getJSONObject(i);
                            reg_data.put(one.getString("reg_date"),one.getInt("id"));
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

            } else{
                AlertDialog.Builder builder = new AlertDialog.Builder(MealCalendarActivity.this);
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

    private void getMealInfo(String date) {
        if (mGetMealInfoTask != null) {
            return;
        }

        SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
        String email = sp1.getString(Constants.SHARE_EMAIL, null);

        mGetMealInfoTask = new GetMealInfoTask(date);
        mGetMealInfoTask.execute((Void) null);
    }

    private class GetMealInfoTask extends AsyncTask<Void, Void, Boolean> {

        private final String mDate;
        private String mErrorMsg;
        private String mData;

        GetMealInfoTask(String date) {
            this.mDate = date;
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
                    getParams.put("id", reg_data.get(mDate));

                    HttpPostRequest httpPostRequest = new HttpPostRequest();
                    result = httpPostRequest.GET(Constants.GET_MEAL_INFO, getParams);

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
            mGetMealInfoTask = null;

            if (success) {
                Intent intent = new Intent(MealCalendarActivity.this, MealInfoActivity.class);
                //TODO
                intent.putExtra("data", mData);
                intent.putExtra("id", reg_data.get(mDate));
                intent.putExtra("regDate", mDate);
                startActivity(intent);
                overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
            } else{
                AlertDialog.Builder builder = new AlertDialog.Builder(MealCalendarActivity.this);
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
            mGetMealInfoTask = null;
        }
    }

    private void getFoodInfo(String date) {
        if (mGetFoodInfoTask != null) {
            return;
        }

        mGetFoodInfoTask = new GetFoodInfoTask(date);
        mGetFoodInfoTask.execute((Void) null);
    }

    private class GetFoodInfoTask extends AsyncTask<Void, Void, Boolean> {

        private final String mDate;
        private String mErrorMsg;
        private String mData;

        GetFoodInfoTask(String date) {
            this.mDate = date;
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
                    result = httpPostRequest.GET(Constants.GET_FOOD_LIST, getParams);

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
            mGetFoodInfoTask = null;

            if (success) {
                Intent intent = new Intent(MealCalendarActivity.this, MealInfoActivity.class);
                //TODO
                intent.putExtra("data", mData);
                intent.putExtra("regDate", mDate);
                startActivity(intent);
                overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
            } else{
                AlertDialog.Builder builder = new AlertDialog.Builder(MealCalendarActivity.this);
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
            mGetFoodInfoTask = null;
        }
    }

}
