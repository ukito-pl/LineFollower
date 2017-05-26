package com.example.ukito.linefollower;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

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

    private ViewPager mViewPager;
    public DataCollector dataCollector;
    public boolean start = false;
    private BluetoothAdapter mBluetoothAdapter;
    private BLEScanner  mBLEScanner;
    public Vector<BLEDevice> mBLEDevices;

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
        dataCollector = new DataCollector();

        mBLEScanner = new BLEScanner(this,7500,-150);
        mBLEDevices = new Vector<>();
        Utils.requestUserBluetooth(this);

    }

    public void consoleNotify(String text){
        TextView consoleText = (TextView) findViewById(R.id.console);
        consoleText.setText(consoleText.getText()+text+"\n");
        scrollDown();
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

    public void drawButton(View view){
        GraphView graph = (GraphView) findViewById(R.id.graph);
        graph.removeAllSeries();
        int dataColor[] = new int[3];
        boolean[] dataShow = checkDataButtons(dataColor);
        for (int i =0; i<dataShow.length;i++){
            if(dataShow[i] == true) {
                LineGraphSeries<DataPoint> series = dataCollector.getLineDataSeries(i);
                series.setColor(dataColor[i]);
                graph.addSeries(series);
            }
        }
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(1.6);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(4);

        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(true);

    }

    public void startButton(View view){
        Button startButton = (Button) findViewById(R.id.startButton);
        if (start==false){
            drawButton(view);
            start = true;
            startButton.setText("Stop");
            consoleNotify("Wystartowano");
        }else{
            start = false;
            startButton.setText("Start");
            consoleNotify("Zatrzymano");
        }


    }

    public void connect(View view){
        CheckBox connectBox = (CheckBox) findViewById(R.id.connectBox);
        if(connectBox.isChecked()){
            if(!mBLEScanner.isScanning()){
                startScan();
            }else {
                stopScan();
            }


        }else{
            stopScan();
        }

    }

    public void sendButton(View view){
        consoleNotify("Parametry wysłano");
    }

    public void scrollDown(){
        ScrollView scroll = (ScrollView) findViewById(R.id.scroll);
        scroll.fullScroll(View.FOCUS_DOWN);
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

    public void stopScan() {
        consoleNotify("Poszukiwanie zakończone");
        consoleNotify("Znalezione urządzenia:");
        if(!mBLEDevices.isEmpty()) {
            int n = mBLEDevices.size();
            for (int i = 0; i < n; i++){
                consoleNotify(mBLEDevices.get(i).getName());
                consoleNotify(mBLEDevices.get(i).getAddress());
                consoleNotify(Integer.toString(mBLEDevices.get(i).getRSSI()));
            }
        }else{
            consoleNotify("Brak");
        }
        consoleNotify("Nie udało się połaczyć");
        CheckBox connectBox = (CheckBox) findViewById(R.id.connectBox);
        connectBox.setChecked(false);
        mBLEScanner.stop();
    }

    public void startScan(){
        consoleNotify("Poszukiwanie urządzeń");

        mBLEDevices.clear();

        mBLEScanner.start();

    }

    public int find(String address){
        if(!mBLEDevices.isEmpty()) {
            int n = mBLEDevices.size();
            for (int i = 0; i < n; i++){
                if (mBLEDevices.get(i).getAddress() == address){
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
            consoleNotify("Znaleziono urządzenie");
            BLEDevice ble_device = new BLEDevice(bluetoothDevice);
            ble_device.setRSSI(new_rssi);

            mBLEDevices.add(ble_device);


        }else{
            mBLEDevices.get(i).setRSSI(new_rssi);
        }

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
