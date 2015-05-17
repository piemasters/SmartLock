package net.davidnorton.securityapp.profile;

import java.io.IOException;
        import java.io.InputStream;
        import java.lang.reflect.InvocationTargetException;

        import org.xmlpull.v1.XmlPullParser;
        import org.xmlpull.v1.XmlPullParserException;

        import android.content.Context;
        import android.util.Log;
        import android.util.Xml;

/**
 * Reads an xml input stream created in XmlCreator and applies it
 * using the Setter class.
 *
 * @author David Norton
 */
public class XmlParser {

    final static String TAG = "XmlParser";

    Context context;
    Setter setter = new Setter();

    /**
     *  Initializes the xml parser with the given context.
     *
     * @param cont Context.
     */
    public XmlParser(Context cont) {
        context = cont;
    }

    /**
     * Sets up the xml parser for the inputstream, then hands over to readAndApplyTags to process it.
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

        parser.require(XmlPullParser.START_TAG, null, "resources");

        // For each tag.
        while (parser.next() != XmlPullParser.END_TAG) {

            // Skip if not a start tag.
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();

            // Look for the entry tag.
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
                    Log.w("XmlParser", "Skip!");
                    parser.nextTag();
                    break;
            }
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
                setter.setLockscreen(context, true);
                Log.i(TAG, "Lockscreen enabled.");
                // If disabled.
            } else if (parser.getAttributeValue(null, "enabled").equals("0")) {
                setter.setLockscreen(context, false);
                Log.i(TAG, "Lockscreen disabled.");
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
                setter.setWifi(context, true);
                Log.i(TAG, "WiFi on.");
            // If disabled.
            } else if (parser.getAttributeValue(null, "enabled").equals("0")) {
                setter.setWifi(context, false);
                Log.i(TAG, "WiFi off.");
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
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void setMobileData(XmlPullParser parser) throws XmlPullParserException, IOException {

        parser.require(XmlPullParser.START_TAG, null, "mobile_data");

        // If state has changed.
        if (parser.getAttributeValue(null, "enabled") != null) {
            // If enabled.
            if (parser.getAttributeValue(null, "enabled").equals("1")) {
                try {
                    setter.setMobileData(context, true);
                } catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException |
                        IllegalAccessException | NoSuchFieldException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "MobileData on.");
            // If disabled.
            } else if (parser.getAttributeValue(null, "enabled").equals("0")) {
                try {
                    setter.setMobileData(context, false);
                } catch (IllegalArgumentException | ClassNotFoundException | NoSuchFieldException |
                        IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    Log.i(TAG, String.format(" ERROR: Mobile Data change not supported by Android Version!"));
                }
                Log.i(TAG, "MobileData off.");
            // If not valid.
            } else {
                Log.e(TAG, "MobileData: Invalid Argument!");
            }
        // If state unchanged.
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
                setter.setBluetooth(context, true);
                Log.i(TAG, "Bluetooth on.");
            } else if (parser.getAttributeValue(null, "enabled").equals("0")) {
                setter.setBluetooth(context, false);
                Log.i(TAG, "Bluetooth off.");
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
                setter.setScreenBrightnessMode(context, true);
                Log.i(TAG, "ScreenBrightnessAutoMode on.");
            } else if (parser.getAttributeValue(null, "auto_mode_enabled").equals("0")) {
                setter.setScreenBrightnessMode(context, false);
                Log.i(TAG, "ScreenBrightnessAutoMode off.");
            } else {
                Log.e(TAG, "ScreenBrightnessAutoMode: Invalid Argument!");
            }
        } else {
            Log.i(TAG, "ScreenBrightnessAutoMode: No change.");
        }

        // Device Display Timeout
        if (parser.getAttributeValue(null, "time_out") != null) {
            if (Integer.parseInt(parser.getAttributeValue(null, "time_out")) >= 0
                    && Integer.parseInt(parser.getAttributeValue(null, "time_out")) <= 6) {
                setter.setScreenTimeout(context, Integer.parseInt(parser.getAttributeValue(null, "time_out")));
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
                setter.setRingerMode(context, Profile.mode.normal);
                Log.i(TAG, "RingerMode: normal");
            } else if (parser.getAttributeValue(null, "mode").equals("silent")) {
                setter.setRingerMode(context, Profile.mode.silent);
                Log.i(TAG, "RingerMode: silent");
            } else if (parser.getAttributeValue(null, "mode").equals("vibrate")) {
                setter.setRingerMode(context, Profile.mode.vibrate);
                Log.i(TAG, "RingerMode: vibrate");
            } else {
                Log.e(TAG, "RingerMode: Invalid Argument!");
            }
        } else {
            Log.i(TAG, "RingerMode: No change.");
        }
        parser.nextTag();
    }
}
