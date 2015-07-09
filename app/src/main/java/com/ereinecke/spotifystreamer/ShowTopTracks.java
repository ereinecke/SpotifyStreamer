package com.ereinecke.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Generates the Top Tracks ListView
 */

public class ShowTopTracks implements Parcelable {

    final String trackName;
    final String albumName;
    private final String artistName;
    private final String trackId;
    final String trackImageUrl;

    public final String NO_IMAGE = "No_Image_Found";

    // Basic constructor
    public ShowTopTracks(String trackName, String albumName, String artistName, String trackId,
                         String trackImageUrl)
    {
        this.trackName  = trackName;
        this.albumName  = albumName;
        this.artistName = artistName;
        this.trackId    = trackId;
        this.trackImageUrl = trackImageUrl;
    }

    // Constructor for use by Parcelable creator
    private ShowTopTracks(Parcel source) {
        // Reconstruct from the parcel

        trackName     = source.readString();
        albumName     = source.readString();
        artistName    = source.readString();
        trackId       = source.readString();
        trackImageUrl = source.readString();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(trackName);
        dest.writeString(albumName);
        dest.writeString(artistName);
        dest.writeString(trackId);
        dest.writeString(trackImageUrl);
    }

    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ShowTopTracks> CREATOR = new Parcelable.Creator<ShowTopTracks>() {

        public ShowTopTracks createFromParcel(Parcel source) {
            return new ShowTopTracks(source);
        }

        public ShowTopTracks[] newArray(int size) {
            return new ShowTopTracks[size];
        }
    };

    public String toString() {
        return ("Top Tracks: " + trackName + "; albumName: " + albumName + "; artist: " + artistName + "; id: " +
                trackId + "; url: " + trackImageUrl);
    }
}
