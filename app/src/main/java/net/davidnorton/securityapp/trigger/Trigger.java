package net.davidnorton.securityapp.trigger;

import java.util.Set;

/**
 * Used to transfer user trigger preferences between
 * activities and methods.
 *
 * @author David Norton
 */
public class Trigger {

	public enum listen_state {listen_off, listen_on, ignore}

	private String name;
	private String profileName;
	private int startHours;
	private int startMinutes;
	private int endHours;
	private int endMinutes;
	private listen_state headphones;
    private int batteryStartLevel;
    private int batteryEndLevel;
	private listen_state batteryCharging;
	private String geofence;
	private int priority;
	private Set<String> weekdays;

    /**
     * Sets default profile states, where every value is unchanged.
     *
     * @param name Trigger name.
     */
    public Trigger(String name) {
        this.name = name;
        this.profileName = null;
        this.startHours = -1;
        this.startMinutes = -1;
        this.endHours = -1;
        this.endMinutes = -1;
        this.headphones = listen_state.ignore;
        this.batteryStartLevel = -1;
        this.batteryEndLevel = -1;
        this.batteryCharging = listen_state.ignore;
        this.geofence = null;
        this.priority = 0;
    }

	public Set<String> getWeekdays() {
		return weekdays;
	}
	public void setWeekdays(Set<String> weekdays) {
		this.weekdays = weekdays;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public String getGeofence() {
		return geofence;
	}
	public void setGeofence(String geofence) {
		this.geofence = geofence;
	}
    public int getBatteryStartLevel() {
        return batteryStartLevel;
    }
    public void setBatteryStartLevel(int batteryStartLevel) {
        this.batteryStartLevel = batteryStartLevel;
    }
    public int getBatteryEndLevel() {
        return batteryEndLevel;
    }
    public void setBatteryEndLevel(int batteryEndLevel) {
        this.batteryEndLevel = batteryEndLevel;
    }
	public listen_state getBatteryState() {
		return batteryCharging;
	}
	public void setBatteryState(listen_state batteryState) {
		this.batteryCharging = batteryState;
	}
	public listen_state getHeadphones() {
		return headphones;
	}
	public void setHeadphones(listen_state headphones) {
		this.headphones = headphones;
	}
	public int getStartHours() {
		return startHours;
	}
	public void setStartHours(int hours) {
			this.startHours = hours;
	}
	public int getStartMinutes() {
		return startMinutes;
	}
	public void setStartMinutes(int minutes) {
			this.startMinutes = minutes;
	}
	public int getEndHours() {
		return endHours;
	}
	public void setEndHours(int hours) {
        this.endHours = hours;
	}
	public int getEndMinutes() {
		return endMinutes;
	}
	public void setEndMinutes(int minutes) {
        this.endMinutes = minutes;
	}
	public String getProfileName() {
		return profileName;
	}
	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
