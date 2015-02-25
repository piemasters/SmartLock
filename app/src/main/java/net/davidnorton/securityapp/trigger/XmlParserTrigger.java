package net.davidnorton.securityapp.trigger;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Reads an xml input stream created in XmlCreatorTrigger and applies it
 * using the TriggerService class.
 *
 * @author David Norton
 */
public class XmlParserTrigger {

	final static String TAG = "XmlParserTrigger";

	Context context;

    /**
     *  Initializes the xml parser with the given context.
     *
     * @param cont Context.
     */
	public XmlParserTrigger(Context cont) {
		context = cont;
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
     * Sets up the xml parser for the inputstream, then hands over to readAndApplyTags to process it.
     *
     * @param in Inputstream to parse.
     * @param trigger Trigger.
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
	public void initializeXmlParser(InputStream in, Trigger trigger) throws XmlPullParserException, IOException {

		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			readAndApplyTags(parser, trigger);
		} finally {
			in.close();
		}
	}

    /**
     * Reads and sets trigger preferences.
     *
     * @param parser The parser to read the tags.
     * @param trigger Trigger.
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
	private void readAndApplyTags(XmlPullParser parser, Trigger trigger) throws XmlPullParserException, IOException {

        parser.require(XmlPullParser.START_TAG, null, "trigger");

        // For each tag.
		while (parser.next() != XmlPullParser.END_TAG) {

            // Skip if not a start tag.
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}

			String name = parser.getName();

            // Look for the entry tag.
            switch (name) {
                case "profile":
                    setProfile(parser, trigger);
                    break;
                case "time":
                    setTime(parser, trigger);
                    break;
                case "battery":
                    setBattery(parser, trigger);
                    break;
                case "headphone":
                    setHeadphone(parser, trigger);
                    break;
                case "geofence":
                    setGeofence(parser, trigger);
                    break;
                case "priority":
                    setPriority(parser, trigger);
                    break;
                case "weekdays":
                    setWeekdays(parser, trigger);
                    break;
                default:
                    Log.w("XmlParser", "Skip!");
                    parser.nextTag();
                    break;
            }
		}
	}

    /**
     * Sets the profile.
     *
     * @param parser The parser to read the tags.
     * @param trigger Trigger.
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
	private void setProfile(XmlPullParser parser, Trigger trigger) throws XmlPullParserException, IOException {

		parser.require(XmlPullParser.START_TAG, null, "profile");

        // If profile name is set.
		if (parser.getAttributeValue(null, "name") != null) {
			trigger.setProfileName(parser.getAttributeValue(null, "name"));
			Log.i(TAG, "Profile: " + parser.getAttributeValue(null, "name"));
		} else {
			Log.e(TAG, "Profile: Invalid Argument!");
		}

		parser.nextTag();
	}

    /**
     * Sets the time.
     *
     * @param parser The parser to read the tags.
     * @param trigger Trigger.
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
	private void setTime(XmlPullParser parser, Trigger trigger) throws XmlPullParserException, IOException {

		parser.require(XmlPullParser.START_TAG, null, "time");

        // If start hour set.
		if (parser.getAttributeValue(null, "start_hours") != null) {

            // If a valid input.
			if (Integer.parseInt(parser.getAttributeValue(null, "start_hours")) >= -1
					&& Integer.parseInt(parser.getAttributeValue(null, "start_hours")) <= 23) {

                trigger.setStartHours(Integer.parseInt(parser .getAttributeValue(null, "start_hours")));

                Log.i(TAG, "start_hours: " + parser.getAttributeValue(null, "start_hours"));
			} else {
				Log.i(TAG, "start_hours: ignore.");
			}
		} else {
			Log.e(TAG, "start_hours: Invalid Argument!");
		}

        // If start minute set.
		if (parser.getAttributeValue(null, "start_minutes") != null) {

            // If a valid input.
            if (Integer.parseInt(parser.getAttributeValue(null,"start_minutes")) >= -1
					&& Integer.parseInt(parser.getAttributeValue(null, "start_minutes")) <= 59) {

                trigger.setStartMinutes(Integer.parseInt(parser.getAttributeValue(null, "start_minutes")));

                Log.i(TAG, "start_minutes: " + parser.getAttributeValue(null, "start_minutes"));

			} else {
				Log.i(TAG, "start_minutes: ignore.");
			}
		} else {
			Log.e(TAG, "start_minutes: Invalid Argument!");
		}

        // If end hour set.
        if (parser.getAttributeValue(null, "end_hours") != null) {

            // If a valid input.
            if (Integer.parseInt(parser.getAttributeValue(null, "end_hours")) >= -1
					&& Integer.parseInt(parser.getAttributeValue(null, "end_hours")) <= 23) {

                trigger.setEndHours(Integer.parseInt(parser.getAttributeValue(null, "end_hours")));

                Log.i(TAG,"end_hours: " + parser.getAttributeValue(null, "end_hours"));
			} else {
				Log.i(TAG, "end_hours: ignore.");
			}
		} else {
			Log.e(TAG, "end_hours: Invalid Argument!");
		}

        // If end minute set.
		if (parser.getAttributeValue(null, "end_minutes") != null) {

            // If a valid input.
            if (Integer.parseInt(parser.getAttributeValue(null, "end_minutes")) >= -1
					&& Integer.parseInt(parser.getAttributeValue(null, "end_minutes")) <= 59) {

                trigger.setEndMinutes(Integer.parseInt(parser.getAttributeValue(null, "end_minutes")));

                Log.i(TAG, "end_minutes: " + parser.getAttributeValue(null, "end_minutes"));
			} else {
				Log.i(TAG, "end_minutes: ignore.");
			}
		} else {
			Log.e(TAG, "end_minutes: Invalid Argument!");
		}

		parser.nextTag();
	}

    /**
     * Sets the battery.
     *
     * @param parser The parser to read the tags.
     * @param trigger Trigger.
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
	private void setBattery(XmlPullParser parser, Trigger trigger) throws XmlPullParserException, IOException {

        parser.require(XmlPullParser.START_TAG, null, "battery");

        // If battery start level set.
        if (parser.getAttributeValue(null, "start_level") != null) {
            // If value is valid.
            if (Integer.parseInt(parser.getAttributeValue(null, "start_level")) >= 0
                    && Integer.parseInt(parser.getAttributeValue(null, "start_level")) <= 100) {

                trigger.setBatteryStartLevel(Integer.parseInt(parser.getAttributeValue(null, "start_level")));
                Log.i(TAG, "BatteryStartLevel: " + parser.getAttributeValue(null, "start_level"));
            } else {
                Log.i(TAG, "BatteryStartLevel: ignore.");
            }
        } else {
            Log.e(TAG, "BatteryStartLevel: Invalid Argument!");
        }

        // If battery end level set.
        if (parser.getAttributeValue(null, "end_level") != null) {
            // If value is valid.
            if (Integer.parseInt(parser.getAttributeValue(null, "end_level")) >= 0
                    && Integer.parseInt(parser.getAttributeValue(null, "end_level")) <= 100) {

                trigger.setBatteryEndLevel(Integer.parseInt(parser.getAttributeValue(null, "end_level")));
                Log.i(TAG, "BatteryEndLevel: " + parser.getAttributeValue(null, "end_level"));
            } else {
                Log.i(TAG, "BatteryEndLevel: ignore.");
            }
        } else {
            Log.e(TAG, "BatteryEndLevel: Invalid Argument!");
        }

        // If battery state set.
		if (parser.getAttributeValue(null, "state") != null) {

            // Listen for battery state.
			if (parser.getAttributeValue(null, "state").equals("1")) {
				trigger.setBatteryState(Trigger.listen_state.listen_on);
				Log.i(TAG, "BatteryState listen on.");
            // Don't listen for battery state.
            } else if (parser.getAttributeValue(null, "state").equals("0")) {
				trigger.setBatteryState(Trigger.listen_state.listen_off);
				Log.i(TAG, "BatteryState listen off.");
			} else {
				Log.i(TAG, "BateryState: ignore.");
			}
		} else {
			Log.e(TAG, "BatteryState: Invalid Argument!");
		}

		parser.nextTag();
	}

    /**
     * Set the headphones state.
     *
     * @param parser The parser to read the tags.
     * @param trigger Trigger.
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
	private void setHeadphone(XmlPullParser parser, Trigger trigger) throws XmlPullParserException, IOException {

        parser.require(XmlPullParser.START_TAG, null, "headphone");

        // If headphones state set.
		if (parser.getAttributeValue(null, "state") != null) {

            // Listen for headphones state.
			if (parser.getAttributeValue(null, "state").equals("1")) {
				trigger.setHeadphones(Trigger.listen_state.listen_on);
				Log.i(TAG, "Headphones listen on.");
            // Listen for headphones state.
			} else if (parser.getAttributeValue(null, "state").equals("0")) {
				trigger.setHeadphones(Trigger.listen_state.listen_off);
				Log.i(TAG, "Headphones listen off.");
			} else {
				Log.i(TAG, "Headphones: ignore.");
			}
		} else {
			Log.e(TAG, "Headphones: Invalid Argument!");
		}

		parser.nextTag();
	}

    /**
     * Set the geofence state.
     *
     * @param parser The parser to read the tags.
     * @param trigger Trigger.
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
	private void setGeofence(XmlPullParser parser, Trigger trigger) throws XmlPullParserException, IOException {

        parser.require(XmlPullParser.START_TAG, null, "geofence");

        // If a geo-fence is selected.
		if (parser.getAttributeValue(null, "id") != null) {

            // If the geo-fence is valid.
			if (!parser.getAttributeValue(null, "id").equals("")) {
                trigger.setGeofence(parser.getAttributeValue(null, "id"));
				Log.i(TAG, "Geofence: " + trigger.getGeofence());
			} else {
				trigger.setGeofence(null);
				Log.i(TAG, "Geofence: ignore");
			}
		} else {
			Log.e(TAG, "Geofence: Invalid Argument!");
		}

		parser.nextTag();
	}

    /**
     * Set the priority.
     *
     * @param parser The parser to read the tags.
     * @param trigger Trigger.
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
	private void setPriority(XmlPullParser parser, Trigger trigger) throws XmlPullParserException, IOException {

        parser.require(XmlPullParser.START_TAG, null, "priority");

        // If value is set.
		if (parser.getAttributeValue(null, "value") != null) {
            // If value is valid.
			if (Integer.parseInt(parser.getAttributeValue(null, "value")) >= 0
					&& Integer.parseInt(parser.getAttributeValue(null, "value")) <= 99) {

                trigger.setPriority(Integer.parseInt(parser.getAttributeValue(null, "value")));
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
     * Set days.
     *
     * @param parser The parser to read the tags.
     * @param trigger Trigger.
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
	private void setWeekdays(XmlPullParser parser, Trigger trigger) throws XmlPullParserException, IOException {

        parser.require(XmlPullParser.START_TAG, null, "weekdays");
		Set<String> weekdays = new HashSet<>();

        // If Monday is selected.
		if (parser.getAttributeValue(null, "mon") != null) {
			if (parser.getAttributeValue(null, "mon").equals("true")) {
				weekdays.add("1");
				Log.i(TAG, "weekdays: monday");
			} else {
				Log.i(TAG, "weekdays: no monday");
			}
		}

        // If Tuesday is selected.
        if (parser.getAttributeValue(null, "tue") != null) {
			if (parser.getAttributeValue(null, "tue").equals("true")) {
				weekdays.add("2");
				Log.i(TAG, "weekdays: tuesday");
			} else {
				Log.i(TAG, "weekdays: no tuesday");
			}
		}

        // If Wednesday is selected.
		if (parser.getAttributeValue(null, "wed") != null) {
			if (parser.getAttributeValue(null, "wed").equals("true")) {
				weekdays.add("3");
				Log.i(TAG, "weekdays: wednesday");
			} else {
				Log.i(TAG, "weekdays: no wednesday");
			}
		}

        // If Thursday is selected.
		if (parser.getAttributeValue(null, "thur") != null) {
			if (parser.getAttributeValue(null, "thur").equals("true")) {
				weekdays.add("4");
				Log.i(TAG, "weekdays: thursday");
			} else {
				Log.i(TAG, "weekdays: no thursday");
			}
		}

        // If Friday is selected.
		if (parser.getAttributeValue(null, "fri") != null) {
			if (parser.getAttributeValue(null, "fri").equals("true")) {
				weekdays.add("5");
				Log.i(TAG, "weekdays: friday");
			} else {
				Log.i(TAG, "weekdays: no friday");
			}
		}

        // If Saturday is selected.
		if (parser.getAttributeValue(null, "sat") != null) {
			if (parser.getAttributeValue(null, "sat").equals("true")) {
				weekdays.add("6");
				Log.i(TAG, "weekdays: saturday");
			} else {
				Log.i(TAG, "weekdays: no saturday");
			}
		}

        // If Sunday is selected.
		if (parser.getAttributeValue(null, "sun") != null) {
			if (parser.getAttributeValue(null, "sun").equals("true")) {
				weekdays.add("7");
				Log.i(TAG, "weekdays: sunday");
			} else {
				Log.i(TAG, "weekdays: no sunday");
			}
		}
		
		trigger.setWeekdays(weekdays);

		parser.nextTag();
	}
}