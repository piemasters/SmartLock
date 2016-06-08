package net.davidnorton.securityapp.profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import net.davidnorton.securityapp.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**
 * Activity used to edit the settings of a Profile.
 * 
 * @author David Norton
 * 
 */
public class ProfileEditActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    // Indicates if there are unsaved changes in the preferences.
	private static boolean preferencesChanged = false;

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
        getActionBar().setTitle(R.string.profile_edit_title);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // Create preference screen.
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
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
        menuInflater.inflate(R.menu.profile_edit, menu);
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
        public void onCreate(final Bundle savedInstanceState){

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            previousName = pref.getString("name", "default name");
            pref.registerOnSharedPreferenceChangeListener(this);
            super.onCreate(savedInstanceState);

            // Add preference pages.
            addPreferencesFromResource(R.xml.pref_profile_edit_general);
            bindPreferenceSummaryToValue(findPreference("name"));
            addPreferencesFromResource(R.xml.pref_profile_edit_security);
            bindPreferenceSummaryToValue(findPreference("lockscreen"));
            addPreferencesFromResource(R.xml.pref_profile_edit_connectivity);
            bindPreferenceSummaryToValue(findPreference("wifi"));
            bindPreferenceSummaryToValue(findPreference("mobile_data"));
            bindPreferenceSummaryToValue(findPreference("bluetooth"));
            addPreferencesFromResource(R.xml.pref_profile_edit_display);
            bindPreferenceSummaryToValue(findPreference("display_auto_mode"));
            bindPreferenceSummaryToValue(findPreference("display_time_out"));
            addPreferencesFromResource(R.xml.pref_profile_edit_sound);
            bindPreferenceSummaryToValue(findPreference("ringer_mode"));
        }

        /**
         * Listens for a preference change.
         *
         * @param pref SharedPreference.
         * @param key Key.
         */
        @Override
        public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
            preferencesChanged = true;
        }
    }

    /**
     * Adds functionality to the save and cancel menu buttons.
     *
     * @param item Menu item selected.
     *
     * @return Item selected effect.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        // If save was selected.
        if (item.getItemId() == R.id.save_profile) {

            // If name is still default value display error message.
            if (pref.getString("name", getResources().getString(R.string.profile_default_name)).equals(getResources().getString(R.string.profile_default_name))) {

                // Create dialog with icon, title, text and confirmation button.
                AlertDialog.Builder dialog;
                if (pref.getBoolean("dark_theme", false)) {
                    dialog = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                    dialog.setIcon(R.drawable.ic_action_warning);
                } else {
                    dialog = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                    dialog.setIcon(R.drawable.ic_action_warning_light);
                }
                dialog.setIcon(R.drawable.ic_action_warning);
                dialog.setTitle(getResources().getString(R.string.profile_alert_name_title));
                dialog.setMessage(getResources().getString(R.string.profile_alert_name_text));
                dialog.setNegativeButton(getResources().getString(R.string.profile_alert_name_button),

                        // Dismiss the dialog when confirmation button is selected.
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,int which) {
                                dialog.dismiss();
                            }
                        });
                dialog.show();
            }

            // If name is blank display error message.
            else if (pref.getString("name", getResources().getString(R.string.profile_default_name)).equals("")) {

                // Create dialog with icon, title, text and confirmation button.
                AlertDialog.Builder dialog;
                if (pref.getBoolean("dark_theme", false)) {
                    dialog = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                    dialog.setIcon(R.drawable.ic_action_warning);
                } else {
                    dialog = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                    dialog.setIcon(R.drawable.ic_action_warning_light);
                }
                dialog.setIcon(R.drawable.ic_action_warning);
                dialog.setTitle(getResources().getString(R.string.profile_alert_name_title));
                dialog.setMessage(getResources().getString(R.string.profile_alert_name_text));
                dialog.setNegativeButton(getResources().getString(R.string.profile_alert_name_button),new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();

            // If entered name is valid, save the profile.
            } else {
                this.saveProfile();
                this.finish();
            }

        // If cancel was selected.
        } else if (item.getItemId() == R.id.cancel_profile) {
            this.finish();

        // If home was selected.
        } else if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
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
            dialog.setTitle(getResources().getString(R.string.profile_alert_discard_title));
            dialog.setMessage(getResources().getString(R.string.profile_alert_discard_text));
            // Exit the edit profile activity.
            dialog.setPositiveButton(getResources().getString(R.string.profile_alert_discard_yes), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            // Dismiss the dialog and resume the profile activity.
            dialog.setNegativeButton(getResources().getString(R.string.profile_alert_discard_no), new DialogInterface.OnClickListener() {

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
     * Saves the selected preferences to the named Profile.
     */
    private void saveProfile() {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        // General

        // Save Profile name.
        String name = pref.getString("name", getResources().getString(R.string.profile_pref_default_name));
		Profile profile = new Profile(name);

        // Lock Screen
        if (pref.getString("lockscreen", "unchanged").equals("enabled")) {
            profile.setLockscreen(Profile.state.enabled);
        } else if (pref.getString("lockscreen", "unchanged").equals("disabled")) {
            profile.setLockscreen(Profile.state.disabled);
        } else {
            profile.setLockscreen(Profile.state.unchanged);
        }

        // Wireless & Networks

        // WiFi
        if (pref.getString("wifi", "unchanged").equals("enabled")) {
            profile.setWifi(Profile.state.enabled);
        } else if (pref.getString("wifi", "unchanged").equals("disabled")) {
            profile.setWifi(Profile.state.disabled);
        } else {
            profile.setWifi(Profile.state.unchanged);
        }

        // Mobile Data
        if (pref.getString("mobile_data", "unchanged").equals("enabled")) {
            profile.setMobileData(Profile.state.enabled);
        } else if (pref.getString("mobile_data", "unchanged")
                .equals("disabled")) {
            profile.setMobileData(Profile.state.disabled);
        } else {
            profile.setMobileData(Profile.state.unchanged);
        }

        // Bluetooth
        if (pref.getString("bluetooth", "unchanged").equals("enabled")) {
            profile.setBluetooth(Profile.state.enabled);
        } else if (pref.getString("bluetooth", "unchanged").equals("disabled")) {
            profile.setBluetooth(Profile.state.disabled);
        } else {
            profile.setBluetooth(Profile.state.unchanged);
        }

        // Display

        // Automatic Brightness
        if (pref.getString("display_auto_mode", "unchanged").equals("enabled")) {
            profile.setScreenBrightnessAutoMode(Profile.state.enabled);
        } else if (pref.getString("display_auto_mode", "unchanged").equals("disabled")) {
            profile.setScreenBrightnessAutoMode(Profile.state.disabled);
        } else {
            profile.setScreenBrightnessAutoMode(Profile.state.unchanged);
        }

        // Display Time Out
        profile.setScreenTimeOut(Integer.parseInt(pref.getString("display_time_out", "-1")));

        // Sound

        // Ringer Mode
		if (pref.getString("ringer_mode", "unchanged").equals("silent")) {
			profile.setRingerMode(Profile.mode.silent);
		} else if (pref.getString("ringer_mode", "unchanged").equals("vibrate")) {
			profile.setRingerMode(Profile.mode.vibrate);
		} else if (pref.getString("ringer_mode", "unchanged").equals("normal")) {
			profile.setRingerMode(Profile.mode.normal);
		} else {
			profile.setRingerMode(Profile.mode.unchanged);
		}

        // Create the Profile.
		XmlCreator creator = new XmlCreator();
		try {
			FileOutputStream output = openFileOutput(profile.getName() + "_profile.xml", Context.MODE_PRIVATE);
			output.write(creator.create(profile).getBytes());
			output.close();
		} catch (IOException | TransformerException | ParserConfigurationException e) {
			e.printStackTrace();
		}

        // If Profile was renamed, delete the old Profile.
		if (!(name.equals(previousName))) {
			File file = new File(String.valueOf(getFilesDir()) + "/" + previousName + "_profile.xml");
			file.delete();
		}
	}

	/**
	 * A listener that detects changes to a preferences value used to update
     * the preference's summary to reflect its new value.
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
		sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,PreferenceManager.getDefaultSharedPreferences(
                        preference.getContext()).getString(preference.getKey(), ""));
	}


    // Not used as Preference Fragment used instead.
	@Override
	public void onSharedPreferenceChanged(SharedPreferences _pref, String key) {

	}
}
