package com.ereinecke.spotifystreamer;

import android.app.IntentService;
import android.content.Intent;

/**
 * Media Player foreground service
 */
public class PlayerService extends IntentService {

    public PlayerService() {
        super("PlayerService");

    }

    @Override
    protected void onHandleIntent(Intent intent) {


    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        return 0; // placeholder return value
    }


}
