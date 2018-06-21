package com.smartparking.amrbadr12.smartparkingadmin;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    BluetoothAdapter bluetoothAdapter;
    public static final String TAG="Main Activity";
    private StateChangedBroadcastReciever btChangedStateListener;
    private ArrayList<DeviceItem> btDevicesList;
    private ListView pairedListView;
    private DevicesListAdapter adapter;
    private EditText writeEditText;
    private Button writeButton;
    private Button clearLogButton;
    private TextView readStreamText;
    private StringBuilder messages;
    private Button disconnectButton;
    private ArrayList<BluetoothDevice>btDevices;
    private Handler handler;
    private ReadMessagesBroadcastReciever readBroadcastReciever;
    private ArrayList<DeviceItem>deviceItems;
    private ImageView qrImage;
    private BluetoothConnectionService bluetoothConnectionService;
    private boolean isBtConnected;
    private BluetoothDeviceFoundBroadcastReciever bluetoothDeviceFoundBroadcastReciever;
    private Button readButton;
    private int connectedTag;
    private ImageView noPairedDevicesImage;
    private TextView noPairedDevicesLabel;
    private ValueEventListener adminValueEventListener;
    private TextView pairedHintLabel;
    private DatabaseReference adminReference;
    private TextView outputStreamLabel;
    private ScrollView outputStreamScorllView;
    private FirebaseDatabase mFirebaseDatabase;
    public void show()
    {
        qrImage.setVisibility(View.VISIBLE);
        pairedListView.setVisibility(View.INVISIBLE);
        pairedHintLabel.setVisibility(View.INVISIBLE);
        noPairedDevicesImage.setVisibility(View.INVISIBLE);
        noPairedDevicesLabel.setVisibility(View.INVISIBLE);
        writeEditText.setVisibility(View.INVISIBLE);
        writeButton.setVisibility(View.INVISIBLE);
        disconnectButton.setVisibility(View.INVISIBLE);
        clearLogButton.setVisibility(View.INVISIBLE);
        outputStreamScorllView.setVisibility(View.INVISIBLE);
        outputStreamLabel.setVisibility(View.INVISIBLE);
    }

    public void hide()
    {
        qrImage.setVisibility(View.INVISIBLE);
        pairedListView.setVisibility(View.VISIBLE);
        pairedHintLabel.setVisibility(View.VISIBLE);
        writeEditText.setVisibility(View.VISIBLE);
        writeButton.setVisibility(View.VISIBLE);
        disconnectButton.setVisibility(View.VISIBLE);
        clearLogButton.setVisibility(View.VISIBLE);
        outputStreamScorllView.setVisibility(View.VISIBLE);
        outputStreamLabel.setVisibility(View.VISIBLE);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        disconnectButton=(Button)findViewById(R.id.disconnected_button);
        isBtConnected=false;
        mFirebaseDatabase=FirebaseDatabase.getInstance();
        adminReference = mFirebaseDatabase.getReference("admin");
        outputStreamScorllView = findViewById(R.id.scrollview);
        outputStreamLabel = findViewById(R.id.output_stream_label);
        pairedHintLabel=(TextView)findViewById(R.id.paired_label);
        messages=new StringBuilder();
        deviceItems=new ArrayList<>();
        handler=new Handler(Looper.getMainLooper());
        noPairedDevicesImage = (ImageView)findViewById(R.id.no_paired_devices);
        noPairedDevicesLabel = (TextView)findViewById(R.id.no_paired_devices_label);
        clearLogButton=(Button)findViewById(R.id.clear_log_button);
        qrImage=(ImageView)findViewById(R.id.qr_image);
        writeEditText=(EditText)findViewById(R.id.write_to_stream);
        writeButton=(Button)findViewById(R.id.write_button);
        readStreamText=(TextView)findViewById(R.id.read_stream_textview);
        btDevicesList=new ArrayList<DeviceItem>();
        btDevices=new ArrayList<BluetoothDevice>();
        bluetoothDeviceFoundBroadcastReciever=new BluetoothDeviceFoundBroadcastReciever();
        pairedListView=(ListView)findViewById(R.id.paired_devices_list);
        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        btChangedStateListener =new StateChangedBroadcastReciever();
            attachAdapterToListView();
            disconnectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(isBtConnected){
                        Log.i("MainActivity","Disconnect called");
                        bluetoothConnectionService.disconnectDevice();
                       // btDevicesList.get(connectedTag);
                        isBtConnected=false;
                        adapter.notifyDataSetChanged();
                        Toast.makeText(MainActivity.this,"Disconnected successfully",Toast.LENGTH_LONG).show();
                        LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(readBroadcastReciever);
                    }
                    else{
                        Toast.makeText(MainActivity.this,"No running connection ",Toast.LENGTH_LONG).show();
                    }
                }
            });
            writeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(isBtConnected){
                        //messages.setLength(0);
                        String writtenText=writeEditText.getText().toString();
                        bluetoothConnectionService.writeToOutputStream(writtenText.getBytes());
                    }
                }
            });
            clearLogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    readStreamText.setText("");
                    messages.setLength(0);
                }
            });

    }

    @Override
    protected void onStop() {
        unregisterReceiver(btChangedStateListener);
        super.onStop();
    }

    private class ReadMessagesBroadcastReciever extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action= intent.getAction();
            if(action.equals("message")){
                String theMessage = intent.getStringExtra("theMessage");
                messages.append(theMessage);
                readStreamText.setText(messages.toString());
                //Log.i(TAG,"message recieved is:"+theMessage);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Main Activity",messages.toString());
        if(!bluetoothAdapter.isEnabled())
        {
            bluetoothAdapter.enable();
        }
        attachDatabaseReadListener();
            IntentFilter intentFilter=new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(btChangedStateListener,intentFilter);

    }

    private void attachAdapterToListView(){
        if(bluetoothAdapter!=null){
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if(pairedDevices.size()>0 && bluetoothAdapter.isEnabled()){
                for(BluetoothDevice bt: pairedDevices){
                    //for already paired devices
                    btDevices.add(bt);
                    btDevicesList.add(new DeviceItem(bt.getName(),bt.getAddress(),false));
                }
                adapter=new DevicesListAdapter(this,btDevicesList);
                pairedListView.setAdapter(adapter);
                pairedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, final View view, final int position, long l) {
                        final ProgressDialog progressDialog=new ProgressDialog(MainActivity.this);
                        progressDialog.setMessage("Connecting...");
                        progressDialog.show();
                        if (bluetoothAdapter.isEnabled()) {
                            BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(adapter.getItem(position).getBluetoothAddress());
                            if (remoteDevice != null && !isBtConnected) {
                                Log.i(TAG, remoteDevice.getName());
                                bluetoothConnectionService = new BluetoothConnectionService(MainActivity.this, remoteDevice, "00001101-0000-1000-8000-00805F9B34FB", bluetoothAdapter);
                                bluetoothConnectionService.connectToDevice();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (bluetoothConnectionService.isConnected()) {
                                            readBroadcastReciever = new ReadMessagesBroadcastReciever();
                                            isBtConnected = true;
                                            bluetoothConnectionService.readFromInputStream();
                                            Log.i("MainActivity", "connected");
                                            connectedTag=position;
                                            TextView connectedTextView = (TextView) view.findViewById(R.id.connected_status_textview);
                                            connectedTextView.setText("Connected");
                                            LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(readBroadcastReciever, new IntentFilter("message"));
                                        } else {
                                            Toast.makeText(MainActivity.this, "Couldn't connect", Toast.LENGTH_LONG).show();
                                            isBtConnected=false;
                                        }
                                        progressDialog.dismiss();
                                    }


                                }, 5000);
                            } else {
                                Toast.makeText(MainActivity.this, "Already connected", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Your bluetooth is not enabled", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                noPairedDevicesImage.setVisibility(View.INVISIBLE);
                noPairedDevicesLabel.setVisibility(View.INVISIBLE);
                pairedHintLabel.setVisibility(View.VISIBLE);
            }
            else {
                Toast.makeText(MainActivity.this, "No paired Devices found or bluetooth is disabled!", Toast.LENGTH_LONG).show();
                noPairedDevicesImage.setVisibility(View.VISIBLE);
                noPairedDevicesLabel.setVisibility(View.VISIBLE);
                pairedHintLabel.setVisibility(View.INVISIBLE);
            }
        }
    }

    private class BluetoothDeviceFoundBroadcastReciever extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice btDevice=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                DeviceItem device=new DeviceItem(btDevice.getName(),btDevice.getAddress(),false);
                adapter.add(device);
                btDevices.add(btDevice);
                adapter.notifyDataSetChanged();
            }
        }
    }


    private class StateChangedBroadcastReciever extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch(state){
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG,"Bluetooth is on");
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG,"Bluetooth is off");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG,"Bluetooth is turning off");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG,"Bluetooth is turning on");
                        break;
                }

            }
        }
    }
    public static Bitmap generateQRCode(String encodedString) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        Bitmap bitmap = null;
        try {
            BitMatrix qr = multiFormatWriter.encode(encodedString, BarcodeFormat.QR_CODE, 200, 200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.createBitmap(qr);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return bitmap;
    }



    private void attachDatabaseReadListener(){
        adminValueEventListener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean isConnecting =dataSnapshot.child("connecting").getValue(Boolean.class);
                String userame=dataSnapshot.child("userNameConnecting").getValue(String.class);
                if(isConnecting) {
                    Toast.makeText(MainActivity.this, userame + " is connecting", Toast.LENGTH_LONG).show();
                    show();
                    qrImage.setImageBitmap(generateQRCode("Hello motherfucker"));
                }
                else{
                    hide();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        adminReference.addValueEventListener(adminValueEventListener);
    }
    private void detachDatabaseReadListener(){
        if(adminValueEventListener!=null){
            adminReference.removeEventListener(adminValueEventListener);
        }
    }

}
