package pulsemeter.app.sanoob.com.pulsemeter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import pulsemeter.app.sanoob.com.pulsemeter.bluetooth.BluetoothOperation;


public class PulseMeter extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnChartValueSelectedListener {

    private LineChart mChart;
    volatile boolean stopWorker;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    InputStream btInputStream;
    Boolean isBluetoothConnected = false;
    private BluetoothOperation bluetoothOperation;
    BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter bluetoothArrayAdapter;
    private BluetoothDevice[] bluetoothDevices;
    private BluetoothDevice btDevice;
    ProgressDialog progressDialog;
    private static  String KEY_X = "x";
    private static  String KEY_Y = "y";
    private static  String KEY_Z= "z";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pulse_meter);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.INVISIBLE);
        /*fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
               // addEntry();
            }
        });*/

        bluetoothOperation = new BluetoothOperation();
        bluetoothArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        mChart = (LineChart) findViewById(R.id.chart1);
        mChart.setOnChartValueSelectedListener(this);

        // no description text
        mChart.setDescription("");
        mChart.setNoDataTextDescription("You need to provide data for the chart.");

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        mChart.setBackgroundColor(Color.WHITE);

        LineData data = new LineData();
        data.setValueTextColor(Color.BLUE);

        LineData data1 = new LineData();
        data1.setValueTextColor(Color.YELLOW);

        // add empty data
        mChart.setData(data);


        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(Legend.LegendForm.LINE);
        //l.setTypeface(mTfLight);
        l.setTextColor(Color.DKGRAY);

        XAxis xl = mChart.getXAxis();
        //xl.setTypeface(mTfLight);
        xl.setTextColor(Color.DKGRAY);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
        //leftAxis.setTypeface(mTfLight);
        leftAxis.setTextColor(Color.DKGRAY);
        leftAxis.setAxisMaxValue(500f);
        leftAxis.setAxisMinValue(200f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);
        //feedMultiple();

    }

    private void addEntry(float val) {

        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);

            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            //data.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * 40) + 30f), 0);
            data.addEntry(new Entry(set.getEntryCount(), val), 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(120);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart.moveViewToX(data.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "X");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        //set.setColor(Color.GRAY);
        set.setCircleColor(Color.GREEN);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.DKGRAY);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    private Thread thread;

    private void feedMultiple() {

        if (thread != null)
            thread.interrupt();

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                addEntry(0f);
            }
        };

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < 1000; i++) {

                    // Don't generate garbage runnables inside the loop.
                    runOnUiThread(runnable);

                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
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


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_bluetooth) {
          //initiate bluetooth
            if(!isBluetoothConnected){

                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if(bluetoothAdapter != null){
                    if (!bluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, 0);
                    }
                    bluetoothArrayAdapter.clear();
                    bluetoothDevices = bluetoothOperation.getListOfDevices(bluetoothAdapter);
                    if((bluetoothDevices != null) && (bluetoothDevices.length > 0)) {
                        for (BluetoothDevice device : bluetoothDevices) {

                            bluetoothArrayAdapter.add(device.getName() + "\n" + device.getAddress());

                        }

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
                        //no paired devices
                    }


                }else{
                    //no bluetooth
                }

            }else{
                new AlertDialog.Builder(PulseMeter.this)
                        .setMessage("Are you sure you want to disconnect")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                        // continue with delete
                                try {
                                    closeBluetooth();
                                }catch (Exception e){e.printStackTrace();}
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();


            }

        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        //final Entry f = e;
       //Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected() {

    }
    @Override
    protected void onPause() {
        super.onPause();

        if (thread != null) {
            thread.interrupt();
        }
    }

    public void btDataListener(){

        final Handler handler = new Handler();
        final byte delimiter = 10;
        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable() {
            public void run() {
                while(!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        if(isBluetoothConnected) {
                            int bytesAvailable = btInputStream.available();
                            if (bytesAvailable > 0) {
                                byte[] packetBytes = new byte[bytesAvailable];
                                btInputStream.read(packetBytes);
                                for (int i = 0; i < bytesAvailable; i++) {
                                    byte b = packetBytes[i];
                                    if (b == delimiter) {
                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                        final String data = new String(encodedBytes, "US-ASCII");
                                        readBufferPosition = 0;

                                        handler.post(new Runnable() {
                                            public void run() {
                                                //myLabel.setText(data);
                                                //Log.d("DATA_BT", ""+data);
                                                //Toast.makeText(PulseMeter.this,data, Toast.LENGTH_SHORT).show();
                                                parseData(data);
                                            }
                                        });
                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }
                        }
                    } catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }
    public void connectToDevice(int position){
        btDevice = bluetoothDevices[position];

       /* btDataListener();

        //bluetoothDevice = bluetoothDevices[position];
        progressDialog = ProgressDialog.show(PulseMeter.this, "", "Connecting please wait..", true, false);
        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                if(bluetoothOperation.connectToDevice(btDevice)){
                    isBluetoothConnected = true;
                     stopWorker = false;

                }else{
                    isBluetoothConnected = false;
                    stopWorker = true;
                }
                runOnUiThread(new  Runnable() {
                    public void run() {
                        progressDialog.dismiss();
                    }
                });
            }
        }).start();*/

        /*btDataListener();*/
        if(bluetoothOperation.connectToDevice(btDevice)){
            btInputStream = bluetoothOperation.getBtInputStream();
            if(btInputStream != null){
                isBluetoothConnected = true;
                stopWorker = false;
                btDataListener();
            }else{
                //no input stream
                isBluetoothConnected = false;
                stopWorker = true;
            }
        }else{
            //connection failed
            isBluetoothConnected = false;
            stopWorker = true;
        }
    }

    public void closeBluetooth() throws  Exception{

            stopWorker = true;
            btInputStream.close();
            bluetoothOperation.closeBT();
            isBluetoothConnected = false;
    }

    public void parseData(String data){
        try {
            JSONObject dataJson = new JSONObject(data);
            //addEntry((float)dataJson.get(KEY_Y));
            //addEntry(15f);
            Iterator<String> keys = dataJson.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if(key.equalsIgnoreCase("x")){
                   // Toast.makeText(getApplicationContext(), "x: "+dataJson.get(key) , Toast.LENGTH_SHORT).show();addEntry(15f);
                    addEntry((float) dataJson.getInt(key));
                }

            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
