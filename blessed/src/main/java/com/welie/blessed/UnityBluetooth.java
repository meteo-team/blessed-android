package com.welie.blessed;

import com.unity3d.player.UnityPlayer;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import timber.log.Timber;

import static com.welie.blessed.BluetoothBytesParser.bytes2String;
import static com.welie.blessed.BluetoothPeripheral.GATT_SUCCESS;

public class UnityBluetooth {
    /*
    Singleton instance.
    */
    private static volatile UnityBluetooth instance;

//    public interface UnityCallback {
//        public void sendMessage(String message);
//    }

    /**
     * Callbacks for BluetoothPeripheral events.
     */
    public interface AndroidBluetoothPeripheralListener {

        public void onServicesDiscovered(BluetoothPeripheral peripheral);

        public void onNotificationStateUpdate(@NotNull BluetoothPeripheral peripheral, @NotNull BluetoothGattCharacteristic characteristic, int status);

        public void onCharacteristicWrite(@NotNull BluetoothPeripheral peripheral, @NotNull byte[] value, @NotNull BluetoothGattCharacteristic characteristic, int status);

        public void onCharacteristicUpdate(BluetoothPeripheral peripheral, byte[] value, BluetoothGattCharacteristic characteristic, int status);

        public void onMtuChanged(@NotNull BluetoothPeripheral peripheral, int mtu, int status);
    }

    protected AndroidBluetoothPeripheralListener mAndroidBluetoothPeripheralListener;

    /**
     * Callbacks for BluetoothCentral events.
     */
    public interface AndroidBluetoothCentralListener {
        public void onPause();

        public void onResume();

        public void onActivityResult(int requestCode, int resultCode, Intent data);

        public void onRequestPermissionsResult(
                int requestCode, String[] permissions, int[] grantResults);

        public void onConnectedPeripheral(BluetoothPeripheral peripheral);

        public void onConnectionFailed(BluetoothPeripheral peripheral, final int status);

        public void onDisconnectedPeripheral(final BluetoothPeripheral peripheral, final int status);

        public void onDiscoveredPeripheral(BluetoothPeripheral peripheral, ScanResult scanResult);

        public void onBluetoothAdapterStateChanged(int state);

        public void onScanFailed(int errorCode);
    }

    protected AndroidBluetoothCentralListener mAndroidBluetoothCentralListener;

    /*
    Static variables
    */
    private static final String TAG = UnityBluetooth.class.getSimpleName();

    // Local variables
    public BluetoothCentral central;
    private final Handler handler = new Handler();
    private int currentTimeCounter = 0;

