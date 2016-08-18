package led.bluetooth.app.sanoob.com.bluetoothled;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.flask.colorpicker.OnColorSelectedListener;

import com.flask.colorpicker.ColorPickerView;
import com.jraska.falcon.Falcon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import in.excogitation.lib.sensey.FlipDetector;
import in.excogitation.lib.sensey.Sensey;
import in.excogitation.lib.sensey.ShakeDetector;
import led.bluetooth.app.sanoob.com.bluetoothled.bluetooth.BluetoothOperation;
import led.bluetooth.app.sanoob.com.bluetoothled.util.Constants;
import led.bluetooth.app.sanoob.com.bluetoothled.util.ImageProcessing;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ColorPickerView colorView;
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice bluetoothDevices[];
    BluetoothDevice bluetoothDevice;
    OutputStream btOutputStream;
    InputStream btInputStream;
    int shakeCounter = 0;
    ProgressDialog progressDialog;
    String lightOffData = "0,0,0";
    //Col
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket btSocket = null;
    ArrayAdapter bluetoothArrayAdapter;
    Boolean isBluetoothConnected = false;
    String errMessage;
    final int REQUEST_ENABLE_BT = 0;
    public static String msg;
    BluetoothOperation BTop = new BluetoothOperation();
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
      /* do what you need to do */
          // checkScreen();
      /* and here comes the "trick" */
          //  handler.postDelayed(this, 5000);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


       // handler.postDelayed(runnable, 5000);

        Sensey.getInstance().init(MainActivity.this);
        Sensey.getInstance().startShakeDetection(new ShakeDetector.ShakeListener() {
            @Override
            public void onShakeDetected() {
               //Toast.makeText(MainActivity.this,"Shake detected", Toast.LENGTH_SHORT).show();


                try{
                    switch(shakeCounter){
                        case 0:  sendData(Constants.COLOR_RED);
                                 shakeCounter++;
                                 break;
                        case 1:  sendData(Constants.COLOR_GREEN);
                                 shakeCounter++;
                                break;
                        case 2:  sendData(Constants.COLOR_BLUE);
                                 shakeCounter++;
                                break;
                        case 3:  sendData(Constants.COLOR_WHITE);
                                    shakeCounter++;
                                break;
                        case 4:  shakeCounter = 0;
                                break;
                        default :
                                  break;

                    }

                }catch (Exception e){}

            }
        });

        bluetoothArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);

        colorView = (ColorPickerView) findViewById(R.id.color_picker_view);

        colorView.addOnColorSelectedListener(new OnColorSelectedListener() {
           @Override
           public void onColorSelected(int i) {


               int color = colorView.getSelectedColor();
               int red=   (color >> 16) & 0xFF;
               int green= (color >> 8) & 0xFF;
               int blue=  (color >> 0) & 0xFF;
              // Toast.makeText(MainActivity.this, "color: "+red+" "+green+" "+blue,Toast.LENGTH_LONG).show();
               final String data = red+","+green+","+blue;
               //showToast("data: "+data);
               new Thread(new Runnable() {
                   @Override
                   public void run() {
                       try {
                           if (isBluetoothConnected){
                               sendData(data);
                            }else{
                               showToast("No device connected");
                           }
                       } catch (IOException e) {
                           e.printStackTrace();
                       }
                   }
               }).start();

           }
       });



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            if(!isBluetoothConnected) {
                initiateBluetooth();
            }else{

                if(btSocket.isConnected()){
                    try {
                        btSocket.close();
                        isBluetoothConnected = false;
                        showToast("Device disconnected");
                       /* Snackbar.make(view, "Device disconnected", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();*/
                    }catch (Exception e){/*Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();*/
                        showToast(e.getMessage());
                    }
                }
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action


                if (!isBluetoothConnected) {
                    initiateBluetooth();
                } else {

                    if (btSocket.isConnected()) {
                        try {
                            btSocket.close();
                            isBluetoothConnected = false;
                            showToast("Device disconnected");
                       /* Snackbar.make(view, "Device disconnected", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();*/
                        } catch (Exception e) {/*Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();*/
                            showToast(e.getMessage());
                        }
                    }

                }

        } else if (id == R.id.nav_gallery) {
            if(isBluetoothConnected){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                                sendData(lightOffData);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }else{
                showToast("No device connected");
            }



        } else if (id == R.id.nav_manage) {



        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void initiateBluetooth(){

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter != null){

            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
               /* Snackbar.make(view, "Bluetooth Activated", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                //showToast("Bluetooth Activated");
            }
            if(bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
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
                        bluetoothArrayAdapter.add(device.getName());
                    }
                }

                //bluetoothListView.setAdapter(bluetoothArrayAdapter);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Select Device");
                builder.setAdapter(bluetoothArrayAdapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        connectToDevice(item);
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }else{
                    //showToast( "Bluetooth is not activated..");
                /*Snackbar.make(view, "Bluetooth is not activated..", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }

        }else{
            //no bluetooth
            showToast("Bluetooth device not found ");
//            Snackbar.make(view, "Bluetooth device not found ", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show();
        }
    }

    public void connectToDevice(int position){

        try {


            bluetoothDevice = bluetoothDevices[position];
            progressDialog = ProgressDialog.show(MainActivity.this, "", "Connecting please wait..", true, false);
            progressDialog.show();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        btSocket = bluetoothDevice.createRfcommSocketToServiceRecord(BTMODULEUUID);
                        btSocket.connect();
                        btInputStream = btSocket.getInputStream();
                        btOutputStream = btSocket.getOutputStream();
                        isBluetoothConnected = true;
                        BTop.setBTOutputStream(btOutputStream);
                    }catch (Exception e){  isBluetoothConnected = false;  errMessage = e.getMessage(); }

                    runOnUiThread(new  Runnable() {
                        public void run() {
                            progressDialog.dismiss();

                            if(isBluetoothConnected){showToast("Connected to "+bluetoothDevice.getName()+" \n address: "+bluetoothDevice.getAddress());}
                                /*Snackbar.make(view, "Connected to "+bluetoothDevice.getName()+" \n address: "+bluetoothDevice.getAddress(),
                                        Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();*/
                            else{showToast("Connection failed "+errMessage);}
                                /*Snackbar.make(view, "Connection failed "+errMessage,
                                        Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();*/
                        }
                    });
                }
            }).start();

        }catch(Exception e){
           /* Snackbar.make(view, "Connection failed "+e.getMessage(),
                    Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();*/
            showToast("Connection failed "+e.getMessage());
        }


    }

    public void sendData(String data) throws IOException {

        data += "\n";
        btOutputStream.write(data.getBytes());
    }
    public void showToast(String message){
        msg = message;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Your code to run in GUI thread here
                Toast.makeText(MainActivity.this, ""+msg,Toast.LENGTH_LONG).show();
            }//public void run() {
        });

    }
    public void checkScreen(){
        new Thread(new Runnable() {
            @Override
            public void run() {



            }
        }).start();
    }
}
