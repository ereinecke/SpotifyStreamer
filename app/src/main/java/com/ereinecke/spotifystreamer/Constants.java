package com.ereinecke.spotifystreamer;

/**
 * Contains all constans for SpotifyStreamer.
 */
class Constants {

    // MainActivity
    // Spotify service constants
    public static final int    REQUEST_CODE = 1137;
    public static final String CLIENT_ID = "0801969fcfb940d69497cd585393a7d0";
    public static final String REDIRECT_URI = "whatup://callback";

    // Service actions
    public static final String MAIN_ACTION = "com.ereinecke.spotifystreamer.action.main";
    public static final String PREV_ACTION = "com.ereinecke.spotifystreamer.action.prev";
    public static final String PLAY_ACTION = "com.ereinecke.spotifystreamer.action.play";
    public static final String NEXT_ACTION = "com.ereinecke.spotifystreamer.action.next";
    public static final String STARTFOREGROUND_ACTION = "com.ereinecke.spotifystreamer.action.startforeground";
    public static final String STOPFOREGROUND_ACTION = "com.ereinecke.spotifystreamer.action.stopforeground";
    public static final int    NOTIFICATION_ID = 111;

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

    // PlayerActivity
    public static final String SERVICE_BOUND = "ServiceBound";
    public static final int     SCRUBBER_INTERVAL = 300; // 30 seconds / 100

}
