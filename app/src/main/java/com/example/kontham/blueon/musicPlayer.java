package com.example.kontham.blueon;

import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.MainThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import static java.util.logging.Logger.global;

public class musicPlayer extends scandevices {


    private String[] mMusicList;
    public Cursor mCursor;
    public MediaPlayer mMediaPlayer = new MediaPlayer();
    public static int time1;
    public static int posit;
    private String TAG = "You are here";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);


        ListView mListView = (ListView) findViewById(R.id.list1);

        mMusicList = getMusic();

        if ((savedInstanceState != null)
                && (savedInstanceState.getSerializable("mediaplayer") != null)) {
            mMediaPlayer = (MediaPlayer) savedInstanceState.getSerializable("mediaplayer");
        }


        ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(this,
                R.layout.playlist, mMusicList);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {
                posit = position;
                try {
                    System.out.println(mMusicList[position]);
                    playSong(position);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.d(TAG, "onSaveInstanceState");
        savedInstanceState.putSerializable("mediaplayer",  (Serializable) mMediaPlayer);
    }



    @Override

    public void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);
        Log.v(TAG, "Inside of onRestoreInstanceState");
        mMediaPlayer = (MediaPlayer) savedInstanceState.getSerializable("mediaplayer");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private String[] getMusic() {
            mCursor = managedQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, null);

        int count = mCursor.getCount();
        System.out.println(MediaStore.Audio.Media.DISPLAY_NAME);

        String[] songs = new String[count];
        paths = new String[count];
        int i = 0;
        if (mCursor.moveToFirst()) {
            do {
                int id1 = mCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                int id2 = mCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
                songs[i] = mCursor.getString(id2);
                paths[i] = mCursor.getString(id1);
                i++;
            } while (mCursor.moveToNext());
        }

        mCursor.close();

        return songs;
    }

    private void playSong(final int pos) throws IllegalArgumentException, IllegalStateException, IOException {
        if (mMediaPlayer.isPlaying()){
            mMediaPlayer.stop();
        }

        mMediaPlayer.reset();
        mMediaPlayer.setDataSource(paths[pos]);
        mMediaPlayer.prepare();
        time1 = mMediaPlayer.getDuration();
        mMediaPlayer.start();

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (pos < paths.length) {
                    mMediaPlayer.reset();
       /* load the new source */
                    try {
                        mMediaPlayer.setDataSource(paths[(pos + 1)%paths.length]);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
       /* Prepare the mediaplayer */
                    try {
                        mMediaPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
       /* start */
                    time1 = mMediaPlayer.getDuration();
                    mMediaPlayer.start();
                } else {
       /* release mediaplayer */
                    mMediaPlayer.release();
                }
            }
        });
        finish();
    }

    public static int retTIME() {
        return time1;
    }


}



