package com.ereinecke.spotifystreamer;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * ServiceFragment is a persistent fragment that binds to the PlayerService to keep it alive
 * through configuration changes (e.g., rotation).
 */
public class ServiceFragment extends Fragment {

    private static final String LOG_TAG = ServiceFragment.class.getSimpleName();

    private boolean mBound;
    private PlayerService mPlayerService;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        // Bind to PlayerService
        Intent playIntent = new Intent(getActivity(), PlayerService.class);
        getActivity().startService(playIntent);
        getActivity().bindService(playIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
        public void onDestroy() {
        super.onDestroy();

        // Only unbind service once app is actually finishing.
        if (getActivity().isFinishing()) {
            Log.d(LOG_TAG, "Exiting SpotifyStreamer");
            getActivity().unbindService(mConnection);
        }
        mConnection = null;

    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(LOG_TAG, "in onServiceConnected()");
            PlayerService.PlayerBinder playerBinder =
                    (PlayerService.PlayerBinder) service;
            // get service
            mPlayerService = playerBinder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(LOG_TAG,"in onServiceDisconnected()");
            mBound = false;
        }
    };
}
