package com.bletest.blemodule;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.AdvertisingSet;
import android.bluetooth.le.AdvertisingSetCallback;
import android.bluetooth.le.AdvertisingSetParameters;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Intent;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Handler;


public class BLEModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private final BLEManager bleManager;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BLEModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        bleManager = BLEManager.getInstance(reactContext);
    }


    @NonNull
    @Override
    public String getName() {
        return "BLEModule";
    }

    @ReactMethod
    public void showToast(String message) {
        Toast.makeText(getReactApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @ReactMethod
    public void getDevices() {
        bleManager.getDevices();
    }

    @ReactMethod
    public void advertise(String serviceUUID) {
        bleManager.advertise(serviceUUID);
    }

    @ReactMethod
    public void startBLEScan(String serviceUUID) {
        bleManager.startScan(serviceUUID);
    }

    @ReactMethod
    public void stopBLEScan() {
        bleManager.stopScan();
    }


    @ReactMethod
    private void startBLEService(String serviceUUID) {
        Intent sIntent = new Intent(this.reactContext, BLEForegroundService.class);
        sIntent.putExtra("serviceUUID", serviceUUID);
        this.reactContext.startService(sIntent);
    }

    @ReactMethod
    public void stopBLEService() {
        this.reactContext.stopService(new Intent(this.reactContext, BLEForegroundService.class));
    }

}
