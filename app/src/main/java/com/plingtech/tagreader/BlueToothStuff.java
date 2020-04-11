package com.plingtech.tagreader;

import com.google.android.material.snackbar.Snackbar;
import com.plingtech.tagreader.MainActivity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.polidea.rxandroidble2.LogConstants;
import com.polidea.rxandroidble2.LogOptions;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.RxBleDeviceServices;
import com.polidea.rxandroidble2.RxBleScanResult;
import com.polidea.rxandroidble2.exceptions.BleScanException;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.polidea.rxandroidble2.scan.ScanSettings;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/*
Class for connecting the BT tag reader.
Uses constants for fixed data - Device Name, Service & Characteristic UUIDs
Uses Rxble for BT library.
Passes data back via updating adaptor from notification callbacks
Main methods
    Get Permissions
    Scan BT Devices
    Connect and Subscribe to Tag notifications
    Reconnect
    Misc handlers, error, cleanup, ...
 */

public class BlueToothStuff {
    private static BlueToothStuff INSTANCE = new BlueToothStuff();

    BlueToothStuff() {}
    public static BlueToothStuff getInstance() {
        return(INSTANCE);
    }

    private static final String TAG = "BT Stuff";
    private static Context context;

    //Rx BLE vars
    private Disposable permsDisposable;
    private Disposable scanDisposable;
    private Disposable connectionDisposable;
    private Disposable connStateDisposable;
    private RxBleClient rxBleClient;  //This is for>
    private RxBleDevice bleDevice;
    private UUID serviceUuid;

    // Tag Reader Device consts
    private static String tagDeviceName = "PlingTech Tag Reader";
    private static UUID tagsServiceUuid = java.util.UUID.fromString("506c696e-6720-5461-6720-526561646572");
    private static UUID tagsCharUuid = java.util.UUID.fromString("45494420-5461-6773-2053-63616e6e6564");

    // Get application context
    private static Context getContext() {
        return (FragmentActivity) MainActivity.getAppContext();
    }

    // Construct Singleton?
    public void BlueToothStuff () {}

    // Setup logging
    public void btLogging() {
        Log.i(TAG, "rxBLE logging setup");
        rxBleClient = RxBleClient.create(getContext());
        RxBleClient.updateLogOptions(new LogOptions.Builder()
                .setLogLevel(LogConstants.DEBUG)
                .setMacAddressLogSetting(LogConstants.MAC_ADDRESS_FULL)
                .setUuidsLogSetting(LogConstants.UUIDS_FULL)
                .setShouldLogAttributeValues(true)
                .setShouldLogScannedPeripherals(true)
                .build()
        );
    }

    // Get Permissions
    public void btPermissions() {
        Log.i(TAG, "starting permissions bit ");
        RxPermissions rxPermissions = new RxPermissions((FragmentActivity) getContext());
        rxPermissions.setLogging(true);
        permsDisposable = rxPermissions.request(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
                )
                .subscribe(granted -> {
                    if (granted) { // Always true pre-M
                        Log.i(TAG, "Permission granted ");
                    } else { // permission denied
                        Log.i(TAG, "Permission denied ");
                    }
                });
    }

    // Scan Devices
    public void scanBleDevices() {
        Log.d(TAG, "scanBleDevices() start");
        scanDisposable = rxBleClient.scanBleDevices(
                new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        //.setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH)
                        .build(),
                new ScanFilter.Builder()
                        //.setDeviceAddress("30:AE:A4:74:BE:AE")
                        .setDeviceName(tagDeviceName)
                        .build()
                )
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(this::disposeScan)
                //.subscribe(resultsAdapter::addScanResult, this::onScanFailure);
                .subscribe(this::processScanResult, this::onScanFailure);
        Log.d(TAG, "scanBleDevices() exit");
    }

    public RxBleDevice getBleDevice() {
        return bleDevice;
    }

