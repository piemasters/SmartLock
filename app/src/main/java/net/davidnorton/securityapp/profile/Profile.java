package net.davidnorton.securityapp.profile;

/**
 * Used to transfer user profile preferences between
 * activities and methods.
 *
 * @author David Norton
 */
public class Profile {

    public enum state {disabled, enabled, unchanged}
    public enum mode {silent, vibrate, normal, unchanged}

    // Sets default profile states, where every value is unchanged.
    private String name;
    private state lockscreen = state.unchanged;
    private state wifi = state.unchanged;
    private state mobileData = state.unchanged;
    private state bluetooth = state.unchanged;
    private state screenBrightnessAutoMode = state.unchanged;
    private int screenTimeOut = -1;
    private mode ringerMode = mode.unchanged;

    public Profile(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public state getLockscreen() {
        return lockscreen;
    }
    public void setLockscreen(state lockscreen) {
        this.lockscreen = lockscreen;
    }
    public state getWifi() {
        return wifi;
    }
    public void setWifi(state wifi) {
        this.wifi = wifi;
    }
    public state getMobileData() {
        return mobileData;
    }
    public void setMobileData(state mobileData) {
        this.mobileData = mobileData;
    }
    public state getBluetooth() {
        return bluetooth;
    }
    public void setBluetooth(state bluetooth) {
        this.bluetooth = bluetooth;
    }
    public state getScreenBrightnessAutoMode() {
        return screenBrightnessAutoMode;
    }
    public void setScreenBrightnessAutoMode(state screenBrightnessAutoMode) {
        this.screenBrightnessAutoMode = screenBrightnessAutoMode;
    }
    public int getScreenTimeOut() {
        return screenTimeOut;
    }
    public void setScreenTimeOut(int screenTimeOut) {
        this.screenTimeOut = screenTimeOut;
    }
    public mode getRingerMode() {
        return ringerMode;
    }
    public void setRingerMode(mode ringerMode) {
        this.ringerMode = ringerMode;
    }
}
