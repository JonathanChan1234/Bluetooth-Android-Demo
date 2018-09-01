package com.jonathan.bluetoothcommunication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {
    public static final int BLUETOOTH_REQUEST = 400;

    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkBTState();
        ListView deviceListView = findViewById(R.id.deviceListView);
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        deviceListView.setAdapter(mPairedDevicesArrayAdapter);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairDevices = mBtAdapter.getBondedDevices();
        if(pairDevices.size() > 0) {
            for(BluetoothDevice device: pairDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            mPairedDevicesArrayAdapter.add("No device is found");
        }

        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);
                Intent intent = new Intent(MainActivity.this, DataActivity.class);
                intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
                startActivity(intent);
            }
        });
    }

    //Check bluetooth status --> enable bluetooth if not
    private void checkBTState() {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBtAdapter == null) {
            Toast.makeText(MainActivity.this, "Do not support bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            if(!mBtAdapter.isEnabled()) {
                Intent bluetoothEnableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(bluetoothEnableIntent, BLUETOOTH_REQUEST);
            }
        }
    }
}
