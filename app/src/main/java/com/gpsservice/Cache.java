package com.gpsservice;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Cache {
    public static final String KEY_SERVER = "server";
    public static final String KEY_ID = "id";
    public static final String KEY_INTERVAL = "interval";
    private static String KEY_AM_WORKING = "alarm_manager";


    public static void saveId(String id, Context context) {
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(KEY_ID, id);
        ed.commit();
    }

    public static void saveServerAddress(String server, Context context) {
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(KEY_SERVER, server);
        ed.commit();
    }

    public static String getLastIdt(Context context) {
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(context);
        String savedId = sPref.getString(KEY_ID, "");
        return savedId;
    }

    public static String getLastServerAddress(Context context) {
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sPref.getString(KEY_SERVER, context.getString(R.string.default_url));
    }

    public static int getLastSavedInterval(Context context) {
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sPref.getInt(KEY_INTERVAL, 1);
    }

    public static void saveInterval(int interval, Context context) {
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putInt(KEY_INTERVAL, interval);
        ed.commit();
    }

    public static void setAlarmManagerWorking(boolean isWorking, Context context) {
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putBoolean(KEY_AM_WORKING, isWorking);
        ed.commit();
    }

    public static boolean isAlarmManagerWorking(Context context) {
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sPref.getBoolean(KEY_AM_WORKING, false);
    }
}
