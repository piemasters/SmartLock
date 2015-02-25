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
import net.davidnorton.securityapp.services.TriggerService;
import net.davidnorton.securityapp.trigger.TriggerList;
import net.davidnorton.securityapp.trigger.TriggerEditActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Triggers extends Fragment implements AdapterView.OnItemLongClickListener {

    ImageView ivIcon;
    TextView tvItemName;
    List<String> triggerList = new ArrayList<>();

    public static final String IMAGE_RESOURCE_ID = "iconResourceID";
    public static final String ITEM_NAME = "itemName";


    /**
     * Creates the Triggers fragment.
     *
     * @param inflater Instantiates a layout XML file into its corresponding View objects.
     * @param container Container used to the base for layouts and views containers.
     * @param savedInstanceState Saves current state of application to be referred back to.
     * @return The view.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_triggers, container, false);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this.getActivity());

        // Set icon and title in fragments first card
        ivIcon = (ImageView) view.findViewById(R.id.triggers_icon);
        tvItemName = (TextView) view.findViewById(R.id.triggers_title);
        tvItemName.setText(getArguments().getString(ITEM_NAME));
        ivIcon.setImageDrawable(view.getResources().getDrawable(getArguments().getInt(IMAGE_RESOURCE_ID)));

        // Add trigger button
        final Button button1 = (Button)view.findViewById(R.id.new_trigger_button);
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
     * Starts the trigger service on creation.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Intent intent = new Intent(getActivity(), TriggerService.class);
        getActivity().startService(intent);
    }

    /**
     * Run when fragment becomes visible. Used to update the list of Triggers.
     */
    @Override
    public void onStart() {
        refreshListView();
        super.onStart();
    }

    /**
     * Refreshes the trigger list and list adapter.
     */
    private void refreshListView() {

        // Clear the current list of items
        triggerList.clear();


        ListView v = (ListView) getActivity().findViewById(R.id.ListViewTriggers);
        String[] fileList = getActivity().getFilesDir().list();

        // Add triggers to list.
        for (String file : fileList) {
            if (file.contains("_tri_dis") || file.contains("_trigger")) {
                triggerList.add(file);
            }
        }

        // Sort triggers alphabetically.
        Collections.sort(triggerList, new Comparator<String>() {

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
        TriggerList listAdapter = new TriggerList(getActivity(), 0, triggerList);
        v.setAdapter(listAdapter);
        v.setOnItemLongClickListener(this);
    }

    /**
     * Create new trigger when button selected.
     */
    public void onOptionsItemSelected() {

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor prefEditor = preferences.edit();

            Set<String> weekdays = new HashSet<>();

            // Loads the default preference values for a new Trigger.
            prefEditor.putString("name_trigger", getString(R.string.trigger_default_name_new));
            prefEditor.putString("profile", getString(R.string.trigger_pref_default_profile));
            prefEditor.putString("priority", "0");
            prefEditor.putString("start_time", getString(R.string.ignored));
            prefEditor.putString("end_time", getString(R.string.ignored));
            prefEditor.putString("battery_state", "ignored");
            prefEditor.putInt("battery_start_level", -1);
            prefEditor.putInt("battery_end_level", -1);
            prefEditor.putString("headphone", "ignored");
            prefEditor.putFloat("geofence_lat", -1F);
            prefEditor.putFloat("geofence_lng", -1F);
            prefEditor.putInt("geofence_radius", 0);
            prefEditor.putStringSet("weekdays", weekdays);
            prefEditor.apply();

            Intent i = new Intent(getActivity(), TriggerEditActivity.class);
            startActivity(i);
    }

    /**
     * Display a dialog message when a Trigger item is long pressed.
     *
     * @param av AdapterView
     * @param v View
     * @param position Trigger selected.
     * @param arg3 arg3
     * @return true
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> av, View v, int position, long arg3) {

        // used to notify the user of the longpress.
        Vibrator vib = (Vibrator) getActivity().getSystemService(getActivity().VIBRATOR_SERVICE);
        vib.vibrate(100);

        // Create and display a dialog with a enable/disable and delete option.
        String filename = triggerList.get(position);
        String[] options;

        if (filename.contains("_trigger")) {
            options = new String[] { getResources().getString(R.string.disabled), getResources().getString(R.string.delete) };
        } else {
            options = new String[] { getResources().getString(R.string.enabled), getResources().getString(R.string.delete) };
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(options, new LongPressMenuListener(av, position));
        builder.show();
        return false;
    }

    /**
     * Listens for a long press on a Trigger item.
     */
    private class LongPressMenuListener implements DialogInterface.OnClickListener {

        AdapterView<?> av;
        int position;

        LongPressMenuListener(AdapterView<?> a, int pos) {
            av = a;
            position = pos;
        }

        /**
         * Deletes and enables/disables a trigger on option selection.
         *
         * @param dialog Dialog Interface.
         * @param which Option selected.
         */
        @Override
        public void onClick(DialogInterface dialog, int which) {

            Boolean enableTrigger = true;
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf(getActivity().getFilesDir()) + "/" + av.getItemAtPosition(position));

            // Check if trigger is enabled or disabled.
            if (sb.toString().contains("_trigger")) {
                enableTrigger = false;
            }

            // Get trigger name.
            sb.delete(sb.length() - 12, sb.length());

            switch (which) {

                // Enable and Disable the trigger.
                case 0: {
                    File file;
                    File newFile;

                    if (enableTrigger) {
                        file = new File(sb.toString() + "_tri_dis.xml");
                        newFile = new File(sb.toString() + "_trigger.xml");
                        file.renameTo(newFile);
                    } else {
                        file = new File(sb.toString() + "_trigger.xml");
                        newFile = new File(sb.toString() + "_tri_dis.xml");
                        file.renameTo(newFile);
                    }

                    // Refresh the list of Triggers.
                    refreshListView();
                    // Refresh this list for the service.
                    Intent intent = new Intent();
                    intent.setAction("net.davidnorton.securityapp.trigger.refresh");
                    getActivity().sendBroadcast(intent);
                }

                break;

                // Delete the Trigger.
                case 1: {
                    File file = new File(sb.toString() + "_trigger.xml");
                    file.delete();
                    file = new File(sb.toString() + "_tri_dis.xml");
                    file.delete();

                    // Refresh the list of Triggers.
                    refreshListView();
                    // Refresh this list for the service.
                    Intent intent = new Intent();
                    intent.setAction("net.davidnorton.securityapp.trigger.refresh");
                    getActivity().sendBroadcast(intent);
                }
                break;
            }
        }
    }
}
