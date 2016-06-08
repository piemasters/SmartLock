package net.davidnorton.securityapp.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.CardView;
import android.text.InputFilter;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import net.davidnorton.securityapp.R;
import net.davidnorton.securityapp.lockscreen.ImageUtils;
import net.davidnorton.securityapp.lockscreen.NFCReader;
import net.davidnorton.securityapp.profile.Handler;
import net.davidnorton.securityapp.services.LockScreenService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Fragment used to set the lock screen and save
 * passwords and NFC tags.
 *
 * @author David Norton
 *
 */
@SuppressWarnings("UnusedAssignment")
public class Lockscreens extends DialogFragment implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener {

    private ImageView ivIcon;
    private TextView tvItemName;

    public static final String IMAGE_RESOURCE_ID = "iconResourceID";
    public static final String ITEM_NAME = "itemName";

    // Content of JSON file
    private JSONObject root;
    private JSONObject settings;
    private JSONArray tags;
    private int blur;

    /*
    JSON file structure:
    root_OBJ:{
        settings_OBJ:{
            "lockscreen":true/false,
            "pin":"4-6 digits",
            "pinLocked":true/false,
            "blur":0-25,
            tags_ARR:[
                {"tagName":"Bracelet", "tagID":"86ja8asdbb2385"},
                {"tagName":"Ring", "tagID":"r2365sd98123sj"} etc.
            ]
        }
    }
    */

    // NFC.
    private NfcAdapter nfcAdapter;
    private TagAdapter adapter;
    private String tagID = null; // ID of tag discovered

    // Interval skip for seek bar.
    private static final int SEEK_BAR_INTERVAL = 5;

    // Used to stop lock screen status message displaying when page opens.
    private Boolean onStart = false;

    // Keyboard Manager.
    private InputMethodManager imm;

    // Layout items
    private EditText pinEdit;
    private SeekBar seekBar;
    private ProgressBar progressBar;
    private TextView lockscreenStatus, backgroundBlurValue, noTags;
    private ListView listView;

