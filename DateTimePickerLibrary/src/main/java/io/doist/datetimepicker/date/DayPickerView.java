package io.doist.datetimepicker.date;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import io.doist.datetimepicker.util.MathUtils;

public class DayPickerView extends RecyclerView {
    private static final String TAG = "DayPickerView";

    private final SimpleMonthAdapter mAdapter = new SimpleMonthAdapter(getContext());

    private SimpleDateFormat mYearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());

    // highlighted time
    private Calendar mSelectedDay = Calendar.getInstance();
    private Calendar mTempDay = Calendar.getInstance();
    private Calendar mMinDate = Calendar.getInstance();
    private Calendar mMaxDate = Calendar.getInstance();

    private Calendar mTempCalendar;

    private SimpleMonthAdapter.OnDaySelectedListener mOnDaySelectedListener;

    private LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

    public DayPickerView(Context context) {
        super(context);
        init();
    }

    public DayPickerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DayPickerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    void init() {
        setLayoutManager(manager);
        mAdapter.setOnDaySelectedListener(mProxyOnDaySelectedListener);
        setAdapter(mAdapter);
        setHasFixedSize(true);
        SnapHelper helper = new PagerSnapHelper();
        helper.attachToRecyclerView(this);
    }

    /**
     * Sets the currently selected date to the specified timestamp. Jumps
     * immediately to the new date. To animate to the new date, use
     * {@link #setDate(long, boolean, boolean)}.
     */
    public void setDate(long timeInMillis) {
        setDate(timeInMillis, false, true);
    }

    public void setDate(long timeInMillis, boolean animate, boolean forceScroll) {
        goTo(timeInMillis, animate, true, forceScroll);
    }

    public long getDate() {
        return mSelectedDay.getTimeInMillis();
    }

    public void setFirstDayOfWeek(int firstDayOfWeek) {
        mAdapter.setFirstDayOfWeek(firstDayOfWeek);
    }

    public int getFirstDayOfWeek() {
        return mAdapter.getFirstDayOfWeek();
    }

    public void setMinDate(long timeInMillis) {
        mMinDate.setTimeInMillis(timeInMillis);
        onRangeChanged();
    }

    public long getMinDate() {
        return mMinDate.getTimeInMillis();
    }

    public void setMaxDate(long timeInMillis) {
        mMaxDate.setTimeInMillis(timeInMillis);
        onRangeChanged();
    }

    public long getMaxDate() {
        return mMaxDate.getTimeInMillis();
    }

    public void setAnchorDate(Calendar anchorDate) {
        mAdapter.setAnchor(anchorDate);
    }

    /**
     * Handles changes to date range.
     */
    public void onRangeChanged() {
        mAdapter.setRange(mMinDate, mMaxDate);

        // Changing the min/max date changes the selection position since we
        // don't really have stable IDs. Jumps immediately to the new position.
        goTo(mSelectedDay.getTimeInMillis(), false, false, true);
    }

    /**
     * Sets the listener to call when the user selects a day.
     *
     * @param listener The listener to call.
     */
    public void setOnDaySelectedListener(SimpleMonthAdapter.OnDaySelectedListener listener) {
        mOnDaySelectedListener = listener;
    }

    /*
     * Sets all the required fields for the list view. Override this method to
     * set a different list view behavior.
     */
    private void setUpListView() {
        setVerticalScrollBarEnabled(false);
//        addOnScrollListener(this);
        setFadingEdgeLength(0);
    }

    private int getDiffMonths(Calendar start, Calendar end) {
        final int diffYears = end.get(Calendar.YEAR) - start.get(Calendar.YEAR);
        final int diffMonths = end.get(Calendar.MONTH) - start.get(Calendar.MONTH) + 12 * diffYears;
        return diffMonths;
    }

    private int getPositionFromDay(long timeInMillis) {
        final int diffMonthMax = getDiffMonths(mMinDate, mMaxDate);
        final int diffMonth = getDiffMonths(mMinDate, getTempCalendarForTime(timeInMillis));
        return MathUtils.constrain(diffMonth, 0, diffMonthMax);
    }

    private Calendar getTempCalendarForTime(long timeInMillis) {
        if (mTempCalendar == null) {
            mTempCalendar = Calendar.getInstance();
        }
        mTempCalendar.setTimeInMillis(timeInMillis);
        return mTempCalendar;
    }

    /**
     * This moves to the specified time in the view. If the time is not already
     * in range it will move the list so that the first of the month containing
     * the time is at the top of the view. If the new time is already in view
     * the list will not be scrolled unless forceScroll is true. This time may
     * optionally be highlighted as selected as well.
     *
     * @param day         The day to move to
     * @param animate     Whether to scroll to the given time or just redraw at the
     *                    new location
     * @param setSelected Whether to set the given time as selected
     * @param forceScroll Whether to recenter even if the time is already
     *                    visible
     */
    private void goTo(long day, boolean animate, boolean setSelected, boolean forceScroll) {

        // Set the selected day
        if (setSelected) {
            mSelectedDay.setTimeInMillis(day);
        }

        mTempDay.setTimeInMillis(day);
        final int position = getPositionFromDay(day);

        if (setSelected) {
            mAdapter.setSelectedDay(mSelectedDay);
        }
        setSelection(position);
    }


    void setCalendarTextColor(ColorStateList colors) {
        mAdapter.setCalendarTextColor(colors);
    }

    void setCalendarTextAppearance(int resId) {
        mAdapter.setCalendarTextAppearance(resId);
    }

