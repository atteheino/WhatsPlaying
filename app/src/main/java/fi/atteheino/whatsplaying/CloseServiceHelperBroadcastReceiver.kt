package fi.atteheino.whatsplaying

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v4.content.LocalBroadcastManager
import android.util.Log

import fi.atteheino.whatsplaying.constants.Constants

class CloseServiceHelperBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "Received Broadcast Message: " + intent.toString())


        if (intent.action == Constants.CLOSE_SERVICE_REQ) {
            val settings = context.getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, 0)
            val editor = settings.edit()
            editor.putBoolean(Constants.LISTENING_ACTIVE, false)
            editor.apply()

            val intent2 = Intent(Constants.CLOSE_SERVICE)
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent2)
            Log.i(TAG, "Intent broadcasted: " + Constants.CLOSE_SERVICE)
        }

    }

    companion object {
        private val TAG = "CloseServiceHelper"
    }


}
