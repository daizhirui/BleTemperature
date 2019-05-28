package com.daizhirui.bletemperature;

import android.bluetooth.BluetoothGattDescriptor;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Show a LineChartView to visualize the data received from the monitored device.
 */
public class MonitorBleDeviceActivity extends BluetoothLeBaseActivity {
    private static final String TAG = MonitorBleDeviceActivity.class.getSimpleName();

    private static final int MAX_SCAN_TRY = 5;
    static final String INTENT_EXTRA_DEVICE_NAME = "INTENT_EXTRA_DEVICE_NAME";
    static final String INTENT_EXTRA_DEVICE_ADDRESS = "INTENT_EXTRA_DEVICE_ADDRESS";
    static final String INTENT_EXTRA_SERVICE_UUID = "INTENT_EXTRA_SERVICE_UUID";
    static final String INTENT_EXTRA_CHARACTERISTIC_UUID = "INTENT_EXTRA_CHARACTERISTIC_UUID";

    String mDeviceName;
    String mDeviceAddress;
    UUID mGattServiceUUID;
    UUID mCharacteristicUUID;
    UUID mDescriptorUUID;

    int mScanTryCount = MAX_SCAN_TRY;

    ProgressBar mProgressBar;
    ImageView mConnectedLogo;
    LineChartView mLineChartView;

    List<PointValue> mData;
    int mNumberOfLines;
    Long mInitialTime;
    Integer mMinimumTemp;
    Integer mMaximumTemp;

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_BLUETOOTH_DEVICE_DISCOVERED.equals(action)) {
                String deviceAddress = intent.getStringExtra(BluetoothLeService.INTENT_EXTRA_DEVICE_ADDRESS);
                if (deviceAddress.equals(mDeviceAddress)) {
                    mBluetoothLeService.scanBleDevices(false);
                    mBluetoothLeService.mSelectedBluetoothDevice = mBluetoothLeService.getBluetoothDevice(deviceAddress);
                    mBluetoothLeService.connectBluetoothDevice();
                }
            } else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.i(TAG, "Gatt connected");
                mScanTryCount = MAX_SCAN_TRY;
                runOnUiThread(new Runnable() {
                                  @Override
                                  public void run() {
                                      mProgressBar.setVisibility(View.INVISIBLE);
                                      mConnectedLogo.setVisibility(View.VISIBLE);
                                  }
                              }
                );
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.i(TAG, "Gatt disconnected, reconnect ... ");
                runOnUiThread(new Runnable() {
                                  @Override
                                  public void run() {
                                      mProgressBar.setVisibility(View.VISIBLE);
                                      mConnectedLogo.setVisibility(View.INVISIBLE);
                                  }
                              }
                );
                mBluetoothLeService.connectBluetoothDevice();
                mBluetoothLeService.setCharacteristicNotification(
                        mBluetoothLeService.mSelectedBluetoothGattCharacteristic,
                        mDescriptorUUID,
                        true);
            } else if (BluetoothLeService.ACTION_GATT_SERVICE_DISCOVERED.equals(action)) {
                Log.i(TAG, "Gatt service discovered");
                if (mBluetoothLeService.mSelectedBluetoothGatt == null) {
                    Log.e(TAG, "Null Gatt!");
                    mBluetoothLeService.connectBluetoothDevice();
                    return;
                }
                mBluetoothLeService.mSelectedBluetoothGattService = mBluetoothLeService.mSelectedBluetoothGatt.getService(mGattServiceUUID);
                if (mBluetoothLeService.mSelectedBluetoothGattService == null) {
                    Log.e(TAG, "Null GattService!");
                    mBluetoothLeService.mSelectedBluetoothGatt.discoverServices();
                    return;
                }
                mBluetoothLeService.mSelectedBluetoothGattCharacteristic = mBluetoothLeService.mSelectedBluetoothGattService.getCharacteristic(mCharacteristicUUID);
                List<BluetoothGattDescriptor> descriptors = mBluetoothLeService.mSelectedBluetoothGattCharacteristic.getDescriptors();
                if (mDescriptorUUID == null) {
                    for (BluetoothGattDescriptor descriptor : descriptors) {
                        if (GattStandard.parseDescriptorName(descriptor.getUuid()).equals(GattStandard.DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION)) {
                            mDescriptorUUID = descriptor.getUuid();
                            break;
                        }
                    }
                }
                Log.i(TAG, "Set Notification");
                mBluetoothLeService.setCharacteristicNotification(
                        mBluetoothLeService.mSelectedBluetoothGattCharacteristic,
                        mDescriptorUUID,
                        true
                );
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Integer value = readData();
                if (mMinimumTemp == null || mMinimumTemp > value) {
                    mMinimumTemp = value;
                }
                if (mMaximumTemp == null || mMaximumTemp < value) {
                    mMaximumTemp = value;
                }
                Log.i(TAG, "Receive: " + value);

                Long timestamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

                PointValue pointValue = new PointValue(timestamp - mInitialTime, value);
                mData.add(pointValue);
                setLineChartViewData();
                setLineChartViewViewport();

            } else if (BluetoothLeService.ACTION_SCAN_STOP.equals(action)) {
                if (mBluetoothLeService.mSelectedBluetoothDevice == null
                        || !mBluetoothLeService.mSelectedBluetoothDevice.getAddress().equals(mDeviceAddress)) {
                    if (mScanTryCount == 0) {
                        Log.i(TAG, "Fail to connect to the device ...");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MonitorBleDeviceActivity.this, "Fail to connect to the device ...", Toast.LENGTH_LONG).show();
                                MonitorBleDeviceActivity.this.finish();
                            }
                        });
                        return;
                    }
                    mBluetoothLeService.scanBleDevices(true);
                    Log.i(TAG, "Device not found yet, continue searching ...");
                    mScanTryCount -= 1;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MonitorBleDeviceActivity.this, "Device not found yet, continue searching ...", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }
    };

    private Integer readData() {
        String stringValue = mBluetoothLeService.mSelectedBluetoothGattCharacteristic.getStringValue(0);
        Integer value = Integer.valueOf(stringValue, 10);
        return value;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);
        setSupportActionBar((Toolbar) findViewById(R.id.monitor_toolbar));

        mProgressBar = findViewById(R.id.monitor_progressbar);
        mProgressBar.setVisibility(View.VISIBLE);
        mConnectedLogo = findViewById(R.id.monitor_connected_logo);
        mConnectedLogo.setVisibility(View.INVISIBLE);

        mLineChartView = findViewById(R.id.monitor_line_chart_view);
        mLineChartView.setInteractive(false);
        mLineChartView.setContainerScrollEnabled(false, ContainerScrollType.HORIZONTAL);
        mLineChartView.setViewportCalculationEnabled(false);
        setLineChartViewData();

