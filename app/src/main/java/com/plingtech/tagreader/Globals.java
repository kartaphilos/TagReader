package com.plingtech.tagreader;

import android.accounts.Account;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.SharedPreferences;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Globals extends Application {

	public final static String TAG = "TagReader";
	public String filesDir = "";
	public SharedPreferences prefs = null;
	BluetoothManager btManager;
	BluetoothAdapter btAdapter;
	BluetoothLeScanner btScanner;
	private final static int REQUEST_ENABLE_BT = 1;
	private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

	//Boolean btScanning = false;
	//int deviceIndex = 0;
	//ArrayList<BluetoothDevice> devicesDiscovered = new ArrayList<BluetoothDevice>();
	BluetoothGatt bluetoothGatt;

	public final static String ACTION_GATT_CONNECTED =
			"com.example.bluetooth.le.ACTION_GATT_CONNECTED";
	public final static String ACTION_GATT_DISCONNECTED =
			"com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
	public final static String ACTION_GATT_SERVICES_DISCOVERED =
			"com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
	public final static String ACTION_DATA_AVAILABLE =
			"com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
	public final static String EXTRA_DATA =
			"com.example.bluetooth.le.EXTRA_DATA";
	//public Map<String, String> uuids = new HashMap<String, String>();

	//private Handler mHandler = new Handler();
	// Stops scanning after 5 seconds.
	private static final long SCAN_PERIOD = 5000;

	private static final String googleAccountName = "matt.devtest72@gmail.com";
    private static final String customerGroupName = "Customers";
    protected boolean pagingEnabled = true;
	//protected static String DATABASE_NAME; // "equisenses_dev";
  	//protected static String DATABASE_URL; // "http://equisenses.iriscouch.com/" + DATABASE_NAME;
	
	@Override
    public void onCreate() {
        //Context ctx = getApplicationContext();
		//TODO - get some from SharedPrefs		
		super.onCreate();
    }
	
	protected String GetGoogleAccountName() {
		return googleAccountName;
	}
	
	protected String GetCustomerGroupName() {
		return customerGroupName;
	}
}
