package com.example.aplicatiecontrolmasinaarduino;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ConectareBluetooth extends AppCompatActivity {
    private Button cautare;
    private Button conectare;
    private ListView listView;
    private BluetoothAdapter bluetoothAdapter;
    private static final int BLUETOOTH_ENABLE_REQUEST = 10;
    private static final int SETARI = 20;
    private UUID deviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private int bufferSize = 50000;
    public static final String DEVICE_EXTRA = "com.example.aplicatiecontrolmasinaarduino.SOCKET";
    public static final String DEVICE_UUID = "com.example.aplicatiecontrolmasinaarduino.dispUUID";
    private static final String DEVICE_LIST = "com.example.aplicatiecontrolmasinaarduino.devicelist";
    private static final String SELECTED_DEVICE_LIST = "com.example.aplicatiecontrolmasinaarduino.devicelistselected";
    public static final String BUFFER_SIZE = "com.example.aplicatiecontrolmasinaarduino.buffersize";
    private static final String TAG = "ConectareBluetooth";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "->In onCreate method!");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conectare_bluetooth);

        cautare = findViewById(R.id.buttonCautare);
        listView = findViewById(R.id.list_view);

        if(savedInstanceState != null ){
            ArrayList<BluetoothDevice> listaDeDispozitiveBluetooth = savedInstanceState.getParcelableArrayList(DEVICE_LIST);
            if(listaDeDispozitiveBluetooth != null){
                initList(listaDeDispozitiveBluetooth);
                MyAdapter adapter = (MyAdapter) listView.getAdapter();
                int indexSelectat = savedInstanceState.getInt(SELECTED_DEVICE_LIST);
                if(indexSelectat != -1){
                    adapter.setSelectedIndex(indexSelectat);
                    conectare.setEnabled(true);
                    Log.d(TAG, "IndexSelectat: " + indexSelectat);
                }
            }else{
                initList(new ArrayList<BluetoothDevice>());
            }
        }else{
            initList(new ArrayList<BluetoothDevice>());
        }

        cautare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Log.d(TAG, "->In onClick cautareButton method!");
                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter == null) {
                    Toast.makeText(getApplicationContext(), "Bluetooth neidentificat!", Toast.LENGTH_SHORT).show();
                } else if (!bluetoothAdapter.isEnabled()) {
                    Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBluetooth, BLUETOOTH_ENABLE_REQUEST);
                } else {
                    new SearchDevices().execute();
                }
                Log.d(TAG, "->OUT onClick cautareButton method!");
            }
        });
        Log.d(TAG, "->OUT onCreate method!");
    }

    protected void onPause() {
        Log.d(TAG, "->IN onPause method!");
        super.onPause();
        Log.d(TAG, "->OUT onPause method!");
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "->IN onStop method!");
        super.onStop();
        Log.d(TAG, "->OUT onStop method!");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "->IN onActivityResult method!");
        switch (requestCode) {
            case BLUETOOTH_ENABLE_REQUEST:
                if (resultCode == RESULT_OK) {
                    message("Bluetooth conectat cu succes!");
                    new SearchDevices().execute();
                } else {
                    message("Bluetooth nu s-a putut conecta!");
                }
                break;
            case SETARI:
                SharedPreferences preferinte = PreferenceManager.getDefaultSharedPreferences(this);
                String uuid = preferinte.getString("preferintaUuid", "Null");
                deviceUUID = UUID.fromString(uuid);
                Log.d(TAG, "UUID: " + uuid);
                String bufSize = preferinte.getString("preferintaTextBuffer", "Null");
                bufferSize = Integer.parseInt(bufSize);

                String orientare = preferinte.getString("preferintaOrientare", "Null");
                Log.d(TAG, "Orientare: " + orientare);
                if (orientare.equals("Landscape")) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else if (orientare.equals("Portrait")) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else if (orientare.equals("Auto")) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "->OUT onActivityResult method!");
    }

    private void message(String str) {
        Log.d(TAG, "->IN message method!");
        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "->OUT message method!");
    }

    private class SearchDevices extends AsyncTask<Void, Void, List<BluetoothDevice>>{

        @Override
        protected List<BluetoothDevice> doInBackground(Void... voids) {
            Log.d(TAG, "->IN doInBackground method!");
            Set<BluetoothDevice> dispozitiveImperechiate = bluetoothAdapter.getBondedDevices();
            List<BluetoothDevice> listaDispozitive = new ArrayList<BluetoothDevice>();
            for (BluetoothDevice dispozitive : dispozitiveImperechiate) {
                listaDispozitive.add(dispozitive);
            }
            Log.d(TAG, "->IN doInBackground method! Lista dispozitive: " + listaDispozitive);
            return listaDispozitive;
        }

        @Override
        protected void onPostExecute(List<BluetoothDevice> listDevices) {
            Log.d(TAG, "->IN onPostExecute method!");
            super.onPostExecute(listDevices);
            if (listDevices.size() > 0) {
                MyAdapter adapter = (MyAdapter) listView.getAdapter();
                adapter.replaceItems(listDevices);
            } else {
                message("Nu au fost gasite dispozitive, te rog conecteaza dispozitivul Bluetooth si incearca din nou!");
            }
            Log.d(TAG, "->OUT onPostExecute method!");
        }

    }

    private void initList(List<BluetoothDevice> obiecte) {
        Log.d(TAG, "->IN initList method!");
        final MyAdapter adapter = new MyAdapter(getApplicationContext(), R.layout.list_item, R.id.lstContent, obiecte);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pozitie, long id) {
                adapter.setSelectedIndex(pozitie);
                try {
                    if(conectare != null){
                        conectare.setEnabled(true);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        Log.d(TAG, "->OUT initList method!");
    }

    private class MyAdapter extends ArrayAdapter<BluetoothDevice> {

        private int indexSlectat;
        private Context context;
        private int culoareSelectata = Color.parseColor("#D7106C99");
        private List<BluetoothDevice> listaDeviceConectate;

        public MyAdapter(Context parametruContext, int resursa, int textViewResursaId, List<BluetoothDevice> obiecte) {
            super(parametruContext, resursa, textViewResursaId, obiecte);
            context = parametruContext;
            listaDeviceConectate = obiecte;
            indexSlectat = -1;
        }

        public void setSelectedIndex(int pozitie) {
            indexSlectat = pozitie;
            notifyDataSetChanged();
        }

        public BluetoothDevice getSelectedItem(){
            Log.d(TAG, "->IN getSelectedItem method!");
            BluetoothDevice deviceSelectat = null;
            try{
                deviceSelectat =  listaDeviceConectate.get(indexSlectat);
            }catch (IndexOutOfBoundsException e){
                Toast.makeText(ConectareBluetooth.this, "Alege un dispozitiv!", Toast.LENGTH_SHORT).show();
            }
            Log.d(TAG, "->OUT getSelectedItem method! DeviceSelectat: " + deviceSelectat);
            return deviceSelectat;
        }

        @Override
        public int getCount() {
            return listaDeviceConectate.size();
        }

        @Override
        public BluetoothDevice getItem(int position) {
            return listaDeviceConectate.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void replaceItems(List<BluetoothDevice> list) {
            listaDeviceConectate = list;
            notifyDataSetChanged();
        }

        private class ViewHolder {
            TextView textView;
        }

        @Override
        public View getView(int pozitie, View convertView, ViewGroup parent) {
            Log.d(TAG, "->IN getView method!");
            View view = convertView;
            ViewHolder holder;
            if (convertView == null) {
                view = LayoutInflater.from(context).inflate(R.layout.list_item, null);
                holder = new ViewHolder();
                holder.textView = view.findViewById(R.id.listaContent);
                view.setTag(holder);

            } else {
                holder = (ViewHolder) view.getTag();
            }

            if (indexSlectat != -1 && pozitie == indexSlectat) {
                holder.textView.setBackgroundColor(culoareSelectata);
                BluetoothDevice device = ((MyAdapter)(listView.getAdapter())).getSelectedItem();
                if(device == null){
                    Toast.makeText(ConectareBluetooth.this,"Alege un dispozitiv!", Toast.LENGTH_LONG).show();
                }else {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra(DEVICE_EXTRA, device);
                    intent.putExtra(DEVICE_UUID, deviceUUID.toString());
                    intent.putExtra(BUFFER_SIZE, bufferSize);
                    startActivity(intent);
                }
            } else {
                holder.textView.setBackgroundColor(Color.WHITE);
            }
            BluetoothDevice device = listaDeviceConectate.get(pozitie);
            holder.textView.setText(device.getName() + "\n " + device.getAddress());
            Log.d(TAG, "->OUT getView method! View: " + view);
            return view;
        }
    }
}