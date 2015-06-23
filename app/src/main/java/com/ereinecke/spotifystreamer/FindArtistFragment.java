package com.ereinecke.spotifystreamer;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * FindArtistFragment handles searching for an artist and displaying all artists that
 * match the search phrase.
 */

public class FindArtistFragment extends Fragment {

    private static final String LOG_TAG = FindArtistFragment.class.getSimpleName();
    private ArtistAdapter mArtistAdapter;
    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;

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

        /* Create ArrayAdapter to display dummy artist data */
        mArtistAdapter = new ArtistAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.list_artist);
        mListView.setAdapter(mArtistAdapter);

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

    private ArrayList<String> dummyArtistList() {
        ArrayList<String> dummyList = new ArrayList<String>();
        dummyList.add("Gil Gutierrez");
        dummyList.add("Gil Gutierrez & Pedro Cartas");
        dummyList.add("Gil Gutierrez & Doc Severinsen");
        dummyList.add("Gil Gutierrez, Pedro Cartas & Doc Severinsen");

        return dummyList;
    }

}
