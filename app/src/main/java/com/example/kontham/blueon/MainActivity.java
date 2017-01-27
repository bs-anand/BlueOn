package com.example.kontham.blueon;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.browse.MediaBrowser;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.kontham.blueon.R;


public class MainActivity extends AppCompatActivity {
    public BluetoothAdapter bluetoothAdapter;
    private Button scan;
    private Button Turnoff;
    private Button play;
    private Button send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        scan = (Button) findViewById(R.id.scan);
        Turnoff = (Button) findViewById(R.id.turnoff);
        play = (Button) findViewById(R.id.play);
        send = (Button) findViewById(R.id.send);

        scan.setEnabled(false);
        Turnoff.setEnabled(false);
        play.setEnabled(false);
        send.setEnabled(false);
    }

    @Override
    public void onResume(){
        super.onResume();
        if (bluetoothAdapter.isEnabled()) {
            scan.setEnabled(true);
            Turnoff.setEnabled(true);
            play.setEnabled(true);
            send.setEnabled(true);

        }

    }

    public void OnbuttonClickView(android.view.View v) {
        if (bluetoothAdapter == null) {
            Toast.makeText(MainActivity.this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();

        }
        else {
            if (bluetoothAdapter.isEnabled()) {
                Toast.makeText(MainActivity.this, "Bluetooth is already on!!!", Toast.LENGTH_SHORT).show();
            }
            else  {
                Toast.makeText(MainActivity.this, "Bluetooth starting........", Toast.LENGTH_LONG).show();
                Intent enableBluetooth = new Intent (BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetooth,1);
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if(bluetoothAdapter.isEnabled())
        {
            scan.setEnabled(true);
            Turnoff.setEnabled(true);
            play.setEnabled(true);
            send.setEnabled(true);
        }

    }


    public void OnDiscoverButtonClick(View v) {
        Intent intent = new Intent("com.example.kontham.blueon.scandevices");
        startActivity(intent);
    }

    public void OnMusicButtonClick(View arg0) {
        Intent intent = new Intent();
        intent.setType("audio/mp3/wav/aac");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(
                intent, "Open Audio (mp3) file"), 1);

    }

    public void OnSendButton(View view) {
        Log.d("hell", "hai");
        Intent intent = new Intent("com.example.kontham.blueon.sendFileActivity");
        startActivity(intent);
    }

    public void OnTurnOff(View view) {
        scan.setEnabled(false);
        Turnoff.setEnabled(false);
        play.setEnabled(false);
        send.setEnabled(false);
        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
        } else {
            Toast.makeText(this, "Bluetooth is Already off", Toast.LENGTH_LONG).show();
        }
    }
}
