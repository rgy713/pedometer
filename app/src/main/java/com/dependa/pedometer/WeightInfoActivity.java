package com.dependa.pedometer;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dependa.pedometer.base.Constants;
import com.dependa.pedometer.base.HttpPostRequest;
import com.dependa.pedometer.base.StepBaseWithMenu;

import org.json.JSONException;
import org.json.JSONObject;

public class WeightInfoActivity extends StepBaseWithMenu {

    private UpdateWeightInfoTask mUpdateWeightInfoTask = null;
    private DeleteWeightInfoTask mDeleteWeightInfoTask = null;
    private JSONObject detailData = null;
    private Integer weightId = null;
    private String regDate = null;
    private TextView regDateView;
    private EditText weightView;
    private EditText targetWeightView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_info);

        TextView title = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.action_bar_title);
        title.setText(R.string.weight_habit);

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        if(bundle.getString("data") != null){
            try {
                detailData = new JSONObject(bundle.getString("data"));
                weightId = detailData.getInt("id");
            } catch (JSONException e) {
                e.printStackTrace();
                weightId = 0;
            }
        }else{
            weightId = 0;
        }

        regDate = bundle.getString("regDate");

        regDateView = (TextView) findViewById(R.id.reg_date);
        regDateView.setText(regDate);

        weightView = (EditText) findViewById(R.id.weight);
        targetWeightView = (EditText) findViewById(R.id.target_weight);

        Button deleteBtn = (Button) findViewById(R.id.delete_btn);

        Double weight = null;
        Double targetWeight = null;

        if (weightId == 0) {
            deleteBtn.setVisibility(View.GONE);
            try {
                targetWeight = bundle.getDouble("lastWeight");
            }
            catch (Exception e){
                e.printStackTrace();
            }

            if(targetWeight != null){
                targetWeightView.setText(String.format("%.2f", targetWeight));
            }
        } else {
            try {
                weight = detailData.getDouble("weight");
                targetWeight = detailData.getDouble("target_weight");
            }
            catch (JSONException e){
                e.printStackTrace();
            }

            if( weight != null)
                weightView.setText(String.format("%.2f", weight));
            if(targetWeight != null)
                targetWeightView.setText(String.format("%.2f", targetWeight));

            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(WeightInfoActivity.this);
                    builder.setMessage(R.string.are_you_delete)
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    deleteWeightInfo(weightId);
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

        Button applyBtn = (Button) findViewById(R.id.apply_btn);

        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Double weight = null;
                Double targetWeight = null;

                if( weightView.getText().toString().isEmpty() || targetWeightView.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(), "体重を入力してください。", Toast.LENGTH_SHORT).show();
                }
                else {
                    weight = Double.parseDouble(String.valueOf(weightView.getText()));
                    targetWeight = Double.parseDouble(String.valueOf(targetWeightView.getText()));
                    updateWeightInfo(weightId, (String) regDateView.getText(), weight, targetWeight);
                }
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

                DatePickerDialog datePickerDialog = new DatePickerDialog(WeightInfoActivity.this,
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

    private void updateWeightInfo(Integer id, String date, Double weight, Double targetWeight) {
        if (mUpdateWeightInfoTask != null) {
            return;
        }

        SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
        String email = sp1.getString(Constants.SHARE_EMAIL, null);

        mUpdateWeightInfoTask = new UpdateWeightInfoTask(email, id, date, weight, targetWeight);
        mUpdateWeightInfoTask.execute((Void) null);
    }

    private class UpdateWeightInfoTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final Integer mId;
        private final String mRegDate;
        private final Double mWeight;
        private final Double mTargetWeight;

        private String mErrorMsg;

        UpdateWeightInfoTask(String mEmail, Integer mId, String mRegDate, Double mWeight, Double mTargetWeight) {
            this.mEmail = mEmail;
            this.mId = mId;
            this.mRegDate = mRegDate;
            this.mWeight = mWeight;
            this.mTargetWeight = mTargetWeight;
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
                    postParams.put("weight", mWeight);
                    postParams.put("target_weight", mTargetWeight);

                    HttpPostRequest httpPostRequest = new HttpPostRequest();

                    if (mId > 0) {
                        postParams.put("id", mId);
                        result = httpPostRequest.POST(Constants.POST_WEIGHT_UPDATE, postParams);
                    } else {
                        result = httpPostRequest.POST(Constants.POST_WEIGHT_ADD, postParams);
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
            mUpdateWeightInfoTask = null;
            AlertDialog.Builder builder = new AlertDialog.Builder(WeightInfoActivity.this);
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
            mUpdateWeightInfoTask = null;
        }
    }

    private void deleteWeightInfo(Integer id) {
        if (mDeleteWeightInfoTask != null) {
            return;
        }

        SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
        String email = sp1.getString(Constants.SHARE_EMAIL, null);

        mDeleteWeightInfoTask = new DeleteWeightInfoTask(email, id);
        mDeleteWeightInfoTask.execute((Void) null);
    }

    private class DeleteWeightInfoTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final Integer mId;

        private String mErrorMsg;

        DeleteWeightInfoTask(String mEmail, Integer mId) {
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
                    result = httpPostRequest.POST(Constants.POST_WEIGHT_DELETE, postParams);

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
            mUpdateWeightInfoTask = null;
            AlertDialog.Builder builder = new AlertDialog.Builder(WeightInfoActivity.this);
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
            mUpdateWeightInfoTask = null;
        }
    }
}
