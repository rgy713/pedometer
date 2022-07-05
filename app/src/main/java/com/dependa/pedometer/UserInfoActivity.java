package com.dependa.pedometer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.dependa.pedometer.base.Constants;
import com.dependa.pedometer.base.DBStepData;
import com.dependa.pedometer.base.HttpPostRequest;
import com.dependa.pedometer.base.StepBaseWithMenu;
import com.dependa.pedometer.base.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class UserInfoActivity extends StepBaseWithMenu {
    private ChangeUserinfoTask mChangeUserinfoTask = null;
    private GetGroupInfoTask mGetGroupInfoTask = null;

    private HashMap<String, Object> userData;
    private AutoCompleteTextView mEmailView;
    private EditText mNameView;
    private TextView mBirthdayView;
    private EditText mHeightView;
    private EditText mWeightView;
    private Spinner mSelectHabitView;
    private ArrayAdapter<CharSequence> mAdapterHabit;
    private Spinner mSelectStepSizeView;
    private ArrayAdapter<CharSequence> mAdapterStepSize;
    private Spinner mSelectGenderView;
    private ArrayAdapter<CharSequence> mAdapterGender;
    private Button mChangeUserinfoButton;
    private View mProgressView;
    private View mUserinfoFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        TextView title = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.action_bar_title);
        title.setText(R.string.userinfo);

        SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
        String email = sp1.getString(Constants.SHARE_EMAIL, null);
        DBStepData dbStepData = new DBStepData(getApplicationContext());
        userData = dbStepData.getUserData(email);

        TextView mChangePasswordText = (TextView) findViewById(R.id.changepassword);
        mChangePasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserInfoActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
            }
        });

        TextView mGroupInfo = (TextView) findViewById(R.id.group_setting);
        mGroupInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getGroupInfo();
            }
        });

        TextView mEmailText = (TextView) findViewById(R.id.email);
        mEmailText.setText(email);

        mNameView = (EditText) findViewById(R.id.name);
        mNameView.setText(userData.get(Constants.FLD_name) == null ? null : userData.get(Constants.FLD_name).toString());

        mBirthdayView = (TextView) findViewById(R.id.birthday);
        mBirthdayView.setText(userData.get(Constants.FLD_birthday) == null ? null : userData.get(Constants.FLD_birthday).toString());

        Button btnDatePicker=(Button)findViewById(R.id.btn_date);
        btnDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);

                if(userData.get(Constants.FLD_birthday) != null && !userData.get(Constants.FLD_birthday).toString().isEmpty()  ){
                    String[] ymd = userData.get(Constants.FLD_birthday).toString().split(" ")[0].split("-");
                    mYear = Integer.parseInt(ymd[0]);
                    mMonth = Integer.parseInt(ymd[1]) - 1;
                    mDay = Integer.parseInt(ymd[2]);
                }

                DatePickerDialog datePickerDialog = new DatePickerDialog(UserInfoActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                String date = year  + "-" + ((monthOfYear + 1) < 10 ? "0" : "") + (monthOfYear + 1) + "-" + (dayOfMonth < 10 ? "0" : "") +  dayOfMonth;
                                mBirthdayView.setText(date);
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        mHeightView = (EditText) findViewById(R.id.height);
        mHeightView.setText(userData.get(Constants.FLD_height).toString());

        mWeightView = (EditText) findViewById(R.id.weight);
        mWeightView.setText(userData.get(Constants.FLD_weight) == null ? null : userData.get(Constants.FLD_weight).toString());

        mSelectHabitView = (Spinner) findViewById(R.id.select_habit);
        mAdapterHabit = ArrayAdapter.createFromResource(this, R.array.userHabit, android.R.layout.simple_spinner_item);
        mAdapterHabit.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSelectHabitView.setAdapter(mAdapterHabit);
        mSelectHabitView.setSelection((int)userData.get(Constants.FLD_habbit));

        mSelectStepSizeView = (Spinner) findViewById(R.id.select_step);
        mAdapterStepSize = ArrayAdapter.createFromResource(this, R.array.userStep, android.R.layout.simple_spinner_item);
        mAdapterStepSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSelectStepSizeView.setAdapter(mAdapterStepSize);
        mSelectStepSizeView.setSelection((int)userData.get(Constants.FLD_step_size));

        mSelectGenderView = (Spinner) findViewById(R.id.select_gender);
        mAdapterGender = ArrayAdapter.createFromResource(this, R.array.gender, android.R.layout.simple_spinner_item);
        mAdapterGender.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSelectGenderView.setAdapter(mAdapterGender);
        mSelectGenderView.setSelection((int)userData.get(Constants.FLD_gender));

        mProgressView = (View) findViewById(R.id.change_userinfo_progress);
        mUserinfoFormView = (View) findViewById(R.id.userinfo_form);

        mChangeUserinfoButton = (Button) findViewById(R.id.change_userinfo_btn);
        mChangeUserinfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptChangeUserinfo();
            }
        });

    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mUserinfoFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mUserinfoFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mUserinfoFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mUserinfoFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void attemptChangeUserinfo() {
        if (mChangeUserinfoTask != null) {
            return;
        }
        // Reset errors.
        mNameView.setError(null);
        mBirthdayView.setError(null);
        mHeightView.setError(null);
        mWeightView.setError(null);

        String email = userData.get(Constants.FLD_email).toString();
        String name = mNameView.getText() == null ? null : mNameView.getText().toString();
        String birthday = mBirthdayView.getText() == null ? null : mBirthdayView.getText().toString();
        String height = mHeightView.getText().toString();
        String weight = mWeightView.getText() == null ? null : mWeightView.getText().toString();
        int habit = mSelectHabitView.getSelectedItemPosition();
        int stepSize = mSelectStepSizeView.getSelectedItemPosition();
        int gender = mSelectGenderView.getSelectedItemPosition();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid height, if the user entered one.
        if (TextUtils.isEmpty(height) || !Utils.isHeightValid(height)) {
            mHeightView.setError(getString(R.string.error_field_required));
            focusView = mHeightView;
            cancel = true;
        }

        // Check for a valid name, if the user entered one.
        if (TextUtils.isEmpty(name) || !Utils.isNameValid(name)) {
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        }

        // Check for a valid birthday, if the user entered one.
        if (TextUtils.isEmpty(birthday) || !Utils.isBirthdayValid(birthday)) {
            mBirthdayView.setError(getString(R.string.error_field_required));
            focusView = mBirthdayView;
            cancel = true;
        }

        // Check for a valid height, if the user entered one.
        if (TextUtils.isEmpty(weight) || !Utils.isWeightValid(weight)) {
            mWeightView.setError(getString(R.string.error_field_required));
            focusView = mWeightView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mChangeUserinfoTask = new UserInfoActivity.ChangeUserinfoTask(email, name,gender, birthday, height, weight, habit, stepSize);
            mChangeUserinfoTask.execute((Void) null);
        }
    }

    private class ChangeUserinfoTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mName;
        private final String mBirthday;
        private final String mHeight;
        private final String mWeight;
        private final int mHabit;
        private final int mStepSize;
        private final int mGender;

        private String mErrorMsg;

        ChangeUserinfoTask(String mEmail, String mName,int mGender, String mBirthday, String mHeight, String mWeight, int mHabit, int mStepSize) {
            this.mEmail = mEmail;
            this.mName = mName;
            this.mBirthday = mBirthday;
            this.mHeight = mHeight;
            this.mWeight = mWeight;
            this.mHabit = mHabit;
            this.mStepSize = mStepSize;
            this.mGender = mGender;
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
                    if(!TextUtils.isEmpty(mName))
                        postParams.put("name", mName);
                    if(!TextUtils.isEmpty(mBirthday))
                        postParams.put("birthday", mBirthday);
                    postParams.put("height", Double.parseDouble(mHeight));
                    if(!TextUtils.isEmpty(mWeight))
                        postParams.put("weight", Double.parseDouble(mWeight));
                    postParams.put("habbit", mHabit);
                    postParams.put("step_size", mStepSize);
                    postParams.put("gender", mGender);

                    HttpPostRequest httpPostRequest = new HttpPostRequest();
                    result = httpPostRequest.POST(Constants.CHANGEUSERINFO_URL, postParams);

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
            mChangeUserinfoTask = null;
            showProgress(false);
            AlertDialog.Builder builder = new AlertDialog.Builder(UserInfoActivity.this);
            Integer message;
            if (success) {
                DBStepData dbStepData = new DBStepData(getApplicationContext());
                Date currentTime = Calendar.getInstance().getTime();
                dbStepData.insertUserData(mEmail, userData.get("password").toString(), mName, mGender, mBirthday, Double.parseDouble(mHeight), Double.parseDouble(mWeight), mHabit, mStepSize, currentTime.toString());
                message = R.string.save_success;
            } else {
                switch (mErrorMsg){
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
                            //do things
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }

        @Override
        protected void onCancelled() {
            mChangeUserinfoTask = null;
            showProgress(false);
        }
    }

    private void getGroupInfo() {
        if (mGetGroupInfoTask != null) {
            return;
        }

        SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
        String email = sp1.getString(Constants.SHARE_EMAIL, null);

        mGetGroupInfoTask = new GetGroupInfoTask(email);
        mGetGroupInfoTask.execute((Void) null);
    }

    private class GetGroupInfoTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private String mErrorMsg;
        private String mData;

        GetGroupInfoTask(String email) {
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
                    result = httpPostRequest.GET(Constants.GET_GROUP_LIST, getParams);

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
            mGetGroupInfoTask = null;

            if (success) {
                Intent intent = new Intent(UserInfoActivity.this, GroupActivity.class);
                //TODO
                intent.putExtra("data", mData);
                startActivity(intent);
                overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
            } else{
                AlertDialog.Builder builder = new AlertDialog.Builder(UserInfoActivity.this);
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
            mGetGroupInfoTask = null;
        }
    }
}
