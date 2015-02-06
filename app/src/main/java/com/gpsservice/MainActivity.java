package com.gpsservice;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    public static final String NOTIFICATION_INTENT = "notification";
    public static final String RESULT_KEY = "result";
    private Button startButton;
    private Button stopButton;
    private NotificationReceiver notificationReceiver;
    private AlertDialog mMockDialog;

    //for test
//    public static String mCustomLatitude;
//    public static String mCustomLongitude;

    @Override
    protected void onStart() {
        super.onStart();
        if (notificationReceiver == null) notificationReceiver = new NotificationReceiver();
        IntentFilter intentFilter = new IntentFilter(NOTIFICATION_INTENT);
        registerReceiver(notificationReceiver, intentFilter);

//        if (isMockLocationEnabed()) {
//            if (mMockDialog == null) {
//                mockDialogShow();
//            }
//        } else {
//            if (mMockDialog != null) {
//                mMockDialog.dismiss();
//                mMockDialog = null;
//            }
//        }
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

        final EditText idEditText = (EditText) findViewById(R.id.id);

        //only for testing
//        final EditText latEditText = (EditText) findViewById(R.id.latitude);
//        final EditText longEditText = (EditText) findViewById(R.id.longitude);

        stopButton = (Button) findViewById(R.id.stop);
        startButton = (Button) findViewById(R.id.start);

        if (isMyServiceRunning()) {
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
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.required_fields), Toast.LENGTH_LONG).show();
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(MainActivity.this, GpsService.class));
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
            }
        });
    }

    private void startGpsService(final String id) {

        Location currentLocation = GPSTracker.getLocation(this);
        if (currentLocation != null) {
//            Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + currentLocation.getLatitude() + "\nLong: " + currentLocation.getLongitude(), Toast.LENGTH_LONG).show();
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(MainActivity.this, GpsService.class);
                    intent.putExtra(GpsService.KEY_ID, id);
//                    if (!mCustomLatitude.isEmpty() && !mCustomLongitude.isEmpty()) {
//                        intent.putExtra(GpsService.KEY_CUSTOM_LOCATION, true);
//                    } else {
//                        intent.putExtra(GpsService.KEY_CUSTOM_LOCATION, false);
//                    }
                    startService(intent);
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
                finish();
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

    private boolean isMockLocationEnabed() {
        if (Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ALLOW_MOCK_LOCATION).equals("0"))
            return false;
        else return true;
    }

    private class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(NOTIFICATION_INTENT)) {
//                Toast.makeText(MainActivity.this, intent.getStringExtra(RESULT_KEY), Toast.LENGTH_LONG).show();
            }
        }
    }
}
