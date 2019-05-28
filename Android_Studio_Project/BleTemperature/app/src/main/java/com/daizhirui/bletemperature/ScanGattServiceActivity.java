package com.daizhirui.bletemperature;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;


/**
 * Show the GattServices of the selected BLE device and allow users to select one from them.
 */
public class ScanGattServiceActivity extends BluetoothLeBaseActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = ScanGattServiceActivity.class.getSimpleName();
    public static final String INTENT_BLE_DEVICE_NAME =
            "com.daizhirui.bletemperature.INTENT_BLE_DEVICE_NAME";
    public static final String INTENT_BLE_DEVICE_ADDRESS =
            "com.daizhirui.bletemperature.INTENT_BLE_DEVICE_ADDRESS";
    static final int REQUEST_SELECT_CHARACTERISTIC = 1;

    private ServiceListViewAdapter mServiceListViewAdapter;

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.i(TAG, "Gatt connected");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.i(TAG, "Gatt disconnected");
            } else if (BluetoothLeService.ACTION_GATT_SERVICE_DISCOVERED.equals(action)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mServiceListViewAdapter.clear();
                    }
                });

                // Show all the supported services and characteristics on the user interface.
                for (final BluetoothGattService service : mBluetoothLeService.getSupportedGattServices()) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mServiceListViewAdapter.addService(service);
                            mServiceListViewAdapter.notifyDataSetChanged();
                        }
                    });

//                    UUID uuid = service.getUuid();
//                    Log.i(TAG, String.format("Service:\nName: %s\nUUID: %s\n" , GattStandard.parseServiceName(uuid), uuid.toString()));
//                    List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
//                    for (BluetoothGattCharacteristic characteristic : characteristics) {
//                        mBluetoothLeService.readCharacteristic(characteristic);
//
//                        uuid = characteristic.getUuid();
//
//                        Log.i(TAG, String.format("Characteristic:\n\tName: %s\n\tUUID: %s\n", GattStandard.parseCharacteristicName(uuid), uuid));
//                        Log.i(TAG, "\tProperties:");
//                        for (String property : GattStandard.parseCharacteristicProperty(characteristic.getProperties())) {
//                            Log.i(TAG, "\t\t"+property);
//                        }
//                        Log.i(TAG, "\tPermissions:");
//                        for (String permission : GattStandard.parseCharacteristicPermission(characteristic.getPermissions())) {
//                            Log.i(TAG, "\t\t"+permission);
//                        }
//                        Log.i(TAG, "\tWriteType:");
//                        for (String writeType : GattStandard.parseWriteType(characteristic.getWriteType())) {
//                            Log.i(TAG, "\t\t"+writeType);
//                        }
//
//                        List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
//                        for (BluetoothGattDescriptor descriptor : descriptors) {
//                            UUID uuid1 = descriptor.getUuid();
//                            descriptor.getPermissions();
//                            Log.i(TAG, String.format("\tDescriptor:\n\t\tName: %s\n\t\tUUID: %s\n",  GattStandard.parseDescriptorName(uuid1), uuid1.toString()));
//                        }
//                    }
                }
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                String uuid = intent.getStringExtra(BluetoothLeService.INTENT_EXTRA_CHARACTERISTIC_UUID);
                String value = intent.getStringExtra(BluetoothLeService.INTENT_EXTRA_CHARACTERISTIC_VALUE);
                Log.i(TAG, String.format("Receive characteristic value:\n\tuuid: %s\n\tvalue: %s\n", uuid, value));
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set layout
        setContentView(R.layout.activity_scangatt);
        TextView deviceAddressTextView = findViewById(R.id.gatt_device_address);
        ListView gattCharacteristicListView = findViewById(R.id.gatt_service_characteristic_listview);
        mServiceListViewAdapter = new ServiceListViewAdapter(getLayoutInflater());
        gattCharacteristicListView.setAdapter(mServiceListViewAdapter);
        gattCharacteristicListView.setOnItemClickListener(this);
        // set title
        Toolbar toolbar = findViewById(R.id.gatt_toolbar);
        if (toolbar == null) {
            Log.e(TAG, "Null toolbar");
        }
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            Log.e(TAG, "Null actionBar!");
            return;
        }
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.scan_gatt_service_activity_title);
        // set device name
        TextView textView = findViewById(R.id.gatt_device_name);
        textView.setText(mBluetoothLeService.getSelectedBluetoothDeviceName());
        // set device address
        final Intent intent = getIntent();
        String deviceAddress = intent.getStringExtra(INTENT_BLE_DEVICE_ADDRESS);
        deviceAddressTextView.setText(deviceAddress);
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        mBluetoothLeService.connectBluetoothDevice();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBluetoothLeService.disconnectBluetoothGatt();
        unregisterReceiver(mGattUpdateReceiver);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICE_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BluetoothGattService service = mServiceListViewAdapter.getItem(position);
        if (service != null) {
            List<BluetoothGattCharacteristic> gattCharacteristics = service.getCharacteristics();
            Log.i(TAG, "Num of characteristics: " + gattCharacteristics.size());
            ServiceListViewAdapter.ViewHolder viewHolder = (ServiceListViewAdapter.ViewHolder) view.getTag();
            if (viewHolder.mHasNotify) {
                Intent intent = new Intent(this, SelectCharacteristicActivity.class);
                mBluetoothLeService.mSelectedBluetoothGattService = service;
                startActivityForResult(intent, ScanGattServiceActivity.REQUEST_SELECT_CHARACTERISTIC);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ScanGattServiceActivity.REQUEST_SELECT_CHARACTERISTIC) {
            if (resultCode == RESULT_OK) {
                setResult(resultCode);
                finish();
            }
        }
    }
}
