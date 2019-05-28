package com.daizhirui.bletemperature;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;


/**
 * Show the characteristics of the selected GattService and allow users to select one from them.
 */
public class SelectCharacteristicActivity extends BluetoothLeBaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private CharacteristicListViewAdapter mCharacteristicListViewAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectchara);
        setTitle("");

        ListView listView = findViewById(R.id.characteristic_listview);
        mCharacteristicListViewAdapter = new CharacteristicListViewAdapter(getLayoutInflater());
        listView.setAdapter(mCharacteristicListViewAdapter);
        listView.setOnItemClickListener(this);

        if (mBluetoothLeService == null || mBluetoothLeService.mSelectedBluetoothGattService == null) {
            finish();
        }
        for (BluetoothGattCharacteristic characteristic : mBluetoothLeService.mSelectedBluetoothGattService.getCharacteristics()) {
            mCharacteristicListViewAdapter.addCharacteristic(characteristic);
        }
        mCharacteristicListViewAdapter.notifyDataSetChanged();

        Button button = findViewById(R.id.cancel_button);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.cancel_button) {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position < mCharacteristicListViewAdapter.getCount()) {
            mBluetoothLeService.mSelectedBluetoothGattCharacteristic = mCharacteristicListViewAdapter.getItem(position);
            setResult(RESULT_OK);
            finish();
        }
    }
}
