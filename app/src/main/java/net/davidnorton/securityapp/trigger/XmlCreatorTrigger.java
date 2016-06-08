package net.davidnorton.securityapp.trigger;

import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Converts a Trigger into a string ready to be processed.
 *
 * @author David Norton
 */
class XmlCreatorTrigger {

	private final static String TAG = "XmlCreatorTrigger";
	
	private final DocumentBuilderFactory buildFactory = DocumentBuilderFactory.newInstance();
	private DocumentBuilder builder;
	private final TransformerFactory transFactory = TransformerFactory.newInstance();
	private Transformer transformer;

    /**
     * Creates a string storing the selected user preferences and saves to an xml.
     *
     * @param trigger The profile object.
     *
     * @return The created xml string.
     *
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     */
	public String create(Trigger trigger) throws ParserConfigurationException, TransformerException, IOException {

        builder = buildFactory.newDocumentBuilder();
		Document xmlProfile = builder.newDocument();

        // Create xml root tag.
		Element rootElement = xmlProfile.createElement("trigger");
		xmlProfile.appendChild(rootElement);

        // Declare output format.
		Properties outputProperties = new Properties();
		outputProperties.setProperty(OutputKeys.INDENT, "yes");
		outputProperties.setProperty(OutputKeys.METHOD, "xml");
		outputProperties.setProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		outputProperties.setProperty(OutputKeys.VERSION, "1.0");
		outputProperties.setProperty(OutputKeys.ENCODING, "UTF-8");

		// Selected profile name.
		Element profile = xmlProfile.createElement("profile");
		profile.setAttribute("name", String.format("%s", trigger.getProfileName()));
		rootElement.appendChild(profile);
		Log.i(TAG, String.format("Profile was selected: %s", trigger.getProfileName()));

		// Priority.
		Element priority = xmlProfile.createElement("priority");
		priority.setAttribute("value", String.valueOf(trigger.getPriority()));
		rootElement.appendChild(priority);
		Log.i(TAG, String.format("Priority was selected: %s", trigger.getPriority()));

		// Time changes.
		if (trigger.getStartMinutes() >= -1 && trigger.getStartHours() >= -1) {
			Element timeElement = xmlProfile.createElement("time");

			if (trigger.getStartHours() >= -1)
				timeElement.setAttribute("start_hours", String.format("%d", trigger.getStartHours()));
			if (trigger.getStartMinutes() >= -1)
				timeElement.setAttribute("start_minutes", String.format("%d", trigger.getStartMinutes()));
			if (trigger.getEndHours() >= -1)
				timeElement.setAttribute("end_hours", String.format("%d", trigger.getEndHours()));
			if (trigger.getEndMinutes() >= -1)
				timeElement.setAttribute("end_minutes", String.format("%d", trigger.getEndMinutes()));

			Log.i(TAG,String.format(
                    "trigger changes were defined as follows: start_hours: %s start_minutes: %s end_minutes: %s end_hours: %s",
                    trigger.getStartHours(), trigger.getStartMinutes(), trigger.getEndHours(), trigger.getEndMinutes()));

            rootElement.appendChild(timeElement);
		}

		// Battery changes.
		if ( trigger.getBatteryState() != null && trigger.getBatteryStartLevel() >= -1 && trigger.getBatteryEndLevel() >= -1) {

            Element batteryElement = xmlProfile.createElement("battery");

            // Battery state.
			if (trigger.getBatteryState() != Trigger.listen_state.ignore) {
				batteryElement.setAttribute("state", String.format("%d", trigger.getBatteryState().ordinal()));
			} else {
				batteryElement.setAttribute("state", String.format("%d", -1));
			}

            // Battery start level.
            if (trigger.getBatteryStartLevel() >= -1) {
                batteryElement.setAttribute("start_level",
                        String.format("%d", trigger.getBatteryStartLevel()));
            }

            // Battery end level.
            if (trigger.getBatteryEndLevel() >= -1) {
                batteryElement.setAttribute("end_level",
                        String.format("%d", trigger.getBatteryEndLevel()));
            }

            Log.i(TAG, String.format("trigger changes were defined as follows:  battery_state: %s", trigger.getBatteryState()));

            rootElement.appendChild(batteryElement);
		}

		// Headphone changes.
		if (trigger.getHeadphones() != null) {

            Element headphoneElement = xmlProfile.createElement("headphone");

			if (trigger.getHeadphones() != Trigger.listen_state.ignore) {
				headphoneElement.setAttribute("state", String.format("%d", trigger.getHeadphones().ordinal()));
			} else {
				headphoneElement.setAttribute("state", String.format("%d", -1));
			}

			Log.i(TAG, String.format("trigger changes were defined as follows: headphone_state: %s", trigger.getHeadphones()));

            rootElement.appendChild(headphoneElement);
		}

		// Geo-fence changes.
		Element geofenceElement = xmlProfile.createElement("geofence");

		if (trigger.getGeofence() != null) {
			geofenceElement.setAttribute("id", trigger.getGeofence());
		} else {
			geofenceElement.setAttribute("id", "");
		}

		Log.i(TAG, String.format("trigger changes were defined as follows: trigger_name: %s", trigger.getGeofence()));
		rootElement.appendChild(geofenceElement);

		// Day changes.
		if (trigger.getWeekdays() != null) {

            Element weekdayElement = xmlProfile.createElement("weekdays");

			if (trigger.getWeekdays().contains("1")) {
				weekdayElement.setAttribute("mon", "true");
			} else {
				weekdayElement.setAttribute("mon", "false");
			}
			if (trigger.getWeekdays().contains("2")) {
				weekdayElement.setAttribute("tue", "true");
			} else {
				weekdayElement.setAttribute("tue", "false");
			}
			if (trigger.getWeekdays().contains("3")) {
				weekdayElement.setAttribute("wed", "true");
			} else {
				weekdayElement.setAttribute("wed", "false");
			}
			if (trigger.getWeekdays().contains("4")) {
				weekdayElement.setAttribute("thur", "true");
			} else {
				weekdayElement.setAttribute("thur", "false");
			}
			if (trigger.getWeekdays().contains("5")) {
				weekdayElement.setAttribute("fri", "true");
			} else {
				weekdayElement.setAttribute("fri", "false");
			}
			if (trigger.getWeekdays().contains("6")) {
				weekdayElement.setAttribute("sat", "true");
			} else {
				weekdayElement.setAttribute("sat", "false");
			}
			if (trigger.getWeekdays().contains("7")) {
				weekdayElement.setAttribute("sun", "true");
			} else {
				weekdayElement.setAttribute("sun", "false");
			}

			Log.i(TAG, String.format("trigger changes were defined as follows: weekday number: %s", trigger.getWeekdays().size()));

            rootElement.appendChild(weekdayElement);
		}

        // Write complete xml file.
		transformer = transFactory.newTransformer();
		transformer.setOutputProperties(outputProperties);
		DOMSource domSource = new DOMSource(xmlProfile.getDocumentElement());

		OutputStream output = new ByteArrayOutputStream();
		StreamResult result = new StreamResult(output);
		transformer.transform(domSource, result);
		String xmlString = output.toString();

		return xmlString;
	}
}