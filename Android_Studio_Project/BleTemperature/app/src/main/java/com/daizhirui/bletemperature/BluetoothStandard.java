package com.daizhirui.bletemperature;

import android.bluetooth.BluetoothDevice;

/**
 * BluetoothStandard provide some methods that parse some values and return the concrete meaning
 * of the provided values.
 */
public class BluetoothStandard {
    public static String parseDeviceType(int type) {
        switch (type) {
            case BluetoothDevice.DEVICE_TYPE_UNKNOWN: return "Unknown";
            case BluetoothDevice.DEVICE_TYPE_CLASSIC: return "Classic - BR/EDR";
            case BluetoothDevice.DEVICE_TYPE_LE: return "Low Energy";
            case BluetoothDevice.DEVICE_TYPE_DUAL: return "Dual Mode - BR/EDR/LE";
            default: return "Undefined";
        }
    }

//    mSelectedBluetoothDevice.getBluetoothClass();  // define device class: computer, audio etc.

    public static String parseDeviceBondState(int bondState) {
        switch (bondState) {
            case BluetoothDevice.BOND_NONE: return "Not bonded";
            case BluetoothDevice.BOND_BONDING: return "Bonding";
            case BluetoothDevice.BOND_BONDED: return "Bonded";
            default: return "Undefined bond state";
        }
    }
}
