package com.ereinecke.spotifystreamer;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * ServiceFragment is a persistent fragment that binds to the PlayerService to keep it alive
 * through configuration changes (e.g., rotation).
 */
public class ServiceFragment extends Fragment {

    private static final String LOG_TAG = ServiceFragment.class.getSimpleName();

    private boolean mBound;
    private PlayerService mPlayerService;

    public ServiceFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Important: this keeps this non-ui fragment alive through display reconfigurations;
        // PlayerService is bound here
        setRetainInstance(true);

        if (!mBound || mPlayerService == null) {
            // Start and bind to PlayerService
            Intent playIntent = new Intent(getActivity(), PlayerService.class);
            getActivity().startService(playIntent);
            getActivity().bindService(playIntent, mConnection,
                    Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT);
        }
    }

    @Override
    public void onDestroy() {

        try {
            getActivity().unbindService(mConnection);
        } catch (Exception e) {
            Log.d(LOG_TAG, "Exception unbinding from PlayerService: " + e.getMessage());
        } finally {
            mConnection = null;
            super.onDestroy();
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            PlayerService.PlayerBinder playerBinder =
                    (PlayerService.PlayerBinder) service;
            // get service
            mPlayerService = playerBinder.getService();
            mBound = true;
            Log.d(LOG_TAG, "in onServiceConnected(), PlayerService: " + mPlayerService.toString());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(LOG_TAG, "in onServiceDisconnected()");
            mPlayerService = null;
            mBound = false;
        }
    };

    public PlayerService getPlayerService() {
        if (mPlayerService == null) {
            Log.d(LOG_TAG, "PlayerService is null.");
        } else {
            Log.d(LOG_TAG, "Returning PlayerService " + mPlayerService.toString());
        }
        return mPlayerService;
    }
}
