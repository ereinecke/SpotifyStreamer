package com.ereinecke.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Generate the Artist ListView
 */

public class ShowArtist implements Parcelable {

    final String artistName;
    final String artistId;
    final String artistImageUrl;

    public static final String NO_IMAGE = "No_Image_Found";

    // Basic constructor
    public ShowArtist(String artistName, String artistId, String artistImageUrl)
    {
        this.artistName = artistName;
        this.artistId = artistId;
        this.artistImageUrl = artistImageUrl;
    }

    // Constructor for use by Parcelable creator
    private ShowArtist(Parcel source) {
        // Reconstruct from the parcel
        artistName     = source.readString();
        artistId       = source.readString();
        artistImageUrl = source.readString();
    }

     public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(artistName);
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
