package com.ereinecke.spotifystreamer;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

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
    public static final String ARTIST_ARRAY = "ArtistArray"; // key for persisting retrieved artists

    private int mPosition = ListView.INVALID_POSITION;

    private ArrayList<ShowArtist> artistArray = new ArrayList<>();
    private ArtistAdapter mArtistAdapter;
    private ListView mListView;
    private SpotifyApi mSpotifyApi = new SpotifyApi();

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

/*          // Issues with this watcher: went to multiline input field in landscape mode,
            // couldn't get keyboard to close up on 'Search'
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
*/

        // Set up action listener for Search Artist editText
        artistSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                boolean handled = false;
                    String artist = artistSearch.getText().toString();
                    Log.d(LOG_TAG, " Artist: " + artist);
                    searchSpotifyArtists spotifyData = new searchSpotifyArtists();
                    spotifyData.execute(artist);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        });

        // Get a reference to the ListView and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.list_artist);

        if (savedInstanceState != null) {
            artistArray = savedInstanceState.getParcelableArrayList(ARTIST_ARRAY);

            // Create ArrayAdapter using persisted artist data
            if (artistArray != null) {
                mArtistAdapter = new ArtistAdapter(getActivity(), artistArray);
                mListView.setAdapter(mArtistAdapter);
            }
        }


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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(ARTIST_ARRAY, artistArray);
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
                Toast.makeText(getActivity(), "No results found.",
                        Toast.LENGTH_SHORT).show();
                Log.d(LOG_TAG, "artistsPager is null");
                return;
            }

            // Populate ListArray here
            artistArray.clear();
            for (Artist artist : artistsPager.artists.items) {
                String url;
                if (artist.images.size() > 0) {

                    // select random image just for fun
                    url = artist.images.get(new Random().nextInt(artist.images.size())).url;
                }
                else {
                    url = ShowArtist.NO_IMAGE;
                }
                ShowArtist showArtist = new ShowArtist(artist.name, artist.id, url);
                final boolean added = artistArray.add(showArtist);
                Log.d(LOG_TAG, " Artist List: " + showArtist.toString());
            }
            // Create ArrayAdapter artist data
            mArtistAdapter = new ArtistAdapter(getActivity(), artistArray);
            mListView.setAdapter(mArtistAdapter);
        } // end searchSpotifyData.onPostExecute
    } // end searchSpotifyData
}
