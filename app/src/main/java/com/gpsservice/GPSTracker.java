package com.gpsservice;


import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
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

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

public class GPSTracker extends Service {

    private static final String LOG_TAG = "com.gpsservice.GPSTracker";

    private static LocationManager locationManager;
    private static Context mContext;
    private static String mId;


    public static boolean isProvidersEnabled(Context context) {
        locationManager = (LocationManager) context
                .getSystemService(LOCATION_SERVICE);
        // Getting GPS status
        boolean isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        // Getting network status
        boolean isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return isGPSEnabled || isNetworkEnabled;
    }

    public static void getLocation(Context context, String id) {
        mContext = context;
        mId = id;
        try {
            determineLocation(context);
        } catch (Exception e) {
            Looper.prepare();
            determineLocation(context);
        }
    }

    private static void determineLocation(Context context) {


        SmartLocation.with(context).location()
                .oneFix()
                .start(new OnLocationUpdatedListener() {
                    @Override
                    public void onLocationUpdated(final Location location) {
                        if (location != null) {
                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    sendRequestToServer(location);
                                }
                            });
                            thread.start();
                        }
                    }
                });
    }


    /**
     * Function to show settings alert dialog.
     * On pressing the Settings button it will launch Settings Options.
     */
    public static void showSettingsAlert(final Context context) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing the Settings button.
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });

        // On pressing the cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }



    private static void sendRequestToServer(Location currentLocation) {
        LatLong currentCoordinates = new LatLong(currentLocation.getLatitude(), currentLocation.getLongitude());
        if (currentLocation != null) {
            Intent intent = new Intent(MainActivity.NOTIFICATION_INTENT);
            boolean isInsideTTK = RegionUtil.coordinateInRegion(new TTK(), currentCoordinates);
            if (isInsideTTK) {
                intent.putExtra(MainActivity.RESULT_KEY, "Зона 1");
                mContext.sendBroadcast(intent);
                sendRequest(1, currentCoordinates);
                Log.e(LOG_TAG, "isInsideTTK: " + isInsideTTK);
                return;
            }
            boolean isInsideMkad = RegionUtil.coordinateInRegion(new MKAD(), currentCoordinates);
            if (isInsideMkad) {
                intent.putExtra(MainActivity.RESULT_KEY, "Зона 2");
                mContext.sendBroadcast(intent);
                sendRequest(2, currentCoordinates);
                Log.e(LOG_TAG, "isInsideMkad: " + isInsideMkad);
                return;
            }
            intent.putExtra(MainActivity.RESULT_KEY, "Зона 3");
            mContext.sendBroadcast(intent);
            sendRequest(3, currentCoordinates);
        } else {
            Log.e(LOG_TAG, "Cannot determine location");
        }
    }

    private static void sendRequest(int zone, LatLong currentCoordinates) {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("id", mId));
        nameValuePairs.add(new BasicNameValuePair("zone", String.valueOf(zone)));
        nameValuePairs.add(new BasicNameValuePair("lat", String.valueOf(currentCoordinates.getLatitude())));
        nameValuePairs.add(new BasicNameValuePair("long", String.valueOf(currentCoordinates.getLongitude())));
        nameValuePairs.add(new BasicNameValuePair("timestamp", String.valueOf(System.currentTimeMillis())));
        HttpClient httpClient = new DefaultHttpClient();
        String paramsString = URLEncodedUtils.format(nameValuePairs, "UTF-8");
        HttpGet httpGet = new HttpGet(Cache.getLastServerAddress(mContext) + "?" + paramsString);
        try {
            HttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == 200) {
                Log.i(LOG_TAG, "Request send");
            } else {
                Log.e(LOG_TAG, "Request error");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }


}
