package com.plingtech.tagreader;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.jakewharton.rx.ReplayingShare;
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
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

import static java.util.concurrent.TimeUnit.SECONDS;


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
    private static final String TAG = "BT Stuff";
    private TagsListFragment ctxFrag;
    private FragmentActivity mActivity;
    public MainActivity ma;

    BlueToothStuff(MainActivity activity) {
        ma = activity;
    }

    // What was this stuff for???
    //private static BlueToothStuff INSTANCE = new BlueToothStuff();
    //public static BlueToothStuff getInstance() { return(INSTANCE); }


    //Rx BLE vars
    private Disposable permsDisposable;
    private Disposable scanDisposable;
    private Disposable connectionDisposable;
    private Disposable connStateDisposable;
    private Disposable notificationDisposable;
    private CompositeDisposable bigDisposable = new CompositeDisposable();
    private Observable<RxBleConnection> connectionObservable;
    private RxBleClient rxBleClient;
    private RxBleDevice bleDevice = null;
    private RxBleConnection.RxBleConnectionState currentConnState = RxBleConnection.RxBleConnectionState.DISCONNECTED;

    //BT STATUS CONSTS
    public static final int BT_OFF = -1;
    public static final int BT_DISCONNECTED = 0;
    public static final int BT_CONNECTED = 1;
    public static final int BT_CONNECTING = 2;

    // Tag Reader Device consts
    private static String tagDeviceName = "PlingTech Tag Reader";
    private static UUID tagsServiceUuid = java.util.UUID.fromString("506c696e-6720-5461-6720-526561646572");
    private static UUID tagsCharUuid = java.util.UUID.fromString("45494420-5461-6773-2053-63616e6e6564");

    // Get application context
    private static Context getContext() {
        return (FragmentActivity) MainActivity.getAppContext();
    }

    // Various getters of infos
    //Device details
    public RxBleDevice getBleDevice() {
        return bleDevice;
    }
    public String getBleDeviceName() {
        return bleDevice.getName();
    }
    public String getBleDeviceMac() {
        return bleDevice.getMacAddress();
    }
    // Connection State
    public RxBleConnection.RxBleConnectionState getCurrentConnState() {
        return currentConnState;
    }

    // RSSI
    public RxBleConnection getDeviceRssi() {
        return null;
    }


    // Construct Singleton?
    //public void BlueToothStuff () {}

    // Setup logging
    void btLogging() {
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
    void btPermissions() {
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
    void scanBleDevices(TagsListFragment ctx) {
        Log.d(TAG, "scanBleDevices() start");
        ctxFrag = ctx;
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
                .subscribe(this::processScanResult, this::onScanFailure);
        Log.d(TAG, "scanBleDevices() exit");
    }

    private void processScanResult(ScanResult result) {
        // Process scan result here.
        //Log.d(TAG,"BLE Scan - Found a BT thingy: "+result.getBleDevice());
        if (Objects.equals(result.getBleDevice().getName(), tagDeviceName)) {
            Log.i(TAG, "Scan: Tag Reader found");
            if (bleDevice == null) {
                bleDevice = result.getBleDevice();
                Log.i(TAG, "Scan: Tag Reader Detail: "+result.toString());
                connectionStateWatcher(); //Setup watcher for BLE connection state changes
                connectTagReader(); // Connect to reader
            } else {
                Log.i(TAG, "Settle Down! - already discovered Tag Reader");
            }
        }
    }

    private void connectionStateWatcher () {
        Log.i(TAG, "BLE connection state change setup started");
        // Note: it is meant for UI updates only — one should not observeConnectionStateChanges() with BLE connection logic
        connStateDisposable = bleDevice.observeConnectionStateChanges()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onConnectionStateChange,
                           this::onConnectionStateFailure);
    }

    // Connect & Subscribe
    private void connectTagReader() {
        Log.d(TAG, "BLE connectTagReader start");
        connectionObservable =  bleDevice.establishConnection(true).compose(ReplayingShare.instance());

        Disposable disp1 = connectionObservable
                .flatMap(rxBleConnection -> rxBleConnection.setupNotification(tagsCharUuid))
                .flatMap(notificationObservable -> notificationObservable)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNotificationReceived, this::onNotificationSetupFailure);
                //.doFinally(this::disposeConnection)
                //.subscribe(this::subscribeToTagsRead, this::onConnectionFailure);
        bigDisposable.add(disp1);

        Disposable disp2 = connectionObservable
                .flatMap(rxBleConnection -> // Set desired interval.
                        Observable.interval(1, SECONDS).flatMapSingle(sequence -> rxBleConnection.readRssi()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::updateRssi, this::onConnectionFailure);
        bigDisposable.add(disp2);

        Log.d(TAG, "BLE connectTagReader finish");
    }

    private void updateRssi(Integer rssi) {
        Log.d(TAG, "RSSI: "+rssi);
    }

    private void onNotificationReceived(byte[] bytes) {
        //String tag = Arrays.toString(bytes);  // This don't work!
        String tag = new String(bytes);  // Must use this to convert array of ascii nums correctly
        Log.i(TAG, "Data from TagReader - Size: "+bytes.length+" Tag: "+tag);
        ctxFrag.tagItemDataBuild(tag);
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
        Log.i(TAG, "BT Connection error: "+throwable);
    }

    private void onConnectionStateFailure(Throwable throwable) {
        Log.d(TAG, "Error setting up connection watcher: "+throwable);
        ma.displayBtStatus(currentConnState);
    }

    private void onNotificationSetupFailure(Throwable throwable) {
        //if (throwable instanceof Excep) {
        //    Log.d(TAG,"Scan Failure!: "+throwable);
        //    BleScanExceptionHandler.handleException((Activity) getContext(), (BleScanException) throwable);
        //}
        Log.d(TAG, "Notifications error: " + throwable);
        ma.displayBtStatus(currentConnState);
    }

    // Helpers
    void stopBleScan() {
       disposeScan();
    }

    private void onConnectionStateChange(RxBleConnection.RxBleConnectionState newState) {
        Log.i(TAG, "BLE Connection state changed to: "+newState.toString()+
                        " (Was: "+currentConnState.toString()+")");
        if (newState != currentConnState) {
            Log.d(TAG, "Changing BT Icon to: "+newState.toString());
            currentConnState = newState;
            ma.displayBtStatus(currentConnState);
        }
    }

    private void notificationHasBeenSetUp(Observable<byte[]> observable) {
        Log.i(TAG,"Notifications have been set up!!");
        Toast.makeText(ma,"Subscribed to Tag Reader updates",Toast.LENGTH_SHORT).show();
    }

    private boolean isScanning() {
        return scanDisposable != null;
    }

    private boolean isConnected() {
        return bleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED;
    }

    // Cleanup
    void cleanup() {
        Log.i(TAG, "Cleanning up disposables");
        disposePerms();
        disposeScan();
        disposeConnection();
        disposeConnectionWatcher();
        disposeNotification();
    }

    private void disposePerms() {
        if (permsDisposable !=null) {
            Log.d(TAG,"disposing perms");
            permsDisposable.dispose();
        } else {
            Log.d(TAG, "Perms already disposed");
        }
    }

    private void disposeScan() {
        if (isScanning()) {
            Log.d(TAG,"disposing scan");
            scanDisposable.dispose();
        } else {
            Log.d(TAG, "Scan already disposed");
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
        } else {
            Log.d(TAG, "Watcher already disposed");
        }
    }

    private void disposeNotification() {
        if (notificationDisposable !=null) {
            Log.d(TAG,"disposing Notification");
            notificationDisposable.dispose();
        } else {
            Log.d(TAG, "Notifications already disposed");
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

    private void discoverServicesAndCharacteristics(RxBleDeviceServices services) {
        if (isConnected()) {
            Log.d(TAG, "Connected?: " + isConnected() + " Entering service loop");
            for (BluetoothGattService service : services.getBluetoothGattServices()) {
                UUID serviceUuid = service.getUuid();
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
    */

    /*
    private void subscribeToTagsRead(RxBleDeviceServices services) throws InterruptedException {
      Log.d(TAG, "Entering subscribeToTagsRead()");
      while (bleDevice.getConnectionState() != RxBleConnection.RxBleConnectionState.DISCONNECTED) {
          Log.d(TAG, "Connection exists. discard and reconnect?");
          Thread.sleep(100);
      }
      Log.d(TAG, "No Connection - start notification subscribe");
      notificationDisposable = bleDevice.establishConnection(true)
              .flatMap(rxBleConnection -> rxBleConnection.setupNotification(tagsCharUuid))
              .doOnNext(this::notificationHasBeenSetUp)
              .flatMap(notificationObservable -> notificationObservable)
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(this::onNotificationReceived, this::onNotificationSetupFailure)
      ;
      Log.d(TAG, "Finished Subscribe on characteristic");
    }
   */

    /*
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
    */

}
