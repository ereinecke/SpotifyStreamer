package com.ereinecke.spotifystreamer;

import android.content.Intent;
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
    public static final String  ARTIST_ARRAY = "ArtistArray"; // key for persisting retrieved artists
    public static final String  ARTIST_NAME      = "ArtistName";     // key for intent extra
    public static final String  ARTIST_ID        = "ArtistId";       // key for intent extra

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

        View rootView = inflater.inflate(R.layout.fragment_artists, container, false);

        // Set up action listener for Search Artist editText
        final EditText artistSearch = (EditText) rootView.findViewById(R.id.search_artist_editText);

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

                mPosition = position;

                // Launch the TopTracksActivity
                Intent intent = new Intent(getActivity(), TopTracksActivity.class);
                intent.putExtra(ARTIST_NAME, artistArray.get(mPosition).artistName);
                intent.putExtra(ARTIST_ID, artistArray.get(mPosition).artistId);
                startActivity(intent);
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
            if (artistsPager.artists.items.size() == 0) {
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
