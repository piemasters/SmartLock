package net.davidnorton.securityapp.profile;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;

/**
 * Class that provides methods to apply different settings.
 *
 * @author Florian Schrofner & Dominik Koeltringer
 *
 */
public class Setter {

    /**
     * Sets the bluetooth adapter to the given state. If the adapter already has
     * the desired state, nothing will be changed.
     *
     * @param _context
     *            the context of your activity.
     * @param _enable
     *            true = enable bluetooth, false = disable bluetooth.
     */
    public void setBluetooth(Context _context, boolean _enable) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter
                .getDefaultAdapter();

        if (bluetoothAdapter != null) {

            if (_enable && !bluetoothAdapter.isEnabled()) {
                // if you want to enable bluetooth and it is disabled at the moment
                bluetoothAdapter.enable();
                Log.i("Setter", "Bluetooth: " + bluetoothAdapter.getState()
                        + (" (on)"));
            } else if (!_enable && bluetoothAdapter.isEnabled()) {
                // if you want to disable bluetooth and it is enabled
                bluetoothAdapter.disable();
                Log.i("Setter", "Bluetooth: " + bluetoothAdapter.getState()
                        + (" (off)"));
            } else {	// just for log messages
                Log.i("Setter", "Bluetooth not changed.");
            }
        }

    }

    /**
     * Sets the wifi to the given state. If it already is in the desired state,
     * nothing will be changed.
     *
     * @param _context
     *            the context of your activity.
     * @param _enable
     *            true = enable wifi, false = disable wifi.
     */
    public void setWifi(Context _context, boolean _enable) {

        WifiManager wifiManager = (WifiManager) _context
                .getSystemService(Context.WIFI_SERVICE);

        if (_enable && !wifiManager.isWifiEnabled()) {
            // if wifi should be enabled and is disabled at the moment
            wifiManager.setWifiEnabled(true);
            Log.i("Setter", "Wifi: " + wifiManager.getWifiState() + " (on)");
        } else if (!_enable && wifiManager.isWifiEnabled()) {
            // if wifi should be disabled and is enabled
            wifiManager.setWifiEnabled(false);
            Log.i("Setter", "Wifi: " + wifiManager.getWifiState() + " (off)");
        } else { // just for log messages
            Log.i("Setter", "Wifi not changed.");
        }
    }

    /**
     * Sets the state of gps to the given state. This uses a not so official way
     * of sending an intent to the official settings widget, otherwise changing
     * the gps state would not be possible. Most probably won't work on not AOSP
     * base roms.
     *
     * @param _context
     *            the context of your activity.
     * @param _enable
     *            true = enable gps, false = disable gps.
     */
    public void setGps(Context _context, boolean _enable) {
        String provider = Settings.Secure.getString(
                _context.getContentResolver(),
                Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        // if gps is disabled and you want to enable it or the other way round
        if ((_enable && !provider.contains("gps"))
                || (!_enable && provider.contains("gps"))) {
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", // sets the class of the intent to the class of the settings widget
                    "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            _context.sendBroadcast(poke); // sends an intent to the official settings widget
            Log.i("Setter", "GPS: state changed");
        } else { // just for log messages
            Log.i("Setter", "GPS not changed.");
        }

    }

    /**
     * Sets the ringer-mode according to the given parameter. Possible modes
     * would be: silent, vibrate and normal.
     *
     * @param _context
     *            the context of your activity.
     * @param _ringerMode
     *            the ringermode you want to set. needs to be of the enum mode,
     *            which is defined inside the profile class. Possible values
     *            would be: Profile.mode.silent, Profile.mode.vibrate or
     *            Profile.mode.normal.
     */
    public void setRingerMode(Context _context, Profile.mode _ringerMode) {
        AudioManager audioManager = (AudioManager) _context
                .getSystemService(Context.AUDIO_SERVICE);

        if (_ringerMode == Profile.mode.normal
                && audioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            // if mode should be set to normal and is not normal at the moment
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            Log.i("Setter", "RingerMode: " + audioManager.getRingerMode()
                    + (" (normal)"));
        } else if (_ringerMode == Profile.mode.silent
                && audioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
            // if mode should be set to silent and is not silent
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            Log.i("Setter", "RingerMode: " + audioManager.getRingerMode()
                    + (" (silent)"));
        } else if (_ringerMode == Profile.mode.vibrate
                && audioManager.getRingerMode() != AudioManager.RINGER_MODE_VIBRATE) {
            // if mode should be set to vibrate and is not vibrate
            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            Log.i("Setter", "RingerMode: " + audioManager.getRingerMode()
                    + (" (vibrate)"));
        } else {
            Log.i("Setter", "RingerMode not changed."); // for log messages only
        }

    }

    /**
     * Sets the media volume to the given value. Please note that the given
     * value will not be checked for validity here, because it will be already
     * checked inside the xmlParser.
     *
     * @param _context
     *            the context of your activity.
     * @param _mediaVolume
     *            the value you want to set the media-volume to.
     */
    public void setMediaVolume(Context _context, int _mediaVolume) {
        AudioManager audioManager = (AudioManager) _context
                .getSystemService(Context.AUDIO_SERVICE);

        if (_mediaVolume != audioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC)) {
            // if media-volume is not already set to the given value
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    _mediaVolume, 0);
            Log.i("Setter", "MediaVolume: "
                    + audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        } else {
            Log.i("Setter", "MediaVolume not changed."); // for log messages only
        }
    }

    /**
     * Sets the ringtone-volume to the given value. Please note that the given
     * value will not be checked for validity here, because it will be already
     * checked inside the xmlParser.
     *
     * @param _context
     *            the context of your activity.
     * @param _ringtoneVolume
     *            the value you want to set the ringtone-volume to.
     */
    public void setRingtoneVolume(Context _context, int _ringtoneVolume) {
        AudioManager audioManager = (AudioManager) _context
                .getSystemService(Context.AUDIO_SERVICE);

        if (_ringtoneVolume != audioManager
                .getStreamVolume(AudioManager.STREAM_RING)) {
            // if the ringtone-volume is not already set to the given value
            audioManager.setStreamVolume(AudioManager.STREAM_RING,
                    _ringtoneVolume, 0);
            Log.i("Setter", "RingtoneVolume: "
                    + audioManager.getStreamVolume(AudioManager.STREAM_RING));
        } else {
            Log.i("Setter", "RingtoneVolume not changed."); // for log messages only
        }
    }

    /**
     * Sets the alarm-volume to the given value. Please note that the given
     * value will not be checked for validity here, because it will be already
     * checked inside the xmlParser.
     *
     * @param _context
     *            your activity context.
     * @param _alarmVolume
     *            the value you want to set the alarm-volume to.
     */
    public void setAlarmVolume(Context _context, int _alarmVolume) {
        AudioManager audioManager = (AudioManager) _context
                .getSystemService(Context.AUDIO_SERVICE);

        if (_alarmVolume != audioManager
                .getStreamVolume(AudioManager.STREAM_ALARM)) {
            // if the alarm-volume is not already set to the given value
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM,
                    _alarmVolume, 0);
            Log.i("Setter", "AlarmVolume: "
                    + audioManager.getStreamVolume(AudioManager.STREAM_ALARM));
        } else {
            Log.i("Setter", "AlarmVolume not changed."); // for log messages only
        }
    }

