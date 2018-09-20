/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.doist.datetimepicker.date;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

import java.util.Calendar;

import io.doist.datetimepicker.R;

/**
 * An adapter for a list of {@link SimpleMonthView} items.
 */
class SimpleMonthAdapter extends BaseAdapter {
    private final Calendar mMinDate = Calendar.getInstance();
    private final Calendar mMaxDate = Calendar.getInstance();
    private Calendar anchor = null;

    private final Context mContext;

    private Calendar mSelectedDay = Calendar.getInstance();
    private ColorStateList mCalendarTextColors = ColorStateList.valueOf(Color.BLACK);
    private OnDaySelectedListener mOnDaySelectedListener;

    private int mFirstDayOfWeek;

    public SimpleMonthAdapter(Context context) {
        mContext = context;
    }

    public void setRange(Calendar min, Calendar max) {
        mMinDate.setTimeInMillis(min.getTimeInMillis());
        mMaxDate.setTimeInMillis(max.getTimeInMillis());

        notifyDataSetInvalidated();
    }

    public void setAnchor(Calendar anchor) {
        this.anchor = anchor;
    }

    public void setFirstDayOfWeek(int firstDayOfWeek) {
        mFirstDayOfWeek = firstDayOfWeek;

        notifyDataSetInvalidated();
    }

    public int getFirstDayOfWeek() {
        return mFirstDayOfWeek;
    }

    /**
     * Updates the selected day and related parameters.
     *
     * @param day The day to highlight
     */
    public void setSelectedDay(Calendar day) {
        mSelectedDay = day;

        notifyDataSetChanged();
    }

    /**
     * Sets the listener to call when the user selects a day.
     *
     * @param listener The listener to call.
     */
    public void setOnDaySelectedListener(OnDaySelectedListener listener) {
        mOnDaySelectedListener = listener;
    }

    void setCalendarTextColor(ColorStateList colors) {
        mCalendarTextColors = colors;
    }

    /**
     * Sets the text color, size, style, hint color, and highlight color from
     * the specified TextAppearance resource. This is mostly copied from
     * {@link android.widget.TextView#setTextAppearance(Context, int)}.
     */
    void setCalendarTextAppearance(int resId) {
        final TypedArray a = mContext.obtainStyledAttributes(resId, R.styleable.TextAppearance);

        final ColorStateList textColor = a.getColorStateList(R.styleable.TextAppearance_android_textColor);
        if (textColor != null) {
            mCalendarTextColors = textColor;
        }

        // TODO: Support font size, etc.

        a.recycle();
    }

    @Override
    public int getCount() {
        final int diffYear = mMaxDate.get(Calendar.YEAR) - mMinDate.get(Calendar.YEAR);
        final int diffMonth = mMaxDate.get(Calendar.MONTH) - mMinDate.get(Calendar.MONTH);
        return diffMonth + 12 * diffYear + 1;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final SimpleMonthView v;
        if (convertView != null) {
            v = (SimpleMonthView) convertView;
        } else {
        v = new SimpleMonthView(mContext);

        // Set up the new view
        final AbsListView.LayoutParams params = new AbsListView.LayoutParams(
                AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.MATCH_PARENT);
        v.setLayoutParams(params);
        v.setClickable(true);
        v.setOnDayClickListener(mOnDayClickListener);

        if (mCalendarTextColors != null) {
            v.setTextColor(mCalendarTextColors);
        }
        }

        final int minMonth = mMinDate.get(Calendar.MONTH);
        final int minYear = mMinDate.get(Calendar.YEAR);
        final int currentMonth = position + minMonth;
        final int month = currentMonth % 12;
        final int year = currentMonth / 12 + minYear;
        final int selectedDay;
        if (isSelectedDayInMonth(year, month)) {
            selectedDay = mSelectedDay.get(Calendar.DAY_OF_MONTH);
        } else {
            selectedDay = -1;
        }

        // Invokes requestLayout() to ensure that the recycled view is set with the appropriate
        // height/number of weeks before being displayed.
        v.reuse();

        final int enabledDayRangeStart;
        if (minMonth == month && minYear == year) {
            enabledDayRangeStart = mMinDate.get(Calendar.DAY_OF_MONTH);
        } else {
            enabledDayRangeStart = 1;
        }

        final int enabledDayRangeEnd;
        if (mMaxDate.get(Calendar.MONTH) == month && mMaxDate.get(Calendar.YEAR) == year) {
            enabledDayRangeEnd = mMaxDate.get(Calendar.DAY_OF_MONTH);
        } else {
            enabledDayRangeEnd = 31;
        }

        v.setMonthParams(selectedDay, month, year, mFirstDayOfWeek,
                enabledDayRangeStart, enabledDayRangeEnd);
        if (anchor != null) {
            if (isAnchorInMonth(year, month)) {
                v.setAnchor(anchor.get(Calendar.DAY_OF_MONTH));
            }
            v.markDays(getMonthMarkType(year, month));
        }
        v.invalidate();

        return v;
    }

