package fi.atteheino.whatsplaying;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Calendar;
import java.util.UUID;

import fi.atteheino.whatsplaying.constants.Constants;
import fi.atteheino.whatsplaying.service.WhatsPlayingService;

public class MySongBroadcastReceiver extends BroadcastReceiver {
    private final static String TAG = "MySongBroadcastReceiver";
    private final static long PAUSE_TIME = 500;
    private int VERBOSITY = 0;
    private TextToSpeech mTextToSpeech;
    private WhatsPlayingService mService;

    private String mPreviousArtist;
    private String mPreviousAlbum;
    private String mPreviousTrack;
    private Calendar mPreviousSongTimestamp;

    public MySongBroadcastReceiver() {
    }

    public MySongBroadcastReceiver(WhatsPlayingService whatsPlayingService, TextToSpeech textToSpeech) {
        this.mTextToSpeech = textToSpeech;
        this.mService = whatsPlayingService;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        int queueAction = TextToSpeech.QUEUE_FLUSH;

        // Receive Intent and break down data
        String action = intent.getAction();
        String cmd = intent.getStringExtra("command");
        Log.v(TAG, action + " / " + cmd);
        String artist = intent.getStringExtra("artist");
        String album = intent.getStringExtra("album");
        String track = intent.getStringExtra("track");
        Log.v(TAG, artist + ":" + album + ":" + track);


        //Let's speak out the previous song title if available
        if (mPreviousSongTimestamp != null && VERBOSITY<2) {
            Calendar thirtyMinutesAgo = Calendar.getInstance();
            thirtyMinutesAgo.add(Calendar.MINUTE, -30);
            //Was the last song played less than 30 minutes ago?
            if(thirtyMinutesAgo.before(mPreviousSongTimestamp)){
                queueAction = TextToSpeech.QUEUE_ADD;
                String prevArtist = "";
                String prevTrackSpeak = "";
                switch (VERBOSITY){
                    case 1:
                        prevArtist = mPreviousArtist;
                        prevTrackSpeak = mPreviousTrack;
                        break;
                    default:
                        prevArtist = "That was " + mPreviousArtist;
                        prevTrackSpeak = "with track " + mPreviousTrack;
                }
                mTextToSpeech.speak(prevArtist, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString());
                silence();
                mTextToSpeech.speak(prevTrackSpeak, TextToSpeech.QUEUE_ADD,null,UUID.randomUUID().toString());
                silence();
            }
        }
        String artistSpeak = "";
        String albumSpeak = "";
        String trackSpeak = "";
        //Now the next song
        switch (VERBOSITY){
            case 1: // Normal
                artistSpeak = artist;
                albumSpeak = album;
                trackSpeak = track;
                break;
            case 2: // Short
                artistSpeak = artist;
                trackSpeak = track;
                break;
            default: //Verbose
                artistSpeak = "Next " + artist;
                albumSpeak = "from album " + album;
                trackSpeak = "track " + track;
        }

        if (artist != null) {
            mTextToSpeech.speak(artistSpeak, queueAction, null, UUID.randomUUID().toString());
            silence();
            if(!albumSpeak.isEmpty()) {
                mTextToSpeech.speak(albumSpeak, TextToSpeech.QUEUE_ADD, null, UUID.randomUUID().toString());
                silence();
            }
            mTextToSpeech.speak(trackSpeak, TextToSpeech.QUEUE_ADD, null, UUID.randomUUID().toString());
            //Store values
            mPreviousSongTimestamp = Calendar.getInstance();
            mPreviousArtist = artist;
            mPreviousAlbum = album;
            mPreviousTrack = track;
        }

        mService.sendNotification(artist + " " + artistSpeak + " " + trackSpeak);

        Intent trackInfoIntent = new Intent(Constants.TRACK_INFO);
        trackInfoIntent.putExtra(Constants.EXTRA_ARTIST, artist);
        trackInfoIntent.putExtra(Constants.EXTRA_ALBUM, album);
        trackInfoIntent.putExtra(Constants.EXTRA_TRACK, track);
        LocalBroadcastManager.getInstance(context).sendBroadcast(trackInfoIntent);
        Log.i(TAG, "Sent intent" + trackInfoIntent);

    }

    private void silence() {
        mTextToSpeech.playSilentUtterance(PAUSE_TIME, TextToSpeech.QUEUE_ADD, UUID.randomUUID().toString());
    }

    public void setVerbosity(int verbosity) {
        VERBOSITY = verbosity;
        Log.i(TAG, "Setting verbosity to:" + verbosity);
    }

}
