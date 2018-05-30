package cn.simonlee.widgetdemo.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import cn.simonlee.widget.badgeview.Badge;
import cn.simonlee.widget.badgeview.BadgeProxy;

/**
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-05-29
 */
public class BadgeImageView extends AppCompatImageView implements Badge {

    private final BadgeProxy mBadgeProxy;

    public BadgeImageView(Context context) {
        super(context);
        mBadgeProxy = new BadgeProxy(this, context, null);
    }

    public BadgeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mBadgeProxy = new BadgeProxy(this, context, attrs);
    }

    public BadgeImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mBadgeProxy = new BadgeProxy(this, context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mBadgeProxy.onMeasure(getMeasuredWidth(), getMeasuredHeight());
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
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
        super.invalidate();
    }

    @Override
    public void setBadgeTextColor(int color) {
        mBadgeProxy.setBadgeTextColor(color);
        super.invalidate();
    }

    @Override
    public void setBadgeTextSize(float size) {
        mBadgeProxy.setBadgeTextSize(size);
        super.invalidate();
    }

    @Override
    public void setBadgeBoldText(boolean boldEnable) {
        mBadgeProxy.setBadgeBoldText(boldEnable);
        super.invalidate();
    }

    @Override
    public void setBadgeBackground(Drawable drawable) {
        mBadgeProxy.setBadgeBackground(drawable);
        super.invalidate();
    }

    @Override
    public void setBadgePadding(float padding) {
        mBadgeProxy.setBadgePadding(padding);
        super.invalidate();
    }

    @Override
    public void setBadgePadding(Float paddingLeft, Float paddingTop, Float paddingRight, Float paddingBottom) {
        mBadgeProxy.setBadgePadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        super.invalidate();
    }

    @Override
    public void setBadgeMargin(float margin) {
        mBadgeProxy.setBadgeMargin(margin);
        super.invalidate();
    }

    @Override
    public void setBadgeMargin(Float marginLeft, Float marginTop, Float marginRight, Float marginBottom) {
        mBadgeProxy.setBadgeMargin(marginLeft, marginTop, marginRight, marginBottom);
        super.invalidate();
    }

    @Override
    public void setBadgeGravity(int gravity) {
        mBadgeProxy.setBadgeGravity(gravity);
        super.invalidate();
    }

}
