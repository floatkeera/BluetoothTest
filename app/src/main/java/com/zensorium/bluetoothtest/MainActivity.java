package com.zensorium.bluetoothtest;

import android.app.AlertDialog;
import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.bluetooth.*;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;


public class MainActivity extends ActionBarActivity {

    public static final UUID WEARABLE_SERVICE = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public final static String TAG = "BluetoothLeService";
    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    public static final long SCAN_PERIOD = 10000;
    public static final long WAIT_PERIOD = 3000;
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    final static int ready = 0;
    final static int getParamInfo = 1;
    final static int getCounter = 2;
    final static int getPacket = 3;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    public static BluetoothGatt mBluetoothGatt;
    public static int decision = 1;
    public int count1;
    public int count2;
    public String userIDData;
    public boolean mScanning;
    public Handler mHandler = new Handler();
    boolean show = false;
    boolean ab;
    private boolean isGetAllDataPacketSize = false;
    private int commandPacketInfoListIndex = 0;
    boolean check = false;

    SwipeRefreshLayout mSwipeRefreshLayout;

    ProgressDialog dialog1;

    boolean checkload = false;

    BluetoothGattCharacteristic currentGattCharacteristic;
    BluetoothGattService currentGattService;
    BluetoothGattCharacteristic currentTxGattCharacteristic;
    BluetoothGattCharacteristic currentRxGattCharacteristic;
    ArrayList<String> myStringArray1 = new ArrayList<String>();
    ArrayList<String> myStringArray2 = new ArrayList();
    ArrayList<String> resultArray = new ArrayList();
    ArrayList<Object> objectArray = new ArrayList<Object>();
    ArrayList<String> parsedResult = new ArrayList<String>();
    ArrayList<String> passing = new ArrayList<>();
    ArrayList<String> getUserID = new ArrayList<>();
    HashMap<String, Object> map = new HashMap<String, Object>();
    int CURRENT_SYNC = 0;
    int _state = ready;
    String sendWearableCommand;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
    private ListView lv;
    private ArrayAdapter<String> deviceArrayAdapter;


