package com.dependa.pedometer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dependa.pedometer.base.BackPressCloseHandler;
import com.dependa.pedometer.base.Constants;
import com.dependa.pedometer.base.DBStepData;
import com.dependa.pedometer.base.HttpPostRequest;
import com.dependa.pedometer.base.StepBase;
import com.dependa.pedometer.base.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * A login screen that offers login via uid/password.
 */
public class LoginActivity extends StepBase {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private BackPressCloseHandler backPressCloseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);


        Button mUidSignInButton = (Button) findViewById(R.id.uid_sign_in_button);
        mUidSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        TextView mForgetPwdText = (TextView) findViewById(R.id.forgetpwd);
        mForgetPwdText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ForgetPwdActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
            }
        });

        TextView mRegistrationText = (TextView) findViewById(R.id.registration);
        mRegistrationText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
            }
        });
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
        String email = sp1.getString(Constants.SHARE_EMAIL, null);
        String pwd = sp1.getString(Constants.SHARE_PWD, null);

        if(email != null)
            mEmailView.setText(email);
        if(pwd != null)
            mPasswordView.setText(pwd);

        if (email != null && pwd != null) {
            if (!email.equals("") && !pwd.equals("")) {
                attemptLogin();
            }
        }

        backPressCloseHandler = new BackPressCloseHandler(this);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }else if (!Utils.isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

            SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
            String u_email = sp1.getString(Constants.SHARE_EMAIL, null);

            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
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

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    private class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private String mErrorMsg;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
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
            // TODO: attempt authentication against a network service.
            if (isConnected()) {
                ContentValues postParams = new ContentValues();
                JSONObject result = null;
                try {
                    postParams.put("email", mEmail);
                    postParams.put("password", mPassword);

                    HttpPostRequest httpPostRequest = new HttpPostRequest();
                    result = httpPostRequest.POST(Constants.LOGIN_URL, postParams);

                    if (result == null) return false;
                    if (!result.getString("type").equals("success")) {
                        mErrorMsg = result.getString("content");
                        return false;
                    }
                    //TODO
                    JSONObject content = result.getJSONObject("content");
                    DBStepData dbStepData = new DBStepData(getApplicationContext());
                    Date currentTime = Calendar.getInstance().getTime();
                    String name = null;
                    String birthday = null;
                    Double weight = null;
                    try{
                        name = content.get("name") == null ? null : content.getString("name");

                        birthday = content.get("birthday") == null ? null : content.getString("birthday");

                        weight = content.get("weight") == null ? null : content.getDouble("weight");
                    }catch (JSONException e){

                    }

                    dbStepData.insertUserData(mEmail, mPassword, name, content.getInt("gender"), birthday, content.getDouble("height"), weight, content.getInt("habbit"), content.getInt("step_size"), currentTime.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                DBStepData dbStepData = new DBStepData(getApplicationContext());
                HashMap<String, Object> userData = dbStepData.getUserData(mEmail);

                String userPassword = "";
                userPassword = (userData.get(Constants.FLD_password)).toString();

                if (!userPassword.equals(mPassword)) return false;
            }
            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                SharedPreferences sp = getSharedPreferences(Constants.SHARE_PREF, 0);
                SharedPreferences.Editor Ed = sp.edit();
                Ed.putString(Constants.SHARE_EMAIL, mEmail);
                Ed.putString(Constants.SHARE_PWD, mPassword);
                Ed.apply();

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
                finish();
            } else{
                showProgress(false);
                if(mErrorMsg == null){
                    Toast.makeText(getApplicationContext(), "ログインに失敗しました。", Toast.LENGTH_SHORT).show();
                    return;
                }
                switch (mErrorMsg){
                    case "ERR_INVALID_PASSWORD":
                        mPasswordView.setError(getString(R.string.error_incorrect_password));
                        mPasswordView.requestFocus();
                        break;
                    case "ERR_NO_USER":
                        mEmailView.setError(getString(R.string.error_invalid_email));
                        mEmailView.requestFocus();
                        break;
                    default:
                        mEmailView.setError(getString(R.string.error_invalid_email));
                        mEmailView.requestFocus();
                }
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }
}

