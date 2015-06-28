package com.ereinecke.spotifystreamer;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;

/**
 * FindArtistFragment handles searching for an artist and displaying all artists that
 * match the search phrase.
 */

public class FindArtistFragment extends Fragment {

    private static final String LOG_TAG = FindArtistFragment.class.getSimpleName();

    private int mPosition = ListView.INVALID_POSITION;
    private ArrayList<ArtistList> artistArray = new ArrayList<>();
    private ArtistAdapter mArtistAdapter;
    private ListView mListView;
    private SpotifyApi mSpotifyApi = new SpotifyApi();
    // private ArtistsPager artistsPager;

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

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Set up action listener for Search Artist editText
        final EditText artistSearch = (EditText) rootView.findViewById(R.id.search_artist_editText);
        artistSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String artist = artistSearch.getText().toString();
                    Log.d(LOG_TAG, " Artist: " + artist);
                    searchSpotifyArtists spotifyData = new searchSpotifyArtists();
                    spotifyData.execute(artist);

                    handled = true;
                }
                return handled;
            }
        });

        // Get a reference to the ListView and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.list_artist);

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

    // TODO: Need to do something here to restore ListView on rotate?
    @Override
    public void onResume() {
    }


    public class searchSpotifyArtists extends AsyncTask<String, Void, ArtistsPager> {

        private final String LOG_TAG = searchSpotifyArtists.class.getSimpleName();

        @Override
        protected ArtistsPager doInBackground(String... params) {
            String artist = null;
            String response = null;
            if (params.length == 0) {
                return null;
            }
            else {artist = params[0];}

            mSpotifyApi.setAccessToken(MainActivity.accessToken());

            SpotifyService spotify = mSpotifyApi.getService();
            ArtistsPager artistsPager = spotify.searchArtists(artist);
            Log.d(LOG_TAG, artistsPager.toString());

            return artistsPager;
        } // end searchSpotifyData.doInBackground

        @Override
        protected void onPostExecute(ArtistsPager artistsPager) {
            if (artistsPager == null) {
                Log.d(LOG_TAG, "artistsPager is null");
                return;
            }

            // Populate ListArray here
            artistArray.clear();
            for (Artist artist : artistsPager.artists.items) {
                String url;
                if (artist.images.size() > 0) {
                    url = artist.images.get(1).url;
                }
                else {
                    url = ArtistList.NO_IMAGE;
                }
                ArtistList artistList = new ArtistList(artist.name, artist.id, url);
                final boolean added = artistArray.add(artistList);
                Log.d(LOG_TAG, " Artist List: " + artistList.toString());
            }
            // Create ArrayAdapter to display dummy artist data
            mArtistAdapter = new ArtistAdapter(getActivity(), artistArray);
            mListView.setAdapter(mArtistAdapter);
        }
    } // end searchSpotifyData
}
