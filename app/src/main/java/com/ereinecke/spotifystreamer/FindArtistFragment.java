package com.ereinecke.spotifystreamer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;

/**
 * FindArtistFragment handles searching for an artist and displaying all artists that
 * match the search phrase.
 */

public class FindArtistFragment extends Fragment {

    private static final String LOG_TAG = FindArtistFragment.class.getSimpleName();

    private int mArtistListPosition = ListView.INVALID_POSITION;

    private ArrayList<ShowArtist> artistArray = new ArrayList<>();
    private String artist;
    private ArtistAdapter mArtistAdapter;
    private ListView mListView;
    private Toast toast;
    private final SpotifyApi mSpotifyApi = new SpotifyApi();
    boolean newView;

    public FindArtistFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        setHasOptionsMenu(true);
        newView = true; // flag to prevent Spotify search on fragment recreation
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Restoring after config change
        if (savedInstanceState != null) {
            Log.d(LOG_TAG, "restoring from savedInstanceState");
            mArtistListPosition = savedInstanceState.getInt(Constants.ARTISTS_LIST_POSITION);
            artistArray = savedInstanceState.getParcelableArrayList((Constants.ARTIST_ARRAY));
            artist = savedInstanceState.getString(getString(R.string.key_artist_name));
            Log.d(LOG_TAG, "Extras include: ArtistName: " + artist + "; ArtistListPosition: " +
                    mArtistListPosition);
        }

        View rootView = inflater.inflate(R.layout.fragment_artists, container, false);

        // Get a reference to the ListView and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.list_artist);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        // Create ArrayAdapter using persisted artist data
        if (artistArray != null  && artistArray.size() > 0) {
            mArtistAdapter = new ArtistAdapter(getActivity(), artistArray);
            mListView.setAdapter(mArtistAdapter);
            if (mArtistListPosition != ListView.INVALID_POSITION) {
                Log.d(LOG_TAG,"setting artist array position to " + mArtistListPosition);

                // Whole lotta stuff that probably doesn't need to be here
                mListView.performItemClick(mListView.getChildAt(mArtistListPosition),
                        mArtistListPosition,
                        mListView.getAdapter().getItemId(mArtistListPosition));

                mListView.setSelection(mArtistListPosition);
                mListView.setItemChecked(mArtistListPosition, true);
                mListView.smoothScrollToPosition(mArtistListPosition);
                Log.d(LOG_TAG, "artist array position: " + mListView.getCheckedItemPosition());
                Log.d(LOG_TAG, "choice mode is " + mListView.getChoiceMode());
                mListView.invalidateViews();  // force a redraw
            }
        }

