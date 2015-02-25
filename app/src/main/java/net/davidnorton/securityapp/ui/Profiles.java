package net.davidnorton.securityapp.ui;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.davidnorton.securityapp.R;
import net.davidnorton.securityapp.profile.ProfileList;
import net.davidnorton.securityapp.profile.ProfileEditActivity;
import net.davidnorton.securityapp.profile.Handler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Fragment used to contain the list of current user Profiles.
 *
 * @author David Norton
 */
public class Profiles extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    ImageView ivIcon;
    TextView tvItemName;
    List<String> profileList = new ArrayList<>();

    public static final String IMAGE_RESOURCE_ID = "iconResourceID";
    public static final String ITEM_NAME = "itemName";

    /**
     * Creates the Profiles fragment.
     *
     * @param inflater Instantiates a layout XML file into its corresponding View objects.
     * @param container Container used to the base for layouts and views containers.
     * @param savedInstanceState Saves current state of application to be referred back to.
     * @return The view.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profiles, container, false);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this.getActivity());

        // Set icon and title in fragments first card
        ivIcon = (ImageView) view.findViewById(R.id.profiles_icon);
        tvItemName = (TextView) view.findViewById(R.id.profiles_title);
        tvItemName.setText(getArguments().getString(ITEM_NAME));
        ivIcon.setImageDrawable(view.getResources().getDrawable(getArguments().getInt(IMAGE_RESOURCE_ID)));

        // Add profile button
        final Button button1 = (Button)view.findViewById(R.id.new_profile_button);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onOptionsItemSelected();
            }
        });

        // Apply correct colour scheme.
        if (pref.getBoolean("dark_theme", false)) {
            View cardLayout = view.findViewById(R.id.card_1);
            cardLayout.setBackgroundColor(Color.rgb(40, 40, 40));
        }

        return view;
    }

    /**
     * Creates the profiles selectable list.
     *
     * @param savedInstanceState Saves current state of application to be referred back to.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        super.onCreate(savedInstanceState);

        // Set up default profiles if app is run for first time
        boolean firstRun = pref.getBoolean("FIRST_RUN", false);
        if (!firstRun) {

            // Create profiles
            Handler handler = new Handler(getActivity());
            handler.createDefaultProfiles();

            // Set preference so not run again
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("FIRST_RUN", true);
            editor.apply();
        }
    }

    /**
     * Run when fragment becomes visible. Used to update the list of Profiles.
     */
    @Override
    public void onStart() {
        super.onStart();
        refreshListView();
    }

    /**
     * Refreshes the list of Profiles after any modifications.
     */
    private void refreshListView() {

        // Clear the current list of items
        profileList.clear();

        // Update the list with all stored profiles
        String[] fileList = getActivity().getFilesDir().list();
        StringBuilder sb = new StringBuilder();

        for (String file : fileList) {
            if (file.contains("_profile")) {
                sb.append(file);
                // Remove '_profile.xml', leaving the profile name
                sb.delete(sb.length() - 12, sb.length());
                profileList.add(sb.toString());
                // Clear buffer
                sb.delete(0, sb.length());
            }
        }

        // Order Profile list alphabetically
        Collections.sort(profileList, new Comparator<String>() {

            @Override
            public int compare(String lhs, String rhs) {
                if (lhs.toLowerCase().compareTo(rhs.toLowerCase()) > 0)
                    return 1;
                if (lhs.toLowerCase().compareTo(rhs.toLowerCase()) < 0)
                    return -1;
                return 0;
            }
        });

        // Recreate the list, setting click listeners.
        ListView profilesView = (ListView) getActivity().findViewById(R.id.ListViewProfiles);
        ProfileList listAdapter = new ProfileList(getActivity(), 0, profileList);
        profilesView.setAdapter(listAdapter);
        profilesView.setOnItemClickListener(this);
        profilesView.setOnItemLongClickListener(this);
    }

    /**
     * Create new profile when button selected.
     */
    public void onOptionsItemSelected() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor prefEditor = preferences.edit();

        // Loads the default values into the shared preferences
        //TODO Add correct profile preferences
        prefEditor.putString("name", getResources().getString(R.string.profile_default_name_new));
        prefEditor.putString("lockscreen", "unchanged");
        prefEditor.putString("wifi", "unchanged");
        prefEditor.putString("mobile_data", "unchanged");
        prefEditor.putString("bluetooth", "unchanged");
        prefEditor.putString("display_auto_mode", "unchanged");
        prefEditor.putString("display_time_out", "-1");
        prefEditor.putString("ringer_mode", "unchanged");

        prefEditor.apply();

        Intent i = new Intent(getActivity(), ProfileEditActivity.class);
        startActivity(i);
    }

    /**
     * When a Profile item is selected apply that Profile.
     *
     * @param av AdapterView
     * @param v View
     * @param position Profile selected.
     * @param arg3 arg3
     */
    @Override
    public void onItemClick(AdapterView<?> av, View v, int position, long arg3) {
        Handler handler = new Handler(getActivity());
        handler.applyProfile((String) av.getItemAtPosition(position));
    }

    /**
     * Display a dialog message when a Profile item is long pressed.
     *
     * @param av AdapterView
     * @param v View
     * @param position Profile selected.
     * @param arg3 arg3
     * @return true
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> av, View v, int position, long arg3) {

        // Vibrate to notify user of long press.
        Vibrator vib = (Vibrator) getActivity().getSystemService(getActivity().VIBRATOR_SERVICE);
        vib.vibrate(100);

        // Create and display a dialog with a single delete option.
        String[] options = new String[] {getResources().getString(R.string.profile_delete)};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(options, new LongPressMenuListener(av, position));
        builder.show();
        return true;
    }

    /**
     * Listens for a long press on a Profile item.
     */
    private class LongPressMenuListener implements DialogInterface.OnClickListener {

        AdapterView<?> av;
        int position;

        LongPressMenuListener(AdapterView<?> a, int pos) {
            av = a;
            position = pos;
        }

        /**
         * Deletes a profile when the delete option is selected.
         *
         * @param dialog Dialog Interface.
         * @param pos Position.
         */
        @Override
        public void onClick(DialogInterface dialog, int pos) {

            File file = new File(String.valueOf(getActivity().getFilesDir())
                    + "/" + av.getItemAtPosition(position) + "_profile.xml");
            file.delete();

            refreshListView();
        }
    }
}