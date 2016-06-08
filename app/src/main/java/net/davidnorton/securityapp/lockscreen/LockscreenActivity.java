package net.davidnorton.securityapp.lockscreen;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.view.GestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import net.davidnorton.securityapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Creates and updates the Lock Screen and handles all
 * screen interaction including PIN & NFC.
 *
 * @author David Norton
 *
 */
public class LockscreenActivity extends Activity implements View.OnClickListener, View.OnTouchListener, GestureDetector.OnGestureListener {

    // Contents of JSON file.
    private JSONObject root;
    private JSONObject settings;
    private JSONArray tags;
    private String pin;
    private Boolean pinLocked;
    private int blur;

    // Managers.
    private WindowManager windowManager;
    private ActivityManager activityManager;
    private int taskID; // Used by activityManager.
    private PackageManager packageManager;
    private WifiManager wifiManager;
    private ConnectivityManager connectivityManager;
    private NetworkInfo isMobileDataOn;
    private AudioManager audioManager;

    // Home Launcher.
    private ComponentName homeLauncher;
    private int componentDisabled, componentEnabled;
    private int homeLauncherToastCount = 2; // Number of times to display select home message.

    // PIN.
    private String pinEntered = ""; // The PIN the user inputs.
    private int pinAttempts = 5; // Number of attempts before PIN locked for 30s.
    private final Handler delayHandler = new Handler(); // Sets 30s delay.
    private static final int PIN_LOCKED_RUNNABLE_DELAY = 30000; // Unlock after 3s.
    private Vibrator vibrator;

    // NFC.
    private NfcAdapter nfcAdapter;
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray(); // For bytes to hex.
    private PendingIntent NFCIntent; // For tag discovery.

    // Brightness & Volume.
    private ContentResolver brightnessResolver;
    private static final int LOW_BRIGHTNESS = 0;
    private static final int MEDIUM_BRIGHTNESS = 80;
    private static final int HIGH_BRIGHTNESS = 255;
    private int brightnessMode = -1; // 0 (LOW), 1 (MEDIUM), 2(HIGH) and 3(AUTO)
    private int ringerMode; // 1 (NORMAL), 2 (VIBRATE) and 3 (SILENT)

    // Time & Date.
    private Calendar calendar;

    // Camera & Torch.
    private Camera defaultCamera;
    private Parameters defaultCameraParameters;
    private Boolean torchAvailable = false; // True if FEATURE_FLASH exists, false otherwise
    private Boolean isFlashOn = false; // True if flash was turned on, false otherwise
    private SurfaceHolder phoneRinging;

    // If phone or camera are set to open, or if receiving phone call.
    private Boolean openPhone = false;
    private Boolean openCamera = false;
    private Boolean isPhoneCalling = false;

    // Gesture detection.
    private static final int SWIPE_MIN_DIST = 180;
    private GestureDetector gestureDetector;
    private Boolean swipedUp = false;
    private Boolean swipedDown = true;

    // Layout items
    private TextView time, date, battery, unlockText, pinInput;
    private ImageButton wifi, data, torch, brightness, sound, phone, camera, up, down;
    private View toolboxFrame;

    // View animations.
    private Animation slideUp, slideDown, fadeIn, fadeOut;

