package com.daizhirui.bletemperature;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;


/**
 * Show the BLE devices found in the scanning and allow users to select one from them.
 */
public class ScanBleDeviceActivity extends BluetoothLeBaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String TAG = ScanBleDeviceActivity.class.getSimpleName();
    public static final int REQUEST_SELECT_SERVICE_CHARACTERISTIC = 454;

    private BleDeviceListViewAdapter mBleDeviceListViewAdapter;

    private final BroadcastReceiver mBleScanUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_BLUETOOTH_DEVICE_DISCOVERED.equals(action)) {
                String deviceName = intent.getStringExtra(BluetoothLeService.INTENT_EXTRA_DEVICE_NAME);
                String deviceAddress = intent.getStringExtra(BluetoothLeService.INTENT_EXTRA_DEVICE_ADDRESS);
                mBleDeviceListViewAdapter.addDevice(deviceName, deviceAddress);
                mBleDeviceListViewAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanble);
        setTitle("");

        prepare_ui();
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(mBleScanUpdateReceiver, new IntentFilter(BluetoothLeService.ACTION_BLUETOOTH_DEVICE_DISCOVERED));
        mBluetoothLeService.scanBleDevices(true);    // fill the list view again
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(mBleScanUpdateReceiver);
        mBluetoothLeService.scanBleDevices(false);   // stop scanning
        mBleDeviceListViewAdapter.clear();  // clear the list view
    }

    private void prepare_ui() {
        // prepare list view
        mBleDeviceListViewAdapter = new BleDeviceListViewAdapter(getLayoutInflater());
        ListView listView = findViewById(R.id.discovered_ble_listview);
        listView.setAdapter(mBleDeviceListViewAdapter);
        listView.setOnItemClickListener(this);

        Button refreshButton = findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(this);

        Button cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.refresh_button:
                Log.d(TAG, "Refresh is pressed!");
                mBleDeviceListViewAdapter.clear();
                mBluetoothLeService.scanBleDevices(true);
                break;
            case R.id.cancel_button:
                mBluetoothLeService.scanBleDevices(false);
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final BleDeviceListViewAdapter.BleDevice device = mBleDeviceListViewAdapter.getDevice(position);
        if (device == null) return;
        final Intent intent = new Intent(ScanBleDeviceActivity.this, ScanGattServiceActivity.class);

        intent.putExtra(ScanGattServiceActivity.INTENT_BLE_DEVICE_NAME, device.deviceName);
        intent.putExtra(ScanGattServiceActivity.INTENT_BLE_DEVICE_ADDRESS, device.deviceAddress);

        BluetoothDevice bluetoothDevice = mBluetoothLeService.getBluetoothDevice(device.deviceAddress);
        if (bluetoothDevice != null) {

            mBluetoothLeService.mSelectedBluetoothDevice = bluetoothDevice;

            Log.i("ListView", "Bluetooth device clicked: " + device.deviceAddress);
            if (BluetoothLeService.mBleScanning) {
                mBluetoothLeService.scanBleDevices(false);
            }
            startActivityForResult(intent, ScanBleDeviceActivity.REQUEST_SELECT_SERVICE_CHARACTERISTIC);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ScanBleDeviceActivity.REQUEST_SELECT_SERVICE_CHARACTERISTIC) {
            if (resultCode == RESULT_OK) {
                setResult(resultCode);
                finish();
            }
        }
    }
}
