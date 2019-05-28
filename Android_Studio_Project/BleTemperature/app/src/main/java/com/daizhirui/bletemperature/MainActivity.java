package com.daizhirui.bletemperature;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The entry activity.
 */
public class MainActivity extends BluetoothLeBaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static BleSavedDeviceListViewAdapter mBleSavedDeviceListViewAdapter;

    static final int REQUEST_ADD_DEVICE = 1;

    List<BleSavedDevice> mSelectedDevices = new ArrayList<>();
    LinearLayout mDeleteButtonGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDeleteButtonGroup = findViewById(R.id.main_delete_action_group);
        mDeleteButtonGroup.setVisibility(View.INVISIBLE);

        ImageButton mDeleteButton = findViewById(R.id.main_delete_button);
        mDeleteButton.setOnClickListener(this);

        Button mCancelButton = findViewById(R.id.main_cancel_button);
        mCancelButton.setOnClickListener(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(this);

        ListView listView = findViewById(R.id.device_listview);
        listView.setOnItemClickListener(this);
        if (mBleSavedDeviceListViewAdapter == null) {
            mBleSavedDeviceListViewAdapter = new BleSavedDeviceListViewAdapter(getLayoutInflater());
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            try {
                mBleSavedDeviceListViewAdapter.mBleSavedDevices = BleSavedDevice.toDeviceList(
                        preferences.getString(BleSavedDevice.JSON_FILE, "")
                );
                mBleSavedDeviceListViewAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                Log.e(TAG, e.toString());
            }
        }
        listView.setAdapter(mBleSavedDeviceListViewAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete) {
            if (mDeleteButtonGroup.getVisibility() == View.VISIBLE) {
                mDeleteButtonGroup.setVisibility(View.INVISIBLE);
            } else {
                mDeleteButtonGroup.setVisibility(View.VISIBLE);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MainActivity.REQUEST_ADD_DEVICE && resultCode == RESULT_OK) {
            Log.i(TAG, "Add device");

            String deviceName = mBluetoothLeService.mSelectedBluetoothDevice.getName();
            if (deviceName == null || deviceName.length() == 0) {
                deviceName = "Unknown";
            }
            String deviceAddress = mBluetoothLeService.mSelectedBluetoothDevice.getAddress();
            UUID serviceUUID = mBluetoothLeService.mSelectedBluetoothGattService.getUuid();
            UUID characteristicUUID = mBluetoothLeService.mSelectedBluetoothGattCharacteristic.getUuid();

            BleSavedDevice device = new BleSavedDevice();
            device.mDeviceName = deviceName;
            device.mDeviceAddress = deviceAddress;
            device.mGattServiceUUID = serviceUUID;
            device.mCharacteristicUUID = characteristicUUID;
            mBleSavedDeviceListViewAdapter.addDevice(device);
            saveDeviceListToPreference();
            mBleSavedDeviceListViewAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.i(TAG, "onItemClick");
        if (position >= mBleSavedDeviceListViewAdapter.getCount()) {
            return;
        }
        BleSavedDevice device = mBleSavedDeviceListViewAdapter.getItem(position);

        if (mDeleteButtonGroup.getVisibility() == View.VISIBLE) {    // delete button is visible!
            Log.i(TAG, "deleteButton is visible");
            BleSavedDeviceListViewAdapter.ViewHolder viewHolder = (BleSavedDeviceListViewAdapter.ViewHolder) view.getTag();
            if (viewHolder == null) {
                return;
            }

            device.mSelected = !device.mSelected;

            if (device.mSelected) {
                mSelectedDevices.add(mBleSavedDeviceListViewAdapter.getItem(position));
            } else {
                mSelectedDevices.remove(mBleSavedDeviceListViewAdapter.getItem(position));
            }
            mBleSavedDeviceListViewAdapter.notifyDataSetChanged();
        } else {
            // start to monitor the selected device
            Intent intent = new Intent(this, MonitorBleDeviceActivity.class);
            intent.putExtra(MonitorBleDeviceActivity.INTENT_EXTRA_DEVICE_NAME, device.mDeviceName);
            intent.putExtra(MonitorBleDeviceActivity.INTENT_EXTRA_DEVICE_ADDRESS, device.mDeviceAddress);
            intent.putExtra(MonitorBleDeviceActivity.INTENT_EXTRA_SERVICE_UUID, device.mGattServiceUUID.toString());
            intent.putExtra(MonitorBleDeviceActivity.INTENT_EXTRA_CHARACTERISTIC_UUID, device.mCharacteristicUUID.toString());
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.main_delete_button) {
            for (BleSavedDevice device : mSelectedDevices) {
                mBleSavedDeviceListViewAdapter.removeDevice(device);
            }
            mSelectedDevices.clear();
            mBleSavedDeviceListViewAdapter.notifyDataSetChanged();
            mDeleteButtonGroup.setVisibility(View.INVISIBLE);
            saveDeviceListToPreference();
        } else if (id == R.id.fab) {
            final Intent intent = new Intent(MainActivity.this, ScanBleDeviceActivity.class);
            startActivityForResult(intent, MainActivity.REQUEST_ADD_DEVICE);
        } else if (id == R.id.main_cancel_button) {
            mDeleteButtonGroup.setVisibility(View.INVISIBLE);
            for (BleSavedDevice device : mSelectedDevices) {
                device.mSelected = false;
            }
            mSelectedDevices.clear();
            mBleSavedDeviceListViewAdapter.notifyDataSetChanged();
        }
    }

    private void saveDeviceListToPreference() {
        try {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.putString(BleSavedDevice.JSON_FILE, BleSavedDevice.toJSONString(mBleSavedDeviceListViewAdapter.mBleSavedDevices));
            editor.apply();
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
    }
}
