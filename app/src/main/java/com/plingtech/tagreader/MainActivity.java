package com.plingtech.tagreader;

import android.content.Context;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.polidea.rxandroidble2.RxBleClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    BlueToothStuff bt;
    private static final String TAG = "TagMainActivity";
    private static Context context;
    RxBleClient rxBleClient;

    public static Context getAppContext() {
        return MainActivity.context;
    }

    // Singleton of rxBleClient ?
    public  RxBleClient getRxBleClient(Context context) {
        MainActivity activity = MainActivity.this;
        return activity.rxBleClient;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Scanned Tags copied to clipboard", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        Log.d(TAG, "getActivity context?");
        MainActivity.context = this;
        Log.d(TAG, "Create btStuff singleton");
        bt = new BlueToothStuff();
        Log.d(TAG, "Setup rxBle Logging");
        bt.btLogging();
        Log.d(TAG, "Get permissions");
        bt.btPermissions();
        Log.d(TAG, "Start BLE scan & connect");
        //TODO: Make observable and subcsribe to result for device &/or connection
        bt.scanBleDevices();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        bt.stopBleScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bt.cleanup();
    }
}