    private SimpleMonthView.Mark getMonthMarkType(int year, int month) {
        if (isMonthInAnchorRange(year, month)) {
            if (isAnchorInMonth(year, month) && isSelectedDayInMonth(year, month)) {
                return SimpleMonthView.Mark.BEWEEN;
            } else if (isAnchorInMonth(year, month)) {
                return anchor.after(mSelectedDay) ? SimpleMonthView.Mark.UNTIL : SimpleMonthView.Mark.AFTER;
            } else if (isSelectedDayInMonth(year, month)) {
                return mSelectedDay.after(anchor) ? SimpleMonthView.Mark.UNTIL : SimpleMonthView.Mark.AFTER;
            } else {
                return SimpleMonthView.Mark.ALL;
            }
        } else {
            return SimpleMonthView.Mark.NONE;
        }
    }

    private boolean isMonthInAnchorRange(int year, int month) {
        Calendar anchorMonthYear = Calendar.getInstance();
        anchorMonthYear.set(Calendar.YEAR, anchor.get(Calendar.YEAR));
        anchorMonthYear.set(Calendar.MONTH, anchor.get(Calendar.MONTH));
        Calendar selectedMonthYear = Calendar.getInstance();
        selectedMonthYear.set(Calendar.YEAR, mSelectedDay.get(Calendar.YEAR));
        selectedMonthYear.set(Calendar.MONTH, mSelectedDay.get(Calendar.MONTH));
        Calendar current = Calendar.getInstance();
        current.set(Calendar.YEAR, year);
        current.set(Calendar.MONTH, month);

        return (current.compareTo(anchorMonthYear) <= 0 && current.compareTo(selectedMonthYear) >= 0) || (current.compareTo(anchorMonthYear) >= 0 && current.compareTo(selectedMonthYear) <= 0);
//        return (year < anchor.get(Calendar.YEAR) && year >= mSelectedDay.get(Calendar.YEAR)) || (year > anchor.get(Calendar.YEAR) && year <= mSelectedDay.get(Calendar.YEAR))
//                || (year == anchor.get(Calendar.YEAR) && year == mSelectedDay.get(Calendar.YEAR) && month <= anchor.get(Calendar.MONTH) && month >= mSelectedDay.get(Calendar.MONTH))
//                || (year == anchor.get(Calendar.YEAR) && year == mSelectedDay.get(Calendar.YEAR) && month >= anchor.get(Calendar.MONTH) && month <= mSelectedDay.get(Calendar.MONTH));
    }

    private boolean isAnchorInMonth(int year, int month) {
        return anchor.get(Calendar.YEAR) == year && anchor.get(Calendar.MONTH) == month;
    }

    private boolean isSelectedDayInMonth(int year, int month) {
        return mSelectedDay.get(Calendar.YEAR) == year && mSelectedDay.get(Calendar.MONTH) == month;
    }

    private boolean isCalendarInRange(Calendar value) {
        return value.compareTo(mMinDate) >= 0 && value.compareTo(mMaxDate) <= 0;
    }

    private final SimpleMonthView.OnDayClickListener mOnDayClickListener = new SimpleMonthView.OnDayClickListener() {
        @Override
        public void onDayClick(SimpleMonthView view, Calendar day) {
            if (day != null && isCalendarInRange(day)) {
                setSelectedDay(day);

                if (mOnDaySelectedListener != null) {
                    mOnDaySelectedListener.onDaySelected(SimpleMonthAdapter.this, day);
                }
            }
        }
    };

    public interface OnDaySelectedListener {
        void onDaySelected(SimpleMonthAdapter view, Calendar day);
    }
}
