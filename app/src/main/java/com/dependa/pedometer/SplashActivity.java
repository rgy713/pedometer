package com.dependa.pedometer;

import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;

import com.dependa.pedometer.base.StepBase;

public class SplashActivity extends StepBase {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                Intent i = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
                finish();
            }
        }, 2000);

    }
}