    /**
     * Create the lock screen.
     *
     * @param savedInstanceState Saves current state of application to be referred back to.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lockscreen);

        readFromJSON();
        gestureDetector = new GestureDetector(this, this);

        // Initialize window manager, activity manager and package manager.
        windowManager = (WindowManager) getApplicationContext().getSystemService(WINDOW_SERVICE);
        activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        taskID = getTaskId();
        packageManager = getPackageManager();
        componentEnabled = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        componentDisabled = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

        // Initialize home launcher.
        homeLauncher = new ComponentName(this, "net.davidnorton.securityapp.SmartLockLauncher");
        packageManager.setComponentEnabledSetting(homeLauncher, componentEnabled, PackageManager.DONT_KILL_APP);

        // Get Audio service for changing volume.
        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        // Get WiFi service for changing connectivity.
        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);

        // Get Mobile Data service and check if connected..
        connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        isMobileDataOn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        // Get NFC service and adapter for reading tags
        NfcManager nfcManager = (NfcManager) this.getSystemService(Context.NFC_SERVICE);
        nfcAdapter = nfcManager.getDefaultAdapter();

        // If Android version less than 4.4, createPendingResult for NFC tag discovery
        if (Build.VERSION.SDK_INT < 19) {
            NFCIntent = PendingIntent.getActivity(
                    this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        }

        // Initialize phone call listener so users can accept or deny calls.
        StateListener phoneStateListener = new StateListener();
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        // Set content resolver for adjusting screen brightness.
        brightnessResolver = getContentResolver();

        // Initilize vibrator.
        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        //TODO: Update to camera2.
        // Initialize surfaceView for camera torch.
        torchAvailable = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        phoneRinging = surfaceView.getHolder();

        // Set blurred or normal wallpaper.
        Drawable blurredWallpaper = ImageUtils.retrieveWallpaperDrawable();

        if (blurredWallpaper != null && blur != 0)
            getWindow().setBackgroundDrawable(blurredWallpaper);
        else {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
            Drawable wallpaperDrawable = wallpaperManager.peekFastDrawable();

            if (wallpaperDrawable != null)
                getWindow().setBackgroundDrawable(wallpaperDrawable);
        }

        // Initialize text layout items.
        time = (TextView) findViewById(R.id.time);
        date = (TextView) findViewById(R.id.date);
        battery = (TextView) findViewById(R.id.battery);
        unlockText = (TextView) findViewById(R.id.unlockText);
        pinInput = (TextView) findViewById(R.id.pinInput);

        // Initialize number buttons.
        ImageButton ic_0 = (ImageButton) findViewById(R.id.ic_0);
        ImageButton ic_1 = (ImageButton) findViewById(R.id.ic_1);
        ImageButton ic_2 = (ImageButton) findViewById(R.id.ic_2);
        ImageButton ic_3 = (ImageButton) findViewById(R.id.ic_3);
        ImageButton ic_4 = (ImageButton) findViewById(R.id.ic_4);
        ImageButton ic_5 = (ImageButton) findViewById(R.id.ic_5);
        ImageButton ic_6 = (ImageButton) findViewById(R.id.ic_6);
        ImageButton ic_7 = (ImageButton) findViewById(R.id.ic_7);
        ImageButton ic_8 = (ImageButton) findViewById(R.id.ic_8);
        ImageButton ic_9 = (ImageButton) findViewById(R.id.ic_9);
        Button delete = (Button) findViewById(R.id.delete);

        // Set onClick listeners
        ic_0.setOnClickListener(this);
        ic_1.setOnClickListener(this);
        ic_2.setOnClickListener(this);
        ic_3.setOnClickListener(this);
        ic_4.setOnClickListener(this);
        ic_5.setOnClickListener(this);
        ic_6.setOnClickListener(this);
        ic_7.setOnClickListener(this);
        ic_8.setOnClickListener(this);
        ic_9.setOnClickListener(this);
        delete.setOnClickListener(this);

        // Initialize bottom drawer items.
        toolboxFrame = findViewById(R.id.toolboxFrame);
        wifi = (ImageButton) findViewById(R.id.wifi);
        data = (ImageButton) findViewById(R.id.data);
        torch = (ImageButton) findViewById(R.id.flashlight);
        brightness = (ImageButton) findViewById(R.id.brightness);
        sound = (ImageButton) findViewById(R.id.sound);
        up = (ImageButton) findViewById(R.id.up);
        down = (ImageButton) findViewById(R.id.down);
        phone = (ImageButton) findViewById(R.id.phone);
        camera = (ImageButton) findViewById(R.id.camera);

        // Set onClick listeners
        wifi.setOnClickListener(this);
        data.setOnClickListener(this);
        torch.setOnClickListener(this);
        brightness.setOnClickListener(this);
        sound.setOnClickListener(this);
        up.setOnClickListener(this);
        down.setOnClickListener(this);
        phone.setOnClickListener(this);
        camera.setOnClickListener(this);

        // Initialize date, time and battery.
        calendar = Calendar.getInstance();
        updateTime();
        updateDate();
        updateBattery(getBatteryLevel());

        // Create an intent filter with time/date/battery changes and register receiver.
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        intentFilter.addAction(Intent.ACTION_DATE_CHANGED);
        this.registerReceiver(mChangeReceiver, intentFilter);

        // Initialize animations
        slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        // Display bottom drawer on swipe up.
        up.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (swipedUp) return false;

                swipedUp = true;
                swipedDown = false;
                swipeUp();

                return true;
            }
        });

        // Hide bottom drawer on swipe down.
        down.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (swipedDown) return false;

                swipedDown = true;
                swipedUp = false;
                swipeDown();

                return true;
            }
        });

        // Set screen brightness mode and display correct image.
        try {
            // If set to automatic.
            if (Settings.System.getInt(brightnessResolver, Settings.System.SCREEN_BRIGHTNESS_MODE) ==
                    Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                brightnessMode = 3;
                brightness.setImageDrawable(ResourcesCompat.getDrawable(getApplicationContext().getResources(),R.drawable.ic_bright_auto, null));
            } else {
                // If set to low.
                if (Settings.System.getInt(brightnessResolver, Settings.System.SCREEN_BRIGHTNESS) >= LOW_BRIGHTNESS &&
                        Settings.System.getInt(brightnessResolver, Settings.System.SCREEN_BRIGHTNESS) < MEDIUM_BRIGHTNESS) {

                    brightnessMode = 0;
                    brightness.setImageDrawable(ResourcesCompat.getDrawable(getApplicationContext().getResources(), R.drawable.ic_bright_0, null));
                }
                // If set to medium.
                else if (Settings.System.getInt(brightnessResolver, Settings.System.SCREEN_BRIGHTNESS) >= MEDIUM_BRIGHTNESS &&
                        Settings.System.getInt(brightnessResolver, Settings.System.SCREEN_BRIGHTNESS) < HIGH_BRIGHTNESS) {

                    brightnessMode = 1;
                    brightness.setImageDrawable(ResourcesCompat.getDrawable(getApplicationContext().getResources(), R.drawable.ic_bright_1, null));
                }
                // If set to high.
                else if (Settings.System.getInt(brightnessResolver, Settings.System.SCREEN_BRIGHTNESS) == HIGH_BRIGHTNESS) {

                    brightnessMode = 2;
                    brightness.setImageDrawable(ResourcesCompat.getDrawable(getApplicationContext().getResources(), R.drawable.ic_bright_2, null));
                }
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        // Set correct WiFi image.
        if (wifiManager.isWifiEnabled()) {
            wifi.setImageDrawable(ResourcesCompat.getDrawable(getApplicationContext().getResources(), R.drawable.ic_wifi_on, null));
        } else {
            wifi.setImageDrawable(ResourcesCompat.getDrawable(getApplicationContext().getResources(), R.drawable.ic_wifi_off, null));
        }

        // Set correct mobile data image if supported.
        if (isMobileDataOn != null) {
            if (isMobileDataOn.isConnected()) {
                data.setImageDrawable(ResourcesCompat.getDrawable(getApplicationContext().getResources(), R.drawable.ic_data_on, null));
            } else {
                data.setImageDrawable(ResourcesCompat.getDrawable(getApplicationContext().getResources(), R.drawable.ic_data_off, null));
            }
        }

        // Set correct ringer mode image.
        switch (audioManager.getRingerMode()) {

            case AudioManager.RINGER_MODE_NORMAL:
                sound.setImageDrawable(ResourcesCompat.getDrawable(getApplicationContext().getResources(), R.drawable.ic_speaker, null));
                ringerMode = 1;
                break;

            case AudioManager.RINGER_MODE_VIBRATE:
                sound.setImageDrawable(ResourcesCompat.getDrawable(getApplicationContext().getResources(), R.drawable.ic_vibration, null));
                ringerMode = 2;
                break;

            case AudioManager.RINGER_MODE_SILENT:
                sound.setImageDrawable(ResourcesCompat.getDrawable(getApplicationContext().getResources(), R.drawable.ic_silent, null));
                ringerMode = 3;
                break;
        }

        // If not default launcher display 'Select Home App' dialog for user to change.
        if (!isDefaultLauncher()) {

            packageManager.clearPackagePreferredActivities(getPackageName());

            // Open 'Select Home' dialog.
            Intent launcherPicker = new Intent();
            launcherPicker.setAction(Intent.ACTION_MAIN);
            launcherPicker.addCategory(Intent.CATEGORY_HOME);
            startActivity(launcherPicker);

            // Display toast instructions (2 times max).
            if (homeLauncherToastCount > 0) {

                homeLauncherToastCount -= 1;

                Toast toast = Toast.makeText(getApplicationContext(), R.string.lockscreen_toast_launcher, Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    /**
     * Check if default home launcher.
     *
     * @return If is default launcher.
     */
    private boolean isDefaultLauncher() {

        final IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);

