package com.dependa.pedometer;

import android.app.Activity;
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
import android.widget.ListView;
import android.widget.TextView;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.dependa.pedometer.adapter.MealInfoAdapter;
import com.dependa.pedometer.base.Constants;
import com.dependa.pedometer.base.HttpPostRequest;
import com.dependa.pedometer.base.StepBaseWithMenu;
import com.dependa.pedometer.model.MealInfoModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class MealInfoActivity extends StepBaseWithMenu {

    private UpdateMealInfoTask mUpdateMealInfoTask = null;
    private DeleteMealInfoTask mDeleteMealInfoTask = null;
    private JSONArray detailData = null;
    private Integer mealId = null;
    private String regDate = null;
    private TextView regDateView;
    private ListView listView;
    MealInfoAdapter mealInfoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_info);

        TextView title = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.action_bar_title);
        title.setText(R.string.meal_habit);

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        try {
            detailData = new JSONArray(bundle.getString("data"));
        } catch (JSONException e) {
            e.printStackTrace();
            finish();
        }

        mealId = bundle.getInt("id");
        regDate = bundle.getString("regDate");

        regDateView = (TextView) findViewById(R.id.reg_date);
        regDateView.setText(regDate);
        listView = (ListView) findViewById(R.id.list_view);

        Button deleteBtn = (Button) findViewById(R.id.delete_btn);

        ArrayList<MealInfoModel> mList = new ArrayList<MealInfoModel>();

        if (mealId == 0) {
            deleteBtn.setVisibility(View.GONE);
            for (int i = 0; i < detailData.length(); i++) {
                JSONObject one = null;
                try {
                    one = detailData.getJSONObject(i);
                    MealInfoModel info = new MealInfoModel(null, one.getString("name"), one.getInt("id"), false, false, false);
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
                    MealInfoModel info = new MealInfoModel(one.getInt("id"), one.getString("food_name"), one.getInt("food_data_id"), one.getBoolean("breakfast"), one.getBoolean("lunch"), one.getBoolean("dinner"));
                    mList.add(info);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MealInfoActivity.this);
                    builder.setMessage(R.string.are_you_delete)
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    deleteMealInfo(mealId);
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

        mealInfoAdapter = new MealInfoAdapter(this, mList);
        listView.setAdapter(mealInfoAdapter);

        Button applyBtn = (Button) findViewById(R.id.apply_btn);

        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<MealInfoModel> data = mealInfoAdapter.getAllData();
                updateMealInfo(mealId, (String) regDateView.getText(), data);
            }
        });

        Button btnDatePicker=(Button)findViewById(R.id.btn_date);
        btnDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] ymd = regDate.split(" ")[0].split("-");
                int mYear = Integer.parseInt(ymd[0]);
                int mMonth = Integer.parseInt(ymd[1]) - 1;
                int mDay = Integer.parseInt(ymd[2]);

                DatePickerDialog datePickerDialog = new DatePickerDialog(MealInfoActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                String date = year  + "-" + ((monthOfYear + 1) < 10 ? "0" : "") + (monthOfYear + 1) + "-" + (dayOfMonth < 10 ? "0" : "") +  dayOfMonth;
                                regDateView.setText(date);
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });
    }

    private void updateMealInfo(Integer id, String date, ArrayList<MealInfoModel> mealInfoModels) {
        if (mUpdateMealInfoTask != null) {
            return;
        }

        SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
        String email = sp1.getString(Constants.SHARE_EMAIL, null);

        mUpdateMealInfoTask = new UpdateMealInfoTask(email, id, date, mealInfoModels);
        mUpdateMealInfoTask.execute((Void) null);
    }

    private class UpdateMealInfoTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final Integer mId;
        private final String mRegDate;
        private final ArrayList<MealInfoModel> mMealInfoModels;

        private String mErrorMsg;

        UpdateMealInfoTask(String mEmail, Integer mId, String mRegDate, ArrayList<MealInfoModel> mealInfoModels) {
            this.mEmail = mEmail;
            this.mId = mId;
            this.mRegDate = mRegDate;
            this.mMealInfoModels = mealInfoModels;
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
                    postParams.put("reg_date", mRegDate);

                    for (MealInfoModel one : mMealInfoModels) {
                        if (mId > 0)
                            postParams.put("meal_info_id[" + one.getFoodDataId() + "]", one.getId());
                        postParams.put("breakfast[" + one.getFoodDataId() + "]", one.getBreakfast() ? 1 : 0);
                        postParams.put("lunch[" + one.getFoodDataId() + "]", one.getLunch() ? 1 : 0);
                        postParams.put("dinner[" + one.getFoodDataId() + "]", one.getDinner() ? 1 : 0);
                    }
                    HttpPostRequest httpPostRequest = new HttpPostRequest();

                    if (mId > 0) {
                        postParams.put("id", mId);
                        result = httpPostRequest.POST(Constants.POST_MEAL_UPDATE, postParams);
                    } else {
                        result = httpPostRequest.POST(Constants.POST_MEAL_CREATE, postParams);
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
            mUpdateMealInfoTask = null;
            AlertDialog.Builder builder = new AlertDialog.Builder(MealInfoActivity.this);
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
            mUpdateMealInfoTask = null;
        }
    }

    private void deleteMealInfo(Integer id) {
        if (mDeleteMealInfoTask != null) {
            return;
        }

        SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
        String email = sp1.getString(Constants.SHARE_EMAIL, null);

        mDeleteMealInfoTask = new DeleteMealInfoTask(email, id);
        mDeleteMealInfoTask.execute((Void) null);
    }

    private class DeleteMealInfoTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final Integer mId;

        private String mErrorMsg;

        DeleteMealInfoTask(String mEmail, Integer mId) {
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
                    result = httpPostRequest.POST(Constants.POST_MEAL_DELETE, postParams);

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
            mUpdateMealInfoTask = null;
            AlertDialog.Builder builder = new AlertDialog.Builder(MealInfoActivity.this);
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
            mUpdateMealInfoTask = null;
        }
    }
}
