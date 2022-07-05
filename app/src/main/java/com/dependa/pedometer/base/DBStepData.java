package com.dependa.pedometer.base;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by RGY on 9/19/2017.
 */

public class DBStepData extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 5;
    public static final String DATABASE_NAME = "DBPedometer.db";

    public DBStepData(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL = "CREATE TABLE " + Constants.TBL_STEP + " (" +
                Constants.FLD_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Constants.FLD_email + " TEXT, " +
                Constants.FLD_startTime + " TEXT, " +
                Constants.FLD_numberOfSteps + " INTEGER, " +
                Constants.FLD_distance + " REAL, " +
                Constants.FLD_step_size + " REAL, " +
                Constants.FLD_stime + " TEXT, " +
                Constants.FLD_etime + " TEXT, " +
                Constants.FLD_latitude + " TEXT, " +
                Constants.FLD_longitude + " TEXT, " +
                Constants.FLD_isUploaded + " INTEGER DEFAULT 0 " +
                ");";
        db.execSQL(SQL);
        SQL = "CREATE TABLE " + Constants.TBL_USER + " (" +
                Constants.FLD_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Constants.FLD_email + " TEXT, " +
                Constants.FLD_password + " TEXT, " +
                Constants.FLD_name + " TEXT DEFAULT NULL, " +
                Constants.FLD_gender + " INTEGER DEFAULT 0 , " +
                Constants.FLD_birthday + " TEXT DEFAULT NULL, " +
                Constants.FLD_height + " REAL, " +
                Constants.FLD_weight + " REAL DEFAULT NULL, " +
                Constants.FLD_habbit + " INTEGER DEFAULT 0 , " +
                Constants.FLD_step_size + " INTEGER DEFAULT 0 , " +
                Constants.FLD_loginDate + " TEXT " +
                ");";
        db.execSQL(SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TBL_STEP);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TBL_USER);
        onCreate(db);
    }

    public void insertStepData(String email, String startTime, int numberOfSteps, double distance, double stepSize, String stime, String etime, double latitude, double longitude) {
        SQLiteDatabase db = getWritableDatabase();

        String SQL = "INSERT INTO " + Constants.TBL_STEP +
                " VALUES(null, '" +
                email + "', '" +
                startTime + "', " +
                numberOfSteps + ", " +
                distance + ", " +
                stepSize + ", '" +
                stime + "', '" +
                etime + "', " +
                String.valueOf(latitude) + ", " +
                String.valueOf(longitude) + ", " +
                0 + ");";

        db.execSQL(SQL);
        db.close();
    }

    public void updateStepDataFailUpload(String email, String startTime) {
        SQLiteDatabase db = getWritableDatabase();
        //TODO
        db.execSQL("UPDATE " + Constants.TBL_STEP + " SET " + Constants.FLD_isUploaded + "=0 WHERE " + Constants.FLD_email + "='" + email + "' AND " + Constants.FLD_startTime + "='" + startTime + "' ;");
        db.close();
    }

    public void updateStepDataSuccessUpload(String email) {
        SQLiteDatabase db = getWritableDatabase();
        //TODO
        db.execSQL("UPDATE " + Constants.TBL_STEP + " SET " + Constants.FLD_isUploaded + "=1 WHERE " + Constants.FLD_email + "='" + email + "' AND " + Constants.FLD_isUploaded + "=0 ;");
        db.close();
    }

    public void updatedStepDataRemove(String email) {
        SQLiteDatabase db = getWritableDatabase();
        //TODO
        db.execSQL("DELETE FROM " + Constants.TBL_STEP + " WHERE " + Constants.FLD_email + "='" + email + "' ;");
        db.close();
    }

    public void deleteStepData(String item) {
        SQLiteDatabase db = getWritableDatabase();
        //TODO
        db.execSQL("DELETE FROM MONEYBOOK WHERE item='" + item + "';");
        db.close();
    }

    public HashMap<String, Integer> getStepBefore7DaysData(String email) {
        SQLiteDatabase db = getReadableDatabase();
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        String SQL = "SELECT " +
                Constants.FLD_startTime + ", " +
                Constants.FLD_numberOfSteps + ", " +
                Constants.FLD_distance + " " +
                " FROM " + Constants.TBL_STEP +
                " WHERE " + Constants.FLD_email + "='" + email + "' " +
                " AND " + Constants.FLD_startTime + " > (SELECT datetime('now', '-6 day'))";

        Cursor cursor = db.rawQuery(SQL, null);
        Integer totalStepsIn7Days = 0;
        Double totalDistanceIn7Days = 0.0;
        while (cursor.moveToNext()) {
            String startTime = (cursor.getString(0)).split("T")[0];
            Integer numberOfSteps = cursor.getInt(1);
            totalStepsIn7Days += numberOfSteps;
            double distance = cursor.getDouble(2);
            totalDistanceIn7Days += distance;
            if (result.get(startTime) == null) {
                result.put(startTime, numberOfSteps);
            } else {
                result.put(startTime, result.get(startTime) + numberOfSteps);
            }
        }
        if (totalStepsIn7Days != 0) {
            result.put("totalStepsIn7Days", totalStepsIn7Days);
            result.put("meanStepWidthIn7Days", (int) (totalDistanceIn7Days * 1000. / totalStepsIn7Days));
        } else {
            result.put("totalStepsIn7Days", 0);
            result.put("meanStepWidthIn7Days", 0);
        }
        return result;
    }

    public Double getMeanStepWidth(String email) {
        SQLiteDatabase db = getReadableDatabase();
        Double result = 0.;
        String SQL = "SELECT " +
                " AVG(" + Constants.FLD_step_size + ") " +
                " FROM " + Constants.TBL_STEP +
                " WHERE " + Constants.FLD_email + "='" + email + "' AND " + Constants.FLD_numberOfSteps + "<>0 ";

        Cursor cursor = db.rawQuery(SQL, null);
        while (cursor.moveToNext()) {
            result = cursor.getDouble(0) * 100.;
        }
        return result;
    }

    public ArrayList<HashMap<String, Object>> getStepBefore1MonthData(String email) {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<HashMap<String, Object>> result = new ArrayList<HashMap<String, Object>>();
        String SQL = "SELECT " +
                Constants.FLD_stime + ", " +
                Constants.FLD_etime + ", " +
                Constants.FLD_latitude + ", " +
                Constants.FLD_longitude + ", " +
                Constants.FLD_numberOfSteps + ", " +
                Constants.FLD_distance + ", " +
                Constants.FLD_step_size + " " +
                " FROM " + Constants.TBL_STEP +
                " WHERE " + Constants.FLD_email + "='" + email + "' AND " + Constants.FLD_numberOfSteps + "<>0 " +
                " AND " + Constants.FLD_startTime + " > (SELECT datetime('now', '-30 day'))";

        Cursor cursor = db.rawQuery(SQL, null);
        while (cursor.moveToNext()) {
            HashMap<String, Object> raw = new HashMap<String, Object>();
            raw.put(Constants.FLD_stime, cursor.getString(0));
            raw.put(Constants.FLD_etime, cursor.getString(1));
            raw.put(Constants.FLD_latitude, cursor.getDouble(2));
            raw.put(Constants.FLD_longitude, cursor.getDouble(3));
            raw.put(Constants.FLD_numberOfSteps, cursor.getInt(4));
            raw.put(Constants.FLD_distance, cursor.getDouble(5));
            raw.put(Constants.FLD_step_size, cursor.getDouble(6) * 100.);
            result.add(raw);
        }
        return result;
    }

    public ArrayList<String> getStepTimeListPerDayData(String email, String date) {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<String> result = new ArrayList<String>();
        String SQL = "SELECT " +
                Constants.FLD_startTime + " " +
                " FROM " + Constants.TBL_STEP +
                " WHERE " + Constants.FLD_email + "='" + email + "' " +
                " AND " + Constants.FLD_startTime + " >= date('" + date + "') " +
                " AND " + Constants.FLD_startTime + " <  date('" + date + "', '+1 day')" +
                " GROUP BY " + Constants.FLD_startTime;

        Cursor cursor = db.rawQuery(SQL, null);
        while (cursor.moveToNext()) {
            String startTime = (cursor.getString(0)).split("T")[1];
            result.add(startTime);
        }
        return result;
    }

    public HashMap<String, String> getMeasureDayData(String email) {
        SQLiteDatabase db = getReadableDatabase();
        HashMap<String, String> result = new HashMap<String, String>();
        String SQL = "SELECT " +
                Constants.FLD_startTime + " " +
                " FROM " + Constants.TBL_STEP +
                " WHERE " + Constants.FLD_email + "='" + email + "' " +
                " GROUP BY " + Constants.FLD_startTime;

        Cursor cursor = db.rawQuery(SQL, null);
        while (cursor.moveToNext()) {
            String startTime = (cursor.getString(0)).split("T")[0];
            result.put(startTime, "true");
        }
        return result;
    }

    public ArrayList<HashMap<String, Object>> getStepData(String email, String startTime) {

        SQLiteDatabase db = getReadableDatabase();
        ArrayList<HashMap<String, Object>> result = new ArrayList<HashMap<String, Object>>();
        String SQL = "SELECT " +
                Constants.FLD_stime + ", " +
                Constants.FLD_etime + ", " +
                Constants.FLD_latitude + ", " +
                Constants.FLD_longitude + ", " +
                Constants.FLD_numberOfSteps + ", " +
                Constants.FLD_distance + ", " +
                Constants.FLD_step_size + " " +
                " FROM " + Constants.TBL_STEP +
                " WHERE " + Constants.FLD_email + "='" + email + "' AND " + Constants.FLD_startTime + "='" + startTime + "' " +
                " ORDER BY " + Constants.FLD_stime + " ;";

        Cursor cursor = db.rawQuery(SQL, null);
        while (cursor.moveToNext()) {
            HashMap<String, Object> raw = new HashMap<String, Object>();
            raw.put(Constants.FLD_stime, cursor.getString(0));
            raw.put(Constants.FLD_etime, cursor.getString(1));
            raw.put(Constants.FLD_latitude, cursor.getDouble(2));
            raw.put(Constants.FLD_longitude, cursor.getDouble(3));
            raw.put(Constants.FLD_numberOfSteps, cursor.getInt(4));
            raw.put(Constants.FLD_distance, cursor.getDouble(5));
            raw.put(Constants.FLD_step_size, cursor.getDouble(6) * 100.);
            result.add(raw);
        }
        return result;
    }

    private HashMap<String, Object> getAllStepData(String email) {

        SQLiteDatabase db = getReadableDatabase();
        HashMap<String, Object> result = new HashMap<String, Object>();
        String SQL = "SELECT " +
                Constants.FLD_startTime + ", " +
                Constants.FLD_stime + " " +
                " FROM " + Constants.TBL_STEP +
                " WHERE " + Constants.FLD_email + "='" + email + "' " +
                " ORDER BY " + Constants.FLD_stime + " ;";

        Cursor cursor = db.rawQuery(SQL, null);
        while (cursor.moveToNext()) {
            result.put(cursor.getString(0) + "-" + cursor.getString(1), 1);
        }
        return result;
    }

    public boolean exportAllStepData(String email, String filePath) {
        File file = new File(filePath);
        FileWriter mFileWriter;
        CSVWriter csvWrite;

        SQLiteDatabase db = getReadableDatabase();

        String SQL = "SELECT " +
                Constants.FLD_startTime + ", " +
                Constants.FLD_numberOfSteps + ", " +
                Constants.FLD_distance + ", " +
                Constants.FLD_step_size + ", " +
                Constants.FLD_stime + ", " +
                Constants.FLD_etime + ", " +
                Constants.FLD_latitude + ", " +
                Constants.FLD_longitude + " " +
                " FROM " + Constants.TBL_STEP +
                " WHERE " + Constants.FLD_email + "='" + email + "' " +
                " ORDER BY " + Constants.FLD_id + " ;";

        Cursor cursor = db.rawQuery(SQL, null);
        try {
            if (file.exists() && !file.isDirectory()) {
                mFileWriter = new FileWriter(filePath, false);
                csvWrite = new CSVWriter(mFileWriter);
            } else {
                csvWrite = new CSVWriter(new FileWriter(filePath));
            }

            while (cursor.moveToNext()) {
                String arrStr[] = {Utils.toUTC(cursor.getString(0), Constants.DATE_FORMAT), cursor.getString(1), cursor.getString(2), cursor.getString(3), Utils.toUTC(cursor.getString(4), Constants.DATE_FORMAT), Utils.toUTC(cursor.getString(5), Constants.DATE_FORMAT), cursor.getString(6), cursor.getString(7)};
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
        } catch (Exception sqlEx) {
            cursor.close();
            return false;
        }
        return true;
    }

    public boolean importStepData(String email, String filePath) {
        SQLiteDatabase db = getWritableDatabase();
        String SQL = "";
        HashMap<String, Object> allStepData = getAllStepData(email);
        boolean isInsert = false;
        if (allStepData.size() == 0) isInsert = true;
        try {
//            db.execSQL("DELETE FROM " + Constants.TBL_STEP + " WHERE " + Constants.FLD_username + "='" + username + "' AND " + Constants.FLD_isUploaded + "=1 ;");
            CSVReader reader = new CSVReader(new FileReader(filePath));
            String[] nextLine;
            boolean isDo = false;
            while ((nextLine = reader.readNext()) != null) {
                if(nextLine.length != 8){
                    continue;
                }
                isDo = true;
                String startTime = Utils.toLocalTime(nextLine[0], Constants.DATE_FORMAT);
                String stime = Utils.toLocalTime(nextLine[4], Constants.DATE_FORMAT);
                if (!isInsert)
                    if (allStepData.get(startTime + "-" + stime)!= null)
                        continue;

                int numberOfSteps = Integer.parseInt(nextLine[1]);
                double distance = Double.parseDouble(nextLine[2]);
                double stepSize = Double.parseDouble(nextLine[3]);
                String etime = Utils.toLocalTime(nextLine[5], Constants.DATE_FORMAT);
                double latitude = Double.parseDouble(nextLine[6]);
                double longitude = Double.parseDouble(nextLine[7]);

                SQL = "INSERT INTO " + Constants.TBL_STEP +
                        " VALUES(null, '" +
                        email + "', '" +
                        startTime + "', " +
                        numberOfSteps + ", " +
                        distance + ", " +
                        stepSize + ", '" +
                        stime + "', '" +
                        etime + "', " +
                        latitude + ", " +
                        longitude + ", " +
                        1 + ");";
                db.execSQL(SQL);
            }

            reader.close();

            if(!isDo){
                File file = new File(filePath);
                if (file.exists() && file.isFile())
                {
                    FileWriter fw = new FileWriter(filePath, false);
                    fw.write("");
                    fw.close();
                }
            }

        } catch (Exception e) {
            db.close();
            return false;
        }
        db.close();
        return true;
    }

    public int getCountWeekWidth2Times(String email) {
        SQLiteDatabase db = getReadableDatabase();
        String SQL = "SELECT " +
                " COUNT(DISTINCT " + Constants.FLD_startTime + ") " +
                " FROM " + Constants.TBL_STEP +
                " WHERE " + Constants.FLD_email + "='" + email + "' " +
                " GROUP BY STRFTIME('%W', " + Constants.FLD_startTime + ")";

        Cursor cursor = db.rawQuery(SQL, null);
        int result = 0;
        while (cursor.moveToNext()) {
            if (cursor.getInt(0) >= 2) {
                result += 1;
            }
        }
        return result;
    }

    public void insertUserData( String email, String password, String name, Integer gender, String birthday, Double height, Double weight, Integer habbit, Integer step_size, String loginDate) {

        if(existUserData(email)){
            updateUserData(email, password, name, gender, birthday, height, weight, habbit, step_size, loginDate);
            return;
        }

        SQLiteDatabase db = getWritableDatabase();

        String SQL = "INSERT INTO " + Constants.TBL_USER +
                " VALUES(null, '" +
                email + "', '" +
                password + "', '" +
                name + "', " +
                gender + ", '" +
                birthday + "', " +
                height + ", " +
                weight + ", " +
                habbit + ", " +
                step_size + ", '" +
                loginDate + "');";

        db.execSQL(SQL);
        db.close();
    }

    public void updateUserData(String email, String password, String name, Integer gender, String birthday, Double height, Double weight, Integer habbit, Integer step_size, String loginDate) {
        SQLiteDatabase db = getWritableDatabase();
        String SQL = "UPDATE " + Constants.TBL_USER + " SET " +
                Constants.FLD_password + "='" + password + "', " +
                Constants.FLD_name + "='" + name + "', " +
                Constants.FLD_gender + "=" + gender + ", " +
                Constants.FLD_birthday + "='" + birthday + "', " +
                Constants.FLD_height + "=" + height + ", " +
                Constants.FLD_weight + "=" + weight + ", " +
                Constants.FLD_habbit + "=" + habbit + ", " +
                Constants.FLD_step_size + "=" + step_size + ", " +
                Constants.FLD_loginDate + "='" + loginDate + "'" +
                " WHERE " + Constants.FLD_email + "='" + email + "';";
        db.execSQL(SQL);
        db.close();
    }

    public void deleteUserData(String item) {
        SQLiteDatabase db = getWritableDatabase();
        //TODO
        db.execSQL("DELETE FROM MONEYBOOK WHERE item='" + item + "';");
        db.close();
    }

    public HashMap<String, Object> getUserData(String email) {
        SQLiteDatabase db = getReadableDatabase();
        String SQL = "SELECT " +
                Constants.FLD_email + ", " +
                Constants.FLD_password + ", " +
                Constants.FLD_name + ", " +
                Constants.FLD_gender + ", " +
                Constants.FLD_birthday + ", " +
                Constants.FLD_height + ", " +
                Constants.FLD_weight + ", " +
                Constants.FLD_habbit + ", " +
                Constants.FLD_step_size + ", " +
                Constants.FLD_loginDate + " " +
                " FROM " + Constants.TBL_USER +
                " WHERE " + Constants.FLD_email + "='" + email + "' " +
                " ORDER BY " + Constants.FLD_loginDate + " DESC " +
                " LIMIT 1";

        Cursor cursor = db.rawQuery(SQL, null);
        cursor.moveToNext();
        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put(Constants.FLD_email, cursor.getString(0));
        result.put(Constants.FLD_password, cursor.getString(1));
        result.put(Constants.FLD_name, cursor.getString(2).equals("null") ? null : cursor.getString(2) );
        result.put(Constants.FLD_gender, cursor.getInt(3));
        result.put(Constants.FLD_birthday, cursor.getString(4).equals("null") ? null : cursor.getString(4));
        result.put(Constants.FLD_height, cursor.getDouble(5));
        result.put(Constants.FLD_weight, cursor.getDouble(6));
        result.put(Constants.FLD_habbit, cursor.getInt(7));
        result.put(Constants.FLD_step_size, cursor.getInt(8));
        result.put(Constants.FLD_loginDate, cursor.getString(9));

        return result;
    }

    public boolean existUserData(String email) {
        SQLiteDatabase db = getReadableDatabase();

        String SQL = "SELECT " +
                " COUNT(" + Constants.FLD_email + ") " +
                " FROM " + Constants.TBL_USER +
                " WHERE " + Constants.FLD_email + "='" + email + "' ;";

        Cursor cursor = db.rawQuery(SQL, null);
        while (cursor.moveToNext()) {
            if (cursor.getInt(0) > 1) {
                return true;
            }
        }
        return false;
    }

}
