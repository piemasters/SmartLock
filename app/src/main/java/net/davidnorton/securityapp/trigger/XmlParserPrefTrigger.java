package net.davidnorton.securityapp.trigger;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Xml;

import net.davidnorton.securityapp.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Stores xml values in shared preferences for Trigger edit activity.
 *
 * @author David Norton
 */
public class XmlParserPrefTrigger {

	final static String TAG = "XmlParserPrefTrigger";
	
	Context context;
	Editor prefEdit;
	String triggerName;

	/**
	 * Initializes the xml parser with the given context.
	 * 
	 * @param _context
	 */

    /**
     * Initializes the xml parser with given context.
     *
     * @param cont Context.
     * @param name Profile Name.
     */
	public XmlParserPrefTrigger(Context cont, String name) {
		context = cont;
		prefEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
		triggerName = name;
	}

	/**
	 * Sets up the xml parser for the given inputstream and then hands it over
	 * to the readAndApplyTags method to process the stream.
	 * 
	 * @param _in
	 *            the input stream you want to parse.
	 * @throws org.xmlpull.v1.XmlPullParserException
	 * @throws java.io.IOException
	 */

    /**
     * Initializes the xml parser with given context.
     *
     * @param in Inputstream to parse.
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
	public void initializeXmlParser(InputStream in) throws XmlPullParserException, IOException {

		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			readAndApplyTags(parser);
		} finally {
			in.close();
		}
	}

	/**
	 * Reads the given input stream and saves the read values.
	 *
	 * @param _parser
	 *            the parser which should read the tags
	 * @throws org.xmlpull.v1.XmlPullParserException
	 * @throws java.io.IOException
	 */

