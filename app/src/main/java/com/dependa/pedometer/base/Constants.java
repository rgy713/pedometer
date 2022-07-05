package com.dependa.pedometer.base;

import android.graphics.Color;

/**
 * Created by RGY on 9/14/2017.
 */

public class Constants {

    public static final String TEST_UID = "pedo";
    public static final String TEST_PWD = "123456";

    public static final String SHARE_PREF = "StepLogin";
    public static final String SHARE_EMAIL = "Email";
    public static final String SHARE_MAIN_DATA = "MainData";
    public static final String SHARE_PWD = "Password";
    public static final String SHARE_CODE = "Code";
    public static final String SHARE_STARTTIME = "StartTime";
    public static final String CONNECT_INTERNET = "connet_internet";

    public static final Integer COUNT_DOWN = 5;

    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String DATE_FORMAT_S = "yyyy-MM-dd HH:mm";
    public static final String DATE_FORMAT_Z = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final String DATE_FORMAT_yMd = "yyyy-MM-dd";
    public static final String DATE_FORMAT_yM = "yyyy-MM";
    public static final String DATE_FORMAT_Hms = "HH:mm:ss";

    public static final int MAIN_COLOR = Color.rgb(1, 173, 187);
    //  ---------------------- DB related Constant------------------------------------
    public static final String DB_NAME = "DBPedometer.db";
    public static final String CSV_NAME = "CSVPedometer.csv";
    public static final String DEVICEID = "pedovisor";
    public static final int SQLITE_VERSION = 3;
    public static final String TBL_STEP = "tbl_step";
    public static final String TBL_USER = "tbl_user";
    public static final String TBL_STAT = "tbl_stat";
    public static final String FLD_id = "_id";
    public static final String FLD_username = "username";
    public static final String FLD_startTime = "startTime";
    public static final String FLD_endTime = "endTime";
    public static final String FLD_numberOfSteps = "numberOfSteps";
    public static final String FLD_distance = "distance";
    public static final String FLD_password = "password";
    public static final String FLD_email = "email";
    public static final String FLD_name = "name";
    public static final String FLD_gender = "gender";
    public static final String FLD_birthday = "birthday";
    public static final String FLD_height = "height";
    public static final String FLD_weight = "weight";
    public static final String FLD_habbit = "habbit";
    public static final String FLD_step_size = "step_size";
    public static final String FLD_loginDate = "loginDate";
    public static final String FLD_time = "time";
    public static final String FLD_latitude = "latitude";
    public static final String FLD_longitude = "longitude";
    public static final String FLD_isUploaded = "isUploaded";
    public static final String FLD_dist = "dist";
    public static final String FLD_stime = "stime";
    public static final String FLD_etime = "etime";

    public static final String GOTO_DASHBOARD_URL = "http://dependa.co.jp/dj/web/pedovisor/dashboard";
    public static final String GOTO_HEALTH_URL = "http://dependa.co.jp/dj/web/pedovisor/health";

    //-------------------------- Post Urls-----------------------------------------------
//    public static final String BASE_URL = "http://192.168.1.137:8080/dj/step/v2";
    public static final String BASE_URL = "http://tk2-402-42013.vs.sakura.ne.jp/dj/step/v2";
    public static final String LOGIN_URL = BASE_URL + "/user/login";
    public static final String REGISTRATION_URL = BASE_URL + "/user/create";
    public static final String CHANGEUSERINFO_URL = BASE_URL + "/user/modify";
    public static final String CHANGEPASSWORD_URL = BASE_URL + "/user/change_pw";
    public static final String EMAILLOGIN_URL = BASE_URL + "/user/verification";
    public static final String RESETPASSWORD_URL = BASE_URL + "/user/reset_pw";
    public static final String FILE_UPLOAD_URL = BASE_URL + "/data/upload";
    public static final String FILE_DOWNLOAD_URL = BASE_URL + "/data/download";
    public static final String SEND_EMAIL_VERIFY = BASE_URL + "/user/send-email-verify";
    public static final String CHECK_VERIFY = BASE_URL + "/user/check-verify";
    public static final String GET_MAIN_DATA = BASE_URL + "/data/get-main-data";
    public static final String GET_CALENDAR_DATA = BASE_URL + "/data/get-calendar-data";
    public static final String GET_MEASUREMENT_DATA = BASE_URL + "/data/get-measurement-data";
    //-------------------------- Health Urls-----------------------------------------------
    public static final String GET_FOOD_LIST = BASE_URL + "/health/get-food-list";
    public static final String GET_SQ_LIST = BASE_URL + "/health/get-sq-list";
    public static final String GET_MEAL_DATA = BASE_URL + "/health/meal-get-list";
    public static final String GET_MEAL_INFO= BASE_URL + "/health/meal-get-info";
    public static final String POST_MEAL_UPDATE= BASE_URL + "/health/meal-update";
    public static final String POST_MEAL_CREATE= BASE_URL + "/health/meal-create";
    public static final String POST_MEAL_DELETE= BASE_URL + "/health/meal-delete";
    public static final String GET_SLEEP_DATA= BASE_URL + "/health/sleep-get-list";
    public static final String GET_SLEEP_INFO= BASE_URL + "/health/sleep-get-info";
    public static final String POST_SLEEP_UPDATE= BASE_URL + "/health/sleep-update";
    public static final String POST_SLEEP_CREATE= BASE_URL + "/health/sleep-create";
    public static final String POST_SLEEP_DELETE= BASE_URL + "/health/sleep-delete";

    public static final String GET_GROUP_LIST = BASE_URL + "/user/get-groups";
    public static final String SET_GROUP_LIST = BASE_URL + "/user/set-groups";

    public static final String GET_WEIGHT_MONTH_DATA = BASE_URL + "/health/weight-get-month-list";
    public static final String POST_WEIGHT_UPDATE= BASE_URL + "/health/weight-update";
    public static final String POST_WEIGHT_ADD= BASE_URL + "/health/weight-add";
    public static final String POST_WEIGHT_DELETE= BASE_URL + "/health/weight-delete";
    public static final String GET_SCORE_DATA= BASE_URL + "/health/get-score-data";
}
