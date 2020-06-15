package com.plingtech.tagreader;

import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.plingtech.tagreader.databinding.ActivityMainBinding;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "TagMainActivity";
    private ActivityMainBinding binding;
    public BlueToothStuff bt;
    public ClipboardManager cm;
    //public Menu menuOptions;
    RxBleConnection.RxBleConnectionState btStatus = null; //RxBleConnection.RxBleConnectionState.DISCONNECTED;
    //public ScannedTagsAdapter adapter;
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
        Log.d(TAG, "Inflating Main Activity with binding");
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        Log.d(TAG, "binding.getRoot");
        View view = binding.getRoot();
        Log.d(TAG, "setView");
        setContentView(view);
        Log.d(TAG, "binding.toolbar");
        setSupportActionBar(binding.toolbar);

        // Start of Logic
        Log.d(TAG, "getActivity context?");
        MainActivity.context = this;
        cm = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);;


        Log.d(TAG, "Create btStuff singleton");
        bt = new BlueToothStuff(this);
        Log.d(TAG, "Setup rxBle Logging");
        bt.btLogging();
        Log.d(TAG, "Get permissions"); //TODO: Its an observable so perms won't complete for a long time. Will fail on new app
        bt.btPermissions();
        //Log.d(TAG, "Scan, connect, subscribe, repeat");
        //bt.connectTagReader2();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        Log.d("CreateOptionsMenu","Menu inflated");
        //menuOptions = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String TAG = "OptionsMenuClicked";
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.bt_status:
                Log.d(TAG,"BT Status clicked");
                //Open PopUp for connect/disconnect/RSSI/MAC/etc - time connected
                DialogFragment newFragment = new BlueToothStatusFragment();
                newFragment.show(getSupportFragmentManager(), "bt");
                return true;
            case R.id.copy_session:
                Log.d(TAG,"Copy clicked");
                //Copy clipboard items
                return true;
            case R.id.debug:
                Log.d(TAG,"Debug clicked");

                // Display debug info - what to show?
            default:
                Log.d(TAG,"Nothing in this part of the menu");
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        //set BT Icon based on global var
        Log.d("PrepareMenu", "menu = "+menu);
        Log.d("PrepareMenu", "btStatus = "+btStatus);
        if (btStatus != null) {
            Log.d("PrepareMenu", "find BT menu item");
            MenuItem bt = menu.findItem(R.id.bt_status);
            Log.d("PrepareMenu", "set BT icon");
            bt.setIcon(setBtIcon(menu));
            Log.d("PrepareMenu", "BT icon set to: "+menu.findItem(R.id.bt_status).getIcon());
        }
        return super.onPrepareOptionsMenu(menu);
    }

    public void displayBtStatus(RxBleConnection.RxBleConnectionState status) {
        Log.d(TAG, "Displaying BT status change Toast: " + status.toString());
        Toast.makeText(getApplicationContext(), status.toString(), Toast.LENGTH_SHORT).show();
        btStatus = status;
        Log.d(TAG, "Refreshing Menu");
        invalidateOptionsMenu();
    }

    private int setBtIcon(Menu menu) {
        Log.d(TAG, "Setting BT Icon");
        //MenuItem btIcon = menu.getItem(R.id.bt_status);
        int btIcon;
        Log.d(TAG, "btStatus = "+btStatus);
        switch (btStatus) {
                case DISCONNECTED:
                case DISCONNECTING:
                    Log.d(TAG, "Setting BT icon DISCONNECTED");
                    btIcon = android.R.drawable.stat_sys_data_bluetooth;
                    break;
                case CONNECTED:
                    Log.d(TAG, "Setting BT icon CONNECTED");
                    btIcon = R.drawable.ic_bt_connected;
                    break;
                case CONNECTING:
                    Log.d(TAG, "Setting BT icon CONNECTING");
                    btIcon = R.drawable.ic_bt_connecting;
                    break;
                default:
                    Log.d(TAG, "Setting BT icon DISABLED");
                    btIcon =R.drawable.ic_bt_disabled;
                    break;
        }
        return btIcon;
    }

    // Code to double back tap to exit (avoiding accidental exits)
    private static final int TIME_INTERVAL = 2000; // # milliseconds, desired time passed between two back presses.
    private long backPressed;
    private Toast exitToast;

    @Override
    public void onBackPressed()
    {
        if (backPressed + TIME_INTERVAL > System.currentTimeMillis())
        {
            exitToast.cancel();
            super.onBackPressed();
            return;
        } else {
            backPressed = System.currentTimeMillis();
            exitToast = Toast.makeText(getBaseContext(), "Tap back again to exit", Toast.LENGTH_SHORT);
            exitToast.show();
        }
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
