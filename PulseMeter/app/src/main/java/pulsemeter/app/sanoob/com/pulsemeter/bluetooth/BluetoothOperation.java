package pulsemeter.app.sanoob.com.pulsemeter.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.widget.ArrayAdapter;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by sanoob on 28/7/16.
 */
public class BluetoothOperation {


    //BluetoothDevice bluetoothDevice;
    BluetoothDevice bluetoothDevices[];
    private BluetoothSocket btSocket = null;
    OutputStream btOutputStream;
    InputStream btInputStream;
    //BluetoothAdapter bluetoothAdapter;

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    public BluetoothDevice[] getListOfDevices(BluetoothAdapter btAdapter){

        if(btAdapter.getState() == BluetoothAdapter.STATE_ON) {
            Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
            bluetoothDevices = new BluetoothDevice[pairedDevices.size()];
            //bluetoothDevices = (BluetoothDevice[]) pairedDevices.toArray();
            // If there are paired devices
            if (pairedDevices.size() > 0) {
                // Loop through paired devices
                int i = 0;
                for (BluetoothDevice device : pairedDevices) {
                    // Add the name and address to an array adapter to show in a ListView
                    // mArrayAdapter.add(device.getName() + "\n" + device.getAddress());

                    bluetoothDevices[i++] = device;

                }
            }
            return bluetoothDevices;
        }else{
            return null;
        }

    }

    public boolean connectToDevice(BluetoothDevice btDevice){
        try{
            btSocket = btDevice.createRfcommSocketToServiceRecord(BTMODULEUUID);
            btSocket.connect();
            btInputStream = btSocket.getInputStream();
            btOutputStream = btSocket.getOutputStream();
            return true;
        }catch (Exception e){ return false;   }

    }


    public boolean sendData(String data, OutputStream outputStream){
        try {
            data += "\n";
            btOutputStream.write(data.getBytes());
            return true;
        }catch(Exception e){
            return false;
        }
    }

    public OutputStream getBtOutputStream(){
        return btOutputStream;
    }

    public InputStream getBtInputStream(){
        return btInputStream;
    }

    public void closeBT() throws Exception {
        //stopWorker = true;
        btOutputStream.close();
        btInputStream.close();
        btSocket.close();

    }

}
