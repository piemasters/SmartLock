package net.davidnorton.securityapp.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import net.davidnorton.securityapp.R;
import net.davidnorton.securityapp.profile.Handler;

public class MainActivity extends Activity {

    // Drawer
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle, mTitle;
    private DrawerAdapter adapter;
    private List<DrawerItem> dataList;
    private ColorFilter filter;

    // Preference Settings
    private final static String TAG = "MainActivity";
    private boolean darkTheme = false, permNotification = false;

    /**
     * Creates the main application.
     *
     * @param savedInstanceState Saves current state of application to be referred back to.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        // Sets the Theme
        changeTheme(pref);

        // Create activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Drawer
        initializeDrawer(savedInstanceState, pref);

        // Starts the permanent notification if activated
        toggleNotification(pref);

        // Update Preferences
        updatePreferences(pref);
    }

    /**
     * Recreates the activity when certain preferences are changed.
     *
     * @param pref Container for stored shared preferences.
     */
    private void updatePreferences(SharedPreferences pref) {

        SharedPreferences.OnSharedPreferenceChangeListener prefListener;
        prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {

            public void onSharedPreferenceChanged(SharedPreferences pref, String key) {

                // Recreate if dark theme preference changed
                if (pref.getBoolean("dark_theme", false) != darkTheme) {
                    Log.i(TAG, "New theme detected. Restart Activity");
                    recreate();
                }

                // Recreate if permanent notification preference changed
                if (pref.getBoolean("notification", false) != permNotification) {
                    Log.i(TAG, "Notification display change detected. Restart Activity");
                    toggleNotification(pref);
                }
            }
        };
        pref.registerOnSharedPreferenceChangeListener(prefListener);
    }

    /**
     * Creates permanent notification if selected.
     *
     * @param pref Container for stored shared preferences.
     */
    private void toggleNotification(SharedPreferences pref) {

        // If permanent notification is selected
        if (pref.getBoolean("notification", true)) {
            permNotification = true;
            Handler handler = new Handler(this);
            handler.updateNotification();
        } else {
            // Deactivate notification
            permNotification = false;
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(123);
        }
    }

    /**
     * Toggle app colour theme.
     *
     * @param pref Container for stored shared preferences.
     */
    private void changeTheme(SharedPreferences pref) {

        // Set dark theme if selected
        if (pref.getBoolean("dark_theme", false)) {
            darkTheme = true;
            setTheme(R.style.AppThemeDark);
            // Set primary colour for icons.
            filter = new LightingColorFilter( getResources().getColor(R.color.dark_primary_dark),getResources().getColor(R.color.dark_primary_dark));

        // Set light theme if selected
        } else {
            darkTheme = false;
            setTheme(R.style.AppThemeLight);
            // Set primary colour for icons.
            filter = new LightingColorFilter( getResources().getColor(R.color.primary_dark),getResources().getColor(R.color.primary_dark));
        }
    }

    /**
     * Creates the side drawer.
     *
     * @param savedInstanceState Saves current state of application to be referred back to.
     */
    private void initializeDrawer(Bundle savedInstanceState, SharedPreferences pref) {

        String[] mMenuTitles, mMenuItems;
        dataList = new ArrayList<>();
        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set drawer colour
        if (pref.getBoolean("dark_theme", false)) {
           mDrawerList.setBackgroundColor(Color.rgb(40, 40, 40));
        }

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // Get menu and item values from array.
        mMenuTitles = getResources().getStringArray(R.array.menu_title_array);
        mMenuItems = getResources().getStringArray(R.array.menu_items_array);

        // Make all icon match the primary colour
        setIconColour();

        // Add pages with matching icons to drawer.
        addDrawerItems(mMenuTitles, mMenuItems);

        adapter = new DrawerAdapter(this, R.layout.drawer_item, dataList);
        mDrawerList.setAdapter(adapter);

        // When opening app, load first item (second item if first is a title)
        if (savedInstanceState == null) {
            if  (dataList.get(0).getTitle() != null) {
                selectItem(1);}
            else {
                selectItem(0);}
        }
    }

