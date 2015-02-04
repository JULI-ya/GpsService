package com.gpsservice;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Cache {

    public static void saveId(String id, Context context) {
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(GpsService.KEY_ID, id);
        ed.commit();
    }

    public static void saveServerAddress(String server, Context context) {
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(GpsService.KEY_SERVER, server);
        ed.commit();
    }

    public static String getLastIdt(Context context) {
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(context);
        String savedId = sPref.getString(GpsService.KEY_ID, "");
        return savedId;
    }

    public static String getLastServerAddress(Context context) {
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sPref.getString(GpsService.KEY_SERVER, context.getString(R.string.default_url));
    }

    public static int getLastSavedInterval(Context context) {
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sPref.getInt(GpsService.KEY_INTERVAL, 1);
    }

    public static void saveInterval(int interval, Context context) {
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putInt(GpsService.KEY_INTERVAL, interval);
        ed.commit();
    }
}
