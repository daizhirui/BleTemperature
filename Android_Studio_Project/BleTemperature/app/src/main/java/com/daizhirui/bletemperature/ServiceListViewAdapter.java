package com.daizhirui.bletemperature;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Adapter for storing GattServices found through scanning and generating correspondent view.
 */
class ServiceListViewAdapter extends BaseAdapter {
//    private static final String TAG = ServiceListViewAdapter.class.getSimpleName();
    class ViewHolder {
        boolean mHasNotify = false;
        TextView mServiceName;
        TextView mServiceUUID;
        TextView mCharacteristicList;
    }

    private List<BluetoothGattService> mGattServices;
    private LayoutInflater mInflator;

    ServiceListViewAdapter(LayoutInflater inflater) {
        super();
        mGattServices = new ArrayList<>();
        mInflator = inflater;
    }

    void addService(BluetoothGattService service) {
        if (!mGattServices.contains(service)) {
            mGattServices.add(service);
        }
    }

    void clear() { mGattServices.clear(); }

    @Override
    public int getCount() { return mGattServices.size(); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public BluetoothGattService getItem(int position) {
        if (position < mGattServices.size()) {
            return mGattServices.get(position);
        } else {
            return null;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (position < mGattServices.size()) {
            ViewHolder viewHolder;

            if (convertView == null) {
                convertView = mInflator.inflate(R.layout.list_gatt_service, null);
                viewHolder = new ViewHolder();
                viewHolder.mServiceName = convertView.findViewById(R.id.gatt_service_name);
                viewHolder.mServiceUUID = convertView.findViewById(R.id.gatt_service_uuid);
                viewHolder.mCharacteristicList = convertView.findViewById(R.id.gatt_service_characteristic);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            BluetoothGattService service = mGattServices.get(position);
            final UUID serviceUUID = service.getUuid();
            String serviceName = GattStandard.parseServiceName(serviceUUID);
            final List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            String characteristicListString = "";
            viewHolder.mHasNotify = false;
            for (BluetoothGattCharacteristic characteristic : characteristics) {
                characteristicListString = characteristicListString.concat(GattStandard.parseCharacteristicName(characteristic.getUuid()));
                if (GattStandard.parseCharacteristicProperty(characteristic.getProperties()).contains(GattStandard.CHARACTERISTIC_PROPERTY_NOTIFY)) {
                    characteristicListString = characteristicListString.concat(" (Notify)");
                    viewHolder.mHasNotify = true;
                }
                characteristicListString = characteristicListString.concat("\n");
            }

            viewHolder.mServiceName.setText(serviceName);
            viewHolder.mServiceUUID.setText(serviceUUID.toString());
            if (characteristicListString.length() > 0) {
                viewHolder.mCharacteristicList.setText(characteristicListString);
            } else {
                viewHolder.mCharacteristicList.setText(R.string.empty);
                viewHolder.mCharacteristicList.setText(viewHolder.mCharacteristicList.getText().toString().concat("\n"));
            }

            return convertView;
        } else {
            return null;
        }
    }
}