    public Lockscreens() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_lockscreens, container, false);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        Context context = getActivity().getApplicationContext();

        // Set icon and title in fragments first card.
        ivIcon = (ImageView) view.findViewById(R.id.passwords_icon);
        tvItemName = (TextView) view.findViewById(R.id.passwords_title);
        tvItemName.setText(getArguments().getString(ITEM_NAME));
        ivIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(), getArguments().getInt(IMAGE_RESOURCE_ID), null));

        // Apply correct colour scheme.
        if (pref.getBoolean("dark_theme", false) ) {

            // Intro Card.
            CardView cardView = (CardView) view.findViewById(R.id.card_1);
            cardView.setCardBackgroundColor(Color.rgb(40, 40, 40));

            // PIN OK Button.
            final Button  setPinButton = (Button) view.findViewById(R.id.setPin);
            setPinButton.setBackgroundColor(ContextCompat.getColor(context, R.color.dark_primary));
            setPinButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    Context context = getActivity().getApplicationContext();
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        setPinButton.setBackgroundColor(ContextCompat.getColor(context, R.color.dark_primary));
                    } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        setPinButton.setBackgroundColor(ContextCompat.getColor(context, R.color.dark_primary_dark));
                    }
                    return false;
                }
            });

            // Update Wallpaper Button.
            final Button updateWallpaperButton = (Button) view.findViewById(R.id.updateWallpaper);
            updateWallpaperButton.setBackgroundColor(ContextCompat.getColor(context, R.color.dark_primary));
            updateWallpaperButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    Context context = getActivity().getApplicationContext();
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        updateWallpaperButton.setBackgroundColor(ContextCompat.getColor(context, R.color.dark_primary));
                    } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        updateWallpaperButton.setBackgroundColor(ContextCompat.getColor(context, R.color.dark_primary_dark));
                    }
                    return false;
                }
            });

            // Save New NFC Tag Button.
            final ImageButton newTagButton = (ImageButton) view.findViewById(R.id.newTag);
            newTagButton.setBackgroundColor(ContextCompat.getColor(context, R.color.button_background));
            newTagButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    Context context = getActivity().getApplicationContext();
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        newTagButton.setBackgroundColor(ContextCompat.getColor(context, R.color.button_background));
                    } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        newTagButton.setBackgroundColor(ContextCompat.getColor(context, R.color.dark_accent));
                    }
                    return false;
                }
            });

            // PIN Title:
            TextView pinTitle = (TextView) view.findViewById(R.id.pinTitle);
            pinTitle.setTextColor(ContextCompat.getColor(context, R.color.dark_primary));

            // Enter PIN text.
            TextView pinEdit = (TextView) view.findViewById(R.id.pinEdit);
            pinEdit.setTextColor(ContextCompat.getColor(context, R.color.dark_primary));

            // NFC Lockscreen Header.
            View lockScreenSeparator = view.findViewById(R.id.lockScreenSeparator);
            lockScreenSeparator.setBackgroundColor(ContextCompat.getColor(context, R.color.dark_primary));

            // NFC Lockscreen Backgroundd Blur Header.
            View backgroundBlurSeparator = view.findViewById(R.id.backgroundBlurSeparator);
            backgroundBlurSeparator.setBackgroundColor(ContextCompat.getColor(context, R.color.dark_primary));

            // NFC Tags Header.
            View NFCTagsSeparator = view.findViewById(R.id.NFCTagsSeparator);
            NFCTagsSeparator.setBackgroundColor(ContextCompat.getColor(context, R.color.dark_primary));
        }

        // Hide keyboard on app launch.
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        // Get NFC service and adapter.
        NfcManager nfcManager = (NfcManager) getActivity().getSystemService(Context.NFC_SERVICE);
        nfcAdapter = nfcManager.getDefaultAdapter();

        // Update lock screen settings.
        readFromJSON();
        writeToJSON();
        readFromJSON();

        // Initialize layout items
        pinEdit = (EditText) view.findViewById(R.id.pinEdit);
        pinEdit.setImeOptions(EditorInfo.IME_ACTION_DONE);
        Button setPin = (Button) view.findViewById(R.id.setPin);
        ImageButton newTag = (ImageButton) view.findViewById(R.id.newTag);
        lockscreenStatus = (TextView) view.findViewById(R.id.lockscreen_status);
        Switch toggle = (Switch) view.findViewById(R.id.toggle);
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        Button updateWallpaper = (Button) view.findViewById(R.id.updateWallpaper);
        listView = (ListView) view.findViewById(R.id.listView);
        backgroundBlurValue = (TextView) view.findViewById(R.id.tagName);
        noTags = (TextView) view.findViewById(R.id.noTags);

        // Initialize TagAdapter for populating the list of NFC tags.
        adapter = new TagAdapter(getActivity(), tags);
        registerForContextMenu(listView);
        listView.setAdapter(adapter);

        // Set click, check and seekBar listeners.
        setPin.setOnClickListener(this);
        newTag.setOnClickListener(this);
        updateWallpaper.setOnClickListener(this);
        toggle.setOnCheckedChangeListener(this);
        seekBar.setOnSeekBarChangeListener(this);

        // Set seekBar position to match blur value.
        try {
            seekBar.setProgress(settings.getInt("blur"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // If no tags, display 'Press + to add Tags' message.
        if (tags.length() == 0) {
            noTags.setVisibility(View.VISIBLE);
        } else {
            noTags.setVisibility(View.INVISIBLE);
        }

        // Refresh the listView height
        updateListViewHeight(listView);

        // If lock screen enabled, initialize switch, text and start service.
        try {
            if (settings.getBoolean("lockscreen")) {
                onStart = true;
                lockscreenStatus.setText(R.string.enabled);
                lockscreenStatus.setTextColor(ContextCompat.getColor(context, R.color.accept));

                toggle.setChecked(true);
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        // If adding NFC tag get tag ID.
        Bundle bundle = this.getArguments();
        boolean tagScanned = pref.getBoolean("TagScanned", false);

        if (bundle.getString("tagID", tagID) != null && tagScanned) {

            // Get ID and prompt user for name.
            tagID = bundle.getString("tagID", tagID);
            NFCIntent();

            // Set TagScanned to false so not called on screen orientation change.
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("TagScanned", false);
            editor.apply();
        }

        return view;
    }

    /**
     * Create rename/delete menu for selecting a stored NFC tag.
     *
     * @param menu Context menu.
     * @param v View.
     * @param menuInfo Menu info.
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);

        menu.add(getResources().getString(R.string.lockscreen_nfc_tag_rename));
        menu.add(getResources().getString(R.string.lockscreen_nfc_tag_delete));
    }

    /**
     * Handle renaming/deleting a stored NFC tag.
     *
     * @param item NFC tag.
     *
     * @return True.
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {

        // Get pressed item information.
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        // If 'Rename Tag' pressed.
        if (item.getTitle().equals(getResources().getString(R.string.lockscreen_nfc_tag_rename))) {

            // Create and configure new EdiText.
            final EditText tagTitle = new EditText(getActivity());
            tagTitle.setSingleLine(true);
            tagTitle.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

            // Limit tag name to 50 characters.
            int maxLength = 50;
            InputFilter[] array = new InputFilter[1];
            array[0] = new InputFilter.LengthFilter(maxLength);
            tagTitle.setFilters(array);

            // Set EditText to current tag name.
            try {
                assert info != null;
                tagTitle.setText(tags.getJSONObject(info.position).getString("tagName"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Add EditText to dialog view.
            final LinearLayout l = new LinearLayout(getActivity());
            l.setOrientation(LinearLayout.VERTICAL);
            l.addView(tagTitle);

            // Show rename dialog.
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.lockscreen_scan_dialog_title_tag_rename)
                    .setView(l)
                    .setPositiveButton(R.string.lockscreen_scan_dialog_button_tag_rename, new DialogInterface.OnClickListener() {

                        //  Change tag name and store.
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            try {
                                JSONObject newTagName = tags.getJSONObject(info.position);
                                newTagName.put("tagName", tagTitle.getText());

                                // Update view.
                                tags.put(info.position, newTagName);
                                adapter.notifyDataSetChanged();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            writeToJSON();

                            Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                                    R.string.lockscreen_nfc_toast_tag_renamed, Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }

            }).show();

            // Display keyboard to enter new name.
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

            return true;
        }

        // If 'Delete Tag' pressed.
        else if (item.getTitle().equals(getResources().getString(R.string.lockscreen_nfc_tag_delete))) {

            // Construct dialog message
            String dialogMessage;
            assert info != null;

            try {
                dialogMessage = getResources().getString(R.string.lockscreen_nfc_tag_dialog_delete) + " '"
                        + tags.getJSONObject(info.position).getString("tagName") + "'?";

            } catch (JSONException e) {
                e.printStackTrace();
                dialogMessage = getResources().getString(R.string.lockscreen_nfc_tag_dialog_delete_nameless);
            }

            // Show delete dialog.
            new AlertDialog.Builder(getActivity())
                    .setMessage(dialogMessage)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            JSONArray newArray = new JSONArray();

                            // Copy contents to new array without the deleted item.
                            for (int i = 0; i < tags.length(); i++) {
                                if (i != info.position) {
                                    try {
                                        newArray.put(tags.get(i));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            tags = newArray;

                            // Write updated list to file.
                            try {
                                settings.put("tags", tags);
                                root.put("settings", settings);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            writeToJSON();

                            // Update view.
                            adapter.adapterData = tags;
                            adapter.notifyDataSetChanged();
                            updateListViewHeight(listView);

                            // If no tags, display 'Press + to add Tags' message.
                            if (tags.length() == 0) {
                                noTags.setVisibility(View.VISIBLE);
                            } else {
                                noTags.setVisibility(View.INVISIBLE);
                            }

                            // Display success message.
                            Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                                    R.string.lockscreen_nfc_toast_tag_deleted, Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();

            return true;
        }
        return super.onContextItemSelected(item);
    }


    /**
     * Handle set pin, refresh wallpaper and add NFC button clicks.
     *
     * @param v Button clicked.
     */
    @Override
    public void onClick(View v) {

        // If 'OK' (set pin) clicked, ask user if sure and store.
        if (v.getId() == R.id.setPin) {

            // If PIN length between 4 and 6, store PIN.
            if (pinEdit.length() >= 4 && pinEdit.length() <= 6) {

                new AlertDialog.Builder(getActivity()).setMessage(R.string.lockscreen_pin_dialog_confirmation)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                            // Store PIN.
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    settings.put("pin", String.valueOf(pinEdit.getText()));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                writeToJSON();

                                // Display success message.
                                Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                                        R.string.lockscreen_pin_toast_set,Toast.LENGTH_SHORT);
                                toast.show();

                                // Hide keyboard after success message.
                                imm.hideSoftInputFromWindow(pinEdit.getWindowToken(), 0);

                            }
                        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
            }

            // Display message that at least 4 digits are required.
            else {
                Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                        R.string.lockscreen_pin_toast_error_short, Toast.LENGTH_LONG);
                toast.show();
            }
        }

        // If 'Update Wallpaper' clicked blur and store wallpaper.
        else if (v.getId() == R.id.updateWallpaper) {

            // Set blur value if Jellybean or later.
            if (Build.VERSION.SDK_INT > 16) {

                try {
                    settings.put("blur", blur);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                writeToJSON();

                // If blur = 0, don't change anything and show update message.
                if (blur == 0) {
                    Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                            R.string.lockscreen_blur_toast_success, Toast.LENGTH_SHORT);
                    toast.show();
                }

                // If blur > 0, convert wallpaper to bitmap, blur and store.
                else {

                    // Check if SmartLock folder exists, if not, create directory.
                    File folder = new File(Environment.getExternalStorageDirectory() + "/SmartLock");
                    boolean folderSuccess = true;

                    if (!folder.exists()) {
                        folderSuccess = folder.mkdir();
                    }

                    // If folder exists get wallpaper.
                    if (folderSuccess) {
                        WallpaperManager wallpaperManager = WallpaperManager.getInstance(getActivity());
                        final Drawable wallpaperDrawable = wallpaperManager.peekFastDrawable();

                        // If wallpaper exists display .
                        if (wallpaperDrawable != null) {

                            // Display progress bar while blurring.
                            progressBar.setVisibility(View.VISIBLE);

                            new Thread(new Runnable() {

                                @Override
                                public void run() {
                                    Bitmap bitmapToBlur = ImageUtils.drawableToBitmap(wallpaperDrawable);
                                    Bitmap blurredWallpaper = null;
                                    boolean stored = false;

                                    // Blur wallpaper.
                                    if (bitmapToBlur != null) {
                                        blurredWallpaper = ImageUtils.fastBlur(getActivity(), bitmapToBlur, blur);
                                    }

                                    // If wallpaper blurred correctly.
                                    if (blurredWallpaper != null) {

                                        stored = ImageUtils.storeImage(blurredWallpaper);
                                        final boolean finalStored = stored;

                                        // Display successfully updated message.
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {

                                                progressBar.setVisibility(View.INVISIBLE);

                                                if (finalStored) {
                                                    Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                                                            R.string.lockscreen_blur_toast_success, Toast.LENGTH_SHORT);
                                                    toast.show();
                                                }
                                            }
                                        });
                                    }

                                    // If wallpaper not blurred correctly.
                                    if (bitmapToBlur == null || blurredWallpaper == null || !stored) {

                                        // Display error message.
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {

                                                progressBar.setVisibility(View.INVISIBLE);

                                                Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                                                        R.string.lockscreen_blur_toast_failed, Toast.LENGTH_SHORT);
                                                toast.show();
                                            }
                                        });
                                    }
                                }
                            }).start();
                        }

                        // Display error message if not updated.
                        else {
                            Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                                    R.string.lockscreen_blur_toast_failed, Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                }
            }

            // If Android version less than 4.2, display cannot blur message.
            else {
                Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                        R.string.lockscreen_blur_toast_old, Toast.LENGTH_SHORT);
                toast.show();
            }
        }

        // If '+' (store new NFC tag) button pressed start NFCReader activity.
        else if (v.getId() == R.id.newTag) {

            // If NFC is on start activity.
            if (nfcAdapter != null && nfcAdapter.isEnabled()) {
                startActivity(new Intent(getActivity(),NFCReader.class));
            }
            // NFC is off, prompt user to enable it and send to NFC settings.
            else {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.lockscreen_scan_dialog_title_nfc_off)
                        .setMessage(R.string.lockscreen_scan_dialog_text_nfc_off)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
                                startActivity(intent);
                            }
                        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
            }
        }
    }

    /**
     * If NFC tag scanned, open save name dialog and save ID.
     */
    private void NFCIntent() {

        Context context = getActivity().getApplicationContext();

        if (!tagID.equals(null)) {

            // Display keyboard to enter new name.
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

            // Set tag name message in dialog.
            final EditText tagTitle = new EditText(getActivity());
            tagTitle.setHint(getResources().getString(R.string.lockscreen_scan_dialog_hint_set_tag_name));
            tagTitle.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
            tagTitle.setSingleLine(true);
            tagTitle.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

            // Limit tag name to 50 characters.
            int maxLength = 50;
            InputFilter[] array = new InputFilter[1];
            array[0] = new InputFilter.LengthFilter(maxLength);
            tagTitle.setFilters(array);

            final LinearLayout l = new LinearLayout(getActivity());
            l.setOrientation(LinearLayout.VERTICAL);
            l.addView(tagTitle);

            tagTitle.requestFocus();

            // Show enter name dialog.
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.lockscreen_scan_dialog_title_set_tag_name)
                    .setView(l)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // Save tag name paired with ID.
                            JSONObject newTag = new JSONObject();

                            try {
                                newTag.put("tagName", tagTitle.getText());
                                newTag.put("tagID", tagID);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            tags.put(newTag);
                            writeToJSON();

                            // Update view.
                            adapter.notifyDataSetChanged();
                            updateListViewHeight(listView);

                            // If no tags, display 'Press + to add Tags' message.
                            if (tags.length() == 0) {
                                noTags.setVisibility(View.VISIBLE);
                            }
                            else {
                                noTags.setVisibility(View.INVISIBLE);
                            }

                            // Display tag added success message.
                            Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                                    R.string.lockscreen_nfc_toast_tag_added, Toast.LENGTH_SHORT);
                            toast.show();

                            // Clear tag details.
                            tagID = "";
                            tagTitle.setText("");
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Clear tag details.
                            tagID = "";
                            tagTitle.setText("");
                            dialog.cancel();
                        }
                    })
                    .show();
        }
    }

    /**
     * If enable/disable lock screen lock switch changed.
     *
     * @param buttonView Switch.
     * @param isChecked State of switch.
     */
    public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {

        Context context = getActivity().getApplicationContext();

        // If lock screen is set to enabled.
        if (isChecked) {
            try {
                // If PIN not saved, display set PIN message.
                if (settings.getString("pin").equals("")) {

                    Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                            R.string.lockscreen_toggle_toast_error_pin_set, Toast.LENGTH_LONG);
                    toast.show();

                    buttonView.setChecked(false);
                }

                // Set lockscreen, start service and store.
                else {
                    if (!onStart) {
                        try {
                            settings.put("lockscreen", true);

                            // Set lock screen status text to enabled.
                            lockscreenStatus.setText(R.string.enabled);
                            lockscreenStatus.setTextColor(ContextCompat.getColor(context, R.color.accept));

                            // Apply active Profile to update lock screen status.
                            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
                            if (!pref.getString("active_profile", getActivity().getApplicationContext().getResources().getString(
                                    R.string.notification_title_no_profile)).equals("None")) {
                                Handler handler = new Handler(getActivity().getApplicationContext());
                                handler.applyProfileHidden(pref.getString("active_profile", getActivity().getApplicationContext().getResources().getString(
                                        R.string.notification_title_no_profile)));
                            }

                            // Display lock screen enabled message.
                            Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                                    R.string.lockscreen_toggle_toast_enabled, Toast.LENGTH_SHORT);
                            toast.show();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        writeToJSON();
                        return;
                    }

                    onStart = false;

                    // Apply active Profile to update lock screen status.
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
                    if (!pref.getString("active_profile", getActivity().getApplicationContext().getResources().getString(
                            R.string.notification_title_no_profile)).equals("None")) {
                        Handler handler = new Handler(getActivity().getApplicationContext());
                        handler.applyProfileHidden(pref.getString("active_profile", getActivity().getApplicationContext().getResources().getString(
                                R.string.notification_title_no_profile)));
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // If unchecked, set lockscreen false, stop service and store.
        else {

            // Set lock screen to false.
            try {
                settings.put("lockscreen", false);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            writeToJSON();

            // Set lock screen status text to disabled.
            lockscreenStatus.setText(R.string.disabled);
            lockscreenStatus.setTextColor(ContextCompat.getColor(context, R.color.warning));

            // Stop lock screen service.
            killService(getActivity());

            // Display lock screen disabled message.
            Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                    R.string.lockscreen_toggle_toast_disabled, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * Handle change to blur seekbar.
     *
     * @param seekBar Blur seekbar.
     * @param progress Value of seekbar.
     * @param fromUser User input.
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        // Calculate progress bar blur value based on change in intervals of 5.
        progress = (Math.round(progress / SEEK_BAR_INTERVAL)) * SEEK_BAR_INTERVAL;

        // Set progress blur value.
        this.seekBar.setProgress(progress);

        // Set blur value to calculated progress bar value.
        blur = progress;

        // Set blur text value to progress bar value.
        backgroundBlurValue.setText(String.valueOf(progress));
    }

    /**
     * Read from JSON file.
     */
    private void readFromJSON() {

        // Read root object, or put if they don't exist.
        try {
            BufferedReader bRead = new BufferedReader(new InputStreamReader(getActivity().openFileInput("settings.json")));
            root = new JSONObject(bRead.readLine());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            root = new JSONObject();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        // Read settings object from root object, or put if they don't exist.
        try {
            settings = root.getJSONObject("settings");
        } catch (JSONException e) {
            try {
                settings = new JSONObject();
                root.put("settings", settings);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }

        // Read individual settings from settings object, or put if they don't exist.
        try {
            Boolean lockscreen = settings.getBoolean("lockscreen");
            String pin = settings.getString("pin");
            Boolean pinLocked = settings.getBoolean("pinLocked");
            blur = settings.getInt("blur");
            tags = settings.getJSONArray("tags");
        } catch (JSONException e) {
            try {
                tags = new JSONArray();

                settings.put("lockscreen", false);
                settings.put("pin", "");
                settings.put("pinLocked", false);
                if (Build.VERSION.SDK_INT > 16) {
                    settings.put("blur", 15);
                } else {
                    settings.put("blur", 0);
                }
                settings.put("tags", tags);

            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * Write to JSON file.
     */
    private void writeToJSON() {

        // Write JSON root object.
        try {
            BufferedWriter bWrite = new BufferedWriter(new OutputStreamWriter
                    (getActivity().openFileOutput("settings.json", Context.MODE_PRIVATE)));
            bWrite.write(root.toString());
            bWrite.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tag Adapter Class used to populate the NFC tags listView.
     */
    private class TagAdapter extends BaseAdapter implements ListAdapter {

        final Activity parentActivity;
        JSONArray adapterData;
        final LayoutInflater inflater;

        public TagAdapter(Activity activity, JSONArray adapterData) {
            this.parentActivity = activity;
            this.adapterData = adapterData;
            this.inflater = (LayoutInflater)getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        /**
         * Get the number of saved NFC tags.
         *
         * @return number of saved NFC tags.
         */
        @Override
        public int getCount() {
            if (this.adapterData != null) {
                return this.adapterData.length();
            } else {
                return 0;
            }
        }

        /**
         * Get a NFC tag.
         *
         * @param position Position of NFC tag.
         *
         * @return NFC tag.
         */
        @Override
        public JSONObject getItem(int position) {
            if (this.adapterData != null) {
                return this.adapterData.optJSONObject(position);
            } else {
                return null;
            }
        }

        /**
         * Get a NFC tag ID.
         *
         * @param position Position of NFC tag.
         *
         * @return NFC tag ID.
         */
        @Override
        public long getItemId(int position) {

            JSONObject jsonObject = getItem(position);

            return jsonObject.optLong("id");
        }

        /**
         * Configure list of NFC tags.
         *
         * @param position NFC tag in list.
         * @param convertView View of NFC tags.
         * @param parent Lockscreens view.
         *
         * @return convertView.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            // Get list of NFC tags.
            if (convertView == null) {
                convertView = this.parentActivity.getLayoutInflater().inflate(R.layout.fragment_lockscreens_list, null);
            }

            // Set NFC tag names.
            TextView text = (TextView) convertView.findViewById(R.id.tagName);
            JSONObject jsonData = getItem(position);

            if (jsonData != null) {
                try {
                    String data = jsonData.getString("tagName");
                    text.setText(data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return convertView;
        }
    }

    /**
     * Stop Lock Screen Service.
     *
     * @param context Context.
     */
    private void killService(Context context) {
        getActivity().stopService(new Intent(context, LockScreenService.class));
    }

    /**
     * Updates the height of the view containing NFC tags.
     *
     * @param listView View containing NFC tags.
     */
    private static boolean updateListViewHeight(ListView listView) {

        // Get listView.
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {

            // Get height of first item.
            View item = listAdapter.getView(0, null, listView);
            item.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            item.measure(0, 0);
            int itemHeight = item.getMeasuredHeight();
            itemHeight = 180;// Fix as height is calculated incorrectly.

            // Multiply to get height of all items.
            int numberOfItems = listAdapter.getCount();
            int totalItemsHeight = itemHeight * numberOfItems;

            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() * (numberOfItems - 1);

            // Set list height.
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight;
            listView.setLayoutParams(params);
            listView.requestLayout();

            return true;

        } else {
            return false;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