    /**
     * Reads and applies settings using setter methods.
     *
     * @param parser The parser to read the tags.
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
	private void readAndApplyTags(XmlPullParser parser) throws XmlPullParserException, IOException {

		prefEdit.putString("name_trigger", triggerName);

		parser.require(XmlPullParser.START_TAG, null, "trigger");

		while (parser.next() != XmlPullParser.END_TAG) {


			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}

			String name = parser.getName();

            switch (name) {
                case "profile":
                    setProfile(parser);
                    break;
                case "time":
                    setTime(parser);
                    break;
                case "battery":
                    setBattery(parser);
                    break;
                case "headphone":
                    setHeadphone(parser);
                    break;
                case "geofence":
                    setGeofence(parser);
                    break;
                case "priority":
                    setPriority(parser);
                    break;
                case "weekdays":
                    setWeekdays(parser);
                    break;
                default:
                    Log.w("XmlParser", "Skip!");
                    parser.nextTag();
                    break;
            }
			prefEdit.commit();
		}
	}

    /**
     * Sets the Profile.
     *
     * @param parser The parser to read the settings.
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
	private void setProfile(XmlPullParser parser) throws XmlPullParserException, IOException {

        parser.require(XmlPullParser.START_TAG, null, "profile");

        // If state has changed.
		if (parser.getAttributeValue(null, "name") != null) {
			prefEdit.putString("profile", parser.getAttributeValue(null, "name"));
			Log.i(TAG, "Profile: " + parser.getAttributeValue(null, "profile"));
		} else {
			Log.e(TAG, "Profile: Invalid Argument!");
		}

        parser.nextTag();
	}

    /**
     * Sets the time.
     *
     * @param parser The parser to read the settings.
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
	private void setTime(XmlPullParser parser) throws XmlPullParserException, IOException {

        parser.require(XmlPullParser.START_TAG, null, "time");

		int startHours = -1, startMinutes = -1, endHours = -1, endMinutes = -1;

        // If start hours has changed.
		if (parser.getAttributeValue(null, "start_hours") != null) {
            // If input is valid.
			if (Integer.parseInt(parser.getAttributeValue(null, "start_hours")) >= -1
					&& Integer.parseInt(parser.getAttributeValue(null, "start_hours")) <= 23) {

                startHours = Integer.parseInt(parser.getAttributeValue(null, "start_hours"));
				Log.i(TAG, "start_hours: " + parser.getAttributeValue(null, "start_hours"));
			} else {
				Log.i(TAG, "start_hours: ignore.");
			}
		} else {
			Log.e(TAG, "start_hours: Invalid Argument!");
		}

        // If start minutes has changed.
		if (parser.getAttributeValue(null, "start_minutes") != null) {
            // If input is valid.
			if (Integer.parseInt(parser.getAttributeValue(null, "start_minutes")) >= -1
					&& Integer.parseInt(parser.getAttributeValue(null, "start_minutes")) <= 59) {

				startMinutes = Integer.parseInt(parser.getAttributeValue(null, "start_minutes"));
				Log.i(TAG, "start_minutes: " + parser.getAttributeValue(null, "start_minutes"));
			} else {
				Log.i(TAG, "start_minutes: ignore.");
			}
		} else {
			Log.e(TAG, "start_minutes: Invalid Argument!");
		}

        // If end hours has changed.
		if (parser.getAttributeValue(null, "end_hours") != null) {
            // If input is valid.
			if (Integer.parseInt(parser.getAttributeValue(null, "end_hours")) >= -1
					&& Integer.parseInt(parser.getAttributeValue(null, "end_hours")) <= 23) {

				endHours = Integer.parseInt(parser.getAttributeValue(null, "end_hours"));
				Log.i(TAG, "end_hours: " + parser.getAttributeValue(null, "end_hours"));
			} else {
				Log.i(TAG, "end_hours: ignore.");
			}
		} else {
			Log.e(TAG, "end_hours: Invalid Argument!");
		}

        // If end minutes has changed.
		if (parser.getAttributeValue(null, "end_minutes") != null) {
            // If input is valid.
			if (Integer.parseInt(parser.getAttributeValue(null, "end_minutes")) >= -1
					&& Integer.parseInt(parser.getAttributeValue(null, "end_minutes")) <= 59) {

				endMinutes = Integer.parseInt(parser.getAttributeValue(null, "end_minutes"));
				Log.i(TAG, "end_minutes: " + parser.getAttributeValue(null, "end_minutes"));
			} else {
				Log.i(TAG, "end_minutes: ignore.");
			}
		} else {
			Log.e(TAG, "end_minutes: Invalid Argument!");
		}

		String startTime, endTime;

        // If both start hours and minutes are set.
		if (startHours != -1 && startMinutes != -1) {
			startTime = String.format("%02d", startHours) + ":" + String.format("%02d", startMinutes);
		} else {
			startTime = context.getString(R.string.ignored);
		}

        // If both end hours and minutes are set.
		if (endHours != -1 && endMinutes != -1) {
			endTime = String.format("%02d", endHours) + ":" + String.format("%02d", endMinutes);
		} else {
			endTime = context.getString(R.string.ignored);
		}

		prefEdit.putString("start_time", startTime);
		prefEdit.putString("end_time", endTime);

		parser.nextTag();
	}

    /**
     * Sets battery settings.
     *
     * @param parser The parser to read the settings.
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
	private void setBattery(XmlPullParser parser) throws XmlPullParserException, IOException {

        parser.require(XmlPullParser.START_TAG, null, "battery");

        // If start level set.
		if (parser.getAttributeValue(null, "start_level") != null) {
            // If value is valid.
			if (Integer.parseInt(parser.getAttributeValue(null, "start_level")) >= 0
					&& Integer.parseInt(parser.getAttributeValue(null, "start_level")) <= 100) {

                prefEdit.putInt("battery_start_level", Integer.parseInt(parser.getAttributeValue(null, "start_level")));
				Log.i(TAG, "BatteryStartLevel: " + parser.getAttributeValue(null, "start_level"));
			} else {
				prefEdit.putInt("battery_start_level", -1);
				Log.i(TAG, "BatteryStartLevel: ignore.");
			}
		} else {
			Log.e(TAG, "BatteryStartLevel: Invalid Argument!");
		}

        // If end level set.
		if (parser.getAttributeValue(null, "end_level") != null) {
            // If value is valid.
			if (Integer.parseInt(parser.getAttributeValue(null, "end_level")) >= 0
					&& Integer.parseInt(parser.getAttributeValue(null, "end_level")) <= 100) {

                prefEdit.putInt("battery_end_level", Integer.parseInt(parser.getAttributeValue(null, "end_level")));
				Log.i(TAG, "BatteryEndLevel: " + parser.getAttributeValue(null, "end_level"));
			} else {
				prefEdit.putInt("battery_end_level", -1);
				Log.i(TAG, "BatteryEndLevel: ignore.");
			}
		} else {
			Log.e(TAG, "BatteryEndLevel: Invalid Argument!");
		}

        // If battery state set.
		if (parser.getAttributeValue(null, "state") != null) {
			// If charging
            if (parser.getAttributeValue(null, "state").equals("1")) {
				prefEdit.putString("battery_state", "charging");
				Log.i(TAG, "BatteryState listen on.");
			// If discharging
            } else if (parser.getAttributeValue(null, "state").equals("0")) {
				prefEdit.putString("battery_state", "discharging");
				Log.i(TAG, "BatteryState listen off.");
			// If neither state.
            } else if (parser.getAttributeValue(null, "state").equals("-1")) {
				prefEdit.putString("battery_state", "ignored");
				Log.i(TAG, "BatteryState ignored.");
			} else {
				Log.i(TAG, "BateryState: ignore.");
			}
		} else {
			Log.e(TAG, "BatteryState: Invalid Argument!");
		}

		parser.nextTag();
	}

    /**
     * Sets headphones settings.
     *
     * @param parser The parser to read the settings.
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
	private void setHeadphone(XmlPullParser parser) throws XmlPullParserException, IOException {

        parser.require(XmlPullParser.START_TAG, null, "headphone");

        // If headphones state set.
		if (parser.getAttributeValue(null, "state") != null) {
            // If plugged in.
			if (parser.getAttributeValue(null, "state").equals("1")) {
				prefEdit.putString("headphone", "plugged_in");
				Log.i(TAG, "Headphones listen on.");
            // If not plugged in.
			} else if (parser.getAttributeValue(null, "state").equals("0")) {
				prefEdit.putString("headphone", "unplugged");
				Log.i(TAG, "Headphones listen off.");
            // If neither state.
			} else if (parser.getAttributeValue(null, "state").equals("-1")) {
				prefEdit.putString("headphone", "ignored");
				Log.i(TAG, "Headphones ignored.");
			} else {
				Log.i(TAG, "Headphones: ignore.");
			}
		} else {
			Log.e(TAG, "Headphones: Invalid Argument!");
		}

		parser.nextTag();
	}

    /**
     * Sets the geo-fence.
     *
     * @param parser The parser to read the settings.
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
	private void setGeofence(XmlPullParser parser) throws XmlPullParserException, IOException {

        parser.require(XmlPullParser.START_TAG, null, "geofence");

        // If geo-fence set.
		if (parser.getAttributeValue(null, "id") != null) {
            // If geo-fence isn't empty.
			if (!parser.getAttributeValue(null, "id").equals("")) {
				SimpleGeofenceStore store = new SimpleGeofenceStore(context);
				SimpleGeofence simple = store.getGeofence(parser.getAttributeValue(null, "id"));
				prefEdit.putFloat("geofence_lat", (float) simple.getLatitude());
				prefEdit.putFloat("geofence_lng", (float) simple.getLongitude());
				prefEdit.putInt("geofence_radius", (int) simple.getRadius());
				Log.i(TAG, "Geofence loaded");
			} else {
				prefEdit.putFloat("geofence_lat", -1F);
				prefEdit.putFloat("geofence_lng", -1F);
				prefEdit.putInt("geofence_radius", -1);
				Log.i(TAG, "Geofence: ignore");
			}
		} else {
			Log.e(TAG, "Geofence: Invalid Argument!");
		}

		parser.nextTag();
	}

    /**
     * Sets priority.
     *
     * @param parser The parser to read the settings.
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
	private void setPriority(XmlPullParser parser) throws XmlPullParserException, IOException {

        parser.require(XmlPullParser.START_TAG, null, "priority");

        // If priority set.
        if (parser.getAttributeValue(null, "value") != null) {
			// If value is valid.
            if (Integer.parseInt(parser.getAttributeValue(null, "value")) >= 0
					&& Integer.parseInt(parser.getAttributeValue(null, "value")) <= 99) {

                prefEdit.putString("priority", parser.getAttributeValue(null, "value"));
				Log.i(TAG, "priority: " + parser.getAttributeValue(null, "value"));
			} else {
				Log.i(TAG, "priority: ignore.");
			}
		} else {
			Log.e(TAG, "priority: Invalid Argument!");
		}

		parser.nextTag();
	}

    /**
     * Sets the days.
     *
     * @param parser The parser to read the settings.
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
	private void setWeekdays(XmlPullParser parser) throws XmlPullParserException, IOException {

        parser.require(XmlPullParser.START_TAG, null, "weekdays");

        Set<String> weekdays = new HashSet<>();

        // If Monday selected.
		if (parser.getAttributeValue(null, "mon") != null) {
			if (parser.getAttributeValue(null, "mon").equals("true")) {
				weekdays.add("1");
				Log.i(TAG, "weekdays: monday");
			} else {
				Log.i(TAG, "weekdays: no monday");
			}
		}

        // If Tuesday selected.
		if (parser.getAttributeValue(null, "tue") != null) {
			if (parser.getAttributeValue(null, "tue").equals("true")) {
				weekdays.add("2");
				Log.i(TAG, "weekdays: tuesday");
			} else {
				Log.i(TAG, "weekdays: no tuesday");
			}
		}

        // If Wednesday selected.
		if (parser.getAttributeValue(null, "wed") != null) {
			if (parser.getAttributeValue(null, "wed").equals("true")) {
				weekdays.add("3");
				Log.i(TAG, "weekdays: wednesday");
			} else {
				Log.i(TAG, "weekdays: no wednesday");
			}
		}

        // If Thursday selected.
		if (parser.getAttributeValue(null, "thur") != null) {
			if (parser.getAttributeValue(null, "thur").equals("true")) {
				weekdays.add("4");
				Log.i(TAG, "weekdays: thursday");
			} else {
				Log.i(TAG, "weekdays: no thursday");
			}
		}

        // If Friday selected.
		if (parser.getAttributeValue(null, "fri") != null) {
			if (parser.getAttributeValue(null, "fri").equals("true")) {
				weekdays.add("5");
				Log.i(TAG, "weekdays: friday");
			} else {
				Log.i(TAG, "weekdays: no friday");
			}
		}

        // If Saturday selected.
		if (parser.getAttributeValue(null, "sat") != null) {
			if (parser.getAttributeValue(null, "sat").equals("true")) {
				weekdays.add("6");
				Log.i(TAG, "weekdays: saturday");
			} else {
				Log.i(TAG, "weekdays: no saturday");
			}
		}

        // If Sunday selected.
		if (parser.getAttributeValue(null, "sun") != null) {
			if (parser.getAttributeValue(null, "sun").equals("true")) {
				weekdays.add("7");
				Log.i(TAG, "weekdays: sunday");
			} else {
				Log.i(TAG, "weekdays: no sunday");
			}
		}
		
		prefEdit.putStringSet("weekdays", weekdays);

		parser.nextTag();
	}
}
