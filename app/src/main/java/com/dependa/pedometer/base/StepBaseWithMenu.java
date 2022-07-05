package com.dependa.pedometer.base;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.dependa.pedometer.LoginActivity;
import com.dependa.pedometer.MainActivity;
import com.dependa.pedometer.MealCalendarActivity;
import com.dependa.pedometer.R;
import com.dependa.pedometer.SleepCalendarActivity;
import com.dependa.pedometer.UserInfoActivity;

import java.io.File;

/**
 * Created by RGY on 10/5/2017.
 */

public class StepBaseWithMenu extends StepBase {

    /*public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        //ActionBar
        int id = item.getItemId();
        Intent i;
        switch (id) {
            case R.id.meal_habit:
                if(this instanceof MealCalendarActivity)
                    break;
                i = new Intent(StepBaseWithMenu.this, MealCalendarActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
                break;
            case R.id.sleep_habit:
                if(this instanceof SleepCalendarActivity)
                    break;
                i = new Intent(StepBaseWithMenu.this, SleepCalendarActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
                break;
            case R.id.userinfo:
                if(this instanceof UserInfoActivity)
                    break;
                i = new Intent(StepBaseWithMenu.this, UserInfoActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
                break;
            case R.id.logout:
                i = new Intent(StepBaseWithMenu.this, LoginActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);

                SharedPreferences sp = getSharedPreferences(Constants.SHARE_PREF, 0);
                SharedPreferences.Editor Ed = sp.edit();
                Ed.putString(Constants.SHARE_PWD, "");
                Ed.apply();
                break;
        }
        return super.onOptionsItemSelected(item);
    }*/
}
