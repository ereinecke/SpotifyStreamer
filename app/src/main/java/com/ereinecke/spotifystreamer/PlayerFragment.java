package com.ereinecke.spotifystreamer;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
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
public class PlayerFragment extends DialogFragment {

    private static final String LOG_TAG = PlayerFragment.class.getSimpleName();
    private static ImageButton playButton;
    private static SeekBar seekBar;
    private static Drawable playButtonDrawable;
    private static Drawable pauseButtonDrawable;
    private static ShowTopTracks trackInfo;
    private static ArrayList<ShowTopTracks> topTracksArrayList;
    private static int mPosition;
    private Intent playIntent;
    private PlayerService mPlayerService;
    private View playerView;
    private boolean mBound;
    private TextView currentTimeView;
    private static ProgressDialog spinner;
    private Timer seekTimer;


    public PlayerFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        // clickPlay();
    }

    @Override
    public void onDestroy() {
        // Log.d(LOG_TAG, "in onDestroy()");
        // Unbind from mPlayerService
        if (mBound) {
            try {
                getActivity().unbindService(mConnection);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mPlayerService.stopForegroundService();
            mBound = false;
        }
        mPlayerService = null;
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle trackInfoBundle;

        if (savedInstanceState != null) {
            topTracksArrayList = savedInstanceState.getParcelableArrayList(Constants.TRACK_INFO);
            mPosition = savedInstanceState.getInt(Constants.TOP_TRACKS_POSITION);
            mBound = savedInstanceState.getBoolean(Constants.SERVICE_BOUND);
        } else {
            // Get track list
            trackInfoBundle = TopTracksFragment.getTrackInfo();
            topTracksArrayList = trackInfoBundle.getParcelableArrayList(Constants.TRACK_INFO);
            mPosition = trackInfoBundle.getInt(Constants.TOP_TRACKS_POSITION);
            trackInfo = topTracksArrayList.get(mPosition);
        }

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
                    // Log.d(LOG_TAG, "Progress from user: " + progress);
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

        // Bind service if necessary
        if (playIntent == null || !mBound) {
            playIntent = new Intent(getActivity(), PlayerService.class);
            // getActivity().startService(playIntent);
            getActivity().bindService(playIntent, mConnection, Context.BIND_AUTO_CREATE);
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

    // Set current track info in the media player
    private void setTrackInfo (View playerView, ShowTopTracks trackInfo) {
        if (trackInfo != null) {
            // Log.d(LOG_TAG, "Track Info: " + trackInfo.toString());

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
            //textView.setText(millisToMinutes(trackInfo.trackLength));
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
        seekBar.setProgress(0);
    }

    private void clickPlay() {
        // Need to toggle Play and Pause
        if (mPlayerService != null) {
            // Log.d(LOG_TAG, "in clickPlay(), mPosition: " + mPosition);
            // TopTracksFragment.setListPosition(mPosition);
            if (PlayerService.isPlaying()) {
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
                    // Log.d(LOG_TAG, "clickPlay() called with PlayerService unbound");
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
        // mPlayerService.setSeek((int) trackInfo.trackLength / newPosition);
        mPlayerService.setSeek(30000 / newPosition);
        setSeekBar(30000 / newPosition);
    }

    // field seekbar updates from PlayerService and seekTo
    private void setSeekBar(int progress) {
        int seekPos = mPlayerService.getSeek();
        if (seekPos > 0) {
            seekBar.setProgress(progress / Constants.SCRUBBER_INTERVAL);
            currentTimeView.setText(millisToMinutes(mPlayerService.getSeek()));
        }
    }

    // Tracks the seek position on the scrubber.  Interval between updates set in milliseconds
    private void startScrubber() {
        seekTimer = new Timer();
        seekTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerMethod();
            }
        }, 0, Constants.SCRUBBER_INTERVAL);
    }

    // stops the scrubber updates
    private void stopScrubber() {
        if (seekTimer != null) {
            seekTimer.cancel();
        }
    }

    private void TimerMethod() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(getSeek);
        }
    }

    public Runnable getSeek = new Runnable() {
        public void run() {
            setSeekBar(PlayerService.getSeek());
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
            // Log.d(LOG_TAG, "in onServiceConnected()");
            PlayerService.PlayerBinder playerBinder =
                    (PlayerService.PlayerBinder) service;
            // get service
            mPlayerService = playerBinder.getService();
            mBound = true;

            mPlayerService.setTrackList(topTracksArrayList, mPosition);
            setTopTracksPosition(mPosition);
            mPlayerService.playTrack();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // Log.d(LOG_TAG,"in onServiceDisconnected()");
            mBound = false;
            mPlayerService = null;
            mConnection = null;
        }
    };

    // Convert milliseconds to HH:MM:SS string for duration scrubber.  Omit hours if == 0
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
