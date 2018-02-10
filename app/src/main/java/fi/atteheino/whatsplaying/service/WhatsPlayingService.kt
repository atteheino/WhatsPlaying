package fi.atteheino.whatsplaying.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.support.v4.content.LocalBroadcastManager
import android.util.Log

import java.util.Locale

import fi.atteheino.whatsplaying.CloseServiceHelperBroadcastReceiver
import fi.atteheino.whatsplaying.MainActivity
import fi.atteheino.whatsplaying.MySongBroadcastReceiver
import fi.atteheino.whatsplaying.R
import fi.atteheino.whatsplaying.constants.Constants

class WhatsPlayingService : Service() {
    private var mTextToSpeech: TextToSpeech? = null
    private var mReceiver: MySongBroadcastReceiver? = null
    private val mMessengerReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            Log.i(TAG, "Received Broadcast Message: " + intent.toString())

            if (intent.filterEquals(Intent(Constants.START_LISTENING_BROADCASTS))) {
                registerMyMusicBroadcastReceiver()
            }
            if (intent.filterEquals(Intent(Constants.STOP_LISTENING_BROADCASTS)) && mReceiver != null) {
                unregisterSongBroadcastReceiver()
            }
            if (intent.action == Constants.CLOSE_SERVICE) {
                closeService()
            }
            if (intent.action == Constants.VERBOSITY_INTENT) {
                setVerbosityOfReceiver()
            }

        }
    }

    override fun onBind(intent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Binding is not supported")
    }

    fun registerMyMusicBroadcastReceiver() {
        val iF = IntentFilter()
        iF.addAction("com.android.music.metachanged")
        iF.addAction("com.htc.music.metachanged")
        iF.addAction("fm.last.android.metachanged")
        iF.addAction("com.sec.android.app.music.metachanged")
        iF.addAction("com.nullsoft.winamp.metachanged")
        iF.addAction("com.amazon.mp3.metachanged")
        iF.addAction("com.miui.player.metachanged")
        iF.addAction("com.real.IMP.metachanged")
        iF.addAction("com.sonyericsson.music.metachanged")
        iF.addAction("com.rdio.android.metachanged")
        iF.addAction("com.samsung.sec.android.MusicPlayer.metachanged")
        iF.addAction("com.andrew.apollo.metachanged")
        iF.addAction("com.spotify.music.metadatachanged")


        registerReceiver(mReceiver, iF)
        Log.i(TAG, "MyMusicBroadcastReceiver registered")
    }

    private fun registerMessengerIntentReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Constants.START_LISTENING_BROADCASTS)
        intentFilter.addAction(Constants.STOP_LISTENING_BROADCASTS)
        intentFilter.addAction(Constants.CLOSE_SERVICE)
        intentFilter.addAction(Constants.VERBOSITY_INTENT)
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessengerReceiver, intentFilter)
        Log.i(TAG, "Messenger Intent Receiver registered")
    }

    fun unregisterSongBroadcastReceiver() {
        try {
            if (mReceiver != null) {
                unregisterReceiver(mReceiver)
                Log.i(TAG, "SongBroadcastReceiver unregistered")
                cancelNotification()
                Log.i(TAG, "Notification Cancelled")
            }
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Caught exception while trying to unregister BroadcastReceiver. No worries...")
        }

    }

    private fun unregisterReceivers() {
        try {
            if (mReceiver != null)
                unregisterReceiver(mReceiver)
            if (mMessengerReceiver != null)
                LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessengerReceiver)
            Log.i(TAG, "BroadcastReceivers unregistered")
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Caught exception while trying to unregister BroadcastReceiver. No worries...")
        }

    }

    fun closeService() {
        Log.i(TAG, "Closing Service")
        cancelNotification()
        stopSelf()
    }

    private fun cancelNotification() {
        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.cancel(Constants.NOTIFICATION_ID)
    }

    private fun setVerbosityOfReceiver() {
        if (mReceiver != null) {
            val settings = getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, 0)
            mReceiver!!.setVerbosity(settings.getInt(Constants.VERBOSITY, 0))
        }
    }

    override fun onCreate() {
        super.onCreate()
        val settings = getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, 0)

        Log.i(TAG, "Starting WhatsPlayingService")
        mTextToSpeech = TextToSpeech(this, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR) {
                mTextToSpeech!!.language = Locale.UK
            }
        })
        mReceiver = MySongBroadcastReceiver(this, mTextToSpeech!!)
        if (settings.getBoolean(Constants.LISTENING_ACTIVE, false)) {
            registerMyMusicBroadcastReceiver()
        }

        setVerbosityOfReceiver()
        registerMessengerIntentReceiver()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceivers()
        cancelNotification()
        mTextToSpeech!!.shutdown()
    }

    fun sendNotification(infoText: String) {
        val homeIntent = Intent(this, MainActivity::class.java)
        val pendingHomeIntent = PendingIntent.getActivity(this, 0, homeIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val closeIntent = Intent(Constants.CLOSE_SERVICE_REQ)
        closeIntent.setClass(this, CloseServiceHelperBroadcastReceiver::class.java)
        val pendingCloseIntent = PendingIntent.getBroadcast(this, 0, closeIntent, 0)
        val closeServiceAction = Notification.Action.Builder(
                R.drawable.ic_close,
                getString(R.string.notification_close),
                pendingCloseIntent).build()


        val notification = Notification.Builder(this).setVisibility(Notification.VISIBILITY_PUBLIC).setSmallIcon(R.drawable.ic_stat_logo2_notification).setContentTitle(getString(R.string.notification_header)).setContentText(infoText).setContentIntent(pendingHomeIntent).addAction(closeServiceAction).build()
        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(Constants.NOTIFICATION_ID, notification)
        Log.i(TAG, "Notification sent: " + infoText)
    }

    companion object {
        private val TAG = "WhatsPlayingService"
    }

}
