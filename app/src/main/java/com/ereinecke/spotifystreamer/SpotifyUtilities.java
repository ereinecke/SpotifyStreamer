package com.ereinecke.spotifystreamer;

/**
 * Class using Spotify Wrapper
 * Created by ereinecke on 6/23/15.
 *
 */
public class SpotifyUtilities {

    private static final String LOG_TAG = SpotifyUtilities.class.getSimpleName();
    private static final String SPOTIFY_QUERY =
            "https://api.spotify.com/v1/search?q=";
    private static final String TYPE_ARTIST = "&type=artist";
    private static final int REQUEST_CODE = 1137;
    private static final String CLIENT_ID = "0801969fcfb940d69497cd585393a7d0";
    private static final String REDIRECT_URI = "whatup://callback";

    private boolean loggedIn = false;

    public boolean isLoggedIn() {
        return true;
    }

/*  TODO: have this in FindArtistFragment, but I think it should be factored out into a separate file
    // Log in to Spotify
    public boolean spotifyLogin() {

        if (!loggedIn) {  // Need a more direct way to determine if already logged in
            AuthenticationRequest.Builder builder =
                    new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN,
                            REDIRECT_URI);

            builder.setScopes(new String[]{"streaming"});
            AuthenticationRequest request = builder.build();

            AuthenticationClient.openLoginActivity(FindArtistFragment.this.getActivity(),
                    REQUEST_CODE, request);
        }
        // TODO: how to determine login success?
        return true;  // temporary
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    loggedIn = true;
                    break;
                case ERROR:
                    loggedIn = false;
                    break;
                default:
                    loggedIn = false;
            }
        }
    }
    */
}