        List<IntentFilter> filters = new ArrayList<>();
        filters.add(filter);

        final String packageName = getPackageName();
        List<ComponentName> activities = new ArrayList<>();

        // Get list of preferred activities.
        packageManager.getPreferredActivities(filters, activities, "net.davidnorton.securityapp");

        // Check app package is on the list.
        for (ComponentName activity : activities) {
            if (packageName.equals(activity.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Broadcast receiver to run in background and detect changes
     * in time, date and battery and update on screen.
     */
    private final BroadcastReceiver mChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            switch (action) {
                case Intent.ACTION_TIME_TICK:
                    updateTime();
                    break;
                case Intent.ACTION_BATTERY_CHANGED:
                    int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    updateBattery(level);
                    break;
                case Intent.ACTION_DATE_CHANGED:
                    updateDate();
                    break;
            }
        }
    };

    /**
     * Update time.
     */
    private void updateTime() {

        calendar = Calendar.getInstance();

        // Get hour and minute.
        String hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        String minute = ":" + String.valueOf(calendar.get(Calendar.MINUTE));

        // Prepend a 0 to hour when only a single digit.
        if (hour.length() < 2) {
            hour = "0" + String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        }

        // Prepend a 0 to minute when only a single digit.
        if (minute.length() < 3) {
            minute = ":" + "0" + String.valueOf(calendar.get(Calendar.MINUTE));
        }

        String timeValue = hour + minute;
        time.setText(timeValue);
    }

    /**
     * Update date.
     */
    private void updateDate() {

        calendar = Calendar.getInstance();

        // Get weekday, day and month.
        String weekday = String.valueOf(calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US));
        String day = " " + String.valueOf(calendar.get(Calendar.DATE));
        String month = " " + String.valueOf(calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US));

