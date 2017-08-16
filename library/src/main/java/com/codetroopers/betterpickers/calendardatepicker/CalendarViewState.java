package com.codetroopers.betterpickers.calendardatepicker;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.FrameLayout;

class CalendarViewState extends FrameLayout.BaseSavedState {

    private Bundle savedInstanceState;

    public CalendarViewState(Parcelable superState, Bundle savedInstanceState) {
        super(superState);
        this.savedInstanceState = savedInstanceState;
    }


    public Bundle getSavedInstanceState() {
        return savedInstanceState;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeBundle(this.savedInstanceState);
    }

    protected CalendarViewState(Parcel in) {
        super(in);
        this.savedInstanceState = in.readBundle(Bundle.EMPTY.getClassLoader());
    }

    public static final Creator<CalendarViewState> CREATOR = new Creator<CalendarViewState>() {
        @Override
        public CalendarViewState createFromParcel(Parcel source) {
            return new CalendarViewState(source);
        }

        @Override
        public CalendarViewState[] newArray(int size) {
            return new CalendarViewState[size];
        }
    };
}