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
import android.content.res.Resources;
import android.support.annotation.ColorInt;
import android.support.annotation.Dimension;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.doist.datetimepicker.R;

/**
 * Displays a selectable list of years.
 */
class YearPickerView extends RecyclerView implements OnDateChangedListener, OnYearSelectedListener {
    private final Calendar mMinDate = Calendar.getInstance();
    private final Calendar mMaxDate = Calendar.getInstance();

    private YearAdapter mAdapter;

    private DatePickerController mController;

    private int mYearSelectedColor;
    private LinearLayoutManager manager = new LinearLayoutManager(getContext());

    public YearPickerView(Context context) {
        this(context, null);
    }

    public YearPickerView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.listViewStyle);
    }

    public YearPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final LayoutParams frame = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        setLayoutParams(frame);

        final Resources res = context.getResources();
        int mChildSize = res.getDimensionPixelOffset(R.dimen.datepicker_year_label_height);

        setVerticalFadingEdgeEnabled(true);
        setFadingEdgeLength(mChildSize / 3);

        setPadding(0, 0, 0, 0);
        Calendar now = Calendar.getInstance();
        int defaultYearRange = 100;
        mMinDate.set(Calendar.YEAR, now.get(Calendar.YEAR) - defaultYearRange);
        mMaxDate.set(Calendar.YEAR, now.get(Calendar.YEAR) + defaultYearRange);
    }

    public void setMinDate(long min) {
        mMinDate.setTimeInMillis(min);
        mAdapter = new YearAdapter(getContext(), updateToAdapterData(), Calendar.getInstance().get(Calendar.YEAR), this);
        setAdapter(mAdapter);
    }

    public void setMaxDate(long max) {
        mMaxDate.setTimeInMillis(max);
        mAdapter = new YearAdapter(getContext(), updateToAdapterData(), Calendar.getInstance().get(Calendar.YEAR), this);
        setAdapter(mAdapter);
    }

    public void init(DatePickerController controller) {
        mController = controller;
        mController.registerOnDateChangedListener(this);
        setLayoutManager(manager);
        mAdapter = new YearAdapter(getContext(), updateToAdapterData(), Calendar.getInstance().get(Calendar.YEAR), this);
        setAdapter(mAdapter);
    }

    public void setYearSelectedCircleColor(int color) {
        if (color != mYearSelectedColor) {
            mYearSelectedColor = color;
        }
        requestLayout();
    }

    public int getYearSelectedCircleColor()  {
        return mYearSelectedColor;
    }

    private List<Integer> updateToAdapterData() {

        final int maxYear = mMaxDate.get(Calendar.YEAR);
        final int minYear = mMinDate.get(Calendar.YEAR);
        List<Integer> years = new ArrayList<>(maxYear - minYear);
        for (int year = minYear; year <= maxYear; year++) {
            years.add(year);
        }

        return years;
    }


    public int getFirstPositionOffset() {
        final View firstChild = getChildAt(0);
        if (firstChild == null) {
            return 0;
        }
        return firstChild.getTop();
    }

    @Override
    public void onDateChanged() {
        int offset = 2; //((manager.findLastVisibleItemPosition() - manager.findFirstVisibleItemPosition()) / 2 );
        int firstCompletlyVisiblePosition = manager.findFirstCompletelyVisibleItemPosition();
            if (mAdapter.getSelectedPosition() < firstCompletlyVisiblePosition + offset) {
                scrollToPosition(firstCompletlyVisiblePosition - Math.abs(mAdapter.getSelectedPosition() - firstCompletlyVisiblePosition - offset));
            } else {
                scrollToPosition(manager.findLastCompletelyVisibleItemPosition() + (mAdapter.getSelectedPosition() - firstCompletlyVisiblePosition - offset));
            }
    }

    @Override
    public void onInitializeAccessibilityEvent(@NonNull AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);

        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            event.setFromIndex(0);
            event.setToIndex(0);
        }
    }

    @Override
    public void onYearSelected(Integer year) {
        mController.tryVibrate();
        mController.onYearSelected(year);
        onDateChanged();
    }

    public void setSelectedYear(int year) {
        mAdapter.setSelectedYear(year);
    }

    public int getFirstVisiblePosition() {
        return manager.findFirstVisibleItemPosition();
    }

    private class YearAdapter extends RecyclerView.Adapter<YearViewHolder> {
        private List<Integer> years;
        private Context context;
        private int selectedPosition;
        private @ColorInt
        int selectedColor;
        private OnYearSelectedListener listener;
        int lastSelectedPosition;

        YearAdapter(Context context, List<Integer> years, int selectedYear, OnYearSelectedListener listener) {
            this.years = years;
            this.context = context;
            selectedPosition = years.indexOf(selectedYear);
            lastSelectedPosition = selectedPosition;
            selectedColor = mYearSelectedColor;
            this.listener = listener;
        }

        public void setSelectedYear(int year) {
            selectedPosition = years.indexOf(year);
            updateSelection(selectedPosition);
        }

        @NonNull
        @Override
        public YearViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return new YearViewHolder(inflater.inflate(R.layout.row_year_picker, parent, false).findViewById(R.id.rowContainer));
        }

        @Override
        public void onBindViewHolder(@NonNull final YearViewHolder holder, final int position) {
            final int year = years.get(position);
            holder.setText(String.valueOf(year));
            if (position == selectedPosition) {
                holder.setTextSize(24);
                holder.setTextColor(selectedColor);
            } else {
                holder.setTextSize(16);
                holder.setTextColor(ContextCompat.getColor(context, android.R.color.black));
            }
            holder.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateSelection(holder.getAdapterPosition());
                    listener.onYearSelected(years.get(holder.getAdapterPosition()));
                }
            });
        }

        private void updateSelection(int position) {
            selectedPosition = position;
            notifyItemChanged(lastSelectedPosition);
            lastSelectedPosition = selectedPosition;
            notifyItemChanged(lastSelectedPosition);
        }

        @Override
        public int getItemCount() {
            return years.size();
        }

        int getSelectedPosition() {
            return selectedPosition;
        }
    }


    private class YearViewHolder extends ViewHolder {
        private TextView year;

        YearViewHolder(View itemView) {
            super(itemView);
            year = itemView.findViewById(R.id.year);
        }

        void setText(String text) {
            year.setText(text);
        }

        void setTextSize(float sizeSp) {
            year.setTextSize(Dimension.SP, sizeSp);
        }

        void setTextColor(@ColorInt int color) {
            year.setTextColor(color);
        }

        void setOnClickListener(OnClickListener listener) {
            year.setOnClickListener(listener);
        }
    }
}