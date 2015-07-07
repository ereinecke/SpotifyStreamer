package com.ereinecke.spotifystreamer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TopTracksActivity extends AppCompatActivity {

    public static String artistName;
    public static String artistId;

    private static final String LOG_TAG = TopTracksActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new TopTracksFragment())
                    .commit();
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            artistName = extras.getString(FindArtistFragment.ARTIST_NAME);
            artistId = extras.getString(FindArtistFragment.ARTIST_ID);
            // artistImageUrl = extras.getString(FindArtistFragment.ARTIST_IMAGE_URL);
            Log.d(LOG_TAG, "Artist ID: " + artistId);
        }
        else {Log.d(LOG_TAG, "intent is null");}

        // Put artist name in the action bar subtitle
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setTitle(getString(R.string.top_tracks_label));
            actionbar.setSubtitle(artistName);
        }
        else {Log.d(LOG_TAG,"action bar null");}
     }


    /**
     * TopTen fragment containing a simple view.
     */
    public static class TopTenFragment extends Fragment {

        private static final String LOG_TAG = TopTenFragment.class.getSimpleName();
        // private String mForecastStr;

        public TopTenFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            return inflater.inflate(R.layout.list_item_topten_listview, container, false);
        }
    }
}