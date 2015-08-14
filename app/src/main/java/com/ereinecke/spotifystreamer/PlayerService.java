package com.ereinecke.spotifystreamer;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Media Player foreground service
 * Significant contribution from this tutorial:
 *      http://truiton.com/2014/10/android-foreground-service-example
 *
 *      TODO: Handle audio focus
 */

public class PlayerService extends IntentService implements MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener {

    private static final String LOG_TAG = PlayerService.class.getSimpleName();
    private static MediaPlayer mMediaPlayer;
    private static String currentTrack;

    public PlayerService() {
        super("PlayerService");

    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnErrorListener(this);
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Bitmap selectedAlbumArt;

        String action = intent.getAction();
        if (action.equals(MainActivity.MAIN_ACTION)) {

            // Start
            Log.d(LOG_TAG, "Received Start Foreground Intent ");
            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.setAction(MainActivity.MAIN_ACTION);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

            // Previous
            Intent previousIntent = new Intent(this, PlayerService.class);
            previousIntent.setAction(MainActivity.PREV_ACTION);
            PendingIntent pPreviousIntent = PendingIntent.getService(this, 0, previousIntent, 0);

            // Play
            Intent playIntent = new Intent(this, PlayerService.class);
            playIntent.setAction(MainActivity.PLAY_ACTION);
            PendingIntent pPlayIntent = PendingIntent.getService(this, 0, playIntent, 0);

            // Next
            Intent nextIntent = new Intent(this, PlayerService.class);
            nextIntent.setAction(MainActivity.NEXT_ACTION);
            PendingIntent pNextIntent = PendingIntent.getService(this, 0, nextIntent, 0);

            // Get current track info
            ShowTopTracks currentTrack = TopTracksFragment.getTrackInfo()
                    .getParcelable(TopTracksFragment.TRACK_INFO);
            // Getting album art for notification ... too much overhead?
            // Need an ImageView for Picasso
            if (currentTrack != null) {
                ImageView albumArtImageView = new ImageView(this);
                Picasso.with(getApplicationContext())
                        .load(currentTrack.trackImageUrl)
                        .resize(128, 128)
                        .into(albumArtImageView);
                selectedAlbumArt = ((BitmapDrawable) albumArtImageView.getDrawable()).getBitmap();
            } else {
                // TODO: Figure out what to do with this error condition
                selectedAlbumArt = ((BitmapDrawable) getResources()
                        .getDrawable(R.mipmap.ic_launcher)).getBitmap();
            }

            // Set up notification
            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle(currentTrack.artistName)
                    .setTicker(currentTrack.trackName)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(Bitmap.createScaledBitmap(selectedAlbumArt, 128, 128, false))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .addAction(android.R.drawable.ic_media_previous,
                            getResources().getString(R.string.previous), pPreviousIntent)
                    .addAction(android.R.drawable.ic_media_play,
                            getResources().getString(R.string.play), pPlayIntent)
                    .addAction(android.R.drawable.ic_media_next,
                            getResources().getString(R.string.next), pNextIntent)
                    .build();

            startForeground(MainActivity.NOTIFICATION_ID, notification);

        }
        // Previous Intent
        else if (intent.getAction().equals(MainActivity.PREV_ACTION)) {
            Log.i(LOG_TAG, "Clicked Previous");
        }
        // Play Intent
        else if (intent.getAction().equals(MainActivity.PLAY_ACTION)) {
            Log.i(LOG_TAG, "Clicked Play");
            Uri uri = Uri.parse(intent.getStringExtra(MainActivity.CURRENT_TRACK_KEY));
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.create(this, uri);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.prepareAsync();

        }
        // Next Intent
        else if (intent.getAction().equals(MainActivity.NEXT_ACTION)) {
            Log.i(LOG_TAG, "Clicked Next");
        }

        // This only takes service out of foreground mode - still need to stop with stopSelf().
        else if (intent.getAction().equals(MainActivity.STOPFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // not sure what to put in here

        // the MediaPlayer has moved to the Error state, must be reset
        mp.reset();

        return true; // TODO: placeholder return value
    }

    public void onPrepared(MediaPlayer player) {
        player.start();
    }

    public static boolean isPlaying() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.isPlaying();
        }
        else { return false; }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "In onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Used only in case of bound services.
        return null;
    }
}
