package net.davidnorton.securityapp.trigger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import net.davidnorton.securityapp.R;

/**
 * Allows user to set geo-fences.
 *
 * @author David Norton
 */
public class MapViewActivity extends Activity implements GoogleMap.OnMapLongClickListener, OnClickListener, OnMapReadyCallback {

    // Indicates if there are unsaved changes in the preferences.
    private static boolean preferencesChanged = false;

    // Geo-fencing preferences.
	private GoogleMap geoMap;
	private LatLng point;
	private int radius;
	private boolean normView = true;

    // Preference Settings.
    ColorFilter filter;

    /**
     * Creates the geo-fence preference menu.
     *
     * @param savedInstanceState Saves current state of application to be referred back to.
     */
	@Override
	protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        // Sets the Theme
        changeTheme(pref);

		super.onCreate(savedInstanceState);

        // Apply title and back button.
        if(getActionBar() != null) {
            getActionBar().setTitle(R.string.trigger_location_edit_title);
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Checks Location Permissions
        int coarseStatus = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int fineStatus = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);

        if (coarseStatus != PackageManager.PERMISSION_GRANTED && fineStatus != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, 255);
        }
        // Create map screen.
        setContentView(R.layout.activity_trigger_map);

        // Get Map
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
	}

    /**
     * Sets up map when permission is given.
     *
     * @param requestCode The requested permission code.
     * @param permissions List of permissions.
     * @param grantResults The granted permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull  String[] permissions, @NonNull  int[] grantResults) {
        setUpMap();
    }

    /**
     * Initialises the map once ready.
     *
     * @param map The Google Map.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        geoMap = map;

        // Checks Location Permissions
        int coarseStatus = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int fineStatus = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);

        // Set Up Map
        if (coarseStatus == PackageManager.PERMISSION_GRANTED && fineStatus == PackageManager.PERMISSION_GRANTED) {
            setUpMap();
        }
    }

    public void setUpMap(){

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        // Checks Location Permissions
        int coarseStatus = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int fineStatus = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);

        // If map has been obtained, manipulate.
        if (geoMap != null) {

            geoMap.setPadding(0, 100, 0, 0);
            geoMap.setMyLocationEnabled(true);
            geoMap.setOnMapLongClickListener(this);

            // Set clear button.
            Button clear = (Button) findViewById(R.id.clear_map_button);
            clear.setOnClickListener(this);

            // Set radius input area.
            EditText editRadius = (EditText) findViewById(R.id.radius_selection);
            editRadius.addTextChangedListener(new TextWatcher() {

                /**
                 * Change radius to match user input.
                 *
                 * @param s User input.
                 * @param start Start.
                 * @param before Before count.
                 * @param count Count.
                 */
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    preferencesChanged = true;

                    // If unset, default to 50, else use the value.
                    if (s.length() == 0) {
                        radius = 50;
                    } else {
                        radius = Integer.parseInt(s.toString());
                    }

                    // If a point and a radius is set.
                    if (point != null && radius > 0) {

                        geoMap.clear();

                        // Set and display visual for selected location.
                        geoMap.addMarker(new MarkerOptions().position(point));
                        geoMap.addCircle(new CircleOptions().radius(radius).center(point).fillColor(0x5533B5E5).strokeColor(0xEE33B5E5).strokeWidth(2));
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            });

            // If a geo-fence has already been defined.
            if (pref.getInt("geofence_radius", -1) > 0) {

                preferencesChanged = false;

                // Get saved geo-fence details.
                point = new LatLng(pref.getFloat("geofence_lat", 0), pref.getFloat("geofence_lng", 0));
                radius = pref.getInt("geofence_radius", 50);
                editRadius.setText(String.valueOf(radius));

                geoMap.clear();

                // Set and display visual for selected location.
                geoMap.addMarker(new MarkerOptions().position(point));
                if (radius > 0) {
                    geoMap.addCircle(new CircleOptions().radius(radius).center(point).fillColor(0x5533B5E5).strokeColor(0xEE33B5E5).strokeWidth(2));
                }

                // Set camera over drawn geo-fence.
                geoMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(point.latitude, point.longitude), 15));
                CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(point.latitude, point.longitude)).zoom(15).build();
                geoMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                // Create a new geo-fence.
            } else {

                if (coarseStatus == PackageManager.PERMISSION_GRANTED && fineStatus == PackageManager.PERMISSION_GRANTED) {

                    // Get location.
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    Criteria criteria = new Criteria();
                    Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));

                    // If location is set.
                    if (location != null) {

                        // Set camera over drawn geo-fence.
                        geoMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
                        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(location.getLatitude(), location.getLongitude())).zoom(15).build();
                        geoMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    }
                }
            }
        }


    }

    /**
     * Creates the save and cancel options on the action bar.
     *
     * @param menu Menu.
     * @return true.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.trigger_map_edit, menu);
        return true;
    }

    /**
     * Toggle app colour theme.
     *
     * @param pref Container for stored shared preferences.
     */
    private void changeTheme(SharedPreferences pref) {

        Context context = getApplicationContext();

        // Set dark theme if selected
        if (pref.getBoolean("dark_theme", false)) {
            setTheme(R.style.AppThemeDark);
            // Set primary colour for icons.
            filter = new LightingColorFilter( ContextCompat.getColor(context, R.color.dark_primary_dark), ContextCompat.getColor(context, R.color.dark_primary_dark));

            // Set light theme if selected
        } else {
            setTheme(R.style.AppThemeLight);
            // Set primary colour for icons.
            filter = new LightingColorFilter( ContextCompat.getColor(context, R.color.primary_dark), ContextCompat.getColor(context, R.color.primary_dark));
        }
    }

    /**
     * Displays the sat/normal option depending on what is currently active.
     *
     * @param menu Menu.
     *
     * @return Correct menu option.
     */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		MenuItem itemSat = menu.findItem(R.id.map_change_sat);
		MenuItem itemNormal = menu.findItem(R.id.map_change_norm);

		// If current view is normal, display sat option.
		if (normView) {
            itemSat.setVisible(true);
            itemNormal.setVisible(false);
        // If current view is sat, display normal option.
        } else {
            itemSat.setVisible(false);
            itemNormal.setVisible(true);
		}

		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */

    /**
     * Adds functionality to the save and cancel menu buttons.
     *
     * @param item Menu item selected.
     *
     * @return Item selected effect.
     */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

        // If home was selected.
		if (item.getItemId() == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
        // If cancel was selected.
		} else if (item.getItemId() == R.id.cancel_location) {
            this.finish();
        // If change to satellite map was selected.
        } else if (item.getItemId() == R.id.map_change_sat) {
            geoMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            normView = true;
        // If change to normal map was selected.
        } else if (item.getItemId() == R.id.map_change_norm) {
            geoMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            normView = false;
        // If save was selected.
        } else if (item.getItemId() == R.id.save_location) {

			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

			// If no geo-fence defined.
			if (point == null) {
				pref.edit().putFloat("geofence_lat", 0F).apply();
				pref.edit().putFloat("geofence_lng", 0F).apply();
				pref.edit().putInt("geofence_radius", -1).apply();
			// Define new geo-fence.
            } else {
				pref.edit().putFloat("geofence_lat", (float) point.latitude).apply();
				pref.edit().putFloat("geofence_lng", (float) point.longitude).apply();
				pref.edit().putInt("geofence_radius", radius).apply();
			}
			this.finish();
		}

		return super.onOptionsItemSelected(item);
	}

    /**
     * If the back button is selected, display a dialog if there are any unsaved changes.
     */
    @Override
    public void onBackPressed() {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        // If any preferences were changed.
        if (preferencesChanged) {

            // Create dialog with icon, title, text and confirmation button.
            AlertDialog.Builder dialog;
            if (pref.getBoolean("dark_theme", false)) {
                dialog = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                dialog.setIcon(R.drawable.ic_action_warning);
            } else {
                dialog = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                dialog.setIcon(R.drawable.ic_action_warning_light);
            }
            dialog.setTitle(getResources().getString(R.string.trigger_alert_discard_title));
            dialog.setMessage(getResources().getString(R.string.trigger_alert_discard_text));
            // Exit the edit profile activity.
            dialog.setPositiveButton(getResources().getString(R.string.trigger_alert_discard_yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            // Dismiss the dialog and resume the profile activity.
            dialog.setNegativeButton( getResources().getString(R.string.trigger_alert_discard_no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            dialog.show();

        // If no preferences were changed then exit the edit profile activity.
        } else {
            finish();
        }
    }

    /**
     * Clears the set geo-fence.
     *
     * @param v View.
     */
	@Override
	public void onClick(View v) {

		// If clear button selected.
		if (v.equals(findViewById(R.id.clear_map_button))) {

            // Clear map.
            geoMap.clear();
			point = null;
			radius = -1;

            // Set radius value back to default.
			EditText editRadius = (EditText) findViewById(R.id.radius_selection);
			editRadius.setText(R.string.trigger_pref_map_radius_default);

			preferencesChanged = true;
		}
	}

    /**
     * Draws the geo-fence on the map where selected using the radius in the text field.
     *
     * @param latLngPoint Point selected on map.
     */
    @Override
    public void onMapLongClick(LatLng latLngPoint) {

        point = latLngPoint;
        EditText editRadius = (EditText) findViewById(R.id.radius_selection);

        // If text field is empty use the default value 50.
        if (editRadius.getText().toString().isEmpty()) {
            radius = 50;
        // Otherwise use the radius defined.
        } else {
            radius = Integer.parseInt(editRadius.getText().toString());
        }

        // If a map is initialized.
        if (geoMap != null) {

            geoMap.clear();

            // Draw the marker.
            geoMap.addMarker(new MarkerOptions().position(point));

            // Draw the circle.
            if (radius > 0) {
                geoMap.addCircle(new CircleOptions().radius(radius).center(point).fillColor(0x5533B5E5).strokeColor(0xEE33B5E5).strokeWidth(2));
            }
        }

        preferencesChanged = true;
    }
}
