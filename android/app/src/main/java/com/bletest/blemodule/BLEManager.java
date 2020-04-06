package com.bletest.blemodule;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BLEManager {

    private static final String TAG = "BLETest";
    private BluetoothAdapter bluetoothAdapter;
    Set <BluetoothDevice> devicesSet = new HashSet<>();
    ReactContext context;
    private static BLEManager sBLEManagerInstance;

    private BLEManager(ReactContext context){
        this.context = context;
        init();
    }

    public static BLEManager getInstance(){
        if (sBLEManagerInstance != null){
            return sBLEManagerInstance;
        }
        return null;
    }

    public static BLEManager getInstance(ReactContext context){
        if (sBLEManagerInstance == null){
            sBLEManagerInstance = new BLEManager(context);
        }
        return sBLEManagerInstance;
    }

    public void init(){
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothAdapter.enable();
    }

    public void getDevices(){
        WritableMap params = Arguments.createMap();
        for(BluetoothDevice device: devicesSet){
            params.putString("device_name", device.getName());
            params.putString("device_address", device.getAddress());
        }
        context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("foundDevices",params);
    }

    private BluetoothAdapter.LeScanCallback leScanCallbackStart = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            WritableMap params = Arguments.createMap();
            params.putString("device_name", device.getName());
            params.putString("device_address", device.getAddress());
            context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("foundDevices",params);
            devicesSet.add(device);
        }
    };

   public void startScan(String serviceUUID){
       int state = bluetoothAdapter.getState();
       boolean isEnabled = bluetoothAdapter.isEnabled();
       bluetoothAdapter.enable();
       if(isEnabled){
           UUID[] serviceUuids = {UUID.fromString(serviceUUID)};
           boolean started = bluetoothAdapter.startLeScan(serviceUuids,leScanCallbackStart);
           context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("scanningStatus",started);
       }
   }

   public void stopScan(){
        bluetoothAdapter.stopLeScan(leScanCallbackStart);
        context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("scanningStatus",false);
        for(BluetoothDevice dev : devicesSet){
            Log.d("BLETest","name: "+dev.getName()+
                    " address:"+dev.getAddress()+
                    " UUID"+ dev.getUuids()+
                    " fetchUuidsWithSdp: "+dev.fetchUuidsWithSdp()+" type: "+dev.getType());
        }
    }


    public void  advertise(String serviceUUID){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && bluetoothAdapter.isMultipleAdvertisementSupported())
        {
            BluetoothLeAdvertiser advertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
            AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();

            ParcelUuid pUuid = new ParcelUuid(UUID.fromString(serviceUUID));
            //Define a service UUID according to your needs
            dataBuilder.addServiceUuid(pUuid);
            dataBuilder.setIncludeDeviceName(true);
            dataBuilder.addServiceData( pUuid, "Lev".getBytes( Charset.forName( "UTF-8" )));


            AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
            settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
            settingsBuilder.setTimeout(18000);

            //Use the connectable flag if you intend on opening a Gatt Server
            //to allow remote connections to your device.
            settingsBuilder.setConnectable(true);

            AdvertiseCallback advertiseCallback=new AdvertiseCallback() {
                @Override
                public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                    super.onStartSuccess(settingsInEffect);
                    context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("advertisingStatus",true);
                }

                @Override
                public void onStartFailure(int errorCode) {
                    super.onStartFailure(errorCode);
                    context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("advertisingStatus",false);
                }
            };
            advertiser.startAdvertising(settingsBuilder.build(),dataBuilder.build(), advertiseCallback);
        }
    }
}
