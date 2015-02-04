package com.gpsservice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

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
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GpsService extends Service {

    final String LOG_TAG = "com.gpsservice.GpsService";

    public static final String KEY_SERVER = "server";
    public static final String KEY_ID = "id";
    public static final String KEY_INTERVAL = "interval";
//    public static final String KEY_CUSTOM_LOCATION = "is_custom_location";

    private String mServerAddress;
    private String mId;
    private Timer mTimer;
//    private boolean isCustomLocation;

    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");
        mServerAddress = Cache.getLastServerAddress(this);
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
        Location currentLocation = GPSTracker.getLocation(this);
//        LatLong currentCoordinates = isCustomLocation ? new LatLong(Double.parseDouble(MainActivity.mCustomLatitude), Double.parseDouble(MainActivity.mCustomLongitude)) : new LatLong(currentLocation.getLatitude(), currentLocation.getLongitude());
        LatLong currentCoordinates = new LatLong(currentLocation.getLatitude(), currentLocation.getLongitude());
        if (currentLocation != null) {
            Intent intent = new Intent(MainActivity.NOTIFICATION_INTENT);
            boolean isInsideTTK = RegionUtil.coordinateInRegion(new TTK(), currentCoordinates);
            if (isInsideTTK) {
                intent.putExtra(MainActivity.RESULT_KEY, "Is inside TTK");
                sendBroadcast(intent);
                sendRequest(1, currentCoordinates);
                Log.e(LOG_TAG, "isInsideTTK: " + isInsideTTK);
                return;
            }
            boolean isInsideMkad = RegionUtil.coordinateInRegion(new MKAD(), currentCoordinates);
            if (isInsideMkad) {
                intent.putExtra(MainActivity.RESULT_KEY, "Is inside MKAD");
                sendBroadcast(intent);
                sendRequest(2, currentCoordinates);
                Log.e(LOG_TAG, "isInsideMkad: " + isInsideMkad);
                return;
            }
            intent.putExtra(MainActivity.RESULT_KEY, "Out of ranges");
            sendBroadcast(intent);
            sendRequest(3, currentCoordinates);
        } else {
            Log.e(LOG_TAG, "Cannot determine location");
        }
    }

    private void sendRequest(int zone, LatLong currentCoordinates) {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("id", mId));
        nameValuePairs.add(new BasicNameValuePair("zone", String.valueOf(zone)));
        nameValuePairs.add(new BasicNameValuePair("lat", String.valueOf(currentCoordinates.getLatitude())));
        nameValuePairs.add(new BasicNameValuePair("long", String.valueOf(currentCoordinates.getLongitude())));
        nameValuePairs.add(new BasicNameValuePair("timestamp", String.valueOf(System.currentTimeMillis())));
        HttpClient httpClient = new DefaultHttpClient();
        String paramsString = URLEncodedUtils.format(nameValuePairs, "UTF-8");
        HttpGet httpGet = new HttpGet(mServerAddress + "?" + paramsString);
        try {
            HttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == 200) {
                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentTitle("Request sent");

                Intent notificationIntent = new Intent(this, MainActivity.class);
                PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(contentIntent);

                // Add as notification
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(Application.FM_NOTIFICATION_ID, builder.build());
                Log.i(LOG_TAG, "Request send");
            } else {
                Log.e(LOG_TAG, "Request error");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
