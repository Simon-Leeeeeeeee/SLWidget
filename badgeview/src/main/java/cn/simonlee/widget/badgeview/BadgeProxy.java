package cn.simonlee.widget.badgeview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-05-29
 */
public class BadgeProxy {
    /**
     * 浮标文本，为null时不显示，长度0时显示小圆点
     */
    private String mBadgeText;

    /**
     * 浮标字体颜色
     */
    private int mBadgeTextColor;

    /**
     * 浮标字体大小
     */
    private float mBadgeTextSize;

    /**
     * 字体是否加粗
     */
    private boolean mBoldTextEnable;

    /**
     * 浮标背景
     */
    private Drawable mBadgeBackground;

    /**
     * 浮标内边距
     */
    private float mBadgePaddingLeft, mBadgePaddingTop, mBadgePaddingRight, mBadgePaddingBottom;

    /**
     * 浮标外边距
     */
    private float mBadgeMarginLeft, mBadgeMarginTop, mBadgeMarginRight, mBadgeMarginBottom;

    /**
     * 浮标对齐方式
     */
    private int mBadgeGravity;

    /**
     * dp&sp转px的系数
     */
    private float mDensityDP, mDensitySP;

    /**
     * 文本画笔工具
     */
    private TextPaint mBadgeTextPaint;

    /**
     * 文本画笔工具的度量
     */
    private Paint.FontMetrics mBadgeTextFontMetrics;

    /**
     * 浮标背景边界值
     */
    private RectF mBadgeBackgroundBounds;

    /**
     * 文本高度
     */
    private float mBadgeTextHeight;

    /**
     * 目标View的宽高
     */
    private int mViewWidth, mViewHeight;

    public BadgeProxy(View view, Context context, AttributeSet attributeSet) {
        //IDE编辑模式下，显示预览效果
        if (view.isInEditMode()) {
            mBadgeText = "99+";
        }
        //初始化变量
        initBadge(context, attributeSet);
    }

    /**
     * 初始化变量
     */
    private void initBadge(Context context, AttributeSet attributeSet) {
        mDensityDP = context.getResources().getDisplayMetrics().density;//DP密度
        mDensitySP = context.getResources().getDisplayMetrics().scaledDensity;//SP密度

        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.Badge);
        //浮标对齐方式
        this.mBadgeGravity = typedArray.getInt(R.styleable.Badge_gravity_badge, Badge.GRAVITY_TOP | Badge.GRAVITY_RIGHT);
        //浮标背景
        this.mBadgeBackground = typedArray.getDrawable(R.styleable.Badge_background_badge);
        //无背景则设置默认红色圆角背景
        if (mBadgeBackground == null) {
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setColor(Color.RED);
            gradientDrawable.setCornerRadius(9999);
            mBadgeBackground = gradientDrawable;
        }
        //浮标内边距
        float badgePadding = typedArray.getDimension(R.styleable.Badge_padding_badge, -1);
        //浮标内左边距，默认值4dp
        this.mBadgePaddingLeft = typedArray.getDimension(R.styleable.Badge_paddingLeft_badge, badgePadding < 0 ? 4 * mDensityDP : badgePadding);
        //浮标内上边距，默认值2dp
        this.mBadgePaddingTop = typedArray.getDimension(R.styleable.Badge_paddingTop_badge, badgePadding < 0 ? 2 * mDensityDP : badgePadding);
        //浮标内右边距，默认值4dp
        this.mBadgePaddingRight = typedArray.getDimension(R.styleable.Badge_paddingRight_badge, badgePadding < 0 ? 4 * mDensityDP : badgePadding);
        //浮标内下边距，默认值2dp
        this.mBadgePaddingBottom = typedArray.getDimension(R.styleable.Badge_paddingBottom_badge, badgePadding < 0 ? 2 * mDensityDP : badgePadding);

        //浮标外边距
        float badgeMargin = typedArray.getDimension(R.styleable.Badge_margin_badge, 0);
        //浮标外左边距，默认值0dp
        this.mBadgeMarginLeft = typedArray.getDimension(R.styleable.Badge_marginLeft_badge, badgeMargin);
        //浮标外上边距，默认值0dp
        this.mBadgeMarginTop = typedArray.getDimension(R.styleable.Badge_marginTop_badge, badgeMargin);
        //浮标外右边距，默认值0dp
        this.mBadgeMarginRight = typedArray.getDimension(R.styleable.Badge_marginRight_badge, badgeMargin);
        //浮标外下边距，默认值0dp
        this.mBadgeMarginBottom = typedArray.getDimension(R.styleable.Badge_marginBottom_badge, badgeMargin);