//    protected class ScrollStateRunnable implements Runnable {
//        private int mNewState;
//        private View mParent;
//
//        ScrollStateRunnable(View view) {
//            mParent = view;
//        }
//
//        /**
//         * Sets up the runnable with a short delay in case the scroll state
//         * immediately changes again.
//         *
//         * @param view        The list view that changed state
//         * @param scrollState The new state it changed to
//         */
//        public void doScrollStateChange(AbsListView view, int scrollState) {
//            mParent.removeCallbacks(this);
//            mNewState = scrollState;
//            mParent.postDelayed(this, SCROLL_CHANGE_DELAY);
//        }
//
//        @Override
//        public void run() {
//            mCurrentScrollState = mNewState;
//            if (Log.isLoggable(TAG, Log.DEBUG)) {
//                Log.d(TAG,
//                        "new scroll state: " + mNewState + " old state: " + mPreviousScrollState);
//            }
//            // Fix the position after a scroll or a fling ends
//            if (mNewState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
//                    && mPreviousScrollState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE
//                    && mPreviousScrollState != AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
//                mPreviousScrollState = mNewState;
//                int i = 0;
//                View child = getChildAt(i);
//                while (child != null && child.getBottom() <= 0) {
//                    child = getChildAt(++i);
//                }
//                if (child == null) {
//                    // The view is no longer visible, just return
//                    return;
//                }
//                int firstPosition = getFirstVisiblePosition();
//                int lastPosition = getLastVisiblePosition();
//                boolean scroll = firstPosition != 0 && lastPosition != getCount() - 1;
//                final int top = child.getTop();
//                final int bottom = child.getBottom();
//                final int midpoint = getHeight() / 2;
//                if (scroll && top < LIST_TOP_OFFSET) {
//                    if (bottom > midpoint) {
//                        smoothScrollBy(top, GOTO_SCROLL_DURATION);
//                    } else {
//                        smoothScrollBy(bottom, GOTO_SCROLL_DURATION);
//                    }
//                }
//            } else {
//                mPreviousScrollState = mNewState;
//            }
//        }
//    }

    /**
     * Attempts to return the date that has accessibility focus.
     *
     * @return The date that has accessibility focus, or {@code null} if no date
     * has focus.
     */
    private Calendar findAccessibilityFocus() {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (child instanceof SimpleMonthView) {
                final Calendar focus = ((SimpleMonthView) child).getAccessibilityFocus();
                if (focus != null) {
                    return focus;
                }
            }
        }

        return null;
    }

    /**
     * Attempts to restore accessibility focus to a given date. No-op if
     * {@code day} is {@code null}.
     *
     * @param day The date that should receive accessibility focus
     * @return {@code true} if focus was restored
     */
    private boolean restoreAccessibilityFocus(Calendar day) {
        if (day == null) {
            return false;
        }

        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (child instanceof SimpleMonthView) {
                if (((SimpleMonthView) child).restoreAccessibilityFocus(day)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        mYearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
    }

    @Override
    public void onInitializeAccessibilityEvent(@NonNull AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setItemCount(-1);
    }

    private String getMonthAndYearString(Calendar day) {
        final StringBuilder sbuf = new StringBuilder();
        sbuf.append(day.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()));
        sbuf.append(" ");
        sbuf.append(mYearFormat.format(day.getTime()));
        return sbuf.toString();
    }

    /**
     * Necessary for accessibility, to ensure we support "scrolling" forward and backward
     * in the month list.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);
            info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);
        } else {
            info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
        }
    }

    /**
     * When scroll forward/backward events are received, announce the newly scrolled-to month.
     */
    @Override
    public boolean performAccessibilityAction(int action, Bundle arguments) {
        if (action != AccessibilityNodeInfo.ACTION_SCROLL_FORWARD &&
                action != AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) {
            return super.performAccessibilityAction(action, arguments);
        }

        // Figure out what month is showing.
        final int firstVisiblePosition = manager.findFirstVisibleItemPosition();
        final int month = firstVisiblePosition % 12;
        final int year = firstVisiblePosition / 12 + mMinDate.get(Calendar.YEAR);
        final Calendar day = Calendar.getInstance();
        day.set(year, month, 1);

        // Scroll either forward or backward one month.
        if (action == AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) {
            day.add(Calendar.MONTH, 1);
            if (day.get(Calendar.MONTH) == 12) {
                day.set(Calendar.MONTH, 0);
                day.add(Calendar.YEAR, 1);
            }
        } else if (action == AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) {
            View firstVisibleView = getChildAt(0);
            // If the view is fully visible, jump one month back. Otherwise, we'll just jump
            // to the first day of first visible month.
            if (firstVisibleView != null && firstVisibleView.getTop() >= -1) {
                // There's an off-by-one somewhere, so the top of the first visible item will
                // actually be -1 when it's at the exact top.
                day.add(Calendar.MONTH, -1);
                if (day.get(Calendar.MONTH) == -1) {
                    day.set(Calendar.MONTH, 11);
                    day.add(Calendar.YEAR, -1);
                }
            }
        }

        // Go to that month.
        announceForAccessibility(getMonthAndYearString(day));
        goTo(day.getTimeInMillis(), true, false, true);
        return true;
    }

    public int getMostVisiblePosition() {
        return manager.findFirstVisibleItemPosition();
    }

    public void setSelection(int listPosition) {
        scrollToPosition(listPosition);
    }

    private final SimpleMonthAdapter.OnDaySelectedListener mProxyOnDaySelectedListener = new SimpleMonthAdapter.OnDaySelectedListener() {
        @Override
        public void onDaySelected(SimpleMonthAdapter view, Calendar day) {
            if (mOnDaySelectedListener != null) {
                mOnDaySelectedListener.onDaySelected((SimpleMonthAdapter) getAdapter(), day);
            }
        }
    };

    public void nextMonth() {
        scrollToPosition(manager.findFirstVisibleItemPosition() + 1);
    }

    public void prevMonth() {
        scrollToPosition(manager.findFirstVisibleItemPosition() - 1);
    }


}
