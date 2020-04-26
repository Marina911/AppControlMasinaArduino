package com.example.aplicatiecontrolmasinaarduino;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "MainActivity";
    Button button_Sageti, button_bluetooth, button_tip_control, button_vocal, button_anti_collision_off;

    private int maxChars = 50000;
   //private UUID deviceUUID;
    private BluetoothDevice bluetoothDevice;

   //private UUID deviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private int bufferSize = 50000;
    public static final String DEVICE_EXTRA = "com.example.aplicatiecontrolmasinaarduino.SOCKET";
    //public static final String DEVICE_UUID = "com.example.aplicatiecontrolmasinaarduino.dispUUID";
    public static final String BUFFER_SIZE = "com.example.aplicatiecontrolmasinaarduino.buffersize";

    private ReadInput readThreads = null;
    private UUID deviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private boolean isUserInitiatedDisconnect = false;
    private boolean isBluetoothConnected = false;
    private ProgressDialog progressDialog;

    private BluetoothSocket bluetoothSocket;
    final static String control_manual = "6";
    final static String control_automat = "7";
    Boolean isControlManual = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        bluetoothDevice = b.getParcelable(ConectareBluetooth.DEVICE_EXTRA);
        //deviceUUID = UUID.fromString(b.getString(ConectareBluetooth.DEVICE_UUID));
        maxChars = b.getInt(ConectareBluetooth.BUFFER_SIZE);

        button_Sageti = findViewById(R.id.buttonSageti);
        button_Sageti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ControlSageti.class);
                intent.putExtra(DEVICE_EXTRA, bluetoothDevice);
               //intent.putExtra(DEVICE_UUID, deviceUUID);
                intent.putExtra(BUFFER_SIZE, maxChars);
                Log.d(TAG, "Ready->deviceUUID:" + deviceUUID + " BluetoothDevice: "+ bluetoothDevice);
                startActivity(intent);
                try {
                    bluetoothSocket.getOutputStream().write(control_manual.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                onPause();
                try {
                    bluetoothSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        button_bluetooth = findViewById(R.id.buttonBluetooth);
        button_bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deschideConectareBluetooth();
            }
        });

        button_tip_control = findViewById(R.id.buttonTipControl);
        button_tip_control.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (isControlManual) {
                        button_tip_control.setBackgroundResource(R.drawable.automat);
                        bluetoothSocket.getOutputStream().write(control_automat.getBytes());
                        isControlManual = false;
                    } else {
                        button_tip_control.setBackgroundResource(R.drawable.manual);
                        bluetoothSocket.getOutputStream().write(control_manual.getBytes());
                        isControlManual = true;
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        });

        button_vocal = findViewById(R.id.buttonVocal);
        button_vocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ControlVocal.class);
                intent.putExtra(DEVICE_EXTRA, bluetoothDevice);
                //intent.putExtra(DEVICE_UUID, deviceUUID);
                intent.putExtra(BUFFER_SIZE, maxChars);
                Log.d(TAG, "Ready->deviceUUID:" + deviceUUID + " BluetoothDevice: "+ bluetoothDevice);
                startActivity(intent);
                try {
                    bluetoothSocket.getOutputStream().write(control_manual.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                onPause();
                try {
                    bluetoothSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        button_anti_collision_off = findViewById(R.id.buttonControlAntiCollisionOff);
        button_anti_collision_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AntiCollisionOff.class);
                intent.putExtra(DEVICE_EXTRA, bluetoothDevice);
                //intent.putExtra(DEVICE_UUID, deviceUUID);
                intent.putExtra(BUFFER_SIZE, maxChars);
                Log.d(TAG, "Ready->deviceUUID:" + deviceUUID + " BluetoothDevice: "+ bluetoothDevice);
                startActivity(intent);
                try {
                    bluetoothSocket.getOutputStream().write(control_manual.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                onPause();
                try {
                    bluetoothSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


    }


    @Override
    public void onClick(View v) {

    }

    public void deschideConectareBluetooth(){
        Intent intent = new Intent(MainActivity.this, ConectareBluetooth.class);
        startActivity(intent);
    }

    private class ReadInput implements Runnable {

        private boolean isStopThread = false;
        private Thread thread;

        public ReadInput() {
            Log.d(TAG, "IN ReadInput method!" );
            thread = new Thread(this, "Input Thread");
            thread.start();
            Log.d(TAG, "OUT ReadInput method! Thread: " + thread );
        }

        public boolean isRunning() {
            Log.d(TAG, "IN isRunning method!" );
            return thread.isAlive();
        }

        @Override
        public void run() {
            Log.d(TAG, "IN run method!" );
            InputStream inputStream;
            try {
                inputStream = bluetoothSocket.getInputStream();
                while (!isStopThread) {
                    byte[] buffer = new byte[256];
                    if (inputStream.available() > 0) {
                        inputStream.read(buffer);
                        int i = 0;
                        //This is needed because new String(buffer) is taking the entire buffer i.e. 256 chars on Android 2.3.4
                        for (i = 0; i < buffer.length && buffer[i] != 0; i++) {
                        }
                        final String strInput = new String(buffer, 0, i);
                        Log.d(TAG, "strInput: " + strInput );
                    }
                    Thread.sleep(500);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "OUT run method!" );
        }

        public void stop() {
            Log.d(TAG, "IN stop method!" );
            isStopThread = true;
            Log.d(TAG, "OUT stop method!" );
        }
    }

    private class DisconnectBluetooth extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d(TAG, "IN doInBackground method!" );

            if (readThreads != null) {
                readThreads.stop();
                while (readThreads.isRunning())
                    ;
                readThreads = null;
            }
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "OUT doInBackground method!");
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.d(TAG, "IN onPostExecute method!");
            super.onPostExecute(result);
            isBluetoothConnected = false;
            if (isUserInitiatedDisconnect) {
                finish();
            }
            Log.d(TAG, "OUT onPostExecute method!");
        }

    }

    private void message(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "IN onPause method!");
        if (bluetoothSocket != null && isBluetoothConnected) {
            new DisconnectBluetooth().execute();
        }
        Log.d(TAG, "Paused");
        super.onPause();
        Log.d(TAG, "OUT onPause method!");
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "IN onResume method!");
        if (bluetoothSocket == null || !isBluetoothConnected) {
            new ConnectBluetooth().execute();
        }
        Log.d(TAG, "Resumed");
        super.onResume();
        Log.d(TAG, "OUT onResume method!");
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "Stopped!");
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "IN onSaveInstanceState method!");
        super.onSaveInstanceState(outState);
        Log.d(TAG, "OUT onSaveInstanceState method!");
    }

    private class ConnectBluetooth extends AsyncTask<Void, Void, Void> {
        private boolean isConnectSuccessful = true;

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "IN onPreExecute method!");
            progressDialog = ProgressDialog.show(MainActivity.this, "Asteptati", "Se conecteaza");
            Log.d(TAG, "OUT onPreExecute method!");
        }

        @Override
        protected Void doInBackground(Void... devices) {
            Log.d(TAG, "IN doInBackground method!");
            try {
                if (bluetoothSocket == null || !isBluetoothConnected) {
                    bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(deviceUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    bluetoothSocket.connect();
                }
            } catch (IOException e) {
                e.printStackTrace();
                isConnectSuccessful = false;
            }
            Log.d(TAG, "OUT doInBackground method!");
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.d(TAG, "IN onPostExecute method!");
            super.onPostExecute(result);
            Log.d(TAG, "isConnectSuccessful: "+ isConnectSuccessful);
            if (!isConnectSuccessful) {
                Toast.makeText(getApplicationContext(), "Nu s-a putut conecta, verificati dispositivul hardware!", Toast.LENGTH_LONG).show();
                finish();
            } else {
                message("Conectat la dispozitiv!");
                isBluetoothConnected = true;
                readThreads = new ReadInput();
            }
            progressDialog.dismiss();
            Log.d(TAG, "OUT onPostExecute method!");
        }

    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "IN onDestroy method!");
        super.onDestroy();
        Log.d(TAG, "OUT onDestroy method!");
    }
}