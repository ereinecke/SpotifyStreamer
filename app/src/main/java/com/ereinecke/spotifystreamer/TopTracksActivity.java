package com.ereinecke.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Displays Top 10 tracks for artist selected in FindArtistFragment
 * Adds artist name to ActionBar as a subtitle.
 */

public class TopTracksActivity extends AppCompatActivity  {

    private static final String LOG_TAG = TopTracksActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String artistName = "";
        String artistId;
        Bundle extras;

        setContentView(R.layout.activity_toptracks);

        if (savedInstanceState == null) {
            extras = getIntent().getExtras();
            if (extras != null) {
                artistId = extras.getString(getString(R.string.key_artist_id));
                artistName = extras.getString(getString(R.string.key_artist_name));
                Log.d(LOG_TAG, "Artist ID: " + artistId);
            } else {
                Log.d(LOG_TAG, "intent is null");
            }
        } else {
            Log.d(LOG_TAG, "Restoring from savedInstanceState (?)");
            extras = savedInstanceState.getBundle(Constants.TRACK_INFO);
            artistId = savedInstanceState.getString(getString(R.string.key_artist_id));
            artistName = savedInstanceState.getString(getString(R.string.key_artist_name));
        }

        // Put artist name in the action bar subtitle
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setTitle(getString(R.string.top_tracks_label));
            actionbar.setSubtitle(artistName);
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Log.d(LOG_TAG, "adding top_tracks_container");
        fragmentTransaction.add(R.id.top_tracks_container, new TopTracksFragment())
                .commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
