/*
 * Copyright (C) 2015 The Android Open Source Project
 */

package com.ereinecke.spotifystreamer;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
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
    // key for persisting retrieved tracks
    private static final String TOP_TRACKS_ARRAY = "TopTracksArray";
    private static final String COUNTRY_CODE = "MX"; // fallback country code
    private static String countryCode;

    private int mPosition = ListView.INVALID_POSITION;

    private ArrayList<ShowTopTracks> topTracksArray = new ArrayList<>();
    private TopTracksAdapter mTopTracksAdapter;
    private ListView mListView;
    private final SpotifyApi mSpotifyApi = new SpotifyApi();

    public TopTracksFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "in onCreate");

        // Get country code
        countryCode = getUserCountry(getActivity());
        Log.d(LOG_TAG, "Country Code: " + countryCode);
        if (countryCode == null) countryCode = COUNTRY_CODE;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        // Get a reference to the ListView and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.list_top_tracks);

        if (savedInstanceState != null) {
            topTracksArray = savedInstanceState.getParcelableArrayList(TOP_TRACKS_ARRAY);

            // Create ArrayAdapter using persisted artist data
            if (topTracksArray != null) {
                mTopTracksAdapter = new TopTracksAdapter(getActivity(), topTracksArray);
                mListView.setAdapter(mTopTracksAdapter);
            }
        }

        // Set up listener for clicking on an item in the ListView
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

        mPosition = position;
        // Will be launching player

            }
        });

        FetchTopTracks spotifyData = new FetchTopTracks();
        if (TopTracksActivity.artistId != null) {
            spotifyData.execute(TopTracksActivity.artistId);
        }
        else {Log.d(LOG_TAG, "artistId null");}
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(TOP_TRACKS_ARRAY, topTracksArray);
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
            Tracks tracks = null;
            try {
                tracks = spotify.getArtistTopTrack(artistId, country);
                Log.d(LOG_TAG, tracks.toString());
            } catch (RetrofitError error) {
                SpotifyError spotifyError = SpotifyError.fromRetrofitError(error);
                Log.d(LOG_TAG,"spotifyError: " + spotifyError.toString());
                tracks = null; // redundant?
            }

            return tracks;
        } // end searchSpotifyData.doInBackground

        @Override
        protected void onPostExecute(Tracks tracks) {
            if (tracks == null || tracks.tracks.isEmpty()) {
                Toast.makeText(getActivity(), getText(R.string.no_results_found) + " \'" +
                        TopTracksActivity.artistName + "\'", Toast.LENGTH_SHORT).show();
                Log.d(LOG_TAG, "Tracks is null");
                return;
            }

            // Populate ListArray here
            topTracksArray.clear();
            for (Track track : tracks.tracks) {
                String url;
                // The image is pulled from the album the track is from
                if (track.album.images.size() > 0) {
                    url = track.album.images.get(1).url;
                } else {
                    url = ShowArtist.NO_IMAGE;
                }
                ShowTopTracks showTopTracks = new ShowTopTracks(track.name, track.album.name,
                        TopTracksActivity.artistName, track.id, url);
                final boolean added = topTracksArray.add(showTopTracks);
                Log.d(LOG_TAG, " Track List: " + showTopTracks.toString());
            }
            // Create ArrayAdapter artist data
            mTopTracksAdapter = new TopTracksAdapter(getActivity(), topTracksArray);
            mListView.setAdapter(mTopTracksAdapter);
        } // end searchSpotifyData.onPostExecute
    }

    /**
     * Get ISO 3166-1 alpha-2 country code for this device (or null if not available)
     * This method was lifted verbatim from StackOverflow, thanks to Marco W.
     * @param context Context reference to get the TelephonyManager instance from
     * @return country code or null
     * TODO: this only works on phones, not on tablet
     */
    private static String getUserCountry(Context context) {
        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final String simCountry = tm.getSimCountryIso();
            if (simCountry != null && simCountry.length() == 2) { // SIM country code is available
                return simCountry.toLowerCase(Locale.US);
            }
            else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                String networkCountry = tm.getNetworkCountryIso();
                if (networkCountry != null && networkCountry.length() == 2) { // network country code is available
                    return networkCountry.toLowerCase(Locale.US);
                }
            }
        }
        catch (Exception e) { }
        return null;
    }

}
