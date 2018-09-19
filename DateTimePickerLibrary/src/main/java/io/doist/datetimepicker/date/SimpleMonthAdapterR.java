package io.doist.datetimepicker.date;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.AbsListView;

import java.util.Calendar;

import io.doist.datetimepicker.R;

public class SimpleMonthAdapterR extends RecyclerView.Adapter<SimpleMonthAdapterR.MonthViewHolder> {
    private final Calendar mMinDate = Calendar.getInstance();
    private final Calendar mMaxDate = Calendar.getInstance();
    private Calendar anchor = null;

    private final Context mContext;

    private Calendar mSelectedDay = Calendar.getInstance();
    private ColorStateList mCalendarTextColors = ColorStateList.valueOf(Color.BLACK);
    private OnDaySelectedListener mOnDaySelectedListener;

    private int mFirstDayOfWeek;

    public SimpleMonthAdapterR(Context mContext) {
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public MonthViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MonthViewHolder(new SimpleMonthView(mContext));
    }

    @Override
    public void onBindViewHolder(@NonNull MonthViewHolder holder, int position) {
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
        holder.reuse();

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

        holder.setMonthParams(selectedDay, month, year, mFirstDayOfWeek,
                enabledDayRangeStart, enabledDayRangeEnd);
        if (anchor != null) {
            if (isAnchorInMonth(year, month)) {
                holder.setAnchor(anchor.get(Calendar.DAY_OF_MONTH));
            }
            holder.markDays(getMonthMarkType(year, month));
        }
        holder.invalidate();
    }

    @Override
    public int getItemCount() {
        final int diffYear = mMaxDate.get(Calendar.YEAR) - mMinDate.get(Calendar.YEAR);
        final int diffMonth = mMaxDate.get(Calendar.MONTH) - mMinDate.get(Calendar.MONTH);
        return diffMonth + 12 * diffYear + 1;
    }

    public void setRange(Calendar min, Calendar max) {
        mMinDate.setTimeInMillis(min.getTimeInMillis());
        mMaxDate.setTimeInMillis(max.getTimeInMillis());

        notifyDataSetChanged();
    }

    public void setAnchor(Calendar anchor) {
        this.anchor = anchor;
    }

    public void setFirstDayOfWeek(int firstDayOfWeek) {
        mFirstDayOfWeek = firstDayOfWeek;

        notifyDataSetChanged();
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
    public long getItemId(int position) {
        return position;
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
                    mOnDaySelectedListener.onDaySelected(SimpleMonthAdapterR.this, day);
                }
            }
        }
    };

    public interface OnDaySelectedListener {
        void onDaySelected(SimpleMonthAdapterR adapter, Calendar day);
    }

    class MonthViewHolder extends RecyclerView.ViewHolder {

        MonthViewHolder(SimpleMonthView itemView) {
            super(itemView);
            // Set up the new view
            final AbsListView.LayoutParams params = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.MATCH_PARENT);
            itemView.setLayoutParams(params);
            itemView.setClickable(true);
            itemView.setOnDayClickListener(mOnDayClickListener);

            if (mCalendarTextColors != null) {
                itemView.setTextColor(mCalendarTextColors);
            }
        }

        void reuse() {
            ((SimpleMonthView) itemView).reuse();
        }

        void setMonthParams(int selectedDay, int month, int year, int mFirstDayOfWeek, int enabledDayRangeStart, int enabledDayRangeEnd) {
            ((SimpleMonthView) itemView).setMonthParams(selectedDay, month, year, mFirstDayOfWeek, enabledDayRangeStart, enabledDayRangeEnd);
        }

        void setAnchor(int anchor) {
            ((SimpleMonthView) itemView).setAnchor(anchor);
        }

        void markDays(SimpleMonthView.Mark monthMarkType) {
            ((SimpleMonthView) itemView).markDays(monthMarkType);
        }

        void invalidate() {
            itemView.invalidate();
        }
    }
}
