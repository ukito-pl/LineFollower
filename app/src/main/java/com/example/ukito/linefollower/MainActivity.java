package com.example.ukito.linefollower;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.List;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    public SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    public static final int REQUEST_ENABLE_BT = 1;

    private final static String TAG = MainActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private String robotAddress = "C8:FD:19:4A:BB:D1";

    private ViewPager mViewPager;
    public DataManager dataManager;
    public boolean start = false;
    private BluetoothAdapter mBluetoothAdapter;
    private BLEScanner  mBLEScanner;
    public Vector<BLEDevice> mBLEDevices;
    private long scanPeriod = 5000;
    private Handler mHandler;
    private MainActivity ma = this;
    private Runnable connectRun;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mGattCharacteristic;
    private BluetoothGattCharacteristic mNotifyCharacteristic;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        dataManager = new DataManager();
        mHandler = new Handler();

        mBLEScanner = new BLEScanner(this,scanPeriod,-150);
        mBLEDevices = new Vector<>();

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);



        Utils.requestUserBluetooth(this);



    }



    public boolean[] checkDataButtons(int[] dataColor){
        CheckBox dataBox[] = new CheckBox[3];
        dataBox[0] = (CheckBox) findViewById(R.id.data1);
        dataBox[1] = (CheckBox) findViewById(R.id.data2);
        dataBox[2] = (CheckBox) findViewById(R.id.data3);
        boolean data[] = new boolean[3];

        for (int i = 0; i<3 ;i++) {
               dataColor[i] = dataBox[i].getCurrentTextColor();
               if(dataBox[i].isChecked()){
                   data[i] = true;
               }
        }
        return data;
    }

    public boolean[] checkDataButtons(){
        CheckBox dataBox[] = new CheckBox[3];
        dataBox[0] = (CheckBox) findViewById(R.id.data1);
        dataBox[1] = (CheckBox) findViewById(R.id.data2);
        dataBox[2] = (CheckBox) findViewById(R.id.data3);
        boolean data[] = new boolean[3];

        for (int i = 0; i<3 ;i++) {
            if(dataBox[i].isChecked()){
                data[i] = true;
            }
        }
        return data;
    }



    public void drawButton(View view){
        GraphView graph = (GraphView) findViewById(R.id.graph);
        graph.removeAllSeries();
        int dataColor[] = new int[3];
        boolean[] dataShow = checkDataButtons(dataColor);
        for (int i =0; i<dataShow.length;i++){
            if(dataShow[i] == true) {
                LineGraphSeries<DataPoint> series = dataManager.getLineDataSeries(i);
                series.setColor(dataColor[i]);
                graph.addSeries(series);
            }
        }/*
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(1.6);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(4);

        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(true);
*/
    }

    public void startButton(View view){
        Button startButton = (Button) findViewById(R.id.startButton);
        if (start==false){
            drawButton(view);
            start = true;
            startButton.setText("Stop");
            Utils.consoleNotify(this, "Startowanie");
            if(mBluetoothLeService != null) {
                mBluetoothLeService.writeCharacteristic(mGattCharacteristic, "#S1*");
            }
        }else{
            start = false;
            startButton.setText("Start");
            Utils.consoleNotify(this, "Zatrzymywanie");
            if(mBluetoothLeService != null) {
                mBluetoothLeService.writeCharacteristic(mGattCharacteristic, "#S0*");
            }
        }



    }

    public void connect(View view){
        CheckBox connectBox = (CheckBox) findViewById(R.id.connectBox);
        if(connectBox.isChecked()){
            int i = find(robotAddress);
            if (i == -1) {                  //if robot's BLEDevice is not found
                if (!mBLEScanner.isScanning()) {
                    startScan();
                } else {
                    stopScan();
                }

            }else{

            }

        }else{

            stopScan();

        }

    }

    public void sendButton(View view){

        EditText PFactor = (EditText) this.findViewById(R.id.PFactor);
        EditText DFactor = (EditText) this.findViewById(R.id.DFactor);
        EditText V = (EditText) this.findViewById(R.id.V);

        String pFactor = PFactor.getText().toString();
        final String dFactor = DFactor.getText().toString();
        final String v = V.getText().toString();
        Utils.consoleNotify(this, "Wysyłanie parametru P");
        if(!pFactor.isEmpty()) {
            if (mBluetoothLeService != null) {
                mBluetoothLeService.writeCharacteristic(mGattCharacteristic, "#P" + pFactor + "*");
            }
        }else{
            Utils.consoleNotify(this, "Nie można wysłać - brak wpisanego parametru P");
        }

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Utils.consoleNotify(ma, "Wysyłanie parametru D");
                if (!dFactor.isEmpty()) {
                    if (mBluetoothLeService != null) {
                        mBluetoothLeService.writeCharacteristic(mGattCharacteristic, "#D" + dFactor + "*");
                    }
                } else {
                    Utils.consoleNotify(ma, "Nie można wysłać - brak wpisanego parametru D");
                }
            }
        }, 200);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Utils.consoleNotify(ma, "Wysyłanie parametru V");
                if (!v.isEmpty()) {
                    if (mBluetoothLeService != null) {
                        mBluetoothLeService.writeCharacteristic(mGattCharacteristic, "#V" + v + "*");
                    }
                } else {
                    Utils.consoleNotify(ma, "Nie można wysłać - brak wpisanego parametru V");
                }
            }
        }, 400);


    }

    public void saveButton(View view){
        boolean check[] = checkDataButtons();
        for (int i = 0; i <check.length; i++) {
            if(check[i]) {
                dataManager.saveData(this,i);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //   return true;
        //}

        return super.onOptionsItemSelected(item);
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                //updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                //updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                //clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                getMyCharacteristic();
                readMyCharacteristic();
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));

            }
        }
    };

    private void displayData(String data) {
        if (data != null) {
            Utils.consoleNotify(this,data);
        }
    }

    public void getMyCharacteristic(){
        List<BluetoothGattService> gattServices = mBluetoothLeService.getSupportedGattServices();
        if( !gattServices.isEmpty()) {
            List<BluetoothGattCharacteristic> gattCharacteristics = gattServices.get(0).getCharacteristics();
            if(!gattCharacteristics.isEmpty()){
                mGattCharacteristic = gattCharacteristics.get(0);

            }
        }
    }

    public void readMyCharacteristic(){
        final int charaProp = mGattCharacteristic.getProperties();
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            // If there is an active notification on a characteristic, clear
            // it first so it doesn't update the data field on the user interface.
            if (mNotifyCharacteristic != null) {
                mBluetoothLeService.setCharacteristicNotification(
                        mNotifyCharacteristic, false);
                mNotifyCharacteristic = null;
            }
            mBluetoothLeService.readCharacteristic(mGattCharacteristic);
        }
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            mNotifyCharacteristic = mGattCharacteristic;
            mBluetoothLeService.setCharacteristicNotification(
                    mGattCharacteristic, true);
        }
    }

    public void stopScan() {
        Utils.consoleNotify(this,"Poszukiwanie zakończone");
        Utils.consoleNotify(this, "Znalezione urządzenia:");
        if(!mBLEDevices.isEmpty()) {
            int n = mBLEDevices.size();
            for (int i = 0; i < n; i++){
                Utils.consoleNotify(this, mBLEDevices.get(i).getName());
                Utils.consoleNotify(this, mBLEDevices.get(i).getAddress());
                Utils.consoleNotify(this, Integer.toString(mBLEDevices.get(i).getRSSI()));
            }
        }else{
            Utils.consoleNotify(this, "Brak");
        }
        final byte[] data;
        mBluetoothLeService.connect(robotAddress);


        mBLEScanner.stop();


    }

    public void startScan(){
        Utils.consoleNotify(this, "Poszukiwanie urządzeń");

        mBLEDevices.clear();

        mBLEScanner.start();

    }

    public int find(String address){
        if(!mBLEDevices.isEmpty()) {
            int n = mBLEDevices.size();
            for (int i = 0; i < n; i++){
                if (mBLEDevices.get(i).getAddress().equals(address)){
                    return i;
                }
            }
        }
        return -1;
    }

    public void addDevice(BluetoothDevice bluetoothDevice, int new_rssi) {
        String address = bluetoothDevice.getAddress();
        int i = find(address);
        if(i == -1){
            Utils.consoleNotify(this, "Znaleziono urządzenie");
            BLEDevice ble_device = new BLEDevice(bluetoothDevice);
            ble_device.setRSSI(new_rssi);

            mBLEDevices.add(ble_device);


        }else{
            mBLEDevices.get(i).setRSSI(new_rssi);
        }

    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize(MainActivity.this)) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }


        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            if (getArguments().getInt(ARG_SECTION_NUMBER) == 1){
                View rootView = inflater.inflate(R.layout.fragment_main1, container, false);
                return rootView;
            }else if(getArguments().getInt(ARG_SECTION_NUMBER) == 2){
                View rootView = inflater.inflate(R.layout.fragment_main2, container, false);

                return rootView;
            }

            return null;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Komunikacja";
                case 1:
                    return "Zebrane dane";

            }
            return null;
        }
    }


}
