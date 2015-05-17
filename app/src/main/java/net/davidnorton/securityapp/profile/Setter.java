package net.davidnorton.securityapp.profile;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;

import net.davidnorton.securityapp.services.LockScreenService;

/**
 * Applies the preference settings to the device.
 *
 * @author David Norton
 */
public class Setter {

    /**
     * Sets WiFi to the given state.
     *
     * @param context Context.
     * @param enable User preference state.
     */
    public void setLockscreen(Context context, boolean enable) {

        if (enable) {
            // enable if currently disabled
            context.startService(new Intent(context, LockScreenService.class));
            Log.i("Setter", "Lockscreen enabled.");
        } else if (!enable) {
            // disable if currently enabled
            context.stopService(new Intent(context, LockScreenService.class));
            Log.i("Setter", "Lockscreen disabled.");
        } else {
            Log.i("Setter", "Lockscreen not changed.");
        }

    }

    /**
     * Sets WiFi to the given state.
     *
     * @param context Context.
     * @param enable User preference state.
     */
    public void setWifi(Context context, boolean enable) {

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (enable && !wifiManager.isWifiEnabled()) {
            // enable if currently disabled
            wifiManager.setWifiEnabled(true);
            Log.i("Setter", "Wifi: " + wifiManager.getWifiState() + " (on)");
        } else if (!enable && wifiManager.isWifiEnabled()) {
            // disable if currently enabled
            wifiManager.setWifiEnabled(false);
            Log.i("Setter", "Wifi: " + wifiManager.getWifiState() + " (off)");
        } else {
            Log.i("Setter", "Wifi not changed.");
        }
    }

    /**
     * Sets mobile data to the given state.
     *
     * @param context Context.
     * @param enable User preference state.
     * @throws ClassNotFoundException
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    public void setMobileData(Context context, boolean enable) throws ClassNotFoundException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        //TODO: This solution no longer works as of Android 5.0
        // http://tinyurl.com/nsrs39x http://tinyurl.com/o6d79sv http://tinyurl.com/p2rv3m8 http://tinyurl.com/p5vgpnp

        final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final Class conmanClass = Class.forName(conman.getClass().getName());
        final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");

        iConnectivityManagerField.setAccessible(true);

        final Object iConnectivityManager = iConnectivityManagerField .get(conman);
        final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
        final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);

        setMobileDataEnabledMethod.setAccessible(true);
        setMobileDataEnabledMethod.invoke(iConnectivityManager, enable);
    }

    /**
     * Sets bluetooth adapter to the given state.
     *
     * @param context Context.
     * @param enable User preference state.
     */
    public void setBluetooth(Context context, boolean enable) {

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter != null) {

            if (enable && !bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.enable();
                Log.i("Setter", "Bluetooth: " + bluetoothAdapter.getState() + (" (on)"));
            } else if (!enable && bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.disable();
                Log.i("Setter", "Bluetooth: " + bluetoothAdapter.getState() + (" (off)"));
            } else {
                Log.i("Setter", "Bluetooth not changed.");
            }
        }
    }

    /**
     * Sets the screen brightness to the given value.
     *
     * @param context Context.
     * @param autoModeEnabled User preference state.
     */
    public void setScreenBrightnessMode(Context context, boolean autoModeEnabled) {

        if (autoModeEnabled == true) {

            android.provider.Settings.System.putInt(
                    context.getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE,
                    android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            Log.i("Setter", "BrightnessMode: " + android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        } else {
            android.provider.Settings.System.putInt(
                    context.getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE,
                    android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            Log.i("Setter", "BrightnessMode: " + android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        }
    }

    /**
     * Sets the screen preference value.
     *
     * @param context Context.
     * @param screenOffTimeout User preference state.
     */
    public void setScreenTimeout(Context context, int screenOffTimeout) {

        int time;
        switch (screenOffTimeout) {
            case 0:
                time = 15000;
                break;
            case 1:
                time = 30000;
                break;
            case 2:
                time = 60000;
                break;
            case 3:
                time = 120000;
                break;
            case 4:
                time = 300000;
                break;
            case 5:
                time = 600000;
                break;
            case 6:
                time = 1800000;
                break;
            default:
                time = -1;
        }

        android.provider.Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, time);
        Log.i("Setter", "TimeOut: " + time);
    }

    /**
     * Sets the ringer-mode to either: silent, vibrate or normal.
     *
     * @param context Context.
     * @param ringerMode User preference state.
     */
    public void setRingerMode(Context context, Profile.mode ringerMode) {

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        // Set to normal
        if (ringerMode == Profile.mode.normal && audioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            Log.i("Setter", "RingerMode: " + audioManager.getRingerMode() + (" (normal)"));
        }
        // Set to silent
        else if (ringerMode == Profile.mode.silent && audioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            Log.i("Setter", "RingerMode: " + audioManager.getRingerMode() + (" (silent)"));
        }
        // Set to vibrate
        else if (ringerMode == Profile.mode.vibrate && audioManager.getRingerMode() != AudioManager.RINGER_MODE_VIBRATE) {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            Log.i("Setter", "RingerMode: " + audioManager.getRingerMode() + (" (vibrate)"));
        } else {
            Log.i("Setter", "RingerMode not changed.");
        }
    }
}