    /**
     * Add items to drawer.
     *
     * @param mMenuTitles Drawer titles.
     * @param mMenuItems Drawer items.
     */
    private void addDrawerItems(String[] mMenuTitles, String[] mMenuItems) {
        dataList.add(new DrawerItem(mMenuTitles[0])); // adding a header to the list
        dataList.add(new DrawerItem(mMenuItems[0], R.drawable.ic_action_person));
        dataList.add(new DrawerItem(mMenuItems[1], R.drawable.ic_action_storage));
        dataList.add(new DrawerItem(mMenuItems[2], R.drawable.ic_action_event));

        dataList.add(new DrawerItem(mMenuTitles[1]));// adding a header to the list
        dataList.add(new DrawerItem(mMenuItems[3], R.drawable.ic_action_secure));
        dataList.add(new DrawerItem(mMenuItems[4], R.drawable.ic_action_add_person));
        dataList.add(new DrawerItem(mMenuItems[5], R.drawable.ic_action_labels));

        dataList.add(new DrawerItem(mMenuTitles[2]));// adding a header to the list
        dataList.add(new DrawerItem(mMenuItems[6], R.drawable.ic_action_location_found));
        dataList.add(new DrawerItem(mMenuItems[7], R.drawable.ic_action_network_wifi));
        dataList.add(new DrawerItem(mMenuItems[8], R.drawable.ic_action_bluetooth));

        dataList.add(new DrawerItem(mMenuTitles[3])); // adding a header to the list
        dataList.add(new DrawerItem(mMenuItems[9], R.drawable.ic_action_about));
        dataList.add(new DrawerItem(mMenuItems[10], R.drawable.ic_action_settings));
        dataList.add(new DrawerItem(mMenuItems[11], R.drawable.ic_action_help));
    }

    /**
     * Applies a colour filter to all icons used within the app.
     */
    private void setIconColour() {

        // Set icons to primary app colour.
        Drawable myIcon = getResources().getDrawable( R.drawable.ic_action_person );
        myIcon.setColorFilter(filter);
        myIcon = getResources().getDrawable( R.drawable.ic_action_storage );
        myIcon.setColorFilter(filter);
        myIcon = getResources().getDrawable( R.drawable.ic_action_event );
        myIcon.setColorFilter(filter);
        myIcon = getResources().getDrawable( R.drawable.ic_action_secure );
        myIcon.setColorFilter(filter);
        myIcon = getResources().getDrawable( R.drawable.ic_action_add_person );
        myIcon.setColorFilter(filter);
        myIcon = getResources().getDrawable( R.drawable.ic_action_labels );
        myIcon.setColorFilter(filter);
        myIcon = getResources().getDrawable(R.drawable.ic_action_location_found);
        myIcon.setColorFilter(filter);
        myIcon = getResources().getDrawable( R.drawable.ic_action_network_wifi );
        myIcon.setColorFilter(filter);
        myIcon = getResources().getDrawable( R.drawable.ic_action_bluetooth );
        myIcon.setColorFilter(filter);
        myIcon = getResources().getDrawable( R.drawable.ic_action_about );
        myIcon.setColorFilter(filter);
        myIcon = getResources().getDrawable( R.drawable.ic_action_settings );
        myIcon.setColorFilter(filter);
        myIcon = getResources().getDrawable( R.drawable.ic_action_help );
        myIcon.setColorFilter(filter);
    }

