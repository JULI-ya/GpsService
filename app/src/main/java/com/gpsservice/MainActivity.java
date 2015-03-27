package com.gpsservice;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gpsservice.alarm.AlarmReceiver;


public class MainActivity extends ActionBarActivity {

    public static final String NOTIFICATION_INTENT = "notification";
    public static final String RESULT_KEY = "result";
    public static final String IS_MOCK = "is_mock";
    private Button startButton;
    private Button stopButton;
    private NotificationReceiver notificationReceiver;
    private AlertDialog mMockDialog;
    private PendingIntent pendingIntent;

    //for test
//    public static String mCustomLatitude;
//    public static String mCustomLongitude;

    @Override
    protected void onStart() {
        super.onStart();
        if (notificationReceiver == null) notificationReceiver = new NotificationReceiver();
        IntentFilter intentFilter = new IntentFilter(NOTIFICATION_INTENT);
        registerReceiver(notificationReceiver, intentFilter);

        checkForMock();
    }

    private void checkForMock() {
        if (isMockLocationEnabled()) {
            if (mMockDialog == null) {
                mockDialogShow();
            }
        } else {
            if (mMockDialog != null) {
                mMockDialog.dismiss();
                mMockDialog = null;
            }
        }
    }

    private void mockDialogShow() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Внимание!")
                .setMessage("Приложение с mock location не работает")
                .setCancelable(false);
        mMockDialog = builder.create();
        mMockDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notificationReceiver != null) unregisterReceiver(notificationReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent alarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, 0);

        final EditText idEditText = (EditText) findViewById(R.id.id);

        //only for testing
//        final EditText latEditText = (EditText) findViewById(R.id.latitude);
//        final EditText longEditText = (EditText) findViewById(R.id.longitude);

        stopButton = (Button) findViewById(R.id.stop);
        startButton = (Button) findViewById(R.id.start);

        if (Cache.isAlarmManagerWorking(this)) {
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
        }

        if (!Cache.getLastIdt(this).isEmpty()) {
            idEditText.setText(Cache.getLastIdt(this));
        }

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = idEditText.getText().toString().trim();
//                mCustomLatitude = latEditText.getText().toString().replace(',', '.');
//                mCustomLongitude = longEditText.getText().toString().replace(',', '.');
                if (!id.isEmpty()) {
                    Cache.saveId(id, MainActivity.this);
                    startGpsService(id);
                    showNotification(true);
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.required_fields), Toast.LENGTH_LONG).show();
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopClick();
            }
        });
    }

    private void stopClick() {
        stop();
//                stopService(new Intent(MainActivity.this, GpsService.class));
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        showNotification(false);
    }

    public void start() {
        Cache.setAlarmManagerWorking(true, this);
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int interval = Cache.getLastSavedInterval(this) * 60 * 1000;

        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);

        Looper.prepare();
    }

    public void stop() {
        Cache.setAlarmManagerWorking(false, this);
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
    }

    private void showNotification(boolean isRunning) {
        String title;
        if (isRunning) {
            title = getString(R.string.service_running);
        } else {
            title = getString(R.string.service_stoped);
        }
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(title);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(Application.FM_NOTIFICATION_ID, builder.build());
    }

    private void startGpsService(final String id) {

//        Location currentLocation = GPSTracker.getLocation(this, id);
        if (GPSTracker.isProvidersEnabled(this)) {
//            Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + currentLocation.getLatitude() + "\nLong: " + currentLocation.getLongitude(), Toast.LENGTH_LONG).show();
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(MainActivity.this, GpsService.class);
                    intent.putExtra(Cache.KEY_ID, id);
//                    if (!mCustomLatitude.isEmpty() && !mCustomLongitude.isEmpty()) {
//                        intent.putExtra(GpsService.KEY_CUSTOM_LOCATION, true);
//                    } else {
//                        intent.putExtra(GpsService.KEY_CUSTOM_LOCATION, false);
//                    }
//                    startService(intent);
                    start();
                }
            });
            thread.start();
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
        } else {
            // Can't get location.
            // GPS or network is not enabled.
            // Ask user to enable GPS/network in settings.
            GPSTracker.showSettingsAlert(this);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            case R.id.quit:
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(Application.FM_NOTIFICATION_ID);
                stopService(new Intent(MainActivity.this, GpsService.class));
                Cache.setAlarmManagerWorking(false, this);
                finish();
                android.os.Process.killProcess(android.os.Process.myPid());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (GpsService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private boolean isMockLocationEnabled() {
        if (Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ALLOW_MOCK_LOCATION).equals("0"))
            return false;
        else return true;
    }

    private class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(NOTIFICATION_INTENT)) {
                if (intent.getBooleanExtra(IS_MOCK, false)) {
                    checkForMock();
                    stopClick();
                } else {
                Toast.makeText(MainActivity.this, intent.getStringExtra(RESULT_KEY), Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
