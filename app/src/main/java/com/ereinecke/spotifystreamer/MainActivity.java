package com.ereinecke.spotifystreamer;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * MainActivity for SpotifyStreamer
 * Handles login to Spotify and launches FindArtistFragment
 */

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static boolean mTwoPane = false;
    private static String countryCode;
    private static String accessToken = null;

    public static String accessToken() {
        return accessToken;
    }

    private static Bitmap placeholderImage;
    private static ServiceFragment mServiceFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(LOG_TAG, "in onCreate()");

        // Expect FindArtistFragment to be statically loaded by activity_main

        // ServiceFragment exists to keep bound to MediaPlayer, should just be started once.
        FragmentManager fm = getSupportFragmentManager();
        mServiceFragment = (ServiceFragment) fm.findFragmentByTag(Constants.SERVICEFRAGMENT_TAG);
        if (mServiceFragment == null) {
            mServiceFragment = new ServiceFragment();
            fm.beginTransaction().add(mServiceFragment, Constants.SERVICEFRAGMENT_TAG)
                    .commit();
            fm.executePendingTransactions();
        }

        // Determine if in two-pane mode by testing existence of top_tracks_container
        mTwoPane = findViewById(R.id.top_tracks_container) != null;

        // Caching this in MainActivity as it comes up quite a bit.
        placeholderImage = ((BitmapDrawable) getResources()
                .getDrawable(R.mipmap.ic_launcher)).getBitmap();

        isPlayerServiceRunning();

    }

    @Override
    public void onRestoreInstanceState(Bundle outState) {
        super.onRestoreInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    /* used to tell if PlayerService is running.  This needs to be implemented in PlayerActivity as
     * well.
     */
    public boolean isPlayerServiceRunning() {

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (service.service.getClassName().equals("PlayerService")) {
                return true;
            }
        }
        return false;
    }

    public static ServiceFragment getServiceFragment() {
        return mServiceFragment;
    }

    /**
     * Returns placeholder image
     */
    public static Bitmap getPlaceholderImage() {
        return placeholderImage;
    }

    /**
     * Returns true if two-pane setup
     */
    public static boolean isTwoPane() {
        return mTwoPane;
    }

    public void setTopTracksPosition(int pos) {
        TopTracksFragment topTracksFragment = (TopTracksFragment) getSupportFragmentManager()
                .findFragmentByTag(Constants.TRACKSFRAGMENT_TAG);

        topTracksFragment.setListPosition(pos);
    }

    /**
     * Get ISO 3166-1 alpha-2 country code for this device using IP address.  This involves
     * calling http://ip-api.com/json and parsing the country code from it.
     * TODO: not yet implemented, currently returns null.
     */
    public static String getUserCountry() {
        if (countryCode == null) {
            // AsyncTask here
            return countryCode;
        } else return null;
    }
}
