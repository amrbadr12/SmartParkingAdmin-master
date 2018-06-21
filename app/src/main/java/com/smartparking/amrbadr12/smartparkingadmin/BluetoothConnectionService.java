package com.smartparking.amrbadr12.smartparkingadmin;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothConnectionService {
    public static final String TAG="BluetoothConnectionService";
    private final BluetoothDevice remoteDevice;
    private BluetoothAdapter mBluetoothAdapater;
    private BluetoothSocket mBluetoothSocket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private LocalBroadcastManager localBroadcastManager;
    private boolean isConnected;
    private Context mContext;

    public BluetoothConnectionService(Context context,BluetoothDevice bluetoothDevice, String UUID, BluetoothAdapter bluetoothAdapter){
        mContext=context;
        isConnected=false;
        remoteDevice=bluetoothDevice;
        java.util.UUID MY_UUID = java.util.UUID.fromString(UUID);
        mBluetoothAdapater=bluetoothAdapter;
        BluetoothSocket tmp=null;
        try {
            tmp=remoteDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.e("BluetoothConnection","Error creating and RFcomm socket");
        }
        mBluetoothSocket=tmp;
    }

    public void connectToDevice() {
        InputStream tmp_in = null;
        OutputStream tmp_out = null;
        if (!isConnected) {
            ConnectionThread connectionThread = new ConnectionThread();
            connectionThread.start();
        }
            try {
                tmp_in = mBluetoothSocket.getInputStream();
                tmp_out = mBluetoothSocket.getOutputStream();
                Log.i("BluetoothConnection", "Done getting the input and output streams");
            } catch (IOException e) {
                Log.e("BluetoothConnection", "Error getting input or output stream");
            }
            inputStream = tmp_in;
            outputStream = tmp_out;
    }

    public void disconnectDevice(){
        if(mBluetoothSocket!=null){
            try {
                mBluetoothSocket.close();
                Log.i("BluetoothConnection","Socket closed Successfully");
                isConnected=false;
            } catch (IOException e) {
                Log.e("BluetoothConnection","Error closing the bluetooth socket");
            }
        }
    }

    public boolean isConnected(){
        return isConnected;
    }
    private void setConnected(boolean connect){
        isConnected=connect;
    }
    public void writeToOutputStream(byte[] bytes){
        String writtenText=new String(bytes, Charset.defaultCharset());
        try {
            outputStream.write(bytes);
            Log.i("BluetoothConnection","write:writing to outputstream "+writtenText);
        } catch (IOException e) {
            Log.e("BluetoothConnection","Couldn't Write to OutputStream");
        }
    }

    public void readFromInputStream(){
        ReadingThread readingThread=new ReadingThread();
        readingThread.start();
    }

    private class ReadingThread extends Thread{
        private static final String TAG="ReadingThread";
       ReadingThread(){
        }
        @Override
        public void run() {
           Log.i(TAG,"Reading thread starting...");
            byte[]buffer= new byte[1024];
            int bytes;
            while(true){
                try{
                    bytes=inputStream.read(buffer);
                    Log.i(TAG,"bytes:"+bytes);
                    String message= new String(buffer,0,bytes);
                    Intent sendMessage=new Intent("message");
                    sendMessage.putExtra("theMessage", message);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(sendMessage);

                } catch (IOException e) {
                    Log.e(TAG,"Error: reading from inputstream");
                    break;
                }
            }

        }
    }


    private class ConnectionThread extends Thread{
        private static final String TAG="ConnectionThread";
        ConnectionThread(){
        }

        @Override
        public void run() {
                Log.i("Connect Thread", "run() called");
                mBluetoothAdapater.cancelDiscovery();
                try {
                    //blocking call until a connection is successful or an exception is thrown
                    mBluetoothSocket.connect();
                    setConnected(true);
                    Log.i(TAG, "connect() called");
                } catch (IOException e) {
                    //unable to connect
                    setConnected(false);
                    Log.e(TAG, "unable to connect");
                    try {
                        mBluetoothSocket.close();
                    } catch (IOException e1) {
                        Log.e(TAG, "Could not close the client socket", e1);
                    }
                    return;
                }
                Log.i(TAG, "Connected Successfully");

            }
            //TODO:update the adapter view to display connected

    }

}
