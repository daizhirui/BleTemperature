package com.daizhirui.bletemperature;

import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Adapter for storing devices found through scanning and generating correspondent view.
 */
class BleDeviceListViewAdapter extends BaseAdapter {

    /**
     * ViewHolder for connecting the view components.
     */
    class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

    /**
     * BleDevice for storing information of a list item.
     */
    class BleDevice {
        String deviceName;
        String deviceAddress;

        BleDevice(String name, String address) {
            deviceName = name;
            deviceAddress = address;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj == null) {
                return false;
            } else {
                return ((BleDevice) obj).deviceAddress.equals(deviceAddress);
            }
        }
    }

    private ArrayList<BleDevice> mLeDevices;
    private LayoutInflater mInflator;

    /**
     * BleDeviceListViewAdapter Constructor.
     * @param inflater  The LayoutInflater of the context which holds the ListView that uses this BleDeviceListViewAdapter.
     */
    BleDeviceListViewAdapter(LayoutInflater inflater) {
        super();
        mLeDevices = new ArrayList<>();
        mInflator = inflater;
    }

    void addDevice(String name, String address) {
        BleDevice device = new BleDevice(name, address);
        if (!this.mLeDevices.contains(device)) {
            this.mLeDevices.add(device);
        } else {
            device = mLeDevices.get(mLeDevices.indexOf(device));
            device.deviceName = name;
            device.deviceAddress = address;
        }
    }

    BleDevice getDevice(int position) {
        return this.mLeDevices.get(position);
    }

    void clear() {
        this.mLeDevices.clear();
    }

    @Override
    public int getCount() {
        return this.mLeDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return this.mLeDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        // General ListView optimization code.
        if (convertView == null) { // no view yet
            convertView = this.mInflator.inflate(R.layout.list_ble_device, null);
            viewHolder = new ViewHolder();
            viewHolder.deviceAddress = convertView.findViewById(R.id.device_address);
            viewHolder.deviceName = convertView.findViewById(R.id.device_name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (position < mLeDevices.size()) {

            BleDevice device = this.mLeDevices.get(position);
            if (device.deviceName != null && device.deviceName.length() > 0) {
                viewHolder.deviceName.setText(device.deviceName);
            } else {
                viewHolder.deviceName.setText(R.string.unknown_device);
            }
            viewHolder.deviceAddress.setText(device.deviceAddress);

            return convertView;
        } else {
            return null;
        }
    }
}
