package com.dependa.pedometer.base;

import android.content.Context;
import android.preference.PreferenceManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by RGY on 10/6/2017.
 */

public class Utils {

    public static boolean isUidValid(String uid) {
        //TODO: Replace this with your own logic
        return true;
    }

    public static boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return true;
    }

    public static boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return (email.contains("@"));
    }

    public static boolean isNameValid(String name) {
        //TODO: Replace this with your own logic
        return true;
    }

    public static boolean isBirthdayValid(String birthday) {

        String DATE_PATTERN = "^[0-9]{4}-(((0[13578]|(10|12))-(0[1-9]|[1-2][0-9]|3[0-1]))|(02-(0[1-9]|[1-2][0-9]))|((0[469]|11)-(0[1-9]|[1-2][0-9]|30)))$";

        Matcher matcher = Pattern.compile(DATE_PATTERN).matcher(birthday);

        if (matcher.matches()) {
            return true;
        }
        return false;
    }

    public static boolean isHeightValid(String height) {
        //TODO: Replace this with your own logic
        return true;
    }

    public static boolean isWeightValid(String weight) {
        //TODO: Replace this with your own logic
        return true;
    }

    private static final TimeZone utcTZ = TimeZone.getTimeZone("UTC");

    public static String getNowTime(String dateFormat){
        Date currentDate = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(currentDate);
    }

    public static boolean timeCompare(String start, String end, String format) throws ParseException {
        long different = (Utils.toMilliSeconds( end, format) - Utils.toMilliSeconds(start, format));
        return different > 0;
    }

    public static String timeDiff(String start, String end, String dateFormat) throws ParseException {
        long different = (Utils.toMilliSeconds( end, dateFormat) - Utils.toMilliSeconds(start, dateFormat));
        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long hours = different / hoursInMilli;
        long elapsedMinutes = different % hoursInMilli;
        long minutes = elapsedMinutes / minutesInMilli;
        return hours + "時間" + minutes + "分";
    }

    public static String toLocalTime(String dateStr, String dateFormat) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, Locale.ENGLISH);
        Date date = formatter.parse(dateStr);
        TimeZone to = TimeZone.getDefault();
        long milliSeconds = convertTime(date.getTime(), utcTZ, to);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
    public static long toMilliSeconds(String dateStr, String dateFormat) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, Locale.ENGLISH);
        Date date = formatter.parse(dateStr);
        return date.getTime();
    }
    public static String toUTC(String dateStr, String dateFormat) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, Locale.ENGLISH);
        Date date = formatter.parse(dateStr);
        TimeZone from = TimeZone.getDefault();
        long milliSeconds = convertTime(date.getTime(), from, utcTZ);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    private static long convertTime(long time, TimeZone from, TimeZone to) {
        return time + getTimeZoneOffset(time, from, to);
    }

    private static long getTimeZoneOffset(long time, TimeZone from, TimeZone to)    {
        int fromOffset = from.getOffset(time);
        int toOffset = to.getOffset(time);
        int diff = 0;

        if (fromOffset >= 0){
            if (toOffset > 0){
                toOffset = -1*toOffset;
            } else {
                toOffset = Math.abs(toOffset);
            }
            diff = (fromOffset+toOffset)*-1;
        } else {
            if (toOffset <= 0){
                toOffset = -1*Math.abs(toOffset);
            }
            diff = (Math.abs(fromOffset)+toOffset);
        }
        return diff;
    }

    private static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_locaction_updates";

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    public static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    public static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }

}
