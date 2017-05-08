package com.example.stevenliao.bluetooth2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static java.lang.Boolean.TRUE;

public class BlueToothChat extends AppCompatActivity {

    private static final UUID MY_UUID =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final String NAME_SECURE = "BluetoothChatSecure";
    private BluetoothAdapter mAdapter;
    private ConnectThread mConnectThread;
    private AcceptThread mAcceptThread;
    private ConnectedThread mConnectedThread;
    private TextView MailView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_tooth_chat);
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        final TextView AddressView = (TextView) findViewById(R.id.textView1);
        MailView = (TextView) findViewById(R.id.textView);
        Button ListenBtn = (Button) findViewById(R.id.ListenBtn);
        Button ConnectBtn = (Button) findViewById(R.id.ConnectBtn);
        Button SendBtn = (Button) findViewById(R.id.SendBtn);

        Intent intent = getIntent();
        String address = intent.getStringExtra("EXTRA_DEVICE_ADDRESS");
        Toast.makeText(this, address, Toast.LENGTH_LONG).show();
        AddressView.setText(address);
        final BluetoothDevice device = mAdapter.getRemoteDevice(address);

        ConnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            mConnectThread = new ConnectThread(device);
            mConnectThread.start();
            Toast.makeText(BlueToothChat.this, "start to connect", Toast.LENGTH_SHORT).show();
            }
        });

        ListenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
            Toast.makeText(BlueToothChat.this, "start to listen", Toast.LENGTH_SHORT).show();
            }
        });

        SendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            String message = "Ya Success";
            byte[] send = (message + " ").getBytes();
            send[send.length - 1] = 0;
            write(send);
            }
        });


    }



    public void write(byte[] out) {

        ConnectedThread r;
        r = mConnectedThread;
        r.write(out);
    }

    public void connected(BluetoothSocket socket) {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

    }


    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
          mmDevice = device;
          BluetoothSocket tmp = null;

          try {
          tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
          Toast.makeText(BlueToothChat.this, "Get Socket", Toast.LENGTH_SHORT).show();
          } catch (IOException e) {}
          mmSocket = tmp;

        }
        public void run() {

          mAdapter.cancelDiscovery();
          Message connectmsg = new Message();

          try {
                mmSocket.connect();

          if(mmSocket.isConnected())
            {
                connectmsg.what = 0;
                connectmsg.arg1 = 1;
                handler.sendMessage(connectmsg);
                connected(mmSocket);
            }

          } catch (IOException e) {
              Log.e("ConnectThread", e.getMessage());
              try {
                  mmSocket.close();
                  }
              catch (IOException e1) { }

              return;}
           synchronized (BlueToothChat.this) {
              mConnectThread = null;
           }
        }

        public void cancel() {

        }
    }

    private class AcceptThread extends Thread {

        private final BluetoothServerSocket mmServerSocket;
        private int listenstatus = 0;
        private int test1 = 0;
        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            try {
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, MY_UUID);
                Toast.makeText(BlueToothChat.this, "Listen socket go~", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {

            }
            mmServerSocket = tmp;
        }

        public void run() {

            Message Acceptmsg = new Message();
            BluetoothSocket socket = null;

                Acceptmsg.what = 0;
                Acceptmsg.arg1 = 2;
                handler.sendMessage(Acceptmsg);
                listenstatus = 0;


            while (listenstatus == 0) {

                try {
                    socket = mmServerSocket.accept();

                } catch (IOException e) {
                Log.e("ListenThead", e.getMessage());

                }


            if (socket != null) {
                listenstatus = 1;
                connected(socket);
            }
        }

        }

        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {

            }
        }
    }

    private class ConnectedThread extends Thread {
        private  final BluetoothSocket mmSocket;
        private  final InputStream mmInStream;
        private  final OutputStream mmOutStream;
        Message connectedmsg = new Message();
        int check = 0;
        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            connectedmsg.what = 3;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }catch (IOException e) {
                Log.e("Connected", e.getMessage());
            }
            connectedmsg.arg1 = 1;
            handler.sendMessage(connectedmsg);
            mmInStream = tmpIn;
            mmOutStream = tmpOut;


        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
                    while (true) {
                        try {

                            bytes = mmInStream.read(buffer);
                            handler.obtainMessage(1, bytes, -1, buffer).sendToTarget();
                        } catch (IOException e) {
                            Log.e("ConnectedThread1", e.getMessage());
                        }
                    }

        }

        public void write(byte[] buffer) {

            try {

                mmOutStream.write(buffer);
                mmOutStream.flush();


            } catch (IOException e) {
                Log.e("WriteThread",e.getMessage());
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            }catch (IOException e) {

            }
        }
    }



   private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    switch (msg.arg1) {
                        case 1:
                            Toast.makeText(BlueToothChat.this, "Connected OK", Toast.LENGTH_SHORT).show();
                            break;
                        case 2:
                            Toast.makeText(BlueToothChat.this, "Socket is null and start to listen", Toast.LENGTH_SHORT).show();
                            break;
                        case 3:
                            Toast.makeText(BlueToothChat.this, "Listen Completed", Toast.LENGTH_SHORT).show();
                            break;
                        case 4:
                            Toast.makeText(BlueToothChat.this, "Listen Got it", Toast.LENGTH_SHORT).show();
                            break;
                        case 5:
                            Toast.makeText(BlueToothChat.this, "Get into while()", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
                case 1:
                    String test = "bytes is";
                    byte[] ReadBuf = (byte[]) msg.obj;
                    String readMsg = new String(ReadBuf, 0, msg.arg1);
                    Toast.makeText(BlueToothChat.this,test + readMsg, Toast.LENGTH_SHORT).show();
                    break;

                case 2:
                    switch (msg.arg1) {
                        case 1:
                            Toast.makeText(BlueToothChat.this, "While Listen fail()", Toast.LENGTH_SHORT).show();
                            break;

                    }
                    break;

                case 3:
                    switch (msg.arg1) {
                        case 1:
                            Toast.makeText(BlueToothChat.this, "Connected build OK", Toast.LENGTH_SHORT).show();
                            MailView.setText("Connected mode");
                            break;
                        case 2:
                            Toast.makeText(BlueToothChat.this, "mmInstream", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
            }
        }
    };

}