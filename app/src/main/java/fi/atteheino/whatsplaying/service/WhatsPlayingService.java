package fi.atteheino.whatsplaying.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.LocalBroadcastManager;

import java.util.Locale;

import fi.atteheino.whatsplaying.MyMessengerBroadcastReceiver;
import fi.atteheino.whatsplaying.MySongBroadcastReceiver;
import fi.atteheino.whatsplaying.constants.Constants;

public class WhatsPlayingService extends Service {
    private TextToSpeech mTextToSpeech;
    private MySongBroadcastReceiver mReceiver;
    private MyMessengerBroadcastReceiver mMessengerReceiver;
    public WhatsPlayingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Binding is not supported");
    }

    public void registerMyMusicBroadcastReceiver() {
        IntentFilter iF = new IntentFilter();
        iF.addAction("com.android.music.metachanged");
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

    private void registerMessengerIntentReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.START_LISTENING_BROADCASTS);
        intentFilter.addAction(Constants.STOP_LISTENING_BROADCASTS);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessengerReceiver, intentFilter);
    }

    public void unregisterReceivers(){
        unregisterReceiver(mReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessengerReceiver);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mTextToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    mTextToSpeech.setLanguage(Locale.UK);
                }
            }
        });
        mReceiver = new MySongBroadcastReceiver(mTextToSpeech);
        registerMyMusicBroadcastReceiver();
        mMessengerReceiver = new MyMessengerBroadcastReceiver(this);
        registerMessengerIntentReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceivers();
    }


}
