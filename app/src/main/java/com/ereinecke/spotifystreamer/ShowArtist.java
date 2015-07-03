package com.ereinecke.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Class used to generate the Artist Listview
 */

public class ShowArtist implements Parcelable {

    String artistName;
    String artistId;
    String artistImageUrl;

    public static final String NO_IMAGE = "No_Image_Found";
    private static final String LOG_TAG = ShowArtist.class.getSimpleName();
    private ShowArtist mShowArtist;

    // Basic constructor
    public ShowArtist(String artistName, String artistId, String artistImageUrl)
    {
        this.artistName = artistName;
        this.artistId = artistId;
        this.artistImageUrl = artistImageUrl;
    }

    // Constructor for use by Parcelable creator
    public ShowArtist(Parcel source) {
        // Reconstruct from the parcel
        Log.d(LOG_TAG, "Recreating from parcel");
        artistName     = source.readString();
        artistId       = source.readString();
        artistImageUrl = source.readString();
    }

     public void writeToParcel(Parcel dest, int flags) {
        Log.d(LOG_TAG, "writeToParcel..." + flags);
        dest.writeString(artistName);
        Log.d(LOG_TAG,"artistName: " + artistName);
        dest.writeString(artistId);
        dest.writeString(artistImageUrl);
    }

    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ShowArtist> CREATOR = new Parcelable.Creator<ShowArtist>() {

        public ShowArtist createFromParcel(Parcel source) {
            return new ShowArtist(source);
        }

        public ShowArtist[] newArray(int size) {
            return new ShowArtist[size];
        }
    };

    public String toString() {
        return ("Artist: " + artistName + "; id: " + artistId + "; url: " + artistImageUrl);
    }
}