        //字体大小
        this.mBadgeTextSize = typedArray.getDimension(R.styleable.Badge_textSize_badge, 10 * mDensitySP);
        //字体颜色
        this.mBadgeTextColor = typedArray.getColor(R.styleable.Badge_textColor_badge, 0xFFFFFFFF);
        //字体加粗
        this.mBoldTextEnable = typedArray.getBoolean(R.styleable.Badge_boldText_badge, true);

        typedArray.recycle();

        //初始化画笔工具
        initTextPaint();
        //计算行高
        measureTextHeight();
        mBadgeBackgroundBounds = new RectF();
    }

    /**
     * 初始化画笔工具
     */
    private void initTextPaint() {
        mBadgeTextPaint = new TextPaint();
        mBadgeTextPaint.setDither(true);//防抖动
        mBadgeTextPaint.setAntiAlias(true);//抗锯齿
        mBadgeTextPaint.setLinearText(true);//不要文本缓存
        mBadgeTextPaint.setSubpixelText(true);//设置亚像素
        mBadgeTextPaint.setColor(mBadgeTextColor);//设置字体颜色
        mBadgeTextPaint.setTextSize(mBadgeTextSize);//设置字体大小
        mBadgeTextPaint.setTextAlign(Paint.Align.CENTER);//文本居中
        mBadgeTextPaint.setFakeBoldText(mBoldTextEnable);//字体加粗
    }

    /**
     * 计算行高
     */
    private void measureTextHeight() {
        mBadgeTextFontMetrics = mBadgeTextPaint.getFontMetrics();
        mBadgeTextHeight = Math.abs(mBadgeTextFontMetrics.descent - mBadgeTextFontMetrics.ascent);
    }

    /**
     * 在目标View的onSizeChanged方法中调用，记录目标View的宽高属性
     */
    public void onSizeChanged(int width, int height) {
        mViewWidth = width;
        mViewHeight = height;
        measureBackgroundBounds();
    }

    /**
     * 返回浮标文本
     */
    public String getBadgeText() {
        return mBadgeText;
    }

    /**
     * 设置浮标文本
     *
     * @param badgeText 为null时不显示，长度为0时显示小圆点
     */
    public void setBadgeText(String badgeText) {
        mBadgeText = badgeText;
        measureBackgroundBounds();
    }

    /**
     * 设置浮标字体颜色
     */
    public void setBadgeTextColor(int color) {
        mBadgeTextColor = color;
        mBadgeTextPaint.setColor(mBadgeTextColor);
    }

    /**
     * 设置浮标字体大小，单位SP
     */
    public void setBadgeTextSize(float size) {
        mBadgeTextSize = size * mDensitySP;
        mBadgeTextPaint.setTextSize(mBadgeTextSize);
        measureTextHeight();
        measureBackgroundBounds();
    }

    /**
     * 设置是否字体加粗
     */
    public void setBadgeBoldText(boolean boldEnable) {
        mBoldTextEnable = boldEnable;
        mBadgeTextPaint.setFakeBoldText(mBoldTextEnable);
        measureTextHeight();
        measureBackgroundBounds();
    }

    /**
     * 设置浮标背景
     */
    public void setBadgeBackground(Drawable drawable) {
        mBadgeBackground = drawable;
    }

    /**
     * 设置浮标内边距，单位DP
     */
    public void setBadgePadding(float padding) {
        mBadgePaddingLeft = mBadgePaddingTop = mBadgePaddingRight = mBadgePaddingBottom = padding * mDensityDP;
        measureBackgroundBounds();
    }

    /**
     * 设置浮标内边距，单位DP
     */
    public void setBadgePadding(Float paddingLeft, Float paddingTop, Float paddingRight, Float paddingBottom) {
        if (paddingLeft != null) mBadgePaddingLeft = paddingLeft * mDensityDP;
        if (paddingTop != null) mBadgePaddingTop = paddingTop * mDensityDP;
        if (paddingRight != null) mBadgePaddingRight = paddingRight * mDensityDP;
        if (paddingBottom != null) mBadgePaddingBottom = paddingBottom * mDensityDP;
        measureBackgroundBounds();
    }

    /**
     * 设置浮标外边距，单位DP
     */
    public void setBadgeMargin(float margin) {
        mBadgeMarginLeft = mBadgeMarginTop = mBadgeMarginRight = mBadgeMarginBottom = margin * mDensityDP;
        measureBackgroundBounds();
    }

    /**
     * 设置浮标外边距，单位DP
     */
    public void setBadgeMargin(Float marginLeft, Float marginTop, Float marginRight, Float marginBottom) {
        if (marginLeft != null) mBadgeMarginLeft = marginLeft * mDensityDP;
        if (marginTop != null) mBadgeMarginTop = marginTop * mDensityDP;
        if (marginRight != null) mBadgeMarginRight = marginRight * mDensityDP;
        if (marginBottom != null) mBadgeMarginBottom = marginBottom * mDensityDP;
        measureBackgroundBounds();
    }

    /**
     * 设置浮标对齐方式
     */
    public void setBadgeGravity(int gravity) {
        mBadgeGravity = gravity;
        measureBackgroundBounds();
    }

    /**
     * 返回浮标对齐方式
     */
    public int getBadgeGravity() {
        return mBadgeGravity;
    }

    /**
     * 在目标View的dispatchDraw方法中最后调用，绘制浮标
     * 不建议在onDraw中调用，因为可能会被目标View的前景遮挡，且ViewGroup在透明背景时不会调用onDraw
     */
    public void dispatchDraw(Canvas canvas) {
        if (mBadgeText == null) {
            return;
        }
        //绘制背景
        if (mBadgeBackground != null) {
            int left = (int) mBadgeBackgroundBounds.left;
            int top = (int) mBadgeBackgroundBounds.top;
            int right = (int) mBadgeBackgroundBounds.right;
            int bottom = (int) mBadgeBackgroundBounds.bottom;
            mBadgeBackground.setBounds(left, top, right, bottom);
            mBadgeBackground.draw(canvas);
        }
        //绘制文本
        if (mBadgeText.length() > 0) {
            //计算文本对齐坐标
            float baseX = mBadgeBackgroundBounds.left + (mBadgeBackgroundBounds.width() + mBadgePaddingLeft - mBadgePaddingRight) / 2F;
            float baseY = mBadgeBackgroundBounds.top + (mBadgeBackgroundBounds.height() + mBadgePaddingTop - mBadgePaddingBottom - mBadgeTextFontMetrics.bottom - mBadgeTextFontMetrics.top) / 2F;
            canvas.drawText(mBadgeText, baseX, baseY, mBadgeTextPaint);
        }
    }

    /**
     * 计算浮标背景尺寸
     */
    private void measureBackgroundBounds() {
        if (mBadgeText == null) {
            return;
        }
        float offsetX = mBadgeMarginLeft, offsetY = mBadgeMarginTop;
        float backgroundWidth, backgroundHeight;
        //先计算浮标的宽高
        if (mBadgeText.length() < 1) {
            //文本长度为0，只显示一个小圆点
            backgroundWidth = backgroundHeight = 2 * mDensityDP + (mBadgePaddingTop + mBadgePaddingBottom + mBadgePaddingLeft + mBadgePaddingRight) / 2F;
        } else {
            //根据文本长度计算浮标的宽高
            backgroundHeight = mBadgePaddingTop + mBadgePaddingBottom + mBadgeTextHeight;
            float badgeTextWidth = mBadgeTextPaint.measureText(mBadgeText);
            //浮标的宽不能小于高
            backgroundWidth = Math.max(backgroundHeight, mBadgePaddingLeft + mBadgePaddingRight + badgeTextWidth);
        }

        //计算水平方向的偏移量
        if ((mBadgeGravity & Badge.GRAVITY_LEFT) == Badge.GRAVITY_LEFT) {//水平居左
//            offsetX = mBadgeMarginLeft;
        } else if ((mBadgeGravity & Badge.GRAVITY_RIGHT) == Badge.GRAVITY_RIGHT) {//水平居右
            offsetX = mViewWidth - backgroundWidth - mBadgeMarginRight;
        } else if ((mBadgeGravity & Badge.GRAVITY_CENTER) == Badge.GRAVITY_CENTER) {//水平居中
            offsetX = (mViewWidth - backgroundWidth) / 2 + mBadgeMarginLeft - mBadgeMarginRight;
        }
        //计算垂直方向的偏移量
        if ((mBadgeGravity & Badge.GRAVITY_TOP) == Badge.GRAVITY_TOP) {//垂直居上
//            offsetY = mBadgeMarginTop;
        } else if ((mBadgeGravity & Badge.GRAVITY_BOTTOM) == Badge.GRAVITY_BOTTOM) {//垂直居下
            offsetY = mViewHeight - backgroundHeight - mBadgeMarginBottom;
        } else if ((mBadgeGravity & Badge.GRAVITY_CENTER) == Badge.GRAVITY_CENTER) {//垂直居中
            offsetY = (mViewHeight - backgroundHeight) / 2 + mBadgeMarginTop - mBadgeMarginBottom;
        }
        //设置浮标的边界值
        mBadgeBackgroundBounds.set(offsetX, offsetY, backgroundWidth + offsetX, backgroundHeight + offsetY);
    }

    /**
     * 返回浮标总宽
     */
    public int getBadgeWidth() {
        return (int) Math.ceil(mBadgeMarginLeft + mBadgeMarginRight + mBadgeBackgroundBounds.width());
    }

    /**
     * 返回浮标总高
     */
    public int getBadgeHeight() {
        return (int) Math.ceil(mBadgeMarginTop + mBadgeMarginBottom + mBadgeBackgroundBounds.height());
    }

}
