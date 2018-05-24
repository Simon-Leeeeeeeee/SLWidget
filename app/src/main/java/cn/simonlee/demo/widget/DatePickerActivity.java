package cn.simonlee.demo.widget;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cn.simonlee.widget.ScrollPickerView;

/**
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-05-22
 */
public class DatePickerActivity extends AppCompatActivity implements ScrollPickerView.OnItemSelectedListener {

    public static final int TYPE_YEAR = 0x10;
    public static final int TYPE_MONTH = 0x08;
    public static final int TYPE_DAY = 0x04;
    public static final int TYPE_HOUR = 0x02;
    public static final int TYPE_MINUTE = 0x01;

    private DatePickerAdapter mYearAdapter, mMonthAdapter, mDayAdapter, mHourAdapter, mMinAdapter;
    private ScrollPickerView mPicker_Year, mPicker_Month, mPicker_Day, mPicker_Hour, mPicker_Min;
    private String mDefaultDate;
    private int mType;//五位二进制数 ，分别对应年月日时分

    private String mSelect_Year;
    private String mSelect_Month;
    private String mSelect_Day;
    private String mSelect_Hour;
    private String mSelect_Min;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datepicker);
        initView();
        initAdapter();
        initSelectedDate();
    }

    private void initView() {
        Intent intent = getIntent();
        mDefaultDate = intent.getStringExtra("defaultDate");
        mType = intent.getIntExtra("type", 0x1C);
        Log.e("PickerView", "initView() type = " + mType);

        mPicker_Year = findViewById(R.id.datepicker_year);
        mPicker_Month = findViewById(R.id.datepicker_month);
        mPicker_Day = findViewById(R.id.datepicker_day);
        mPicker_Hour = findViewById(R.id.datepicker_hour);
        mPicker_Min = findViewById(R.id.datepicker_minute);

        if (!hasType(TYPE_YEAR)) {
            mPicker_Year.setVisibility(View.GONE);
        } else {
            mPicker_Year.setOnItemSelectedListener(this);
        }
        if (!hasType(TYPE_MONTH)) {
            mPicker_Month.setVisibility(View.GONE);
        } else {
            mPicker_Month.setOnItemSelectedListener(this);
        }
        if (!hasType(TYPE_DAY)) {
            mPicker_Day.setVisibility(View.GONE);
        } else {
            mPicker_Day.setOnItemSelectedListener(this);
        }
        if (!hasType(TYPE_HOUR)) {
            mPicker_Hour.setVisibility(View.GONE);
        } else {
            mPicker_Hour.setOnItemSelectedListener(this);
        }
        if (!hasType(TYPE_MINUTE)) {
            mPicker_Min.setVisibility(View.GONE);
        } else {
            mPicker_Min.setOnItemSelectedListener(this);
        }
    }

    private void initAdapter() {
        DecimalFormat doubleDigitFormat = new DecimalFormat("00");
        if (hasType(TYPE_YEAR)) {
            mYearAdapter = new DatePickerAdapter(1800, 2200);
            mPicker_Year.setAdapter(mYearAdapter);
        }
        if (hasType(TYPE_MONTH)) {
            mMonthAdapter = new DatePickerAdapter(1, 12, doubleDigitFormat);
            mPicker_Month.setAdapter(mMonthAdapter);
        }
        if (hasType(TYPE_DAY)) {
            mDayAdapter = new DatePickerAdapter(1, 31, doubleDigitFormat);
            mPicker_Day.setAdapter(mDayAdapter);
        }
        if (hasType(TYPE_HOUR)) {
            mHourAdapter = new DatePickerAdapter(1, 24, doubleDigitFormat);
            mPicker_Hour.setAdapter(mHourAdapter);
        }
        if (hasType(TYPE_MINUTE)) {
            mMinAdapter = new DatePickerAdapter(0, 59, doubleDigitFormat);
            mPicker_Min.setAdapter(mMinAdapter);
        }
    }

    private void initSelectedDate() {
        if (mDefaultDate == null || mDefaultDate.length() < 1) {
            mDefaultDate = new SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.US).format(new Date());
        } else {
            mDefaultDate = mDefaultDate.replaceAll("(?:[ :/年月日时分])", "-");
        }
        String[] date = mDefaultDate.split("-");
        int index = 0;
        try {
            if (hasType(TYPE_YEAR))
                mPicker_Year.setSelectedPosition(mYearAdapter.indexOf(date[index++]));
            if (hasType(TYPE_MONTH))
                mPicker_Month.setSelectedPosition(mMonthAdapter.indexOf(date[index++]));
            if (hasType(TYPE_DAY))
                mPicker_Day.setSelectedPosition(mDayAdapter.indexOf(date[index++]));
            if (hasType(TYPE_HOUR))
                mPicker_Hour.setSelectedPosition(mHourAdapter.indexOf(date[index++]));
            if (hasType(TYPE_MINUTE))
                mPicker_Min.setSelectedPosition(mMinAdapter.indexOf(date[index]));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemSelected(View view, int position, String value) {
        switch (view.getId()) {
            case R.id.datepicker_year: {
                int month = mPicker_Month.getSelectedPosition() + mMonthAdapter.getMinValue();
                int year = mPicker_Year.getSelectedPosition() + mYearAdapter.getMinValue();
                int newMaxDay = getMaxDay(year, month);
                if (newMaxDay != mDayAdapter.getMaxValue()) {
                    mDayAdapter.resetMax(newMaxDay);
                    mPicker_Day.invalidate();
                }
                mSelect_Year = value;
                break;
            }
            case R.id.datepicker_month: {
                int year = mPicker_Year.getSelectedPosition() + mYearAdapter.getMinValue();
                int month = mPicker_Month.getSelectedPosition() + mMonthAdapter.getMinValue();
                int newMaxDay = getMaxDay(year, month);
                if (newMaxDay != mDayAdapter.getMaxValue()) {
                    mDayAdapter.resetMax(newMaxDay);
                    mPicker_Day.invalidate();
                }
                mSelect_Month = value;
                break;
            }
            case R.id.datepicker_day: {
                mSelect_Day = value;
                break;
            }
            case R.id.datepicker_hour: {
                mSelect_Hour = value;
                break;
            }
            case R.id.datepicker_minute: {
                mSelect_Min = value;
                break;
            }
        }
    }

    private int getMaxDay(int year, int month) {
        if (month == 4 || month == 6 || month == 9 || month == 11) {
            return 30;
        } else if (month == 2) {
            if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
                return 29;
            } else {
                return 28;
            }
        }
        return 31;
    }

    private boolean hasType(int type) {
        return (mType & type) == type;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("date", getSelectDate());
        setResult(999, intent);
        super.onBackPressed();
    }

    private String getSelectDate() {
        String date = "";
        if (hasType(TYPE_YEAR))
            date = mSelect_Year;
        if (hasType(TYPE_MONTH))
            date += (date.length() > 0 ? "-" : "") + mSelect_Month;
        if (hasType(TYPE_DAY))
            date += (date.length() > 0 ? "-" : "") + mSelect_Day;

        String time = "";
        if (hasType(TYPE_HOUR))
            time = mSelect_Hour;
        if (hasType(TYPE_MINUTE))
            time += (time.length() > 0 ? ":" : "") + mSelect_Min;

        if (date.length() > 0 && time.length() > 0) {
            date += " " + time;
        } else if (time.length() > 0) {
            date = time;
        }
        return date;
    }

}
