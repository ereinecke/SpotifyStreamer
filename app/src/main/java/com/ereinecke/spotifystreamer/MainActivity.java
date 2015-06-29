package com.ereinecke.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;


public class MainActivity extends ActionBarActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_CODE = 1137;
    private static final String CLIENT_ID = "0801969fcfb940d69497cd585393a7d0";
    private static final String REDIRECT_URI = "whatup://callback";

    private static String accessToken = null;

    public static boolean isLoggedIn() {return (accessToken != null);}
    public static String accessToken() {return accessToken;}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new FindArtistFragment())
                    .commit();
        }
        spotifyLogin();
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

    // Log in to Spotify
    private void spotifyLogin() {

        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN,
                        REDIRECT_URI);

        builder.setScopes(new String[]{"streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        Log.d(LOG_TAG, "onActivityResult started");

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            Log.d(LOG_TAG, "Spotify responseType: " + response.getType());
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
}