    private void processScanResult(ScanResult result) {
        // Process scan result here.
        Log.i(TAG,"BLE Scan - something found");
        Log.i(TAG, "Scanned Device: "+result.getBleDevice());
        if (Objects.equals(result.getBleDevice().getName(), tagDeviceName)) {
            Log.i(TAG, "Scan: Tag Reader found");
            bleDevice = result.getBleDevice();
            connectionStateWatcher(); //Setup watcher for BLE connection state changes
            connectTagReader(); // Connect to reader
        }
    }

    private void connectionStateWatcher () {
        Log.i(TAG, "BLE connection state change setup");
        // Note: it is meant for UI updates only — one should not observeConnectionStateChanges() with BLE connection logic
        connStateDisposable = bleDevice.observeConnectionStateChanges()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onConnectionStateChange, this::onConnectionStateFailure);
    }

    // Connect & Subscribe
    private void connectTagReader() {
        Log.d(TAG, "BLE connectTagReader start");
        connectionDisposable = bleDevice.establishConnection(true)
                .flatMapSingle(RxBleConnection::discoverServices)
                .take(1) // Disconnect automatically after discovery
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(this::disposeConnection)
                .subscribe(this::subscribeToTagsRead, this::onConnectionFailure);
        Log.d(TAG, "BLE connectTagReader finish");
    }

    private void subscribeToTagsRead(RxBleDeviceServices services) {
        Log.d(TAG, "Entering subscribeToTagsRead()");
        Log.d(TAG, "Starting Subscribe on characteristic");
        if (isConnected()) {
            Log.i(TAG, "Connection exists. discard and reconnect?");
            disposeConnection();
        }
        connectionDisposable = bleDevice.establishConnection(true)
                .flatMap(rxBleConnection -> rxBleConnection.setupNotification(tagsCharUuid))
                //.doOnNext(notificationObservable -> runOnUiThread(this::notificationHasBeenSetUp))
                .flatMap(notificationObservable -> notificationObservable)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNotificationReceived, this::onNotificationSetupFailure
                );
        Log.d(TAG, "Finished Subscribe on characteristic");
    }

    private void onNotificationReceived(byte[] bytes) {
        Log.i(TAG, "Data from TagReader: "+ new String(bytes));
        //Snackbar.make(findViewById(R.id.content), "Change: " + HexString.bytesToHex(bytes), Snackbar.LENGTH_SHORT).show();
    }

    // Reconnect logic
    //TODO: Figure out how to do automagic reconnects (if library doesn't do it). Also if scan fails so re-run scan

    // OnErrors
    private void onScanFailure(Throwable throwable) {
        if (throwable instanceof BleScanException) {
            Log.d(TAG,"Scan Failure!: "+throwable);
            BleScanExceptionHandler.handleException((Activity) getContext(), (BleScanException) throwable);
        }
    }
    private void onConnectionFailure(Throwable throwable) {
        //noinspection ConstantConditions
        Log.i(TAG, "BT Connection error: "+throwable);
        //Snackbar.make(findViewById(android.R.id.content), "Connection error: " + throwable, Snackbar.LENGTH_SHORT).show();
    }

    private void onConnectionStateFailure(Throwable throwable) {
        Log.d(TAG, "Error setting upp connection watcher: "+throwable);
    }

    private void onNotificationSetupFailure(Throwable throwable) {
        Log.i(TAG, "Notifications error: " + throwable);
        //Snackbar.make(findViewById(R.id.content), "Notifications error: " + throwable, Snackbar.LENGTH_SHORT).show();
    }

    // Helpers
    public void stopBleScan() {
       disposeScan();
    }
    private void onConnectionStateChange(RxBleConnection.RxBleConnectionState newState) {
        Log.i(TAG, "BLE Connection state changed to: "+newState.toString());
    }

    private void notificationHasBeenSetUp() {
        Log.i(TAG,"Notifications have been set up!!");
        //Snackbar.make(, "Notifications has been set up", Snackbar.LENGTH_SHORT).show();
    }

    private boolean isScanning() {
        return scanDisposable != null;
    }

    private boolean isConnected() {
        return bleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED;
    }

    // Cleanup
    public void cleanup() {
        disposePerms();
        disposeScan();
        disposeConnection();
        disposeConnectionWatcher();
    }

    private void disposePerms() {
        if (permsDisposable !=null) {
            Log.d(TAG,"disposing perms");
            permsDisposable.dispose();
        }
    }

    private void disposeScan() {
        if (isScanning()) {
            Log.d(TAG,"disposing scan");
            scanDisposable.dispose();
        }
    }

    private void disposeConnection() {
        if (connectionDisposable != null) {
            Log.d(TAG,"disposing Connection");
            connectionDisposable.dispose();
        } else {
            Log.d(TAG,"Connection already disposed?");
        }
    }

    private void disposeConnectionWatcher() {
        if (connStateDisposable !=null) {
            Log.d(TAG,"disposing Watcher");
            connStateDisposable.dispose();
        }
    }


    //Unused?
    /*
    @SuppressWarnings("unused")
    private void onConnectionReceived(RxBleConnection connection) {
        //noinspection ConstantConditions
        Log.i(TAG,"BLE connection received");
        Snackbar.make(findViewById(android.R.id.content), "Connection received", Snackbar.LENGTH_SHORT).show();
        final Disposable disposable = bleDevice.establishConnection(true)
                .flatMapSingle(RxBleConnection::discoverServices)
                //.take(1) // Disconnect automatically after discovery
                .observeOn(AndroidSchedulers.mainThread())
                //.doFinally(this::updateUI)
                .subscribe(this::subscribeToTagsRead, this::onConnectionFailure);
        servicesDisposable.add(disposable);
    }
    */
    private void discoverServicesAndCharacteristics(RxBleDeviceServices services) {
        if (isConnected()) {
            Log.d(TAG, "Connected?: " + isConnected() + " Entering service loop");
            for (BluetoothGattService service : services.getBluetoothGattServices()) {
                serviceUuid = service.getUuid();
                Log.i(TAG, "Service Type: " + getServiceType(service) + "Service UUID: " + serviceUuid);
                Log.i(TAG, "Target Service = " + tagsServiceUuid);
                Log.d(TAG, "Service Match?: " + (tagsServiceUuid == serviceUuid));
                if (serviceUuid == tagsServiceUuid) {
                    Log.d(TAG, "Found Tags Service - getting Characteristics");
                    final List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                    Log.d(TAG, "Entering Characteristic loop");
                    for (BluetoothGattCharacteristic characteristic : characteristics) {
                        Log.i(TAG, "Characteristic props: " + describeProperties(characteristic));
                        Log.i(TAG, "Characteristc UUID: " + characteristic.getUuid());
                        if (characteristic.getUuid() == tagsCharUuid) {
                            Log.i(TAG, "Tag Characteristic exists on connected device");
                        }
                    }
                }
            }
            Log.d(TAG, "Finished service & characteristics loop");
        }
    }

    private String describeProperties(BluetoothGattCharacteristic characteristic) {
        List<String> properties = new ArrayList<>();
        if (isCharacteristicReadable(characteristic)) properties.add("Read");
        if (isCharacteristicWriteable(characteristic)) properties.add("Write");
        if (isCharacteristicNotifiable(characteristic)) properties.add("Notify");
        return TextUtils.join(" ", properties);
    }

    private String getServiceType(BluetoothGattService service) {
        return service.getType() == BluetoothGattService.SERVICE_TYPE_PRIMARY ? "primary" : "secondary";
    }

    private boolean isCharacteristicNotifiable(BluetoothGattCharacteristic characteristic) {
        return (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
    }

    private boolean isCharacteristicReadable(BluetoothGattCharacteristic characteristic) {
        return ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) != 0);
    }

    private boolean isCharacteristicWriteable(BluetoothGattCharacteristic characteristic) {
        return (characteristic.getProperties() & (BluetoothGattCharacteristic.PROPERTY_WRITE
                | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) != 0;
    }

}
