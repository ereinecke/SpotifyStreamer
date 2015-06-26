package com.ereinecke.spotifystreamer;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * {@link ArtistAdapter} exposes a list of artists matching the search term
 * from a {@link Cursor} to a {@link android.widget.ListView}.
 */
public class ArtistAdapter extends CursorAdapter {

    // TODO: Utterly confused with this
    private static final int figureThisOut = 1;

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final ImageView artistImageView;
        public final TextView artistNameView;

        public ViewHolder(View view) {
            artistImageView = (ImageView) view.findViewById(R.id.list_artist_imageView);
            artistNameView  = (TextView) view.findViewById(R.id.list_artist_textView);
        }
    }

    public ArtistAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        int layoutId = R.layout.list_item_artist_listview;

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int viewType = getItemViewType(cursor.getPosition());

        // TODO: Figure this out!  .setImageResource and .SetText
        /* public void setImageResource (int resId)

        Added in API level 1
        Sets a drawable as the content of this ImageView.

        This does Bitmap reading and decoding on the UI thread, which can cause a latency
        hiccup. If that's a concern, consider using setImageDrawable(android.graphics.drawable.Drawable)
        or setImageBitmap(android.graphics.Bitmap) and BitmapFactory instead.
         */
        // Get artist artwork
        viewHolder.artistImageView.setImageResource(R.mipmap.ic_launcher);

        // Get artist name
        viewHolder.artistNameView.setText(R.string.artist_name_placeholder);

    }

}

