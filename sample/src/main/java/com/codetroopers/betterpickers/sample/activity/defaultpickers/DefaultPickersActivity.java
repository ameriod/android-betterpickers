package com.codetroopers.betterpickers.sample.activity.defaultpickers;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.codetroopers.betterpickers.sample.R;
import com.codetroopers.betterpickers.sample.activity.BaseSampleActivity;

public class DefaultPickersActivity extends BaseSampleActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.default_pickers_activity);

        findViewById(R.id.btn_default_date_picker)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final DatePicker datePicker = new DatePicker(DefaultPickersActivity.this);
                        new AlertDialog.Builder(DefaultPickersActivity.this)
                                .setView(datePicker)
                                .setPositiveButton("Done", null)
                                .setNegativeButton("Cancel", null)
                                .show();
                    }
                });

        findViewById(R.id.btb_default_time_picker)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final TimePicker timePicker = new TimePicker(DefaultPickersActivity.this);
                        new AlertDialog.Builder(DefaultPickersActivity.this)
                                .setView(timePicker)
                                .setPositiveButton("Done", null)
                                .setNegativeButton("Cancel", null)
                                .show();
                    }
                });
    }
}
