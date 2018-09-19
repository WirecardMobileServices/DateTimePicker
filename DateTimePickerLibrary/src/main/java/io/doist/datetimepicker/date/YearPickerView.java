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
import android.view.Gravity;
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
    private final int mChildSize;

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
        mChildSize = res.getDimensionPixelOffset(R.dimen.datepicker_year_label_height);

        setVerticalFadingEdgeEnabled(true);
        setFadingEdgeLength(mChildSize / 3);

        final int paddingTop = res.getDimensionPixelSize(R.dimen.datepicker_year_picker_padding_top);
        setPadding(0, paddingTop, 0, 0);
        Calendar now = Calendar.getInstance();
        mMinDate.set(Calendar.YEAR, now.get(Calendar.YEAR) - 100);
        mMaxDate.set(Calendar.YEAR, now.get(Calendar.YEAR) + 100);
    }

    public void setRange(Calendar min, Calendar max) {
        mMinDate.setTimeInMillis(min.getTimeInMillis());
        mMaxDate.setTimeInMillis(max.getTimeInMillis());
    }

    public void init(DatePickerController controller) {
        mController = controller;
        mController.registerOnDateChangedListener(this);
        setLayoutManager(manager);
        mAdapter = new YearAdapter(getContext(), updateToAdapterData(), Calendar.getInstance().get(Calendar.YEAR), this);
        setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        onDateChanged();
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
        scrollToPosition((manager.findLastVisibleItemPosition() - manager.findFirstVisibleItemPosition()) / 2 - 1);
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


        //        int mItemTextAppearanceResId;

//        public YearAdapter(Context context, int resource) {
//            super(context, resource);
//        }

//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            TextViewWithCircularIndicator v = (TextViewWithCircularIndicator)
//                    super.getView(position, convertView, parent);
//            v.setTextAppearance(getContext(), mItemTextAppearanceResId);
//            v.requestLayout();
//            int year = getItem(position);
//            boolean selected = mController.getSelectedDay().get(Calendar.YEAR) == year;
//            v.setDrawIndicator(selected);
//            if (selected) {
//                v.setCircleColor(mYearSelectedColor);
//            }
//            return v;
//        }

//        public void setItemTextAppearance(int resId) {
//            mItemTextAppearanceResId = resId;
//        }

        @NonNull
        @Override
        public YearViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView textView = new TextView(context);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            return new YearViewHolder(textView);
        }

        @Override
        public void onBindViewHolder(@NonNull final YearViewHolder holder, final int position) {
            final int year = years.get(position);
            holder.setText(String.valueOf(year));
            if (position == selectedPosition) {
                holder.setTextSize(20);
                holder.setTextColor(ContextCompat.getColor(context, android.R.color.holo_blue_dark));
            } else {
                holder.setTextSize(24);
                holder.setTextColor(ContextCompat.getColor(context, android.R.color.black));
            }
            holder.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onYearSelected(years.get(holder.getAdapterPosition()));
                    notifyItemChanged(lastSelectedPosition);
                    lastSelectedPosition = holder.getAdapterPosition();
                    notifyItemChanged(lastSelectedPosition);
                }
            });
        }

        @Override
        public int getItemCount() {
            return years.size();
        }

    }


    private class YearViewHolder extends ViewHolder {
        public YearViewHolder(TextView itemView) {
            super(itemView);
        }

        void setText(String text) {
            ((TextView) itemView).setText(text);
        }

        void setTextSize(float sizeSp) {
            ((TextView) itemView).setTextSize(Dimension.SP, sizeSp);
        }

        void setTextColor(@ColorInt int color) {
            ((TextView) itemView).setTextColor(color);
        }

        void setOnClickListener(OnClickListener listener) {
            itemView.setOnClickListener(listener);
        }
    }
}