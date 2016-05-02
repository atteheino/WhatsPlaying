package fi.atteheino.whatsplaying;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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
        if(intent.filterEquals(new Intent(Constants.START_LISTENING_BROADCASTS))){
            mService.registerMyMusicBroadcastReceiver();
        }
        if(intent.filterEquals(new Intent(Constants.STOP_LISTENING_BROADCASTS))){
            mService.unregisterReceivers();
        }
    }
}
