package com.ereinecke.spotifystreamer;

/**
 * Contains all constans for SpotifyStreamer.
 */
class Constants {

    // MainActivity
    // Spotify service constants
    public static final int REQUEST_CODE = 1137;
    public static final String CLIENT_ID = "0801969fcfb940d69497cd585393a7d0";
    public static final String REDIRECT_URI = "whatup://callback";
    public static final String FINDARTISTFRAGMENT_TAG = "FAFTAG";
    public static final String TRACKSFRAGMENT_TAG = "DFTAG";
    public static final String PLAYERFRAGMENT_TAG = "PLYFTAG";
    public static final String SERVICEFRAGMENT_TAG = "SFTAG";

    // Service actions
    public static final String MAIN_ACTION = "com.ereinecke.spotifystreamer.main";
    public static final String PREV_ACTION = "com.ereinecke.spotifystreamer.prev";
    public static final String PLAY_ACTION = "com.ereinecke.spotifystreamer.play";
    public static final String NEXT_ACTION = "com.ereinecke.spotifystreamer.next";
    public static final int NOTIFICATION_ID = 111;

    // FindArtistsFragment
    // key for persisting retrieved artists
    public static final String ARTISTS_LIST_POSITION = "ListPosition";

    // ShowArtist
    public static final String NO_IMAGE = "No_Image_Found";

    // TopTracksFragment
    // Keys
    public static final String CURRENT_TRACK_KEY = "CurrentTrack";
    public static final String ARTIST_ARRAY = "ArtistArray";
    public static final String TRACK_INFO = "TrackInfo";
    public static final String TOP_TRACKS_ARRAY = "TopTracksArray";
    public static final String TOP_TRACKS_POSITION = "TopTracksPosition";
    public static final String COUNTRY_CODE = "MX"; // fallback country code
    public static final String LIST_POSITION_KEY = "ListPositionKey";
    public static final String NEW_TRACK = "NewTrack";
    public static final int USE_CURRENT = -1;

    // PlayerActivity
    public static final String SERVICE_BOUND = "ServiceBound";
    public static final int SCRUBBER_INTERVAL = 300; // 30 seconds / 100

}
