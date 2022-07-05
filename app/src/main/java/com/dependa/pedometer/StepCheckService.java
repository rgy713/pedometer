package com.dependa.pedometer;

/**
 * Created by RGY on 9/14/2017.
 */

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.dependa.pedometer.base.CSVWriter;
import com.dependa.pedometer.base.Constants;
import com.dependa.pedometer.base.DBStepData;
import com.dependa.pedometer.base.StepValue;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import uk.me.berndporr.iirj.*;

public class StepCheckService extends Service implements SensorEventListener {


    public static final String ACTION = "com.example.pedometer.StepCheckService";
    private PowerManager pm;
    private PowerManager.WakeLock wl;

    private long lastTime = 0;
    private float speed;
    private float lastX;
    private float lastY;
    private float lastZ;

    private double x, y, z;
    private static final int SHAKE_THRESHOLD = 1500;

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;

    boolean checkStepStat = false;
    private CSVWriter csvWrite;

    private static final double TIME_INTERVAL = 5.;

    private ArrayList<Long> t;
    private ArrayList<Double> ax;
    private ArrayList<Double> ay;
    private ArrayList<Double> az;
    private boolean nextWindowStep = false;

    private double height;
    private double weight;

    private String stime = null;
    int notificationId = 1234;
    String CHANNEL_ID = "dependa";
    String channel_name = "pedovisor";
    String channel_description = "pedovisor notification channel";
    private NotificationManagerCompat notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("onCreate", "IN");
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        t = new ArrayList<Long>();
        ax = new ArrayList<Double>();
        ay = new ArrayList<Double>();
        az = new ArrayList<Double>();

        SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
        String uid = sp1.getString(Constants.SHARE_EMAIL, null);
        DBStepData dbStepData = new DBStepData(getApplicationContext());
        HashMap<String, Object> userData = dbStepData.getUserData(uid);
        height = (double) userData.get(Constants.FLD_height) / 100.;

        createNotificationChannel();

//        weight = (double) userData.get(Constants.FLD_weight);

        /*Notification not = null;

        Intent viewIntent = new Intent(this, MapTrackingActivity.class);
        PendingIntent viewPendingIntent =
                PendingIntent.getActivity(getApplicationContext(), 0, viewIntent, 0);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setContentTitle("Pedo-visor")
                        .setContentText("歩数=" + StepValue.Step)
                        .setContentIntent(viewPendingIntent);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        not = notificationBuilder.build();
        notificationManager.notify(notificationId, not);
        startForeground(notificationId, not);*/

/*
        String filePath= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/sensor.csv";
        File file = new File(filePath);
        FileWriter mFileWriter;

        try {
            if (file.exists() && !file.isDirectory()) {
                mFileWriter = new FileWriter(filePath, false);
                csvWrite = new CSVWriter(mFileWriter);
            } else {
                csvWrite = new CSVWriter(new FileWriter(filePath));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
*/
    } // end of onCreate

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        //        //Power management
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        assert pm != null;
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, ACTION);
        wl.acquire();

        Log.i("onStartCommand", "IN");
        if (accelerometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
        } // end of if

        return START_STICKY;
    } // end of onStartCommand

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("onDestroy", "IN");
        if (wl.isHeld()) {
            wl.release();
        }

        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
            StepValue.Step = 0;
            StepValue.StepState = false;
            if(notificationManager != null)
                notificationManager.cancel(notificationId);
/*
            try {
                csvWrite.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
*/
        } // end of if
    } // end of onDestroy

/*
    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.i("onSensorChanged", "IN");
        Log.i("SensorEventType", event.sensor.getType() + "");
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            long currentTime = System.currentTimeMillis();
            long gabOfTime = (currentTime - lastTime);
            writeSensorData(event);
            */
/*if (gabOfTime > 150) { //  gap of time of step count
                Log.i("onSensorChanged_IF", "FIRST_IF_IN");
                lastTime = currentTime;

                x = event.values[0];
                y = event.values[1];
                z = event.values[2];
                Log.i("X", x + "");
                Log.i("Y", y + "");
                Log.i("Z", z + "");
                speed = ((x - lastX) * (x - lastX) + (y - lastY) * (y - lastY) + (z - lastZ) * (z - lastZ)) / gabOfTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    if (checkStepStat) {
                        checkStepStat = false;
                        Log.i("onSensorChanged_IF", "SECOND_IF_IN");
                        Intent myFilteredResponse = new Intent(ACTION);

                        StepValue.Step++;
                        StepValue.StepState = true;
                        String msg = StepValue.Step + "";
                        myFilteredResponse.putExtra("stepService", msg);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(myFilteredResponse);
                    } else {
                        checkStepStat = true;
                    }
                } // end of if

                lastX = event.values[0];
                lastY = event.values[1];
                lastZ = event.values[2];
            } // end of if*//*

        } // end of if

    } // end of onSensorChanged
*/

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.i("onSensorChanged", "IN");
        Log.i("SensorEventType", event.sensor.getType() + "");
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            long currentTime = System.currentTimeMillis();
            if (lastTime == 0) lastTime = currentTime;
            long gabOfTime = (currentTime - lastTime);
