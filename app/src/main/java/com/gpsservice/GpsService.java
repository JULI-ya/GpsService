package com.gpsservice;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import com.gpsservice.models.LatLong;
import com.gpsservice.models.MKAD;
import com.gpsservice.models.TTK;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GpsService extends Service {

    final String LOG_TAG = "com.gpsservice.GpsService";

    public static final String KEY_SERVER = "server";
    public static final String KEY_ID = "id";
    public static final String KEY_INTERVAL = "interval";
//    public static final String KEY_CUSTOM_LOCATION = "is_custom_location";

    private String mId;
    private Timer mTimer;
//    private boolean isCustomLocation;

    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");
        mId = Cache.getLastIdt(this);
//            isCustomLocation = intent.getBooleanExtra(KEY_CUSTOM_LOCATION, false);
        execute();
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return null;
    }

    void execute() {
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                check();
            }
        }, 0, Cache.getLastSavedInterval(this) * 60 * 1000);

    }

    private void check() {
        Log.d(LOG_TAG, "execute");
        GPSTracker.getLocation(this, mId);
//        LatLong currentCoordinates = isCustomLocation ? new LatLong(Double.parseDouble(MainActivity.mCustomLatitude), Double.parseDouble(MainActivity.mCustomLongitude)) : new LatLong(currentLocation.getLatitude(), currentLocation.getLongitude());
    }
}
