package com.gpsservice.alarm;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.gpsservice.Cache;
import com.gpsservice.GPSTracker;
import com.gpsservice.MainActivity;

import java.util.Timer;
import java.util.TimerTask;

public class AlarmReceiver extends BroadcastReceiver {

    private static String TAG = "com.gpsservice.alarm.AlarmReceiver";
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        // For our recurring task, we'll just display a message
//        Toast.makeText(context, "I'm running", Toast.LENGTH_SHORT).show();
        Thread thread = new Thread(new GpsService());
        thread.start();
        Log.i(TAG, "AlarmReceiver onReceive");
    }

    public class GpsService implements Runnable {

        final String LOG_TAG = "com.gpsservice.GpsService";
//    public static final String KEY_CUSTOM_LOCATION = "is_custom_location";

        private String mId;
//    private boolean isCustomLocation;

        private void check() {
            Log.d(LOG_TAG, "execute");

            if (!isMockLocationEnabled()) {
                GPSTracker.getLocation(mContext, mId);
            } else {
                Intent intent = new Intent(MainActivity.NOTIFICATION_INTENT);
                intent.putExtra(MainActivity.IS_MOCK, true);
                mContext.sendBroadcast(intent);
            }
//        LatLong currentCoordinates = isCustomLocation ? new LatLong(Double.parseDouble(MainActivity.mCustomLatitude), Double.parseDouble(MainActivity.mCustomLongitude)) : new LatLong(currentLocation.getLatitude(), currentLocation.getLongitude());
        }

        @Override
        public void run() {
            mId = Cache.getLastIdt(mContext);
//            isCustomLocation = intent.getBooleanExtra(KEY_CUSTOM_LOCATION, false);
            check();
        }

        private boolean isMockLocationEnabled() {
            if (Settings.Secure.getString(mContext.getContentResolver(),
                    Settings.Secure.ALLOW_MOCK_LOCATION).equals("0"))
                return false;
            else return true;
        }
    }
}
