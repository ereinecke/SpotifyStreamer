package com.ereinecke.spotifystreamer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;

/**
 * Media Player foreground service
 * Significant contribution from these tutorials:
 *      http://truiton.com/2014/10/android-foreground-service-example
 *      http://code.tutsplus.com/tutorials/create-a-music-player-on-android-song-playback--mobile-22778
 *
 *      TODO: Handle audio focus
 */

    public class PlayerService extends Service implements MediaPlayer.OnErrorListener,
            MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private static final String LOG_TAG = PlayerService.class.getSimpleName();
    private static MediaPlayer mMediaPlayer;
    private static boolean playing = false;
    private final IBinder mBinder = new PlayerBinder();

    public PlayerService() {}

    public class PlayerBinder extends Binder {
        PlayerService getService() {
            return PlayerService.this;
        }
    }

    // Sets up notification
    public void newTrack(ShowTopTracks currentTrack, Bitmap currentTrackArt) {

        Log.d(LOG_TAG, "Setting up new track");
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        // Previous
        Intent previousIntent = new Intent(this, PlayerService.class);
        previousIntent.setAction(Constants.PREV_ACTION);
        PendingIntent pPreviousIntent = PendingIntent.getService(this, 0, previousIntent, 0);

        // Play
        Intent playIntent = new Intent(this, PlayerService.class);
        playIntent.setAction(Constants.PLAY_ACTION);
        PendingIntent pPlayIntent = PendingIntent.getService(this, 0, playIntent, 0);

        // Next
        Intent nextIntent = new Intent(this, PlayerService.class);
        nextIntent.setAction(Constants.NEXT_ACTION);
        PendingIntent pNextIntent = PendingIntent.getService(this, 0, nextIntent, 0);

        if (currentTrack != null) {
            // Set up notification
            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle(currentTrack.artistName)
                    .setTicker(currentTrack.trackName)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(Bitmap.createScaledBitmap(currentTrackArt, 128, 128, false))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .addAction(android.R.drawable.ic_media_previous,
                            getResources().getString(R.string.previous), pPreviousIntent)
                    .addAction(android.R.drawable.ic_media_play,
                            getResources().getString(R.string.play), pPlayIntent)
                    .addAction(android.R.drawable.ic_media_next,
                            getResources().getString(R.string.next), pNextIntent)
                    .build();

            startForeground(Constants.NOTIFICATION_ID, notification);

            try {
                mMediaPlayer.setDataSource(currentTrack.trackMediaUrl);
                Log.d(LOG_TAG, "setDatasource to " + currentTrack.trackMediaUrl);
            } catch (IOException e) {
                Log.d(LOG_TAG, "Can't setDataSource to " + currentTrack.trackMediaUrl);
                e.printStackTrace();
            }
        }
    }

    public void playTrack() {
        Log.i(LOG_TAG, "Clicked Play");
        // If playing, pause
//        if (mMediaPlayer.isPlaying()) {
        if (playing) {
            playing = false;
            mMediaPlayer.pause();
        } else {  // if not playing, play!
            playing = true;
            mMediaPlayer.prepareAsync();
        }
    }

    public void stopForegroundService() {
        Log.i(LOG_TAG, "Stopping foreground service");
        stopForeground(true);
        stopSelf();
    }

    // saved copy of OnHandleIntent

