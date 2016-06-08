package net.davidnorton.securityapp.trigger;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.location.Geofence;

import net.davidnorton.securityapp.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**
 * Activity used to edit the settings of a Profile.
 * 
 * @author David Norton
 * 
 */
public class TriggerEditActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	// Indicates if there are unsaved changes in the preferences.
    private static boolean preferencesChanged = false;
    private static CharSequence[] profileArray;

    // Store original profile name before renamed so it can be deleted.
	private static String previousName;

    // Preference Settings.
    private ColorFilter filter;

     /**
     * Sets up the actionbar and main preference page.
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
		getActionBar().setTitle(R.string.trigger_edit_title);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // Create preference screen.
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();

        // Update Profile list.
        refreshProfileArray();
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
        menuInflater.inflate(R.menu.trigger_edit, menu);
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
     * Creates the preference page.
     */
    public static class MyPreferenceFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

        /**
         * Creates the preference page, listing each preference with its selected option.
         *
         * @param savedInstanceState Saves current state of application to be referred back to.
         */
        @Override
        public void onCreate(final Bundle savedInstanceState) {

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            previousName = pref.getString("name_trigger", "default name");
            pref.registerOnSharedPreferenceChangeListener(this);
            super.onCreate(savedInstanceState);

            // Add preference pages.
            addPreferencesFromResource(R.xml.pref_trigger_edit_general);
            bindPreferenceSummaryToValue(findPreference("name_trigger"));
            bindPreferenceSummaryToValue(findPreference("profile"));
            bindPreferenceSummaryToValue(findPreference("priority"));

            // Update Profile list.
            ListPreference lp = (ListPreference) findPreference("profile");
            lp.setEntries(profileArray);
            lp.setEntryValues(profileArray);
            bindPreferenceSummaryToValue(findPreference("profile"));
            addPreferencesFromResource(R.xml.pref_trigger_edit_location);
            bindPreferenceSummaryToValue(findPreference("location"));
            addPreferencesFromResource(R.xml.pref_trigger_edit_time);
            bindPreferenceSummaryToValue(findPreference("start_time"));
            bindPreferenceSummaryToValue(findPreference("end_time"));
            //bindPreferenceSummaryToValue(findPreference("weekdays"));
            addPreferencesFromResource(R.xml.pref_trigger_edit_battery);
            bindPreferenceSummaryToValue(findPreference("battery_state"));
            addPreferencesFromResource(R.xml.pref_trigger_edit_headphone);
            bindPreferenceSummaryToValue(findPreference("headphone"));

            // Bind the summary to the location preference.
            if (pref.getInt("geofence_radius", 50) > 0) {
                findPreference("location").setSummary(
                                getString(R.string.trigger_pref_location_lat) + ": "
                                + pref.getFloat("geofence_lat", 0F) + "\u00B0, "
                                + getString(R.string.trigger_pref_location_lng) + ": "
                                + pref.getFloat("geofence_lng", 0F) + "\u00B0, "
                                + getString(R.string.trigger_pref_location_radius) + ": "
                                + pref.getInt("geofence_radius", 50) + "m");
            } else {
                findPreference("location").setSummary(R.string.ignored);
            }

            // Bind the summary to the time preference.
            if (pref.getString("start_time", getString(R.string.ignored)).equals(getString(R.string.ignored))) {
                findPreference("end_time").setEnabled(false);
                pref.edit().putString("end_time", getString(R.string.ignored)).apply();
            }

            // Disables and enables the start battery level.
            if (!pref.getBoolean("battery_level_check", false)) {
                pref.edit().putInt("battery_start_level", -1).apply();
                findPreference("battery_start_level").setEnabled(false);
                findPreference("battery_start_level").setSummary(getString(R.string.ignored));
            } else if (pref.getBoolean("battery_level_check", false)) {
                findPreference("battery_start_level").setEnabled(true);
            }

            // Bind the summary to the battery level preference.
            if (pref.getInt("battery_start_level", -1) == -1) {
                findPreference("battery_end_level").setEnabled(false);
                pref.edit().putInt("battery_end_level", -1).apply();
            }

            // Bind the summary to the weekdays preference.
            int size = pref.getStringSet("weekdays", null).size();

            // If no days selected set ignored.
            if (pref.getStringSet("weekdays", null).isEmpty()) {
                findPreference("weekdays").setSummary(R.string.trigger_pref_weekday_none);
            // If all days selected set every day.
            } else if (size == 7) {
                findPreference("weekdays").setSummary(R.string.trigger_pref_weekday_all);
            // If week days selected set weekdays.
            } else if (size == 5 &&
                    pref.getStringSet("weekdays", null).contains("1") &&
                    pref.getStringSet("weekdays", null).contains("2") &&
                    pref.getStringSet("weekdays", null).contains("3") &&
                    pref.getStringSet("weekdays", null).contains("4") &&
                    pref.getStringSet("weekdays", null).contains("5")) {
                findPreference("weekdays").setSummary(R.string.trigger_pref_weekday_weekdays);
            // If week end selected set weekend.
            } else if (size == 2 && pref.getStringSet("weekdays", null).contains("6") &&
                    pref.getStringSet("weekdays", null).contains("7")) {
                findPreference("weekdays").setSummary(R.string.trigger_pref_weekday_weekend);
            // Otherwise list days.
            } else {
                StringBuilder summary = new StringBuilder();
                int i = 1;

                // Monday.
                if ((pref.getStringSet("weekdays", null).contains("1"))) {
                    summary.append(getResources().getString(R.string.trigger_pref_mon));
                    if (i < size - 1) {
                        summary.append(", ");
                        i++;
                    } else if (i == size - 1) {
                        summary.append(" ").append(getResources().getString(R.string.trigger_pref_and)).append(" ");
                        i++;
                    }
                }

                // Tuesday.
                if ((pref.getStringSet("weekdays", null).contains("2"))) {
                    summary.append(getResources().getString(R.string.trigger_pref_tue));
                    if (i < size - 1) {
                        summary.append(", ");
                        i++;
                    } else if (i == size - 1) {
                        summary.append(" ").append(getResources().getString(R.string.trigger_pref_and)).append(" ");
                        i++;
                    }
                }

                // Wednesday.
                if ((pref.getStringSet("weekdays", null).contains("3"))) {
                    summary.append(getResources().getString(R.string.trigger_pref_wed));
                    if (i < size - 1) {
                        summary.append(", ");
                        i++;
                    } else if (i == size - 1) {
                        summary.append(" ").append(getResources().getString(R.string.trigger_pref_and)).append(" ");
                        i++;
                    }
                }

                // Thursday.
                if ((pref.getStringSet("weekdays", null).contains("4"))) {
                    summary.append(getResources().getString(R.string.trigger_pref_thur));
                    if (i < size - 1) {
                        summary.append(", ");
                        i++;
                    } else if (i == size - 1) {
                        summary.append(" ").append(getResources().getString(R.string.trigger_pref_and)).append(" ");
                        i++;
                    }
                }

                // Friday.
                if ((pref.getStringSet("weekdays", null).contains("5"))) {
                    summary.append(getResources().getString(R.string.trigger_pref_fri));
                    if (i < size - 1) {
                        summary.append(", ");
                        i++;
                    } else if (i == size - 1) {
                        summary.append(" ").append(getResources().getString(R.string.trigger_pref_and)).append(" ");
                        i++;
                    }
                }

                // Saturday.
                if ((pref.getStringSet("weekdays", null).contains("6"))) {
                    summary.append(getResources().getString(R.string.trigger_pref_sat));
                    if (i == size - 1) {
                        summary.append(" ").append(getResources().getString(R.string.trigger_pref_and)).append(" ");
                        // i++;
                    }
                }

                // Sunday.
                if ((pref.getStringSet("weekdays", null).contains("7"))) {
                    summary.append(getResources().getString(R.string.trigger_pref_sun));
                }

                findPreference("weekdays").setSummary(summary.toString());
            }
        }

        /**
         * Listens for a preference change.
         *
         * @param pref SharedPreference.
         * @param key Key.
         */
        @Override
        public void onSharedPreferenceChanged(SharedPreferences pref, String key) {

            // Checks fragment is attached to the activity (to prevent crash.
            if (isAdded()) {

                preferencesChanged = true;

                // Disables and enables the end time depending on the start time.
                if (key.equals("start_time") && pref.getString("start_time", getString(R.string.ignored)).equals(getString(R.string.ignored))) {
                    pref.edit().putString("end_time", getString(R.string.ignored)).apply();
                    findPreference("end_time").setEnabled(false);
                    findPreference("end_time").setSummary(getString(R.string.ignored));
                } else if (key.equals("start_time") && !pref.getString("start_time", getString(R.string.ignored)).equals(getString(R.string.ignored))) {
                    findPreference("end_time").setEnabled(true);
                }

                // Disables and enables the start battery level.
                if (!pref.getBoolean("battery_level_check", false)) {
                    pref.edit().putInt("battery_start_level", -1).apply();
                    findPreference("battery_start_level").setEnabled(false);
                    //findPreference("battery_start_level").setSummary(getString(R.string.ignored));
                } else if (pref.getBoolean("battery_level_check", false)) {
                    findPreference("battery_start_level").setEnabled(true);
                }

                // Disables and enables the end battery level depending on the battery start level.
                if (key.equals("battery_start_level") && pref.getInt("battery_start_level", -1) == -1) {
                    pref.edit().putInt("battery_end_level", -1).apply();
                    findPreference("battery_end_level").setEnabled(false);
                    //findPreference("battery_end_level").setSummary(getString(R.string.ignored));
                } else if (key.equals("battery_start_level") && pref.getInt("battery_start_level", -1) != -1) {
                    findPreference("battery_end_level").setEnabled(true);
                }

                // Binds the summary of the location.
                if (key.equals("geofence_lat") || key.equals("geofence_lng") || key.equals("geofence_radius")) {

                    if (pref.getInt("geofence_radius", 50) > 0) {
                        findPreference("location").setSummary(
                                getString(R.string.trigger_pref_location_lat) + ": "
                                        + pref.getFloat("geofence_lat", 0F) + "\u00B0, "
                                        + getString(R.string.trigger_pref_location_lng) + ": "
                                        + pref.getFloat("geofence_lng", 0F) + "\u00B0, "
                                        + getString(R.string.trigger_pref_location_radius) + ": "
                                        + pref.getInt("geofence_radius", 50) + "m");
                    } else {
                        findPreference("location").setSummary(R.string.ignored);
                    }
                }

                // Binds the summary of the weekday.
                if (key.equals("weekdays")) {

                    int size = pref.getStringSet("weekdays", null).size();

                    // If no days selected set ignored.
                    if (pref.getStringSet("weekdays", null).isEmpty()) {
                        findPreference("weekdays").setSummary(R.string.trigger_pref_weekday_none);
                        // If all days selected set every day.
                    } else if (size == 7) {
                        findPreference("weekdays").setSummary(R.string.trigger_pref_weekday_all);
                        // If week days selected set weekdays.
                    } else if (size == 5 &&
                            pref.getStringSet("weekdays", null).contains("1") &&
                            pref.getStringSet("weekdays", null).contains("2") &&
                            pref.getStringSet("weekdays", null).contains("3") &&
                            pref.getStringSet("weekdays", null).contains("4") &&
                            pref.getStringSet("weekdays", null).contains("5")) {
                        findPreference("weekdays").setSummary(R.string.trigger_pref_weekday_weekdays);
                        // If week end selected set weekend.
                    } else if (size == 2 && pref.getStringSet("weekdays", null).contains("6") &&
                            pref.getStringSet("weekdays", null).contains("7")) {
                        findPreference("weekdays").setSummary(R.string.trigger_pref_weekday_weekend);
                        // Otherwise list days.
                    } else {
                        StringBuilder summary = new StringBuilder();
                        int i = 1;

                        // Monday.
                        if ((pref.getStringSet("weekdays", null).contains("1"))) {
                            summary.append(getResources().getString(R.string.trigger_pref_mon));
                            if (i < size - 1) {
                                summary.append(", ");
                                i++;
                            } else if (i == size - 1) {
                                summary.append(" ").append(getResources().getString(R.string.trigger_pref_and)).append(" ");
                                i++;
                            }
                        }

                        // Tuesday.
                        if ((pref.getStringSet("weekdays", null).contains("2"))) {
                            summary.append(getResources().getString(R.string.trigger_pref_tue));
                            if (i < size - 1) {
                                summary.append(", ");
                                i++;
                            } else if (i == size - 1) {
                                summary.append(" ").append(getResources().getString(R.string.trigger_pref_and)).append(" ");
                                i++;
                            }
                        }

                        // Wednesday.
                        if ((pref.getStringSet("weekdays", null).contains("3"))) {
                            summary.append(getResources().getString(R.string.trigger_pref_wed));
                            if (i < size - 1) {
                                summary.append(", ");
                                i++;
                            } else if (i == size - 1) {
                                summary.append(" ").append(getResources().getString(R.string.trigger_pref_and)).append(" ");
                                i++;
                            }
                        }

                        // Thursday.
                        if ((pref.getStringSet("weekdays", null).contains("4"))) {
                            summary.append(getResources().getString(R.string.trigger_pref_thur));
                            if (i < size - 1) {
                                summary.append(", ");
                                i++;
                            } else if (i == size - 1) {
                                summary.append(" ").append(getResources().getString(R.string.trigger_pref_and)).append(" ");
                                i++;
                            }
                        }

                        // Friday.
                        if ((pref.getStringSet("weekdays", null).contains("5"))) {
                            summary.append(getResources().getString(R.string.trigger_pref_fri));
                            if (i < size - 1) {
                                summary.append(", ");
                                i++;
                            } else if (i == size - 1) {
                                summary.append(" ").append(getResources().getString(R.string.trigger_pref_and)).append(" ");
                                i++;
                            }
                        }

                        // Saturday.
                        if ((pref.getStringSet("weekdays", null).contains("6"))) {
                            summary.append(getResources().getString(R.string.trigger_pref_sat));
                            if (i == size - 1) {
                                summary.append(" ").append(getResources().getString(R.string.trigger_pref_and)).append(" ");
                                // i++;
                            }
                        }

                        // Sunday.
                        if ((pref.getStringSet("weekdays", null).contains("7"))) {
                            summary.append(getResources().getString(R.string.trigger_pref_sun));
                        }

                        findPreference("weekdays").setSummary(summary.toString());
                    }
                }
            }
        }
    }

    /**
     * Adds functionality to the save and cancel menu buttons.
     *
     * @param item Menu item selected.
     * @return Item selected effect.
     */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

		// Show dialogs if the user wants to save and something is wrong

        // If save was selected.
		if (item.getItemId() == R.id.save_trigger) {

            // If trigger/profile name is default/blank display error message.
			if (pref.getString("name_trigger", getResources().getString(R.string.trigger_pref_default_name)).equals(getResources().getString(R.string.trigger_pref_default_name))
					|| pref.getString("name_trigger", getResources() .getString(R.string.trigger_default_name_new)).equals("")
					|| pref.getString("profile", getResources().getString(R.string.trigger_pref_default_profile)).equals(getResources().getString(R.string.trigger_pref_default_profile))
                    || pref.getInt("battery_start_level", -1) >= pref.getInt("battery_end_level", -1)
                    && pref.getInt("battery_end_level", -1) != -1) {

                // Create dialog with icon, title, text and confirmation button.
                AlertDialog.Builder dialog;
                if (pref.getBoolean("dark_theme", false)) {
                    dialog = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                    dialog.setIcon(R.drawable.ic_action_warning);
                } else {
                    dialog = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                    dialog.setIcon(R.drawable.ic_action_warning_light);
                }
				// Name is default.
                if (pref.getString("name_trigger", getResources().getString(R.string.trigger_pref_default_name)).equals(getResources().getString(R.string.trigger_pref_default_name))) {
					dialog.setTitle(getResources().getString(R.string.trigger_pref_default_name));
					dialog.setMessage(getResources().getString(R.string.trigger_alert_name_text));
				// Name is blank.
                } else if (pref.getString("name_trigger",getResources().getString(R.string.trigger_pref_default_name)).equals("")) {
					dialog.setTitle(getResources().getString(R.string.trigger_alert_name_title));
					dialog.setMessage(getResources().getString(R.string.trigger_alert_name_text));
				// No Profile set.
                } else if (pref.getString("profile", getResources().getString(R.string.trigger_pref_default_profile)).equals(getResources().getString(R.string.trigger_pref_default_profile))) {
					dialog.setTitle(getResources().getString(R.string.profile_alert_name_title));
					dialog.setMessage(getResources().getString(R.string.trigger_alert_profile_text));
                // Battery low-level < battery high-level
				} else if (pref.getInt("battery_start_level", -1) > pref.getInt("battery_end_level", -1) && pref.getInt("battery_end_level", -1) != -1) {
                    dialog.setTitle(getResources().getString( R.string.trigger_alert_battery_title));
                    dialog.setMessage(getResources().getString( R.string.trigger_alert_battery_text));
                // Battery low-level = battery high-level
                } else if (pref.getInt("battery_start_level", -1) == pref.getInt("battery_end_level", -1) && pref.getInt("battery_end_level", -1) != -1) {
                    dialog.setTitle(getResources().getString(R.string.trigger_alert_battery_title));
                    dialog.setMessage(getResources().getString(R.string.trigger_alert_battery_exact_text));
                }

                // Set cancel button.
                dialog.setNegativeButton(getResources().getString(R.string.trigger_alert_name_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

				dialog.show();

            // If entered name is valid and profile selected, save the trigger.
			} else {
				this.saveTrigger();
				this.finish();
			}

        // If cancel was selected.
		} else if (item.getItemId() == R.id.cancel_trigger) {
			finish();

        // If home was selected.
		} else if (item.getItemId() == android.R.id.home) {
            finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

    /**
     * If the back button is selected, display a dialog if there are any unsaved changes.
     */
	@Override
	public void onBackPressed() {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

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
            // Exit the edit trigger activity.
            dialog.setPositiveButton(getResources().getString(R.string.trigger_alert_discard_yes), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });

			dialog.setNegativeButton(getResources().getString(R.string.trigger_alert_discard_no), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

			dialog.show();

        // If no preferences were changed then exit the edit profile activity.
		} else {
			this.finish();
		}
	}

	/**
	 * Saves the current settings of the activity to a profile object and lets
	 * it be written by the XmlCreator.
	 */
    private void saveTrigger() {

		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		LocationTrigger locationTrigger = new LocationTrigger(this);
		String name = pref.getString("name_trigger", getResources().getString(R.string.trigger_pref_default_name));
		Trigger trigger = new Trigger(name);
		trigger.setPriority(Integer.parseInt(pref.getString("priority", "0")));

        // Set start time.
		if (pref.getString("start_time", getString(R.string.ignored)).equals(getString(R.string.ignored))) {
			trigger.setStartHours(-1);
			trigger.setStartMinutes(-1);
		} else {
			trigger.setStartHours(Integer.parseInt(pref.getString("start_time", "00:00").split(":")[0]));
			trigger.setStartMinutes(Integer.parseInt(pref.getString("start_time", "00:00").split(":")[1]));
		}

        // Set end time.
		if (pref.getString("end_time", getString(R.string.ignored)).equals(getString(R.string.ignored))) {
			trigger.setEndHours(-1);
			trigger.setEndMinutes(-1);
		} else {
			trigger.setEndHours(Integer.parseInt(pref.getString("end_time", "00:00").split(":")[0]));
			trigger.setEndMinutes(Integer.parseInt(pref.getString("end_time", "00:00").split(":")[1]));
		}

        // Set days.
		trigger.setWeekdays(pref.getStringSet("weekdays", null));

        // Set profile name.
		trigger.setProfileName(pref.getString("profile", getResources().getString(R.string.trigger_pref_default_profile)));

        // Set battery state.
		if (pref.getString("battery_state", "ignored").equals("charging")) {
			trigger.setBatteryState(Trigger.listen_state.listen_on);
		} else if (pref.getString("battery_state", "ignored").equals("discharging")) {
			trigger.setBatteryState(Trigger.listen_state.listen_off);
		} else {
			trigger.setBatteryState(Trigger.listen_state.ignore);
		}

        trigger.setBatteryStartLevel(pref.getInt("battery_start_level", -1));

        trigger.setBatteryEndLevel(pref.getInt("battery_end_level", -1));

        // Set headphones state.
		if (pref.getString("headphone", "ignored").equals("plugged_in")) {
			trigger.setHeadphones(Trigger.listen_state.listen_on);
		} else if (pref.getString("headphone", "ignored").equals("unplugged")) {
			trigger.setHeadphones(Trigger.listen_state.listen_off);
		} else {
			trigger.setHeadphones(Trigger.listen_state.ignore);
		}

		// Unregister old geo-fence from system.
		locationTrigger.unregisterGeofence(name);
		locationTrigger.unregisterGeofence(name + "_exit");

		// Delete list of currently triggered geo-fences from the service.
		Intent intent = new Intent();
		intent.setAction("net.davidnorton.securityapp.trigger.clearGeofences");
		sendBroadcast(intent);

		if (pref.getInt("geofence_radius", 50) > 0) {

			// Geo-fence that registers if you enter the area.
			SimpleGeofence simple = new SimpleGeofence(name, pref.getFloat(
					"geofence_lat", 0F), pref.getFloat("geofence_lng", 0F),
					pref.getInt("geofence_radius", 0), Geofence.NEVER_EXPIRE,
					Geofence.GEOFENCE_TRANSITION_ENTER);
			locationTrigger.registerGeofence(simple);

			// Geo-fence that registers if you leave the area.
			simple = new SimpleGeofence(name + "_exit", pref.getFloat(
					"geofence_lat", 0F), pref.getFloat("geofence_lng", 0F),
					pref.getInt("geofence_radius", 0), Geofence.NEVER_EXPIRE,
					Geofence.GEOFENCE_TRANSITION_EXIT);
			locationTrigger.registerGeofence(simple);

			// Set geo-fence of trigger to the enter event.
			trigger.setGeofence(name);
		} else {
			locationTrigger.unregisterGeofence(name);
			locationTrigger.unregisterGeofence(name + "_exit");
			trigger.setGeofence(null);
		}

		// Generate XML.
		XmlCreatorTrigger creator = new XmlCreatorTrigger();

		try {
			FileOutputStream output = openFileOutput(trigger.getName() + "_trigger.xml", Context.MODE_PRIVATE);
			output.write(creator.create(trigger).getBytes());
			output.close();
		} catch (TransformerException | ParserConfigurationException | IOException e1) {
			e1.printStackTrace();
		}

        // If editing file, delete old file.
        if (!(name.equals(previousName))) {
			File file = new File(String.valueOf(getFilesDir()) + "/" + previousName + "_trigger.xml");
			file.delete();
		}

		intent = new Intent();
		intent.setAction("net.davidnorton.securityapp.trigger.refresh");
		sendBroadcast(intent);
	}

	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	private static final Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {

        /**
         * Updates the preference summary text.
         *
         * @param preference Preference changed.
         * @param value Value of preference.
         * @return True.
         */
        @Override
		public boolean onPreferenceChange(Preference preference, Object value) {

            String stringValue = value.toString();

            // For list preferences, look up the correct display value in the preference's 'entries' list.
			if (preference instanceof ListPreference) {
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
				preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);

            // For all other preferences, set the summary to the value's string representation.
            } else {
				preference.setSummary(stringValue);
			}
			return true;
		}
	};

    /**
     * Binds a preference's summary to its value.
     *
     * @param preference Preference changed.
     */
	private static void bindPreferenceSummaryToValue(Preference preference) {

        // Set the listener to watch for value changes.
		preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's current value.
		sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(
                        preference.getContext()).getString(preference.getKey(), ""));
	}

	/**
	 * Refreshes the profile array.
	 */
	private void refreshProfileArray() {

        List<String> profileList = new ArrayList<>();
		String[] fileList = getFilesDir().list();
		StringBuilder sb = new StringBuilder();

        // Get the name of each profile and add to list.
		for (String file : fileList) {
			if (file.contains("_profile")) {
				sb.append(file);
				sb.delete(sb.length() - 12, sb.length());
				profileList.add(sb.toString());
				sb.delete(0, sb.length());
			}
		}

        // Create list of profiles with the default value as the first item.
		profileArray = new CharSequence[profileList.size() + 1];
		profileArray[0] = getResources().getString(R.string.trigger_pref_default_profile);

		for (int i = 0; i < profileArray.length - 1; i++) {
			profileArray[i + 1] = profileList.get(i);
		}
	}

    // Not used as Preference Fragment used instead.
    @Override
    public void onSharedPreferenceChanged(SharedPreferences _pref, String key) {

    }

}
