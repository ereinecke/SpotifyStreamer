package com.ereinecke.spotifystreamer;

import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;


/**
 * PlayerFragment is a container for the MediaPlayer.
 */
public class PlayerFragment extends DialogFragment {

    private static final String LOG_TAG = PlayerFragment.class.getSimpleName();
    private static ImageButton playButton;
    private static Drawable playButtonDrawable;
    private static Drawable pauseButtonDrawable;
    private static ShowTopTracks trackInfo;
    private Intent playIntent;
    private PlayerService mPlayerService;
    private Bitmap albumArt;
    private boolean mBound = false;


    public PlayerFragment() {
    } // setHasOptionsMenu(true);

    @Override
    public void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(getActivity(), PlayerService.class);
//            getActivity().startService(playIntent);
            getActivity().bindService(playIntent, mConnection, Context.BIND_AUTO_CREATE);
        }
        // TODO: Get album art here
//        ImageView trackArt = (ImageView) playerView.findViewById(R.id.album_art_imageview);
//        albumArt = ((BitmapDrawable) trackArt.getDrawable()).getBitmap();
        albumArt = ((BitmapDrawable) getResources().getDrawable(R.mipmap.ic_launcher)).getBitmap();

    }

    @Override
    public void onStop() {
        super.onStop();
        // Unbind from mPlayerService
        if (mBound) {
            getActivity().unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onDestroy() {
        getActivity().stopService(playIntent);
        mPlayerService = null;
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View playerView = inflater.inflate(R.layout.media_player, container, false);

        Bundle trackInfoBundle = TopTracksFragment.getTrackInfo();
        trackInfo = trackInfoBundle.getParcelable(Constants.TRACK_INFO);

        playButton = (ImageButton) playerView.findViewById(R.id.play_button);
        ImageButton prevButton = (ImageButton) playerView.findViewById(R.id.previous_button);
        ImageButton nextButton = (ImageButton) playerView.findViewById(R.id.next_button);
        pauseButtonDrawable = getResources().getDrawable(android.R.drawable.ic_media_pause);
        playButtonDrawable = getResources().getDrawable(android.R.drawable.ic_media_play);

        // Set up button listeners
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevClick();
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playClick();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextClick();
            }
        });

        // Populate layout fields
        setTrackInfo(playerView, trackInfo);

        return playerView;
    }

    // Set current track info in the media player
    private void setTrackInfo (View playerView, ShowTopTracks trackInfo) {
        if (trackInfo != null) {
            Log.d(LOG_TAG, "Track Info: " + trackInfo.toString());

            // Populate layout fields
            TextView textView = (TextView) playerView.findViewById(R.id.artist_name_textview);
            textView.setText(trackInfo.artistName);
            textView = (TextView) playerView.findViewById(R.id.album_name_textview);
            textView.setText(trackInfo.albumName);
            textView = (TextView) playerView.findViewById(R.id.song_title_textview);
            textView.setText(trackInfo.trackName);
            textView = (TextView) playerView.findViewById(R.id.end_time_textview);
            textView.setText(millisToMinutes(trackInfo.trackLength));

            ImageView trackArt = (ImageView) playerView.findViewById(R.id.album_art_imageview);
            int trackArtSize = playerView.getContext().getResources()
                    .getInteger(R.integer.player_track_art_size);
            Picasso.with(playerView.getContext())
                    .load(trackInfo.trackImageUrl)
                    .resize(trackArtSize, trackArtSize)
                    .into(trackArt);
        }
    }


    // Track controls, specified in media_player.xml
    // The approach is to select previous track from adapter and regenerate fragment
    // Wrap to end if at first item
    private void prevClick() {
        // Need to get previous track from adapter?

    }

    private void playClick() {

            // Need to toggle Play and Pause
            if (PlayerService.isPlaying()) {
                // change button to Pause
                playButton.setImageDrawable(playButtonDrawable);
            } else {
                // start playback
                // change button to Play
                playButton.setImageDrawable(pauseButtonDrawable);
            }
        mPlayerService.playTrack();
    }

    // The approach is to select previous track from adapter and regenerate fragment
    // Wrap to end if at last item
    private void nextClick() {
        // need to get next track from adapter?

    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayerService.PlayerBinder playerBinder =
                    (PlayerService.PlayerBinder) service;
            // get service
            mPlayerService = playerBinder.getService();
            mBound = true;

            mPlayerService.newTrack(trackInfo, albumArt);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    // Convert milliseconds to HH:MM:SS string for duration scrubber.  Omit hours if == 0
    private String millisToMinutes(long millis) {
        String minutesString;

        Long hours = TimeUnit.HOURS.convert(millis, TimeUnit.MILLISECONDS);
        Long minutes = TimeUnit.MINUTES.convert(millis, TimeUnit.MILLISECONDS) % 60;
        Long seconds = TimeUnit.SECONDS.convert(millis, TimeUnit.MILLISECONDS) % 60;

        if (hours > 0) minutesString =  String.format("%02d:%02d:%02d", hours, minutes, seconds);
        else minutesString = String.format("%02d:%02d", minutes, seconds);

        return minutesString;
    }

}
