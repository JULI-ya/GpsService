package com.gpsservice;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final EditText serverAddressEditText = (EditText) findViewById(R.id.server_address);
        final EditText intervalEditText = (EditText) findViewById(R.id.interval);

        if (!Cache.getLastServerAddress(this).isEmpty()) {
            serverAddressEditText.setText(Cache.getLastServerAddress(this));
        }
        intervalEditText.setText(String.valueOf(Cache.getLastSavedInterval(this)));

        Button saveButton = (Button) findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String serverAddress = serverAddressEditText.getText().toString().trim();
                String interval = intervalEditText.getText().toString().trim();
                Cache.saveServerAddress(serverAddress, SettingsActivity.this);
                Cache.saveInterval(Integer.parseInt(interval), SettingsActivity.this);
                Toast.makeText(SettingsActivity.this, "Данные сохранены", Toast.LENGTH_LONG).show();
            }
        });
    }
}