//            writeSensorData(event);
            long time = event.timestamp;
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];
            t.add(time);
            ax.add(x);
            ay.add(y);
            az.add(z);
            if (gabOfTime / 1000.0 > TIME_INTERVAL) { //  gap of time of step count
                Log.i("onSensorChanged_IF", "FIRST_IF_IN");
                stepAnaysis();
                lastTime = currentTime;
                t.clear();
                ax.clear();
                ay.clear();
                az.clear();
            }
        } // end of if

    } // end of onSensorChanged

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void writeSensorData(SensorEvent event) {
        String arrStr[] = {String.valueOf(event.timestamp), String.valueOf(event.values[0]), String.valueOf(event.values[1]), String.valueOf(event.values[2])};
        csvWrite.writeNext(arrStr);
    }

    private void stepAnaysis() {
        Butterworth butterworth = new Butterworth();
        int order = 6;
        double samplingfreq = 52.0;
        double centerfreq = 1.95;
        double widthfreq = 1.25;

        double th = 0.12;
        int t0 = -1;
        int t1 = -1;
        int t2 = -1;
        int step = 0;
        boolean preCheck = false;
        double allDist = 0.;
        int size = t.size();
        double[] r = new double[size];
        double[] ar = new double[size];
        double[] tt = new double[size];

        samplingfreq = 1. / ((t.get(size - 1) - t.get(0)) / 1000000000. / size) + 1.;
        butterworth.bandPass(order, samplingfreq, centerfreq, widthfreq);
        double xAmt = maxMinCalc(ax);
        double yAmt = maxMinCalc(ay);
        double zAmt = maxMinCalc(az);
        double tmpMax = 0.0;
        ArrayList<Double> selA = new ArrayList<Double>();
        if (xAmt > tmpMax) {
            tmpMax = xAmt;
            selA = ax;
        }
        if (yAmt > tmpMax) {
            tmpMax = yAmt;
            selA = ay;
        }
        if (zAmt > tmpMax) {
            tmpMax = zAmt;
            selA = az;
        }

        for (int i = 0; i < size; i++) {
            tt[i] = (t.get(i) - t.get(0)) / 1000000000.;
            double xyz = selA.get(i);
            r[i] = Math.sqrt(ax.get(i) * ax.get(i) + ay.get(i) * ay.get(i) + az.get(i) * az.get(i));
            ar[i] = butterworth.filter(xyz);
            if (ar[i] > th) {
                if (nextWindowStep && t0 == -1 && t1 == -1) {
                    nextWindowStep = false;
                    step += 1;
                    allDist += height * 0.48;
                    t0 = i;
                    continue;
                }
                if (t0 == -1) {
                    t0 = i;
                } else {
                    if (t1 != -1) {
                        t2 = i;
                        HashMap<String, Object> result = parseStep(t0, t1, t2, tt, ar, r, selA);
                        int type = (int) result.get("type");
                        double dist = (double) result.get("dist");
                        if (type == 1) {
                            step = step + 1;
                            allDist += dist;
                            t0 = i;
                            t1 = -1;
                            t2 = -1;
                            preCheck = true;
                        } else if (type == -1) {
                            t0 = i;
                            t1 = -1;
                            t2 = -1;
                            preCheck = false;
                        } else if (type == 0) {
                            if (preCheck) {
                                step += 1;
                                allDist += dist;
                                t0 = i;
                                t1 = -1;
                                t2 = -1;
                                preCheck = true;
                            } else {
                                t0 = i;
                                t1 = -1;
                                t2 = -1;
                                preCheck = false;
                            }
                        }
                    }
                }
            }
            if (t0 != -1 && t1 == -1 && ar[i] <= 0.)
                t1 = i;
        }

        if (step > 0) {
            if (step < 11) {
                step += 1;
                allDist += height * 0.48;
            }
            allDist = allDist * 1.15;
            double stepSize = allDist / step + height * 0.05;
            if (stepSize > 1.2) {
                stepSize = height * .52;
            }
            SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
            String startTime = sp1.getString(Constants.SHARE_STARTTIME, null);
            String email = sp1.getString(Constants.SHARE_EMAIL, null);
            if (stime == null) stime = startTime;
            Date currentDate = Calendar.getInstance().getTime();
            SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
            String etime = sdf.format(currentDate);

            DBStepData dbStepData = new DBStepData(getApplicationContext());
            dbStepData.insertStepData(email, startTime, step, step * stepSize, stepSize, stime, etime, StepValue.curLatitude, StepValue.curLongitude);

            stime = etime;

            Log.i("onSensorChanged_IF", "SECOND_IF_IN");
            Intent myFilteredResponse = new Intent(ACTION);
            StepValue.Step += step;
            StepValue.StepState = true;
            String msg = StepValue.Step + "";
            myFilteredResponse.putExtra("stepService", msg);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(myFilteredResponse);

            ArrayList<HashMap<String, Object>> stat_data;
            stat_data = dbStepData.getStepData(email, startTime);
            double distance = 0.0;
            for (HashMap<String, Object> one_data : stat_data) {
                distance += (double) one_data.get(Constants.FLD_distance);
            }
            String notificationText = "歩数: " + StepValue.Step + "歩,  " + "歩行距離: " + String.format("%.3f", (distance / 1000.0)) + "km";

            viewNotification(notificationText);

        }
        if (t0 != -1 && t1 != -1 && preCheck)
            nextWindowStep = true;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = channel_name;
            String description = channel_description;
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setSound(null,null);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void viewNotification(String notificationText){

        if(!isAppIsInBackground(getApplicationContext())) {
            if(notificationManager != null)
                notificationManager.cancel(notificationId);
            return;
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.pedo_logo)
                .setContentTitle(getString(R.string.notifier_title))
                .setContentText(notificationText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true);

        notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId, mBuilder.build());
    }

    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

    private HashMap<String, Object> parseStep(int t0, int t1, int t2, double[] tt, double[] ar, double[] r, ArrayList<Double> sel) {
        HashMap<String, Object> result = new HashMap<String, Object>();
        double Tmin = 0.20;
        double Tmax = 1.75;
        double Amin = 1.00;
        double Dc = .43;
        double u = .42;
        double sigma = .085;
        double alpha = 0.15;
        double beta = 0.01;
        double Dmin = 0.35;
        double Dmax = 1.2;
        double amin = 10000.;
        double amax = 0.;
        double avgr = 0.;
        double initVC = 0.91;
        double vx = Math.sqrt(height * initVC / 3.);
        double vy = Math.sqrt(height * initVC / 3.);
        double vz = Math.sqrt(height * initVC / 3.);
        double dist = 0.;
        for (int i = t0; i < t2; i++) {

            if (sel.get(i) < amin)
                amin = sel.get(i);
            if (sel.get(i) > amax)
                amax = sel.get(i);

            double dt = i == t0 ? 0 : tt[i] - tt[i - 1];
            avgr += r[i] * dt;
            vx += ax.get(i) * dt;
            vy += ay.get(i) * dt;
            vz += az.get(i) * dt;
            double v = Math.sqrt(vx * vx + vy * vy + vz * vz);
            dist += v * dt;
        }

        double Amp = (amax - amin);
        double Tstep = tt[t2] - tt[t0];
        double duty = (tt[t1] - tt[t0]) / Tstep;
        double dk = Math.exp(-(duty - u) * (duty - u) / (2 * sigma * sigma));
        dist = dist * 1.17;//TODO
        if (dist < Dmin) {
            dist = height * Dc;
        }
//      Rule 1
        if (Tstep < Tmin || Tstep > Tmax || Amp < Amin) {
            result.put("type", -1);
            result.put("dist", 0.);
            return result;
        }
//      Rule 2
        if (Tstep >= Tmin && Tstep <= Tmax && dk >= alpha) {

            result.put("type", 1);
            result.put("dist", dist);
            return result;
        }
//      Rule 3
        if (Tstep >= Tmin && Tstep <= Tmax && dk <= beta) {
            result.put("type", -1);
            result.put("dist", 0.);
            return result;
        }
        result.put("type", 0);
        result.put("dist", dist);
        return result;
    }

    private double maxMinCalc(ArrayList<Double> m) {
        double max = 0.;
        double min = 1000.;
        for (int i = 0; i < m.size(); i++) {
            if (m.get(i) > max)
                max = m.get(i);
            if (m.get(i) < min)
                min = m.get(i);
        }

        return max - min;
    }
}
