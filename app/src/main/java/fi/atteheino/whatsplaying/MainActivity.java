package fi.atteheino.whatsplaying;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import fi.atteheino.whatsplaying.constants.Constants;

public class MainActivity extends AppCompatActivity {


    private Switch mIsActive;
    private TextView mArtist;
    private TextView mAlbum;
    private TextView mTrack;


        CompoundButton.OnCheckedChangeListener mOnCheckedIsActiveListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked){
                createAndSendBroadcast(Constants.START_LISTENING_BROADCASTS);
            } else {
                createAndSendBroadcast(Constants.STOP_LISTENING_BROADCASTS);
                setDefaultsForUI();
            }
        }
    };

    private void createAndSendBroadcast(String intentAction) {
        Intent intent = new Intent(intentAction);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
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

    }

}
