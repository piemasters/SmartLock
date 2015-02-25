package net.davidnorton.securityapp.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import net.davidnorton.securityapp.profile.Handler;

/**
 * Used to start the notification if the permanent notification
 * preference has been selected.
 *
 * @author David Norton
 */
public class AutostartService extends Service {

    /**
     * Turns the notification on is preference selected.
     *
     * @param intent Intent.
     * @param flags Data about start request.
     * @param startId The request to start.
     *
     * @return Start notification if preference is set.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        Log.i("AutostartService", "checking if notification is enabled");

        // checks if the permanent notification option is enabled
        if (pref.getBoolean("notification", false)) {
            Handler handler = new Handler(this);
            handler.updateNotification();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
