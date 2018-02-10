package fi.atteheino.whatsplaying

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.support.v4.content.LocalBroadcastManager
import android.util.Log

import java.util.Calendar
import java.util.UUID

import fi.atteheino.whatsplaying.constants.Constants
import fi.atteheino.whatsplaying.service.WhatsPlayingService
import java.text.SimpleDateFormat

class MySongBroadcastReceiver : BroadcastReceiver {
    private var VERBOSITY = 0
    private var mTextToSpeech: TextToSpeech? = null
    private var mService: WhatsPlayingService? = null
    private var mRunning = false

    private var mPreviousArtist: String? = null
    private var mPreviousAlbum: String? = null
    private var mPreviousTrack: String? = null
    private var mPreviousSongTimestamp: Calendar? = null

    constructor() {
    }

    constructor(whatsPlayingService: WhatsPlayingService, textToSpeech: TextToSpeech) {
        this.mTextToSpeech = textToSpeech
        this.mService = whatsPlayingService
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.v(TAG, "Running: " + mRunning)
        mRunning=true
        var queueAction = TextToSpeech.QUEUE_FLUSH

        // Receive Intent and break down data
        val action = intent.action
        val cmd = intent.getStringExtra("command")
        Log.v(TAG, action + " / " + cmd)
        val artist = intent.getStringExtra("artist")
        val album = intent.getStringExtra("album")
        val track = intent.getStringExtra("track")
        Log.v(TAG, "$artist:$album:$track")
        val format = SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS" )
        if(mPreviousSongTimestamp != null)
            Log.v(TAG, "Previous Song: $mPreviousArtist:$mPreviousAlbum:$mPreviousTrack:${format. format(mPreviousSongTimestamp?.time)}")

        // Only perform action if song actually changed.
        if(isCurrentlyPlayingSong(artist, album, track)) {
            Log.v(TAG, "Received notification about same song already playing.")
        } else {
            //Let's speak out the previous song title if available
            if (mPreviousSongTimestamp != null && VERBOSITY < 2 && mPreviousArtist != null && mPreviousTrack != null) {
                val thirtyMinutesAgo = Calendar.getInstance()
                thirtyMinutesAgo.add(Calendar.MINUTE, -30)
                //Was the last song played less than 30 minutes ago?
                if (thirtyMinutesAgo.before(mPreviousSongTimestamp)) {
                    queueAction = TextToSpeech.QUEUE_ADD
                    val prevArtist = "That was " + mPreviousArtist!!
                    mTextToSpeech?.speak(prevArtist, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
                    silence()
                    mTextToSpeech?.speak(getPrevTrackSpeakText(), TextToSpeech.QUEUE_ADD, null, UUID.randomUUID().toString())
                    silence()
                }
            }
            var artistSpeak = ""
            var albumSpeak = ""
            var trackSpeak = ""
            //Now the next song
            when (VERBOSITY) {
                1 // Normal
                -> {
                    artistSpeak = artist
                    albumSpeak = album
                    trackSpeak = track
                }
                2 // Short
                -> {
                    artistSpeak = artist
                    trackSpeak = track
                }
                else //Verbose
                -> {
                    artistSpeak = "Next " + artist!!
                    albumSpeak = "from album " + album
                    trackSpeak = "track " + track
                }
            }

            if (artist != null) {
                mTextToSpeech?.speak(artistSpeak, queueAction, null, UUID.randomUUID().toString())
                silence()
                if (!albumSpeak.isEmpty()) {
                    mTextToSpeech?.speak(albumSpeak, TextToSpeech.QUEUE_ADD, null, UUID.randomUUID().toString())
                    silence()
                }
                mTextToSpeech?.speak(trackSpeak, TextToSpeech.QUEUE_ADD, null, UUID.randomUUID().toString())
                //Store values
                mPreviousSongTimestamp = Calendar.getInstance()
                mPreviousArtist = artist
                mPreviousAlbum = album
                mPreviousTrack = track
            }

            mService?.sendNotification(artist + ": " + track)

            val trackInfoIntent = Intent(Constants.TRACK_INFO)
            trackInfoIntent.putExtra(Constants.EXTRA_ARTIST, artist)
            trackInfoIntent.putExtra(Constants.EXTRA_ALBUM, album)
            trackInfoIntent.putExtra(Constants.EXTRA_TRACK, track)
            LocalBroadcastManager.getInstance(context).sendBroadcast(trackInfoIntent)
            Log.i(TAG, "Sent intent" + trackInfoIntent)
        }
        mRunning=false
    }

    private fun isCurrentlyPlayingSong(artist: String?, album: String?, song: String?): Boolean {
        return (mPreviousArtist.equals(artist) && mPreviousAlbum.equals(album) && mPreviousTrack.equals(song))
    }

    private fun getPrevTrackSpeakText(): String {
        when (VERBOSITY) {
            1 -> return mPreviousTrack as String
            else -> return "with track " + mPreviousTrack!!
        }
    }

    private fun silence() {
        mTextToSpeech?.playSilentUtterance(PAUSE_TIME, TextToSpeech.QUEUE_ADD, UUID.randomUUID().toString())
    }

    fun setVerbosity(verbosity: Int) {
        VERBOSITY = verbosity
        Log.i(TAG, "Setting verbosity to:" + verbosity)
    }

    companion object {
        private val TAG = "MySongBroadcastReceiver"
        private val PAUSE_TIME: Long = 500
    }

}
