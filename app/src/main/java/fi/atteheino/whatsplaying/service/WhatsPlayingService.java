package fi.atteheino.whatsplaying.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Locale;

import fi.atteheino.whatsplaying.MyMessengerBroadcastReceiver;
import fi.atteheino.whatsplaying.MySongBroadcastReceiver;
import fi.atteheino.whatsplaying.R;
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
        Log.i("tag", "MyMusicBroadcastReceiver registered");
    }

    private void registerMessengerIntentReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.START_LISTENING_BROADCASTS);
        intentFilter.addAction(Constants.STOP_LISTENING_BROADCASTS);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessengerReceiver, intentFilter);
        Log.i("tag", "Messenger Intent Receiver registered");
    }

    public void unregisterReceivers(){
        unregisterReceiver(mReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessengerReceiver);
        Log.i("tag", "BroadcastReceivers unregistered");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences settings = getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, 0);

        Log.i("tag", "Starting WhatsPlayingService");
        mTextToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    mTextToSpeech.setLanguage(Locale.UK);
                }
            }
        });
        mReceiver = new MySongBroadcastReceiver(this, mTextToSpeech);
        if(settings.getBoolean(Constants.LISTENING_ACTIVE, false)){
            registerMyMusicBroadcastReceiver();
        }
        mMessengerReceiver = new MyMessengerBroadcastReceiver(this);
        registerMessengerIntentReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceivers();
    }

    public void sendNotification(String infoText){
        Notification notification = new Notification.Builder(this)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.notification_header))
                .setContentText(infoText)
                .build();
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(Constants.NOTIFICATION_ID, notification);
        Log.i("tag", "Notification sent: " + infoText);
    }

}
