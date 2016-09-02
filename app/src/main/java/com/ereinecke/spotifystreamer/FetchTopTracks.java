package com.ereinecke.spotifystreamer;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
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
 * FetchTopTracks is an AsyncTask to fetch the Top 10 Tracks for an artist from Spotify.
 * A List of Track is passed to onPostExecute to populate the ListArray
 * tracksArray
 */
public class FetchTopTracks extends AsyncTask<String, Void, Tracks> {

    private final String LOG_TAG = FetchTopTracks.class.getSimpleName();
    private String mArtistName;
    private ListView mListView;
    private Fragment mFragment;
    private SpotifyApi mSpotifyApi;
    private ArrayList<ShowTopTracks> mTopTracksArray;

    public FetchTopTracks(String artistName, ArrayList<ShowTopTracks> topTracksArray,
                          ListView listView, Fragment fragment, SpotifyApi spotifyApi) {

        mArtistName = artistName;
        mTopTracksArray = topTracksArray;
        mListView = listView;
        mFragment = fragment;
        mSpotifyApi = spotifyApi;
    }

    @Override
    protected Tracks doInBackground(String... params) {
        String artistId;
        if (params.length == 0) {
            return null;
        } else {
            artistId = params[0];
        }

        String countryCode = MainActivity.getUserCountry();
        Log.d(LOG_TAG, "in onCreate(), Country Code: " + countryCode);
        if (countryCode == null) countryCode = Constants.COUNTRY_CODE;

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
            tracks = null;
        }
        return tracks;
    } // end searchSpotifyData.doInBackground

    @Override
    protected void onPostExecute(Tracks tracks) {
        Bitmap trackAlbumArt;

        if (tracks == null || tracks.tracks.isEmpty()) {
            // Start a blank TopTracksFragment
            FragmentManager fragmentManager = mFragment.getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            Bundle extras = new Bundle();
            extras.putString(mFragment.getString(R.string.key_artist_name), "");
            extras.putString(mFragment.getString(R.string.key_artist_id), "");

            TopTracksFragment topTracksFragment = new TopTracksFragment();
            topTracksFragment.setArguments(extras);

            Log.d(LOG_TAG, "replacing top_tracks_container");
            fragmentTransaction.replace(R.id.top_tracks_container, topTracksFragment,
                    Constants.TRACKSFRAGMENT_TAG);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

            Toast.makeText(mFragment.getActivity(), mFragment.getText(R.string.no_results_found) +
                    " \'" + mArtistName + "\'", Toast.LENGTH_SHORT).show();
            Log.d(LOG_TAG, "Tracks is null");
            return;
        }

        // Populate ListArray here
        mTopTracksArray.clear();
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
                    mArtistName, track.id, imageUrl, trackUrl, trackLength);
            mTopTracksArray.add(showTopTracks);
            Log.d(LOG_TAG, " Track List: " + showTopTracks.toString());
        }
        Context context = mFragment.getActivity();
        while (context == null) {
            // NOTE: not sure if this is a good approach
            try {
                wait(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
            context = mFragment.getActivity();
        }

        // Create ArrayAdapter artist data
        TopTracksAdapter mTopTracksAdapter = new TopTracksAdapter(mFragment.getActivity(), mTopTracksArray);
        mListView.setAdapter(mTopTracksAdapter);

    } // end searchSpotifyData.onPostExecute
}