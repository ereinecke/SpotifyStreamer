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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TopTracksViewHolder viewHolder;

        // Gets the ShowTopTracks object from the ArrayAdapter at the appropriate position
        ShowTopTracks showTopTracks = getItem(position);

        // Adapters recycle views to AdapterViews.
        // If this is a new View object we're getting, then inflate the layout.
        // If not, this view already has the layout inflated from a previous call to getView,
        // and we modify the View widgets as usual.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate((R.layout.list_item_topten_listview), parent, false);

            viewHolder = new TopTracksViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (TopTracksViewHolder) convertView.getTag();
        }


        ImageView artistImageView = viewHolder.artistImageView;
        Log.d(LOG_TAG, "trackImageUrl: " + showTopTracks.trackImageUrl);
        if (showTopTracks.trackImageUrl.equals(Constants.NO_IMAGE)) {
            artistImageView.setImageResource(R.drawable.no_image);
        }
        else {
            int thumbnailHeight = getContext().getResources().getInteger(R.integer.thumbnail_width);
            Picasso.with(getContext())
                    .load(showTopTracks.trackImageUrl)
                    .resize(thumbnailHeight, thumbnailHeight)
                    .into(artistImageView);
        }

        TextView albumNameView = viewHolder.albumNameView;
        albumNameView.setText(showTopTracks.albumName);

        TextView trackNameView = viewHolder.trackNameView;
        trackNameView.setText(showTopTracks.trackName);

        return convertView;
    }

    public static class TopTracksViewHolder {
        public final ImageView artistImageView;
        public final TextView albumNameView;
        public final TextView trackNameView;

        public TopTracksViewHolder(View view) {
            artistImageView = (ImageView) view.findViewById(R.id.list_top_tracks_imageView);
            albumNameView = (TextView) view.findViewById(R.id.list_album_textView);
            trackNameView = (TextView) view.findViewById(R.id.list_track_textView);
        }
    }
}