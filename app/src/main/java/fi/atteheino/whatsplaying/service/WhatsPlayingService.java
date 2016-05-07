package fi.atteheino.whatsplaying.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Locale;

import fi.atteheino.whatsplaying.CloseServiceHelperBroadcastReceiver;
import fi.atteheino.whatsplaying.MainActivity;
import fi.atteheino.whatsplaying.MySongBroadcastReceiver;
import fi.atteheino.whatsplaying.R;
import fi.atteheino.whatsplaying.constants.Constants;

public class WhatsPlayingService extends Service {
    private final static String TAG = "WhatsPlayingService";
    private TextToSpeech mTextToSpeech;
    private MySongBroadcastReceiver mReceiver;
    private BroadcastReceiver mMessengerReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received Broadcast Message: " + intent.toString());

            if (intent.filterEquals(new Intent(Constants.START_LISTENING_BROADCASTS))) {
                registerMyMusicBroadcastReceiver();
            }
            if (intent.filterEquals(new Intent(Constants.STOP_LISTENING_BROADCASTS)) && mReceiver!=null) {
                unregisterSongBroadcastReceiver();
            }
            if (intent.getAction().equals(Constants.CLOSE_SERVICE)) {
                closeService();
            }

        }
    };

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
        Log.i(TAG, "MyMusicBroadcastReceiver registered");
    }

    private void registerMessengerIntentReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.START_LISTENING_BROADCASTS);
        intentFilter.addAction(Constants.STOP_LISTENING_BROADCASTS);
        intentFilter.addAction(Constants.CLOSE_SERVICE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessengerReceiver, intentFilter);
        Log.i(TAG, "Messenger Intent Receiver registered");
    }

    public void unregisterSongBroadcastReceiver() {
        if(mReceiver != null) {
            unregisterReceiver(mReceiver);
            Log.i(TAG, "SongBroadcastReceiver unregistered");
            cancelNotification();
            Log.i(TAG, "Notification Cancelled");
        }
    }

    private void unregisterReceivers() {
        if(mReceiver != null)
            unregisterReceiver(mReceiver);
        if(mMessengerReceiver!=null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessengerReceiver);
        Log.i(TAG, "BroadcastReceivers unregistered");
    }

    public void closeService() {
        Log.i(TAG, "Closing Service");
        cancelNotification();
        stopSelf();
    }

    private void cancelNotification() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(Constants.NOTIFICATION_ID);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences settings = getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, 0);

        Log.i(TAG, "Starting WhatsPlayingService");
        mTextToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    mTextToSpeech.setLanguage(Locale.UK);
                }
            }
        });
        mReceiver = new MySongBroadcastReceiver(this, mTextToSpeech);
        if (settings.getBoolean(Constants.LISTENING_ACTIVE, false)) {
            registerMyMusicBroadcastReceiver();
        }

        registerMessengerIntentReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceivers();
        cancelNotification();
        mTextToSpeech.shutdown();
    }

    public void sendNotification(String infoText) {
        Intent homeIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingHomeIntent = PendingIntent.getActivity(this, 0, homeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent closeIntent = new Intent(Constants.CLOSE_SERVICE_REQ);
        closeIntent.setClass(this, CloseServiceHelperBroadcastReceiver.class);
        PendingIntent pendingCloseIntent = PendingIntent.getBroadcast(this, 0, closeIntent, 0);
        Notification.Action closeServiceAction = new Notification.Action.Builder(
                R.drawable.ic_close,
                getString(R.string.notification_close),
                pendingCloseIntent)
                .build();


        Notification notification = new Notification.Builder(this)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.notification_header))
                .setContentText(infoText)
                .setContentIntent(pendingHomeIntent)
                .addAction(closeServiceAction)
                .build();
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(Constants.NOTIFICATION_ID, notification);
        Log.i(TAG, "Notification sent: " + infoText);
    }

}
