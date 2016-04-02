package fi.atteheino.whatsplaying;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private final static long PAUSE_TIME = 500;
    private TextToSpeech mTextToSpeech;
    private Switch mIsActive;
    private TextView mArtist;
    private TextView mAlbum;
    private TextView mTrack;


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {



        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String cmd = intent.getStringExtra("command");
            Log.v("tag ", action + " / " + cmd);
            String artist = intent.getStringExtra("artist");
            String album = intent.getStringExtra("album");
            String track = intent.getStringExtra("track");
            Log.v("tag", artist + ":" + album + ":" + track);


            mArtist.setText("ARTIST\n" + artist);
            mAlbum.setText("ALBUM\n" + album);
            mTrack.setText("TRACK\n"+track);

            String artistSpeak = artist;
            String albumSpeak = "from album " + album;
            String trackSpeak = "track " + track;
            if(artist != null) {
                mTextToSpeech.speak(artistSpeak, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString());
                mTextToSpeech.playSilentUtterance(PAUSE_TIME, TextToSpeech.QUEUE_ADD, UUID.randomUUID().toString());
                mTextToSpeech.speak(albumSpeak, TextToSpeech.QUEUE_ADD, null, UUID.randomUUID().toString());
                mTextToSpeech.playSilentUtterance(PAUSE_TIME, TextToSpeech.QUEUE_ADD, UUID.randomUUID().toString());
                mTextToSpeech.speak(trackSpeak, TextToSpeech.QUEUE_ADD, null, UUID.randomUUID().toString());
            }
        }
    };
    CompoundButton.OnCheckedChangeListener mOnCheckedIsActiveListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked){
                registerIntentListener();
            } else {
                unregisterReceiver(mReceiver);
                setDefaultsForUI();
            }
        }
    };

    private void registerIntentListener() {
        IntentFilter iF = new IntentFilter();
        iF.addAction("com.android.music.metachanged");
//        iF.addAction("com.android.music.playstatechanged");
//        iF.addAction("com.android.music.playbackcomplete");
//        iF.addAction("com.android.music.queuechanged");
        iF.addAction("com.htc.music.metachanged");
        iF.addAction("fm.last.android.metachanged");
        iF.addAction("com.sec.android.app.music.metachanged");
        iF.addAction("com.nullsoft.winamp.metachanged");
        iF.addAction("com.amazon.mp3.metachanged");
        iF.addAction("com.miui.player.metachanged");
        iF.addAction("com.real.IMP.metachanged");
        iF.addAction("com.sonyericsson.music.metachanged");
        iF.addAction("com.rdio.android.metachanged");
        iF.addAction("com.samsung.sec.android.MusicPlayer.metachanged");
        iF.addAction("com.andrew.apollo.metachanged");
        iF.addAction("com.spotify.music.metadatachanged");


        registerReceiver(mReceiver, iF);
    }

    private void setDefaultsForUI() {
        mTrack.setText("TRACK");
        mAlbum.setText("ALBUM");
        mArtist.setText("ARTIST");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mArtist = (TextView) findViewById(R.id.artist);
        mAlbum = (TextView) findViewById(R.id.album);
        mTrack = (TextView) findViewById(R.id.track);

        mIsActive = (Switch) findViewById(R.id.isActive);
        mIsActive.setOnCheckedChangeListener(mOnCheckedIsActiveListener);

        mTextToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    mTextToSpeech.setLanguage(Locale.UK);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mIsActive.isChecked()){
            unregisterReceiver(mReceiver);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mIsActive.isChecked()){
            registerIntentListener();
        }
    }
}
