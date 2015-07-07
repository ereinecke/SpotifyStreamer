package com.ereinecke.spotifystreamer;

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
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;


/**
 * TopTenFragment displays top ten tracks for a selected artist.
 */

public class TopTracksFragment extends Fragment {

    private static final String LOG_TAG = TopTracksFragment.class.getSimpleName();
    // key for persisting retrieved tracks
    private static final String TOP_TRACKS_ARRAY = "TopTracksArray";
    private static final String COUNTRY_CODE = "MX"; // TODO: this should look up device location

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

            // Not sure if this is necessary, having been applied in FindArtistsFragment
            // mSpotifyApi.setAccessToken(MainActivity.accessToken());

            SpotifyService spotify = mSpotifyApi.getService();
            Map<String, Object> countryCode = new HashMap<>();
            countryCode.put("country", COUNTRY_CODE);
            Log.d(LOG_TAG, "artistId: " + artistId);
            Tracks tracks = spotify.getArtistTopTrack(artistId, countryCode);
            Log.d(LOG_TAG, tracks.toString());

            return tracks;
        } // end searchSpotifyData.doInBackground

        @Override
        protected void onPostExecute(Tracks tracks) {
            if (tracks.tracks.size() == 0) {
                Toast.makeText(getActivity(), "No results found.",
                        Toast.LENGTH_SHORT).show();
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
}
