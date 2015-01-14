package net.davidnorton.securityapp.ui;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;

import net.davidnorton.securityapp.R;

public class MainActivity extends Activity {

    // Drawer
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle, mTitle;
    DrawerAdapter adapter;
    List<DrawerItem> dataList;
    ColorFilter filter;

    // Preference Settings
    final static String TAG = "MainActivity";
    private boolean darkTheme = false, permNotification = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        // Sets the Theme
        changeTheme(pref);

        // Create activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Drawer
        initializeDrawer(savedInstanceState);

        // Starts the permanent notification if activated
        toggleNotification(pref);

        // Update Preferences
        updatePreferences(pref);
    }

    private void updatePreferences(SharedPreferences pref) {

        SharedPreferences.OnSharedPreferenceChangeListener prefListener;
        prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {

            public void onSharedPreferenceChanged(SharedPreferences pref, String key) {

                if (pref.getBoolean("dark_theme", false) != darkTheme) {
                    Log.i(TAG, "New theme detected. Restart Activity");
                    recreate();
                }

                if (pref.getBoolean("notification", false) != permNotification) {
                    Log.i(TAG, "Notification display change detected. Restart Activity");
                    recreate();
                }
            }
        };
        pref.registerOnSharedPreferenceChangeListener(prefListener);
    }

    private void toggleNotification(SharedPreferences pref) {

        if (pref.getBoolean("notification", true)) {
            permNotification = true;

            Intent resultIntent = new Intent(this, ListDialogActivity.class);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, 0);

            NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this);

            nBuilder.setSmallIcon(R.drawable.notification_icon);
            int color = getResources().getColor(R.color.notification_background);
            nBuilder.setColor(color);
            nBuilder.setContentText(getResources().getString(R.string.notification_content));
            nBuilder.setContentTitle(getResources().getString(R.string.notification_title));
            nBuilder.setContentIntent(resultPendingIntent);
            nBuilder.setOngoing(true);
            nBuilder.setWhen(0);
            nBuilder.setPriority(1);

            Notification notification = nBuilder.build();
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(123, notification);

        } else {
            // Deactivate notification
            permNotification = false;
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(123);
        }
    }

    private void changeTheme(SharedPreferences pref) {

        if (pref.getBoolean("dark_theme", false)) {
            setTheme(R.style.AppThemeDark);
            // Set primary colour for icons.
            filter = new LightingColorFilter( getResources().getColor(R.color.dark_primary_dark),getResources().getColor(R.color.dark_primary_dark));
            darkTheme = true;
        } else {
            setTheme(R.style.AppThemeLight);
            // Set primary colour for icons.
            filter = new LightingColorFilter( getResources().getColor(R.color.primary_dark),getResources().getColor(R.color.primary_dark));
            darkTheme = false;
        }
    }

    @Override
    protected void onResume() {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        // Update theme (fixes bug when app minimised and reopened)
        if (pref.getBoolean("dark_theme", false) != darkTheme) {
            Log.i(TAG, "New theme detected. Restart Activity");
            recreate();
        }

        // Update notification status (fixes bug when app minimised and reopened)
        if (pref.getBoolean("notification", false) != permNotification) {
            Log.i(TAG, "Notification display change detected. Restart Activity");
            recreate();
        }

        super.onResume();
    }

    private void initializeDrawer(Bundle savedInstanceState) {

        String[] mMenuTitles, mMenuItems;
        dataList = new ArrayList<>();
        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
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

    private void addDrawerItems(String[] mMenuTitles, String[] mMenuItems) {
        dataList.add(new DrawerItem(mMenuTitles[0])); // adding a header to the list
        dataList.add(new DrawerItem(mMenuItems[0], R.drawable.ic_action_person));
        dataList.add(new DrawerItem(mMenuItems[1], R.drawable.ic_action_storage));
        dataList.add(new DrawerItem(mMenuItems[2], R.drawable.ic_action_event));

        dataList.add(new DrawerItem(mMenuTitles[1]));// adding a header to the list
        dataList.add(new DrawerItem(mMenuItems[3], R.drawable.ic_action_secure));
        dataList.add(new DrawerItem(mMenuItems[4], R.drawable.ic_action_add_person));
        dataList.add(new DrawerItem(mMenuItems[5], R.drawable.ic_action_location_found));
        dataList.add(new DrawerItem(mMenuItems[6], R.drawable.ic_action_network_wifi));
        dataList.add(new DrawerItem(mMenuItems[7], R.drawable.ic_action_labels));

        dataList.add(new DrawerItem(mMenuTitles[2])); // adding a header to the list
        dataList.add(new DrawerItem(mMenuItems[8], R.drawable.ic_action_about));
        dataList.add(new DrawerItem(mMenuItems[9], R.drawable.ic_action_settings));
        dataList.add(new DrawerItem(mMenuItems[10], R.drawable.ic_action_help));
    }

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
        myIcon = getResources().getDrawable(R.drawable.ic_action_location_found);
        myIcon.setColorFilter(filter);
        myIcon = getResources().getDrawable( R.drawable.ic_action_network_wifi );
        myIcon.setColorFilter(filter);
        myIcon = getResources().getDrawable( R.drawable.ic_action_labels );
        myIcon.setColorFilter(filter);
        myIcon = getResources().getDrawable( R.drawable.ic_action_about );
        myIcon.setColorFilter(filter);
        myIcon = getResources().getDrawable( R.drawable.ic_action_settings );
        myIcon.setColorFilter(filter);
        myIcon = getResources().getDrawable( R.drawable.ic_action_help );
        myIcon.setColorFilter(filter);
    }

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
                fragment = new Locations();
                args.putString(Locations.ITEM_NAME, dataList.get(position).getItemName());
                args.putInt(Locations.IMAGE_RESOURCE_ID, dataList.get(position).getImgResID());
                break;
            case 8:
                fragment = new Networks();
                args.putString(Networks.ITEM_NAME, dataList.get(position).getItemName());
                args.putInt(Networks.IMAGE_RESOURCE_ID, dataList.get(position).getImgResID());
                break;
            case 9:
                fragment = new NFC();
                args.putString(NFC.ITEM_NAME, dataList.get(position).getItemName());
                args.putInt(NFC.IMAGE_RESOURCE_ID, dataList.get(position).getImgResID());
                break;
            case 11:
                fragment = new About();
                args.putString(About.ITEM_NAME, dataList.get(position).getItemName());
                args.putInt(About.IMAGE_RESOURCE_ID, dataList.get(position).getImgResID());
                break;
            case 12:
                fragment = new AdvancedSettings();
                args.putString(AdvancedSettings.ITEM_NAME, dataList.get(position).getItemName());
                args.putInt(AdvancedSettings.IMAGE_RESOURCE_ID, dataList.get(position).getImgResID());
                break;
            case 13:
                fragment = new Help();
                args.putString(Help.ITEM_NAME, dataList.get(position).getItemName());
                args.putInt(Help.IMAGE_RESOURCE_ID, dataList.get(position).getImgResID());
                break;
            default:
                break;
        }

        fragment.setArguments(args);
        FragmentManager frgManager = getFragmentManager();
        frgManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        mDrawerList.setItemChecked(position, true);
        setTitle(dataList.get(position).getItemName());
        mDrawerLayout.closeDrawer(mDrawerList);

    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        //boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        return super.onPrepareOptionsMenu(menu);
    }

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
            args.putString(AdvancedSettings.ITEM_NAME, dataList.get(12).getItemName());
            args.putInt(AdvancedSettings.IMAGE_RESOURCE_ID, dataList.get(12).getImgResID());
            fragment.setArguments(args);
            FragmentManager frgManager = getFragmentManager();
            frgManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
            mDrawerList.setItemChecked(12, true);
            setTitle(dataList.get(12).getItemName());
            mDrawerLayout.closeDrawer(mDrawerList);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (dataList.get(position).getTitle() == null) {
                selectItem(position);
            }
        }
    }
}