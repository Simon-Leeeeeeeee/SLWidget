package cn.simonlee.widgetdemo.scrollpicker;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Random;

import cn.simonlee.widget.scrollpicker.ScrollPickerView;
import cn.simonlee.widgetdemo.BaseActivity;
import cn.simonlee.widgetdemo.R;

/**
 * 日期选择页面
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-05-22
 */
public class ScrollPickerActivity extends BaseActivity implements ScrollPickerView.OnItemSelectedListener, CompoundButton.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener, RadioGroup.OnCheckedChangeListener, View.OnClickListener {

    private DatePickerAdapter mYearAdapter, mMonthAdapter, mDayAdapter, mHourAdapter, mMinAdapter;
    private ScrollPickerView mPicker_Year, mPicker_Month, mPicker_Day, mPicker_Hour, mPicker_Min;

    private int mSelectedYear, mSelectedMonth, mSelectedDay, mSelectedHour, mSelectedMin;

    private TextView mTextView_Result;
    private TextView mTextView_TextRows, mTextView_TextSpacing, mTextView_TextSize, mTextView_TextRatio;
    private float mDensityDP, mDensitySP;
    private Random mRandom;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrollpicker);

        mDensityDP = getResources().getDisplayMetrics().density;//DP密度
        mDensitySP = getResources().getDisplayMetrics().scaledDensity;//SP密度

        Toolbar toolbar = getToolbar();
        if (toolbar != null) {
            toolbar.setTitle(R.string.scrollpicker);
            toolbar.setNavigationOnClickListener(this);
        }

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

        findViewById(R.id.btn_color_center).setOnClickListener(this);
        findViewById(R.id.btn_color_outside).setOnClickListener(this);

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.base_toolbar_navigation: {
                onBackPressed();
                break;
            }
            case R.id.btn_color_center: {
                if (mRandom == null) {
                    mRandom = new Random();
                }
                int randomColor = Color.argb(255, mRandom.nextInt(255), mRandom.nextInt(255), mRandom.nextInt(255));
                mPicker_Year.setCenterTextColor(randomColor);
                mPicker_Month.setCenterTextColor(randomColor);
                mPicker_Day.setCenterTextColor(randomColor);
                mPicker_Hour.setCenterTextColor(randomColor);
                mPicker_Min.setCenterTextColor(randomColor);
                break;
            }
            case R.id.btn_color_outside: {
                if (mRandom == null) {
                    mRandom = new Random();
                }
                int randomColor = Color.argb(255, mRandom.nextInt(255), mRandom.nextInt(255), mRandom.nextInt(255));
                mPicker_Year.setOutsideTextColor(randomColor);
                mPicker_Month.setOutsideTextColor(randomColor);
                mPicker_Day.setOutsideTextColor(randomColor);
                mPicker_Hour.setOutsideTextColor(randomColor);
                mPicker_Min.setOutsideTextColor(randomColor);
                break;
            }
        }
    }

    private void initAdapter() {
        DecimalFormat doubleDigitFormat = new DecimalFormat("00");
        mYearAdapter = new DatePickerAdapter(1800, 2200);
        mPicker_Year.setAdapter(mYearAdapter);
        mMonthAdapter = new DatePickerAdapter(1, 12, doubleDigitFormat);
        mPicker_Month.setAdapter(mMonthAdapter);
        mDayAdapter = new DatePickerAdapter(1, 31, doubleDigitFormat);
        mPicker_Day.setAdapter(mDayAdapter);
        mHourAdapter = new DatePickerAdapter(0, 23, doubleDigitFormat);
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
    public void onItemSelected(View view, int position) {
        switch (view.getId()) {
            case R.id.datepicker_year: {
                mSelectedYear = mYearAdapter.getDate(position);
                resetMaxDay();
                break;
            }
            case R.id.datepicker_month: {
                mSelectedMonth = mMonthAdapter.getDate(position);
                resetMaxDay();
                break;
            }
            case R.id.datepicker_day: {
                mSelectedDay = mDayAdapter.getDate(position);
                break;
            }
            case R.id.datepicker_hour: {
                mSelectedHour = mHourAdapter.getDate(position);
                break;
            }
            case R.id.datepicker_minute: {
                mSelectedMin = mHourAdapter.getDate(position);
                break;
            }
        }
        mTextView_Result.setText(mSelectedYear + "-" + mSelectedMonth + "-" + mSelectedDay + " " + mSelectedHour + ":" + mSelectedMin);
    }

    private void resetMaxDay() {
        int newMaxDay = getMaxDay(mSelectedYear, mSelectedMonth);
        if (newMaxDay != mDayAdapter.getMaxValue()) {
            mDayAdapter.setMaxValue(newMaxDay);
            mPicker_Day.invalidate();
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
        mPicker_Year.setLoopEnable(isChecked);
        mPicker_Month.setLoopEnable(isChecked);
        mPicker_Day.setLoopEnable(isChecked);
        mPicker_Hour.setLoopEnable(isChecked);
        mPicker_Min.setLoopEnable(isChecked);
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
                progress += 3;
                mTextView_TextRows.setText(progress + "行");
                mPicker_Year.setTextRows(progress);
                mPicker_Month.setTextRows(progress);
                mPicker_Day.setTextRows(progress);
                mPicker_Hour.setTextRows(progress);
                mPicker_Min.setTextRows(progress);
                break;
            }
            case R.id.sb_spacing: {
                progress -= 40;
                mTextView_TextSpacing.setText(progress + "DP");
                mPicker_Year.setRowSpacing(progress * mDensityDP);
                mPicker_Month.setRowSpacing(progress * mDensityDP);
                mPicker_Day.setRowSpacing(progress * mDensityDP);
                mPicker_Hour.setRowSpacing(progress * mDensityDP);
                mPicker_Min.setRowSpacing(progress * mDensityDP);
                break;
            }
            case R.id.sb_textsize: {
                progress++;
                mTextView_TextSize.setText(progress + "SP");
                mPicker_Year.setTextSize(progress * mDensitySP);
                mPicker_Month.setTextSize(progress * mDensitySP);
                mPicker_Day.setTextSize(progress * mDensitySP);
                mPicker_Hour.setTextSize(progress * mDensitySP);
                mPicker_Min.setTextSize(progress * mDensitySP);
                break;
            }
            case R.id.sb_textratio: {
                float ratio = progress / 10F;
                mTextView_TextRatio.setText(ratio + "倍");
                mPicker_Year.setTextRatio(ratio);
                mPicker_Month.setTextRatio(ratio);
                mPicker_Day.setTextRatio(ratio);
                mPicker_Hour.setTextRatio(ratio);
                mPicker_Min.setTextRatio(ratio);
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
