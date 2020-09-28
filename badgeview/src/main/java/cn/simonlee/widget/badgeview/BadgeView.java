package cn.simonlee.widget.badgeview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

/**
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-05-25
 */
public class BadgeView extends View implements IBadge {

    private final Badge mBadge;

    private int mLayoutWidth, mLayoutHeight;
    private boolean isExactlyDimension = true;

    public BadgeView(Context context) {
        super(context);
        mBadge = new Badge(this, null);
    }

    public BadgeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mBadge = new Badge(this, attrs);
    }

    public BadgeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mBadge = new Badge(this, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BadgeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mBadge = new Badge(this, attrs);
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        mBadge.drawBadge(canvas);
    }

    @Override
    public Badge getBadge() {
        return mBadge;
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        mLayoutWidth = params.width;
        mLayoutHeight = params.height;
        super.setLayoutParams(params);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        isExactlyDimension = widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY;

        //if (mLayoutWidth == ViewGroup.LayoutParams.WRAP_CONTENT)
        if (widthMode == MeasureSpec.UNSPECIFIED) {
            widthSize = mBadge.getBadgeWidth();
        } else if (widthMode == MeasureSpec.AT_MOST) {
            widthSize = Math.min(widthSize, mBadge.getBadgeWidth());
        }

        //if (mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT)
        if (heightMode == MeasureSpec.UNSPECIFIED) {
            heightSize = mBadge.getBadgeHeight();
        } else if (heightMode == MeasureSpec.AT_MOST) {
            heightSize = Math.min(heightSize, mBadge.getBadgeHeight());
        }

        super.setMeasuredDimension(resolveSize(widthSize, widthMeasureSpec), resolveSize(heightSize, heightMeasureSpec));
    }

    public void refreshBadge() {
        if (mLayoutWidth == ViewGroup.LayoutParams.WRAP_CONTENT || mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT || !isExactlyDimension) {
            super.requestLayout();
        } else {
            super.invalidate();
        }
    }

}
