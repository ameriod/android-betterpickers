package com.codetroopers.betterpickers.calendardatepicker;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.codetroopers.betterpickers.HapticFeedbackController;
import com.codetroopers.betterpickers.R;
import com.codetroopers.betterpickers.Utils;
import com.nineoldandroids.animation.ObjectAnimator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;

public class CalendarDatePickerView extends FrameLayout implements View.OnClickListener,
        CalendarDatePickerControllerView {

    private static final int UNINITIALIZED = -1;
    private static final int MONTH_AND_DAY_VIEW = 0;
    private static final int YEAR_VIEW = 1;

    private static final String KEY_SELECTED_YEAR = "year";
    private static final String KEY_SELECTED_MONTH = "month";
    private static final String KEY_SELECTED_DAY = "day";
    private static final String KEY_LIST_POSITION = "list_position";
    private static final String KEY_WEEK_START = "week_start";
    private static final String KEY_DATE_START = "date_start";
    private static final String KEY_DATE_END = "date_end";
    private static final String KEY_CURRENT_VIEW = "current_view";
    private static final String KEY_LIST_POSITION_OFFSET = "list_position_offset";
    private static final String KEY_THEME = "theme";
    private static final String KEY_DISABLED_DAYS = "disabled_days";

    private static final MonthAdapter.CalendarDay DEFAULT_START_DATE = new MonthAdapter.CalendarDay(1900, Calendar
            .JANUARY, 1);
    private static final MonthAdapter.CalendarDay DEFAULT_END_DATE = new MonthAdapter.CalendarDay(2100, Calendar
            .DECEMBER, 31);

    private static final int ANIMATION_DURATION = 300;
    private static final int ANIMATION_DELAY = 500;

    private static final SimpleDateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy", Locale.getDefault());
    private static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("dd", Locale.getDefault());

    private final Calendar mCalendar = Calendar.getInstance();
    private OnDateSetListener mCallBack;

    private HashSet<OnDateChangedListener> mListeners = new HashSet<>();

    private AccessibleDateAnimator mAnimator;
    private LinearLayout mSelectedDateLayout;
    private TextView mDayOfWeekView;
    private LinearLayout mMonthAndDayView;
    private TextView mSelectedMonthTextView;
    private TextView mSelectedDayTextView;
    private TextView mYearView;
    private DayPickerView mDayPickerView;
    private YearPickerView mYearPickerView;

    private int mCurrentView = UNINITIALIZED;
    private int mWeekStart = mCalendar.getFirstDayOfWeek();
    private MonthAdapter.CalendarDay mMinDate = DEFAULT_START_DATE;
    private MonthAdapter.CalendarDay mMaxDate = DEFAULT_END_DATE;
    private String mDoneText;
    private String mCancelText;

    private SparseArray<MonthAdapter.CalendarDay> mDisabledDays;

    private HapticFeedbackController mHapticFeedbackController;

    private boolean mDelayAnimation = true;
    // Accessibility strings.
    private String mDayPickerDescription;
    private String mSelectDay;
    private String mYearPickerDescription;
    private String mSelectYear;

    private int mStyleResId;
    private int mSelectedColor;
    private int mUnselectedColor;

    private int mListPosition = -1;
    private int mListPositionOffset = 0;
    //private int mCurrentView = MONTH_AND_DAY_VIEW;

    public CalendarDatePickerView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public CalendarDatePickerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CalendarDatePickerView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(@NonNull Context context) {
        LayoutInflater.from(context).inflate(R.layout.calendar_date_picker_dialog, this, true);
        mSelectedDateLayout = (LinearLayout) findViewById(R.id.day_picker_selected_date_layout);
        mDayOfWeekView = (TextView) findViewById(R.id.date_picker_header);
        mMonthAndDayView = (LinearLayout) findViewById(R.id.date_picker_month_and_day);
        mMonthAndDayView.setOnClickListener(this);
        mSelectedMonthTextView = (TextView) findViewById(R.id.date_picker_month);
        mSelectedDayTextView = (TextView) findViewById(R.id.date_picker_day);
        mYearView = (TextView) findViewById(R.id.date_picker_year);
        mYearView.setOnClickListener(this);

        mDayPickerView = new SimpleDayPickerView(context, this);
        mYearPickerView = new YearPickerView(context, this);

        Resources res = getResources();
        TypedArray themeColors = context.obtainStyledAttributes(mStyleResId, R.styleable.BetterPickersDialogs);
        mDayPickerDescription = res.getString(R.string.day_picker_description);
        mSelectDay = res.getString(R.string.select_day);
        mYearPickerDescription = res.getString(R.string.year_picker_description);
        mSelectYear = res.getString(R.string.select_year);

        int headerBackgroundColor = themeColors.getColor(R.styleable.BetterPickersDialogs_bpHeaderBackgroundColor,
                ContextCompat.getColor(context, R.color.bpWhite));
        int preHeaderBackgroundColor = themeColors.getColor(R.styleable
                .BetterPickersDialogs_bpPreHeaderBackgroundColor, ContextCompat.getColor(context, R.color.bpWhite));
        int bodyBgColor = themeColors.getColor(R.styleable.BetterPickersDialogs_bpBodyBackgroundColor, ContextCompat
                .getColor(context, R.color.bpWhite));
        int buttonBgColor = themeColors.getColor(R.styleable.BetterPickersDialogs_bpButtonsBackgroundColor,
                ContextCompat.getColor(context, R.color.bpWhite));
        int buttonTextColor = themeColors.getColor(R.styleable.BetterPickersDialogs_bpButtonsTextColor, ContextCompat
                .getColor(context, R.color.bpBlue));
        mSelectedColor = themeColors.getColor(R.styleable.BetterPickersDialogs_bpHeaderSelectedTextColor,
                ContextCompat.getColor(context, R.color.bpWhite));
        mUnselectedColor = themeColors.getColor(R.styleable.BetterPickersDialogs_bpHeaderUnselectedTextColor,
                ContextCompat.getColor(context, R.color.radial_gray_light));


        mAnimator = (AccessibleDateAnimator) findViewById(R.id.animator);
        mAnimator.addView(mDayPickerView);
        mAnimator.addView(mYearPickerView);
        mAnimator.setDateMillis(mCalendar.getTimeInMillis());
        // TODO: Replace with animation decided upon by the design team.
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(ANIMATION_DURATION);
        mAnimator.setInAnimation(animation);
        // TODO: Replace with animation decided upon by the design team.
        Animation animation2 = new AlphaAnimation(1.0f, 0.0f);
        animation2.setDuration(ANIMATION_DURATION);
        mAnimator.setOutAnimation(animation2);

        updateDisplay(false);
        setCurrentView(mCurrentView);

        if (mListPosition != -1) {
            if (mCurrentView == MONTH_AND_DAY_VIEW) {
                mDayPickerView.postSetSelection(mListPosition);
            } else if (mCurrentView == YEAR_VIEW) {
                mYearPickerView.postSetSelectionFromTop(mListPosition, mListPositionOffset);
            }
        }

        mHapticFeedbackController = new HapticFeedbackController(context);

        mDayPickerView.setTheme(themeColors);
        mYearPickerView.setTheme(themeColors);

        mSelectedDateLayout.setBackgroundColor(headerBackgroundColor);
        mYearView.setBackgroundColor(headerBackgroundColor);
        mMonthAndDayView.setBackgroundColor(headerBackgroundColor);

        if (mDayOfWeekView != null) {
            mDayOfWeekView.setBackgroundColor(preHeaderBackgroundColor);
        }
        setBackgroundColor(bodyBgColor);
        mYearPickerView.setBackgroundColor(bodyBgColor);
        mDayPickerView.setBackgroundColor(bodyBgColor);
    }


    private void setCurrentView(final int viewIndex) {
        long millis = mCalendar.getTimeInMillis();

        switch (viewIndex) {
            case MONTH_AND_DAY_VIEW:
                ObjectAnimator pulseAnimator = Utils.getPulseAnimator(mMonthAndDayView, 0.9f, 1.05f);
                if (mDelayAnimation) {
                    pulseAnimator.setStartDelay(ANIMATION_DELAY);
                    mDelayAnimation = false;
                }
                mDayPickerView.onDateChanged();
                if (mCurrentView != viewIndex) {
                    mMonthAndDayView.setSelected(true);
                    mYearView.setSelected(false);
                    mSelectedDayTextView.setTextColor(mSelectedColor);
                    mSelectedMonthTextView.setTextColor(mSelectedColor);
                    mYearView.setTextColor(mUnselectedColor);
                    mAnimator.setDisplayedChild(MONTH_AND_DAY_VIEW);
                    mCurrentView = viewIndex;
                }
                pulseAnimator.start();

                int flags = DateUtils.FORMAT_SHOW_DATE;
                String dayString = DateUtils.formatDateTime(getContext(), millis, flags);
                mAnimator.setContentDescription(mDayPickerDescription + ": " + dayString);
                Utils.tryAccessibilityAnnounce(mAnimator, mSelectDay);
                break;
            case YEAR_VIEW:
                pulseAnimator = Utils.getPulseAnimator(mYearView, 0.85f, 1.1f);
                if (mDelayAnimation) {
                    pulseAnimator.setStartDelay(ANIMATION_DELAY);
                    mDelayAnimation = false;
                }
                mYearPickerView.onDateChanged();
                if (mCurrentView != viewIndex) {
                    mMonthAndDayView.setSelected(false);
                    mYearView.setSelected(true);
                    mSelectedDayTextView.setTextColor(mUnselectedColor);
                    mSelectedMonthTextView.setTextColor(mUnselectedColor);
                    mYearView.setTextColor(mSelectedColor);
                    mAnimator.setDisplayedChild(YEAR_VIEW);
                    mCurrentView = viewIndex;
                }
                pulseAnimator.start();

                CharSequence yearString = YEAR_FORMAT.format(millis);
                mAnimator.setContentDescription(mYearPickerDescription + ": " + yearString);
                Utils.tryAccessibilityAnnounce(mAnimator, mSelectYear);
                break;
        }
    }

    private void updateDisplay(boolean announce) {
        if (mDayOfWeekView != null) {
            mDayOfWeekView.setText(mCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG,
                    Locale.getDefault()).toUpperCase(Locale.getDefault()));
        }

        mSelectedMonthTextView.setText(mCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT,
                Locale.getDefault()).toUpperCase(Locale.getDefault()));
        mSelectedDayTextView.setText(DAY_FORMAT.format(mCalendar.getTime()));
        mYearView.setText(YEAR_FORMAT.format(mCalendar.getTime()));

        // Accessibility.
        long millis = mCalendar.getTimeInMillis();
        mAnimator.setDateMillis(millis);
        int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR;
        String monthAndDayText = DateUtils.formatDateTime(getContext(), millis, flags);
        mMonthAndDayView.setContentDescription(monthAndDayText);

        if (announce) {
            flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR;
            String fullDateText = DateUtils.formatDateTime(getContext(), millis, flags);
            Utils.tryAccessibilityAnnounce(mAnimator, fullDateText);
        }
    }

    /**
     * Sets the range of the dialog to be within the specific dates. Years and months outside of the
     * range are not shown, the days that are outside of the range are visible but cannot be selected.
     *
     * @param startDate The start date of the range (inclusive)
     * @param endDate   The end date of the range (inclusive)
     * @throws IllegalArgumentException in case the end date is smaller than the start date
     */
    public CalendarDatePickerView setDateRange(@Nullable MonthAdapter.CalendarDay startDate,
                                               @Nullable MonthAdapter.CalendarDay endDate) {
        if (startDate == null) {
            mMinDate = DEFAULT_START_DATE;
        } else {
            mMinDate = startDate;
        }
        if (endDate == null) {
            mMaxDate = DEFAULT_END_DATE;
        } else {
            mMaxDate = endDate;
        }
        if (mMaxDate.compareTo(mMinDate) < 0) {
            throw new IllegalArgumentException("End date must be larger than start date");
        }
        if (mDayPickerView != null) {
            mDayPickerView.onChange();
        }
        return this;
    }

    public CalendarDatePickerView setOnDateSetListener(OnDateSetListener listener) {
        mCallBack = listener;
        return this;
    }

    // If the newly selected month / year does not contain the currently selected day number,
    // change the selected day number to the last day of the selected month or year.
    //      e.g. Switching from Mar to Apr when Mar 31 is selected -> Apr 30
    //      e.g. Switching from 2012 to 2013 when Feb 29, 2012 is selected -> Feb 28, 2013
    private void adjustDayInMonthIfNeeded(int month, int year) {
        int day = mCalendar.get(Calendar.DAY_OF_MONTH);
        int daysInMonth = Utils.getDaysInMonth(month, year);
        if (day > daysInMonth) {
            mCalendar.set(Calendar.DAY_OF_MONTH, daysInMonth);
        }
    }

    @Override
    public void onClick(View v) {
        tryVibrate();
        if (v.getId() == R.id.date_picker_year) {
            setCurrentView(YEAR_VIEW);
        } else if (v.getId() == R.id.date_picker_month_and_day) {
            setCurrentView(MONTH_AND_DAY_VIEW);
        }
    }

    @Override
    public void onYearSelected(int year) {
        adjustDayInMonthIfNeeded(mCalendar.get(Calendar.MONTH), year);
        mCalendar.set(Calendar.YEAR, year);
        updatePickers();
        setCurrentView(MONTH_AND_DAY_VIEW);
        updateDisplay(true);
    }

