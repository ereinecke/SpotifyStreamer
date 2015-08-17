package com.ereinecke.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

/**
 * PlayerActivity hosts PlayerFragment, which runs a Media Player instance on small screens
 */

public class PlayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        if (savedInstanceState == null) {
//            getFragmentManager().beginTransaction()
//                    .add(R.id.player_container, new PlayerFragment())
//                    .commit();
        }

        // Get the track info from the intent
        // Intent intent = getIntent();
        // Bundle trackInfo = intent.getExtras();
        Bundle trackInfo = TopTracksFragment.getTrackInfo();

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

    /**
     * StartPlayer starts the foreground service PlayerService when requested
     */
    public void startPlayer(String uri) {
        Intent startIntent = new Intent(this, PlayerService.class);
        startIntent.putExtra(Constants.CURRENT_TRACK_KEY, uri);
        startIntent.setAction(Constants.STARTFOREGROUND_ACTION);
        startService(startIntent);
    }

    /**
     * StopPlayer stops the PlayerService
     */
    public void stopPlayer() {
        Intent stopIntent = new Intent(this, PlayerService.class);
        stopIntent.setAction(Constants.STOPFOREGROUND_ACTION);
        startService(stopIntent);

    }
}