package cn.simonlee.widget.badgeview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-05-25
 */
public class BadgeView extends View implements Badge {

    private final BadgeProxy mBadgeProxy;

    private int mLayoutWidth, mLayoutHeight;

    public BadgeView(Context context) {
        super(context);
        mBadgeProxy = new BadgeProxy(this, context, null);
    }

    public BadgeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mBadgeProxy = new BadgeProxy(this, context, attrs);
    }

    public BadgeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mBadgeProxy = new BadgeProxy(this, context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BadgeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mBadgeProxy = new BadgeProxy(this, context, attrs);
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        mLayoutWidth = params.width;
        mLayoutHeight = params.height;
        int badgeGravity = mBadgeProxy.getBadgeGravity();
        if (mLayoutWidth == ViewGroup.LayoutParams.WRAP_CONTENT) {
            badgeGravity |= Badge.GRAVITY_LEFT;//宽度自适应时，强制左对齐
        }
        if (mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
            badgeGravity |= Badge.GRAVITY_TOP;//高度自适应时，强制上对齐
        }
        mBadgeProxy.setBadgeGravity(badgeGravity);
        super.setLayoutParams(params);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (mLayoutWidth == ViewGroup.LayoutParams.WRAP_CONTENT && widthMode != MeasureSpec.EXACTLY) {
            widthSize = mBadgeProxy.getBadgeWidth();
        }
        if (mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT && heightMode != MeasureSpec.EXACTLY) {
            heightSize = mBadgeProxy.getBadgeHeight();
        }
        super.setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBadgeProxy.onSizeChanged(w, h);
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        mBadgeProxy.dispatchDraw(canvas);
    }

    @Override
    public String getBadgeText() {
        return mBadgeProxy.getBadgeText();
    }

    @Override
    public void setBadgeText(String text) {
        mBadgeProxy.setBadgeText(text);
        //宽高不固定，重新设置尺寸
        if (mLayoutWidth == ViewGroup.LayoutParams.WRAP_CONTENT || mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
            super.requestLayout();
        } else {
            super.invalidate();
        }
    }

    @Override
    public void setBadgeTextColor(int color) {
        mBadgeProxy.setBadgeTextColor(color);
        super.invalidate();
    }

    @Override
    public void setBadgeTextSize(float size) {
        mBadgeProxy.setBadgeTextSize(size);
        //宽高不固定，重新设置尺寸
        if (mLayoutWidth == ViewGroup.LayoutParams.WRAP_CONTENT || mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
            super.requestLayout();
        } else {
            super.invalidate();
        }
    }

    @Override
    public void setBadgeBoldText(boolean boldEnable) {
        mBadgeProxy.setBadgeBoldText(boldEnable);
        //宽高不固定，重新设置尺寸
        if (mLayoutWidth == ViewGroup.LayoutParams.WRAP_CONTENT || mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
            super.requestLayout();
        } else {
            super.invalidate();
        }
    }

    @Override
    public void setBadgeBackground(Drawable drawable) {
        mBadgeProxy.setBadgeBackground(drawable);
        super.invalidate();
    }

    public void setBadgeBackgroundResource(@DrawableRes int resid) {
        Drawable drawable = null;
        if (resid != 0) {
            drawable = ResourcesCompat.getDrawable(getResources(), resid, null);
        }
        setBadgeBackground(drawable);
    }

    @Override
    public void setBadgePadding(float padding) {
        mBadgeProxy.setBadgePadding(padding);
        //宽高不固定，重新设置尺寸
        if (mLayoutWidth == ViewGroup.LayoutParams.WRAP_CONTENT || mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
            super.requestLayout();
        } else {
            super.invalidate();
        }
    }

    @Override
    public void setBadgePadding(Float paddingLeft, Float paddingTop, Float paddingRight, Float paddingBottom) {
        mBadgeProxy.setBadgePadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        //宽高不固定，重新设置尺寸
        if (mLayoutWidth == ViewGroup.LayoutParams.WRAP_CONTENT || mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
            super.requestLayout();
        } else {
            super.invalidate();
        }
    }

    @Override
    public void setBadgeMargin(float margin) {
        mBadgeProxy.setBadgeMargin(margin);
        //宽高不固定，重新设置尺寸
        if (mLayoutWidth == ViewGroup.LayoutParams.WRAP_CONTENT || mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
            super.requestLayout();
        } else {
            super.invalidate();
        }
    }

    @Override
    public void setBadgeMargin(Float marginLeft, Float marginTop, Float marginRight, Float marginBottom) {
        mBadgeProxy.setBadgeMargin(marginLeft, marginTop, marginRight, marginBottom);
        //宽高不固定，重新设置尺寸
        if (mLayoutWidth == ViewGroup.LayoutParams.WRAP_CONTENT || mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
            super.requestLayout();
        } else {
            super.invalidate();
        }
    }

    @Override
    public void setBadgeGravity(int gravity) {
        if (mLayoutWidth == ViewGroup.LayoutParams.WRAP_CONTENT) {
            gravity |= Badge.GRAVITY_LEFT;//宽度自适应时，强制左对齐
        }
        if (mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
            gravity |= Badge.GRAVITY_TOP;//高度自适应时，强制上对齐
        }
        mBadgeProxy.setBadgeGravity(gravity);
        super.invalidate();
    }

}