    // Callback for peripherals
    private final BluetoothPeripheralCallback peripheralCallback = new BluetoothPeripheralCallback() {
        @Override
        public void onServicesDiscovered(@NotNull BluetoothPeripheral peripheral) {
            Timber.i("discovered services");
            mAndroidBluetoothPeripheralListener.onServicesDiscovered(peripheral);
            // Request a higher MTU, iOS always asks for 185
            peripheral.requestMtu(185);

            // Request a new connection priority
//            peripheral.requestConnectionPriority(CONNECTION_PRIORITY_HIGH);
//
//            // Read manufacturer and model number from the Device Information Service
//            peripheral.readCharacteristic(DIS_SERVICE_UUID, MANUFACTURER_NAME_CHARACTERISTIC_UUID);
//            peripheral.readCharacteristic(DIS_SERVICE_UUID, MODEL_NUMBER_CHARACTERISTIC_UUID);
//
//            // Turn on notifications for Current Time Service and write it if possible
//            BluetoothGattCharacteristic currentTimeCharacteristic = peripheral.getCharacteristic(CTS_SERVICE_UUID, CURRENT_TIME_CHARACTERISTIC_UUID);
//            if (currentTimeCharacteristic != null) {
//                peripheral.setNotify(currentTimeCharacteristic, true);
//
//                // If it has the write property we write the current time
//                if ((currentTimeCharacteristic.getProperties() & PROPERTY_WRITE) > 0) {
//                    // Write the current time unless it is an Omron device
//                    if (!(peripheral.getName().contains("BLEsmart_"))) {
//                        BluetoothBytesParser parser = new BluetoothBytesParser();
//                        parser.setCurrentTime(Calendar.getInstance());
//                        peripheral.writeCharacteristic(currentTimeCharacteristic, parser.getValue(), WRITE_TYPE_DEFAULT);
//                    }
//                }
//            }
//
//            // Try to turn on notifications for other characteristics
//            peripheral.readCharacteristic(BTS_SERVICE_UUID, BATTERY_LEVEL_CHARACTERISTIC_UUID);
//            peripheral.setNotify(BLP_SERVICE_UUID, BLOOD_PRESSURE_MEASUREMENT_CHARACTERISTIC_UUID, true);
//            peripheral.setNotify(HTS_SERVICE_UUID, TEMPERATURE_MEASUREMENT_CHARACTERISTIC_UUID, true);
//            peripheral.setNotify(HRS_SERVICE_UUID, HEARTRATE_MEASUREMENT_CHARACTERISTIC_UUID, true);
//            peripheral.setNotify(PLX_SERVICE_UUID, PLX_CONTINUOUS_MEASUREMENT_CHAR_UUID, true);
//            peripheral.setNotify(PLX_SERVICE_UUID, PLX_SPOT_MEASUREMENT_CHAR_UUID, true);
//            peripheral.setNotify(WSS_SERVICE_UUID, WSS_MEASUREMENT_CHAR_UUID, true);
//            peripheral.setNotify(GLUCOSE_SERVICE_UUID, GLUCOSE_MEASUREMENT_CHARACTERISTIC_UUID, true);
//            peripheral.setNotify(GLUCOSE_SERVICE_UUID, GLUCOSE_MEASUREMENT_CONTEXT_CHARACTERISTIC_UUID, true);
//            peripheral.setNotify(GLUCOSE_SERVICE_UUID, GLUCOSE_RECORD_ACCESS_POINT_CHARACTERISTIC_UUID, true);
//            peripheral.setNotify(CONTOUR_SERVICE_UUID, CONTOUR_CLOCK, true);
        }

        @Override
        public void onNotificationStateUpdate(@NotNull BluetoothPeripheral peripheral, @NotNull BluetoothGattCharacteristic characteristic, int status) {
            mAndroidBluetoothPeripheralListener.onNotificationStateUpdate(peripheral, characteristic, status);
//            if (status == GATT_SUCCESS) {
//                final boolean isNotifying = peripheral.isNotifying(characteristic);
//                Timber.i("SUCCESS: Notify set to '%s' for %s", isNotifying, characteristic.getUuid());
//                if (characteristic.getUuid().equals(CONTOUR_CLOCK)) {
//                    writeContourClock(peripheral);
//                }
//            } else {
//                Timber.e("ERROR: Changing notification state failed for %s", characteristic.getUuid());
//            }
        }

        @Override
        public void onCharacteristicWrite(@NotNull BluetoothPeripheral peripheral, @NotNull byte[] value, @NotNull BluetoothGattCharacteristic characteristic, int status) {
            mAndroidBluetoothPeripheralListener.onCharacteristicWrite(peripheral, value, characteristic, status);
//            if (status == GATT_SUCCESS) {
//                Timber.i("SUCCESS: Writing <%s> to <%s>", bytes2String(value), characteristic.getUuid().toString());
//            } else {
//                Timber.i("ERROR: Failed writing <%s> to <%s>", bytes2String(value), characteristic.getUuid().toString());
//            }
        }

        @Override
        public void onCharacteristicUpdate(@NotNull BluetoothPeripheral peripheral, @NotNull byte[] value, @NotNull BluetoothGattCharacteristic characteristic, int status) {
            if (status != GATT_SUCCESS) return;
            mAndroidBluetoothPeripheralListener.onCharacteristicUpdate(peripheral, value, characteristic, status);
            Timber.i("%s", bytes2String(value));
        }

        @Override
        public void onMtuChanged(@NotNull BluetoothPeripheral peripheral, int mtu, int status) {
            Timber.i("new MTU set: %d", mtu);
        }

        private void sendMeasurement(@NotNull Intent intent, @NotNull BluetoothPeripheral peripheral) {
//            intent.putExtra(MEASUREMENT_EXTRA_PERIPHERAL, peripheral.getAddress());
//            context.sendBroadcast(intent);
        }

        private void writeContourClock(@NotNull BluetoothPeripheral peripheral) {

        }
    };

