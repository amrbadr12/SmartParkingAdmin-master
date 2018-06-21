package com.smartparking.amrbadr12.smartparkingadmin;

/**
 a POJO class that represents a single bluetooth device
 */

public class DeviceItem {
    private String bluetoothName;
    private String bluetoothAddress;
    private boolean isConnected;

    public DeviceItem(String name,String address,boolean connected){
        bluetoothName=name;
        bluetoothAddress=address;
        isConnected=connected;
    }

    public String getBluetoothName() {
        return bluetoothName;
    }

    public String getBluetoothAddress() {
        return bluetoothAddress;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected){
        this.isConnected=connected;
    }
}
