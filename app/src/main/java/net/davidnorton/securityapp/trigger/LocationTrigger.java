package net.davidnorton.securityapp.trigger;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import net.davidnorton.securityapp.R;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/**
 * Used for geo-fence handling via the Google Location API.
 *
 * @author David Norton
 */
public class LocationTrigger implements ConnectionCallbacks, OnConnectionFailedListener {

	final static String TAG = "LocationTrigger";

	private Context context;

    private GoogleApiClient mGoogleApiClient;

	// Flag that indicates a request is underway.
	private boolean inProgress;

	// List of geo-fence objects.
	List<Geofence> geofenceList;
	List<String> removeList;

	// Persistent storage for geo-fences.
	private SimpleGeofenceStore geofenceStorage;

    /**
     * Set up geo-fence storage.
     *
     * @param cont Context.
     */
	public LocationTrigger(Context cont) {

        this.context = cont;

		// Instantiate a geo-fence storage area.
		geofenceStorage = new SimpleGeofenceStore(context);

		// Instantiate the current List of geo-fences.
		geofenceList = new ArrayList<>();

        mGoogleApiClient = new GoogleApiClient.Builder(this.context).addApi(LocationServices.API).build();

	}

    /**
     * Confirms Google Play services are available before making
     * a request or displays a dialog if not.
     *
     * @return True if Google Play services are available.
     */
	public boolean servicesConnected() {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        // Check Google Play services are available
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);

		// If available continue.
		if (ConnectionResult.SUCCESS == resultCode) {
			Log.d("Geo-fence Detection", "Google Play services is available.");
			return true;
	    // If not available.
		} else {

            // Create dialog with icon, title, text and confirmation button.
            AlertDialog.Builder dialog;
            if (pref.getBoolean("dark_theme", false)) {
                dialog = new AlertDialog.Builder(context, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                dialog.setIcon(R.drawable.ic_action_warning);
            } else {
                dialog = new AlertDialog.Builder(context, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                dialog.setIcon(R.drawable.ic_action_warning_light);
            }
			dialog.setTitle(context.getResources().getString(R.string.profile_alert_name_title));
			dialog.setMessage(context.getResources().getString(R.string.profile_alert_name_text));
			dialog.setNegativeButton(context.getResources().getString(R.string.profile_alert_name_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {dialog.dismiss();}
            });
			dialog.show();

			return false;
		}
	}

    /**
     * Creates a pending intent for location changes.
     *
     * @return The pending intent.
     */
	private PendingIntent getPendingIntent() {

		Log.i(TAG, "Creating pending intent.");

		// Create an explicit Intent.
		Intent intent = new Intent();
		intent.setAction("net.davidnorton.securityapp.trigger.location_change");

		return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

    /**
     * Registers a geo-fence and saves it to the store.
     *
     * @param geofence Geo-fence to be registered.
     */
	public void registerGeofence(SimpleGeofence  geofence) {

		Log.i(TAG, "Started register geo-fence.");

		geofenceList.add(geofence.toGeofence());
		geofenceStorage.setGeofence(geofence.getId(), geofence);
		refreshGeofences();
	}

	/**
	 * Unregister a geo-fence.
	 *
	 * @param id The id of the geo-fence to unregister.
	 */
	public void unregisterGeofence(String id) {

		Log.i(TAG, "Started unregister geo-fence");

		ArrayList<String> ids = new ArrayList<>();
		ids.add(id);
		removeList = ids;
		refreshGeofences();
	}

    /**
     * Connect to Google Play services if not already to refresh geo-fences.
     */
	private void refreshGeofences() {

		Log.i(TAG, "Started addGeofences");

        // Start a request to add geofences

        // If not connected to Google Play Services.
        if (!servicesConnected()) {
			Log.e(TAG, "Google Play Services not connected");
			return;
		}

        //TODO: Possible reason why geo-fence not working.
		// If a request is not already underway.
		if (!inProgress) {

			// Set that a request is underway.
			inProgress = true;

			// Request a connection to Location Services.
            mGoogleApiClient.connect();

			Log.i(TAG, "Location Client connected");

		} else {
			Log.e(TAG, "There is already a location client connected");
		}
	}

    /**
     * When connected to LocationClient add geo-fences in geofenceList
     * to geo-fences and remove those in the removeList.
     *
     * @param arg0 Arg 0.
     */
	@Override
	public void onConnected(Bundle arg0) {

		// Get the PendingIntent for the request.
        PendingIntent pendingIntent = getPendingIntent();

		// Sends request to add geo-fences.
		if (geofenceList != null && geofenceList.size() > 0) {
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, geofenceList, pendingIntent);
			Log.i(TAG, "Geofences added.");
			geofenceList = new ArrayList<>();
        // Sends request to remove geo-fences.
		} else if (removeList != null && removeList.size() > 0) {
            LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, removeList);
			Log.i(TAG, "Geofences removed");
			removeList = new ArrayList<>();
		}
	}

	/**
	 * When locationClient is disconnected, set inProgress to false and
	 * locationClient to null.
	 */
	@Override
	public void onDisconnected() {
		// Turn off the request flag.
		inProgress = false;
		// Destroy the current location client.
        mGoogleApiClient = null;
	}

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {}

}
