package io.doist.datetimepicker.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import io.doist.datetimepicker.R;
import io.doist.datetimepicker.time.OnTimeSetListener;
import io.doist.datetimepicker.time.TimePicker;

public class TimePickerDialogFragmentDelegate extends PickerDialogFragmentDelegate
        implements TimePicker.OnTimeChangedListener {
    private static final String KEY_HOUR_OF_DAY = "hour";
    private static final String KEY_MINUTE = "minute";
    private static final String KEY_IS_24_HOUR = "is24Hour";
    private static final String KEY_IS_ALL_DAY = "isAllDay";

    private TimePicker mTimePicker;

    private OnTimeSetListener mOnTimeSetListener;

    public static Bundle createArguments(int hourOfDay, int minute, boolean is24Hour, boolean isAllDay) {
        Bundle arguments = new Bundle();
        arguments.putInt(KEY_HOUR_OF_DAY, hourOfDay);
        arguments.putInt(KEY_MINUTE, minute);
        arguments.putBoolean(KEY_IS_24_HOUR, is24Hour);
        arguments.putBoolean(KEY_IS_ALL_DAY, isAllDay);
        return arguments;
    }

    public TimePickerDialogFragmentDelegate() {
        super(R.attr.timePickerDialogTheme);
    }

    @SuppressWarnings("InflateParams")
    @Override
    protected View onCreateDialogView(LayoutInflater inflater, Bundle savedInstanceState, Bundle arguments) {
        View view = inflater.inflate(R.layout.time_picker_dialog, null);
        mTimePicker = view.findViewById(R.id.timePicker);
        if (savedInstanceState == null) {
            int hourOfDay = arguments.getInt(KEY_HOUR_OF_DAY);
            int minute = arguments.getInt(KEY_MINUTE);
            boolean is24Hour = arguments.getBoolean(KEY_IS_24_HOUR);
            boolean isAllDay = arguments.getBoolean(KEY_IS_ALL_DAY);

            mTimePicker = view.findViewById(R.id.timePicker);
            mTimePicker.setCurrentHour(hourOfDay);
            mTimePicker.setCurrentMinute(minute);
            mTimePicker.setIs24Hour(is24Hour);
            mTimePicker.setIsAllDay(isAllDay);
        }
        mTimePicker.setOnTimeChangedListener(this);
        mTimePicker.setValidationCallback(new TimePicker.ValidationCallback() {
            @Override
            public void onValidationChanged(boolean valid) {
                final Button positive = getDialog().getButton(AlertDialog.BUTTON_POSITIVE);
                if (positive != null) {
                    positive.setEnabled(valid);
                }
            }
        });
        return view;
    }

    @Override
    protected AlertDialog.Builder onBindDialogBuilder(AlertDialog.Builder builder, View view) {
        return super.onBindDialogBuilder(builder, view)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mOnTimeSetListener != null) {
                                mOnTimeSetListener.onTimeSet(
                                        mTimePicker,
                                        mTimePicker.getCurrentHour(),
                                        mTimePicker.getCurrentMinute(),
                                        mTimePicker.isAllDay());
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null);
    }

    public void setOnTimeSetListener(OnTimeSetListener listener) {
        mOnTimeSetListener = listener;
    }

    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        // Do nothing.
    }

    public TimePicker getTimePicker() {
        return mTimePicker;
    }

    public void updateTime(int hourOfDay, int minuteOfHour) {
        mTimePicker.setCurrentHour(hourOfDay);
        mTimePicker.setCurrentMinute(minuteOfHour);
    }
}
