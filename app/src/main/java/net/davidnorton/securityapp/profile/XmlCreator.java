package net.davidnorton.securityapp.profile;

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
 * Converts a Profile into a string ready to be processed.
 *
 * @author David Norton
 */
public class XmlCreator {

	final static String TAG = "XmlCreator";
	
	DocumentBuilderFactory buildFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder builder;
	TransformerFactory transFactory = TransformerFactory.newInstance();
	Transformer transformer;

    /**
     * Creates a string storing the selected user preferences and saves to an xml.
     *
     * @param profile The profile object.
     *
     * @return The created xml string.
     *
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     */
    public String create(Profile profile) throws ParserConfigurationException, TransformerException, IOException {

        builder = buildFactory.newDocumentBuilder();
        Document xmlProfile = builder.newDocument();

        // Create xml root tag.
        Element rootElement = xmlProfile.createElement("resources");
        xmlProfile.appendChild(rootElement);

        // Declare output format.
        Properties outputProperties = new Properties();
        outputProperties.setProperty(OutputKeys.INDENT, "yes");
        outputProperties.setProperty(OutputKeys.METHOD, "xml");
        outputProperties.setProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        outputProperties.setProperty(OutputKeys.VERSION, "1.0");
        outputProperties.setProperty(OutputKeys.ENCODING, "UTF-8");

        // Lock Screen
        Element lockscreenElement = xmlProfile.createElement("lockscreen");

        if(profile.getLockscreen() != Profile.state.unchanged){
            lockscreenElement.setAttribute("enabled", String.format("%s", profile.getLockscreen().ordinal()));
        } else {
            lockscreenElement.setAttribute("enabled", String.format("%s", -1));
        }
        rootElement.appendChild(lockscreenElement);
        Log.i(TAG, String.format("lockscreen was defined as %s", profile.getLockscreen()));

        // WiFi
        Element wifiElement = xmlProfile.createElement("wifi");

        if (profile.getWifi() != Profile.state.unchanged) {
            wifiElement.setAttribute("enabled", String.format("%s", profile.getWifi().ordinal()));
        } else {
            wifiElement.setAttribute("enabled", String.format("%s", -1));
        }
        rootElement.appendChild(wifiElement);
        Log.i(TAG, String.format("wifi was defined as %s", profile.getWifi()));

        // Mobile Data
        Element dataElement = xmlProfile.createElement("mobile_data");

        if (profile.getMobileData() != Profile.state.unchanged) {
            dataElement.setAttribute("enabled", String.format("%s", profile.getMobileData().ordinal()));
        } else {
            dataElement.setAttribute("enabled", String.format("%s", -1));
        }
        rootElement.appendChild(dataElement);
        Log.i(TAG, String.format("mobile-data was defined as %s", profile.getMobileData()));

        // Bluetooth
        Element bluetoothElement = xmlProfile.createElement("bluetooth");

        if (profile.getBluetooth() != Profile.state.unchanged) {
            bluetoothElement.setAttribute("enabled", String.format("%s", profile.getBluetooth().ordinal()));
        } else {
            bluetoothElement.setAttribute("enabled", String.format("%s", -1));
        }
        rootElement.appendChild(bluetoothElement);
        Log.i(TAG, String.format("bluetooth was defined as %s", profile.getBluetooth()));

        // Display
        Element displayElement = xmlProfile.createElement("display");

        // Auto Brightness
        if (profile.getScreenBrightnessAutoMode() != Profile.state.unchanged) {
            displayElement.setAttribute("auto_mode_enabled", String.format("%d", profile.getScreenBrightnessAutoMode().ordinal()));
        } else {
            displayElement.setAttribute("auto_mode_enabled", String.format("%d", -1));
        }

        // Display Time Out
        if (profile.getScreenTimeOut() >= -1) {
            displayElement.setAttribute("time_out", String.format("%d", profile.getScreenTimeOut()));
        }

        rootElement.appendChild(displayElement);
        Log.i(TAG, String.format("display changes were defined as follows: autoMode: %s timeOut: %s",
                        profile.getScreenBrightnessAutoMode(), profile.getScreenTimeOut()));

        // Ringer Mode
		Element ringerModeElement = xmlProfile.createElement("ringer_mode");
		ringerModeElement.setAttribute("mode", String.format("%s", profile.getRingerMode()));
		rootElement.appendChild(ringerModeElement);
		Log.i(TAG, String.format("vibration was defined as %s", profile.getRingerMode()));

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