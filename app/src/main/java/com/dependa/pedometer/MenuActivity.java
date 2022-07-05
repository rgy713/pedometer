package com.dependa.pedometer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dependa.pedometer.base.Constants;
import com.dependa.pedometer.base.StepBaseWithMenu;

public class MenuActivity extends StepBaseWithMenu {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        TextView title = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.action_bar_title);
        title.setText(R.string.health_habit);

        LinearLayout goMeal = findViewById(R.id.goto_meal);

        goMeal.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i;
                    i = new Intent(MenuActivity.this, MealCalendarActivity.class);
                    startActivity(i);
                    overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
                }
            }
        );

        LinearLayout goSleep = findViewById(R.id.goto_sleep);
        goSleep.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i;
                    i = new Intent(MenuActivity.this, SleepCalendarActivity.class);
                    startActivity(i);
                    overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
                }
            }
        );

        LinearLayout goWeight = findViewById(R.id.goto_weight);
        goWeight.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i;
                    i = new Intent(MenuActivity.this, WeightCalendarActivity.class);
                    startActivity(i);
                    overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
                }
            }
        );

        LinearLayout goScore = findViewById(R.id.goto_score);
        goScore.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i;
                        i = new Intent(MenuActivity.this, ScoreActivity.class);
                        startActivity(i);
                        overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
                    }
                }
        );


        LinearLayout goUser = findViewById(R.id.goto_user);
        goUser.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i;
                        i = new Intent(MenuActivity.this, UserInfoActivity.class);
                        startActivity(i);
                        overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
                    }
                }
        );

        LinearLayout goLogout = findViewById(R.id.goto_logout);
        goLogout.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i;
                        i = new Intent(MenuActivity.this, LoginActivity.class);
                        startActivity(i);
                        overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);

                        SharedPreferences sp = getSharedPreferences(Constants.SHARE_PREF, 0);
                        SharedPreferences.Editor Ed = sp.edit();
                        Ed.putString(Constants.SHARE_PWD, "");
                        Ed.apply();
                    }
                }
        );
    }

    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
    }
}
