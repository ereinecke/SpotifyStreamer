/*
 * Copyright (C) 2015 The Android Open Source Project
 */

package com.ereinecke.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
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

    private static Bundle trackInfoBundle;
    private static String countryCode;

    private int mPosition = ListView.INVALID_POSITION;
    private String artistName;
    private String artistId;

    private ArrayList<ShowTopTracks> topTracksArray = new ArrayList<>();
    private TopTracksAdapter mTopTracksAdapter;
    private ListView mListView;
    private final SpotifyApi mSpotifyApi = new SpotifyApi();
    private static Bitmap selectedAlbumArt;

    public TopTracksFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get country code not yet implemented
        countryCode = MainActivity.getUserCountry();
        Log.d(LOG_TAG, "Country Code: " + countryCode);
        if (countryCode == null) countryCode = Constants.COUNTRY_CODE;

        Bundle extras = getActivity().getIntent().getExtras();
        artistId = extras.getString(getString(R.string.key_artist_id));
        artistName = extras.getString(getString(R.string.key_artist_name));
        Log.d(LOG_TAG,"onCreate: ArtistName: " + artistName + " ArtistId: " + artistId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        // Get a reference to the ListView and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.list_top_tracks);

        if (savedInstanceState != null) {
            topTracksArray = savedInstanceState.getParcelableArrayList(Constants.TOP_TRACKS_ARRAY);

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
            ShowTopTracks trackInfo = (ShowTopTracks) adapterView.getItemAtPosition(position);
            showMediaPlayer(trackInfo);
            }
        });

        FetchTopTracks spotifyData = new FetchTopTracks();
        if (artistId != null) {
            spotifyData.execute(artistId);
        }
        else {Log.d(LOG_TAG, "artistId null");}
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(Constants.TOP_TRACKS_ARRAY, topTracksArray);
        outState.putBundle(Constants.TRACK_INFO, trackInfoBundle);
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
                Log.d(LOG_TAG,"spotifyError: " + spotifyError.toString());
                tracks = null; // redundant?
            }

            return tracks;
        } // end searchSpotifyData.doInBackground

        @Override
        protected void onPostExecute(Tracks tracks) {
            if (tracks == null || tracks.tracks.isEmpty()) {
                Toast.makeText(getActivity(), getText(R.string.no_results_found) + " \'" +
                        artistName + "\'", Toast.LENGTH_SHORT).show();
                Log.d(LOG_TAG, "Tracks is null");
                return;
            }

            // Populate ListArray here
            topTracksArray.clear();
            for (Track track : tracks.tracks) {
                String trackUrl = track.external_urls.get("spotify");
                long trackLength = track.duration_ms;
                String imageUrl;
                // The image is pulled from the album the track is from
                if (track.album.images.size() > 0) {
                    // TODO: better way to get right size image? Picasso?
                    imageUrl = track.album.images.get(1).url;
                } else {
                    imageUrl = Constants.NO_IMAGE;
                }
                ShowTopTracks showTopTracks = new ShowTopTracks(track.name, track.album.name,
                        artistName, track.id, imageUrl, trackUrl, trackLength);
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

    private void showMediaPlayer(ShowTopTracks trackInfo) {

        Intent intent = new Intent(getActivity(), PlayerActivity.class);
        trackInfoBundle = new Bundle();
        trackInfoBundle.putParcelable(Constants.TRACK_INFO, trackInfo);
        intent.putExtras(trackInfoBundle);
        startActivity(intent);
    }

    // TODO: Need to pass trackInfo via intent
    // Don't like doing this, but having a heck of a time passing trackInfo through intent extra
    public static Bundle getTrackInfo() {
        return trackInfoBundle;
    }
}
