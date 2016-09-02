package com.ereinecke.spotifystreamer;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static android.widget.SeekBar.OnSeekBarChangeListener;


/**
 * PlayerFragment is a container for the MediaPlayer.
 */
public class PlayerFragment extends DialogFragment implements DialogInterface.OnDismissListener {

    private static final String LOG_TAG = PlayerFragment.class.getSimpleName();
    private static ImageButton playButton;
    private static SeekBar seekBar;
    private static Drawable playButtonDrawable;
    private static Drawable pauseButtonDrawable;
    private static ShowTopTracks trackInfo;
    private static ArrayList<ShowTopTracks> topTracksArrayList;
    private static int mPosition;
    private static PlayerService mPlayerService;
    private static View playerView;
    private boolean mBound;
    private boolean newTrack;
    private TextView currentTimeView;
    private static ProgressDialog spinner;
    private Timer seekTimer;


    public PlayerFragment() {
    }

    static PlayerFragment newInstance() {
        return new PlayerFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Connect to ServiceFragment to get mPlayerService
        FragmentManager fm = getActivity().getSupportFragmentManager();
        ServiceFragment serviceFragment = MainActivity.getServiceFragment();
        Log.d(LOG_TAG, "Existing ServiceFragment at " + serviceFragment.toString());
        if (serviceFragment == null) {
            Log.d(LOG_TAG, "Starting new ServiceFragment");

            // Hopefully won't start second PlayerService, shouldn't happen
            serviceFragment = new ServiceFragment();
            fm.beginTransaction().add(serviceFragment, Constants.SERVICEFRAGMENT_TAG).commit();
        }

        mPlayerService = serviceFragment.getPlayerService();
        Intent playIntent = new Intent(getActivity(), PlayerService.class);
        getActivity().bindService(playIntent, mConnection,
                Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT);

        if (mPlayerService == null) {
            Log.d(LOG_TAG, "found null PlayerService.");
        }
        else {
            mBound = true;
        }
        setHasOptionsMenu(true);
    }


    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "in onDestroy()");

        // Unbind from mPlayerService
        if (mBound) {
            try {
                Log.d(LOG_TAG, "unbinding mConnection");
                getActivity().unbindService(mConnection);
                mConnection = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            mBound = false;
        }
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        // Workaround for DialogFragment self-destruct per
        //     https://code.google.com/p/android/issues/detail?id=17423
         if (getDialog() != null && getRetainInstance())
            this.getDialog().setOnDismissListener(null);
        super.onDestroyView();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle trackInfoBundle;

        // Get track list from savedInstanceState or TopTracksFragment
        if (savedInstanceState != null) {
            Log.d(LOG_TAG, "in onCreateView, restoring savedInstanceState");
            topTracksArrayList = savedInstanceState.getParcelableArrayList(Constants.TRACK_INFO);
            mPosition = savedInstanceState.getInt(Constants.TOP_TRACKS_POSITION);
            newTrack = false;
        } else {
            // Get track list from TopTracksFragment, needed for new start
            trackInfoBundle = TopTracksFragment.getTrackInfo();
            topTracksArrayList = trackInfoBundle.getParcelableArrayList(Constants.TRACK_INFO);
            mPosition = trackInfoBundle.getInt(Constants.TOP_TRACKS_POSITION);
            newTrack = trackInfoBundle.getBoolean(Constants.NEW_TRACK);
        }
        // Get tracklist from PlayerService if it's playing
        if (savedInstanceState != null && !newTrack &&
                mPlayerService != null && mPlayerService.isPlaying()) {
            trackInfoBundle = mPlayerService.getTrackList();
            topTracksArrayList = trackInfoBundle.getParcelableArrayList(Constants.TRACK_INFO);
            mPosition = trackInfoBundle.getInt(Constants.TOP_TRACKS_POSITION);
        }
        trackInfo = topTracksArrayList.get(mPosition);
        playerView = inflater.inflate(R.layout.media_player, container, false);

        // Identify widgets
        playButton = (ImageButton) playerView.findViewById(R.id.play_button);
        ImageButton prevButton = (ImageButton) playerView.findViewById(R.id.previous_button);
        ImageButton nextButton = (ImageButton) playerView.findViewById(R.id.next_button);
        seekBar = (SeekBar) playerView.findViewById(R.id.seek_bar);
        pauseButtonDrawable = getResources().getDrawable(android.R.drawable.ic_media_pause);
        playButtonDrawable = getResources().getDrawable(android.R.drawable.ic_media_play);
        currentTimeView = (TextView) playerView.findViewById(R.id.current_time_textview);
        currentTimeView.setText("0:00");

        // Set up listeners
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickPrev();
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickPlay();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickNext();
            }
        });

        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                int progress;

                progress = progressValue;
                if (fromUser) {
                    seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekbar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekbar) { }
        });

        // set up spinner
        spinner = new ProgressDialog(getActivity());
        spinner.setMessage(getResources().getString(R.string.buffering));
        spinner.setIndeterminate(true);

        // Populate layout fields
        setTrackInfo(playerView, trackInfo);

        if (newTrack) {  // stop current track
            Log.d(LOG_TAG, "Stopping current track.");
            clickPlay();  // TODO: simplify?
        }

        // Update position if playing
        if (mPlayerService.isPlaying()) {
            Log.d(LOG_TAG, "mPlayerService: " + mPlayerService + " is playing");

            playButton.setImageDrawable(pauseButtonDrawable);
            setSeekBar(mPlayerService.getSeek());
            startScrubber();
        } else if (mPlayerService != null) {  // No need to wait for connection, start player
            startPlay();
        }

        return playerView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(Constants.TRACK_INFO, topTracksArrayList);
        outState.putInt(Constants.TOP_TRACKS_POSITION, mPosition);
        outState.putBoolean(Constants.SERVICE_BOUND, mBound);
    }

    private void startPlay() {
        if (mPlayerService == null) {
            Log.d(LOG_TAG, "Attempt to startPlay() with null PlayerService");
            return;
        }
        // if newTrack, kill the playing track and clear the newTrack flag
        if (newTrack) {
            mPlayerService.stopForegroundService();
            newTrack = false;
        }

        // Set new track list
        mPlayerService.setTrackList(topTracksArrayList, mPosition);
        setTopTracksPosition(mPosition);
        if (!mPlayerService.isPlaying()) {
            Log.d(LOG_TAG, "in onServiceConnected, isPlaying(): " + mPlayerService.isPlaying());
            clickPlay();
        }
    }

    // Called by the mediaplayer service when a track advances so UI updates.
    public static void updateTrackInfo(int position) {
        mPosition = position;
        setTrackInfo(playerView, topTracksArrayList.get(position));
    }

    // Set current track info in the media player
    @SuppressLint("SetTextI18n")
    private static void setTrackInfo (View playerView, ShowTopTracks trackInfo) {
        if (trackInfo != null) {
            Log.d(LOG_TAG, "Track Info: " + trackInfo.toString());

            // Populate layout fields
            TextView textView = (TextView) playerView.findViewById(R.id.artist_name_textview);
            textView.setText(trackInfo.artistName);
            textView = (TextView) playerView.findViewById(R.id.album_name_textview);
            textView.setText(trackInfo.albumName);
            textView = (TextView) playerView.findViewById(R.id.song_title_textview);
            textView.setText(trackInfo.trackName);

            // Duration of preview is always 0:30, but duration reflects full track length
            // Could just set it in media_player.xml, but leaving this here for when I get
            // full tracks working
            textView = (TextView) playerView.findViewById(R.id.end_time_textview);
            // textView.setText(millisToMinutes(trackInfo.trackLength));
            textView.setText("0:30");
            textView = (TextView) playerView.findViewById(R.id.current_time_textview);
            textView.setText("0:00");

            ImageView trackArt = (ImageView) playerView.findViewById(R.id.album_art_imageview);
            int trackArtSize = playerView.getContext().getResources()
                    .getInteger(R.integer.player_track_art_size);
            Picasso.with(playerView.getContext())
                    .load(trackInfo.trackImageUrl)
                    .resize(trackArtSize, trackArtSize)
                    .into(trackArt);
        }

        Log.d(LOG_TAG,"PlayerService.getSeek(): " + mPlayerService.getSeek());
        seekBar.setProgress(mPlayerService.getSeek());
    }

    private void clickPlay() {
        // Need to toggle Play and Pause
        if (mPlayerService == null) {
            Log.d(LOG_TAG, " clickPlay() on null mPlayerService");
        }
        else {
            if (mPlayerService.isPlaying()) {
                // change button to Play, pause player
                playButton.setImageDrawable(playButtonDrawable);
                mPlayerService.pauseTrack();
                stopScrubber();
            } else { // change button to Pause
                playButton.setImageDrawable(pauseButtonDrawable);
                startScrubber();
                if (mPlayerService.isPaused()) {  // if paused
                    mPlayerService.playTrack();
                } else if (mBound) { // Need to call prepareAsync()
                    setTrackInfo(playerView, topTracksArrayList.get(mPosition));
                    mPlayerService.startTrack();
                } else {
                    Log.d(LOG_TAG, "clickPlay() called with PlayerService unbound");
                }
            }
        }
    }

    // Track controls, specified in media_player.xml
    // List wraps, so if at first item, go to last.
    private void clickPrev() {
        if (mPosition > 0) {
            mPosition -= 1;
        } else {
            mPosition = topTracksArrayList.size() - 1;
        }
        setTopTracksPosition(mPosition);
        setTrackInfo(playerView, topTracksArrayList.get(mPosition));
        startScrubber();
        mPlayerService.prevTrack();
    }

    // The approach is to select previous track from ArrayList and regenerate fragment
    // Wrap to end if at last item
    private void clickNext() {
        if (mPosition < topTracksArrayList.size() - 1) {
            mPosition += 1;
        } else {
            mPosition = 0;
        }
        setTopTracksPosition(mPosition);
        setTrackInfo(playerView, topTracksArrayList.get(mPosition));
        startScrubber();
        mPlayerService.nextTrack();
    }

    // newPosition is from 1 to 100
    private void seekTo(int newPosition) {
        Log.d(LOG_TAG,"Calling seekTo with newPosition = " + newPosition);
        // mPlayerService.setSeek((int) trackInfo.trackLength / newPosition);
        mPlayerService.setSeek(30000 * newPosition/100);
        setSeekBar(30000 / newPosition);
    }

    // field seekbar updates from PlayerService and seekTo
    private void setSeekBar(int progress) {
        // Log.d(LOG_TAG,"mPlayerService.getSeek(): " + mPlayerService.getSeek());
        int seekPos = mPlayerService.getSeek();
        if (seekPos > 0) {
            seekBar.setProgress(progress / Constants.SCRUBBER_INTERVAL);
            currentTimeView.setText(millisToMinutes(mPlayerService.getSeek()));
        }
    }

    // Tracks the seek position on the scrubber.  Interval between updates set in milliseconds
    public void startScrubber() {
        seekTimer = new Timer();
        seekTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerMethod();
            }
        }, 0, Constants.SCRUBBER_INTERVAL);
    }

    // stops the scrubber updates
    public void stopScrubber() {
        if (seekTimer != null) {
            seekTimer.cancel();
        }
    }

    private void TimerMethod() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(getSeek);
        }
    }

    public final Runnable getSeek = new Runnable() {
        public void run() {
            setSeekBar(mPlayerService.getSeek());
        }
    };

    // start progress dialog
    public static void onStartTrack() {
        if (spinner != null) {
            spinner.show();
        }
    }

    // stop progress dialog
    public static void onStartPlay() {
        if (spinner != null) {
            spinner.dismiss();
        }
    }

    private void setTopTracksPosition(int position) {
        Intent positionIntent = new Intent();
        positionIntent.putExtra(Constants.CURRENT_TRACK_KEY, position);
        mPlayerService.sendBroadcast(positionIntent);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(LOG_TAG, "in onServiceConnected()");
            PlayerService.PlayerBinder playerBinder =
                    (PlayerService.PlayerBinder) service;

            // Get service and bind to it
            if (mPlayerService != null) {
                Log.d(LOG_TAG, "Existing mPlayerService in onConnection: " + mPlayerService.toString());
            } else {
                mPlayerService = playerBinder.getService();
                Log.d(LOG_TAG, "mPlayerService: " + mPlayerService.toString() + " connected");
            }
            mBound = true;

            startPlay();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(LOG_TAG,"in onServiceDisconnected()");
            Log.d(LOG_TAG, "mPlayerService: " + mPlayerService);
            mBound = false;
            mPlayerService = null;
            mConnection = null;
        }
    };

    // Convert milliseconds to HH:MM:SS string for duration scrubber.  Omit hours if == 0
    @SuppressLint("DefaultLocale")
    private String millisToMinutes(long millis) {
        String minutesString;

        Long hours = TimeUnit.HOURS.convert(millis, TimeUnit.MILLISECONDS);
        Long minutes = TimeUnit.MINUTES.convert(millis, TimeUnit.MILLISECONDS) % 60;
        Long seconds = TimeUnit.SECONDS.convert(millis, TimeUnit.MILLISECONDS) % 60;

        if (hours > 0) minutesString =  String.format("%02d:%02d:%02d", hours, minutes, seconds);
        else minutesString = String.format("%2d:%02d", minutes, seconds);

        return minutesString;
    }

}
