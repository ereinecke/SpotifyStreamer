package com.ereinecke.spotifystreamer;

import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
    private Intent playIntent;
    private static String currentTrack;
    private static ImageButton prevButton;
    private static ImageButton playButton;
    private static ImageButton nextButton;
    private static Drawable playButtonDrawable;
    private static Drawable pauseButtonDrawable;
    private static Bundle trackInfoBundle;


    public PlayerFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View playerView = inflater.inflate(R.layout.media_player, container, false);

        // TODO: Get track info via Intent ; have not been able to get it to work
        // Bundle trackInfoBundle = getArguments().getBundle(TopTracksFragment.TRACK_INFO);
        // Bundle trackInfoBundle = getActivity().getIntent().getExtras();
        trackInfoBundle = TopTracksFragment.getTrackInfo();

        // Cache control buttons
        prevButton = (ImageButton) playerView.findViewById(R.id.previous_button);
        playButton = (ImageButton) playerView.findViewById(R.id.play_button);
        nextButton = (ImageButton) playerView.findViewById(R.id.next_button);
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
        setTrackInfo(playerView, trackInfoBundle);

        // set up playIntent
        playIntent = new Intent(getActivity(), PlayerService.class);
        playIntent.putExtras(trackInfoBundle);
        playIntent.setAction(MainActivity.STARTFOREGROUND_ACTION);

        return playerView;
    }

    // Set current track info in the media player
    public void setTrackInfo (View playerView, Bundle trackInfoBundle) {
        if (trackInfoBundle != null) {
            ShowTopTracks trackInfo = trackInfoBundle.getParcelable(TopTracksFragment.TRACK_INFO);
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

                currentTrack = trackInfo.trackMediaUrl;
            }
        }
    }


    // Track controls, specified in media_player.xml
    public void prevClick() {
        // Need to get previous track from adapter?

    }

    public void playClick() {
        getActivity().startService(playIntent);
        // Need to toggle Play and Pause
        if (PlayerService.isPlaying()) {
            // change button to Pause
            playButton.setImageDrawable(pauseButtonDrawable);
        }
        else {
            // start playback
            // change button to Play
            playButton.setImageDrawable(playButtonDrawable);
        }
    }

    public void nextClick() {
        // need to get next track from adapter?

    }

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
