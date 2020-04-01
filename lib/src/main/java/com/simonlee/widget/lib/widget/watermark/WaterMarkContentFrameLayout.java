package com.simonlee.widget.lib.widget.watermark;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.support.v7.widget.ContentFrameLayout;
import android.util.AttributeSet;

import cn.simonlee.widget.watermark.IWaterMark;
import cn.simonlee.widget.watermark.WaterMark;

/**
 * 水印布局，继承自ContentFrameLayout，用于替换R.id.content
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2019-06-16
 */
@SuppressLint("RestrictedApi")
public class WaterMarkContentFrameLayout extends ContentFrameLayout implements IWaterMark {

    private final WaterMark mWaterMark;

    public WaterMarkContentFrameLayout(Context context) {
        super(context);
        mWaterMark = new WaterMark(this, null, 0);
    }

    public WaterMarkContentFrameLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mWaterMark = new WaterMark(this, attrs, 0);
    }

    public WaterMarkContentFrameLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mWaterMark = new WaterMark(this, attrs, defStyleAttr);
    }

    @Override
    public WaterMark getWaterMark() {
        return mWaterMark;
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        mWaterMark.drawWaterMark(canvas);
    }

}
