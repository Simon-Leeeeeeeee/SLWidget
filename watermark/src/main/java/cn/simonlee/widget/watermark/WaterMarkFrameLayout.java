package cn.simonlee.widget.watermark;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

/**
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2019-06-15
 */
public class WaterMarkFrameLayout extends FrameLayout implements IWaterMark {

    private final WaterMark mWaterMark;

    public WaterMarkFrameLayout(Context context) {
        super(context);
        mWaterMark = new WaterMark(this, null, 0);
    }

    public WaterMarkFrameLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mWaterMark = new WaterMark(this, attrs, 0);
    }

    public WaterMarkFrameLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mWaterMark = new WaterMark(this, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WaterMarkFrameLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mWaterMark = new WaterMark(this, attrs, defStyleAttr);
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        mWaterMark.drawWaterMark(canvas);
    }

    @Override
    public WaterMark getWaterMark() {
        return mWaterMark;
    }

}