        String dateFinal = weekday + day + month;
        date.setText(dateFinal);
    }

    /**
     * Get battery level.
     * @return Battery level.
     */
    private int getBatteryLevel() {

        int level = -1;
        Intent batteryIntent = getApplicationContext().registerReceiver(null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        // Get battery level.
        if (batteryIntent != null) {
            level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        }

        return level;
    }

    /**
     * Update battery level.
     *
     * @param batteryLevel Battery level.
     */
    private void updateBattery(int batteryLevel) {

        Context context = getApplicationContext();

        battery.setText(String.valueOf(batteryLevel) + "% " + getResources().getString(R.string.lockscreen_battery_text));

        // Set text color based on battery level.
        if (batteryLevel > 15) {
            battery.setTextColor(ContextCompat.getColor(context, R.color.accept));
        } else {
            battery.setTextColor(ContextCompat.getColor(context, R.color.warning));
        }
    }

    /**
     * Handle screen touches.
     *
     * @param v Lock screen view.
     */
    @Override
    public void onClick(View v) {

        // If PIN number button pressed, add number to pinEntered and call checkPin.
        if (v.getId() == R.id.ic_0) {
            pinEntered += "0";
            checkPIN();
        } else if (v.getId() == R.id.ic_1) {
            pinEntered += "1";
            checkPIN();
        } else if (v.getId() == R.id.ic_2) {
            pinEntered += "2";
            checkPIN();
        } else if (v.getId() == R.id.ic_3) {
            pinEntered += "3";
            checkPIN();
        } else if (v.getId() == R.id.ic_4) {
            pinEntered += "4";
            checkPIN();
        } else if (v.getId() == R.id.ic_5) {
            pinEntered += "5";
            checkPIN();
        } else if (v.getId() == R.id.ic_6) {
            pinEntered += "6";
            checkPIN();
        } else if (v.getId() == R.id.ic_7) {
            pinEntered += "7";
            checkPIN();
        } else if (v.getId() == R.id.ic_8) {
            pinEntered += "8";
            checkPIN();
        } else if (v.getId() == R.id.ic_9) {
            pinEntered += "9";
            checkPIN();
        }

        // If delete pressed, delete last pinEntered number.
        else if (v.getId() == R.id.delete) {
            // If PIN isn't locked and the entered PIN isn't blank.
            if (!pinLocked && pinEntered.length() > 0) {
                // If entered PIN is 1 digit, set to blank.
                if (pinEntered.length() == 1) {
                    pinEntered = "";
                    pinInput.setText(pinEntered);
                    vibrator.vibrate(50);
                    return;
                }
                // If entered PIN is more than 1 digit, remove the last digit.
                else {
                    pinEntered = pinEntered.substring(0, pinEntered.length() - 1);
                    pinInput.setText(pinEntered);
                    return;
                }
            }
            vibrator.vibrate(50);
        }

        // If WiFi button pressed, enable/disable WiFi.
        else if (v.getId() == R.id.wifi) {
            if (wifiManager.isWifiEnabled()) {
                wifiOn(false);
            } else {
                wifiOn(true);
            }
        }

        // If mobile data button pressed, enable/disable mobile data.
        else if (v.getId() == R.id.data) {

            // If mobile data is available toggle on/off.
            if (isMobileDataOn != null) {

                if (isMobileDataOn.isConnected()) {
                    try {
                        mobileDataOn(false);
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        mobileDataOn(true);
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
            // No mobile data detected, show error.
            else {
                Toast toast = Toast.makeText(getApplicationContext(), R.string.lockscreen_toast_mobile_error, Toast.LENGTH_SHORT);
                toast.show();
            }
        }

        // If torch button pressed, enable/disable torch.
        else if (v.getId() == R.id.flashlight) {

            // If torch is available toggle on/off.
            if (torchAvailable) {
                if (!isFlashOn) {
                    torchOn(true);
                    // Add window flag to keep screen on.
                    this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } else {
                    torchOn(false);
                    // Remove window flag that kept the screen on.
                    this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            }

            // If torch is not available, show error.
            else {
                Toast toast = Toast.makeText(getApplicationContext(), R.string.lockscreen_toast_torch_error, Toast.LENGTH_SHORT);
                toast.show();
            }
        }

        // If brightness button pressed, set brightness.
        else if (v.getId() == R.id.brightness) {

            // If brightnessMode not initialized, set to auto.
            if (brightnessMode == -1) {
                brightnessMode = 3;
            }

            // If auto set to low.
            if (brightnessMode == 3) {
                brightnessChange(0);
                brightnessMode = 0;
            }
            // If low set to medium.
            else if (brightnessMode == 0) {
                brightnessChange(1);
                brightnessMode = 1;
            }
            // If medium set to high.
            else if (brightnessMode == 1) {
                brightnessChange(2);
                brightnessMode = 2;
            }
            // If high set to auto.
            else if (brightnessMode == 2) {
                brightnessChange(3);
                brightnessMode = 3;
            }
        }

        // If sound button pressed, change ringer mode.
        else if (v.getId() == R.id.sound) {

            // If normal set to vibrate.
            if (ringerMode == 1) {
                ringerChange(2);
                ringerMode = 2;
            }
            // If vibrate set to silent.
            else if (ringerMode == 2) {
                ringerChange(3);
                ringerMode = 3;
            }
            // If silent set to normal.
            else if (ringerMode == 3) {
                ringerChange(1);
                ringerMode = 1;
            }
        }

        // If phone button pressed, set to open when lock screen is destroyed and display message.
        else if (v.getId() == R.id.phone) {
            openPhone = true;
            openCamera = false;

            Toast toast = Toast.makeText(getApplicationContext(), R.string.lockscreen_toast_phone_success, Toast.LENGTH_SHORT);
            toast.show();
        }


        // If camera button pressed, set to open when lock screen is destroyed and display message.
        else if (v.getId() == R.id.camera) {
            openCamera = true;
            openPhone = false;

            Toast toast = Toast.makeText(getApplicationContext(), R.string.lockscreen_toast_camera_success, Toast.LENGTH_SHORT);
            toast.show();
        }

        // If up button pressed, display toolbox.
        else if (v.getId() == R.id.up) {
            swipeUp();
        }

        // If down button pressed, hide toolbox.
        else if (v.getId() == R.id.down) {
            swipeDown();
        }
    }

    /**
     * Detects swipe up/down to toggle the toolbox.
     *
     * @param e1 Swipe start point.
     * @param e2 Swipe end point.
     * @param velocityX X velocity of swipe.
     * @param velocityY Y velocity of swipe.
     *
     * @return false.
     */
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        // If swipe down gesture detected call swipeDown.
        if (e1.getY() < e2.getY() && (e2.getY() - e1.getY()) > SWIPE_MIN_DIST) {
            if (swipedDown) {
                return false;
            }
            swipedDown = true;
            swipedUp = false;
            swipeDown();
        }
        // If swipe up gesture detected call swipeUp.
        else if (e1.getY() > e2.getY() && (e1.getY() - e2.getY()) > SWIPE_MIN_DIST) {
            if (swipedUp) {
                return false;
            }
            swipedUp = true;
            swipedDown = false;
            swipeUp();
        }

        return false;
    }

    /**
     * Hide up/phone/camera and display down/toolbox.
     */
    private void swipeUp() {

        up.startAnimation(fadeOut);
        phone.startAnimation(fadeOut);
        camera.startAnimation(fadeOut);

        up.setVisibility(View.INVISIBLE);
        phone.setVisibility(View.INVISIBLE);
        camera.setVisibility(View.INVISIBLE);

        toolboxFrame.startAnimation(slideUp);
        toolboxFrame.setVisibility(View.VISIBLE);

        wifi.startAnimation(fadeIn);
        data.startAnimation(fadeIn);
        torch.startAnimation(fadeIn);
        brightness.startAnimation(fadeIn);
        sound.startAnimation(fadeIn);

        wifi.setVisibility(View.VISIBLE);
        data.setVisibility(View.VISIBLE);
        torch.setVisibility(View.VISIBLE);
        brightness.setVisibility(View.VISIBLE);
        sound.setVisibility(View.VISIBLE);

        down.setVisibility(View.VISIBLE);
    }

    /**
     * Hide down/toolbox and display up/phone/camera.
     */
    private void swipeDown() {

        down.setVisibility(View.INVISIBLE);

        wifi.startAnimation(fadeOut);
        data.startAnimation(fadeOut);
        torch.startAnimation(fadeOut);
        brightness.startAnimation(fadeOut);
        sound.startAnimation(fadeOut);

        wifi.setVisibility(View.INVISIBLE);
        data.setVisibility(View.INVISIBLE);
        torch.setVisibility(View.INVISIBLE);
        brightness.setVisibility(View.INVISIBLE);
        sound.setVisibility(View.INVISIBLE);

        toolboxFrame.startAnimation(slideDown);
        toolboxFrame.setVisibility(View.INVISIBLE);

        up.startAnimation(fadeIn);
        phone.startAnimation(fadeIn);
        camera.startAnimation(fadeIn);

        up.setVisibility(View.VISIBLE);
        phone.setVisibility(View.VISIBLE);
        camera.setVisibility(View.VISIBLE);
    }

    /**
     * Handle different phone call states.
     */
    private class StateListener extends PhoneStateListener {

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            super.onCallStateChanged(state, incomingNumber);

            switch (state) {
                // If state is ringing, set isPhoneCalling to true and move lock screen to back.
                case TelephonyManager.CALL_STATE_RINGING:
                    isPhoneCalling = true;
                    moveTaskToBack(true);
                    break;
                // If state is offhook, set isPhoneCalling to true and move lock screen to back.
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    isPhoneCalling = true;
                    moveTaskToBack(true);
                    break;
                // If call stopped or idle and isPhoneCalling is true, move lock screen to front.
                case TelephonyManager.CALL_STATE_IDLE:
                    if (isPhoneCalling) {
                        activityManager.moveTaskToFront(taskID, 0);
                        isPhoneCalling = false;
                    }
                    break;
            }
        }
    }

    /**
     * Get camera & parameters.
     */
    private void getCamera() {

        if (defaultCamera == null) {
            try {
                defaultCamera = Camera.open();
                if (defaultCamera != null) {
                    defaultCameraParameters = defaultCamera.getParameters();
                }
            } catch (RuntimeException re) {
                re.printStackTrace();
                // Display error message if camera fails.
                Toast toast = Toast.makeText(getApplicationContext(), R.string.lockscreen_toast_camera_error, Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    /**
     * Stop camera and release.
     */
    private void releaseCamera() {

        if (defaultCamera != null) {
            try {
                defaultCamera.stopPreview();
                defaultCamera.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            defaultCamera = null;
        }
    }

    /**
     * Turn WiFi on or off.
     *
     * @param enabled On or Off.
     */
    private void wifiOn(boolean enabled) {

        if (enabled) {
            wifiManager.setWifiEnabled(true);
            wifi.setImageDrawable(ResourcesCompat.getDrawable(getApplicationContext().getResources(), R.drawable.ic_wifi_on, null));
        } else {
            wifiManager.setWifiEnabled(false);
            wifi.setImageDrawable(ResourcesCompat.getDrawable(getApplicationContext().getResources(), R.drawable.ic_wifi_off, null));
        }
    }

    /**
     * Turn Mobile Data on or off.
     *
     * @param enabled On or Off.
     *
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private void mobileDataOn(boolean enabled) throws NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {

        //TODO: This solution no longer works as of Android 5.0
        // http://tinyurl.com/nsrs39x http://tinyurl.com/o6d79sv http://tinyurl.com/p2rv3m8 http://tinyurl.com/p5vgpnp

        Method mobileData = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
        mobileData.setAccessible(true);
        mobileData.invoke(connectivityManager, enabled);

        if (enabled) {
            data.setImageDrawable(ResourcesCompat.getDrawable(getApplicationContext().getResources(), R.drawable.ic_data_on, null));
        } else {
            data.setImageDrawable(ResourcesCompat.getDrawable(getApplicationContext().getResources(), R.drawable.ic_data_off, null));
        }
    }

    /**
     * Turn torch on or off
     *
     * @param enabled On or Off.
     */
    private void torchOn(boolean enabled) {

        // Turn torch on.
        if (enabled) {

            // Get camera.
            getCamera();

            // If camera fails display error.
            if (defaultCamera == null || defaultCameraParameters == null) {
                Toast toast = Toast.makeText(getApplicationContext(), R.string.lockscreen_toast_camera_error, Toast.LENGTH_SHORT);
                toast.show();

                return;
            }

            // Set surface holder for camera.
            try {
                if (phoneRinging != null) {
                    defaultCamera.setPreviewDisplay(phoneRinging);
                }
            } catch (IOException e) {
                e.printStackTrace();
                // If surface holder fails display error.
                Toast toast = Toast.makeText(getApplicationContext(), R.string.lockscreen_toast_camera_error, Toast.LENGTH_SHORT);
                toast.show();

                return;
            }

            // Enable torch parameter.
            defaultCameraParameters = defaultCamera.getParameters();
            defaultCameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);

            // Apply parameters.
            try {
                defaultCamera.setParameters(defaultCameraParameters);
            } catch (RuntimeException re) {
                re.printStackTrace();
                // If camera fails display error.
                Toast toast = Toast.makeText(getApplicationContext(), R.string.lockscreen_toast_camera_error, Toast.LENGTH_SHORT);
                toast.show();

                return;
            }

            //  Start camera turning on torch.
            try {
                defaultCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
                // If camera fails display error.
                Toast toast = Toast.makeText(getApplicationContext(), R.string.lockscreen_toast_camera_error,  Toast.LENGTH_SHORT);
                toast.show();

                return;
            }

            isFlashOn = true;
            torch.setImageDrawable(ResourcesCompat.getDrawable(getApplicationContext().getResources(), R.drawable.ic_flashlight_on, null));
        }

        // Turn torch off.
        else {

            // If camera fails display error.
            if (defaultCamera == null || defaultCameraParameters == null) {
                Toast toast = Toast.makeText(getApplicationContext(), R.string.lockscreen_toast_camera_error, Toast.LENGTH_SHORT);
                toast.show();

                return;
            }

            // Disable torch parameter.
            defaultCameraParameters = defaultCamera.getParameters();
            defaultCameraParameters.setFlashMode(Parameters.FLASH_MODE_OFF);

            // Apply parameters.
            try {
                defaultCamera.setParameters(defaultCameraParameters);
            } catch (RuntimeException re) {
                re.printStackTrace();
            }

            torch.setImageDrawable(ResourcesCompat.getDrawable(getApplicationContext().getResources(), R.drawable.ic_flashlight_off, null));

            releaseCamera();
            isFlashOn = false;
        }
    }

    /**
     * Change screen brightness.
     *
     * @param mode Brightness mode.
     */
    private void brightnessChange(int mode) {

        // Set low brightness.
        if (mode == 0) {

            // Update image.
            if (brightness != null) {
                brightness.setImageDrawable(ResourcesCompat.getDrawable(getApplicationContext().getResources(), R.drawable.ic_bright_0, null));
            }

            // Update brightness.
            if (brightnessResolver != null) {
                Settings.System.putInt(brightnessResolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

                Settings.System.putInt(brightnessResolver, Settings.System.SCREEN_BRIGHTNESS, LOW_BRIGHTNESS);
            }
        }

        // Set medium brightness.
        else if (mode == 1) {

            // Update image.
            if (brightness != null) {
                brightness.setImageDrawable(ResourcesCompat.getDrawable(getApplicationContext().getResources(), R.drawable.ic_bright_1, null));
            }

            // Update brightness.
            if (brightnessResolver != null) {
                Settings.System.putInt(brightnessResolver, Settings.System.SCREEN_BRIGHTNESS, MEDIUM_BRIGHTNESS);
            }
        }

        // Set high brightness.
        else if (mode == 2) {

            // Update image.
            if (brightness != null)
                brightness.setImageDrawable(ResourcesCompat.getDrawable(getApplicationContext().getResources(), R.drawable.ic_bright_2, null));

            // Update brightness.
            if (brightnessResolver != null) {
                Settings.System.putInt(brightnessResolver, Settings.System.SCREEN_BRIGHTNESS, HIGH_BRIGHTNESS);
            }
        }

        // Set auto brightness
        else if (mode == 3) {

            // Update image.
            if (brightness != null)
                brightness.setImageDrawable(ResourcesCompat.getDrawable(getApplicationContext().getResources(), R.drawable.ic_bright_auto, null));

            // Update brightness.
            if (brightnessResolver != null) {
                Settings.System.putInt(brightnessResolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            }
        }
    }

    /**
     * Change ringer mode.
     *
     * @param mode Ringer mode.
     */
    private void ringerChange(int mode) {

        // Set to normal mode.
        if (mode == 1) {
            sound.setImageDrawable(ResourcesCompat.getDrawable(getApplicationContext().getResources(), R.drawable.ic_speaker, null));
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        }
        // Set to vibrate.
        else if (mode == 2) {
            sound.setImageDrawable(ResourcesCompat.getDrawable(getApplicationContext().getResources(), R.drawable.ic_vibration, null));
            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        }
        // Set to silent
        else if (mode == 3) {
            sound.setImageDrawable(ResourcesCompat.getDrawable(getApplicationContext().getResources(), R.drawable.ic_silent, null));
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        }
    }

    /**
     * Check PIN entered is correct.
     */
    private void checkPIN() {

        // If PIN not locked from too many incorrect attempts.
        if (!pinLocked) {

            // Display entered PIN on screen.
            pinInput.setText(pinEntered);

            // If the length or the pin is correct.
            if (pinEntered.length() == pin.length()) {

                // Set home launcher activity component disabled
                // If PIN is correct.
                if (pinEntered.equals(pin)) {

                    // Reset entered PIN and display.
                    pinEntered = "";
                    pinInput.setText(pinEntered);

                    // Remove 30 sec delay if active.
                    delayHandler.removeCallbacksAndMessages(null);

                    // Disable as home activity so home button doesn't launch the lock screen, but don't kill app.
                    packageManager.setComponentEnabledSetting(homeLauncher, componentDisabled, PackageManager.DONT_KILL_APP);

                    // Close lock screen.
                    finish();
                }

                // If PIN entered is incorrect.
                else {

                    // If number of attempts is above 0.
                    if (pinAttempts > 0) {

                        // Reduce number of attempts.
                        pinAttempts -= 1;

                        // Reset entered PIN and display.
                        pinEntered = "";
                        pinInput.setText(pinEntered);

                        // Vibrate and display wrong PIN message.
                        vibrator.vibrate(250);
                        unlockText.setText(getResources().getString(R.string.lockscreen_message_pin_wrong));
                    }

                    // If no login attempts remain.
                    else {

                        vibrator.vibrate(500);

                        // If NFC is enabled display prompt to scan NFC tag, else display 30sec timeout message.
                        if (nfcAdapter != null && nfcAdapter.isEnabled()) {
                                unlockText.setText(getResources().getString(R.string.lockscreen_message_pin_locked));
                        } else {
                            unlockText.setText(getResources().getString(R.string.lockscreen_message_pin_locked_nfc_off));
                        }

                        // Reset entered PIN and display.
                        pinEntered = "";
                        pinInput.setText(pinEntered);

                        // Lock PIN due to too many failed attempts.
                        pinLocked = true;

                        // Reset number of attempts.
                        pinAttempts = 5;

                        // Update JSON file.
                        try {
                            settings.put("pinLocked", true);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        writeToJSON();

                        // Run pinLockedRunnable to unlock the PIN after 30sec.
                        delayHandler.postDelayed(pinLockedRunnable, PIN_LOCKED_RUNNABLE_DELAY);
                    }
                }
            }
        }
    }

    /**
     * Enable the PIN after being disabled from too many
     * incorrect attempts.
     */
    private final Runnable pinLockedRunnable = new Runnable() {
        @Override
        public void run() {

            vibrator.vibrate(150);

            // Unlock PIN and update JSON.
            pinLocked = false;

            try {
                settings.put("pinLocked", false);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            writeToJSON();

            // Reset entered PIN and remove PIN and timer message.
            pinEntered = "";
            pinInput.setText(pinEntered);
            unlockText.setText("");

        }
    };

    /**
     * Resume the lock screen and enable NFC tag discovery.
     */
    @Override
    protected void onResume() {

        // Move lock screen to front.
        super.onResume();
        activityManager.moveTaskToFront(taskID, 0);

        // Re-enable as home launcher.
        if (packageManager != null) {
            packageManager.setComponentEnabledSetting(homeLauncher, componentEnabled, PackageManager.DONT_KILL_APP);
        }

        // If NFC is enabled.
        if (nfcAdapter != null && nfcAdapter.isEnabled()) {

            // If Android version lower than 4.4, use foreground dispatch method.
            if (Build.VERSION.SDK_INT < 19) {
                nfcAdapter.enableForegroundDispatch(this, NFCIntent, new IntentFilter[]{
                                new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)}, new String[][]{
                                new String[]{"android.nfc.tech.MifareClassic"},
                                new String[]{"android.nfc.tech.MifareUltralight"},
                                new String[]{"android.nfc.tech.NfcA"},
                                new String[]{"android.nfc.tech.NfcB"},
                                new String[]{"android.nfc.tech.NfcF"},
                                new String[]{"android.nfc.tech.NfcV"},
                                new String[]{"android.nfc.tech.Ndef"},
                                new String[]{"android.nfc.tech.IsoDep"},
                                new String[]{"android.nfc.tech.NdefFormatable"}
                        }
                );
            }
            // For newer versions of Android use enableReaderMode.
            else {
                nfcAdapter.enableReaderMode(this, new NfcAdapter.ReaderCallback() {

                    // If a tag is discovered.
                    @Override
                    public void onTagDiscovered(Tag tag) {

                        // Get tag ID and convert to readable string
                        byte[] tagID = tag.getId();
                        String tagDiscovered = bytesToHex(tagID);

                        if (!tagDiscovered.equals("")) {

                            // Loop through added NFC tags
                            for (int i = 0; i < tags.length(); i++) {

                                try {
                                    // When discovered tag ID matches a stored tag.
                                    if (tagDiscovered.equals(tags.getJSONObject(i).getString("tagID"))) {

                                        // Reset tagDiscovered, set pinLocked to false and store.
                                        tagDiscovered = "";
                                        pinLocked = false;
                                        settings.put("pinLocked", false);
                                        writeToJSON();

                                        // Remove 30 sec delay if active.
                                        delayHandler.removeCallbacksAndMessages(null);

                                        // Disable as home activity so home button doesn't launch the lock screen, but don't kill app.
                                        packageManager.setComponentEnabledSetting(homeLauncher, componentDisabled, PackageManager.DONT_KILL_APP);

                                        // Close lock screen.
                                        finish();
                                        return;
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            // If tag ID is not correct display wrong tag message.
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    unlockText.setText(getResources().getString(R.string.lockscreen_message_nfc_wrong));
                                }
                            });

                            vibrator.vibrate(250);

                            // After 1.5sec replace wrong tag message with previous message.
                            unlockText.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (!pinLocked)
                                        unlockText.setText("");
                                    else
                                        unlockText.setText(getResources().getString(R.string.lockscreen_message_pin_locked));
                                }
                            }, 1500);
                        }
                    }
                }, NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS | NfcAdapter.FLAG_READER_NFC_A |
                        NfcAdapter.FLAG_READER_NFC_B | NfcAdapter.FLAG_READER_NFC_BARCODE |
                        NfcAdapter.FLAG_READER_NFC_F | NfcAdapter.FLAG_READER_NFC_V |
                        NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
            }
        }

        // If NFC isn't active.
        else {
            // If PIN is locked display 30sec timeout message.
            if (pinLocked)
                unlockText.setText(getResources().getString(R.string.lockscreen_message_pin_locked_nfc_off));
            // Else display NFC error message.
            else
                unlockText.setText(getResources().getString(R.string.lockscreen_message_nfc_off));
        }
    }

    /**
     * NFC tag discovory for Android versions lower than 4.4.
     *
     * @param intent NFC discovery.
     */
    @Override
    protected void onNewIntent(Intent intent) {

        String action = intent.getAction();

        // If NFC tag detected.
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            // Get tag id and convert to readable string
            byte[] tagID = tag.getId();
            String tagDiscovered = bytesToHex(tagID);

            if (!tagDiscovered.equals("")) {

                // Loop through added NFC tags.
                for (int i = 0; i < tags.length(); i++) {

                    // When discovered tag ID is equal to one of the stored tags.
                    try {
                        if (tagDiscovered.equals(tags.getJSONObject(i).getString("tagID"))) {

                            // Reset tagDiscovered string, set pinLocked false and store
                            // Remove handle callback and messages
                            // Disable home launcher activity component and finish

                            // Reset tagDiscovered, set pinLocked to false and store.
                            tagDiscovered = "";
                            pinLocked = false;
                            settings.put("pinLocked", false);
                            writeToJSON();

                            // Remove 30 sec delay if active.
                            delayHandler.removeCallbacksAndMessages(null);

                            // Disable as home activity so home button doesn't launch the lock screen, but don't kill app.
                            packageManager.setComponentEnabledSetting(homeLauncher, componentDisabled, PackageManager.DONT_KILL_APP);

                            // Close lock screen.
                            finish();
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                // After 1.5sec replace wrong tag message with previous message.
                unlockText.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!pinLocked)
                            unlockText.setText("");
                        else
                            unlockText.setText(getResources().getString(R.string.lockscreen_message_pin_locked));
                    }
                }, 1500);

                vibrator.vibrate(250);

                // If tag ID is not correct display wrong tag message.
                unlockText.setText(getResources().getString(R.string.lockscreen_message_nfc_wrong));
            }
        }
    }

    // Bytes to hex string method

    /**
     * Convert an NFC tag ID from bytes to hex.
     *
     * @param bytes NFC tag ID.
     *
     * @return NFC tag ID as hex.
     */
    private static String bytesToHex(byte[] bytes) {

        char[] hexChars = new char[bytes.length * 2];

        for (int i = 0; i < bytes.length; i++) {
            int x = bytes[i] & 0xFF;
            hexChars[i * 2] = hexArray[x >>> 4];
            hexChars[i * 2 + 1] = hexArray[x & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * If lock screen is paused, by pressing the power button, disable NFC discovery.
     */
    @Override
    protected void onPause() {

        super.onPause();

        // If torch is on, turn off.
        if (torchAvailable) {
            if (isFlashOn) {
                torchOn(false);
                this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }

        // Disable NFC discovery for Android versions lower than 4.4.
        if (Build.VERSION.SDK_INT < 19) {
            if (nfcAdapter != null)
                nfcAdapter.disableForegroundDispatch(this);
        }
        // Disable NFC discovery for newer versions of Android.
        else if (Build.VERSION.SDK_INT >= 19) {
            if (nfcAdapter != null)
                nfcAdapter.disableReaderMode(this);
        }
    }

    /**
     * Close lock screen and clean up when password entered successfully.
     */
    @Override
    protected void onDestroy() {

        super.onDestroy();

        // Disable as home activity so home button doesn't launch the lock screen, but don't kill app.
        if (packageManager != null) {
            packageManager.setComponentEnabledSetting(homeLauncher, componentDisabled, PackageManager.DONT_KILL_APP);
        }

        // Unregister background receivers .
        try {
            unregisterReceiver(mChangeReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        // Turn torch off.
        if (torchAvailable) {
            if (isFlashOn) {
                torchOn(false);
                this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }

        // Release camera.
        releaseCamera();

        // Set phone state to idle.
        isPhoneCalling = false;

        // Remove 30 sec delay if active.
        delayHandler.removeCallbacksAndMessages(null);

        try {
            settings.put("pinLocked", false);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        writeToJSON();

        // If phone button pressed, start default phone app.
        if (openPhone) {

            openPhone = false;

            final Intent phoneIntent = new Intent(Intent.ACTION_DIAL);

            try {
                startActivity(phoneIntent);
            } catch (ActivityNotFoundException anfe) {
                anfe.printStackTrace();
            }
        }

        // If camera button pressed, start default camera app.
        if (openCamera) {

            openCamera = false;

            final Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            try {
                startActivity(cameraIntent);
            } catch (ActivityNotFoundException anfe) {
                anfe.printStackTrace();
            }
        }
    }


    /**
     * Read from JSON file.
     */
    private void readFromJSON() {

        // Read root object.
        try {
            BufferedReader bReader = new BufferedReader(new InputStreamReader(openFileInput("settings.json")));
            root = new JSONObject(bReader.readLine());
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        // Read settings object from root object.
        try {
            settings = root.getJSONObject("settings");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Read individual settings from settings object.
        try {
            pin = settings.getString("pin");
            pinLocked = settings.getBoolean("pinLocked");
            blur = settings.getInt("blur");
            tags = settings.getJSONArray("tags");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write to JSON file.
     */
    private void writeToJSON() {

        // Write JSON root object.
        try {
            BufferedWriter bWriter = new BufferedWriter(new OutputStreamWriter
                    (openFileOutput("settings.json", Context.MODE_PRIVATE)));
            bWriter.write(root.toString());
            bWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * If any prompt other than an incoming call takes
     * focus on the screen, dismiss it.
     *
     * @param hasFocus If lock screen has focus.
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        super.onWindowFocusChanged(hasFocus);

        // If focus is lost not due to a phone call, dismiss it and move the lock screen to front.
        if (!hasFocus && !isPhoneCalling) {
            activityManager.moveTaskToFront(taskID, 0);
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);
        }
    }

    /**
     * If the user does anything to send the lock screen to the background, move it to the front.
     */
    @Override
    protected void onUserLeaveHint() {

        super.onUserLeaveHint();
        activityManager.moveTaskToFront(taskID, 0);
    }

    /**
     * If touched outside lock screen bring lock screen back to front.
     *
     * @param v Lock screen view.
     * @param event Touch event.
     *
     * @return true.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            activityManager.moveTaskToFront(taskID, 0);
            return true;
        }
        return true;
    }

    /**
     * If touched outside lock screen bring lock screen back to front.
     *
     * @param event Touch event.
     *
     * @return Touch event.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            activityManager.moveTaskToFront(taskID, 0);
            return gestureDetector.onTouchEvent(event);
        }
        return gestureDetector.onTouchEvent(event);
    }

    /**
     * Do nothing if back button is pressed.
     */
    @Override
    public void onBackPressed() {

    }

    /**
     * If home key pressed move lock screen to front.
     *
     * @param keyCode Key pressed.
     * @param event Pressed key Event.
     *
     * @return True if lock screen moved to front.
     */
    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_HOME) {
            activityManager.moveTaskToFront(taskID, 0);
            return true;
        }
        return false;
    }

    /**
     * If home key pressed move lock screen to front.
     *
     * @param event Pressed key Event.
     *
     * @return True if lock screen moved to front.
     */
    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {

        if (event.getKeyCode() == KeyEvent.KEYCODE_HOME) {
            activityManager.moveTaskToFront(taskID, 0);
            return true;
        }
        return false;
    }

    // Empty method required for GestureListener.
    public boolean onDown(MotionEvent e) {
        return false;
    }

    // Empty method required for GestureListener.
    public void onShowPress(MotionEvent e) {}

    // Empty method required for GestureListener.
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    // Empty method required for GestureListener.
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    // Empty method required for GestureListener.
    public void onLongPress(MotionEvent e) {}
}