//	/**
//	 * Sets the notification-volume to the given value. Please note that the
//	 * given value will not be checked for validity here, because it will be
//	 * already checked inside the xmlParser.
//	 *
//	 * @param _context
//	 *            your activity context.
//	 * @param _notificationVolume
//	 *            the value you want to set the notification volume to.
//	 */
//	public void setNotificationVolume(Context _context, int _notificationVolume) {
//		AudioManager audioManager = (AudioManager) _context
//				.getSystemService(Context.AUDIO_SERVICE);
//
//		if (_notificationVolume != audioManager
//				.getStreamVolume(AudioManager.STREAM_NOTIFICATION)) {
//			// if the notification volume is already set to the given value
//			audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION,
//					_notificationVolume, 0);
//			Log.i("Setter", "NotificationVolume: "
//					+ audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION));
//		} else {
//			Log.i("Setter", "NotificationVolume not changed."); // for log messages only
//		}
//	}

    /**
     * Sets mobile data to the given state.
     *
     * @param _context
     *            your activity context.
     * @param _enable
     *            true = enable mobile data, false = disable mobile data.
     * @throws ClassNotFoundException
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    public void setMobileData(Context _context, boolean _enable)
            throws ClassNotFoundException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        final ConnectivityManager conman = (ConnectivityManager) _context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final Class conmanClass = Class.forName(conman.getClass().getName());
        final Field iConnectivityManagerField = conmanClass
                .getDeclaredField("mService");
        iConnectivityManagerField.setAccessible(true);
        final Object iConnectivityManager = iConnectivityManagerField
                .get(conman);
        final Class iConnectivityManagerClass = Class
                .forName(iConnectivityManager.getClass().getName());
        final Method setMobileDataEnabledMethod = iConnectivityManagerClass
                .getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
        setMobileDataEnabledMethod.setAccessible(true);

        setMobileDataEnabledMethod.invoke(iConnectivityManager, _enable);
    }

    /**
     * Sets the screen brightness to the given value. Please note that the given
     * value will not be checked for validity here, because it will be already
     * checked inside the xmlParser.
     *
     * @param _context
     *            your activity context.
     * @param _brightness
     *            the value you want to set the brightness volume to.
     */
    public void setScreenBrightness(Context _context, int _brightness) {

        android.provider.Settings.System.putInt(_context.getContentResolver(),
                android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE,
                android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        android.provider.Settings.System
                .putInt(_context.getContentResolver(),
                        android.provider.Settings.System.SCREEN_BRIGHTNESS,
                        _brightness);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            WindowManager.LayoutParams lp = ((Activity) _context).getWindow()
                    .getAttributes();
            lp.screenBrightness = _brightness / 255.0f;
            ((Activity) _context).getWindow().setAttributes(lp);
        }

        Log.i("Setter", "Brightness: " + _brightness);
    }

    /**
     * Sets the ScreenBrightnessMode to manual or automatic. Please note that
     * the given value will not be checked for validity here, because it will be
     * already checked inside the xmlParser.
     *
     * @param _context
     *            your activity context.
     * @param _autoModeEnabled
     *            the value you want to set the brightness volume to.
     */
    public void setScreenBrightnessMode(Context _context,
                                        boolean _autoModeEnabled) {

        if (_autoModeEnabled == true) {
            android.provider.Settings.System
                    .putInt(_context.getContentResolver(),
                            android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE,
                            android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            Log.i("Setter",
                    "BrightnessMode: "
                            + android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        } else {
            android.provider.Settings.System
                    .putInt(_context.getContentResolver(),
                            android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE,
                            android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            Log.i("Setter",
                    "BrightnessMode: "
                            + android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        }
    }

    /**
     * Sets screen off timeout to the given value. Please note that the given
     * value will not be checked for validity here, because it will be already
     * checked inside the xmlParser.
     *
     * @param _context
     *            your activity context.
     * @param _screenOffTimeout
     *            screenOffTimeout int 0~6
     */
    public void setScreenTimeout(Context _context, int _screenOffTimeout) {
        int time;
        switch (_screenOffTimeout) {
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
        android.provider.Settings.System.putInt(_context.getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT, time);
        Log.i("Setter", "TimeOut: " + time);
    }
}
