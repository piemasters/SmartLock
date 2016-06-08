package net.davidnorton.securityapp.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Starts the AutostartService to show the permanent
 * notification on device reboot.
 */
public class BootCompletedReceiver extends BroadcastReceiver {

	private final static String TAG = "BootCompletedReceiver";

    /**
     * Starts the AutostartService.
     *
     * @param context Context.
     * @param in Intent.
     */
	@Override
	public void onReceive(Context context, Intent in) {
		Log.i(TAG, "boot completed");
		
		Intent intent = new Intent(context, AutostartService.class);
		context.startService(intent);
	}
}