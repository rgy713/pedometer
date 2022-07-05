package com.dependa.pedometer;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.app.TimePickerDialog;
import android.widget.TimePicker;

import com.dependa.pedometer.adapter.SleepInfoAdapter;
import com.dependa.pedometer.base.Constants;
import com.dependa.pedometer.base.HttpPostRequest;
import com.dependa.pedometer.base.StepBaseWithMenu;
import com.dependa.pedometer.base.Utils;
import com.dependa.pedometer.model.SleepInfoModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;

public class SleepInfoActivity extends StepBaseWithMenu {

    private UpdateSleepInfoTask mUpdateSleepInfoTask = null;
    private DeleteSleepInfoTask mDeleteSleepInfoTask = null;
    private JSONArray detailData = null;
    private Integer sleepId = null;
    private String bedTime = null;
    private String wakeupTime = null;
    private TextView bedTimeView;
    private TextView wakeupTimeView;
    private TextView sleepTimeView;
    private ListView listView;
    SleepInfoAdapter SleepInfoAdapter;
    private String tmpDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_info);

        TextView title = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.action_bar_title);
        title.setText(R.string.sleep_habit);

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        try {
            detailData = new JSONArray(bundle.getString("data"));
        } catch (JSONException e) {
            e.printStackTrace();
            finish();
        }

        sleepId = bundle.getInt("id");
        if (sleepId>0){
            try {
                bedTime = Utils.toLocalTime(bundle.getString("bedTime"), Constants.DATE_FORMAT_Z).replace("T", " ").replace(":00Z","");
                wakeupTime = Utils.toLocalTime(bundle.getString("wakeupTime"), Constants.DATE_FORMAT_Z).replace("T", " ").replace(":00Z","");
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        else{
            bedTime = Utils.getNowTime(Constants.DATE_FORMAT_S);
            wakeupTime = bedTime;
        }


        bedTimeView = (TextView) findViewById(R.id.bed_time);
        bedTimeView.setText(bedTime);
        wakeupTimeView = (TextView) findViewById(R.id.wakeup_time);
        wakeupTimeView.setText(wakeupTime);
        sleepTimeView = (TextView) findViewById(R.id.sleep_time);
        try {
            sleepTimeView.setText(Utils.timeDiff(bedTime, wakeupTime, Constants.DATE_FORMAT_S));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        listView = (ListView) findViewById(R.id.list_view);

        Button deleteBtn = (Button) findViewById(R.id.delete_btn);

        ArrayList<SleepInfoModel> mList = new ArrayList<SleepInfoModel>();

        if (sleepId == 0) {
            deleteBtn.setVisibility(View.GONE);
            for (int i = 0; i < detailData.length(); i++) {
                JSONObject one = null;
                try {
                    one = detailData.getJSONObject(i);
                    SleepInfoModel info = new SleepInfoModel(null, one.getInt("id"), 3,  one.getString("name"), one.getString("level0"),one.getString("level1"),one.getString("level2"),one.getString("level3"));
                    mList.add(info);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        } else {
            for (int i = 0; i < detailData.length(); i++) {
                JSONObject one = null;
                try {
                    one = detailData.getJSONObject(i);
                    SleepInfoModel info = new SleepInfoModel(one.getInt("id"), one.getInt("sq_id"), one.getInt("level"),  one.getString("sq_name"), one.getString("level0"),one.getString("level1"),one.getString("level2"),one.getString("level3"));
                    mList.add(info);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SleepInfoActivity.this);
                    builder.setMessage(R.string.are_you_delete)
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    deleteSleepInfo(sleepId);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });;
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });
        }

        SleepInfoAdapter = new SleepInfoAdapter(this, mList);
        listView.setAdapter(SleepInfoAdapter);

        Button applyBtn = (Button) findViewById(R.id.apply_btn);

        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<SleepInfoModel> data = SleepInfoAdapter.getAllData();
                try {
                    if(checkTime())
                        updateSleepInfo(sleepId, (String) bedTimeView.getText(), (String) wakeupTimeView.getText(), data);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        Button btnBedDateTimePicker=(Button)findViewById(R.id.btn_bed_time);
        btnBedDateTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] ymd = bedTime.split(" ")[0].split("-");
                int mYear = Integer.parseInt(ymd[0]);
                int mMonth = Integer.parseInt(ymd[1]) - 1;
                int mDay = Integer.parseInt(ymd[2]);

                DatePickerDialog datePickerDialog = new DatePickerDialog(SleepInfoActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                tmpDate = year  + "-" + ((monthOfYear + 1) < 10 ? "0" : "") + (monthOfYear + 1) + "-" + (dayOfMonth < 10 ? "0" : "") +  dayOfMonth;

                                String[] hm = bedTime.split(" ")[1].split(":");
                                int mHour = Integer.parseInt(hm[0]);
                                int mMinute = Integer.parseInt(hm[1]);

                                // Launch Time Picker Dialog
                                TimePickerDialog timePickerDialog = new TimePickerDialog(SleepInfoActivity.this,
                                        new TimePickerDialog.OnTimeSetListener() {

                                            @Override
                                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                                  int minute) {
                                                bedTimeView.setText(tmpDate + " " + (hourOfDay < 10 ? "0" : "") + hourOfDay + ":" + (minute < 10 ? "0" : "") + minute);

                                                try {
                                                    sleepTimeView.setText(Utils.timeDiff((String)bedTimeView.getText(), (String)wakeupTimeView.getText(), Constants.DATE_FORMAT_S));
                                                } catch (ParseException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }, mHour, mMinute, false);
                                timePickerDialog.show();
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        Button btnWakeupDateTimePicker=(Button)findViewById(R.id.btn_wakeup_time);
        btnWakeupDateTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] ymd = wakeupTime.split(" ")[0].split("-");
                int mYear = Integer.parseInt(ymd[0]);
                int mMonth = Integer.parseInt(ymd[1]) - 1;
                int mDay = Integer.parseInt(ymd[2]);


                DatePickerDialog datePickerDialog = new DatePickerDialog(SleepInfoActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                tmpDate = year  + "-" + ((monthOfYear + 1) < 10 ? "0" : "") + (monthOfYear + 1) + "-" + (dayOfMonth < 10 ? "0" : "") +  dayOfMonth;

                                String[] hm = wakeupTime.split(" ")[1].split(":");
                                int mHour = Integer.parseInt(hm[0]);
                                int mMinute = Integer.parseInt(hm[1]);

                                // Launch Time Picker Dialog
                                TimePickerDialog timePickerDialog = new TimePickerDialog(SleepInfoActivity.this,
                                        new TimePickerDialog.OnTimeSetListener() {

                                            @Override
                                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                                  int minute) {
                                                wakeupTimeView.setText(tmpDate + " " + (hourOfDay < 10 ? "0" : "") + hourOfDay + ":" + (minute < 10 ? "0" : "") + minute);
                                                try {
                                                    sleepTimeView.setText(Utils.timeDiff((String)bedTimeView.getText(), (String)wakeupTimeView.getText(), Constants.DATE_FORMAT_S));
                                                } catch (ParseException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }, mHour, mMinute, false);
                                timePickerDialog.show();
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });
    }

    private boolean checkTime() throws ParseException {
        String bedTime = (String) bedTimeView.getText();
        String wakeupTime = (String) wakeupTimeView.getText();
        if(Utils.timeCompare(bedTime,wakeupTime,Constants.DATE_FORMAT_S)){
            return true;
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(SleepInfoActivity.this);
            builder.setMessage(R.string.invalid_time)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
            return false;
        }
    }

    private void updateSleepInfo(Integer id, String bedTime, String wakeupTime, ArrayList<SleepInfoModel> sleepInfoModels) {
        if (mUpdateSleepInfoTask != null) {
            return;
        }

        SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
        String email = sp1.getString(Constants.SHARE_EMAIL, null);

        mUpdateSleepInfoTask = new UpdateSleepInfoTask(email, id, bedTime, wakeupTime, sleepInfoModels);
        mUpdateSleepInfoTask.execute((Void) null);
    }

    private class UpdateSleepInfoTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final Integer mId;
        private final String mBedTime;
        private final String mWakeupTime;
        private final ArrayList<SleepInfoModel> mSleepInfoModels;

        private String mErrorMsg;

        UpdateSleepInfoTask(String mEmail, Integer mId, String mBedTime, String mWakeupTime, ArrayList<SleepInfoModel> sleepInfoModels) {
            this.mEmail = mEmail;
            this.mId = mId;
            this.mBedTime = mBedTime;
            this.mWakeupTime = mWakeupTime;
            this.mSleepInfoModels = sleepInfoModels;
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
                    postParams.put("email", mEmail);
                    postParams.put("bed_time", Utils.toUTC(mBedTime, Constants.DATE_FORMAT_S));
                    postParams.put("wakeup_time", Utils.toUTC(mWakeupTime, Constants.DATE_FORMAT_S));

                    for (SleepInfoModel one : mSleepInfoModels) {
                        if(mId>0)
                            postParams.put("sleep_info_id[" + one.getSqId() + "]", one.getId());
                        postParams.put("level[" + one.getSqId() + "]", one.getLevel());
                    }

                    HttpPostRequest httpPostRequest = new HttpPostRequest();

                    if (mId > 0) {
                        postParams.put("id", mId);
                        result = httpPostRequest.POST(Constants.POST_SLEEP_UPDATE, postParams);
                    } else {
                        result = httpPostRequest.POST(Constants.POST_SLEEP_CREATE, postParams);
                    }

                    if (result == null) return false;
                    if (!result.getString("type").equals("success")) {
                        mErrorMsg = result.getString("content");
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                mErrorMsg = Constants.CONNECT_INTERNET;
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mUpdateSleepInfoTask = null;
            AlertDialog.Builder builder = new AlertDialog.Builder(SleepInfoActivity.this);
            Integer message;
            if (success) {
                message = R.string.save_success;
            } else {
                switch (mErrorMsg) {
                    case Constants.CONNECT_INTERNET:
                        message = R.string.connet_internet;
                        break;
                    default:
                        message = R.string.save_error;
                }
            }
            builder.setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (mId == 0)
                                finish();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }

        @Override
        protected void onCancelled() {
            mUpdateSleepInfoTask = null;
        }
    }

    private void deleteSleepInfo(Integer id) {
        if (mDeleteSleepInfoTask != null) {
            return;
        }

        SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
        String email = sp1.getString(Constants.SHARE_EMAIL, null);

        mDeleteSleepInfoTask = new DeleteSleepInfoTask(email, id);
        mDeleteSleepInfoTask.execute((Void) null);
    }

    private class DeleteSleepInfoTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final Integer mId;

        private String mErrorMsg;

        DeleteSleepInfoTask(String mEmail, Integer mId) {
            this.mEmail = mEmail;
            this.mId = mId;
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
                    postParams.put("email", mEmail);
                    postParams.put("id", mId);
                    HttpPostRequest httpPostRequest = new HttpPostRequest();
                    result = httpPostRequest.POST(Constants.POST_SLEEP_DELETE, postParams);

                    if (result == null) return false;
                    if (!result.getString("type").equals("success")) {
                        mErrorMsg = result.getString("content");
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                mErrorMsg = Constants.CONNECT_INTERNET;
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mUpdateSleepInfoTask = null;
            AlertDialog.Builder builder = new AlertDialog.Builder(SleepInfoActivity.this);
            Integer message;
            if (success) {
                message = R.string.delete_success;
            } else {
                switch (mErrorMsg) {
                    case Constants.CONNECT_INTERNET:
                        message = R.string.connet_internet;
                        break;
                    default:
                        message = R.string.delete_error;
                }
            }
            builder.setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }

        @Override
        protected void onCancelled() {
            mUpdateSleepInfoTask = null;
        }
    }
}