        // Set up action listener for Search Artist editText
        final EditText artistSearch = (EditText) rootView.findViewById(R.id.search_artist_editText);
        artistSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                artist = artistSearch.getText().toString();
                Log.d(LOG_TAG, " in afterTextChanged(), Artist: " + artist);
                // don't want to run if we have a good array that matches text
                Log.d(LOG_TAG, " artistArray size: " + artistArray.size());
                if (artistArray.size() == 0) {
                        Log.d(LOG_TAG, "Searching Spotify...");
                        searchSpotifyArtists spotifyData = new searchSpotifyArtists();
                        spotifyData.execute(artist);
                }
                // if artist in search field is different from in the list, search Spotify
                // this should keep us from searching after a rotation
                else if (!artist.equals(artistArray.get(0).artistName)) {
                    Log.d(LOG_TAG, "Searching Spotify...");
                    searchSpotifyArtists spotifyData = new searchSpotifyArtists();
                    spotifyData.execute(artist);
                    newView = false;
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });  // end artistSearch.addTextChangedListener

        // Set up listener for clicking on an item in the ListView
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                mArtistListPosition = position;

                if (MainActivity.isTwoPane()) { // if TwoPane, start TopTracksFragment
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    Bundle extras = new Bundle();
                    extras.putString(getString(R.string.key_artist_name),
                            artistArray.get(mArtistListPosition).artistName);
                    extras.putString(getString(R.string.key_artist_id),
                            artistArray.get(mArtistListPosition).artistId);

                    TopTracksFragment topTracksFragment = new TopTracksFragment();
                    topTracksFragment.setArguments(extras);

                    Log.d(LOG_TAG, "replacing top_tracks_container");
                    fragmentTransaction.replace(R.id.top_tracks_container, topTracksFragment,
                            Constants.TRACKSFRAGMENT_TAG);
                    fragmentTransaction.addToBackStack(Constants.TRACKSFRAGMENT_TAG);
                    fragmentTransaction.commit();
                } else {        // if not TwoPane, start TopTracksActivity
                    Intent intent = new Intent(getActivity(), TopTracksActivity.class);
                    intent.putExtra(getString(R.string.key_artist_name), artistArray.get(mArtistListPosition).artistName);
                    intent.putExtra(getString(R.string.key_artist_id), artistArray.get(mArtistListPosition).artistId);
                    startActivity(intent);
                }
            }
        });  // end mListView.setOnItemClickListener()

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        newView = false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(LOG_TAG, "in onSaveInstanceState: ArtistName: " + artist + "; ArtistListPosition :" +
                mArtistListPosition);
        outState.putInt(Constants.ARTISTS_LIST_POSITION, mArtistListPosition);
        outState.putParcelableArrayList(Constants.ARTIST_ARRAY, artistArray);
        outState.putString(getString(R.string.key_artist_name), artist);
    }

    /**
     * searchSpotifyArtists is an AsyncTask called after every change in the Search EditText.
     * Artists meeting the search criteria passed to onPostExecute to populate the ListArray
     * artistsArray
     */
    public class searchSpotifyArtists extends AsyncTask<String, Void, ArtistsPager> {

        private final String LOG_TAG = searchSpotifyArtists.class.getSimpleName();


        @Override
        protected ArtistsPager doInBackground(String... params) {
            String artist;
            if (params.length == 0) {
                return null;
            }
            else {artist = params[0];}

            mSpotifyApi.setAccessToken(MainActivity.accessToken());

            ArtistsPager artistsPager;
            try {
                SpotifyService spotify = mSpotifyApi.getService();
                artistsPager = spotify.searchArtists(artist);
                Log.d(LOG_TAG, artistsPager.toString());
            } catch (RetrofitError error) {
                SpotifyError spotifyError = SpotifyError.fromRetrofitError(error);
                Log.d(LOG_TAG,"spotifyError: " + spotifyError.toString());
                artistsPager = null; // redundant?
            }

            return artistsPager;
        } // end searchSpotifyArtists.doInBackground

        @Override
        protected void onPostExecute(ArtistsPager artistsPager) {
            // Cancel pending toast - this happens because search occurs after every keystroke
            if (toast != null) {toast.cancel();}
            if (artistsPager == null || artistsPager.artists.items.size() == 0) {
                Toast toast = Toast.makeText(getActivity(), getText(R.string.no_results_found) + " \'" +
                        artist + "\'", Toast.LENGTH_SHORT);
                toast.show();
                Log.d(LOG_TAG, "Tracks is null");
                // Clear previous results.  This is necessary because the search is done after
                // every keystroke.  When adding or removing a char takes you from having results
                // to no results found, you have to clear the ListView.
                artistArray.clear();
                if (mArtistAdapter != null) {
                    mArtistAdapter.notifyDataSetChanged();
                }
                if (MainActivity.isTwoPane()) {  // clear out toptracksfragment
                    // TODO: need to call TopTracksFragment.clearTopTracksFragment
                }
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
                    url = Constants.NO_IMAGE;
                }
                ShowArtist showArtist = new ShowArtist(artist.name, artist.id, url);
                artistArray.add(showArtist);
                Log.d(LOG_TAG, " parsing artists: " + showArtist.toString());
            }
            // Create ArrayAdapter artist data.  Make sure activity is ready (exists) before
            // calling ArtistAdapter
            Context context = getActivity();
            while (context == null) {
                // NOTE: not sure if this is a good approach
                try {
                    wait(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
                context = getActivity();
            }
            mArtistAdapter = new ArtistAdapter(getActivity(), artistArray);
            mListView.setAdapter(mArtistAdapter);

        } // end searchSpotifyArtists.onPostExecute
    } // end searchSpotifyData

}
