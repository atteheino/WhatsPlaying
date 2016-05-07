package fi.atteheino.whatsplaying;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import fi.atteheino.whatsplaying.constants.Constants;
import fi.atteheino.whatsplaying.service.WhatsPlayingService;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";
    private Switch mIsActive;
    private TextView mArtist;
    private TextView mAlbum;
    private TextView mTrack;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getStringExtra(Constants.EXTRA_ARTIST) != null) {
                mArtist.setText(intent.getStringExtra(Constants.EXTRA_ARTIST));
            }
            if(intent.getStringExtra(Constants.EXTRA_ALBUM) != null) {
                mAlbum.setText(intent.getStringExtra(Constants.EXTRA_ALBUM));
            }
            if(intent.getStringExtra(Constants.EXTRA_TRACK) != null) {
                mTrack.setText(intent.getStringExtra(Constants.EXTRA_TRACK));
            }
        }
    };
    private CompoundButton.OnCheckedChangeListener mOnCheckedIsActiveListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SharedPreferences settings = getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, 0);
            SharedPreferences.Editor editor = settings.edit();
            if (isChecked) {
                Intent intent = new Intent(getApplicationContext(), WhatsPlayingService.class);
                startService(intent);
                Log.i(TAG, "Starting Service");
                editor.putBoolean(Constants.LISTENING_ACTIVE, true);
                createAndSendBroadcast(Constants.START_LISTENING_BROADCASTS);
            } else {
                editor.putBoolean(Constants.LISTENING_ACTIVE, false);
                createAndSendBroadcast(Constants.STOP_LISTENING_BROADCASTS);
                setDefaultsForUI();
            }
            editor.apply();
        }
    };
    Button.OnClickListener mCloseButtonOnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            mIsActive.setOnCheckedChangeListener(null);
            mIsActive.setChecked(false);
            mIsActive.setOnCheckedChangeListener(mOnCheckedIsActiveListener);
            SharedPreferences settings = getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(Constants.LISTENING_ACTIVE, false);

            setDefaultsForUI();
            createAndSendBroadcast(Constants.CLOSE_SERVICE);
        }
    };
    private Button mCloseButton;

    private void createAndSendBroadcast(String intentAction) {
        Intent intent = new Intent(intentAction);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.i(TAG, "Intent broadcasted: " + intentAction);
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

        mCloseButton = (Button) findViewById(R.id.stopButton);
        mCloseButton.setOnClickListener(mCloseButtonOnClickListener);

        registerSongInfoBroadcastReceiver();

        Intent intent = new Intent(this, WhatsPlayingService.class);
        startService(intent);
        Log.i(TAG, "Starting the service...");

    }

    private void registerSongInfoBroadcastReceiver() {
        IntentFilter miF = new IntentFilter(Constants.TRACK_INFO);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, miF);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences settings = getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, 0);
        if (settings.getBoolean(Constants.LISTENING_ACTIVE, false)) {
            mIsActive = (Switch) findViewById(R.id.isActive);
            mIsActive.setChecked(true);
        }
        registerSongInfoBroadcastReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }
}
