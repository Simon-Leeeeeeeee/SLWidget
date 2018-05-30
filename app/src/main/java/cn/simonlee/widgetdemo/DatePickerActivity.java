package cn.simonlee.widgetdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.text.DecimalFormat;

import cn.simonlee.widget.scrollpicker.ScrollPickerView;

/**
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-05-22
 */
public class DatePickerActivity extends AppCompatActivity implements ScrollPickerView.OnItemSelectedListener, CompoundButton.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener, RadioGroup.OnCheckedChangeListener {

    private DatePickerAdapter mYearAdapter, mMonthAdapter, mDayAdapter, mHourAdapter, mMinAdapter;
    private ScrollPickerView mPicker_Year, mPicker_Month, mPicker_Day, mPicker_Hour, mPicker_Min;

    private String mSelect_Year;
    private String mSelect_Month;
    private String mSelect_Day;
    private String mSelect_Hour;
    private String mSelect_Min;
    private TextView mTextView_Result;
    private TextView mTextView_TextRows, mTextView_TextSpacing, mTextView_TextSize, mTextView_TextRatio;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datepicker);
        initView();
        initAdapter();
        initSelectedDate();
    }

    private void initView() {
        mPicker_Year = findViewById(R.id.datepicker_year);
        mPicker_Month = findViewById(R.id.datepicker_month);
        mPicker_Day = findViewById(R.id.datepicker_day);
        mPicker_Hour = findViewById(R.id.datepicker_hour);
        mPicker_Min = findViewById(R.id.datepicker_minute);

        mTextView_Result = findViewById(R.id.tv_result);

        mTextView_TextRows = findViewById(R.id.tv_rows);
        mTextView_TextSpacing = findViewById(R.id.tv_spacing);
        mTextView_TextSize = findViewById(R.id.tv_textsize);
        mTextView_TextRatio = findViewById(R.id.tv_textratio);

        ((Switch) findViewById(R.id.swicth_loop)).setOnCheckedChangeListener(this);

        ((SeekBar) findViewById(R.id.sb_rows)).setOnSeekBarChangeListener(this);
        ((SeekBar) findViewById(R.id.sb_spacing)).setOnSeekBarChangeListener(this);
        ((SeekBar) findViewById(R.id.sb_textsize)).setOnSeekBarChangeListener(this);
        ((SeekBar) findViewById(R.id.sb_textratio)).setOnSeekBarChangeListener(this);

        ((RadioGroup) findViewById(R.id.radiogroup_gravity)).setOnCheckedChangeListener(this);

        mPicker_Year.setOnItemSelectedListener(this);
        mPicker_Month.setOnItemSelectedListener(this);
        mPicker_Day.setOnItemSelectedListener(this);
        mPicker_Hour.setOnItemSelectedListener(this);
        mPicker_Min.setOnItemSelectedListener(this);
    }

    private void initAdapter() {
        DecimalFormat doubleDigitFormat = new DecimalFormat("00");
        mYearAdapter = new DatePickerAdapter(1800, 2200);
        mPicker_Year.setAdapter(mYearAdapter);
        mMonthAdapter = new DatePickerAdapter(1, 12, doubleDigitFormat);
        mPicker_Month.setAdapter(mMonthAdapter);
        mDayAdapter = new DatePickerAdapter(1, 31, doubleDigitFormat);
        mPicker_Day.setAdapter(mDayAdapter);
        mHourAdapter = new DatePickerAdapter(1, 24, doubleDigitFormat);
        mPicker_Hour.setAdapter(mHourAdapter);
        mMinAdapter = new DatePickerAdapter(0, 59, doubleDigitFormat);
        mPicker_Min.setAdapter(mMinAdapter);
    }

    private void initSelectedDate() {
        mPicker_Year.setSelectedPosition(mYearAdapter.indexOf(2018));
        mPicker_Month.setSelectedPosition(mMonthAdapter.indexOf(5));
        mPicker_Day.setSelectedPosition(mDayAdapter.indexOf(22));
        mPicker_Hour.setSelectedPosition(mHourAdapter.indexOf(9));
        mPicker_Min.setSelectedPosition(mMinAdapter.indexOf(16));
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
        if (mSelect_Year != null && mSelect_Month != null && mSelect_Day != null && mSelect_Hour != null && mSelect_Min != null) {
            mTextView_Result.setText(mSelect_Year + "-" + mSelect_Month + "-" + mSelect_Day + " " + mSelect_Hour + ":" + mSelect_Min);
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

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mPicker_Year.setLoopable(isChecked);
        mPicker_Month.setLoopable(isChecked);
        mPicker_Day.setLoopable(isChecked);
        mPicker_Hour.setLoopable(isChecked);
        mPicker_Min.setLoopable(isChecked);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        int gravity;
        switch (checkedId) {
            case R.id.radio_gravityleft: {
                gravity = ScrollPickerView.GRAVITY_LEFT;
                break;
            }
            case R.id.radio_gravitycenter: {
                gravity = ScrollPickerView.GRAVITY_CENTER;
                break;
            }
            case R.id.radio_gravityright: {
                gravity = ScrollPickerView.GRAVITY_RIGHT;
                break;
            }
            default:
                return;
        }
        mPicker_Year.setGravity(gravity);
        mPicker_Month.setGravity(gravity);
        mPicker_Day.setGravity(gravity);
        mPicker_Hour.setGravity(gravity);
        mPicker_Min.setGravity(gravity);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.sb_rows: {
                mTextView_TextRows.setText(String.valueOf(progress + 3));
                mPicker_Year.setTextRows(progress + 3);
                mPicker_Month.setTextRows(progress + 3);
                mPicker_Day.setTextRows(progress + 3);
                mPicker_Hour.setTextRows(progress + 3);
                mPicker_Min.setTextRows(progress + 3);
                break;
            }
            case R.id.sb_spacing: {
                mTextView_TextSpacing.setText(String.valueOf(progress - 40));
                mPicker_Year.setRowSpacing(progress - 40);
                mPicker_Month.setRowSpacing(progress - 40);
                mPicker_Day.setRowSpacing(progress - 40);
                mPicker_Hour.setRowSpacing(progress - 40);
                mPicker_Min.setRowSpacing(progress - 40);
                break;
            }
            case R.id.sb_textsize: {
                mTextView_TextSize.setText(String.valueOf(progress));
                mPicker_Year.setTextSize(progress);
                mPicker_Month.setTextSize(progress);
                mPicker_Day.setTextSize(progress);
                mPicker_Hour.setTextSize(progress);
                mPicker_Min.setTextSize(progress);
                break;
            }
            case R.id.sb_textratio: {
                mTextView_TextRatio.setText(String.valueOf(progress / 10F));
                mPicker_Year.setTextRatio(progress / 10F);
                mPicker_Month.setTextRatio(progress / 10F);
                mPicker_Day.setTextRatio(progress / 10F);
                mPicker_Hour.setTextRatio(progress / 10F);
                mPicker_Min.setTextRatio(progress / 10F);
                break;
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

}
