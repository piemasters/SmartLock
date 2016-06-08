package net.davidnorton.securityapp.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import net.davidnorton.securityapp.R;
import net.davidnorton.securityapp.lockscreen.LockscreenActivity;
import net.davidnorton.securityapp.profile.Handler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Enables lock screen when SCREEN_OFF or BOOT_COMPLETED
 * event is detected.
 *
 * @author David Norton
 *
 */
public class LockScreenReceiver extends BroadcastReceiver {

    private Boolean lockscreen = false;
    private Context thisContext;

    /**
     * Start the lock screen when screen is turned off or device is booted.
     *
     * @param context Context.
     * @param intent Intent.
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        thisContext = context;
        readFromJSON();

        // If SCREEN_OFF event detected, start lock screen if enabled
        if (lockscreen && intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {

            Intent lockIntent = new Intent(context, LockscreenActivity.class);
            lockIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            context.startActivity(lockIntent);
        }

        // If BOOT_COMPLETED event detected, start service and lock screen if enabled.
        else if (lockscreen && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {


            // Apply active Profile to update lock screen status.
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            if (!pref.getString("active_profile", context.getApplicationContext().getResources().getString(
                    R.string.notification_title_no_profile)).equals("None")) {
                Handler handler = new Handler(context.getApplicationContext());
                handler.applyProfileHidden(pref.getString("active_profile", context.getApplicationContext().getResources().getString(
                        R.string.notification_title_no_profile)));
            }

            Intent lockIntent = new Intent(context, LockscreenActivity.class);
            lockIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            context.startActivity(lockIntent);
        }
    }

    /**
     * Read from JSON file to determine if lock screen is enabled.
     */
    private void readFromJSON() {

        try {
            // Get JSON file.
            BufferedReader bRead = new BufferedReader(new InputStreamReader(thisContext.getApplicationContext().openFileInput("settings.json")));
            JSONObject root = new JSONObject(bRead.readLine());

            // Access settings.
            JSONObject settings = root.getJSONObject("settings");

            // Get lock screen value.
            lockscreen = settings.getBoolean("lockscreen");

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }
}
