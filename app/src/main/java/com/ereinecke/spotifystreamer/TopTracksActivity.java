package com.ereinecke.spotifystreamer;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Displays Top 10 tracks for artist selected in FindArtistFragment
 * Adds artist name to ActionBar as a subtitle.
 */

public class TopTracksActivity extends AppCompatActivity
        implements PlayerActivity.MediaPlayerListener {

    private static final String LOG_TAG = TopTracksActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new TopTracksFragment())
                    .commit();
        }
        String artistName = "";
        String artistId;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            artistId = extras.getString(getString(R.string.key_artist_id));
            artistName = extras.getString(getString(R.string.key_artist_name));
            Log.d(LOG_TAG, "Artist ID: " + artistId);
        }
        else {Log.d(LOG_TAG, "intent is null");}

        // Put artist name in the action bar subtitle
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setTitle(getString(R.string.top_tracks_label));
            actionbar.setSubtitle(artistName);
        }
        else {Log.d(LOG_TAG,"action bar null");}
     }

    // Player button listeners
    public void onPreviousClick(PlayerFragment player) {

    }

    public void onNextClick(PlayerFragment player) {

    }
    public void onPlayClick(PlayerFragment player) {

    }

    public void onPauseClick(PlayerFragment player) {

    }

    public void onScrubber(PlayerFragment player) {
        
    }

}