//    @Override
//    public void onDismiss(DialogInterface dialoginterface) {
//        super.onDismiss(dialoginterface);
//        if (mDimissCallback != null) {
//            mDimissCallback.onDialogDismiss(dialoginterface);
//        }
//    }

    @Override
    public void onDayOfMonthSelected(int year, int month, int day) {
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, month);
        mCalendar.set(Calendar.DAY_OF_MONTH, day);
        updatePickers();
        updateDisplay(true);
    }

    private void updatePickers() {
        Iterator<OnDateChangedListener> iterator = mListeners.iterator();
        while (iterator.hasNext()) {
            iterator.next().onDateChanged();
        }
    }

    @Override
    public MonthAdapter.CalendarDay getSelectedDay() {
        return new MonthAdapter.CalendarDay(mCalendar);
    }

    @Override
    public MonthAdapter.CalendarDay getMinDate() {
        return mMinDate;
    }

    @Override
    public MonthAdapter.CalendarDay getMaxDate() {
        return mMaxDate;
    }

    @Override
    public SparseArray<MonthAdapter.CalendarDay> getDisabledDays() {
        return mDisabledDays;
    }

    /**
     * Sets a map of disabled days to declare as unselectable by the user. These days can be styled
     * in a different way than the currently selected day
     *
     * @param disabledDays sparse array of key date int (yyyyMMdd) to a calendar day object
     * @throws IllegalArgumentException in case the end date is smaller than the start date
     */
    public CalendarDatePickerView setDisabledDays(@NonNull SparseArray<MonthAdapter.CalendarDay>
                                                          disabledDays) {
        mDisabledDays = disabledDays;

        if (mDayPickerView != null) {
            mDayPickerView.onChange();
        }
        return this;
    }

    @Override
    public int getFirstDayOfWeek() {
        return mWeekStart;
    }

    public CalendarDatePickerView setFirstDayOfWeek(int startOfWeek) {
        if (startOfWeek < Calendar.SUNDAY || startOfWeek > Calendar.SATURDAY) {
            throw new IllegalArgumentException("Value must be between Calendar.SUNDAY and Calendar.SATURDAY");
        }
        mWeekStart = startOfWeek;
        if (mDayPickerView != null) {
            mDayPickerView.onChange();
        }
        return this;
    }

    @Override
    public void registerOnDateChangedListener(OnDateChangedListener listener) {
        mListeners.add(listener);
    }

    @Override
    public void unregisterOnDateChangedListener(OnDateChangedListener listener) {
        mListeners.remove(listener);
    }

    @Override
    public void tryVibrate() {
        mHapticFeedbackController.tryVibrate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mHapticFeedbackController.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHapticFeedbackController.stop();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof CalendarViewState) {
            CalendarViewState viewState = (CalendarViewState) state;
            Bundle savedInstanceState = viewState.getSavedInstanceState();
            if (savedInstanceState != null) {
                mWeekStart = savedInstanceState.getInt(KEY_WEEK_START);
                mMinDate = new MonthAdapter.CalendarDay(savedInstanceState.getLong(KEY_DATE_START));
                mMaxDate = new MonthAdapter.CalendarDay(savedInstanceState.getLong(KEY_DATE_END));
                mCurrentView = savedInstanceState.getInt(KEY_CURRENT_VIEW);
                mListPosition = savedInstanceState.getInt(KEY_LIST_POSITION);
                mListPositionOffset = savedInstanceState.getInt(KEY_LIST_POSITION_OFFSET);
                mStyleResId = savedInstanceState.getInt(KEY_THEME);
                mDisabledDays = savedInstanceState.getSparseParcelableArray(KEY_DISABLED_DAYS);
                // TODO restore the state of the view...
            }
            super.onRestoreInstanceState(viewState.getSuperState());
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle outState = new Bundle();
        outState.putInt(KEY_SELECTED_YEAR, mCalendar.get(Calendar.YEAR));
        outState.putInt(KEY_SELECTED_MONTH, mCalendar.get(Calendar.MONTH));
        outState.putInt(KEY_SELECTED_DAY, mCalendar.get(Calendar.DAY_OF_MONTH));
        outState.putInt(KEY_WEEK_START, mWeekStart);
        outState.putLong(KEY_DATE_START, mMinDate.getDateInMillis());
        outState.putLong(KEY_DATE_END, mMaxDate.getDateInMillis());
        outState.putInt(KEY_CURRENT_VIEW, mCurrentView);
        outState.putInt(KEY_THEME, mStyleResId);
        int listPosition = -1;
        if (mCurrentView == MONTH_AND_DAY_VIEW) {
            listPosition = mDayPickerView.getMostVisiblePosition();
        } else if (mCurrentView == YEAR_VIEW) {
            listPosition = mYearPickerView.getFirstVisiblePosition();
            outState.putInt(KEY_LIST_POSITION_OFFSET, mYearPickerView.getFirstPositionOffset());
        }
        outState.putInt(KEY_LIST_POSITION, listPosition);
        outState.putSparseParcelableArray(KEY_DISABLED_DAYS, mDisabledDays);
        return new CalendarViewState(super.onSaveInstanceState(), outState);
    }

    /**
     * The callback used to indicate the user is done filling in the date.
     */
    public interface OnDateSetListener {

        /**
         * @param view      The view associated with this listener.
         * @param year        The year that was set.
         * @param monthOfYear The month that was set (0-11) for compatibility with {@link java.util.Calendar}.
         * @param dayOfMonth  The day of the month that was set.
         */
        void onDateSet(View view, int year, int monthOfYear, int dayOfMonth);
    }

    /**
     * The callback used to notify other date picker components of a change in selected date.
     */
    public interface OnDateChangedListener {

        void onDateChanged();
    }

    @Override
    public void registerOnDateChangedListener(CalendarDatePickerDialogFragment.OnDateChangedListener listener) {
        // no op
    }

    @Override
    public void unregisterOnDateChangedListener(CalendarDatePickerDialogFragment.OnDateChangedListener listener) {
        // no op
    }
}
