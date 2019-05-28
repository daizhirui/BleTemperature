package com.daizhirui.bletemperature;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothLeService extends Service {

    private static final String TAG = BluetoothLeService.class.getSimpleName();
    private static final String BLUETOOTH_MANAGER_ERROR_MSG = "Unable to initialize BluetoothManager.";

    private static final int STATE_DISCONNECTED = BluetoothProfile.STATE_DISCONNECTED;
    private static final int STATE_CONNECTING = BluetoothProfile.STATE_CONNECTING;
    private static final int STATE_CONNECTED = BluetoothProfile.STATE_CONNECTED;

    static final String ACTION_INITIALIZATION_FAILURE =
            "com.daizhirui.bletemperature.ACTION_INITIALIZATION_FAILURE";
    static final String ACTION_BLUETOOTH_ENABLED_REQUIRED =
            "com.daizhirui.bletemperature.ACTION_BLUETOOTH_ENABLED_REQUIRED";
    static final String ACTION_SCAN_STOP =
            "com.daizhirui.bletemperature.ACTION_SCAN_STOP";
    static final String ACTION_BLUETOOTH_DEVICE_DISCOVERED =
            "com.daizhirui.bletemperature.ACTION_BLUETOOTH_DEVICE_DISCOVERED";
    static final String ACTION_GATT_CONNECTED =
            "com.daizhirui.bletemperature.ACTION_GATT_CONNECTED";
    static final String ACTION_GATT_DISCONNECTED =
            "com.daizhirui.bletemperature.ACTION_GATT_DISCONNECTED";
    static final String ACTION_GATT_SERVICE_DISCOVERED =
            "com.daizhirui.bletemperature.ACTION_GATT_SERVICE_DISCOVERED";
    static final String ACTION_DATA_AVAILABLE =
            "com.daizhirui.bletemperature.ACTION_DATA_AVAILABLE";
    static final String INTENT_EXTRA_CHARACTERISTIC_UUID =
            "com.daizhirui.bletemperature.INTENT_EXTRA_CHARACTERISTIC_UUID";
    static final String INTENT_EXTRA_CHARACTERISTIC_VALUE =
            "com.daizhirui.bletemperature.INTENT_EXTRA_CHARACTERISTIC_VALUE";
    static final String INTENT_EXTRA_DEVICE_NAME =
            "com.daizhirui.bletemperature.INTENT_EXTRA_DEVICE_NAME";
    static final String INTENT_EXTRA_DEVICE_ADDRESS =
            "com.daizhirui.bletemperature.INTENT_EXTRA_DEVICE_ADDRESS";

    // bluetooth manager and adapter
    static boolean mBluetoothEnabled = false;
    static BluetoothManager mBluetoothManager = null;
    static BluetoothAdapter mBluetoothAdapter = null;
    // ble scanning
    static ArrayList<BluetoothDevice> mBluetoothDevices;
    static boolean mBleScanning = false;
    static Handler mBleScanningHandler = new Handler();
    static int mBleScanningPeriod = 10000;

    private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            BluetoothLeService.mBluetoothDevices.add(device);
            Intent intent = new Intent(ACTION_BLUETOOTH_DEVICE_DISCOVERED);
            final String deviceName = device.getName();
            intent.putExtra(INTENT_EXTRA_DEVICE_NAME, deviceName);
            intent.putExtra(INTENT_EXTRA_DEVICE_ADDRESS, device.getAddress());
            sendBroadcast(intent);
        }
    };
    // selected bluetooth device, gatt, etc.
    private int mConnectionState = STATE_DISCONNECTED;
    BluetoothDevice mSelectedBluetoothDevice = null;
    BluetoothGatt mSelectedBluetoothGatt = null;
    BluetoothGattService mSelectedBluetoothGattService = null;
    BluetoothGattCharacteristic mSelectedBluetoothGattCharacteristic = null;

    class RxTxJob {
        static final int JOB_TYPE_READ = 0;
        static final int JOB_TYPE_WRITE = 1;
        static final int JOB_TYPE_NOTIFICATION = 2;

        int mJobType;
        BluetoothGattCharacteristic mCharacteristic;
        UUID mConfigDescriptorUUID = null;
        boolean mSetNotification = false;

        RxTxJob(int jobType, BluetoothGattCharacteristic characteristic) {
            mJobType = jobType;
            mCharacteristic = characteristic;
        }

        RxTxJob(int jobType,
                BluetoothGattCharacteristic characteristic, UUID configDescriptorUUID,
                boolean setNotification) {
            mJobType = jobType;
            mCharacteristic = characteristic;
            mConfigDescriptorUUID = configDescriptorUUID;
            mSetNotification = setNotification;
        }
    }

    private List<RxTxJob> mRxTxJobs = new ArrayList<>();
    private boolean mProcessingRxTxJobs = false;

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                broadcastUpdate(intentAction);
                mConnectionState = STATE_CONNECTED;
                Log.i(TAG, "Connected to GATT Server");
                mSelectedBluetoothGatt.discoverServices();      // start to search services
            } else if (newState == STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                broadcastUpdate(intentAction);
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT Server");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICE_DISCOVERED);
            } else {
                Log.w(TAG, "onServiceDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
            processRxTxJobs();
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            UUID uuid = characteristic.getUuid();
            Log.d(TAG, String.format("onCharacteristicWrite: uuid = %s, status = %d", uuid.toString(), status));
            processRxTxJobs();
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            processRxTxJobs();
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor, int status) {
            UUID uuid = descriptor.getCharacteristic().getUuid();
            Log.d(TAG, String.format("onDescriptorWrite: uuid = %s, status = %d", uuid.toString(), status));
            processRxTxJobs();
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.d(TAG, "onReadRemoteRssi: rssi = " + rssi);
        }
    };

    /**
     * Try to get an instance of {@link BluetoothManager} which is the system service for Bluetooth.
     * If fails, it will broadcast an intent of which the action is {@link BluetoothLeService#ACTION_INITIALIZATION_FAILURE}.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        if (BluetoothLeService.mBluetoothManager == null) {
            BluetoothLeService.mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (BluetoothLeService.mBluetoothManager == null) {
                Log.w(TAG, BluetoothLeService.BLUETOOTH_MANAGER_ERROR_MSG);
                Toast.makeText(BluetoothLeService.this, BluetoothLeService.BLUETOOTH_MANAGER_ERROR_MSG, Toast.LENGTH_LONG).show();
                broadcastUpdate(BluetoothLeService.ACTION_INITIALIZATION_FAILURE);
            }
        }
    }

    /**
     * Check if Bluetooth is enabled when execute the start command.
     * @param intent    Intent: The Intent supplied to {@link Context#startService(Intent)}, as given. This may be null if the service is being
     *               restarted after its process has gone away, and it had previously returned anything except {@link Service#START_STICKY_COMPATIBILITY}.
     * @param flags     int: Additional data about this start request.
     * @param startId   int: A unique integer representing this specific request to start. Use with Service.stopSelfResult(int).
     * @return          The return value indicates what semantics the system should use for the service's current started state.
     *                  It may be one of the constants associated with the {@link Service#START_CONTINUATION_MASK} bits.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Try to start service");
        bluetoothIsEnabled();   // check if bluetooth is enabled and get the bluetooth adapter

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Before destroying this service, the following things will be done.
     * Close the connected bluetooth device.
     * Release all system resources, {@link BluetoothLeService#mBluetoothManager} and {@link BluetoothLeService#mBluetoothAdapter} before destroyed.
     * Reset {@link BluetoothLeService#mBleScanning} to false and discard all the pending callbacks in {@link BluetoothLeService#mBleScanningHandler}.
     * Reset {@link BluetoothLeService#mProcessingRxTxJobs} to false and discard all the pending {@link RxTxJob}s in {@link BluetoothLeService#mRxTxJobs}.
     */
    @Override
    public void onDestroy() {
        closeBluetoothGatt();
        mBleScanningHandler.removeCallbacksAndMessages(null);
        BluetoothLeService.mBluetoothManager = null;
        BluetoothLeService.mBluetoothAdapter = null;
        BluetoothLeService.mBleScanning = false;
        mRxTxJobs.clear();
        mProcessingRxTxJobs = false;
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    // Binder
    class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        closeBluetoothGatt();
        return super.onUnbind(intent);
    }

    /**
     * Check if Bluetooth is enabled.
     * @return  True if Bluetooth is enabled, otherwise, false and broadcast intent with {@link BluetoothLeService#ACTION_BLUETOOTH_ENABLED_REQUIRED} action.
     */
    boolean bluetoothIsEnabled() {
        if (BluetoothLeService.mBluetoothAdapter == null) {
            BluetoothLeService.mBluetoothAdapter = BluetoothLeService.mBluetoothManager.getAdapter();
        }
        if (BluetoothLeService.mBluetoothAdapter == null || !BluetoothLeService.mBluetoothAdapter.isEnabled()) {
            broadcastUpdate(ACTION_BLUETOOTH_ENABLED_REQUIRED);
            BluetoothLeService.mBluetoothEnabled = false;
            return false;
        } else {
            BluetoothLeService.mBluetoothEnabled = true;
            return true;
        }
    }

    // scan, connect, disconnect, close Bluetooth(Gatt)

    /**
     * Scan or stop scanning BLE devices.
     * @param enable    True for starting scanning and false for stop scanning.
     */
    void scanBleDevices(final boolean enable) {
        if (!bluetoothIsEnabled()) {
            return;
        }
        if (enable == BluetoothLeService.mBleScanning) return;
        if (enable) {
            BluetoothLeService.mBluetoothDevices = new ArrayList<>();
            BluetoothLeService.mBleScanningHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (BluetoothLeService.mBleScanning) {
                        BluetoothLeService.mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        BluetoothLeService.mBleScanning = false;
                        broadcastUpdate(ACTION_SCAN_STOP);
                    }
                }
            }, BluetoothLeService.mBleScanningPeriod);

            BluetoothLeService.mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            BluetoothLeService.mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        BluetoothLeService.mBleScanning = enable;
    }

    /**
     * Get a BluetoothDevice according to the device address.
     * @param address   String: The MAC address of the target bluetooth device.
     * @return          BluetoothDevice: The BluetoothDevice whose device address is equal to the provided one. Null if no qualified device is found.
     */
    BluetoothDevice getBluetoothDevice(String address) {
        for (BluetoothDevice device : mBluetoothDevices) {
            if (device.getAddress().equals(address)) {
                return device;
            }
        }
        return null;
    }

    /**
     * Get the device name of the selected Bluetooth device stored in {@link BluetoothLeService#mSelectedBluetoothDevice}.
     * @return  String: the name of the selected bluetooth device.
     */
    String getSelectedBluetoothDeviceName() {
        if (mSelectedBluetoothDevice == null) {
            return "None";
        }
        String name = mSelectedBluetoothDevice.getName();
        if (name == null || name.length() < 1) {
            return "Unknown";
        }
        return name;
    }

    void connectBluetoothDevice() {
        if (!bluetoothIsEnabled()) {
            return;
        }
        if (mSelectedBluetoothGatt != null) {
            mSelectedBluetoothGatt.close();     // release
        }
        mSelectedBluetoothGatt = mSelectedBluetoothDevice.connectGatt(this, false, mGattCallback);
        mConnectionState = STATE_CONNECTING;
    }

    void disconnectBluetoothGatt() {
        if (mSelectedBluetoothGatt == null) {
            Log.w(TAG, "No BluetoothGatt to disconnect.");
        } else {
            mSelectedBluetoothGatt.disconnect();
            mSelectedBluetoothGatt = null;
        }
        mConnectionState = STATE_DISCONNECTED;
        mProcessingRxTxJobs = false;
        mRxTxJobs.clear();
    }

    void closeBluetoothGatt() {
        if (mSelectedBluetoothGatt == null) {
            Log.w(TAG, "No BluetoothGatt to close.");
        } else {
            mSelectedBluetoothGatt.close();
        }
    }

    // get available services
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mSelectedBluetoothGatt == null)
            return null;

        mSelectedBluetoothDevice.getUuids();

        return mSelectedBluetoothGatt.getServices();
    }

    // get device type
    public int getDeviceType() {
        if (mSelectedBluetoothDevice == null) {
            return BluetoothDevice.DEVICE_TYPE_UNKNOWN;
        }
        return mSelectedBluetoothDevice.getType();
    }

    // callback function for read and write characteristic
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (!bluetoothIsEnabled()) {
            return;
        }
        if (mSelectedBluetoothGatt == null) {
            Log.w(TAG, "No BluetoothGatt is selected.");
            return;
        }

        mRxTxJobs.add(new RxTxJob(RxTxJob.JOB_TYPE_READ, characteristic));
        if (mConnectionState != BluetoothLeService.STATE_CONNECTED) {
            mSelectedBluetoothGatt.connect();
        } else if (!mProcessingRxTxJobs){
            processRxTxJobs();
        }
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (!bluetoothIsEnabled()) {
            return;
        }
        if (mSelectedBluetoothGatt == null) {
            Log.w(TAG, "No BluetoothGatt is selected.");
            return;
        }
        mRxTxJobs.add(new RxTxJob(RxTxJob.JOB_TYPE_WRITE, characteristic));
        if (mConnectionState != BluetoothLeService.STATE_CONNECTED) {
            mSelectedBluetoothGatt.connect();
        } else if (!mProcessingRxTxJobs){
            processRxTxJobs();
        }
    }

    /**
     * Enables or disables notification on a give characteristic.
     * @param characteristic    BluetoothGattCharacteristic: the characteristic which the notification is from. This characteristic must support "Notify" property.
     * @param configDescriptorUUID  UUID: the UUID of Client Characteristic Configuration Descriptor.
     * @param enabled           boolean: true for enable notification, false for turn off notification.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              UUID configDescriptorUUID,
                                              boolean enabled) {
        if (!bluetoothIsEnabled()) {
            return;
        }
        if (mSelectedBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mRxTxJobs.add(new RxTxJob(RxTxJob.JOB_TYPE_NOTIFICATION, characteristic, configDescriptorUUID, enabled));
        if (mConnectionState != BluetoothLeService.STATE_CONNECTED) {
            mSelectedBluetoothGatt.connect();
        } else if (!mProcessingRxTxJobs){
            processRxTxJobs();
        }
    }

    // get device signal strength: rssi
    public boolean getRssiVal() {
        if (mSelectedBluetoothGatt == null)
            return false;
        if (mConnectionState != BluetoothLeService.STATE_CONNECTED) {
            mSelectedBluetoothGatt.connect();
        }
        return mSelectedBluetoothGatt.readRemoteRssi();
    }

    /**
     * If all the Rx or Tx jobs are submitted to {@link BluetoothLeService#mSelectedBluetoothGatt} at the same time without waiting for any
     * former job to be completed. {@link BluetoothLeService#mSelectedBluetoothGatt} will save and execute the final submitted job. Therefore,
     * {@link BluetoothLeService#mRxTxJobs} and this function are designed for processing Rx and Tx jobs sequentially.
     */
    private void processRxTxJobs() {
        if (mRxTxJobs.size() > 0) {
            Log.i(TAG, String.format("processRxTxJobs: %d left", mRxTxJobs.size()));
            RxTxJob rxTxJob = mRxTxJobs.remove(0);
            switch (rxTxJob.mJobType) {
                case RxTxJob.JOB_TYPE_READ: {
                    mSelectedBluetoothGatt.readCharacteristic(rxTxJob.mCharacteristic);
                    break;
                }
                case RxTxJob.JOB_TYPE_WRITE: {
                    mSelectedBluetoothGatt.writeCharacteristic(rxTxJob.mCharacteristic);
                    break;
                }
                case RxTxJob.JOB_TYPE_NOTIFICATION: {
                    Log.i(TAG, "setCharacteristicNotification");
                    mSelectedBluetoothGatt.setCharacteristicNotification(rxTxJob.mCharacteristic, rxTxJob.mSetNotification);
                    BluetoothGattDescriptor descriptor = rxTxJob.mCharacteristic.getDescriptor(rxTxJob.mConfigDescriptorUUID);
                    if (descriptor != null) {
                        if (rxTxJob.mSetNotification) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        } else {
                            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                        }
                        mSelectedBluetoothGatt.writeDescriptor(descriptor);
                    }
                    break;
                }
            }
            mProcessingRxTxJobs = true;
        } else {
            mProcessingRxTxJobs = false;
        }
    }

    // broadcast
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        this.sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        final UUID uuid = characteristic.getUuid();
        final String value = GattStandard.bytes2HexString(characteristic.getValue());
        Log.i(TAG, String.format("Received value from %s, value = %s\n", uuid.toString(), value));
        intent.putExtra(INTENT_EXTRA_CHARACTERISTIC_UUID, uuid.toString());
        intent.putExtra(INTENT_EXTRA_CHARACTERISTIC_VALUE, value);

        this.sendBroadcast(intent);
    }
}
