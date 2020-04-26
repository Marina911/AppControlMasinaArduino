package com.example.aplicatiecontrolmasinaarduino;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

public class ControlVocal extends AppCompatActivity {
    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;
    private static String TEXT = null;
    private static final String TAG = "ControlVocal";
    private int maxChars = 50000;

    private BluetoothSocket bluetoothSocket;
    private ReadInput readThreads = null;
    private boolean isUserInitiatedDisconnect = false;
    private boolean isBluetoothConnected = false;
    private ProgressDialog progressDialog;

    private BluetoothDevice bluetoothDevice;
    private UUID deviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    Button buttonMicInregistrare;
    TextView textViewShowTextSpeak;

    final static String inainte = "1";
    final static String spate = "2";
    final static String stanga = "3";
    final static String dreapta = "4";
    final static String stop = "5";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_vocal);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActivityHelper.initialize(this);

        final Intent intent = getIntent();
        Bundle b = intent.getExtras();
        bluetoothDevice = b.getParcelable(ConectareBluetooth.DEVICE_EXTRA);
        maxChars = b.getInt(ConectareBluetooth.BUFFER_SIZE);

        Log.d(TAG, "Ready->deviceUUID:" + deviceUUID + " BluetoothDevice: "+ bluetoothDevice);
        checkPermission();

        textViewShowTextSpeak = findViewById(R.id.textViewShowTextSpeak);

        final SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                Log.d(TAG, "IN onResults() method!");
                ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null) {
                    String text = matches.get(0).toUpperCase();
                    textViewShowTextSpeak.setText(text);

                    Log.d(TAG, "Comanda vocala: " + matches.get(0));
                    if(text.equals("FORWARD") || text.equals("FRONT")){
                        try {
                            bluetoothSocket.getOutputStream().write(inainte.getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }else if(text.equals("BACKWARD") || text.equals("BACK")){
                        try {
                            bluetoothSocket.getOutputStream().write(spate.getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }else if(text.equals("LEFT")) {
                        try {
                            bluetoothSocket.getOutputStream().write(stanga.getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }else if(text.equals("RIGHT")){
                        try {
                            bluetoothSocket.getOutputStream().write(dreapta.getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }else if(text.equals("STOP")){
                        try {
                            bluetoothSocket.getOutputStream().write(stop.getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), "Comanda invalida! Incercati din nou!", Toast.LENGTH_SHORT).show();
                        try {
                            bluetoothSocket.getOutputStream().write(stop.getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }else{
                    Log.d(TAG, "Comanda vocala: " + null);
                }
                Log.d(TAG, "OUT onResults() method!");
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        buttonMicInregistrare = findViewById(R.id.buttonMicInregistrare);
        buttonMicInregistrare.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.d(TAG, "IN onTouch() method!");
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:
                        speechRecognizer.stopListening();
                        textViewShowTextSpeak.setHint("Apasati microfonul pentru a transmite comanda");
                        break;

                    case MotionEvent.ACTION_DOWN:

                        speechRecognizer.startListening(speechRecognizerIntent);
                        textViewShowTextSpeak.setText("");
                        textViewShowTextSpeak.setHint("Vorbeste!");

                        break;
                }
                Log.d(TAG, "OUT onTouch() method!");
                return false;
            }
        });
    }

    private void checkPermission() {
        Log.d(TAG, "IN checkPermission() method!");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                finish();
            }
        }
        Log.d(TAG, "OUT checkPermission() method!");
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
            progressDialog = ProgressDialog.show(ControlVocal.this, "Asteptati!", "Se conecteaza");
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
