package net.davidnorton.securityapp.trigger;

import com.google.android.gms.location.Geofence;

/**
 * Container for a simple geo-fence.
 */
public class SimpleGeofence {

	private final String mId;
	private final double mLatitude;
	private final double mLongitude;
	private final float mRadius;
	private final long mExpirationDuration;
	private final int mTransitionType;

    /**
     * Initialize geo-fence.
     *
     * @param geofenceId Request ID.
     * @param latitude Latitude of geo-fence's center.
     * @param longitude Longitude of geo-fence's center.
     * @param radius Radius of the geo-fence's area.
     * @param expiration Expiration duration.
     * @param transition Type of transition.
     */
	public SimpleGeofence(String geofenceId, double latitude, double longitude, float radius, long expiration, int transition) {

		this.mId = geofenceId;
		this.mLatitude = latitude;
		this.mLongitude = longitude;
		this.mRadius = radius;
		this.mExpirationDuration = expiration;
		this.mTransitionType = transition;
	}


	public String getId() {
		return mId;
	}
	public double getLatitude() {
		return mLatitude;
	}
	public double getLongitude() {
		return mLongitude;
	}
	public float getRadius() {
		return mRadius;
	}
	public long getExpirationDuration() {
		return mExpirationDuration;
	}
	public int getTransitionType() {
		return mTransitionType;
	}

	/**
	 * Creates a Location Services Geofence object from a SimpleGeofence.
	 * 
	 * @return A Geofence object
	 */

    /**
     * Create a Location Services Geofence object from a SimpleGeofence.
     *
     * @return A geo-fence object.
     */
	public Geofence toGeofence() {

		return new Geofence.Builder().setRequestId(getId())
				.setTransitionTypes(mTransitionType)
				.setCircularRegion(getLatitude(), getLongitude(), getRadius())
				.setExpirationDuration(mExpirationDuration).build();
	}
}