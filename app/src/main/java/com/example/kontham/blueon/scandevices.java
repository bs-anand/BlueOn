package com.example.kontham.blueon;

import android.app.Activity;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static android.R.attr.data;
import static android.R.attr.publicKey;


public class scandevices extends AppCompatActivity {

    private static final String TAG = "DeviceListActivity";
    public int size_pair;
    public String[] paths;
    private int time;
    public SparseBooleanArray checked;
    public int size;

    private String file;


    /*Adapters for paired_device list and new_device list */
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    ArrayAdapter<String> pairedDevicesArrayAdapter;

    /*Array list where the bluetooth devices are stored*/
    public ArrayList<BluetoothDevice> bluetoothScanDevices = new ArrayList<>();

    /*List where the bluetooth devices are stored as strings*/
    public ArrayList<String> SelectedDevices = new ArrayList<>();

    /*List views to show the paired device list and new device list*/
    public ListView pairedListView ;

    /*String to show the bluetooth device address as string*/
    public  String targetDeviceAddress;

    /*Bluetooth socket for the communication between the devices*/
    public BluetoothSocket bluetoothSocket;

    /*Bluetooth Adapter */
    private BluetoothAdapter mBtAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scandevices);

        setResult(Activity.RESULT_CANCELED);


        /*
        On click of the scan button
         */
        final Button scanButton = (Button) findViewById(R.id.scandevices);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
                setTitle("Scanning");
                scanButton.setText("Scanning");
            }
        });

        /*
        Paired device array adapter
         */
        pairedDevicesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, SelectedDevices);
        pairedListView = (ListView) findViewById(R.id.list_item_paired);
        pairedListView.setAdapter(pairedDevicesArrayAdapter);
        pairedListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);


        /*
        List view for newly available devices
         */
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.devicename);
        ListView newDeviceListView = (ListView) findViewById(R.id.list_item_scan);
        newDeviceListView.setAdapter(mNewDevicesArrayAdapter);
        newDeviceListView.setOnItemClickListener(DeviceClickListenerscan);

        /*
        Registering the Broadcast Receiver for the device discovery
         */
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        /*
        Registering the Broadcast Reciever for the Discovery Finish
         */
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        /*
        Get the Local Bluetooth Adapter
         */
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        /*
        Get the set of currently paired devices
         */
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        /*
        If there is paired devices in the set
         */
        size_pair = pairedDevices.size();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                SelectedDevices.add(device.getName() + "\n" + device.getAddress());
                bluetoothScanDevices.add(device);
            }
        } else {

        }
    }



    public void onPlayButton  (View view) {
        SparseBooleanArray temp = pairedListView.getCheckedItemPositions();
        int temp1 = temp.size();// number of name-value pairs in the array
        System.out.println("size = " + temp1);
        checked = temp;
        size = temp1;
        try {
            Intent intent = new Intent("com.example.kontham.blueon.musicPlayer");
            startActivityForResult(intent, 1);

        } catch (Exception e) {
            System.out.println(e);
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        System.out.println(time);
        time = musicPlayer.retTIME();

        Thread t = new Thread(new Runnable() {
            public void run() {
                // Insert some method call here.
                if (size > 3) {
                    Toast.makeText(getApplicationContext(), "More than 3 items selected", Toast.LENGTH_SHORT).show();
                } else for (int i = 0; i < size; i++) {

                    int key = checked.keyAt(i);
                    boolean value = checked.get(key);
                    if (value)
                        System.out.println("value" + key);

                    DeviceClickListener(key);
                    try {
                        System.out.println((time/1000));
                        TimeUnit.SECONDS.sleep((time/1000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();
    }

    public void DeviceClickListener(int key) {
        ConnectThread connectThread = null;
        try {
            connectThread = new ConnectThread(bluetoothScanDevices.get(key));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            connectThread.start();
        } catch (NullPointerException e) {
            System.out.println("caught");
        }

        connectUsingBluetoothA2dp(getApplicationContext(),bluetoothScanDevices.get(key));



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        Log.d(TAG, "doDiscovery()");
        //setTitle(R.string.scanning);

        // If we're already discovering, stop it
       /* if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }*/

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }


    private AdapterView.OnItemClickListener DeviceClickListenerscan
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();
            if (getBondState(mBtAdapter.getRemoteDevice(bluetoothScanDevices.get(arg2 + size_pair).getAddress())) == "Paired") {
                Toast.makeText(getApplicationContext(), "Device is already paired",
                        Toast.LENGTH_SHORT).show();
                // finish();
            } else {
                connect(bluetoothScanDevices.get(arg2 + size_pair).getAddress());
            }
        }
    };

    private String getBondState(BluetoothDevice bluetoothVar) {
        int checkState = bluetoothVar.getBondState();
        switch (checkState) {
            case 10:
                return "Not Paired";
            case 11:
                return "Not yet paired";
            case 12:
                return "Paired";
            default:
                return "Not paired";
        }
    }

    private void connect(String address) {
        targetDeviceAddress = address;
        new BluetoothConnectAsynchronously().execute();
    }

    private class BluetoothConnectAsynchronously extends AsyncTask<Void, String, Void> {
        @Override
        protected void onPreExecute() {
            mBtAdapter.cancelDiscovery();
        }

        @Override
        protected Void doInBackground(Void... params) {
            connectToDevice();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Toast.makeText(getApplicationContext(), "Please wait ..", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    behaveOnConnectResult();
                }
            }, 5000);
        }
    }

    private void behaveOnConnectResult() {
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        /*
        If there is paired devices in the set
         */
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                SelectedDevices.add(device.getName() + "\n" + device.getAddress());
                bluetoothScanDevices.add(device);
            }
        } else {

        }
        //  Intent intent = new Intent(getApplicationContext(),getApplicationContext().getClass());
        //  startActivity(intent);
    }

    private void connectToDevice() {
        Log.d(TAG, "Connect to device");

        BluetoothDevice targetDeviceToConnect = mBtAdapter.getRemoteDevice(targetDeviceAddress);
        //UUID DEFAULT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


        try {
            Method m = targetDeviceToConnect.getClass().getMethod("createRfcommSocket", int.class);
            bluetoothSocket = (BluetoothSocket) m.invoke(targetDeviceToConnect, 2);

            //bluetoothSocket = targetDeviceToConnect.createRfcommSocketToServiceRecord(DEFAULT_UUID);

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            bluetoothSocket.connect();


        } catch (IOException e) {
            e.printStackTrace();
            Log.d("TAG", e.toString());

            try {
                bluetoothSocket.close();

            } catch (IOException inner) {
                inner.printStackTrace();
            }
        }
    }

    /**
     * The BroadcastReceiver that listens for discovered devices and changes the title when
     * discovery is finished
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Button scanButton = (Button) findViewById(R.id.scandevices);
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {

                    bluetoothScanDevices.add(device);
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    // bluetoothScanDevices.add(device);

                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                scanButton.setText("Scan Complete");
                setTitle(R.string.select_device);
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                    Toast.makeText(getApplicationContext(),"got",Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private UUID DEFAULT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        public ConnectThread(BluetoothDevice device) throws IOException {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                if (mmDevice!=null) {
                    Log.i(TAG,"DEVICE NAMe"+mmDevice.getName());
                    Log.i(TAG,"UUID"+mmDevice.getUuids()[0].getUuid());
                    tmp = device.createRfcommSocketToServiceRecord(mmDevice.getUuids()[0].getUuid());
                }
                else Log.i(TAG,"device is NUlL");

            } catch (NullPointerException e) {
                tmp = device.createInsecureRfcommSocketToServiceRecord(DEFAULT_UUID);
            }
            catch (IOException e){}
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBtAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();

            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            //manageConnectedSocket(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    public void connectUsingBluetoothA2dp(Context context,
                                          final BluetoothDevice deviceToConnect) {

        try {
            Class<?> c2 = Class.forName("android.os.ServiceManager");
            Method m2 = c2.getDeclaredMethod("getService", String.class);
            IBinder b = (IBinder) m2.invoke(c2.newInstance(), "bluetooth_a2dp");
            if (b == null) {
                // For Android 4.2 Above Devices
                BluetoothAdapter.getDefaultAdapter().getProfileProxy(context,
                        new BluetoothProfile.ServiceListener() {

                            @Override
                            public void onServiceDisconnected(int profile) {

                            }

                            @Override
                            public void onServiceConnected(int profile,
                                                           BluetoothProfile proxy) {
                                BluetoothA2dp a2dp = (BluetoothA2dp) proxy;
                                try {
                                    a2dp.getClass()
                                            .getMethod("connect",BluetoothDevice.class)
                                            .invoke(a2dp, deviceToConnect);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }, BluetoothProfile.A2DP);

            } else {
                // For Android below 4.2 devices

                // Class<?>[] s2 = c3.getDeclaredClasses();
                //Class<?> c = s2[0];
                // Method m = c.getDeclaredMethod("asInterface", IBinder.class);
                // m.setAccessible(true);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
