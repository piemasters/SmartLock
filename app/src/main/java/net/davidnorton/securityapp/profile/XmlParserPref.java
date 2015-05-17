package net.davidnorton.securityapp.profile;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Stores xml values in shared preferences for Profile edit activity.
 *
 * @author David Norton
 */
public class XmlParserPref {

	final static String TAG = "XmlParserPref";

	Context context;
	Editor prefEdit;
	String profileName;

    /**
     * Initializes the xml parser with given context.
     *
     * @param cont Context.
     * @param name Profile Name.
     */
	public XmlParserPref(Context cont, String name) {
		context = cont;
		prefEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
		profileName = name;
	}

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
     * Reads and applies settings using setter methods.
     *
     * @param parser The parser to read the tags.
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
	private void readAndApplyTags(XmlPullParser parser) throws XmlPullParserException, IOException {

		prefEdit.putString("name", profileName);

		parser.require(XmlPullParser.START_TAG, null, "resources");

		while (parser.next() != XmlPullParser.END_TAG) {

            if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}

			String name = parser.getName();

            switch (name) {
                case "lockscreen":
                    setLockscreen(parser);
                    break;
                case "wifi":
                    setWifi(parser);
                    break;
                case "mobile_data":
                    setMobileData(parser);
                    break;
                case "bluetooth":
                    setBluetooth(parser);
                    break;
                case "display":
                    setDisplay(parser);
                    break;
                case "ringer_mode":
                    setRingerMode(parser);
                    break;
                default:
                    Log.i("XmlParser", "Skip!");
                    parser.nextTag();
                    break;
            }
			prefEdit.commit();
		}
	}

    /**
     * Sets Lockscreen state.
     *
     * @param parser The parser to read the settings.
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void setLockscreen(XmlPullParser parser) throws XmlPullParserException, IOException {

        parser.require(XmlPullParser.START_TAG, null, "lockscreen");

        // If state has changed.
        if (parser.getAttributeValue(null, "enabled") != null) {
            // If enabled.
            if (parser.getAttributeValue(null, "enabled").equals("1")) {
                prefEdit.putString("lockscreen", "enabled");
                Log.i(TAG, "Lockscreen enabled.");
                // If disabled.
            } else if (parser.getAttributeValue(null, "enabled").equals("0")) {
                prefEdit.putString("lockscreen", "disabled");
                Log.i(TAG, "Lockscreen off.");
                // If unchanged.
            } else if (parser.getAttributeValue(null, "enabled").equals("-1")) {
                prefEdit.putString("lockscreen", "unchanged");
                Log.i(TAG, "Lockscreen unchanged.");
                // If not valid.
            } else {
                Log.e(TAG, "Lockscreen: Invalid Argument!");
            }
            // If state unchanged.
        } else {
            Log.i(TAG, "Lockscreen: No change.");
        }
        parser.nextTag();
    }

    /**
     * Sets WiFi state.
     *
     * @param parser The parser to read the settings.
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void setWifi(XmlPullParser parser) throws XmlPullParserException, IOException {

        parser.require(XmlPullParser.START_TAG, null, "wifi");

        // If state has changed.
        if (parser.getAttributeValue(null, "enabled") != null) {
            // If enabled.
            if (parser.getAttributeValue(null, "enabled").equals("1")) {
                prefEdit.putString("wifi", "enabled");
                Log.i(TAG, "WiFi on.");
            // If disabled.
            } else if (parser.getAttributeValue(null, "enabled").equals("0")) {
                prefEdit.putString("wifi", "disabled");
                Log.i(TAG, "WiFi off.");
            // If unchanged.
            } else if (parser.getAttributeValue(null, "enabled").equals("-1")) {
                prefEdit.putString("wifi", "unchanged");
                Log.i(TAG, "WiFi unchanged.");
            // If not valid.
            } else {
                Log.e(TAG, "WiFi: Invalid Argument!");
            }
        // If state unchanged.
        } else {
            Log.i(TAG, "WiFi: No change.");
        }
        parser.nextTag();
    }

    /**
     *  Sets Mobile Data state.
     *
     * @param parser The parser to read the settings.
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void setMobileData(XmlPullParser parser) throws XmlPullParserException, IOException {

        parser.require(XmlPullParser.START_TAG, null, "mobile_data");

        if (parser.getAttributeValue(null, "enabled") != null) {
            if (parser.getAttributeValue(null, "enabled").equals("1")) {
                prefEdit.putString("mobile_data", "enabled");
                Log.i(TAG, "MobileData on.");
            } else if (parser.getAttributeValue(null, "enabled").equals("0")) {
                prefEdit.putString("mobile_data", "disabled");
                Log.i(TAG, "MobileData off.");
            } else if (parser.getAttributeValue(null, "enabled").equals("-1")) {
                prefEdit.putString("mobile_data", "unchanged");
                Log.i(TAG, "MobileData unchanged.");
            } else {
                Log.e(TAG, "MobileData: Invalid Argument!");
            }
        } else {
            Log.i(TAG, "MobileData: No change.");
        }
        parser.nextTag();
    }

    /**
     *  Sets Bluetooth state.
     *
     * @param parser The parser to read the settings.
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void setBluetooth(XmlPullParser parser) throws XmlPullParserException, IOException {

        parser.require(XmlPullParser.START_TAG, null, "bluetooth");

        if (parser.getAttributeValue(null, "enabled") != null) {
            if (parser.getAttributeValue(null, "enabled").equals("1")) {
                prefEdit.putString("bluetooth", "enabled");
                Log.i(TAG, "Bluetooth on.");
            } else if (parser.getAttributeValue(null, "enabled").equals("0")) {
                prefEdit.putString("bluetooth", "disabled");
                Log.i(TAG, "Bluetooth off.");
            } else if (parser.getAttributeValue(null, "enabled").equals("-1")) {
                prefEdit.putString("bluetooth", "unchanged");
                Log.i(TAG, "Bluetooth unchanged.");
            } else {
                Log.e(TAG, "Bluetooth: Invalid Argument!");
            }
        } else {
            Log.i(TAG, "Bluetooth: No change.");
        }
        parser.nextTag();
    }

    /**
     *  Sets Display states.
     *
     * @param parser The parser to read the settings.
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void setDisplay(XmlPullParser parser) throws XmlPullParserException, IOException {

        parser.require(XmlPullParser.START_TAG, null, "display");

        // Auto Brightness
        if (parser.getAttributeValue(null, "auto_mode_enabled") != null) {
            if (parser.getAttributeValue(null, "auto_mode_enabled").equals("1")) {
                prefEdit.putString("display_auto_mode", "enabled");
                Log.i(TAG, "ScreenBrightnessAutoMode on.");
            } else if (parser.getAttributeValue(null, "auto_mode_enabled").equals("0")) {
                prefEdit.putString("display_auto_mode", "disabled");
                Log.i(TAG, "ScreenBrightnessAutoMode off.");
            } else if (parser.getAttributeValue(null, "auto_mode_enabled").equals("-1")) {
                prefEdit.putString("display_auto_mode", "unchanged");
                Log.i(TAG, "ScreenBrightnessAutoMode unchanged.");
            } else {
                Log.e(TAG, "ScreenBrightnessAutoMode: Invalid Argument!");
            }
        } else {
            Log.i(TAG, "ScreenBrightnessAutoMode: No change.");
        }

        // Device Display Timeout
        if (parser.getAttributeValue(null, "time_out") != null) {
            if (Integer.parseInt(parser.getAttributeValue(null, "time_out")) >= -1
                    && Integer.parseInt(parser.getAttributeValue(null, "time_out")) <= 6) {
                prefEdit.putString("display_time_out", parser.getAttributeValue(null, "time_out"));
                Log.i(TAG, "TimeOut: " + parser.getAttributeValue(null, "time_out"));
            } else {
                Log.e(TAG, "TimeOut: Invalid Argument!");
            }
        } else {
            Log.i(TAG, "TimeOut: No change.");
        }

        parser.nextTag();
    }

    /**
     * Sets Ringer Mode states.
     *
     * @param parser The parser to read the settings.
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
	private void setRingerMode(XmlPullParser parser) throws XmlPullParserException, IOException {

		parser.require(XmlPullParser.START_TAG, null, "ringer_mode");

		if (parser.getAttributeValue(null, "mode") != null) {
			if (parser.getAttributeValue(null, "mode").equals("normal")) {
				prefEdit.putString("ringer_mode", "normal");
				Log.i(TAG, "RingerMode: normal");
			} else if (parser.getAttributeValue(null, "mode").equals("silent")) {
				prefEdit.putString("ringer_mode", "silent");
				Log.i(TAG, "RingerMode: silent");
			} else if (parser.getAttributeValue(null, "mode").equals("vibrate")) {
				prefEdit.putString("ringer_mode", "vibrate");
				Log.i(TAG, "RingerMode: vibrate");
			} else if (parser.getAttributeValue(null, "mode").equals("unchanged")) {
				prefEdit.putString("ringer_mode", "unchanged");
				Log.i(TAG, "RingerMode: unchanged");
			} else {
				Log.e(TAG, "RingerMode: Invalid Argument!");
			}
		} else {
			Log.i(TAG, "RingerMode: No change.");
		}

		parser.nextTag();
	}
}