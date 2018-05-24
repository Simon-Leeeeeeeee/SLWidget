package cn.simonlee.demo.widget;

import java.text.DecimalFormat;

import cn.simonlee.widget.PickAdapter;

/**
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-05-17
 */
public class DatePickerAdapter implements PickAdapter {

    private int mMinValue;
    private int mMaxValue;
    private final DecimalFormat mDecimalFormat;

    public DatePickerAdapter(int minValue, int maxValue) {
        this(minValue, maxValue, null);
    }

    public DatePickerAdapter(int minValue, int maxValue, DecimalFormat decimalFormat) {
        this.mMinValue = minValue;
        this.mMaxValue = maxValue;
        this.mDecimalFormat = decimalFormat;
    }

    @Override
    public int getCount() {
        return mMaxValue - mMinValue + 1;
    }

    @Override
    public String getItem(int position) {
        if (position >= 0 && position < getCount()) {
            if (mDecimalFormat == null) {
                return String.valueOf(mMinValue + position);
            } else {
                return mDecimalFormat.format(mMinValue + position);
            }
        }
        return null;
    }

    public int indexOf(String valueString) {
        int value;
        try {
            value = Integer.parseInt(valueString);
        } catch (NumberFormatException e) {
            return -1;
        }
        return indexOf(value);
    }

    public int indexOf(int value) {
        if (value < mMinValue || value > mMaxValue) {
            return -1;
        }
        return value - mMinValue;
    }

    public int getMinValue() {
        return mMinValue;
    }

    public int getMaxValue() {
        return mMaxValue;
    }

    public void resetMin(int minValue) {
        this.mMinValue = minValue;
    }

    public void resetMax(int maxValue) {
        this.mMaxValue = maxValue;
    }

}
