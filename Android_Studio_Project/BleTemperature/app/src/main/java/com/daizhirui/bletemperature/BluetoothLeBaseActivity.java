package com.daizhirui.bletemperature;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * BluetoothLeBaseActivity is a base activity for all activities that use Bluetooth Low Energy feature.
 */
public class BluetoothLeBaseActivity extends AppCompatActivity {

    private static final String TAG = BluetoothLeBaseActivity.class.getSimpleName();

    private static final int ACTION_BLUETOOTH_ENABLE_REQUEST = 982;
    private static final int ACTION_PERMISSION_REQUEST = 381;
    // permission check
    static List<String> mPermissions = new ArrayList<>();
    static boolean permissionCheckPassed = false;
    // bluetooth service
    static BluetoothLeService mBluetoothLeService;
    /**
     * There is only one BluetoothLeBaseActivity being bound with BluetoothLeService,
     * which is a Service wrapping and managing BLE APIs provided by Android SDK.
     * Do not modified this attribute. It is managed by this class.
     */
    protected static BluetoothLeBaseActivity mBluetoothLeServiceBindActivity;
    static int mActivityCount = 0;
    // Code to manage Service lifecycle.
    private static final ServiceConnection mBluetoothLeServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder serviceBinder) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) serviceBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    /**
     * {@link BluetoothLeBaseActivity} only receives Intent of which the action is {@link BluetoothLeService#ACTION_BLUETOOTH_ENABLED_REQUIRED} or
     * {@link BluetoothLeService#ACTION_INITIALIZATION_FAILURE}.
     * Activities that extend from this class can register their own BroadcastReceiver to receive more Intent from {@link BluetoothLeBaseActivity#mBluetoothLeService}.
     * When {@link BluetoothLeService#ACTION_BLUETOOTH_ENABLED_REQUIRED} is received, this class will send a request to the system for enabling Bluetooth.
     */
    private final BroadcastReceiver mBluetoothLeServiceBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothLeService.ACTION_BLUETOOTH_ENABLED_REQUIRED.equals(action)) {
                Log.i(TAG, "Request for enabling bluetooth");
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), BluetoothLeBaseActivity.ACTION_BLUETOOTH_ENABLE_REQUEST);
            } else if (BluetoothLeService.ACTION_INITIALIZATION_FAILURE.equals(action)) {
                Toast.makeText(BluetoothLeBaseActivity.this, "Fail to initialize BluetoothLeService, please retry.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BluetoothLeBaseActivity.mActivityCount += 1;
    }

    /**
     * {@link BluetoothLeBaseActivity} will check permissions related to Bluetooth and start up a {@link BluetoothLeService} if permission check passes.
     * Besides, it also register a broadcast receiver to receive intent from {@link BluetoothLeBaseActivity#mBluetoothLeService}.
     */
    @Override
    protected void onResume() {
        super.onResume();

        checkPermissions(); // BluetoothLeService will be started up if passed

        registerReceiver(mBluetoothLeServiceBroadcastReceiver, makeIntentFilter());
    }

    /**
     * Unregister the broadcast receiver when paused.
     */
    @Override
    protected void onPause() {
        unregisterReceiver(mBluetoothLeServiceBroadcastReceiver);

        super.onPause();
    }

    /**
     * When the final instance of BluetoothLeBaseActivity is going to be destroyed, {@link BluetoothLeBaseActivity#mBluetoothLeService}
     * should be stopped and unbind. And {@link BluetoothLeBaseActivity#mBluetoothLeServiceBindActivity} will be assigned null. So, please
     * make sure that all activities using BLE extend from {@link BluetoothLeBaseActivity}.
     */
    @Override
    protected void onDestroy() {
        BluetoothLeBaseActivity.mActivityCount -= 1;
        if (BluetoothLeBaseActivity.mBluetoothLeServiceBindActivity == this) {
            BluetoothLeBaseActivity.mBluetoothLeServiceBindActivity = null;
            Intent stopIntent = new Intent(this, BluetoothLeService.class);
            stopService(stopIntent);
            unbindService(mBluetoothLeServiceConnection);
            BluetoothLeBaseActivity.mBluetoothLeService = null;
        }
        super.onDestroy();
    }

    /**
     * If the user denies to open Bluetooth, all the following procedures cannot be executed. So, {@link BluetoothLeBaseActivity} will finish itself.
     * @param requestCode   int: The integer request code originally supplied to startActivityForResult(), allowing you to identify who this result came from.
     * @param resultCode    int: The integer result code returned by the child activity through its setResult().
     * @param data          Intent: An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == BluetoothLeBaseActivity.ACTION_BLUETOOTH_ENABLE_REQUEST && resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Exit due to unavailable bluetooth.", Toast.LENGTH_LONG).show();
            finish();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * If one of the permissions is not granted, {@link BluetoothLeBaseActivity} will try to request it over and over again until the permission is granted.
     * @param requestCode   int: The request code passed in requestPermissions(String[], int).
     * @param permissions   String: The requested permissions. Never null.
     * @param grantResults  int: The grant results for the corresponding permissions which is either {@link PackageManager#PERMISSION_GRANTED} or {@link PackageManager#PERMISSION_DENIED}. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionsResult");
        if (permissions.length == 0) {
            // User's action is interrupted, try again
            checkPermissions();
        }
        if (requestCode == BluetoothLeBaseActivity.ACTION_PERMISSION_REQUEST) {
            for (int i = 0; i < grantResults.length; ++i) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, String.format("%s is necessary for discover bluetooth devices!", permissions[i]), Toast.LENGTH_LONG).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{permissions[i]}, BluetoothLeBaseActivity.ACTION_PERMISSION_REQUEST);
                    }
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Check permissions and request those un-granted permissions. When all the required permissions are granted,
     * {@link BluetoothLeBaseActivity#mBluetoothLeService} will be started up.
     */
    void checkPermissions() {
        Log.d(TAG, "Check permission!!!");
        BluetoothLeBaseActivity.permissionCheckPassed = true;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            BluetoothLeBaseActivity.mPermissions.add(Manifest.permission.BLUETOOTH);
            BluetoothLeBaseActivity.permissionCheckPassed = false;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            BluetoothLeBaseActivity.mPermissions.add(Manifest.permission.BLUETOOTH_ADMIN);
            BluetoothLeBaseActivity.permissionCheckPassed = false;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            BluetoothLeBaseActivity.mPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            BluetoothLeBaseActivity.permissionCheckPassed = false;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            BluetoothLeBaseActivity.mPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            BluetoothLeBaseActivity.permissionCheckPassed = false;
        }

        if (BluetoothLeBaseActivity.permissionCheckPassed) {
            Log.i(TAG, "Permission check passed");
            startBluetoothLeService();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(BluetoothLeBaseActivity.mPermissions.toArray(new String[0]), BluetoothLeBaseActivity.ACTION_PERMISSION_REQUEST);
            }
        }
    }

    /**
     * Only start up a BluetoothLeService if {@link BluetoothLeBaseActivity#mBluetoothLeService} is null.
     */
    private void startBluetoothLeService() {
        if (mBluetoothLeService == null) {
            Intent bindIntent = new Intent(this, BluetoothLeService.class);
            bindService(bindIntent, mBluetoothLeServiceConnection, BIND_AUTO_CREATE);
            BluetoothLeBaseActivity.mBluetoothLeServiceBindActivity = this;
            startService(bindIntent);
        }
    }

    /**
     * Make an IntentFilter for {@link BluetoothLeBaseActivity#mBluetoothLeServiceBroadcastReceiver}.
     * @return  An IntentFilter with {@link BluetoothLeService#ACTION_BLUETOOTH_ENABLED_REQUIRED} and {@link BluetoothLeService#ACTION_INITIALIZATION_FAILURE} inside.
     */
    private IntentFilter makeIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothLeService.ACTION_BLUETOOTH_ENABLED_REQUIRED);
        filter.addAction(BluetoothLeService.ACTION_INITIALIZATION_FAILURE);
        return filter;
    }
}
