package fi.atteheino.whatsplaying;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import fi.atteheino.whatsplaying.constants.Constants;

public class CloseServiceHelperBroadcastReceiver extends BroadcastReceiver {
    private final static String TAG = "CloseServiceHelper";

    public CloseServiceHelperBroadcastReceiver() {
    }

    @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received Broadcast Message: " + intent.toString());


            if (intent.getAction().equals(Constants.CLOSE_SERVICE_REQ)) {
                Intent intent2 = new Intent(Constants.CLOSE_SERVICE);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent2);
                Log.i(TAG, "Intent broadcasted: " + Constants.CLOSE_SERVICE);
            }

        }





}
