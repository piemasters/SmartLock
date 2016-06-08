package net.davidnorton.securityapp.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import net.davidnorton.securityapp.profile.Handler;
import net.davidnorton.securityapp.trigger.LocationTrigger;
import net.davidnorton.securityapp.trigger.SimpleGeofence;
import net.davidnorton.securityapp.trigger.SimpleGeofenceStore;
import net.davidnorton.securityapp.trigger.Trigger;
import net.davidnorton.securityapp.trigger.XmlParserTrigger;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Service that manages triggers.
 * 
 * @author David Norton
 * 
 */
@SuppressWarnings("RedundantIfStatement")
public class TriggerService extends Service {

    private final static String TAG = "TriggerService";

	private TriggerBroadcastReceiver triggerReceiver;
    private Context context;
	private int currentHours;
	private int currentMinutes;
	private String currentWeekday;
	private boolean headphones;
	private boolean batteryCharging;
	private int batteryLevel;
	private final List<Trigger> triggerList = new ArrayList<>();
	private final List<Trigger> triggerPriorityList = new ArrayList<>();
	private String[] geofences;

    /**
     * Sets initial values, initialises & registers the broadcast receiver and loads existing triggers.
     *
     * @param intent Intent.
     * @param flags Flags.
     * @param startId Start ID.
     * @return START_STICKY (1).
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "TriggerService started");

        // Set initial trigger values.
        setInitialTime();
        setInitialWeekday();
        setInitialHeadphones(context);

        // Create a broadcast receiver to handle changes.
        triggerReceiver = new TriggerBroadcastReceiver(this);

        // Register broadcast receivers for the intents.
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(triggerReceiver,filter);
        filter = new IntentFilter(Intent.ACTION_TIME_TICK);
        registerReceiver(triggerReceiver,filter);
        filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryIntent = registerReceiver(triggerReceiver, filter);
        setInitialBatteryState(batteryIntent);
        filter = new IntentFilter(Intent.ACTION_POWER_CONNECTED);
        registerReceiver(triggerReceiver, filter);
        filter = new IntentFilter(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(triggerReceiver, filter);
        filter = new IntentFilter("net.davidnorton.securityapp.trigger.refresh");
        registerReceiver(triggerReceiver, filter);
        filter = new IntentFilter("net.davidnorton.securityapp.trigger.location_change");
        registerReceiver(triggerReceiver, filter);
        filter = new IntentFilter("net.davidnorton.securityapp.trigger.clearGeofences");
        registerReceiver(triggerReceiver, filter);

        refreshTriggers();

        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    /**
     * Refreshes the list of triggers.
     */
    public void refreshTriggers() {

        triggerList.clear();

        String[] fileList = getFilesDir().list();
        XmlParserTrigger parser = new XmlParserTrigger(this);

        try {
            // For each active trigger.
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].contains("_trigger")) {
                    // Get name and add to list.
                    Trigger trigger = new Trigger(fileList[i].substring(0, fileList[i].length() - 12));
                    Log.i(TAG, "Trigger found: " + trigger.getName());
                    parser.initializeXmlParser(openFileInput(fileList[i]), trigger);
                    triggerList.add(trigger);
                } else {
                    Log.i(TAG, "Not a trigger file");
                }
            }
        } catch (NotFoundException | IOException | XmlPullParserException e) {
            e.printStackTrace();
        }

        registerExistingGeofences();
        Log.i(TAG, "triggerList: " + triggerList.size());
    }

    /**
     * Compares the triggers with the actual state.
     */
    private void compareTriggers() {

        triggerPriorityList.clear();
        Log.i(TAG, "compareTriggers called");

        for (Trigger trigger : triggerList) {
            Log.i(TAG, "Compare Trigger:\n" +
                    "-------------------------------------\n" +
                    trigger.getName() +"\n" +
                    "-------------------------------------");
            if(compareTime(trigger)){
                Log.i(TAG, "trigger matching time");
                if(compareWeekday(trigger)){
                    Log.i(TAG, "trigger matching weekday");
                    if(compareHeadphones(trigger)){
                        Log.i(TAG, "trigger matching headphones");
                        if(compareBatteryCharging(trigger)){
                            Log.i(TAG, "trigger matching battery state");
                            if(compareBatteryLevel(trigger)) {
                                Log.i(TAG, "trigger matching battery level");
                                if (compareGeofence(trigger)) {
                                    Log.i(TAG, "trigger matching geofence");
                                    Log.i(TAG, "adding trigger to triggerPriorityList: " + trigger.getName());
                                    triggerPriorityList.add(trigger);
                                    Log.i(TAG, "highestPriority add: " + trigger.getName());
                                } else {
                                    Log.i(TAG, trigger.getName() + " does not match geofence");
                                }
                            }
                            else {
                                    Log.i(TAG, trigger.getName() + " does not match battery level");
                                }
                        } else {
                            Log.i(TAG, trigger.getName() + " does not match battery state");
                        }
                    } else {
                        Log.i(TAG, trigger.getName() + " does not match headphones");
                    }
                } else {
                    Log.i(TAG, trigger.getName() + " does not match weekday");
                }
            } else {
                Log.i(TAG, trigger.getName() + " does not match time " + trigger.getStartHours() + " " + trigger.getEndHours());
            }
        }

        comparePriorities();
    }

    /**
     * Unregister trigger.
     */
    @Override
    public void onDestroy() {
        unregisterReceiver(triggerReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

	/**
	 * Sets status of headphones state on initialization and compares the triggers.
     *
     * @param context Context.
     */
	private void setInitialHeadphones(Context context){

        AudioManager audiomanager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        if(audiomanager.isWiredHeadsetOn()){
			headphones = true;
			Log.i(TAG, "Headphone value defined as: plugged");
		} else {
			headphones = false;
			Log.i(TAG, "Headphone value defined as: unplugged");
		}

		compareTriggers();
	}

    /**
     * Set headphone state.
     *
     * @param headphones Initial headphone state
     */
    public void setHeadphones(boolean headphones) {
        this.headphones = headphones;
        Log.i(TAG, "headphones changed to " + headphones);
        compareTriggers();
    }

    /**
     * Compares saved headphone state with current state received by the broadcast receiver.
     *
     * @param trigger Trigger comparing to.
     * @return true if match, false otherwise.
     */
    private boolean compareHeadphones(Trigger trigger){

        if(!trigger.getHeadphones().equals(Trigger.listen_state.ignore)){

            if(headphones && trigger.getHeadphones().equals(Trigger.listen_state.listen_on)){
                return true;
            } else if(!headphones && trigger.getHeadphones().equals(Trigger.listen_state.listen_off)){
                return true;
            } else{
                return false;
            }
        } else {
            return true;
        }
    }
	
	/**
	 * Sets the time on initialization and compares the triggers.
	 */
	private void setInitialTime() {

		int hour = Integer.parseInt(String.valueOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));
		int min = Integer.parseInt(String.valueOf(Calendar.getInstance().get(Calendar.MINUTE)));
		setTime(hour, min);
		
		compareTriggers();
	}

    /**
     * Sets the time.
     *
     * @param cHours Current hour.
     * @param cMinutes Current minutes.
     */
    public void setTime(int cHours, int cMinutes) {

        currentHours = cHours;
        currentMinutes = cMinutes;
        Log.i(TAG, "current time updated: " + currentHours + ":" + currentMinutes);

        compareTriggers();
    }

    /**
     * Compares time saved with current time received by the broadcast receiver.
     *
     * @param trigger Trigger comparing to.
     * @return true if match, false otherwise.
     */
    private boolean compareTime(Trigger trigger){

        Log.i(TAG, "compare time called!");

        // If no time range set.
        if (trigger.getStartHours() == -1 && trigger.getEndHours() == -1){
            Log.i(TAG, "time ignored");
            return true;
        }
        // If time end not set.
        else if (trigger.getEndHours() == -1){
            Log.i(TAG, "no end time set, only compared to certain time");

            // If the current time matches the time set in the trigger.
            if(currentHours == trigger.getStartHours() && currentMinutes == trigger.getStartMinutes()){
                Log.i(TAG, "trigger matches exact current time");
                return true;
            }
            else {
                return false;
            }
        }
        // If start and end hours are on the same day.
        else if((trigger.getStartHours() < trigger.getEndHours()) ||
                (trigger.getStartHours() == trigger.getEndHours() && trigger.getStartMinutes() < trigger.getEndMinutes())){

            Log.i(TAG, "time range on same day");

            // If the hours are between the trigger hours.
            if(currentHours > trigger.getStartHours() && currentHours < trigger.getEndHours()){
                return true;
            }
            // If the start hour are the same as the current hour.
            else if(currentHours == trigger.getStartHours() && currentMinutes >= trigger.getStartMinutes()){

                if(currentHours < trigger.getEndHours()){
                    return true;
                }
                else if(currentHours == trigger.getEndHours() && currentMinutes <= trigger.getEndMinutes()){
                    return true;
                }
                else {
                    return false;
                }
            }
            // If the end hour are the same as the current hour.
            else if(currentHours == trigger.getEndHours() && currentMinutes <= trigger.getEndMinutes()){

                if(currentHours > trigger.getStartHours()){
                    return true;
                }
                else if(currentHours == trigger.getStartHours() && currentMinutes > trigger.getStartMinutes()){
                    return true;
                }
                else {
                    return false;
                }
            }
            else{
                return false;
            }
        // If the end time is already on the next day.
        } else if (trigger.getStartHours() > trigger.getEndHours() ||
                (trigger.getStartHours() == trigger.getEndHours() && trigger.getStartMinutes() > trigger.getEndMinutes())){

            Log.i(TAG, "time range on other day");

            // If the time is after the start time or before the end time.
            if(currentHours > trigger.getStartHours() || currentHours < trigger.getEndHours()){
                return true;
            }
            // If the hour is the same as the start hour.
            else if(currentHours == trigger.getStartHours() && currentMinutes >= trigger.getStartMinutes()){
                return true;
            }
            // If the hour is the same as the end hour.
            else if(currentHours == trigger.getEndHours() && currentMinutes <= trigger.getEndMinutes()){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
	
	/**
	 * Sets the days on initialization and compares the triggers.
	 */
	private void setInitialWeekday() {

	    Calendar cal = Calendar.getInstance();
        String weekday = "";
	    int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

	    if (Calendar.MONDAY == dayOfWeek) {
	        weekday = "1";
	    } else if (Calendar.TUESDAY == dayOfWeek) {
	        weekday = "2";
	    } else if (Calendar.WEDNESDAY == dayOfWeek) {
	        weekday = "3";
	    } else if (Calendar.THURSDAY == dayOfWeek) {
	        weekday = "4";
	    } else if (Calendar.FRIDAY == dayOfWeek) {
	        weekday = "5";
	    } else if (Calendar.SATURDAY == dayOfWeek) {
	        weekday = "6";
	    } else if (Calendar.SUNDAY == dayOfWeek) {
	        weekday = "7";
	    }
	    
	    setWeekday(weekday);
	    
	    compareTriggers();
	}

    /**
     * Sets the day.
     *
     * @param currentDay Current day.
     */
    private void setWeekday(String currentDay) {
        currentWeekday = currentDay;
        Log.i(TAG, "current weekday updated: " + currentWeekday);

        compareTriggers();
    }

    /**
     * Compares saved day with current day received by the broadcast receiver.
     *
     * @param trigger Trigger comparing to.
     * @return true if match, false otherwise.
     */
    private boolean compareWeekday(Trigger trigger) {

        Log.i(TAG, "compare day called!");

        // If not set.
        if (trigger.getWeekdays() == null) {
            return true;
        }

        // If every or no days set.
        if (trigger.getWeekdays().size() == 7 || trigger.getWeekdays().size() == 0){
            Log.i(TAG, "every or no day is set");
            return true;
        }

        // If day is set.
        if (trigger.getWeekdays().contains(currentWeekday)){
            return true;
        }

        return false;
    }
	
	/**
	 * Sets the battery state on initialization and compares the triggers.
     *
     * @param intent Intent.
     */
    private void setInitialBatteryState(Intent intent){

        // Get battery charging state.
		int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		boolean batteryCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING);
		Log.i(TAG, "initial battery state defined as " + batteryCharging);

        // Get battery level value.
		int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		float batteryLevelF = level / (float)scale;
		batteryLevel = (int)(batteryLevelF * 100);
		Log.i(TAG, "initial battery level defined as " + batteryLevel);

		compareTriggers();
	}

    /**
     * Set battery charging state.
     *
     * @param batteryCharging Initial battery charging state.
     */
	public void setBatteryCharging(boolean batteryCharging) {

		this.batteryCharging = batteryCharging;
		Log.i(TAG, "batterystate changed to " + batteryCharging);

		compareTriggers();
	}

    /**
     * Set the battery level value.
     *
     * @param _batteryLevel Initial battery level value.
     */
	public void setBatteryLevel(int _batteryLevel) {

		this.batteryLevel = _batteryLevel;
		Log.i(TAG, "batterylevel changed to " + _batteryLevel);

		compareTriggers();
	}

    /**
     * Compares saved battery state with current battery state received by the broadcast receiver.
     *
     * @param trigger Trigger comparing to.
     * @return true if match, false otherwise.
     */
    private boolean compareBatteryCharging(Trigger trigger){

        // If state is set.
        if(!trigger.getBatteryState().equals(Trigger.listen_state.ignore)){

            if(trigger.getBatteryState().equals(Trigger.listen_state.listen_on) && batteryCharging){
                return true;
            } else if(trigger.getBatteryState().equals(Trigger.listen_state.listen_off) && !batteryCharging){
                return true;
            } else {
                return false;
            }
        } else{
            return true;
        }
    }

    /**
     * Compares saved battery level with current battery state received by the broadcast receiver.
     * If the trigger has only the battery start level defined, it needs to match the value exactly.
     *
     * @param trigger Trigger comparing to.
     * @return true if match, false otherwise.
     */
    private boolean compareBatteryLevel(Trigger trigger){

        if(trigger.getBatteryStartLevel() == -1 && trigger.getBatteryEndLevel() == -1){
            return true;
        } else if(trigger.getBatteryEndLevel() == -1 && trigger.getBatteryStartLevel() == batteryLevel){
            return true;
        } else if(trigger.getBatteryStartLevel() < batteryLevel && trigger.getBatteryEndLevel() > batteryLevel){
            return true;
        }
        return false;
    }

    /**
     * Registers all the geo-fences already stored in a trigger.
     */
    private void registerExistingGeofences() {

        SimpleGeofence geofence;
        SimpleGeofenceStore store = new SimpleGeofenceStore(getApplicationContext());
        LocationTrigger trigger = new LocationTrigger(getApplicationContext());

        // For each trigger.
        for(int i=0; i < triggerList.size(); i++) {
            // If a geo-fence is set.
            if(triggerList.get(i).getGeofence() != null){
                // Register geo-fence.
                geofence = store.getGeofence(triggerList.get(i).getGeofence());
                trigger.registerGeofence(geofence);
                Log.i(TAG, "Registered existing geofence: " + geofence.getId());
            }
        }
    }

    /**
     * Clears currently triggered geo-fences.
     */
    public void clearGeofences(){

        geofences = null;
        Log.i(TAG, "all geofences cleared!");
    }

    /**
     * Set geo-fences.
     *
     * @param geofence List of geo-fences.
     */
	public void setGeofences(String[] geofence) {
        Log.i(TAG, "GEO-FENCE SET!");
        this.geofences = geofence;
        compareTriggers();
	}

    /**
     * Compares saved geo-fence with current geo-fence received by the broadcast receiver.
     *
     * @param trigger Trigger comparing to.
     * @return true if match, false otherwise.
     */

    private boolean compareGeofence(Trigger trigger){

        // If no geo-fence set for the trigger.
        if(trigger.getGeofence() == null){
            return true;
        }

        // For each set geo-fence
        if(geofences != null){
            Log.i(TAG, "NUMBER OF GEO-FENCES FOUND: " + geofences.length);
            for (String geofence : geofences) {
                Log.i(TAG, "POTENTIAL GEO-FENCE MATCH!");
                // If inside geo-fence
                if (geofence.equals(trigger.getGeofence())) {
                    Log.i(TAG, "INSIDE THE GEO-FENCE!");
                    return true;
                }
            }
        }

        return false;
    }

	/**
	 * Compares the priorities of triggers.
	 */
	private void comparePriorities() {

		int highestPriority = -1;
		Trigger highestTrigger = null;
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		
		Log.i(TAG, "comparePriorities called");

        // For more than one trigger.
		if (triggerPriorityList.size() > 1) {

			Log.i(TAG, "triggerPriorityList: " + triggerPriorityList.size());

            // Get highest priority.
			for(Trigger trigger : triggerPriorityList) {
				
				if (trigger.getPriority() > highestPriority) {
					highestTrigger = trigger;
					highestPriority = trigger.getPriority();
				}
			}
			// If highest trigger's profile isn't the active profile, apply profile.
			if (highestTrigger != null &&
                    !highestTrigger.getProfileName().equals(pref.getString("active_profile", "Default"))) {
				Handler handler = new Handler(getApplicationContext());
				handler.applyProfile(highestTrigger.getProfileName());
				Log.i(TAG, "matching trigger found: " + highestTrigger.getName());
			}
        // If only one trigger and its profile isn't the active profile.
		} else if (triggerPriorityList.size() == 1 &&
                !triggerPriorityList.get(0).getProfileName().equals(pref.getString("active_profile", "Default"))) {
			Handler handler = new Handler(getApplicationContext());
			handler.applyProfile(triggerPriorityList.get(0).getProfileName());
			Log.i(TAG, "matching trigger found: " + triggerPriorityList.get(0).getName());
		}
	}
}
