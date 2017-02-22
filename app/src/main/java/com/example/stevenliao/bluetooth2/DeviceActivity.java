package com.example.stevenliao.bluetooth2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;


public class DeviceActivity extends AppCompatActivity {

    private BluetoothAdapter mBtadapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1);
        mBtadapter = BluetoothAdapter.getDefaultAdapter();

        ListView newDevicesListView = (ListView) findViewById(R.id.ListView1);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        mNewDevicesArrayAdapter.add("Start Scan");

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        Button startBtn = (Button) findViewById(R.id.button1);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doDiscovery();
            }
        });

        Button cancelBtn = (Button) findViewById(R.id.button2);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quitDiscovery();
            }
        });
    }

    private void doDiscovery() {
        mBtadapter.startDiscovery();
    }

    private void quitDiscovery() {
        mBtadapter.cancelDiscovery();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    mNewDevicesArrayAdapter.add("noDevices");
                }
            }

        }
    };
}
