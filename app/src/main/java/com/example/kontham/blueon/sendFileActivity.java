package com.example.kontham.blueon;


import java.io.File;
import java.util.Set;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class sendFileActivity extends AppCompatActivity {

    private static final String TAG = "BTSendFile";

    private final int ACTIVITY_SELECT_IMAGE = 1;
    private final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    private Button select;
    private Button sendDirectly;
    private TextView textStatus;
    private Uri uri;
    private String filePath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_file);
        Log.d(TAG, "hai");

        textStatus = (TextView) findViewById(R.id.sendStatus);
        select = (Button) findViewById(R.id.select);
        sendDirectly = (Button) findViewById(R.id.sendDirectly);

        select.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
               // Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                //i.setType("*/*");
                //i.addCategory(Intent.CATEGORY_OPENABLE);
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, ACTIVITY_SELECT_IMAGE);
            }
        });

        sendDirectly.setEnabled(false);
        sendDirectly.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                System.out.println(v);

                if (btAdapter.isEnabled()) {
                    Set<BluetoothDevice> devices = btAdapter.getBondedDevices();
                    final String btDeviceName = v.toString();
                    BluetoothDevice device = null;

                       /*ContentValues values = new ContentValues();
                        // values.put(BluetoothShare.URI, Uri.fromFile(new
                        // File(uri.getPath())).toString());
                        values.put(BluetoothShare.URI, uri.toString());
                        values.put(BluetoothShare.MIMETYPE, "image/jpeg");
                        values.put(BluetoothShare.DESTINATION, device.getAddress());
                        values.put(BluetoothShare.DIRECTION, BluetoothShare.DIRECTION_OUTBOUND);
                        Long ts = System.currentTimeMillis();
                        values.put(BluetoothShare.TIMESTAMP, ts);
                        final Uri contentUri = getApplicationContext().getContentResolver().insert(BluetoothShare.CONTENT_URI, values);
                        Log.v(TAG, "Insert contentUri: " + contentUri + "  to device: " + device.getName());*/

                  /*  if(filePath.startsWith("file")||filePath.startsWith("content")||filePath.startsWith("FILE")||filePath.startsWith("CONTENT")){

                    }else{
                        filePath="file://"+filePath;
                    }*/
                    /*Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(filePath));
                    shareIntent.setType("video/mp4");
                    startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.sendTo)));*/

                   Intent sharingIntent = new Intent();
                    sharingIntent.setAction(Intent.ACTION_VIEW);
                    sharingIntent.setAction(Intent.ACTION_SEND);
                    sharingIntent.setType("file");
                    sharingIntent.setComponent(new ComponentName("com.android.bluetooth", "com.android.bluetooth.opp.BluetoothOppLauncherActivity"));
                    //Uri fileuri = Uri.parse(filePath);
                    //filePath = "/storage/emulated/0/DCIM/100ANDRO/DSC_0019.JPG";
                    File file = new File(filePath);

                    System.out.println(file);
                    System.out.println(Uri.fromFile(file));
                    file.setReadable(true);

                    sharingIntent.putExtra(Intent.EXTRA_STREAM ,Uri.fromFile(file));
                    //System.out.println(fileuri);
                    startActivity(Intent.createChooser(sharingIntent, "share File"));





                }
                else {
                    textStatus.setText("Bluetooth not activated");
                }
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTIVITY_SELECT_IMAGE:
                if (resultCode == RESULT_OK) {
                    uri = data.getData();
                    filePath = getRealPathFromURI(uri);
                    textStatus.setText(filePath);
                    sendDirectly.setEnabled(true);
                } else {
                    sendDirectly.setEnabled(false);
                }
                break;
            default:
                assert   false;
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }
}
