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
        try {
            super.start();
        } catch (Exception e) {
            Log.d(LOG_TAG, "Exception starting MediaPlayer: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        Log.d(LOG_TAG, "Calling stop()");
        try {
            super.stop();
        } catch (Exception e) {
            Log.d(LOG_TAG, "Exception stopping MediaPlayer: " + e.getMessage());
        }
    }

    @Override
    public void pause() {
        Log.d(LOG_TAG, "Calling pause()");
        try {
            super.pause();
        } catch (Exception e) {
            Log.d(LOG_TAG, "Exception pausing MediaPlayer: " + e.getMessage());
        }
    }

    @Override
    public void release() {
        Log.d(LOG_TAG, "Calling release()");
        try {
            super.release();
        } catch (Exception e) {
            Log.d(LOG_TAG, "Exception releasing MediaPlayer: " + e.getMessage());
        }
    }

    @Override
    public void reset() {
        Log.d(LOG_TAG, "Calling reset()");
        try {
            super.reset();
        } catch (Exception e) {
            Log.d(LOG_TAG, "Exception releasing MediaPlayer: " + e.getMessage());
        }
    }
}
