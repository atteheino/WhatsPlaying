package fi.atteheino.whatsplaying;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import fi.atteheino.whatsplaying.constants.Constants;
import fi.atteheino.whatsplaying.service.WhatsPlayingService;

public class MyMessengerBroadcastReceiver extends BroadcastReceiver {
    private WhatsPlayingService mService;

    public MyMessengerBroadcastReceiver() {
    }

    public MyMessengerBroadcastReceiver(WhatsPlayingService mService) {
        this.mService = mService;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("tag","Received Broadcast Message: " + intent.toString());
        if(intent.filterEquals(new Intent(Constants.START_LISTENING_BROADCASTS))){
            mService.registerMyMusicBroadcastReceiver();
        }
        if(intent.filterEquals(new Intent(Constants.STOP_LISTENING_BROADCASTS))){
            mService.unregisterSongBroadcastReceiver();
        }
        if(intent.filterEquals(new Intent(Constants.CLOSE_SERVICE))){
            mService.closeService();
        }
    }
}
