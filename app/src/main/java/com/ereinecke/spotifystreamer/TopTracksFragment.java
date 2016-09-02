/*
 * Copyright (C) 2015 The Android Open Source Project
 */

package com.ereinecke.spotifystreamer;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;

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
    private View rootView;

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

        countryCode = MainActivity.getUserCountry();
        Log.d(LOG_TAG, "in onCreate(), Country Code: " + countryCode);
        if (countryCode == null) countryCode = Constants.COUNTRY_CODE;

        if (MainActivity.isTwoPane()) {
            // fragment arguments from FindArtistFragment
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

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        // TODO: need to account for change in mTracksListPosition due to Prev/Next/onCompletion.
        if (savedInstanceState != null) {

            topTracksArray = savedInstanceState.getParcelableArrayList(Constants.TOP_TRACKS_ARRAY);
            trackInfoBundle = savedInstanceState.getBundle(Constants.TRACK_INFO);
            artistId = savedInstanceState.getString(getString(R.string.key_artist_id));
            artistName = savedInstanceState.getString(getString(R.string.key_artist_name));
            // TODO: ?? don't read mTracksListPosition from savedInstanceState
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
            // TODO: Scroll list to proper position?
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
                    // Last parameter true means stop player before starting
                    showMediaPlayer(topTracksArray, mTracksListPosition, true);
                } else {
                    throw new IllegalArgumentException();
                }
            }
        });

        // in TwoPane mode, set header for TopTracksFragment unless artistName not yet defined
        if (MainActivity.isTwoPane()) {
            TextView topTracksHeader = (TextView) rootView.findViewById(R.id.top_tracks_header);
            if (artistName != null && !artistName.equals("")) {
                topTracksHeader.setText(getString(R.string.top_tracks_label) + " - " + artistName);
            }
        }

        // Get top tracks list from Spotify in background task
        // This should only happen if topTracksArray is null or empty
        if (topTracksArray != null || topTracksArray.size() > 0) {
            FetchTopTracks spotifyData = new FetchTopTracks(artistName, topTracksArray,
                    mListView, this, mSpotifyApi);
            if (artistId != null && artistId != "") {
                spotifyData.execute(artistId);
            } else {
                clearTopTracksFragment();
                Log.d(LOG_TAG, "Set up blank TopTracksFragment with artistId null");
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

    @Override
    public void onActivityCreated(Bundle outState) {

        super.onActivityCreated(outState);

        if (outState != null) {
            topTracksArray = outState.getParcelableArrayList(Constants.TOP_TRACKS_ARRAY);
            mTracksListPosition = outState.getInt(Constants.TOP_TRACKS_POSITION);
            trackInfoBundle = outState.getBundle(Constants.TRACK_INFO);
            artistId = outState.getString(getString(R.string.key_artist_id));
            artistName = outState.getString(getString(R.string.key_artist_name));
        }
    }


    /* Brings up PlayerActivity or PlayerFragment as appropriate.  If called with a null
     * topTracksArray, then just bring player forward showing what is currently playing.
     */
    private void showMediaPlayer(ArrayList<ShowTopTracks> topTracksArray, int mPosition,
                                 boolean newTrack) {

        trackInfoBundle = new Bundle();
        if (topTracksArray == null) {
            trackInfoBundle.putInt(Constants.TOP_TRACKS_POSITION, Constants.USE_CURRENT);

        } else {
            trackInfoBundle.putInt(Constants.TOP_TRACKS_POSITION, mPosition);
        }
        trackInfoBundle.putParcelableArrayList(Constants.TRACK_INFO, topTracksArray);
        trackInfoBundle.putBoolean(Constants.NEW_TRACK, newTrack);


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
            newPlayerFragment.show(ft, Constants.PLAYERFRAGMENT_TAG);

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
        mListView.setSelection(position); // works if not in touch mode
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

        if (MainActivity.isTwoPane()) {
            TextView topTracksHeader = (TextView) rootView.findViewById(R.id.top_tracks_header);
            topTracksHeader.setText("");
        }

        mTopTracksAdapter = new TopTracksAdapter(getActivity(), new ArrayList<ShowTopTracks>());
        mListView.setAdapter(mTopTracksAdapter);
        topTracksArray.clear();

    }
}
