package net.davidnorton.securityapp.trigger;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TimePicker;

import net.davidnorton.securityapp.R;

/**
 * A time selection preference object.
 *
 * @author David Norton
 */
public class TimePickerPreference extends DialogPreference implements OnCheckedChangeListener {
	
	private int lastHour = 0;
	private int lastMinute = 0;
	private TimePicker picker = null;
	private CheckBox checkbox;

    public TimePickerPreference(Context context, AttributeSet attrs) {

        super(context, attrs);
        setDialogLayoutResource(R.layout.layout_triggers_timepicker);

        setPositiveButtonText(R.string.save);
        setNegativeButtonText(R.string.cancel);
    }

    /**
     * Create dialog.
     *
     * @return View.
     */
    @Override
    protected View onCreateDialogView() {

        View view = super.onCreateDialogView();
        String time = getPersistedString(getContext().getString(R.string.ignored));

        picker = (TimePicker) view.findViewById(R.id.timepicker);
        picker.setIs24HourView(true);
        checkbox = (CheckBox) view.findViewById(R.id.checkbox_timepicker);
        checkbox.setOnCheckedChangeListener(this);

        // Ensure time is in correct format.
        if (time.contains(":")) {
            checkbox.setChecked(true);
            picker.setEnabled(true);
            lastHour = getHour(time);
            lastMinute = getMinute(time);
        } else {
            checkbox.setChecked(false);
            picker.setEnabled(false);
        }

        return view;
    }

    /**
     * Gets the default value from array.
     *
     * @param a Array.
     * @param index Index.
     * @return Value at default index of array.
     */
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    /**
     * Sets the initial value.
     *
     * @param restoreValue If value has been set to be restored to default.
     * @param defaultValue Default value.
     */
    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

        String time;

        // Initialise the preferences if set to restore.
        if (restoreValue) {
            // By default tick box is unchecked so set time to ignored.
            if (defaultValue == null) {
                time = getPersistedString(getContext().getString(R.string.ignored));
                // If time has already been set, enable the preference screen.
            } else if (getPersistedString(getContext().getString(R.string.ignored)).contains(":")){
                time = getPersistedString(defaultValue.toString());
                checkbox.setChecked(true);
                picker.setEnabled(true);
                lastHour = getHour(time);
                lastMinute = getMinute(time);
                // If tick box is unchecked, disable the preference screen.
            } else {
                time = getPersistedString(defaultValue.toString());
                checkbox.setChecked(false);
                picker.setEnabled(false);
            }
        } else {
            time = defaultValue.toString();
        }
    }

    /**
     * Enable or disable the time preference based on
     * the tick box value.
     *
     * @param arg0 Arg.
     * @param isChecked If tick box has been checked.
     */
    @Override
    public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {

        // Enable or disable the preference screen depending on if the tick box is checked.
        if (isChecked) {
            picker.setEnabled(true);
        } else {
            picker.setEnabled(false);
        }
    }

    // Gets the hour by taking the value left of the :.
	public static int getHour(String time) {

        String[] pieces = time.split(":");

		return (Integer.parseInt(pieces[0]));
	}

    // Gets the minutes by taking the value right of the :.
	public static int getMinute(String time) {

        String[] pieces = time.split(":");

		return (Integer.parseInt(pieces[1]));
	}

    /**
     * Set hour and minutes.
     *
     * @param v View.
     */
	@Override
	protected void onBindDialogView(@NonNull View v) {

		super.onBindDialogView(v);

		picker.setCurrentHour(lastHour);
		picker.setCurrentMinute(lastMinute);
	}

    /**
     * Handle dialog being closed.
     *
     * @param positiveResult If time has been selected.
     */
	@Override
	protected void onDialogClosed(boolean positiveResult) {

		super.onDialogClosed(positiveResult);

		picker.clearFocus();

        // If checkbox is ticked save time.
		if (checkbox.isChecked() && positiveResult) {

			lastHour = picker.getCurrentHour();
			lastMinute = picker.getCurrentMinute();

			String time = String.format("%02d", lastHour) + ":" + String.format("%02d", lastMinute);

			if (callChangeListener(time)) {
				persistString(time);
			}
        // If checkbox isn't selected, set value to ignored.
		} else if (positiveResult) {

			String time = getContext().getString(R.string.ignored);

			if (callChangeListener(time)) {
				persistString(time);
			}
		}
	}
}