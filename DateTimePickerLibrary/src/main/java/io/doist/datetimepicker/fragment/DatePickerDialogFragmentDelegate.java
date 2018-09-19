package io.doist.datetimepicker.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import java.util.Calendar;

import io.doist.datetimepicker.R;
import io.doist.datetimepicker.date.DatePicker;
import io.doist.datetimepicker.date.OnDateSetListener;

public class DatePickerDialogFragmentDelegate extends PickerDialogFragmentDelegate
        implements DatePicker.OnDateChangedListener {
    private static final String KEY_YEAR = "year";
    private static final String KEY_MONTH_OF_YEAR = "month";
    private static final String KEY_DAY_OF_MONTH = "day";
    private static final String ANCHOR_KEY_YEAR = "anchoryear";
    private static final String ANCHOR_KEY_MONTH_OF_YEAR = "anchormonth";
    private static final String ANCHOR_KEY_DAY_OF_MONTH = "anchorday";

    private DatePicker mDatePicker;

    private OnDateSetListener mOnDateSetListener;

    public static Bundle createArguments(int year, int monthOfYear, int dayOfMonth) {
        Bundle arguments = new Bundle();
        arguments.putInt(KEY_YEAR, year);
        arguments.putInt(KEY_MONTH_OF_YEAR, monthOfYear);
        arguments.putInt(KEY_DAY_OF_MONTH, dayOfMonth);
        return arguments;
    }

    public static Bundle createArguments(int year, int monthOfYear, int dayOfMonth, int anchorYear, int anchorMonthOfYear, int anchorDayOfMonth) {
        Bundle arguments = createArguments(year, monthOfYear, dayOfMonth);
        arguments.putInt(ANCHOR_KEY_YEAR, anchorYear);
        arguments.putInt(ANCHOR_KEY_MONTH_OF_YEAR, anchorMonthOfYear);
        arguments.putInt(ANCHOR_KEY_DAY_OF_MONTH, anchorDayOfMonth);
        return arguments;
    }

    public DatePickerDialogFragmentDelegate() {
        super(R.attr.datePickerDialogTheme);
    }

    @SuppressWarnings("InflateParams")
    @Override
    protected View onCreateDialogView(LayoutInflater inflater, Bundle savedInstanceState, Bundle arguments) {
        View view = inflater.inflate(R.layout.date_picker_dialog, null);
        mDatePicker = view.findViewById(R.id.datePicker);
        if (savedInstanceState == null) {
            int year = arguments.getInt(KEY_YEAR);
            int monthOfYear = arguments.getInt(KEY_MONTH_OF_YEAR);
            int dayOfMonth = arguments.getInt(KEY_DAY_OF_MONTH);
            mDatePicker.init(year, monthOfYear, dayOfMonth, this);
            if (arguments.containsKey(ANCHOR_KEY_YEAR) && arguments.containsKey(ANCHOR_KEY_MONTH_OF_YEAR) && arguments.containsKey(ANCHOR_KEY_DAY_OF_MONTH)) {
                Calendar anchor = Calendar.getInstance();
                anchor.set(arguments.getInt(ANCHOR_KEY_YEAR), arguments.getInt(ANCHOR_KEY_MONTH_OF_YEAR), arguments.getInt(ANCHOR_KEY_DAY_OF_MONTH));
                mDatePicker.setAnchorDate(anchor);
            }
        } else {
            mDatePicker.setOnDateChangedListener(this);
        }
        mDatePicker.setValidationCallback(new DatePicker.ValidationCallback() {
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
                    .setPositiveButton(R.string.done_label, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mOnDateSetListener != null) {
                                mOnDateSetListener.onDateSet(
                                        mDatePicker,
                                        mDatePicker.getYear(),
                                        mDatePicker.getMonth(),
                                        mDatePicker.getDayOfMonth());
                            }
                        }
                    });
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        mDatePicker.init(year, monthOfYear, dayOfMonth, this);
    }

    public void setOnDateSetListener(OnDateSetListener listener) {
        mOnDateSetListener = listener;
    }

    public DatePicker getDatePicker() {
        return mDatePicker;
    }

    public void updateDate(int year, int monthOfYear, int dayOfMonth) {
        mDatePicker.updateDate(year, monthOfYear, dayOfMonth);
    }
}
