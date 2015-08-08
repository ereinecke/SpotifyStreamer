package com.ereinecke.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

/**
 * PlayerActivity hosts PlayerFragment, which runs a Media Player instance on small screens
 */

public class PlayerActivity extends AppCompatActivity {

    public interface MediaPlayerListener {
        void onPreviousClick(PlayerFragment player);
        void onNextClick(PlayerFragment player);
        void onPlayClick(PlayerFragment player);
        void onPauseClick(PlayerFragment player);
        void onScrubber(PlayerFragment player);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.player_container, new PlayerFragment())
                    .commit();
        }

        // Get the track info from the intent
        // Intent intent = getIntent();
        // Bundle trackInfo = intent.getExtras();
        Bundle trackInfo = TopTracksFragment.getTrackInfo();


        // Faking it for now
        trackInfo.putString(TopTracksFragment.ARTIST_NAME_KEY, "Deja Vu");
        trackInfo.putString(TopTracksFragment.ALBUM_NAME_KEY, "Crosby, Stills, Nash & Young");
        trackInfo.putString(TopTracksFragment.TRACK_NAME_KEY, "Carry On");
        trackInfo.putString(TopTracksFragment.TRACK_URL_KEY, "https://open.spotify.com/track/4bjvLvKovcWqZwDbXT5QQX");
        trackInfo.putString(TopTracksFragment.IMAGE_URL_KEY, "https://i.scdn.co/image/1287b3b9201fc5ed8c51101dbeb0326a450671ec");
        trackInfo.putLong(TopTracksFragment.DURATION_KEY, 265933L);

        // Pass trackInfo to fragment and launch it
        PlayerFragment player = new PlayerFragment();
        player.setArguments(trackInfo);
         getFragmentManager().beginTransaction()
                .add(R.id.player_container, player)
                .commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}