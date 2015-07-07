package com.ereinecke.spotifystreamer;

import android.app.Activity;
import android.database.Cursor;
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
 * {@link ArtistAdapter} exposes a list of artists matching the search term
 * from a {@link Cursor} to a {@link android.widget.ListView}.
 */
class ArtistAdapter extends ArrayAdapter<ShowArtist> {


    private static final String LOG_TAG = ArtistAdapter.class.getSimpleName();

    /**
     * This is our own custom constructor (it doesn't mirror a superclass constructor).
     * The context is used to inflate the layout file, and the List is the data we want
     * to populate into the lists
     *  @param context        The current context. Used to inflate the layout file.
     * @param showArtist     A List of showArtist objects to display in a list
     */
    public ArtistAdapter(Activity context, ArrayList<ShowArtist> showArtist) {
        // Here, we initialize the ArrayAdapter's internal storage for the context and the list.
        // the second argument is used when the ArrayAdapter is populating a single TextView.
        // Because this is a custom adapter for two TextViews and an ImageView, the adapter is not
        // going to use this second argument, so it can be any value. Here, we used 0.
        super(context, 0, showArtist);
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
        // Gets the showArtist object from the ArrayAdapter at the appropriate position
        ShowArtist showArtist = getItem(position);

        // Adapters recycle views to AdapterViews.
        // If this is a new View object we're getting, then inflate the layout.
        // If not, this view already has the layout inflated from a previous call to getView,
        // and we modify the View widgets as usual.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate((R.layout.list_item_artist_listview),
                                        parent, false);
        }

        ImageView artistImageView = (ImageView) convertView.findViewById(R.id.list_artist_imageView);
        if (showArtist.artistImageUrl.equals(showArtist.NO_IMAGE)) {
            artistImageView.setImageResource(R.drawable.no_image);
        }
        else {
            int thumbnailHeight = getContext().getResources().getInteger(R.integer.thumbnail_width);
            Picasso.with(getContext())
                    .load(showArtist.artistImageUrl)
                    .resize(thumbnailHeight, thumbnailHeight)
                    .into(artistImageView);
        }

        TextView artistNameView = (TextView) convertView.findViewById(R.id.list_artist_textView);
        artistNameView.setText(showArtist.artistName);

        return convertView;
    }
}


