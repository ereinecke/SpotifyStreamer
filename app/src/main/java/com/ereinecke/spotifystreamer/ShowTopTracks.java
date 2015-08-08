package com.ereinecke.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Generates the Top Tracks ListView
 */

public class ShowTopTracks implements Parcelable {

    public final String trackName;
    public final String albumName;
    public final String artistName;
    public final String trackId;
    public final String trackImageUrl;
    public final String trackMediaUrl;
    public final long trackLength;

    public final String NO_IMAGE = "No_Image_Found";

    // Basic constructor
    public ShowTopTracks(String trackName, String albumName, String artistName, String trackId,
                         String trackImageUrl, String trackMediaUrl, long trackLength)
    {
        this.trackName  = trackName;
        this.albumName  = albumName;
        this.artistName = artistName;
        this.trackId    = trackId;
        this.trackImageUrl = trackImageUrl;
        this.trackMediaUrl = trackMediaUrl;
        this.trackLength = trackLength;
    }

    // Constructor for use by Parcelable creator
    private ShowTopTracks(Parcel source) {
        // Reconstruct from the parcel
        trackName     = source.readString();
        albumName     = source.readString();
        artistName    = source.readString();
        trackId       = source.readString();
        trackImageUrl = source.readString();
        trackMediaUrl = source.readString();
        trackLength   = source.readLong();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(trackName);
        dest.writeString(albumName);
        dest.writeString(artistName);
        dest.writeString(trackId);
        dest.writeString(trackImageUrl);
        dest.writeString(trackMediaUrl);
        dest.writeLong(trackLength);
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
        return ("Top Tracks: " + trackName + "; albumName: " + albumName + "; artist: " +
                artistName + "; id: " + trackId + "; imageUrl: " + trackImageUrl + "; mediaUrl: " +
                trackMediaUrl + "; trackLength: " + trackLength);
    }
}