    private Button btn2;
    private Button btn3;
    private Spinner spn;
    private boolean isSyncCompleted = false;
    private boolean isSyncStatusBroadcast = false;
    private TextView txt;
    public final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (ACTION_DATA_AVAILABLE.equals(action)) {

                Log.i(TAG, "ACTION_DATA_AVAILABLE");
                String b = bytesToHex(intent.getExtras().getByteArray(EXTRA_DATA));

                if (decision == 2) {

                    if (check == false) {
                        Log.i(TAG, b);

                        int tmpPacketSize = getNumberOfDataPacket(b);
                        checkpacket(tmpPacketSize);


                    } else if (check == true) {
                        String[] s = b.split("0D0A");


                        for (String sTmp : s) {
                            if (!sTmp.startsWith("FFF4")) {
                                if (!sTmp.startsWith("0")) {
                                    if (sTmp.startsWith("FF") || sTmp.startsWith("F8") || sTmp.startsWith("F7")) {
                                        Log.d(TAG, " Add To List:" + sTmp);
                                        resultArray.add(sTmp);

                                    }
                                }
                            } else {
                                Log.d(TAG, "Not Add To List:" + sTmp);
                            }
                        }
                    }

                    if (resultArray.size() == 11 && commandListDataPacketCounter[0] > 11) {
                        sendAck();
                    }

                } else if (decision == 1) {


                    resultArray.add(b);
                    Log.i(TAG, b);

                } else if (decision == 0) {
                    getUserID.add(b);


                }

            } else if (ACTION_GATT_CONNECTED.equals
                    (action)) {
                btn2.setEnabled(true);
                btn3.setEnabled(true);
                txt.setText(R.string.statusconnected);


            } else if (ACTION_GATT_DISCONNECTED.equals(action)) {
                btn2.setEnabled(false);
                btn3.setEnabled(false);
                txt.setText(R.string.statusdisconnected);
            }
        }
    };


    private String deviceAddress;
    private BluetoothManager mBluetoothManager;
    private String mBluetoothDeviceAddress;
    private int mConnectionState = STATE_DISCONNECTED;
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;

                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());
                Log.i(TAG, "Connected to " + deviceAddress);


            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);

            }
        }


        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                Log.i(TAG, "Discovered the following services:");


                for (BluetoothGattService bluetoothGattService : mBluetoothGatt.getServices()) {
                    String a = bluetoothGattService.getUuid().toString();
                    Log.i(TAG, a);
                    for (BluetoothGattCharacteristic bluetoothGattCharacteristic : bluetoothGattService.getCharacteristics()) {
                        String b = bluetoothGattCharacteristic.getUuid().toString();
                        Log.i(TAG, b);
                    }
                }

                selectWearableGattServices(mBluetoothGatt.getServices());

                displayGattServices(mBluetoothGatt.getServices());


            } else {
                Log.w(TAG, "ERROR: onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }


    };


    // If there are paired devices
    private String wearableCommand;
    private long transferedByteData = 0L;
    private String tmpRawRxString = "";
    private int CURRENT_MODE = 0;
    private int CURRENT_PACKET_COUNTER = 0;
    private int CURRENT_PACKET_SIZE = 0;
    private String CURRENT_PACKET_COMMAND = "00";
    private int[] commandListDataPacketCounter = new int[GlobalVariables.SYNC_COMMAND_LIST.length];
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {


                    String deviceAddress = device.getAddress();
                    String deviceName = device.getName();

                    if (deviceName != null && deviceName.equalsIgnoreCase("being") == true) {


                        if (!myStringArray2.contains(deviceAddress)) {
                            myStringArray2.add(deviceAddress);
                            myStringArray1.add(deviceName + "\n" + deviceAddress);
                        }
                        setAbc();
                    }


                }
            });
        }


    };

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_GATT_CONNECTED);
        intentFilter.addAction(ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    public void onBackPressed() {
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        mHandler.removeCallbacks(run);
        mHandler.removeCallbacksAndMessages(null);
        finish();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.


        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;


    }

    public void onSaveInstanceState(Bundle savedInstanceState) {


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setIcon(R.drawable.logo);


        lv = (ListView) findViewById(R.id.listView);

        btn2 = (Button) findViewById(R.id.dcbutton);
        btn3 = (Button) findViewById(R.id.btnData);
        txt = (TextView) findViewById(R.id.txt);
        txt.setText(R.string.status);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_scan_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeColors(Color.rgb(255, 193, 2), Color.rgb(73, 184, 248), Color.rgb(77, 166, 92), Color.rgb(159, 82, 181));

        spn = (Spinner) findViewById(R.id.spinner);
        spn.setSelection(1);

        checkBT();
        spn.setPrompt("Select data");

        spn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


                if (position == 0) {
                    decision = 1;
                } else if (position == 1) {
                    decision = 2;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

/*        btn1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkBT();
                myStringArray1.clear();
                myStringArray2.clear();
                scanLeDevice(ab);

            }
        });*/

        btn2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                dcbt();

            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
                                                 {
                                                     @Override
                                                     public void onRefresh() {

                                                         checkBT();
                                                         myStringArray1.clear();
                                                         myStringArray2.clear();
                                                         scanLeDevice(ab);


                                                     }
                                                 }
        );


        btn3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog1 = new ProgressDialog(MainActivity.this);

                dialog1.setMessage(getString(R.string.gettingdata));
                dialog1.show();
                Log.i(TAG, String.valueOf(decision));

                if (decision == 1) {

                    getDeviceInfo();
                }

                if (decision == 2) {
                    decision = 0;
                    getDeviceInfo();

                }


                mHandler.postDelayed(run

                        , 1000);

            }


        });

        btn2.setEnabled(false);
        btn3.setEnabled(false);

        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                String info = myStringArray2.get(position);


                connectBT(info);


            }
        });


        queryPaired();


    }

    private void makeText(String a) {
        Toast.makeText(this, a, Toast.LENGTH_SHORT).show();
    }

    @Override

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    public void checkBT() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        ab = mBluetoothAdapter.isEnabled();

        if (ab == true) {
            scanLeDevice(ab);
        }

        if (ab == false)

        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);


        }


    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);

                    mSwipeRefreshLayout.setRefreshing(false);


                }
            }, SCAN_PERIOD);

            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);


        } else {

            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    public void setAbc() {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this, R.layout.customt,
                myStringArray1);


        lv.setAdapter(arrayAdapter);


    }

    private void connectBT(String info) {

        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(info);
        deviceAddress = info;
        txt.setText(R.string.statusconnecting);
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        mBluetoothGatt.connect();

        mBluetoothGatt.discoverServices();


    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
        makeGattUpdateIntentFilter();
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        String a = characteristic.toString();
        intent.putExtra(EXTRA_DATA, characteristic.getValue());
        Log.i(TAG, a);
        sendBroadcast(intent);
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic, String value, boolean notification) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        Log.i("SYNC_COMM", "TX:" + value.toUpperCase());
        transferedByteData = transferedByteData + value.getBytes().length;
        characteristic.setValue(hexStringToByteArray(value.toUpperCase()));
        mBluetoothGatt.setCharacteristicNotification(characteristic, notification);
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    private void dcbt() {
        mBluetoothGatt.disconnect();
        sendSyncEnd();
    }

    private void queryPaired() {


        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceAddress = device.getAddress();
                if (!myStringArray2.contains(deviceAddress)) {
                    myStringArray2.add(deviceAddress);
                    myStringArray1.add(deviceName + "\n" + deviceAddress);
                }
                setAbc();
            }
        }


    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);

    }


    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            if (gattService.getUuid().equals(GlobalVariables.WEARABLE_SERVICE)) {
                Log.i(TAG, "GET SERVICE");
                // get specific SERVICE UUID
                currentGattService = gattService;
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                // Loops through available Characteristics.
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    // get specific Characteristic UUID
                    if (gattCharacteristic.getUuid().equals(GlobalVariables.TX_DATA_CHAR)) {
                        currentGattCharacteristic = gattCharacteristic;
                        Log.i(TAG, "GET Characteristic");
                        if (currentGattCharacteristic != null) {
                            Log.i(TAG, "GET characteristic");
                            final BluetoothGattCharacteristic characteristic = currentGattCharacteristic;
                            this.setCharacteristicNotification(characteristic, true);
                            this.readCharacteristic(characteristic);

                            for (BluetoothGattDescriptor descriptor : currentGattCharacteristic.getDescriptors()) {
                                Log.i(TAG, "GET descriptor" + descriptor.getUuid());
                            }
                            break;
                        }
                        break;
                    }
                }
            }
        }
    }


    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);


        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(GlobalVariables.CONFIG_DESCRIPTOR);
        //descriptor.setValue("2".getBytes()); //set descriptor value
        //mBluetoothGatt.writeDescriptor(descriptor);
        //}
    }

    private void selectWearableGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            if (gattService.getUuid().equals(GlobalVariables.WEARABLE_SERVICE)) {
                Log.i(TAG, "GET SERVICE");
                // get specific SERVICE UUID
                currentGattService = gattService;
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                // Loops through available Characteristics.
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    // get specific Characteristic UUID
                    if (gattCharacteristic.getUuid().equals(GlobalVariables.TX_DATA_CHAR)) {
                        currentTxGattCharacteristic = gattCharacteristic;
                        Log.d(TAG, "GET Characteristic");
                        if (currentTxGattCharacteristic != null) {
                            Log.d(TAG, "GET characteristic | Permission " + currentTxGattCharacteristic.getPermissions());
                            final BluetoothGattCharacteristic characteristic = currentTxGattCharacteristic;
                            Log.d(TAG, "info: " + currentTxGattCharacteristic.toString());
                            for (BluetoothGattDescriptor descriptor1 : currentTxGattCharacteristic.getDescriptors()) {
                                Log.d(TAG, "info: tx descriptor: " + descriptor1.getUuid().toString());
                            }
                            //Set notification on selected characteristics
                            setCharacteristicNotification(characteristic, true);
                        }
                    }
                    if (gattCharacteristic.getUuid().equals(GlobalVariables.RX_DATA_CHAR)) {
                        currentRxGattCharacteristic = gattCharacteristic;
                        Log.d(TAG, "GET Characteristic");
                        if (currentRxGattCharacteristic != null) {
                            Log.d(TAG, "GET characteristic | Permission " + currentRxGattCharacteristic.getPermissions());
                            final BluetoothGattCharacteristic characteristic = currentRxGattCharacteristic;
                            Log.d(TAG, "info: " + currentRxGattCharacteristic.toString());
                            //Enable notification on selected characteristics
                            setRxCharacteristicNotification(characteristic, true);
                            //Read this characteristic
                            readCharacteristic(characteristic);
                            for (BluetoothGattDescriptor descriptor1 : currentRxGattCharacteristic.getDescriptors()) {
                                Log.d(TAG, "info: rx descriptor: " + descriptor1.getUuid().toString());
                            }
                        }
                    }
                }
            }
        }
    }

    public void setRxCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(GlobalVariables.CONFIG_DESCRIPTOR);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE); //set descriptor value
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        mBluetoothGatt.writeDescriptor(descriptor);
    }

    public void getDeviceInfo() {
        this.CURRENT_MODE = GlobalVariables.USER_INFO_SYNC;
        this.CURRENT_SYNC = GlobalVariables.USER_INFO_SYNC;
        CURRENT_PACKET_SIZE = 7;
        sendWearableCommand = GlobalVariables.CMD_DEVICE_REG_REQUEST;
        writeCharacteristic(currentTxGattCharacteristic, sendWearableCommand, false);
    }


    private String getRequestPacketCommandString(String counterTxt, String parameterTxt) {
        String commd = "FFF40000000003";
        commd = commd.concat(parameterTxt);
        commd = commd.concat(counterTxt);
        String _checkSumText = calculateCheckSum(commd);
        commd = commd.concat(_checkSumText);
        return commd;
    }

    private String calculateCheckSum(String stringData) {
        int hexAddition = addHex(stringData);
        int ck = 0xFF;
        hexAddition = ck - hexAddition + 1;
        int LSB = hexAddition & 0xFF;
        String _lsb = String.format("%02X", LSB);
        return _lsb;
    }

    private int addHex(String stringData) {
        byte[] stringDataBytes = hexStringToByteArray(stringData);
        int count = stringDataBytes.length;
        int result = 0;
        for (int i = 0; i < count; i++) {
            result += stringDataBytes[i];
        }
        return result;
    }


    public void stressSync() {
        this.CURRENT_MODE = GlobalVariables.PACKET_REQUEST_TRANSFER;

        isSyncCompleted = false;//init sync status flag
        isSyncStatusBroadcast = false;
        try {
            this.CURRENT_SYNC = GlobalVariables.STRESS_SYNC;
            CURRENT_PACKET_COMMAND = GlobalVariables.STRESS;
            CURRENT_PACKET_SIZE = commandListDataPacketCounter[0];
            sendWearableCommand = getRequestPacketCommandString("01", GlobalVariables.STRESS);
            Log.d(TAG, "Parameter: " + GlobalVariables.STRESS + " has data size: " + commandListDataPacketCounter[0]);
            writeCharacteristic(currentTxGattCharacteristic, sendWearableCommand, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String rawDataConverter(String rawDataString) {
        return String.valueOf(Long.parseLong(rawDataString, 16));
    }

    public void deviceInfoConverter(ArrayList<String> inputDataArray) {

        String deviceID = null;
        String deviceVersion = null;
        String deviceTime = null;
        String timezone = null;
        String hourlySync = null;
        String hourlyVibrate = null;

        String userID = null;
        String age = null;
        String height = null;
        String weight = null;
        String language = null;
        String gender = null;

        for (int index = 0; index < inputDataArray.size(); index++) {
            Log.d(TAG, "Raw Byte:" + inputDataArray.get(index));
            String dataValue = inputDataArray.get(index).substring(6); // get data row fff2dc
            Log.d(TAG, "dataValue:" + dataValue);

            if (index == 0) { //Device ID, Device Version, Device Time
                deviceID = dataValue.substring(0, 14);
                Log.d(TAG, "deviceID:" + deviceID);
                deviceID = rawDataConverter(deviceID);
                parsedResult.add(deviceID);

                deviceVersion = dataValue.substring(14, 22);
                Log.d(TAG, "deviceVersion:" + deviceVersion);
                deviceVersion = rawDataConverter(deviceVersion);
                parsedResult.add(deviceVersion);

                deviceTime = dataValue.substring(22, 30);
                Log.d(TAG, "DeviceUnixTime:" + deviceTime);
                deviceTime = rawDataConverter(deviceTime);
                parsedResult.add(deviceTime);
                // Add Data Time Converter
                Date tmpTimeStart = new java.util.Date(Long.parseLong(deviceTime));
                String formattedTime = new SimpleDateFormat("HH:mm").format(tmpTimeStart);
                parsedResult.add(formattedTime);
            } else if (index == 1) {//Time zone Hr:Min, Hourly Sync, Hourly Virbrate
                String zoneSign = String.valueOf(dataValue.charAt(0));
                Log.d(TAG, "zeroSign:" + zoneSign);
                if (zoneSign.equalsIgnoreCase("0")) {
                    zoneSign = "+";
                } else {
                    zoneSign = "-";
                }


                String zoneHr = String.valueOf(dataValue.charAt(1));
                Log.d(TAG, "zoneHValue:" + zoneHr);
                zoneHr = rawDataConverter(zoneHr);
                zoneHr = String.format("%02d", Integer.parseInt(zoneHr));


                String zoneMin = String.valueOf(dataValue.substring(2, 4));
                Log.d(TAG, "zoneMValue:" + zoneMin);
                zoneMin = rawDataConverter(zoneMin);
                if (zoneMin.equalsIgnoreCase("0")) {
                    zoneMin = "00";
                } else {
                    zoneMin = String.format("%02d", Integer.parseInt(zoneMin));
                }


                timezone = zoneSign.concat(zoneHr).concat(".").concat(zoneMin);
                parsedResult.add(timezone);

                hourlySync = dataValue.substring(4, 6);
                Log.d(TAG, "hourlySync:" + hourlySync);
                hourlySync = rawDataConverter(hourlySync);
                parsedResult.add(hourlySync);

                hourlyVibrate = dataValue.substring(6, 8);
                Log.d(TAG, "hourlyVibrate:" + hourlyVibrate);
                hourlyVibrate = rawDataConverter(hourlyVibrate);
                parsedResult.add(hourlyVibrate);

            } else if (index == 2) {
                userID = dataValue.substring(0, 16);
                Log.d(TAG, "userID:" + userID);
                userID = asciiStringFromHexString(userID);
                parsedResult.add(userID);

                age = dataValue.substring(16, 18);
                Log.d(TAG, "age:" + age);
                age = rawDataConverter(age);
                parsedResult.add(age);

                height = dataValue.substring(18, 22);
                Log.d(TAG, "height:" + height);
                height = rawDataConverter(height);
                parsedResult.add(height);

                weight = dataValue.substring(22, 26);
                Log.d(TAG, "weight:" + weight);
                weight = rawDataConverter(weight);
                parsedResult.add(weight);

                language = dataValue.substring(26, 28);
                Log.d(TAG, "language:" + language);
                language = rawDataConverter(language);
                parsedResult.add(language);

                gender = dataValue.substring(28, 30);
                Log.d(TAG, "gender:" + gender);
                gender = rawDataConverter(gender);
                parsedResult.add(gender);

            } else {


                Log.d(TAG, "ALARM IDGAF");

            }
        }
    }

    public String asciiStringFromHexString(String hex) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < hex.length(); i += 2) {
            String str = hex.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }

    Runnable run = new Runnable() {
        @Override

        public void run() {
            if (decision == 0) {
                deviceInfoConverter(getUserID);
                userIDData = parsedResult.get(7);
                decision = 2;
                startAllPacketSizeCount();

            }
            mHandler.postDelayed(new Runnable() {
                @Override


                public void run() {



                    dialog1.dismiss();

                    if (decision == 1) {

                        deviceInfoConverter(resultArray);
                        DataRowInfo dr = new DataRowInfo(parsedResult.get(0), parsedResult.get(1), parsedResult.get(2), parsedResult.get(3), parsedResult.get(4), parsedResult.get(5), parsedResult.get(6), parsedResult.get(7), parsedResult.get(8), parsedResult.get(9), parsedResult.get(10), parsedResult.get(11), parsedResult.get(12));

                           /*)
                            Gson gson = new Gson();
                            String json = gson.toJson(dr);
                            passing.add(json);
                            */
                        passing.add("deviceID:" + dr.deviceID);
                        passing.add("deviceVersion:" + dr.deviceVersion);
                        passing.add("deviceTime:" + dr.deviceTime);
                        passing.add("formattedTime:" + dr.formattedTime);
                        passing.add("timezone:" + dr.timezone);
                        passing.add("hourlySync:" + dr.hourlySync);
                        passing.add("hourlyVibrate:" + dr.hourlyVibrate);
                        passing.add("userID:" + dr.userID);
                        passing.add("age:" + dr.age);
                        passing.add("height:" + dr.height);
                        passing.add("weight:" + dr.weight);
                        passing.add("language:" + dr.language);
                        passing.add("gender:" + dr.gender);


                    } else if (decision == 2) {

                        for (int i = 0; i < resultArray.size(); i++) {
                            Log.i(TAG, resultArray.get(i));
                        }
                        objectArray = stressConverter(resultArray);
                        for (Object a : objectArray) {

                            HashMap map1 = (HashMap) a;
                            DataRowStress dr = new DataRowStress((String) map1.get("timeStamp"), (String) map1.get("ASDPP"), (String) map1.get("SDHR"), (String) map1.get("LFHF_Ratio"), (String) map1.get("PP"), (String) map1.get("HR"), (String) map1.get("Quadrant"), (String) map1.get("Type"), (String) map1.get("Interval"));
                            String builder = "stressdata,userid=" + userIDData;
                            builder = builder + " ASDPP=" + dr.ASDPP;
                            builder = builder + ",SDHR=" + dr.SDHR;
                            builder = builder + ",LFHF_Ratio=" + dr.LFHF_Ratio;
                            builder = builder + ",PP=" + dr.PP;
                            builder = builder + ",HR=" + dr.HR;
                            builder = builder + ",Quadrant=" + dr.Quadrant;
                            builder = builder + ",Type=" + dr.Type;
                            builder = builder + ",Interval=" + dr.Interval;
                            builder = builder + " " + dr.timeStamp;
                            Log.i(TAG, dr.timeStamp);
                            passing.add(builder);


                            //Gson gson = new Gson();
                            //String json = gson.toJson(dr);
                            //passing.add(json);

                        }
                    } else if (decision == 0) {


                    }
                        /*
                        map.put("RESULT", resultArray);
                        map.put("PARSED", passing);
                        */
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                    if (decision == 2) {

                        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User clicked OK button
                                String a = "";

                                /*

                                if (decision == 1) {
                                    a = "http://10.5.10.126:8080/api/getDataInfo?a=";

                                    for (int i = 0; i < passing.size(); i++) {
                                        String b = passing.get(i);
                                        show = true;
                                        String d = URLEncoder.encode(b);
                                        a = a + d;
                                        new upload().execute(a);
                                    }
                                } else if(decision == 2){
                                    for (int i = 0; i < passing.size(); i++) {
                                        String b = "http://10.5.10.126:8080/api/getDataStress?a=";
                                        String c = passing.get(i);
                                        String d = URLEncoder.encode(c);
                                        String e = b + d;

                                        if(i == (passing.size() - 1)){
                                            show = true;
                                        } else{
                                            show = false;
                                        }

                                        new upload().execute(e);


                                        Log.i(TAG, String.valueOf(resultArray.isEmpty()));



                                    }
                                }
                                */


                                if (decision == 2) {
                                    for (int i = 0; i < passing.size(); i++) {
                                        if (i == (passing.size() - 1)) {
                                            show = true;
                                        } else {
                                            show = false;
                                        }
                                        new upload().execute(passing.get(i));
                                    }
                                }

                                count2 = passing.size();
                                resultArray.clear();
                                passing.clear();
                                mBluetoothGatt.disconnect();
                                mBluetoothGatt.close();
                                broadcastUpdate(ACTION_GATT_DISCONNECTED);


                            }

                        });
                    }


                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            resultArray.clear();
                            passing.clear();
                            Log.i(TAG, String.valueOf(resultArray.isEmpty()));
                            mBluetoothGatt.disconnect();
                            mBluetoothGatt.close();
                            broadcastUpdate(ACTION_GATT_DISCONNECTED);
                            dialog.dismiss();
                        }
                    });

                    LayoutInflater inflater = getLayoutInflater();
                    View convertView = (View) inflater.inflate(R.layout.custom, null);


                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                            getApplicationContext(), R.layout.customt, passing);


                    AlertDialog dialog = builder.create();
                    dialog.setView(convertView);
                    dialog.setTitle(R.string.datareceived);


                    ListView lv = (ListView) convertView.findViewById(R.id.listView1);
                    lv.setAdapter(arrayAdapter);
                    dialog.show();

                        /*

                        for (int i = 0; i < parsedResult.size(); i++) {
                            Log.i(TAG, parsedResult.get(i));
                            parsedArr[i] = parsedResult.get(i);
                            Log.i(TAG, parsedArr[i]);
                        }
                        */

                    Intent i = new Intent(getApplicationContext(), DataActivity.class);
                    i.putExtra("RESULT", map);

                    //startActivity(i);
                }

            }, 3000);


        }
    };


    public void sendSyncEnd() {
        sendWearableCommand = GlobalVariables.CMD_SYNC_END;
        writeCharacteristic(currentTxGattCharacteristic, sendWearableCommand, false);
        Log.d(TAG, "Send sendSyncEnd");


    }

    public ArrayList<Object> stressConverter(ArrayList<String> inputDataArray) {
        Log.d(TAG, "Input Array:" + inputDataArray);
        HashMap<String, Object> resultDic = new HashMap<>();
        ArrayList<Object> resultArrayList = new ArrayList<Object>();
        String timeStart = inputDataArray.get(0);
        timeStart = timeStart.substring(6);
        timeStart = rawDataConverter(timeStart);
        Log.d(TAG, "Time Check:" + timeStart);
        // Add Data Time Converter
        Date tmpTimeStart = new java.util.Date(Long.parseLong(timeStart) * 1000L);
        String formattedTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z").format(tmpTimeStart);
        Log.d(TAG, "Time Check Str:" + formattedTime);
        for (int index = 1; index < inputDataArray.size(); index++) {
            try {
                Log.d(TAG, "Raw Byte:" + inputDataArray.get(index));
                String dataValue = inputDataArray.get(index); // get data row
                dataValue = dataValue.substring(2, dataValue.length());
                Log.d(TAG, "dataValue:" + dataValue);
                String dc = dataValue.substring(0, 4);
                Log.d(TAG, "dc:" + dc);
                dc = rawDataConverter(dc);
                Log.d(TAG, "dc:" + dc);
                String asdpp = dataValue.substring(4, 8);
                Log.d(TAG, "asdpp:" + asdpp);

                asdpp = rawDataConverter(asdpp);
                asdpp = String.valueOf(Float.parseFloat(asdpp) / 1000);
                Log.d(TAG, "asdpp:" + asdpp);

                String sdhr = dataValue.substring(8, 12);
                Log.d(TAG, "sdhr:" + sdhr);
                sdhr = rawDataConverter(sdhr);
                sdhr = String.valueOf(Float.parseFloat(sdhr) / 1000);
                Log.d(TAG, "sdhr:" + sdhr);
                String lfhf = dataValue.substring(12, 16);
                Log.d(TAG, "lfhf:" + lfhf);
                lfhf = rawDataConverter(lfhf);
                Log.d(TAG, "lfhf:" + lfhf);
                String pp = dataValue.substring(16, 18);
                Log.d(TAG, "pp:" + pp);
                pp = rawDataConverter(pp);
                Log.d(TAG, "pp:" + pp);
                String hr = dataValue.substring(18, 20);
                Log.d(TAG, "hr:" + hr);
                hr = rawDataConverter(hr);
                Log.d(TAG, "hr:" + hr);
                String quadrant = dataValue.substring(20, 22);
                Log.d(TAG, "quadrant:" + quadrant);
                quadrant = rawDataConverter(quadrant);
                Log.d(TAG, "quadrant:" + quadrant);
                String type = dataValue.substring(22, 24);
                Log.d(TAG, "type:" + type);
                type = rawDataConverter(type);
                Log.d(TAG, "type:" + type);
                String interval = dataValue.substring(24, 28);
                Log.d(TAG, "interval:" + interval);
                interval = rawDataConverter(interval);
                Log.d(TAG, "interval:" + interval);

                tmpTimeStart = new java.util.Date((Long.parseLong(timeStart) * 1000L) + (Integer.valueOf(interval) * 60) * 1000L);
                Long a = ((Long.parseLong(timeStart) * 1000L) + (Integer.valueOf(interval) * 60) * 1000L) * 1000000;
                String timeStamp = String.valueOf(a);
                //String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z").format(tmpTimeStart);
                Log.d(TAG, "timeStamp:" + timeStamp);


                resultDic = new HashMap<>();
                resultDic.put("timeStamp", timeStamp);
                resultDic.put("ASDPP", asdpp);
                resultDic.put("SDHR", sdhr);
                resultDic.put("LFHF_Ratio", lfhf);
                resultDic.put("PP", pp);
                resultDic.put("HR", hr);
                resultDic.put("Quadrant", quadrant);
                resultDic.put("Type", type);
                resultDic.put("Interval", interval);
                resultArrayList.add(resultDic);
                resultDic = null;

                resultDic = null;
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "************ Error has occured:");
                Log.d(TAG, "Observe:" + inputDataArray.get(index));
                Log.d(TAG, String.valueOf(index));
                return null;
            }
        }//End loop
        return resultArrayList;
    }

    static public class DataRowStress {

        String timeStamp;
        String ASDPP;
        String SDHR;
        String LFHF_Ratio;
        String PP;
        String HR;
        String Quadrant;
        String Type;
        String Interval;


        public DataRowStress(String _timeStamp, String _ASDPP, String _SDHR, String _LFHF_Ratio, String _PP, String _HR, String _Quadrant, String _Type, String _Interval) {
            timeStamp = _timeStamp;
            ASDPP = _ASDPP;
            SDHR = _SDHR;
            LFHF_Ratio = _LFHF_Ratio;
            PP = _PP;
            HR = _HR;
            Quadrant = _Quadrant;
            Type = _Type;
            Interval = _Interval;
        }


    }

    public class DataRowInfo {

        String deviceID;
        String deviceVersion;
        String deviceTime;
        String formattedTime;
        String timezone;
        String hourlySync;
        String hourlyVibrate;
        String userID;
        String age;
        String height;
        String weight;
        String language;
        String gender;


        public DataRowInfo(String _deviceID, String _deviceVersion, String _deviceTime, String _formattedTime, String _timezone, String _hourlySync, String _hourlyVibrate, String _userID, String _age, String _height, String _weight, String _language, String _gender) {

            deviceID = _deviceID;
            deviceVersion = _deviceVersion;
            deviceTime = _deviceTime;
            formattedTime = _formattedTime;
            timezone = _timezone;
            hourlySync = _hourlySync;
            hourlyVibrate = _hourlyVibrate;
            userID = _userID;
            age = _age;
            height = _height;
            weight = _weight;
            language = _language;
            gender = _gender;

        }


    }

    public class upload extends AsyncTask<String, Void, String> {

        String a;
        int b = 0;
        public ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        @Override
        protected String doInBackground(String... strings) {


            int count = strings.length;
            for (int i = 0; i < count; i++) {
                HttpClient client = new DefaultHttpClient();


                b = i;
                HttpPost httppost = new HttpPost("http://128.199.189.195:8086/write?db=stressdb");
                // replace with your url

                HttpResponse response;
                try {

                    httppost.setEntity(new StringEntity(strings[i]));
                    response = client.execute(httppost);

                    HttpEntity entity = response.getEntity();
                    InputStream inputStream = null;

                    if (inputStream != null) {
                        BufferedReader br = null;
                        StringBuilder sb = new StringBuilder();

                        String line;
                        try {

                            br = new BufferedReader(new InputStreamReader(inputStream));
                            while ((line = br.readLine()) != null) {
                                sb.append(line);
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (br != null) {
                                try {
                                    br.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        a = sb.toString();
                    } else {
                        a = "";
                    }


                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block]
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            return a;
        }

        protected void onPreExecute() {
            dialog.setMessage("Sending data to server...");
            dialog.show();

        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(String result) {

            count1++;
            Log.i(TAG, String.valueOf(count1));
            if (count1 == count2) {
                Toast.makeText(getApplicationContext(), "Uploaded data to server.", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getApplicationContext(), AnalyticsActivity.class);
                finish();
            }


            dialog.dismiss();

        }


        private void makeGetRequest(final String a) {


            new Thread() {
                @Override
                public void run() {
                    HttpClient client = new DefaultHttpClient();
                    HttpGet request = new HttpGet(a);
                    // replace with your url

                    HttpResponse response;
                    try {
                        response = client.execute(request);

                        HttpEntity entity = response.getEntity();
                        InputStream inputStream = entity.getContent();


                        Log.d("Response of GET request", response.getStatusLine().toString());
                    } catch (ClientProtocolException e) {
                        // TODO Auto-generated catch block]
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }.start();


        }
    }

    public void startAllPacketSizeCount() {

        CURRENT_MODE = GlobalVariables.PACKET_REQUEST_INFO;
        isGetAllDataPacketSize = true;
        commandPacketInfoListIndex = 0;

        Log.d(TAG, "Start Automatic Data Packet Count");
        sendWearableCommand = getCommandString("0000", GlobalVariables.SYNC_COMMAND_LIST[0]);
        try {
            writeCharacteristic(currentTxGattCharacteristic, sendWearableCommand, true);
            commandPacketInfoListIndex++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getCommandString(String counterTxt, String parameterTxt) {
        String commd = "FFF400";
        commd = commd.concat(counterTxt);
        commd = commd.concat("0003");
        commd = commd.concat(parameterTxt);
        commd = commd.concat("00");
        commd = commd.concat(calculateCheckSum(commd));
        return commd;
    }

    private int getNumberOfDataPacket(String tmpByteBuffer) {
        String packetLen = "00";
        String tmpString = dataPacketInfoCleanup(tmpByteBuffer);
        try {
            if (tmpString.startsWith("FFF4")) {
                packetLen = tmpString.substring(6, 10);
                byte[] t1 = hexStringToByteArray(packetLen);
                String requestCommand = tmpString.substring(14, 16);
                CURRENT_PACKET_COMMAND = requestCommand;
                CURRENT_PACKET_SIZE = ((t1[0] & 0xFF) << 8) | (t1[1] & 0xFF);
                CURRENT_PACKET_COUNTER = 1;// initial counter
                Log.d(TAG, "SDK:" + CURRENT_PACKET_SIZE + "/" + !isGetAllDataPacketSize);
                if (CURRENT_PACKET_SIZE > 0 && (!isGetAllDataPacketSize)) {
                    sendDataPacketRequest(CURRENT_PACKET_SIZE, CURRENT_PACKET_COMMAND, CURRENT_PACKET_COUNTER);
                    Log.d(TAG, "L1:");
                } else if (CURRENT_PACKET_SIZE <= 0 && (!isGetAllDataPacketSize)) {
                    Log.d(TAG, "L2:");
                }
                Log.d(TAG, "Current Command: " + CURRENT_PACKET_COMMAND + " Packet size:" + CURRENT_PACKET_SIZE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CURRENT_PACKET_SIZE;
    }

    private String dataPacketInfoCleanup(String d) {
        int startPos = 0;
        int endPos = 0;
        char[] s = d.toCharArray();
        for (int i = 0; i < d.length(); i++) {
            if (i + 21 <= d.length()) {
                if ((s[i + 0] == 'F') && (s[i + 1] == 'F') && (s[i + 2] == 'F') && (s[i + 3] == '4') && (s[i + 20] == '0') && (s[i + 21] == 'D') && (s[i + 22] == '0') && (s[i + 23] == 'A')) {
                    startPos = i;
                }
            }
            if (i + 7 <= d.length()) {
                if ((s[i + 0] == '0') && (s[i + 1] == 'D') && (s[i + 2] == '0') && (s[i + 3] == 'A')) {
                    endPos = i;
                    break;
                } else {
                    endPos = d.length();
                }
            }
        }
        String tmpResult = d.copyValueOf(s, startPos, endPos - startPos);
        Log.d(TAG, "Head:" + startPos + " Tail:" + endPos + " data Packet:" + tmpResult);
        return tmpResult;
    }

    private boolean sendDataPacketRequest(int packetLen, String dataPacketCommand, int currentSendingCounter) {
        boolean isFinished = false;
        String packetRequestCommand = getCommandString(String.format("%04x", (currentSendingCounter)), dataPacketCommand);
        this.CURRENT_MODE = GlobalVariables.PACKET_REQUEST_TRANSFER;
        try {
            writeCharacteristic(currentTxGattCharacteristic, packetRequestCommand, true);
            isFinished = true;
            Log.d(TAG, "Sent " + dataPacketCommand + " LEN:" + packetLen + " packetNum" + String.format("%04x", (currentSendingCounter)) + " Command:" + packetRequestCommand);
            CURRENT_PACKET_COUNTER++;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isFinished;
    }

    private void sendAck() {
        sendWearableCommand = getDataTransferAckCommandString(CURRENT_PACKET_COMMAND);
        writeCharacteristic(currentTxGattCharacteristic, sendWearableCommand, false);
        Log.d(TAG, "Send ACK");
    }


    public String getDataTransferAckCommandString(String parameterTxt) {
        String commd = GlobalVariables.ACK_NEXT_DATA;
        return commd;
    }

    public void checkpacket(int a) {

        int tmpPacketSize = a;
        if (commandPacketInfoListIndex == GlobalVariables.SYNC_COMMAND_LIST.length) {
            Log.i(TAG, ("STRESS:[" + commandListDataPacketCounter[0] + "]/STEP:[" + commandListDataPacketCounter[1] + "] /SLEEP:[" + commandListDataPacketCounter[2] + "]"));
            check = true;
            if (decision == 2) {
                stressSync();
            }

        } else if (commandPacketInfoListIndex < GlobalVariables.SYNC_COMMAND_LIST.length && (isGetAllDataPacketSize)) {
            commandListDataPacketCounter[commandPacketInfoListIndex - 1] = tmpPacketSize;
            Log.d(TAG, "Start info request number:" + (commandPacketInfoListIndex - 1));
            sendWearableCommand = getCommandString("0000", GlobalVariables.SYNC_COMMAND_LIST[commandPacketInfoListIndex]);
            try {
                writeCharacteristic(currentTxGattCharacteristic, sendWearableCommand, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d(TAG, "End info request number:" + (commandPacketInfoListIndex - 1));
        } else {

        }
        commandPacketInfoListIndex++;
    }

}







