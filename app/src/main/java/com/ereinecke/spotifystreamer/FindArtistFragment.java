package com.ereinecke.spotifystreamer;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;

/**
 * FindArtistFragment handles searching for an artist and displaying all artists that
 * match the search phrase.
 */

public class FindArtistFragment extends Fragment {

    private static final String LOG_TAG = FindArtistFragment.class.getSimpleName();

    private int mPosition = ListView.INVALID_POSITION;
    private ArtistAdapter mArtistAdapter;
    private ListView mListView;
    private SpotifyApi mSpotifyApi = new SpotifyApi();
    private ArtistsPager artistsPager;

    public FindArtistFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Debugging purposes
        // For now, construct URL in here; in future, pass whole string
        String debugArtist = "Gil+Gutierrez";
        String response = null;
        String queryUrl = SpotifyApi.SPOTIFY_WEB_API_ENDPOINT + "?q=" + debugArtist + "&type=artist";
        Log.d(LOG_TAG, "Query URL: " + queryUrl);

        // AsyncTask to execute search
        fetchSpotifyData spotifyData = new fetchSpotifyData();
        spotifyData.execute(queryUrl);

        // Create ArrayAdapter to display dummy artist data
        mArtistAdapter = new ArtistAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Set up focus listener for Search Artist editText
        final EditText artistSearch = (EditText) rootView.findViewById(R.id.search_artist_editText);
        artistSearch.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Log.d(LOG_TAG, "hasFocus true");
                    String artist = artistSearch.getText().toString();
                    Log.d(LOG_TAG, " Artist: " + artist + "len(artist): " + artist.length());

                    if ((artist != null) && (artist.length() != 0)) {

                        artistsPager = mSpotifyApi.getService().searchArtists(artist);
                        // just for debug purposes
                        logArtistsPager(artistsPager);
                    }
                }
                else {
                    Log.d(LOG_TAG, "hasFocus false");
                }
            }
        });

        // Get a reference to the ListView and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.list_artist);
        mListView.setAdapter(mArtistAdapter);

        // Set up listener for clicking on an item in the ListView
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {

                    /*
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    ((Callback) getActivity())
                            .onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                    locationSetting, cursor.getLong(COL_WEATHER_DATE)
                            )); */
                }
                mPosition = position;

                // If there's instance state, mine it for useful information.
                // The end-goal here is that the user never knows that turning their device sideways
                // does crazy lifecycle related things.  It should feel like some stuff stretched out,
                // or magically appeared to take advantage of room, but data or place in the app was never
                // actually *lost*.
                /*
                if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
                    // The listview probably hasn't even been populated yet.  Actually perform the
                    // swapout in onLoadFinished.
                    mPosition = savedInstanceState.getInt(SELECTED_KEY);
                } */

            }
        });
        return rootView;
    }

    public class fetchSpotifyData extends AsyncTask<String, Void, String> {

        private final String LOG_TAG = "FetchSpotifyData";

        @Override
        protected String doInBackground(String... params) {
            String queryUrl = null;
            String response = null;
            if (params.length == 0) {
                    return null;
            }
            else {queryUrl = params[0];}

            // Use direct call to okhttp for starters
/*
            HttpClient test = new HttpClient();
            String response = null;
                try {
                    response = test.run(queryUrl, accessToken);
                    Log.d(LOG_TAG, "Response: " + response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
*/
            // TODO: Now try the spotify wrapper


            return response;
        } // end FetchSpotifyData.doInBackground

    } // end FetchSpotifyData

    private void logArtistsPager(ArtistsPager artistsPager) {
    Log.v(LOG_TAG, "artistsPager:");
    if (artistsPager == null) Log.d(LOG_TAG, "artistsPager is null");
    else {
        for (Artist artist : artistsPager.artists.items) {
            Log.d(LOG_TAG, "  artist id: " + artist.id + "; artist name: " + artist.name);
        }
    }
    }
}
