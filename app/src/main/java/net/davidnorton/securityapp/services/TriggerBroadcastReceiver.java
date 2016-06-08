package net.davidnorton.securityapp.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.Calendar;
import java.util.List;


/**
 * Receives broadcasts and sets the variables in the trigger service.
 * 
 * @author David Norton
 *
 */
public class TriggerBroadcastReceiver extends BroadcastReceiver {

    private final static String TAG = "TriggerReceiver";

    private final TriggerService triggerservice;
	
	TriggerBroadcastReceiver(TriggerService service){
		triggerservice = service;
	}

    /**
     * Receives the broadcasts registered in the constructor and sets the
     * information in the TriggerService.
     *
     * @param context Context.
     * @param intent Intent.
     */
	@Override
	public void onReceive(Context context, Intent intent) {
		
        Log.i(TAG, "Broadcast received: " + intent.getAction());

        // Set time.
        if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {

            int h = Integer.parseInt(String.valueOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));
            int m = Integer.parseInt(String.valueOf(Calendar.getInstance().get(Calendar.MINUTE)));
            triggerservice.setTime(h, m);
            Log.i(TAG, "Time: " + h + ":" + m);
        }

		// Set headphones state.
        if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)){

            int state = intent.getIntExtra("state", -1);

            switch (state) {
            case 0:
                triggerservice.setHeadphones(false);
                Log.i(TAG, "Headset unplugged");
                break;
            case 1:
                triggerservice.setHeadphones(true);
                Log.i(TAG, "Headset plugged");
                break;
            }
        }

        // Set battery charging state.
		if(intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)){
        	triggerservice.setBatteryCharging(true);
        } else if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)){
        	triggerservice.setBatteryCharging(false);
        }

        // Set battery level state.
		if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)){

			int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
			float batteryLevel = level / (float)scale;

			batteryLevel = batteryLevel * 100;
			triggerservice.setBatteryLevel((int)batteryLevel);
		}

        // Refresh list of triggers.
		if(intent.getAction().equals("net.davidnorton.securityapp.trigger.refresh")){
			triggerservice.refreshTriggers();
		}

        // Clear currently triggered geo-fences.
		if(intent.getAction().equals("net.davidnorton.securityapp.trigger.clearGeofences")){
			triggerservice.clearGeofences();
		}

        // Set location change.
		if(intent.getAction().equals("net.davidnorton.securityapp.trigger.location_change")){

            Log.i(TAG, "Location change detected");

            GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

            // Check and log errors.
            if (geofencingEvent.hasError()) {

                int errorCode = geofencingEvent.getErrorCode();
                Log.e("ReceiveTransition", "Location Services error: " + Integer.toString(errorCode));

            // If no errors, get transition type and ID/s of the geo-fence/s that triggered the transition.
            } else {

                // Get transition.
                int transition = geofencingEvent.getGeofenceTransition();

                // If transition is an enter or exit event.
                if ((transition == Geofence.GEOFENCE_TRANSITION_ENTER) || (transition == Geofence.GEOFENCE_TRANSITION_EXIT)) {

                    // Get list of geo-fence IDs that were triggered.
                    List<Geofence> triggerList = geofencingEvent.getTriggeringGeofences();
                    String[] triggerIds = new String[triggerList.size()];

                    // Store each ID.
                    for (int i = 0; i < triggerIds.length; i++) {
                        triggerIds[i] = triggerList.get(i).getRequestId();
                        Log.i(TAG, "matching geofence: " + triggerIds[i]);
                    }

                    triggerservice.setGeofences(triggerIds);

				// An invalid transition occurred.
				} else {
					Log.e("ReceiveTransition", "Geofence transition error: " + Integer.toString(transition));
				}
			}
		}
	}
}