//    // onHandleIntent if for intent service.  How do we handle intents for service?
//    @Override
//    public void onHandleIntent(Intent intent) {
//        super(onHandleIntent(intent));
//        Bitmap selectedAlbumArt;
//
//        String action = intent.getAction();
//        if (action.equals(Constants.STARTFOREGROUND_ACTION)) {
//
//            // Start
//            Log.d(LOG_TAG, "Received Start Foreground Intent ");
//            Intent notificationIntent = new Intent(this, MainActivity.class);
//            notificationIntent.setAction(Constants.MAIN_ACTION);
//            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
//                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
//
//            // Previous
//            Intent previousIntent = new Intent(this, PlayerService.class);
//            previousIntent.setAction(Constants.PREV_ACTION);
//            PendingIntent pPreviousIntent = PendingIntent.getService(this, 0, previousIntent, 0);
//
//            // Play
//            Intent playIntent = new Intent(this, PlayerService.class);
//            playIntent.setAction(Constants.PLAY_ACTION);
//            PendingIntent pPlayIntent = PendingIntent.getService(this, 0, playIntent, 0);
//
//            // Next
//            Intent nextIntent = new Intent(this, PlayerService.class);
//            nextIntent.setAction(Constants.NEXT_ACTION);
//            PendingIntent pNextIntent = PendingIntent.getService(this, 0, nextIntent, 0);
//
//            // Get current track info
//            ShowTopTracks currentTrack = TopTracksFragment.getTrackInfo()
//                    .getParcelable(Constants.TRACK_INFO);
//            // Getting album art for notification
//            if (currentTrack != null) {
//                try {
//                    selectedAlbumArt = Picasso.with(getApplicationContext())
//                            .load(currentTrack.trackImageUrl)
//                            .resize(128, 128)
//                            .get();
//                } catch (IOException e) {
//                    selectedAlbumArt = ((BitmapDrawable) getResources()
//                            .getDrawable(R.mipmap.ic_launcher)).getBitmap();
//                    e.printStackTrace();
//                }
//
//                // Set up notification
//                Notification notification = new NotificationCompat.Builder(this)
//                        .setContentTitle(currentTrack.artistName)
//                        .setTicker(currentTrack.trackName)
//                        .setSmallIcon(R.mipmap.ic_launcher)
//                        .setLargeIcon(Bitmap.createScaledBitmap(selectedAlbumArt, 128, 128, false))
//                        .setContentIntent(pendingIntent)
//                        .setOngoing(true)
//                        .addAction(android.R.drawable.ic_media_previous,
//                                getResources().getString(R.string.previous), pPreviousIntent)
//                        .addAction(android.R.drawable.ic_media_play,
//                                getResources().getString(R.string.play), pPlayIntent)
//                        .addAction(android.R.drawable.ic_media_next,
//                                getResources().getString(R.string.next), pNextIntent)
//                        .build();
//
//                startForeground(Constants.NOTIFICATION_ID, notification);
//            }
//
//            // Previous Intent
//            else if (intent.getAction().equals(Constants.PREV_ACTION)) {
//                Log.i(LOG_TAG, "Clicked Previous");
//            }
//            // Play Intent
//            else if (intent.getAction().equals(Constants.PLAY_ACTION)) {
//                Log.i(LOG_TAG, "Clicked Play");
//                // If playing, pause
//                if (mMediaPlayer.isPlaying()) {
//                    mMediaPlayer.pause();
//                } else {  // if not playing, play!
//                    // If no extras in play intent, bail out
//                    if (intent.hasExtra(Constants.CURRENT_TRACK_KEY)) {
//                        try {
//                            mMediaPlayer.setDataSource(intent.getStringExtra(Constants.CURRENT_TRACK_KEY));
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        mMediaPlayer.prepareAsync();
//                    }
//                }
//            }
//            // Next Intent
//            else if (intent.getAction().equals(Constants.NEXT_ACTION)) {
//                Log.i(LOG_TAG, "Clicked Next");
//            }
//
//            // Not sure if I'll be calling this
//            else if (intent.getAction().equals(Constants.STOPFOREGROUND_ACTION)) {
//                Log.i(LOG_TAG, "Received Stop Foreground Intent");
//                stopForeground(true);
//                stopSelf();
//            }
//        }
//    }  // end OnHandleIntent()

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize MediaPlayer
        if (mMediaPlayer == null) { initMediaPlayer(); }
    }

    private void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    // Callbacks for async operation
    @Override
    public boolean onError(MediaPlayer player, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_UNKNOWN: {
                Log.d(LOG_TAG, "Media Error unknown; extra: " + extra);
                break;
            }
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED: {
                Log.d(LOG_TAG, "Media Error Server Died; extra: " + extra);
                break;
            }
        }

        // the MediaPlayer has moved to the Error state, must be reset.  Now in idle state
        player.reset();
        return true; // Means error handled
    }

    @Override
    public void onPrepared(MediaPlayer player) {
        player.start();
    }

    @Override
    public void onCompletion(MediaPlayer player) {
        // Go to next song?
    }

    public static boolean isPlaying() {
        return (mMediaPlayer != null && mMediaPlayer.isPlaying());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "In onDestroy");
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mMediaPlayer = null;
        return false;
    }
}