    /**
     * Opens the corresponding fragment to the drawer item selected.
     *
     * @param position Position of the selected drawer item.
     */
    public void selectItem(int position) {

        Fragment fragment = null;
        Bundle args = new Bundle();
        switch (position) {

            case 1:
                fragment = new Profiles();
                args.putString(Profiles.ITEM_NAME, dataList.get(position).getItemName());
                args.putInt(Profiles.IMAGE_RESOURCE_ID, dataList.get(position).getImgResID());
                break;
            case 2:
                fragment = new Triggers();
                args.putString(Triggers.ITEM_NAME, dataList.get(position).getItemName());
                args.putInt(Triggers.IMAGE_RESOURCE_ID, dataList.get(position).getImgResID());
                break;
            case 3:
                fragment = new ActivityLog();
                args.putString(ActivityLog.ITEM_NAME, dataList.get(position).getItemName());
                args.putInt(ActivityLog.IMAGE_RESOURCE_ID, dataList.get(position).getImgResID());
                break;
            case 5:
                fragment = new Passwords();
                args.putString(Passwords.ITEM_NAME, dataList.get(position).getItemName());
                args.putInt(Passwords.IMAGE_RESOURCE_ID, dataList.get(position).getImgResID());
                break;
            case 6:
                fragment = new Biometrics();
                args.putString(Biometrics.ITEM_NAME, dataList.get(position).getItemName());
                args.putInt(Biometrics.IMAGE_RESOURCE_ID, dataList.get(position).getImgResID());
                break;
            case 7:
                fragment = new NFC();
                args.putString(NFC.ITEM_NAME, dataList.get(position).getItemName());
                args.putInt(NFC.IMAGE_RESOURCE_ID, dataList.get(position).getImgResID());
                break;
            case 9:
                fragment = new Locations();
                args.putString(Locations.ITEM_NAME, dataList.get(position).getItemName());
                args.putInt(Locations.IMAGE_RESOURCE_ID, dataList.get(position).getImgResID());
                break;
            case 10:
                fragment = new Networks();
                args.putString(Networks.ITEM_NAME, dataList.get(position).getItemName());
                args.putInt(Networks.IMAGE_RESOURCE_ID, dataList.get(position).getImgResID());
                break;
            case 11:
                fragment = new Devices();
                args.putString(Devices.ITEM_NAME, dataList.get(position).getItemName());
                args.putInt(Devices.IMAGE_RESOURCE_ID, dataList.get(position).getImgResID());
                break;
            case 13:
                fragment = new About();
                args.putString(About.ITEM_NAME, dataList.get(position).getItemName());
                args.putInt(About.IMAGE_RESOURCE_ID, dataList.get(position).getImgResID());
                break;
            case 14:
                fragment = new AdvancedSettings();
                args.putString(AdvancedSettings.ITEM_NAME, dataList.get(position).getItemName());
                args.putInt(AdvancedSettings.IMAGE_RESOURCE_ID, dataList.get(position).getImgResID());
                break;
            case 15:
                fragment = new Help();
                args.putString(Help.ITEM_NAME, dataList.get(position).getItemName());
                args.putInt(Help.IMAGE_RESOURCE_ID, dataList.get(position).getImgResID());
                break;
            default:
                break;
        }

        // Open selected fragment
        fragment.setArguments(args);
        FragmentManager frgManager = getFragmentManager();
        frgManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        // Update drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(dataList.get(position).getItemName());
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    /**
     * Set title of the activity.
     *
     * @param title Title of activity.
     */
    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * Sync the toggle state after onRestoreInstanceState has occurred.
     *
     * @param savedInstanceState Saves current state of application to be referred back to.
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    /**
     * Create the settings button.
     *
     * @param menu Menu bar.
     * @return Open the settings button menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * If the nav drawer is open, hide action items related to the content view.
     *
     * @param menu Menu bar.
     * @return Drawer menu.
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Open advanced settings fragment when the settings button is selected.
     *
     * @param item Menu item (Settings).
     * @return Open advanced settings menu.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // The action bar home/up action should open or close the drawer.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle action bar item clicks here.
        int id = item.getItemId();

        // Settings Button - open Advanced Settings
        if (id == R.id.action_settings) {
            Fragment fragment = new AdvancedSettings();
            Bundle args = new Bundle();
            args.putString(AdvancedSettings.ITEM_NAME, dataList.get(13).getItemName());
            args.putInt(AdvancedSettings.IMAGE_RESOURCE_ID, dataList.get(13).getImgResID());
            fragment.setArguments(args);
            FragmentManager frgManager = getFragmentManager();
            frgManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
            mDrawerList.setItemChecked(13, true);
            setTitle(dataList.get(13).getItemName());
            mDrawerLayout.closeDrawer(mDrawerList);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Pass any configuration change to the drawer toggles.
     *
     * @param newConfig Configuration.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Handle the selection of a drawer item.
     */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (dataList.get(position).getTitle() == null) {
                selectItem(position);
            }
        }
    }
}