package com.smartparking.amrbadr12.smartparkingadmin;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class DevicesListAdapter extends ArrayAdapter<DeviceItem> {
    private Context mContext;
    private ArrayList<DeviceItem>deviceItemArrayList;

    public DevicesListAdapter(@NonNull Context context, ArrayList<DeviceItem> devices) {
        super(context,0,devices);
        deviceItemArrayList=devices;
        mContext=context;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View inflatedView=convertView;
        if(inflatedView==null){
            inflatedView=LayoutInflater.from(mContext).inflate(R.layout.bt_list,parent,false);
        }
        TextView bluetoothName = (TextView) inflatedView.findViewById(R.id.bt_name_textview);
        inflatedView.setTag(position);
        TextView bluetoothAddress = (TextView) inflatedView.findViewById(R.id.address_textview);
        TextView connectedStatus = (TextView) inflatedView.findViewById(R.id.connected_status_textview);
        DeviceItem bluetoothDevice = getItem(position);
        if(bluetoothDevice!=null) {
            bluetoothName.setText(bluetoothDevice.getBluetoothName());
            bluetoothAddress.setText(bluetoothDevice.getBluetoothAddress());
            if(bluetoothDevice.isConnected()){
                connectedStatus.setText("Connected");
            }
            else{
                connectedStatus.setText("Unconnected");
            }
        }
        return inflatedView;
    }
}
