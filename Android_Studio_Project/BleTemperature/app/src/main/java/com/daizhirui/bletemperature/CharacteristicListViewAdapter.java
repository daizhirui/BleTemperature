package com.daizhirui.bletemperature;

import android.bluetooth.BluetoothGattCharacteristic;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for storing characteristics found through scanning and generating correspondent view.
 */
class CharacteristicListViewAdapter extends BaseAdapter {

    class ViewHolder {
        TextView mCharacteristicName;
        TextView mCharacteristicUUID;
    }
    private List<BluetoothGattCharacteristic> mCharacteristics = new ArrayList<>();
    private LayoutInflater mInflator;

    CharacteristicListViewAdapter(LayoutInflater inflater) {
        mInflator = inflater;
    }

    void addCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (!mCharacteristics.contains(characteristic)) {
            mCharacteristics.add(characteristic);
        }
    }


    @Override
    public int getCount() {
        return mCharacteristics.size();
    }

    @Override
    public BluetoothGattCharacteristic getItem(int position) {
        if (position < mCharacteristics.size()) {
            return mCharacteristics.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflator.inflate(R.layout.list_characteristic, null);
            viewHolder = new ViewHolder();
            viewHolder.mCharacteristicName = convertView.findViewById(R.id.characteristic_name);
            viewHolder.mCharacteristicUUID = convertView.findViewById(R.id.characteristic_uuid);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (position < mCharacteristics.size()) {
            BluetoothGattCharacteristic characteristic = mCharacteristics.get(position);
            viewHolder.mCharacteristicName.setText(GattStandard.parseCharacteristicName(characteristic.getUuid()));
            if (GattStandard.parseCharacteristicProperty(characteristic.getProperties()).contains(GattStandard.CHARACTERISTIC_PROPERTY_NOTIFY)) {
                String characteristicName = viewHolder.mCharacteristicName.getText().toString();
                viewHolder.mCharacteristicName.setText(characteristicName.concat(" (Notify)"));
            }
            viewHolder.mCharacteristicUUID.setText(characteristic.getUuid().toString());
            return convertView;
        }

        return null;
    }
}
