package com.ereinecke.spotifystreamer;

import android.media.MediaPlayer;
import android.util.Log;

/**
 * Subclass of MediaPlayer to aid in debugging
 */
public class DebugMediaPlayer extends MediaPlayer {

    private static final String LOG_TAG = "DebugMediaPlayer";

    @Override
    public void start() {
        Log.d(LOG_TAG, "Calling start()");

        super.start();
    }

    @Override
    public void stop() {
        Log.d(LOG_TAG, "Calling stop()");
        super.stop();
    }

    @Override
    public void pause() {
        Log.d(LOG_TAG, "Calling pause()");
        super.pause();
    }

    @Override
    public void release() {
        Log.d(LOG_TAG, "Calling release()");
        super.release();
    }

    @Override
    public void reset() {
        Log.d(LOG_TAG, "Calling reset()");
        super.reset();
    }

}
