package net.davidnorton.securityapp.lockscreen;

import android.app.Activity;
import android.os.Bundle;

/**
 * A blank activity that acts as the Home Launcher.
 * This means when the home button is selected, the
 * app will return the user to the Android default
 * home, rather than the lock screen.
 *
 * @author David Norton
 */
public class SmartLockLauncher extends Activity {

    /**
     * Create blank activity.
     *
     * @param savedInstanceState Saves current state of application to be referred back to.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // End activity, returning to Android default home screen.
        finish();
    }
}
