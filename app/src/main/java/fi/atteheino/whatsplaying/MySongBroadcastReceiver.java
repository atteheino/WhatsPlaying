package fi.atteheino.whatsplaying;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.UUID;

import fi.atteheino.whatsplaying.service.WhatsPlayingService;

public class MySongBroadcastReceiver extends BroadcastReceiver {
    private final static long PAUSE_TIME = 500;
    private TextToSpeech mTextToSpeech;
    private WhatsPlayingService mService;

    public MySongBroadcastReceiver() {
    }
    public MySongBroadcastReceiver(WhatsPlayingService whatsPlayingService, TextToSpeech textToSpeech){
        this.mTextToSpeech = textToSpeech;
        this.mService = whatsPlayingService;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        String cmd = intent.getStringExtra("command");
        Log.v("tag ", action + " / " + cmd);
        String artist = intent.getStringExtra("artist");
        String album = intent.getStringExtra("album");
        String track = intent.getStringExtra("track");
        Log.v("tag", artist + ":" + album + ":" + track);

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

        mService.sendNotification(artist + " " + artistSpeak + " " + trackSpeak);
    }

}
