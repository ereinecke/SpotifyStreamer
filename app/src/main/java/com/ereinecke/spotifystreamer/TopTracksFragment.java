/*
 * Copyright (C) 2015 The Android Open Source Project
 */

package com.ereinecke.spotifystreamer;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;

/**
 * TopTenFragment displays top ten tracks for a selected artist.
 */

public class TopTracksFragment extends Fragment {

    private static final String LOG_TAG = TopTracksFragment.class.getSimpleName();

    // Call broadcast receiver for list position updates
    private ChangeTrackReceiver changeTrackReceiver;

    private static Bundle trackInfoBundle;
    private static String countryCode;

    private static int mTracksListPosition = ListView.INVALID_POSITION;
    private String artistName;
    private String artistId;

    private ArrayList<ShowTopTracks> topTracksArray = new ArrayList<>();
    private TopTracksAdapter mTopTracksAdapter;
    private ListView mListView;
    private final SpotifyApi mSpotifyApi = new SpotifyApi();

    public TopTracksFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras;

        setRetainInstance(true);

        countryCode =  MainActivity.getUserCountry();
        Log.d(LOG_TAG, "in onCreate(), Country Code: " + countryCode);
        if (countryCode == null) countryCode = Constants.COUNTRY_CODE;

        if (MainActivity.isTwoPane()) {
            // fragment arguments from MainActivity
            extras = getArguments();
            if (extras == null) {
                Log.d(LOG_TAG, "in onCreate(): fragment parameters not available.");
            }

        } else if (getActivity().getIntent() != null) {  // Started by TopTracksActivity, via Intent
            extras = getActivity().getIntent().getExtras();
        } else {        // can't get extras from MainActivity or TopTracksActivity
            extras = savedInstanceState;
        }

