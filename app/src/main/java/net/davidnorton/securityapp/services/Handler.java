package net.davidnorton.securityapp.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import net.davidnorton.securityapp.R;
import net.davidnorton.securityapp.profile.XmlParser;
import net.davidnorton.securityapp.ui.ListDialogActivity;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by David on 24/12/2014.
 */
public class Handler {
    final static String TAG = "Handler";

    private Context context;
    SharedPreferences pref;

    public Handler(Context _context) {
        context = _context;
        pref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Applies a profile, updates the notification and shows a toast.
     *
     * @param _name The name of the profile
     */
    public void applyProfile(String _name) {
        XmlParser parser = new XmlParser(context);
        try {
            // applies the profile.
            parser.initializeXmlParser(context.openFileInput(_name + "_profile.xml"));
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // saves the active profile into the shared preferences
        pref.edit().putString("active_profile", _name).commit();

        // updates the notification
        if (pref.getBoolean("notification", true)) {
            updateNotification();
        }

        // shows the toast
        Toast toast = Toast.makeText(context, _name + " " + context.getResources().getString(R.string.profileApplied),Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * Updates the notification
     */
    public void updateNotification() {
            Intent resultIntent = new Intent(context, ListDialogActivity.class);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(context,0, resultIntent, 0);

            NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(context);

            nBuilder.setSmallIcon(R.drawable.notification_icon);
            int color = context.getResources().getColor(R.color.notification_background);
            nBuilder.setColor(color);
            nBuilder.setContentText(context.getResources().getString(R.string.notification_content));
            nBuilder.setContentTitle(context.getResources().getString(R.string.notification_title));
            nBuilder.setContentIntent(resultPendingIntent);
            nBuilder.setOngoing(true);
            nBuilder.setWhen(0);
            nBuilder.setPriority(1);

            Notification notification = nBuilder.build();
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(123, notification);
    }
}
