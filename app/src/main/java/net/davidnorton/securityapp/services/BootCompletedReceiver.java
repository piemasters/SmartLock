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

    final static String TAG = "BootCompletedReceiver";

	@Override
	public void onReceive(Context _context, Intent _intent) {
		Log.i(TAG, "boot completed");
		
		Intent intent = new Intent(_context, AutostartService.class);
		_context.startService(intent);
	}
}