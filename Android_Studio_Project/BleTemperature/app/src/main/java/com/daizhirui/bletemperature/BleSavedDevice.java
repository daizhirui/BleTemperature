package com.daizhirui.bletemperature;

import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * BleSavedDevice is for storing information of a saved device.
 */
class BleSavedDevice {
    boolean mSelected = false;
    String mDeviceName;
    String mDeviceAddress;
    UUID mGattServiceUUID;
    UUID mCharacteristicUUID;

    private static final String TAG = BleSavedDevice.class.getSimpleName();
    static final String JSON_FILE = "BLE_DEVICES";
    private static final String JSON_KEY_DEVICE = "JSON_KEY_DEVICE";
    private static final String JSON_KEY_NAME = "JSON_KEY_NAME";
    private static final String JSON_KEY_ADDRESS = "JSON_KEY_ADDRESS";
    private static final String JSON_KEY_SERVICE_UUID = "JSON_KEY_SERVICE_UUID";
    private static final String JSON_KEY_CHARACTERISTIC_UUID = "JSON_KEY_CHARACTERISTIC_UUID";

    /**
     * Convert a JSON String to an ArrayList of BleSavedDevices.
     * @param string A JSON String to convert.
     * @return  An ArrayList of BleSavedDevices.
     * @throws JSONException Thrown when the JSON String is incomplete.
     */
    static ArrayList<BleSavedDevice> toDeviceList(@Nullable String string) throws JSONException {
        ArrayList<BleSavedDevice> result = new ArrayList<>();
        JSONObject root;
        JSONArray deviceArray;

        if (string != null && string.length() > 0) {
            root = new JSONObject(string);
            try {
                deviceArray = root.getJSONArray(JSON_KEY_DEVICE);
            } catch (JSONException e) {
                Log.e(TAG, e.toString());
                deviceArray = new JSONArray();
            }

            if (deviceArray.length() > 0) {
                for (int i = 0; i < deviceArray.length(); ++i) {
                    JSONObject device = deviceArray.getJSONObject(i);
                    BleSavedDevice bleSavedDevice = new BleSavedDevice();
                    try {
                        bleSavedDevice.mDeviceName = device.getString(JSON_KEY_NAME);
                    } catch (JSONException e) {
                        bleSavedDevice.mDeviceName = "Unknown";
                    }
                    bleSavedDevice.mDeviceAddress = device.getString(JSON_KEY_ADDRESS);
                    bleSavedDevice.mGattServiceUUID = UUID.fromString(device.getString(JSON_KEY_SERVICE_UUID));
                    bleSavedDevice.mCharacteristicUUID = UUID.fromString(device.getString(JSON_KEY_CHARACTERISTIC_UUID));
                    result.add(bleSavedDevice);
                }
            }
        }

        return result;
    }

    /**
     * Convert a List of BleSavedDevices to a JSON String.
     * @param deviceList    A List of BleSavedDevices to convert.
     * @return  A JSON String which contains BleSavedDevice instances stored in the provided List.
     * @throws JSONException Thrown when the method fails to generate certain keys.
     */
    static String toJSONString(List<BleSavedDevice> deviceList) throws JSONException {
        JSONObject root = new JSONObject();
        JSONArray deviceArray = new JSONArray();

        for (BleSavedDevice device : deviceList) {
            JSONObject deviceObj = new JSONObject();
            deviceObj.put(JSON_KEY_NAME, device.mDeviceName);
            deviceObj.put(JSON_KEY_ADDRESS, device.mDeviceAddress);
            deviceObj.put(JSON_KEY_SERVICE_UUID, device.mGattServiceUUID);
            deviceObj.put(JSON_KEY_CHARACTERISTIC_UUID, device.mCharacteristicUUID);
            deviceArray.put(deviceObj);
        }

        root.put(JSON_KEY_DEVICE, deviceArray);
        return root.toString();
    }

    /**
     * Judge if two objects are equal.
     * @param obj   The other object to compare.
     * @return  True if both objects have the same device address, service uuid and characteristic uuid.
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        } else {
            BleSavedDevice device = (BleSavedDevice)obj;
            return mDeviceAddress.equals(device.mDeviceAddress)
                    && mGattServiceUUID.equals(device.mGattServiceUUID)
                    && mCharacteristicUUID.equals(device.mCharacteristicUUID);
        }
    }
}
