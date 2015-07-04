package com.ereinecke.spotifystreamer;

import android.app.Activity;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;


/**
 * Adapted, without pun intention, from Udacity class example
 * {@link TopTracksAdapter} exposes a list of artists matching the search term
 * from a {@link Cursor} to a {@link android.widget.ListView}.
 */
class TopTracksAdapter extends ArrayAdapter<ShowTopTracks> {

    private static final String LOG_TAG = TopTracksAdapter.class.getSimpleName();

    /**
     * This is our own custom constructor (it doesn't mirror a superclass constructor).
     * The context is used to inflate the layout file, and the List is the data we want
     * to populate into the lists
     *  @param context        The current context. Used to inflate the layout file.
     *  @param showTopTracks     A List of showTopTracks objects to display in a list
     */
    public TopTracksAdapter(Activity context, ArrayList<ShowTopTracks> showTopTracks) {
        // Here, we initialize the ArrayAdapter's internal storage for the context and the list.
        // the second argument is used when the ArrayAdapter is populating a single TextView.
        // Because this is a custom adapter for two TextViews and an ImageView, the adapter is not
        // going to use this second argument, so it can be any value. Here, we used 0.
        super(context, 0, showTopTracks);
    }

    /**
     * Provides a view for an AdapterView (ListView, GridView, etc.)
     *
     * @param position    The AdapterView position that is requesting a view
     * @param convertView The recycled view to populate.
     * @param parent The parent ViewGroup that is used for inflation.
     * @return The View for the position in the AdapterView.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Gets the ShowTopTracks object from the ArrayAdapter at the appropriate position
        ShowTopTracks showTopTracks = getItem(position);

        // Adapters recycle views to AdapterViews.
        // If this is a new View object we're getting, then inflate the layout.
        // If not, this view already has the layout inflated from a previous call to getView,
        // and we modify the View widgets as usual.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate((R.layout.list_item_topten_listview), parent, false);
        }

        // TODO: Update for this view
        ImageView artistImageView = (ImageView) convertView.findViewById(R.id.list_top_tracks_imageView);
        int thumbnailHeight = getContext().getResources().getInteger(R.integer.thumbnail_width);
        Log.d(LOG_TAG, "trackImageUrl: " + showTopTracks.trackImageUrl);
        Picasso.with(getContext())
                .load(showTopTracks.trackImageUrl)
                .resize(thumbnailHeight, thumbnailHeight)
                .into(artistImageView);

        TextView albumNameView = (TextView) convertView.findViewById(R.id.list_album_textView);
        albumNameView.setText(showTopTracks.albumName);

        TextView trackNameView = (TextView) convertView.findViewById(R.id.list_track_textView);
        trackNameView.setText(showTopTracks.trackName);

        return convertView;
    }
}