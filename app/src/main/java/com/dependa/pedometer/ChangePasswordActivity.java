package com.dependa.pedometer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dependa.pedometer.base.Constants;
import com.dependa.pedometer.base.HttpPostRequest;
import com.dependa.pedometer.base.StepBase;

import org.json.JSONObject;

public class ChangePasswordActivity extends StepBase {
    private ChangePasswordTask mChangePasswordTask = null;

    private EditText mOldPasswordView;
    private EditText mNewPasswordView;
    private EditText mConfirmPasswordView;
    private View mProgressView;
    private View mChangePasswordFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        TextView title = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.action_bar_title);
        title.setText(R.string.change_password);

        mOldPasswordView = (EditText) findViewById(R.id.oldpassword);
        mNewPasswordView = (EditText) findViewById(R.id.newpassword);
        mConfirmPasswordView = (EditText) findViewById(R.id.confirmPassword);
        Button mChangePasswordBtn = (Button) findViewById(R.id.change_password_btn);
        mChangePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptChangePassword();
            }
        });

        mChangePasswordFormView = findViewById(R.id.change_password_form);
        mProgressView = findViewById(R.id.change_password_progress);
    }

    private void attemptChangePassword() {
        if (mChangePasswordTask != null) {
            return;
        }

        // Reset errors.
        mOldPasswordView.setError(null);
        mNewPasswordView.setError(null);
        mConfirmPasswordView.setError(null);

        // Store values at the time of the login attempt.
        SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
        String email = sp1.getString(Constants.SHARE_EMAIL, null);
        String oldPassword = mOldPasswordView.getText().toString();
        String newPassword = mNewPasswordView.getText().toString();
        String confirmPassword = mConfirmPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(oldPassword)) {
            mOldPasswordView.setError(getString(R.string.error_incorrect_password));
            focusView = mOldPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(newPassword)) {
            mNewPasswordView.setError(getString(R.string.error_incorrect_password));
            focusView = mNewPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            mConfirmPasswordView.setError(getString(R.string.error_incorrect_password));
            focusView = mConfirmPasswordView;
            cancel = true;
        } else if (!confirmPassword.equals(newPassword)) {
            mNewPasswordView.setError(getString(R.string.error_incorrect_password));
            focusView = mNewPasswordView;
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
            mChangePasswordTask = new ChangePasswordTask(email, oldPassword, newPassword);
            mChangePasswordTask.execute((Void) null);
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

            mChangePasswordFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mChangePasswordFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mChangePasswordFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mChangePasswordFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private class ChangePasswordTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mOldPassword;
        private final String mNewPassword;

        ChangePasswordTask(String email, String oldPassword, String newPassword) {
            mEmail = email;
            mOldPassword = oldPassword;
            mNewPassword = newPassword;
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
                    postParams.put("cur_password", mOldPassword);
                    postParams.put("new_password", mNewPassword);

                    HttpPostRequest httpPostRequest = new HttpPostRequest();
                    result = httpPostRequest.POST(Constants.CHANGEPASSWORD_URL, postParams);

                    if (result == null) return false;
                    if (!result.getString("type").equals("success")) {
                        return false;
                    }
                    //TODO


                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                return false;
            }
            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mChangePasswordTask = null;
            showProgress(false);

            if (success) {
                SharedPreferences sp = getSharedPreferences(Constants.SHARE_PREF, 0);
                SharedPreferences.Editor Ed = sp.edit();
                Ed.putString(Constants.SHARE_EMAIL, mEmail);
                Ed.putString(Constants.SHARE_PWD, "");
                Ed.apply();

                Intent intent = new Intent(ChangePasswordActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
                finish();
            } else {
                mOldPasswordView.setError(getString(R.string.error_incorrect_password));
                mOldPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mChangePasswordTask = null;
            showProgress(false);
        }
    }

}
