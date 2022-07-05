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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import com.dependa.pedometer.base.Constants;
import com.dependa.pedometer.base.HttpPostRequest;
import com.dependa.pedometer.base.StepBase;
import com.dependa.pedometer.base.Utils;

import org.json.JSONObject;

import java.util.List;

public class CheckVerificationActivity extends StepBase {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private CheckVerificationActivity.CheckVerifyTask mCheckVerifyTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_verification);

        TextView title = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.action_bar_title);
        title.setText(R.string.check_verification);

        Button mResetGoButton = (Button) findViewById(R.id.reset_go_button);
        mResetGoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptCheckVerification();
            }
        });

    }

    private void attemptCheckVerification() {
        if (mCheckVerifyTask != null) {
            return;
        }

        SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
        String email = sp1.getString(Constants.SHARE_EMAIL, null);
        mCheckVerifyTask = new CheckVerificationActivity.CheckVerifyTask(email);
        mCheckVerifyTask.execute((Void) null);
    }

    private class CheckVerifyTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;

        CheckVerifyTask(String email) {
            mEmail = email;
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
                    HttpPostRequest httpPostRequest = new HttpPostRequest();
                    result = httpPostRequest.POST(Constants.CHECK_VERIFY, postParams);

                    if (result == null) return false;
                    if (!result.getString("type").equals("success")) {
                        return false;
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
            mCheckVerifyTask = null;

            if (success) {
                Intent intent = new Intent(CheckVerificationActivity.this, PasswordResetActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
                finish();
            } else{
                AlertDialog.Builder builder = new AlertDialog.Builder(CheckVerificationActivity.this);
                builder.setMessage(R.string.check_mail_msg)
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
            mCheckVerifyTask = null;
        }
    }
}