//        if (mBluetoothLeService != null
//        && mBluetoothLeService.mSelectedBluetoothDevice != null
//        && mBluetoothLeService.mSelectedBluetoothGatt != null
//        && mBluetoothLeService.mSelectedBluetoothGattService != null
//        && mBluetoothLeService.mSelectedBluetoothGattCharacteristic != null) {
//
//        }
        Intent intent = getIntent();

        mDeviceName = intent.getStringExtra(INTENT_EXTRA_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(INTENT_EXTRA_DEVICE_ADDRESS);
        mGattServiceUUID = UUID.fromString(intent.getStringExtra(INTENT_EXTRA_SERVICE_UUID));
        mCharacteristicUUID = UUID.fromString(intent.getStringExtra(INTENT_EXTRA_CHARACTERISTIC_UUID));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mDeviceName);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (mBluetoothLeService.mSelectedBluetoothDevice == null
                || !mBluetoothLeService.mSelectedBluetoothDevice.getAddress().equals(mDeviceAddress)) {
            mBluetoothLeService.scanBleDevices(true);
        } else {
            mBluetoothLeService.connectBluetoothDevice();
        }

        mData = new ArrayList<>();
        mNumberOfLines = 1;
        mMinimumTemp = null;
        mMaximumTemp = null;

        mInitialTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();

        registerReceiver(mBroadcastReceiver, makeIntentFilter());
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();

        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
        mBluetoothLeService.setCharacteristicNotification(
                mBluetoothLeService.mSelectedBluetoothGattCharacteristic,
                mDescriptorUUID,
                false
        );
        mBluetoothLeService.disconnectBluetoothGatt();
        mBluetoothLeService.scanBleDevices(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    IntentFilter makeIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothLeService.ACTION_BLUETOOTH_DEVICE_DISCOVERED);
        filter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        filter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        filter.addAction(BluetoothLeService.ACTION_GATT_SERVICE_DISCOVERED);
        filter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        filter.addAction(BluetoothLeService.ACTION_SCAN_STOP);
        return filter;
    }

    private void setLineChartViewData() {
        // list for lines
        List<Line> lines = new ArrayList<>();
//        mData.add(new PointValue(0, 0));
        // generate line
        for (int i = 0; i < mNumberOfLines; ++i) {
            Line line = new Line(mData);
            line.setColor(ChartUtils.COLORS[i]);
            line.setShape(ValueShape.CIRCLE);           // shape of data point
            line.setCubic(false);                       // line is straight or not
            line.setFilled(false);                      // fill the area or not
            line.setHasLabels(false);                   // label for every point or not
            line.setHasLabelsOnlyForSelected(false);    // is every label selectable or not
            line.setHasLines(true);                     // connect points with lines or not
            line.setHasPoints(false);                   // show every point or not
            lines.add(line);
        }

        LineChartData data = new LineChartData(lines);

        Axis axisX = new Axis();
        Axis axisY = new Axis().setHasLines(true);
        axisX.setTextColor(Color.BLACK);    // set x label color
        axisY.setTextColor(Color.BLACK);    // set y label color
        axisX.setName("Timestamp");
        axisY.setName("Temperature ˚C");
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);

        // remove axes
//        data.setAxisXBottom(null);
//        data.setAxisYLeft(null);

        data.setBaseValue(Float.NEGATIVE_INFINITY);
        mLineChartView.setLineChartData(data);
    }

    void setLineChartViewViewport() {
        final Viewport v = new Viewport(mLineChartView.getMaximumViewport());
        v.bottom = mMinimumTemp - 5;
        v.top = mMaximumTemp + 5;
        v.left = 0;
        v.right = mData.size() - 1;
        mLineChartView.setMaximumViewport(v);
        mLineChartView.setCurrentViewport(v);
    }
}
