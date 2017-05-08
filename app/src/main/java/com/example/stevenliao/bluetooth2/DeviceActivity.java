package com.example.stevenliao.bluetooth2;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;


public class DeviceActivity extends AppCompatActivity {

    private static final String TAG = "DeviceActivity";

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ACCESS_RESULT = 1;
    private BluetoothAdapter mBtadapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1);

        //ACCESS Permission
        int permissionCheck = ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "ACCESS NO", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this, new String[] {ACCESS_COARSE_LOCATION}, REQUEST_ACCESS_RESULT );
        }
        else{
            Toast.makeText(this, "ACCESS YES", Toast.LENGTH_LONG).show();
        }
        //ACCESS Permission End

        mBtadapter = BluetoothAdapter.getDefaultAdapter();  // See if bluetooth is On
        if(mBtadapter == null){
            Toast.makeText(this, "No Bluetooth Support", Toast.LENGTH_LONG).show();
        }
        if(!mBtadapter.isEnabled()){
            Toast.makeText(this, "Bluetooth OFF and open now", Toast.LENGTH_LONG).show();
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, REQUEST_ENABLE_BT);
        }

        ListView newDevicesListView = (ListView) findViewById(R.id.ListView1);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDevicesClickListener);
        mNewDevicesArrayAdapter.add("Start Scan");

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        Button startBtn = (Button) findViewById(R.id.button1);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Start Discovery");
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

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == REQUEST_ENABLE_BT){
            if(resultCode == RESULT_OK){
                Toast.makeText(this, "BlueTooth is ON", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void doDiscovery() {
        Toast.makeText(this, "BlueTooth is Scanning", Toast.LENGTH_LONG).show();
        mBtadapter.startDiscovery();
    }

    private void quitDiscovery() {
        Toast.makeText(this, "Stop Scanning", Toast.LENGTH_LONG).show();
        mBtadapter.cancelDiscovery();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                Toast.makeText(DeviceActivity.this, "Got Device", Toast.LENGTH_LONG).show();
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(DeviceActivity.this, "Scanning Finished", Toast.LENGTH_LONG).show();
                mNewDevicesArrayAdapter.add("noDevices");

            }

        }
    };

    private AdapterView.OnItemClickListener mDevicesClickListener
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBtadapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            Intent intent = new Intent(DeviceActivity.this, BlueToothChat.class);
            intent.putExtra("EXTRA_DEVICE_ADDRESS", address);
            startActivity(intent);

            // Set result and finish this Activity


        }
    };
}