        if (extras != null) {
            artistId = extras.getString(getString(R.string.key_artist_id));
            artistName = extras.getString(getString(R.string.key_artist_name));
            Log.d(LOG_TAG, "onCreate: ArtistName: " + artistName + " ArtistId: " + artistId);
            topTracksArray = extras.getParcelableArrayList(Constants.TOP_TRACKS_ARRAY);
            mTracksListPosition = extras.getInt(Constants.TOP_TRACKS_POSITION);
        } else {
            // This should only happen when starting up, before first artist selection
            // TODO: need to clear list programmatically, when search text changes but doesn't trigger a search
            Log.d(LOG_TAG, "onCreate: no extras available, blank topTracksFragment");

        changeTrackReceiver = new ChangeTrackReceiver();
        IntentFilter intentFilter = new IntentFilter(Constants.LIST_POSITION_KEY);
        getActivity().registerReceiver(changeTrackReceiver, intentFilter);
        Log.d(LOG_TAG, "in onCreate(), registering changeTrackReceiver");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        // TODO: need to account for change in mTracksListPosition due to Prev/Next/onCompletion.
        if (savedInstanceState != null) {
            topTracksArray = savedInstanceState.getParcelableArrayList(Constants.TOP_TRACKS_ARRAY);
            trackInfoBundle = savedInstanceState.getBundle(Constants.TRACK_INFO);
            artistId = savedInstanceState.getString(getString(R.string.key_artist_id));
            artistName = savedInstanceState.getString(getString(R.string.key_artist_name));
            // don't read mTracksListPosition from savedInstanceState
            if (mTracksListPosition == ListView.INVALID_POSITION) {
                mTracksListPosition = savedInstanceState.getInt(Constants.TOP_TRACKS_POSITION);
            }
        } else {   // read from intent, launched by TopTracksActivity
            Bundle extras = getActivity().getIntent().getExtras();
            if (extras == null) {  // don't use intent, must be TwoPane. Read fragment arguments.
                extras = this.getArguments();
            }
            if (extras == null) {
                // Error condition
                Log.d(LOG_TAG, "Can't read extras in onCreateView().");
                artistId = "";
                artistName = "";
            } else {
                artistId = extras.getString(getString(R.string.key_artist_id));
                artistName = extras.getString(getString(R.string.key_artist_name));
                topTracksArray = extras.getParcelableArrayList(Constants.TOP_TRACKS_ARRAY);
                mTracksListPosition = extras.getInt(Constants.TOP_TRACKS_POSITION);
            }
            if (topTracksArray == null) { // should only happen in TwoPane mode; blank fragment
                topTracksArray = new ArrayList<>();
                mTracksListPosition = ListView.INVALID_POSITION;
            }

        // Get a reference to the ListView and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.list_item_top_tracks_display);
        if (mTracksListPosition >= 0) {
            // mListView.setSelection(mTracksListPosition);
            mListView.setItemChecked(mTracksListPosition, true);
        }

        // Create ArrayAdapter using persisted artist data
        if (topTracksArray != null) {
            mTopTracksAdapter = new TopTracksAdapter(getActivity(), topTracksArray);
            mListView.setAdapter(mTopTracksAdapter);
        }

        // Set up listener for clicking on an item in the ListView
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                mTracksListPosition = position;
                Log.d(LOG_TAG, "item #" + mTracksListPosition + " clicked");
                if (mTracksListPosition >= 0 && (mTracksListPosition < topTracksArray.size())) {
                    showMediaPlayer(topTracksArray, mTracksListPosition);
                } else {
                    throw new IllegalArgumentException();
                }
            }
        });

        // in TwoPane mode, set header for TopTracksFragment unless artistName not yet defined
        if (MainActivity.isTwoPane()) {
            TextView topTracksHeader = (TextView) rootView.findViewById(R.id.top_tracks_header);
            if (artistName != null && artistName != "") {
                topTracksHeader.setText(getString(R.string.top_tracks_label) + " - " + artistName);
            }
        }

            // Get top tracks list from Spotify in background task
            // This should only happen if topTracksArray is null or empty
            if (topTracksArray != null || topTracksArray.size() > 0) {
                FetchTopTracks spotifyData = new FetchTopTracks();
                if (artistId != null && artistId != "") {
                    spotifyData.execute(artistId);
                } else {
                    Log.d(LOG_TAG, "Set up blank TopTracksFragment with artistId null");
                }
            }
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Register broadcast receiver for track change, use to update mTracksListPosition
        if (changeTrackReceiver == null) {
            changeTrackReceiver = new ChangeTrackReceiver();
            IntentFilter intentFilter = new IntentFilter(Constants.LIST_POSITION_KEY);
            getActivity().registerReceiver(changeTrackReceiver, intentFilter);
            Log.d(LOG_TAG, "in onResume(), registering changeTrackReceiver");
        }
    }

    @Override
    public void onPause() {
        if (changeTrackReceiver != null) {
            Log.d(LOG_TAG, "in onPause(), unregistering changeTrackReceiver");
            getActivity().unregisterReceiver(changeTrackReceiver);
            changeTrackReceiver = null;
        }
        super.onPause();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(Constants.TOP_TRACKS_ARRAY, topTracksArray);
        outState.putInt(Constants.TOP_TRACKS_POSITION, mTracksListPosition);
        outState.putBundle(Constants.TRACK_INFO, trackInfoBundle);
        outState.putString(getString(R.string.key_artist_id), artistId);
        outState.putString(getString(R.string.key_artist_name), artistName);
    }

    /**
     * FetchTopTracks is an AsyncTask to fetch the Top 10 Tracks for an artist from Spotify.
     * A List of Track is passed to onPostExecute to populate the ListArray
     * tracksArray
     */
    public class FetchTopTracks extends AsyncTask<String, Void, Tracks> {

        private final String LOG_TAG = FetchTopTracks.class.getSimpleName();

        @Override
        protected Tracks doInBackground(String... params) {
            String artistId;
            if (params.length == 0) {
                return null;
            } else {
                artistId = params[0];
            }

            SpotifyService spotify = mSpotifyApi.getService();
            Map<String, Object> country = new HashMap<>();
            country.put("country", countryCode);
            Log.d(LOG_TAG, "artistId: " + artistId);
            Tracks tracks;
            try {
                tracks = spotify.getArtistTopTrack(artistId, country);
                Log.d(LOG_TAG, tracks.toString());
            } catch (RetrofitError error) {
                SpotifyError spotifyError = SpotifyError.fromRetrofitError(error);
                Log.d(LOG_TAG, "spotifyError: " + spotifyError.toString());
                tracks = null; // redundant?
            }
            return tracks;
        } // end searchSpotifyData.doInBackground

        @Override
        protected void onPostExecute(Tracks tracks) {
            Bitmap trackAlbumArt;

            if (tracks == null || tracks.tracks.isEmpty()) {
                // Start a blank TopTracksFragment
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                Bundle extras = new Bundle();
                extras.putString(getString(R.string.key_artist_name), "");
                extras.putString(getString(R.string.key_artist_id), "");

                TopTracksFragment topTracksFragment = new TopTracksFragment();
                topTracksFragment.setArguments(extras);

                Log.d(LOG_TAG, "replacing top_tracks_container");
                fragmentTransaction.replace(R.id.top_tracks_container, topTracksFragment,
                        Constants.TRACKSFRAGMENT_TAG);
                fragmentTransaction.addToBackStack(null);

                Toast.makeText(getActivity(), getText(R.string.no_results_found) + " \'" +
                        artistName + "\'", Toast.LENGTH_SHORT).show();
                Log.d(LOG_TAG, "Tracks is null");
                return;
            }

            // Populate ListArray here
            topTracksArray.clear();
            for (Track track : tracks.tracks) {
                // AAARGGHHH!  Can't use external urls with MediaPlayer. Stuck with previews for now.
                // String trackUrl = track.external_urls.get("spotify");
                String trackUrl = track.preview_url;
                long trackLength = track.duration_ms;
                final String imageUrl;
                // The image is pulled from the album the track is from
                // Don't like this approach; we have to download track art twice, here
                // and when populating the adapter.
                if (track.album.images.size() > 0) {
                    imageUrl = track.album.images.get(1).url;
                } else {
                    imageUrl = Constants.NO_IMAGE;
                    trackAlbumArt = MainActivity.getPlaceholderImage();
                }
                ShowTopTracks showTopTracks = new ShowTopTracks(track.name, track.album.name,
                        artistName, track.id, imageUrl, trackUrl, trackLength);
                        // artistName, track.id, imageUrl, trackUrl, trackLength, trackAlbumArt);
                topTracksArray.add(showTopTracks);
                Log.d(LOG_TAG, " Track List: " + showTopTracks.toString());
            }
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

            // Create ArrayAdapter artist data
            mTopTracksAdapter = new TopTracksAdapter(getActivity(), topTracksArray);
            mListView.setAdapter(mTopTracksAdapter);

        } // end searchSpotifyData.onPostExecute
    }

    private void showMediaPlayer(ArrayList<ShowTopTracks> topTracksArray, int mPosition) {

        trackInfoBundle = new Bundle();
        trackInfoBundle.putParcelableArrayList(Constants.TRACK_INFO, topTracksArray);
        trackInfoBundle.putInt(Constants.TOP_TRACKS_POSITION, mPosition);

        if (MainActivity.isTwoPane()) {             // start player fragment
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Fragment prev = getFragmentManager().findFragmentByTag(Constants.PLAYERFRAGMENT_TAG);
            if (prev != null) {
                Log.d(LOG_TAG, "Removing PlayerFragment");
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            DialogFragment newPlayerFragment = new PlayerFragment();
            newPlayerFragment.setArguments(trackInfoBundle);
            newPlayerFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
            newPlayerFragment.setShowsDialog(true);
//            newPlayerFragment.show(ft, Constants.PLAYERFRAGMENT_TAG);
            ft.show(newPlayerFragment);

        } else {                                    // start player activity
            Intent intent = new Intent(getActivity(), PlayerActivity.class);

            intent.putExtras(trackInfoBundle);
            Log.d(LOG_TAG, "Starting PlayerActivity");
            startActivity(intent);
        }
    }

    public static int getListPosition() {
        return mTracksListPosition;
    }

    public void setListPosition(int position) {
        mTracksListPosition = position;
        // mListView.setSelection(position); // works if not in touch mode
        mListView.setItemChecked(position, true); // works in touch mode

    }

    // Don't like doing this, but having a heck of a time passing trackInfo through intent extra
    // Shouldn't be null because it's called from PlayerFragment which was launched from
    // showMediaPlayer().
    public static Bundle getTrackInfo() {
        return trackInfoBundle;
    }

    // BroadcastReceiver for when highlighted track needs to be updated
    public class ChangeTrackReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(Constants.CURRENT_TRACK_KEY)) {
                int newPos = intent.getIntExtra(Constants.CURRENT_TRACK_KEY, 0);
                Log.d(LOG_TAG, "changeTrackReceiver called, newPos: " + newPos);
                // This logic assumes 10 tracks always
                if (newPos > 0 && newPos < 10) {
                    setListPosition(newPos);
                }
            }
        }
    }

    // Clear all data from TopTracksFragment, used when artist search gets no hits
    public void clearTopTracksFragment() {

        TextView topTracksHeader = (TextView) getView().findViewById(R.id.top_tracks_header);
        topTracksHeader.setText("");

        mTopTracksAdapter = new TopTracksAdapter(getActivity(), new ArrayList<ShowTopTracks>());
        mListView.setAdapter(mTopTracksAdapter);
        topTracksArray.clear();

    }
}
