package com.jonathan.bluetoothcommunication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.UUID;

public class DataActivity extends AppCompatActivity {
    public static final String ERROR = "ERROR";
    ListView recordListView;
    TextView messageText;
    String address;

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    Handler handler;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    ConnectThread thread;
    ArrayAdapter<String> arrayAdapter;

    StringBuilder sb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        recordListView = findViewById(R.id.recordListView);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        recordListView.setAdapter(arrayAdapter);
        messageText = findViewById(R.id.messageText);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        sb = new StringBuilder();
        checkBTState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        address = intent.getStringExtra(MainActivity.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        Log.d("Device", device.getName());
        try {
            btSocket = createBluetoothSocket(device);
        } catch(IOException e) {
            Toast.makeText(DataActivity.this, "Socket creation failed", Toast.LENGTH_LONG).show();
        }

        try {
            btSocket.connect();
            Log.d(ERROR, "Socket connection success");
        } catch(IOException e) {
            Log.d(ERROR, "Socket connection fail");
            e.printStackTrace();
            try {
                btSocket.close();
                Log.d(ERROR, "Socket close success");
            } catch (IOException e1) {
                Log.d(ERROR, "Socket close fail");
                e1.printStackTrace();
            }
        }
        handler = new MyHandler(this);
        thread = new ConnectThread(btSocket);
        thread.start();
    }

    private class ConnectThread extends Thread {
        private final InputStream is;
        private final OutputStream os;

        ConnectThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
                Log.d(ERROR, "Get input/output stream success");
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(ERROR, "Get input/output stream fail");
            }
            is = tmpIn;
            os = tmpOut;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[256];
            int bytes;
            while(true) {
                try {
                    bytes = is.read(buffer);
                    String message = new String(buffer, 0, bytes);
                    handler.obtainMessage(0, bytes, -1, message).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(ERROR, "Read Data fail");
                    break;
                }
            }
        }
    }

    private static class MyHandler extends Handler {
        WeakReference<DataActivity> dataActivityWeakReference;
        MyHandler(DataActivity dataActivity) {
            dataActivityWeakReference = new WeakReference<>(dataActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            DataActivity dataActivity = dataActivityWeakReference.get();
            if(msg.what == 0) {
                String receivedMessage = (String) msg.obj;
                dataActivity.sb.append(receivedMessage);
                int endOfLineIndex = dataActivity.sb.indexOf("~");
                if(endOfLineIndex > 0 ) {
                    Log.d("Data received", dataActivity.sb.toString());
                    String dataInPrint = dataActivity.sb.substring(0, endOfLineIndex);
                    dataActivity.messageText.setText(dataInPrint);
                    dataActivity.arrayAdapter.add(dataInPrint);
                    dataActivity.sb.delete(0, dataActivity.sb.length());
                }
            }
        }
    }


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    private void checkBTState() {
        if (btAdapter == null) {
            Toast.makeText(DataActivity.this, "Do not support bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            if (!btAdapter.isEnabled()) {
                Intent bluetoothEnableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(bluetoothEnableIntent, 1);
            }
        }
    }
}