    // Callback for central
    private final BluetoothCentralCallback bluetoothCentralCallback = new BluetoothCentralCallback() {

        @Override
        public void onConnectedPeripheral(@NotNull BluetoothPeripheral peripheral) {
            Timber.i("connected to '%s'", peripheral.getName());
            mAndroidBluetoothCentralListener.onConnectedPeripheral(peripheral);
        }

        @Override
        public void onConnectionFailed(@NotNull BluetoothPeripheral peripheral, final int status) {
            Timber.e("connection '%s' failed with status %d", peripheral.getName(), status);
            mAndroidBluetoothCentralListener.onConnectionFailed(peripheral, status);
        }

        @Override
        public void onDisconnectedPeripheral(@NotNull final BluetoothPeripheral peripheral, final int status) {
            Timber.i("disconnected '%s' with status %d", peripheral.getName(), status);
            mAndroidBluetoothCentralListener.onDisconnectedPeripheral(peripheral, status);
            // Reconnect to this device when it becomes available again
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    central.autoConnectPeripheral(peripheral, peripheralCallback);
                }
            }, 5000);

        }

        @Override
        public void onDiscoveredPeripheral(@NotNull BluetoothPeripheral peripheral, @NotNull ScanResult scanResult) {
            Timber.i("Found peripheral '%s'", peripheral.getName());
            mAndroidBluetoothCentralListener.onDiscoveredPeripheral(peripheral, scanResult);
//            central.stopScan();
//            central.connectPeripheral(peripheral, peripheralCallback);
        }

        @Override
        public void onBluetoothAdapterStateChanged(int state) {
            Timber.i("bluetooth adapter changed state to %d", state);
            mAndroidBluetoothCentralListener.onBluetoothAdapterStateChanged(state);
            if (state == BluetoothAdapter.STATE_ON) {
                // Bluetooth is on now, start scanning again
                // Scan for peripherals with a certain service UUIDs
//                central.startPairingPopupHack();
//                central.scanForPeripheralsWithServices(new UUID[]{});//BLP_SERVICE_UUID, HTS_SERVICE_UUID, HRS_SERVICE_UUID
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Timber.i("scanning failed with error %d", errorCode);
            mAndroidBluetoothCentralListener.onScanFailed(errorCode);
        }
    };

    private UnityBluetooth() {
        Log.d(TAG, "UnityBluetooth: saving unityActivity in private var.");


//        this.context = context;
//
//        // Plant a tree
//        Timber.plant(new Timber.DebugTree());
//
//        // Create BluetoothCentral
//        central = new BluetoothCentral(context, bluetoothCentralCallback, new Handler());
//
//        // Scan for peripherals with a certain service UUIDs
//        central.startPairingPopupHack();
//        central.scanForPeripheralsWithServices(new UUID[]{});
    }

    /*
    Public methods that can be directly called by Unity
    PluginInstance.Call("InitUnityBluetooth", new object[] { new UnityCallback(callback)});
    */
    public void InitUnityBluetooth(final AndroidBluetoothCentralListener centralListener, final AndroidBluetoothPeripheralListener peripheralListener) {
        System.out.println("Android Executing: InitUnityBluetooth");
        mAndroidBluetoothCentralListener = centralListener;
        mAndroidBluetoothPeripheralListener = peripheralListener;
        if (!UnityPlayer.currentActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.d(TAG, "onCreate: fail: missing FEATURE_BLUETOOTH_LE");
            ////UnityPlayer.UnitySendMessage("BLEControllerEventHandler", BLEUnityMessageName_OnBleDidInitialize, "Fail: missing Bluetooth LE");
//            callback.sendMessage("Fail: missing Bluetooth LE");
            return;
        }
        // Plant a tree
        Timber.plant(new Timber.DebugTree());

        // Create BluetoothCentral
        central = new BluetoothCentral(UnityPlayer.currentActivity.getApplicationContext(), bluetoothCentralCallback, new Handler());
    }

    /*
     *  Public static methods
     */
    public static synchronized UnityBluetooth getInstance() {
//        public static synchronized BluetoothHandler getInstance(Context context) {
//        if (instance == null) {
//            instance = new BluetoothHandler(context.getApplicationContext());
//        }
//        return instance;
//    }

        if (instance == null) {
            Log.d(TAG, "UnityBluetooth: Creation of _instance");
            instance = new UnityBluetooth();
        }


        return instance;
    }

    public void ConnectPeripheral(@NotNull BluetoothPeripheral peripheral) {
        Timber.i("connect peripheral %s", peripheral.getName());
        central.connectPeripheral(peripheral, peripheralCallback);
    }

    public void StopScan() {
        Timber.i("stop scanning");
        central.stopScan();
    }

    // "00001810-0000-1000-8000-00805f9b34fb"
    public void ScanForPeripheralsWithServices(String uuid) {
        Timber.i("scanning for uuid %s", uuid);
        central.startPairingPopupHack();
        central.scanForPeripheralsWithServices(new UUID[]{UUID.fromString(uuid)});//BLP_SERVICE_UUID, HTS_SERVICE_UUID, HRS_SERVICE_UUID
    }

    public void ScanForPeripherals() {
        Timber.i("scanForPeripherals");
        central.startPairingPopupHack();
        central.scanForPeripherals();
    }

}
