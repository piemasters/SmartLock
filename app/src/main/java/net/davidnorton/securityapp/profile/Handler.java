package net.davidnorton.securityapp.profile;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import net.davidnorton.securityapp.R;
import net.davidnorton.securityapp.ui.MainActivity;

import org.xmlpull.v1.XmlPullParserException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**
 * Used to create and apply Profiles and also
 * updates the notification on a Profile change.
 *
 * @author David Norton
 */
public class Handler {

    private final Context context;
    private final SharedPreferences pref;

    public Handler(Context cont) {
        context = cont;
        pref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Applies a Profile, updates the notification and displays a toast.
     *
     * @param name Name of Profile.
     */
    public void applyProfile(String name) {

        // Apply the profile.
        XmlParser parser = new XmlParser(context);
        try {
            parser.initializeXmlParser(context.openFileInput(name + "_profile.xml"));
        } catch (Resources.NotFoundException | IOException | XmlPullParserException e) {
            e.printStackTrace();
        }

        // Save profile as current active profile.
        pref.edit().putString("active_profile", name).apply();

        // Update the notification.
        if (pref.getBoolean("notification", true)) {
            updateNotification();
        }

        // Show toast confirming the Profile was applied.
        Toast toast = Toast.makeText(context, name + " " + context.getResources().getString(R.string.profile_applied),Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * Applies a Profile, updates the notification but displays no toast.
     *
     * @param name Name of Profile.
     */
    public void applyProfileHidden(String name) {

        // Apply the profile.
        XmlParser parser = new XmlParser(context);
        try {
            parser.initializeXmlParser(context.openFileInput(name + "_profile.xml"));
        } catch (Resources.NotFoundException | IOException | XmlPullParserException e) {
            e.printStackTrace();
        }

        // Save profile as current active profile.
        pref.edit().putString("active_profile", name).apply();

        // Update the notification.
        if (pref.getBoolean("notification", true)) {
            updateNotification();
        }
    }

    /**
     * Applies a profile directly for use with NFC tags.
     *
     * @param profile The profile-object.
     */
    public void applyProfile(Profile profile) {
        //TODO: this is for NFC

        Setter setter = new Setter();

        // Lockscreen
        if (profile.getLockscreen() == Profile.state.enabled) {
            setter.setLockscreen(context, true);
        } else if (profile.getLockscreen() == Profile.state.disabled) {
            setter.setLockscreen(context, false);
        }

        // WiFi
        if (profile.getWifi() == Profile.state.enabled) {
            setter.setWifi(context, true);
        } else if (profile.getWifi() == Profile.state.disabled) {
            setter.setWifi(context, false);
        }

        // Mobile Data
        try {
            if (profile.getMobileData() == Profile.state.enabled) {
                setter.setMobileData(context, true);
            } else if (profile.getMobileData() == Profile.state.disabled) {
                setter.setMobileData(context, false);
            }
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            Log.i("Handler", String.format("Not supported by Android Version"));
        }

        // Bluetooth
        if (profile.getBluetooth() == Profile.state.enabled) {
            setter.setBluetooth(context, true);
        } else if (profile.getBluetooth() == Profile.state.disabled) {
            setter.setBluetooth(context, false);
        }

        // Auto Screen Brightness
        if (profile.getScreenBrightnessAutoMode() == Profile.state.enabled) {
            setter.setScreenBrightnessMode(context, true);
        } else if (profile.getScreenBrightnessAutoMode() == Profile.state.disabled) {
            setter.setScreenBrightnessMode(context, false);
        }

        // Display Timeout
        if (profile.getScreenTimeOut() != -1) {
            setter.setScreenTimeout(context, profile.getScreenTimeOut());
        }

        // Ringer Mode
        setter.setRingerMode(context, profile.getRingerMode());

        // Saves the active profile into the shared preferences
        pref.edit().putString("active_profile", profile.getName()).commit();

        // Update the notification.
        if (pref.getBoolean("notification", true)) {
            updateNotification();
        }

        // Show toast confirming the Profile was applied.
        Toast toast = Toast.makeText(context, profile.getName() + " was applied!", Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * Updates the notification
     */
    public void updateNotification() {

        Intent resultIntent = new Intent(context, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context,0, resultIntent, 0);
        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(context);
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);

        // Set the background colour and icon.
        int color = ContextCompat.getColor(context, R.color.notification_background);

        nBuilder.setColor(color);
        nBuilder.setLargeIcon(largeIcon);

        // Set text and hide time.
        nBuilder.setContentText(context.getResources().getString(R.string.notification_content));
        nBuilder.setContentTitle(pref.getString("active_profile", context.getResources().getString(R.string.notification_title_no_profile)));
        nBuilder.setWhen(0);

        // Set small icon based on Profile selected.
        nBuilder.setSmallIcon(R.drawable.notification_icon);
        if(pref.getString("active_profile", context.getResources().getString(R.string.notification_title_no_profile)).equals("Home")) {
            nBuilder.setSmallIcon(R.drawable.notification_icon_home);
        } else if (pref.getString("active_profile", context.getResources().getString(R.string.notification_title_no_profile)).equals("Travel")) {
            nBuilder.setSmallIcon(R.drawable.notification_icon_travel);
        } else if (pref.getString("active_profile", context.getResources().getString(R.string.notification_title_no_profile)).equals("Work")) {
            nBuilder.setSmallIcon(R.drawable.notification_icon_work);
        }

        // Open app when selected, set priority level and make permanent.
        nBuilder.setContentIntent(resultPendingIntent);
        nBuilder.setPriority(1);
        nBuilder.setOngoing(true);

        // Create notification.
        Notification notification = nBuilder.build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(123, notification);
    }

    /**
     * Creates the default Profiles.
     */
    public void createDefaultProfiles() {

        // Assign Default Profile preference settings.
        Profile pDefault = new Profile("Default");
        pDefault.setLockscreen(Profile.state.enabled);
        pDefault.setWifi(Profile.state.disabled);
        pDefault.setMobileData(Profile.state.enabled);
        pDefault.setBluetooth(Profile.state.disabled);
        pDefault.setScreenBrightnessAutoMode(Profile.state.enabled);
        pDefault.setRingerMode(Profile.mode.normal);

        // Assign Home Profile preference settings.
        Profile pHome = new Profile("Home");
        pDefault.setLockscreen(Profile.state.disabled);
        pHome.setWifi(Profile.state.enabled);
        pHome.setMobileData(Profile.state.disabled);
        pHome.setBluetooth(Profile.state.enabled);
        pHome.setScreenBrightnessAutoMode(Profile.state.enabled);
        pHome.setRingerMode(Profile.mode.normal);

        // Assign Travel Profile preference settings.
        Profile pTravel = new Profile("Travel");
        pDefault.setLockscreen(Profile.state.enabled);
        pTravel.setWifi(Profile.state.disabled);
        pTravel.setMobileData(Profile.state.enabled);
        pTravel.setBluetooth(Profile.state.disabled);
        pTravel.setScreenBrightnessAutoMode(Profile.state.enabled);
        pTravel.setRingerMode(Profile.mode.vibrate);

        // Assign Work Profile preference settings.
        Profile pWork = new Profile("Work");
        pDefault.setLockscreen(Profile.state.enabled);
        pWork.setWifi(Profile.state.enabled);
        pWork.setMobileData(Profile.state.disabled);
        pWork.setBluetooth(Profile.state.disabled);
        pWork.setScreenBrightnessAutoMode(Profile.state.enabled);
        pWork.setRingerMode(Profile.mode.vibrate);

        // Create Profile files based on these defined preferences.
        XmlCreator creator = new XmlCreator();
        FileOutputStream output;
        try {
            output = context.openFileOutput(pDefault.getName() + "_profile.xml", Context.MODE_PRIVATE);
            output.write(creator.create(pDefault).getBytes());
            output.close();

            output = context.openFileOutput(pHome.getName() + "_profile.xml", Context.MODE_PRIVATE);
            output.write(creator.create(pHome).getBytes());
            output.close();

            output = context.openFileOutput(pTravel.getName() + "_profile.xml", Context.MODE_PRIVATE);
            output.write(creator.create(pTravel).getBytes());
            output.close();

            output = context.openFileOutput(pWork.getName() + "_profile.xml", Context.MODE_PRIVATE);
            output.write(creator.create(pWork).getBytes());
            output.close();
        } catch (IOException | ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
    }
}
