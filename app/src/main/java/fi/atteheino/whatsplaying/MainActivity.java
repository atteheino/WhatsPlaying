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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import org.honorato.multistatetogglebutton.MultiStateToggleButton;
import org.honorato.multistatetogglebutton.ToggleButton;

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
            Log.i(TAG, "Received intent: " + intent);
            if (intent.getAction().equals(Constants.INSTANCE.getTRACK_INFO())) {
                if (intent.getStringExtra(Constants.INSTANCE.getEXTRA_ARTIST()) != null) {
                    mArtist.setText(intent.getStringExtra(Constants.INSTANCE.getEXTRA_ARTIST()));
                }
                if (intent.getStringExtra(Constants.INSTANCE.getEXTRA_ALBUM()) != null) {
                    mAlbum.setText(intent.getStringExtra(Constants.INSTANCE.getEXTRA_ALBUM()));
                }
                if (intent.getStringExtra(Constants.INSTANCE.getEXTRA_TRACK()) != null) {
                    mTrack.setText(intent.getStringExtra(Constants.INSTANCE.getEXTRA_TRACK()));
                }
            }
            if (intent.getAction().equals(Constants.INSTANCE.getCLOSE_SERVICE())){
                mIsActive.setOnCheckedChangeListener(null);
                mIsActive.setChecked(false);
                mIsActive.setOnCheckedChangeListener(mOnCheckedIsActiveListener);
                setDefaultsForUI();
            }
        }
    };

    private MultiStateToggleButton mVerbosityButton;
    private CompoundButton.OnCheckedChangeListener mOnCheckedIsActiveListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SharedPreferences settings = getSharedPreferences(Constants.INSTANCE.getSHARED_PREFERENCES_FILE(), 0);
            SharedPreferences.Editor editor = settings.edit();
            if (isChecked) {
                Intent intent = new Intent(getApplicationContext(), WhatsPlayingService.class);
                startService(intent);
                Log.i(TAG, "Starting Service");
                editor.putBoolean(Constants.INSTANCE.getLISTENING_ACTIVE(), true);
                createAndSendBroadcast(Constants.INSTANCE.getSTART_LISTENING_BROADCASTS());
            } else {
                editor.putBoolean(Constants.INSTANCE.getLISTENING_ACTIVE(), false);
                createAndSendBroadcast(Constants.INSTANCE.getSTOP_LISTENING_BROADCASTS());
                setDefaultsForUI();
            }
            editor.apply();
        }
    };

    private ToggleButton.OnValueChangedListener mVerbosityButtonOnValueChangedListener = new ToggleButton.OnValueChangedListener() {
        @Override
        public void onValueChanged(int position) {
            Log.d(TAG, "Verbosity is set to: " + getResources().getStringArray(R.array.verbosity_array)[position] );
            SharedPreferences settings = getSharedPreferences(Constants.INSTANCE.getSHARED_PREFERENCES_FILE(), 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(Constants.INSTANCE.getVERBOSITY(), position);
            editor.apply();
            createAndSendBroadcast(Constants.INSTANCE.getVERBOSITY_INTENT());
        }
    };

    private void createAndSendBroadcast(String intentAction) {
        Intent intent = new Intent(intentAction);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.i(TAG, "Intent broadcasted: " + intentAction);
    }


    private void setDefaultsForUI() {
        mTrack.setText("TRACK");
        mAlbum.setText("ALBUM");
        mArtist.setText("ARTIST");
        setVerbosityUI();
    }

    private void setVerbosityUI() {
        SharedPreferences settings = getSharedPreferences(Constants.INSTANCE.getSHARED_PREFERENCES_FILE(), 0);
        mVerbosityButton.setValue(settings.getInt(Constants.INSTANCE.getVERBOSITY(), 0));
        createAndSendBroadcast(Constants.INSTANCE.getVERBOSITY_INTENT());
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

        mVerbosityButton = (MultiStateToggleButton) this.findViewById(R.id.verbosity);
        mVerbosityButton.setOnValueChangedListener(mVerbosityButtonOnValueChangedListener);
        setVerbosityUI();

        registerSongInfoBroadcastReceiver();

        Intent intent = new Intent(this, WhatsPlayingService.class);
        startService(intent);
        Log.i(TAG, "Starting the service...");

    }

    private void registerSongInfoBroadcastReceiver() {
        IntentFilter miF = new IntentFilter(Constants.INSTANCE.getTRACK_INFO());
        miF.addAction(Constants.INSTANCE.getCLOSE_SERVICE());
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, miF);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences settings = getSharedPreferences(Constants.INSTANCE.getSHARED_PREFERENCES_FILE(), 0);
        if (settings.getBoolean(Constants.INSTANCE.getLISTENING_ACTIVE(), false)) {
            mIsActive = (Switch) findViewById(R.id.isActive);
            mIsActive.setChecked(true);
        }
        setVerbosityUI();
        registerSongInfoBroadcastReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_about) {
            Intent aboutIntent = new Intent(getApplicationContext(), About.class);
            startActivity(aboutIntent);
            return true;
        }
        if (id == R.id.action_close_service ) {
            mIsActive.setOnCheckedChangeListener(null);
            mIsActive.setChecked(false);
            mIsActive.setOnCheckedChangeListener(mOnCheckedIsActiveListener);
            SharedPreferences settings = getSharedPreferences(Constants.INSTANCE.getSHARED_PREFERENCES_FILE(), 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(Constants.INSTANCE.getLISTENING_ACTIVE(), false);
            editor.apply();

            setDefaultsForUI();
            createAndSendBroadcast(Constants.INSTANCE.getCLOSE_SERVICE());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
