package net.davidnorton.securityapp.trigger;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import java.util.List;

/**
 * Stores geo-fences in the shared preferences.
 * 
 * @author David Norton
 * 
 */
public class SimpleGeofenceStore {

	final static String TAG = "SimpleGeofenceStore";

	// Keys for flattened geo-fences stored in SharedPreferences.
	public static final String KEY_LATITUDE = "net.davidnorton.securityapp.geofence.KEY_LATITUDE";
	public static final String KEY_LONGITUDE = "net.davidnorton.securityapp.geofence.KEY_LONGITUDE";
	public static final String KEY_RADIUS = "net.davidnorton.securityapp.geofence.KEY_RADIUS";
	public static final String KEY_EXPIRATION_DURATION = "net.davidnorton.securityapp.geofence.KEY_EXPIRATION_DURATION";
	public static final String KEY_TRANSITION_TYPE = "net.davidnorton.securityapp.geofence.KEY_TRANSITION_TYPE";

	// The prefix for flattened geofence keys
	public static final String KEY_PREFIX = "net.davidnorton.securityapp.geofence.KEY";

	// Invalid values to test geo-fence storage when retrieving geo-fences.
	public static final long INVALID_LONG_VALUE = -999l;
	public static final float INVALID_FLOAT_VALUE = -999.0f;
	public static final int INVALID_INT_VALUE = -999;

	// The SharedPreferences object that stores geo-fences.
	private final SharedPreferences mPrefs;

	// SharedPreferences name.
	private static final String SHARED_PREFERENCES = "geofences";

	// Create the SharedPreferences storage with private access only.
	public SimpleGeofenceStore(Context context) {
		mPrefs = context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
	}

    /**
     * Returns a stored geo-fence by its ID, or returns null if not found.
     *
     * @param id  The ID of a stored geofence.
     *
     * @return A geo-fence defined by center and radius.
     */
	public SimpleGeofence getGeofence(String id) {

        // Gets latitude by ID, or invalid value if it doesn't exist.
		double lat = mPrefs.getFloat(getGeofenceFieldKey(id, KEY_LATITUDE), INVALID_FLOAT_VALUE);

        // Gets longitude by ID, or invalid value if it doesn't exist.
		double lng = mPrefs.getFloat(getGeofenceFieldKey(id, KEY_LONGITUDE), INVALID_FLOAT_VALUE);

        // Gets radius by ID, or invalid value if it doesn't exist.
		float radius = mPrefs.getFloat(getGeofenceFieldKey(id, KEY_RADIUS), INVALID_FLOAT_VALUE);

        // Gets expiration by ID, or invalid value if it doesn't exist.
		long expirationDuration = mPrefs.getLong(getGeofenceFieldKey(id, KEY_EXPIRATION_DURATION), INVALID_LONG_VALUE);

        // Gets transition type by ID, or invalid value if it doesn't exist.
		int transitionType = mPrefs.getInt(getGeofenceFieldKey(id, KEY_TRANSITION_TYPE), INVALID_INT_VALUE);

		// If no incorrect values return geo-fence object.
		if (lat != GeofenceUtils.INVALID_FLOAT_VALUE
				&& lng != GeofenceUtils.INVALID_FLOAT_VALUE
				&& radius != GeofenceUtils.INVALID_FLOAT_VALUE
				&& expirationDuration != GeofenceUtils.INVALID_LONG_VALUE
				&& transitionType != GeofenceUtils.INVALID_INT_VALUE) {

			return new SimpleGeofence(id, lat, lng, radius, expirationDuration, transitionType);
		} else {
			return null;
		}
	}

    /**
     * Saves the geo-fence in shared preferences.
     *
     * @param id The ID of a Geofence object
     * @param geofence The SimpleGeofence containing the values to save in SharedPreferences.
     */
	public void setGeofence(String id, SimpleGeofence geofence) {

		Editor editor = mPrefs.edit();

		// Write the geo-fence values to SharedPreferences.
		editor.putFloat(getGeofenceFieldKey(id, KEY_LATITUDE), (float) geofence.getLatitude());
		editor.putFloat(getGeofenceFieldKey(id, KEY_LONGITUDE), (float) geofence.getLongitude());
		editor.putFloat(getGeofenceFieldKey(id, KEY_RADIUS), geofence.getRadius());
		editor.putLong(getGeofenceFieldKey(id, KEY_EXPIRATION_DURATION), geofence.getExpirationDuration());
		editor.putInt(getGeofenceFieldKey(id, KEY_TRANSITION_TYPE), geofence.getTransitionType());

		// Commit the changes
		editor.apply();
		Log.i(TAG, "saved simple geofence");
	}

    /**
     * Remove a geo-fence object by removing its keys.
     *
     * @param id The ID of a stored geofence.
     */
    public void clearGeofence(String id) {

        Editor editor = mPrefs.edit();
        editor.remove(getGeofenceFieldKey(id, KEY_LATITUDE));
        editor.remove(getGeofenceFieldKey(id, KEY_LONGITUDE));
        editor.remove(getGeofenceFieldKey(id, KEY_RADIUS));
        editor.remove(getGeofenceFieldKey(id, KEY_EXPIRATION_DURATION));
        editor.remove(getGeofenceFieldKey(id, KEY_TRANSITION_TYPE));
        editor.apply();

        Log.i(TAG, "cleared simple geofence");
    }

    /**
     * Saves a list of simple geo-fences to shared preferences.
     *
     * @param geofences The list of simple geo-fences to save.
     */
	public void setGeofenceList(List<SimpleGeofence> geofences) {

        if (geofences != null) {

			for (int i = 0; i < geofences.size(); i++) {
				setGeofence(geofences.get(i).getId(), geofences.get(i));
			}
		}
		Log.i(TAG, "saved list of simple geofences");
	}

    /**
     * Clears a list of simple geo-fences.
     *
     * @param ids IDs of the list of geo-fences to clear.
     */
	public void clearGeofenceList(String[] ids) {

        if (ids != null) {
            for (String id : ids) {
                clearGeofence(id);
            }
		}
		Log.i(TAG, "cleared list of simple geofences");
	}

    /**
     * Given a geo-fences ID and field name (e.g. KEY_LATITUDE), return
     * key name of the object's value in SharedPreferences.
     *
     * @param id The ID of a stored geo-fence.
     * @param fieldName The field represented by the key.
     *
     * @return The key name of a value in SharedPreferences.
     */
	private String getGeofenceFieldKey(String id, String fieldName) {
		return KEY_PREFIX + "_" + id + "_" + fieldName;
	}
}
