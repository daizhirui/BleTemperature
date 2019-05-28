package com.daizhirui.bletemperature;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for storing saved devices and generating correspondent view.
 */
class BleSavedDeviceListViewAdapter extends BaseAdapter {
    private static final String TAG = BleSavedDeviceListViewAdapter.class.getSimpleName();

    class ViewHolder {
        TextView mDeviceName;
        TextView mDeviceAddress;
        TextView mServiceUUID;
        TextView mCharacteristicUUID;
    }

    List<BleSavedDevice> mBleSavedDevices;
    private LayoutInflater mInflator;

    /**
     * BleSavedDeviceListViewAdapter Constructor.
     * @param inflater  The LayoutInflater of the context which holds the ListView that uses this BleDeviceListViewAdapter.
     */
    BleSavedDeviceListViewAdapter(LayoutInflater inflater) {
        mInflator = inflater;
        mBleSavedDevices = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mBleSavedDevices.size();
    }

    @Override
    public BleSavedDevice getItem(int position) {
        if (position < mBleSavedDevices.size()) {
            return mBleSavedDevices.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position < mBleSavedDevices.size()) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = mInflator.inflate(R.layout.list_saved_device, null);
                viewHolder = new ViewHolder();
                viewHolder.mDeviceName = convertView.findViewById(R.id.saved_device_name);
                viewHolder.mDeviceAddress = convertView.findViewById(R.id.saved_device_address);
                viewHolder.mServiceUUID = convertView.findViewById(R.id.saved_service_uuid);
                viewHolder.mCharacteristicUUID = convertView.findViewById(R.id.saved_characteristic_uuid);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            BleSavedDevice device = mBleSavedDevices.get(position);
            viewHolder.mDeviceName.setText(device.mDeviceName);
            viewHolder.mDeviceAddress.setText(device.mDeviceAddress);
            viewHolder.mServiceUUID.setText(device.mGattServiceUUID.toString());
            viewHolder.mCharacteristicUUID.setText(device.mCharacteristicUUID.toString());

            if (device.mSelected) {
                convertView.setBackgroundColor(mInflator.getContext().getColor(android.R.color.holo_blue_light));
            } else {
                convertView.setBackgroundColor(mInflator.getContext().getColor(android.R.color.transparent));
            }

            return convertView;
        }
        return null;
    }

    void addDevice(BleSavedDevice device) {
        if (!mBleSavedDevices.contains(device)) {
            mBleSavedDevices.add(device);
        }
    }

    boolean removeDevice(BleSavedDevice device) {
        return mBleSavedDevices.remove(device);
    }
}
