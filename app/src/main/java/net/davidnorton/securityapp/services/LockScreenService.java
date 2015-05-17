package net.davidnorton.securityapp.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

/**
 * Service that manages the lock screen.
 *
 * @author David Norton
 *
 */
public class LockScreenService extends Service {

    private BroadcastReceiver lockscreenReceiver;

    /**
     * Starts the receiver to detect a SCREEN_OFF event.
     */
    @Override
    public void onCreate() {

        super.onCreate();

        // Start receiver for SCREEN_OFF event.
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        lockscreenReceiver = new LockScreenReceiver();
        registerReceiver(lockscreenReceiver, filter);
    }

    /**
     * Unregisters the receiver when the service is stopped.
     */
    @Override
    public void onDestroy() {

        super.onDestroy();

        // When the service is stopped, unregister the receiver.
        try {
            unregisterReceiver(lockscreenReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        // Stop service running in foreground.
        stopForeground(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}