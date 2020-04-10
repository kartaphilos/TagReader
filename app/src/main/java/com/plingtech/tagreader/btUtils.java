package com.plingtech.tagreader;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class btUtils {

    private static btUtils INSTANCE = null;

    //private static BluetoothManager btManager;
    private static BluetoothAdapter btAdapter;
    private static BluetoothLeScanner btScanner;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private Boolean btScanning = false;
    private int deviceIndex = 0;
    ArrayList<BluetoothDevice> devicesDiscovered = new ArrayList<BluetoothDevice>();
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
    public Map<String, String> uuids = new HashMap<String, String>();
    // Stops scanning after 5 seconds.
    private Handler mHandler = new Handler();
    private static final long SCAN_PERIOD = 5000;

    private btUtils() {};
    public static btUtils getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new btUtils();
        }
        return(INSTANCE);
    }

    public void startBT() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();
        checkPermissions();

    }

    private void checkPermissions() {
        //BT off so request on. Do popup here? Need context?
        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //ctx.startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        // Make sure we have access coarse location enabled, if not, prompt the user to enable it
        /*if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        }
        */

    }
    //client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

    // Device scan callback.
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            devicesDiscovered.add(result.getDevice());
            deviceIndex++;
        }
    };

    // Device connect call back
    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation
            final String charUuid = characteristic.getUuid().toString();
            final String charValue = characteristic.getStringValue(0);
            System.out.println("Data: "+charValue+" from UUID: "+charUuid);
        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            // this will get called when a device connects or disconnects
            System.out.println(newState);
            switch (newState) {  // Change to an if?  return int of state
                case 0: // Device disconnected
                    break;
                case 2: // Device Connected
                    // discover services and characteristics for this device
                    bluetoothGatt.discoverServices();
                    break;
                default: // Unknown state...
                    break;
            }
        }


            @Override
            public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
                // this will get called after the client initiates a BluetoothGatt.discoverServices() call
                System.out.println("Device services discovered\n");
                displayGattServices(bluetoothGatt.getServices());
            }

            @Override
            // Result of a characteristic read operation
            public void onCharacteristicRead(BluetoothGatt gatt,
                                             BluetoothGattCharacteristic characteristic,
                                             int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                }
            }
        };

        private void broadcastUpdate(final String action,
                                     final BluetoothGattCharacteristic characteristic) {

            System.out.println(characteristic.getUuid());
            System.out.println(characteristic.getValue());
        }

        //@Override
        public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
            switch (requestCode) {
                case PERMISSION_REQUEST_COARSE_LOCATION: {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        System.out.println("coarse location permission granted");
                    } else {
                        /*final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Functionality limited");
                        builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                            @Override
                            public void onDismiss(DialogInterface dialog) {
                            }

                        });
                        builder.show();

                        */
                    }
                    return;
                }
            }
        }

        public void startScanning() {
            System.out.println("start scanning");
            btScanning = true;
            deviceIndex = 0;
            devicesDiscovered.clear();
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    btScanner.startScan(leScanCallback);
                }
            });

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopScanning();
                }
            }, SCAN_PERIOD);
        }

        public void stopScanning() {
            System.out.println("stopping scanning");
            btScanning = false;
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    btScanner.stopScan(leScanCallback);
                }
            });
        }

        public void connectToDeviceSelected() {
            System.out.println("Trying to connect to device\n");
            //int deviceSelected = Integer.parseInt(deviceIndexInput.getText().toString());
            //bluetoothGatt = devicesDiscovered.get(deviceSelected).connectGatt(this, false, btleGattCallback);
        }

        public void disconnectDeviceSelected() {
            System.out.println("Disconnecting from device\n");
            bluetoothGatt.disconnect();
        }

        private void displayGattServices(List<BluetoothGattService> gattServices) {
            if (gattServices == null) return;

            // Loops through available GATT Services.
            for (BluetoothGattService gattService : gattServices) {

                final String uuid = gattService.getUuid().toString();
                System.out.println("Service discovered: " + uuid);
                new ArrayList<HashMap<String, String>>();
                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();

                // Loops through available Characteristics.
                for (BluetoothGattCharacteristic gattCharacteristic :
                        gattCharacteristics) {

                    final String charUuid = gattCharacteristic.getUuid().toString();
                    final String charValue = gattCharacteristic.getStringValue(0);
                    System.out.println("Characteristic discovered for service: " + charUuid+" with Value: <"+charValue+">");
                    // Various settings from SO threads https://stackoverflow.com/questions/27068673/subscribe-to-a-ble-gatt-notification-android
                    bluetoothGatt.setCharacteristicNotification(gattCharacteristic, true);
                    gattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    // 0x2902 org.bluetooth.descriptor.gatt.client_characteristic_configuration.xml
                    UUID x2902 = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
                    BluetoothGattDescriptor descriptor = gattCharacteristic.getDescriptor(x2902);
                    System.out.println("Characteristic descriptor: <"+descriptor+">");
                    if (descriptor != null) {
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        bluetoothGatt.writeDescriptor(descriptor);
                    }
                }
            }
        }


        // Was in OnStart()   AppIndex.AppIndexApi.start(client, viewAction);



    // Were in onStop()
    // AppIndex.AppIndexApi.end(client, viewAction);
    // client.disconnect();
}




