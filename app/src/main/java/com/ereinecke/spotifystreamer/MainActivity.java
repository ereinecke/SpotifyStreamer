package com.ereinecke.spotifystreamer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

/**
 * MainActivity for SpotifyStreamer
 * Handles login to Spotify and launches FindArtistFragment
 */

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static boolean mTwoPane = false;
    private static String countryCode;
    private static String accessToken = null;
    public static  String accessToken() {return accessToken;}
    private static Bitmap placeholderImage;
    private boolean sfmBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(LOG_TAG, "in onCreate()");

        // Expect FindArtistFragment to be statically loaded by activity_main

        // Determine if in two-pane mode by testing existence of top_tracks_container
        if (findViewById(R.id.top_tracks_container) != null) {
            mTwoPane = true;
        } else {
            mTwoPane = false;
        }

        // Caching this in MainActivity as it comes up quite a bit.
        placeholderImage = ((BitmapDrawable) getResources()
                .getDrawable(R.mipmap.ic_launcher)).getBitmap();

        // Implemented Spotify login, but am not calling it at this point.
        // spotifyLogin();
    }


    @Override
    public void onRestoreInstanceState (Bundle outState) {
        super.onRestoreInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "in MainActivity.onDestroy()");
        super.onDestroy();
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // TODO: create SettingsActivity.java
            // startActivity(new Intent(this, SettingsActivity.class));
            // Pop a toast as an interim measure
            Toast.makeText(getApplicationContext(), "Settings not yet implemented",
                           Toast.LENGTH_SHORT)
                 .show();
            return true;
        }

        if (id == R.id.action_logout) {
            Log.d(LOG_TAG, "Logging out!");
            AuthenticationClient.logout(this);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Handles login to Spotify via WebView
     */
    private void spotifyLogin() {

        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(Constants.CLIENT_ID,
                        AuthenticationResponse.Type.TOKEN, Constants.REDIRECT_URI);

        builder.setScopes(new String[]{"streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, Constants.REQUEST_CODE, request);
    }

    /**
     * Handles the result of the spotifyLogin activity
     * @param requestCode - identifies the correct activity result
     * @param resultCode -
     * @param intent - intent that launched spotify login
     *
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == Constants.REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    accessToken = response.getAccessToken();
                    Log.d(LOG_TAG, "Spotify accessToken: " + accessToken);
                    break;
                case ERROR:
                    Log.d(LOG_TAG, "Error obtaining access token: " + response.getError());
                    accessToken = null;
                    break;
                default:
                    Log.d(LOG_TAG, "Unexpected Spotify responseType(): " + response.getType());
                    accessToken = null;
            }
        }
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

    // TODO: Not sure this is used
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
