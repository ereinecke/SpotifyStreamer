package com.ereinecke.spotifystreamer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

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
    private static DebugMediaPlayer mMediaPlayer;
    // private static MediaPlayer mMediaPlayer;
    private static boolean playing = false;
    private static boolean trackReady = false;
    private final IBinder mBinder = new PlayerBinder();
    private ArrayList<ShowTopTracks> topTracksArrayList;
    private int mPosition;
    private PendingIntent pendingIntent;
    private PendingIntent pPlayIntent;
    private PendingIntent pPreviousIntent;
    private PendingIntent pNextIntent;
    private ShowTopTracks currentTrack;
    private Notification notification;

    public PlayerService() {}

    public class PlayerBinder extends Binder {
        PlayerService getService() {
            return PlayerService.this;
        }
    }

    // Prepares notification PendingIntents. gets albumTrackArt sets up notification
    // and calls prepareAsync().
    public void initTrack() {

        Log.d(LOG_TAG, "Setting up new track, mPosition: " + mPosition);
        logMediaPlayerState();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
        pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        // Previous
        Intent previousIntent = new Intent(this, PlayerService.class);
        previousIntent.setAction(Constants.PREV_ACTION);
        pPreviousIntent = PendingIntent.getService(this, 0, previousIntent, 0);

        // Play
        Intent playIntent = new Intent(this, PlayerService.class);
        playIntent.setAction(Constants.PLAY_ACTION);
        pPlayIntent = PendingIntent.getService(this, 0, playIntent, 0);

        // Next
        Intent nextIntent = new Intent(this, PlayerService.class);
        nextIntent.setAction(Constants.NEXT_ACTION);
        pNextIntent = PendingIntent.getService(this, 0, nextIntent, 0);

        // Pull down album art for track on background thread
        new getAlbumTrackArt().execute(currentTrack.trackImageUrl);
    }

    // Pull down album art on background thread.  Start foreground service onPostExecute()
    public class getAlbumTrackArt extends AsyncTask<String, Void, Bitmap> {

        private final String LOG_TAG = getAlbumTrackArt.class.getSimpleName();

        @Override
        protected Bitmap doInBackground(String... params) {
            String trackImageUrl = params[0];
            Bitmap trackImageArt;

            try {
                trackImageArt = Picasso.with(getApplicationContext()).load(trackImageUrl)
                        .resize(128, 128).get();
            } catch (IOException e) {
                e.printStackTrace();
                trackImageArt = MainActivity.getPlaceholderImage();
            }
            Log.d (LOG_TAG, "got trackImageArt");
            return trackImageArt;
        } // end getAlbumTrackArt.doInBackground

        @Override
        protected void onPostExecute(Bitmap trackAlbumArt) {

            setNotification(trackAlbumArt);
            startForegroundService();
            mMediaPlayer.prepareAsync();
        }
    }

    // Called after getAlbumTrackArt completes, sets up notification
    private void setNotification(Bitmap trackAlbumArt) {

        if (currentTrack != null) {
            Log.d(LOG_TAG, "Setting notification");
            // Set up notification
            notification = new NotificationCompat.Builder(this)
                    .setContentTitle(currentTrack.trackName)
                    .setTicker(currentTrack.trackName)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(trackAlbumArt)
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .addAction(android.R.drawable.ic_media_previous,
                            getResources().getString(R.string.previous), pPreviousIntent)
                    .addAction(android.R.drawable.ic_media_play,
                            getResources().getString(R.string.play), pPlayIntent)
                    .addAction(android.R.drawable.ic_media_next,
                            getResources().getString(R.string.next), pNextIntent)
                    .build();
        }
    }

    // Play from beginning
    public void startTrack() {
        Log.d(LOG_TAG, "in startTrack()");
        logMediaPlayerState();
        currentTrack = topTracksArrayList.get(mPosition);
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.reset();
            stopForegroundService();
            initTrack();        // sets up notification, starts foreground service asynchronously
            PlayerFragment.onStartTrack();  // Starts buffering progress spinner
//            playing = true;  // will be true even if isPlaying() is false until initTrack completes
        } else {
            Log.d(LOG_TAG, "Attempted to startTrack() when already playing");
        }
    }

    // Resume play from pause
    public void playTrack() {
        Log.d(LOG_TAG, "in playTrack()");
        logMediaPlayerState();
        if (!mMediaPlayer.isPlaying()) {
            playing = true;
            // PlayerFragment.onStartPlay();
            mMediaPlayer.start();
        } else {
            Log.d(LOG_TAG, "Attempted to playTrack() when already playing");
        }
    }

    public void pauseTrack() {
        // If playing, pause
        Log.d(LOG_TAG, "in pauseTrack()");
        logMediaPlayerState();
        if (mMediaPlayer.isPlaying()) {
            playing = false;
            mMediaPlayer.pause();
        } else {
            Log.d(LOG_TAG, "Attempted to pauseTrack() when not playing");
        }
    }

    public void prevTrack() {
        Log.d(LOG_TAG, "in prevTrack()");
        if (mPosition > 0) {  // decrement
            mPosition -= 1;
        } else {              // set to last
            mPosition = topTracksArrayList.size() - 1;
        }
        mMediaPlayer.stop();
        trackReady = false;
        playing = false;
        currentTrack = topTracksArrayList.get(mPosition);
        setTopTracksPosition(mPosition);
        startTrack();
    }

    public void nextTrack() {
        Log.d(LOG_TAG, "in nextTrack()");
        if (mPosition < topTracksArrayList.size() - 1) {     // increment
            mPosition += 1;
        } else {                // set to first
            mPosition = 0;
        }
        mMediaPlayer.stop();
        trackReady = false;
        playing = false;
        currentTrack = topTracksArrayList.get(mPosition);
        setTopTracksPosition(mPosition);
        startTrack();
    }

    // Moves MediaPlayer to Initialized state by starting foreground service and setting data source
    public void startForegroundService() {
        try {
            startForeground(Constants.NOTIFICATION_ID, notification);
        } catch (Exception e) {
            Log.d(LOG_TAG,"Error trying to start Foreground service: probably already running.");
            e.printStackTrace();
        }

        try {
            mMediaPlayer.setDataSource(currentTrack.trackMediaUrl);
            Log.d(LOG_TAG, "setDatasource to " + currentTrack.trackMediaUrl);
        } catch (IOException e) {
            Log.d(LOG_TAG, "Can't setDataSource to " + currentTrack.trackMediaUrl);
            e.printStackTrace();
        }

    }

    public void stopForegroundService() {
        Log.i(LOG_TAG, "Stopping foreground service");
        trackReady = false;
        stopForeground(true);
        stopSelf();
    }

    private void initMediaPlayer() {
        // mMediaPlayer = new MediaPlayer();
        mMediaPlayer = new DebugMediaPlayer();
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        logMediaPlayerState();
    }

     public void setTrackList(ArrayList<ShowTopTracks> topTracksArrayList, int position) {
        mPosition = position;
        this.topTracksArrayList = topTracksArrayList;
        currentTrack = topTracksArrayList.get(mPosition);
    }

    public static int getSeek() {
        if (mMediaPlayer != null ) {
            return mMediaPlayer.getCurrentPosition();
        } else return -1;
    }

    public void setSeek(int newPositionMillis) {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            Log.d("SetSeek: ", "Current Position: " + mMediaPlayer.getCurrentPosition());
            Log.d(LOG_TAG, "Setting position to: " + newPositionMillis);
            mMediaPlayer.seekTo(newPositionMillis);
            Log.d("SetSeek: ", "New Position: " + mMediaPlayer.getCurrentPosition());
            mMediaPlayer.start();
        }
    }

    // Callbacks for async operation
    @Override
    public boolean onError(MediaPlayer player, int what, int extra) {
        trackReady = false;
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
        startTrack();   // ???
        return true; // Means error handled
    }

    @Override
    public void onPrepared(MediaPlayer player) {
        Log.d(LOG_TAG, "onPrepared called: starting player");
        trackReady = true;
        PlayerFragment.onStartPlay();
        setSeek(0);
        player.start();
    }

    @Override
    public void onCompletion(MediaPlayer player) {
        trackReady = false;
        nextTrack();
    }

    // Tells MainActivity to change TopTracks selection position
    private  void setTopTracksPosition(int position) {
        Intent positionIntent = new Intent();
        positionIntent.putExtra(Constants.CURRENT_TRACK_KEY, position);
        sendBroadcast(positionIntent);
    }

    private void logMediaPlayerState() {
        if (mMediaPlayer == null) Log.d(LOG_TAG, "MediaPlayer null");
        else {
            Log.d(LOG_TAG, "Media player isPlaying(): " + isPlaying() + "; " +
                    "playing: " + playing + "\n");
            if (isPlaying() != playing) {
                Log.d(LOG_TAG, "isPlaying() and playing are not in agreement.");
            }
        }
    }

    public static boolean isPlaying() {
        return (mMediaPlayer != null && mMediaPlayer.isPlaying());
    }

    // Assumption that if trackReady = true and isPlaying() is false, must be paused.
    public boolean isPaused() {
        Log.d(LOG_TAG, "isPaused(): " + (trackReady && !isPlaying()));
        return (trackReady && !isPlaying());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "in onCreate()");

        // Initialize MediaPlayer
        if (mMediaPlayer == null) { initMediaPlayer(); }
    }

    @Override
    public void onDestroy() {
        Log.i(LOG_TAG, "In onDestroy");
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG, "in onUnbind()");
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mMediaPlayer = null;
        return false;
    }
}
