package cn.simonlee.widget.badgeview;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.core.content.res.ResourcesCompat;

/**
 * 自定义角标
 * <p>
 * 用法：
 * 1.自定义View，实现IBadge接口；
 * 2.在构造方法中创建Badge实例，在getBadge()方法中返回Badge实例；
 * 3.重写{@link View#dispatchDraw(Canvas)}或{@link View#onDraw(Canvas)}方法，调用Badge的{@link #drawBadge(Canvas)}方法。
 * <p>
 * 自定义属性：
 * badge_gravity       对齐方式
 * badge_background    角标背景
 * <p>
 * badge_textSize      字体大小
 * badge_textColor     字体颜色
 * badge_boldText      文本加粗
 * <p>
 * badge_offsetX       横向偏移量
 * badge_offsetY       纵向偏移量
 * <p>
 * badge_dotRadius     小圆点半径
 * badge_padding       角标边距
 * badge_paddingLeft   角标左边距
 * badge_paddingTop    角标上边距
 * badge_paddingRight  角标右边距
 * badge_paddingBottom 角标下边距
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-05-29
 */
@SuppressWarnings({"unused", "WeakerAccess", "JavadocReference"})
public class Badge {

    /**
     * 角标相对目标View左对齐
     */
    public static final int GRAVITY_LEFT = 1;
    /**
     * 角标相对目标View顶对齐
     */
    public static final int GRAVITY_TOP = 2;
    /**
     * 角标相对目标View右对齐
     */
    public static final int GRAVITY_RIGHT = 4;
    /**
     * 角标相对目标View底对齐
     */
    public static final int GRAVITY_BOTTOM = 8;
    /**
     * 角标相对目标View居中
     */
    public static final int GRAVITY_CENTER = 16;

    /**
     * 目标View
     */
    private final View mTargetView;

    /**
     * 角标文本，为null时不显示，长度0时显示小圆点
     */
    private String mBadgeText;

    /**
     * 角标字体颜色
     */
    private int mBadgeTextColor;

    /**
     * 角标字体大小，默认值12dp
     */
    private float mBadgeTextSize;

    /**
     * 字体是否加粗
     */
    private boolean mBoldTextEnable;

    /**
     * 角标背景
     */
    private Drawable mBadgeBackground;

    /**
     * 角标内边距
     */
    private float mBadgePaddingLeft, mBadgePaddingTop, mBadgePaddingRight, mBadgePaddingBottom;

    /**
     * 角标横向偏移量
     */
    private float mBadgeOffsetX;

    /**
     * 角标纵向偏移量
     */
    private float mBadgeOffsetY;

    /**
     * 角标小圆点半径，默认值10dp
     */
    private float mBadgeDotRadius;

    /**
     * 角标对齐方式
     */
    private int mBadgeGravity;

    /**
     * 文本画笔工具
     */
    private TextPaint mBadgeTextPaint;

    /**
     * 文本画笔工具的度量
     */
    private Paint.FontMetrics mBadgeTextFontMetrics;

    /**
     * 角标背景边界值
     */
    private Rect mBadgeBackgroundBounds;

    /**
     * 文本高度
     */
    private float mBadgeTextHeight;

    /**
     * 目标View的宽高值
     */
    private int mViewWidth, mViewHeight;

    public Badge(View targetView, AttributeSet attributeSet) {
        this.mTargetView = targetView;
        mTargetView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mViewWidth = right - left;
                mViewHeight = bottom - top;
                measureBackgroundBounds();
            }
        });
        //初始化变量
        initBadge(mTargetView.getContext(), attributeSet);
        //IDE编辑模式下，显示预览效果
        if (mTargetView.isInEditMode()) {
            mBadgeText = "99+";
            measureBackgroundBounds();
        }
    }

    /**
     * 初始化变量
     */
    private void initBadge(Context context, AttributeSet attributeSet) {
        //dp&sp转px的系数
        float mDensityDP = context.getResources().getDisplayMetrics().density;
        float mDensitySP = context.getResources().getDisplayMetrics().scaledDensity;

        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.Badge);
        //角标对齐方式
        this.mBadgeGravity = typedArray.getInt(R.styleable.Badge_badge_gravity, GRAVITY_TOP | GRAVITY_RIGHT);
        //角标背景
        this.mBadgeBackground = typedArray.getDrawable(R.styleable.Badge_badge_background);
        //无背景则设置默认红色圆角背景
        if (mBadgeBackground == null) {
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setColor(Color.RED);
            gradientDrawable.setCornerRadius(9999);
            mBadgeBackground = gradientDrawable;
        }
        //角标内边距
        float badgePadding = typedArray.getDimension(R.styleable.Badge_badge_padding, -1);
        //角标内左边距，默认值4dp
        this.mBadgePaddingLeft = typedArray.getDimension(R.styleable.Badge_badge_paddingLeft, badgePadding < 0 ? 4 * mDensityDP : badgePadding);
        //角标内上边距，默认值2dp
        this.mBadgePaddingTop = typedArray.getDimension(R.styleable.Badge_badge_paddingTop, badgePadding < 0 ? 2 * mDensityDP : badgePadding);
        //角标内右边距，默认值4dp
        this.mBadgePaddingRight = typedArray.getDimension(R.styleable.Badge_badge_paddingRight, badgePadding < 0 ? 4 * mDensityDP : badgePadding);
        //角标内下边距，默认值2dp
        this.mBadgePaddingBottom = typedArray.getDimension(R.styleable.Badge_badge_paddingBottom, badgePadding < 0 ? 2 * mDensityDP : badgePadding);

        //角标横向偏移量，默认值0dp
        this.mBadgeOffsetX = typedArray.getDimension(R.styleable.Badge_badge_offsetX, 0);
        //角标横向偏移量，默认值0dp
        this.mBadgeOffsetY = typedArray.getDimension(R.styleable.Badge_badge_offsetY, 0);

        //字体大小
        this.mBadgeTextSize = typedArray.getDimension(R.styleable.Badge_badge_textSize, 12 * mDensitySP);
        //字体颜色
        this.mBadgeTextColor = typedArray.getColor(R.styleable.Badge_badge_textColor, Color.WHITE);
        //字体加粗
        this.mBoldTextEnable = typedArray.getBoolean(R.styleable.Badge_badge_boldText, true);

        //角标小圆点半径
        this.mBadgeDotRadius = typedArray.getDimension(R.styleable.Badge_badge_dotRadius, 5 * mDensityDP);
        typedArray.recycle();

        //初始化画笔工具
        initTextPaint();
        //计算行高
        measureTextHeight();
        mBadgeBackgroundBounds = new Rect();
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
     * 获取角标文本
     */
    public String getBadgeText() {
        return mBadgeText;
    }

    /**
     * 设置角标文本
     *
     * @param badgeText 为null时不显示，长度为0时显示小圆点
     */
    public void setBadgeText(String badgeText) {
        if ((mBadgeText == null && badgeText != null) || (mBadgeText != null && !mBadgeText.equals(badgeText))) {
            mBadgeText = badgeText;
            measureBackgroundBounds();
            invalidate();
        }
    }

    /**
     * 设置角标字体颜色
     */
    public void setBadgeTextColor(int color) {
        mBadgeTextColor = color;
        mBadgeTextPaint.setColor(mBadgeTextColor);
        mTargetView.invalidate();
    }

    /**
     * 设置角标字体大小，单位px
     *
     * @param size 必须大于0
     */
    public void setBadgeTextSize(float size) {
        if (size > 0 && mBadgeTextSize != size) {
            mBadgeTextSize = size;
            mBadgeTextPaint.setTextSize(mBadgeTextSize);
            measureTextHeight();
            measureBackgroundBounds();
            invalidate();
        }
    }

    /**
     * 设置是否字体加粗
     */
    public void setBadgeBoldText(boolean boldEnable) {
        if (mBoldTextEnable != boldEnable) {
            mBoldTextEnable = boldEnable;
            mBadgeTextPaint.setFakeBoldText(mBoldTextEnable);
            measureTextHeight();
            measureBackgroundBounds();
            invalidate();
        }
    }

    /**
     * 设置角标背景
     */
    public void setBadgeBackground(Drawable drawable) {
        mBadgeBackground = drawable;
        mTargetView.invalidate();
    }

    /**
     * 设置角标背景
     */
    public void setBadgeBackgroundResource(@DrawableRes int resid) {
        try {
            setBadgeBackground(ResourcesCompat.getDrawable(mTargetView.getResources(), resid, null));
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置角标偏移量
     */
    public void setBadgeOffset(float offsetX, float offsetY) {
        if (mBadgeOffsetX != offsetX || mBadgeOffsetY != offsetY) {
            mBadgeOffsetX = offsetX;
            mBadgeOffsetY = offsetY;
            measureBackgroundBounds();
            invalidate();
        }
    }

    /**
     * 设置角标横向偏移量 角标纵向偏移量
     */
    public void setBadgeOffsetX(float offsetX) {
        if (mBadgeOffsetX != offsetX) {
            mBadgeOffsetX = offsetX;
            measureBackgroundBounds();
            invalidate();
        }
    }

    /**
     * 设置角标纵向偏移量
     */
    public void setBadgeOffsetY(float offsetY) {
        if (mBadgeOffsetY != offsetY) {
            mBadgeOffsetY = offsetY;
            measureBackgroundBounds();
            invalidate();
        }
    }

    /**
     * 设置角标内边距，单位px
     */
    public void setBadgePadding(float padding) {
        setBadgePadding(padding, padding, padding, padding);
    }

    /**
     * 设置角标内边距，单位px
     */
    public void setBadgePadding(float paddingLeft, float paddingTop, float paddingRight, float paddingBottom) {
        if (mBadgePaddingLeft != paddingLeft || mBadgePaddingTop != paddingTop || mBadgePaddingRight != paddingRight || mBadgePaddingBottom != paddingBottom) {
            mBadgePaddingLeft = paddingLeft;
            mBadgePaddingTop = paddingTop;
            mBadgePaddingRight = paddingRight;
            mBadgePaddingBottom = paddingBottom;
            measureBackgroundBounds();
            invalidate();
        }
    }

    /**
     * 设置角标内边距-左，单位px
     */
    public void setBadgePaddingLeft(float paddingLeft) {
        if (mBadgePaddingLeft != paddingLeft) {
            mBadgePaddingLeft = paddingLeft;
            measureBackgroundBounds();
            invalidate();
        }
    }

    /**
     * 设置角标内边距-上，单位px
     */
    public void setBadgePaddingTop(float paddingTop) {
        if (mBadgePaddingTop != paddingTop) {
            mBadgePaddingTop = paddingTop;
            measureBackgroundBounds();
            invalidate();
        }
    }

    /**
     * 设置角标内边距-右，单位px
     */
    public void setBadgePaddingRight(float paddingRight) {
        if (mBadgePaddingRight != paddingRight) {
            mBadgePaddingRight = paddingRight;
            measureBackgroundBounds();
            invalidate();
        }
    }

    /**
     * 设置角标内边距-下，单位px
     */
    public void setBadgePaddingBottom(float paddingBottom) {
        if (mBadgePaddingBottom != paddingBottom) {
            mBadgePaddingBottom = paddingBottom;
            measureBackgroundBounds();
            invalidate();
        }
    }

    /**
     * 设置小圆点半径，单位px
     *
     * @param dotRadius 必须大于0
     */
    public void setBadgeDotRadius(int dotRadius) {
        if (dotRadius > 0 && mBadgeDotRadius != dotRadius) {
            mBadgeDotRadius = dotRadius;
            if (mBadgeText != null && mBadgeText.length() < 1) {
                measureBackgroundBounds();
                invalidate();
            }
        }
    }

    /**
     * 返回角标对齐方式
     */
    public int getBadgeGravity() {
        return mBadgeGravity;
    }

    /**
     * 设置角标对齐方式
     */
    public void setBadgeGravity(int gravity) {
        if (mBadgeGravity != gravity) {
            mBadgeGravity = gravity;
            measureBackgroundBounds();
            invalidate();
        }
    }

    /**
     * 在目标View的dispatchDraw方法中最后调用，绘制角标
     * 不建议在onDraw中调用，因为可能会被目标View的前景遮挡，且ViewGroup在透明背景时不会调用onDraw
     */
    public void drawBadge(Canvas canvas) {
        if (mBadgeText == null) {
            return;
        }
        //绘制背景
        if (mBadgeBackground != null) {
            mBadgeBackground.setBounds(mBadgeBackgroundBounds.left, mBadgeBackgroundBounds.top, mBadgeBackgroundBounds.right, mBadgeBackgroundBounds.bottom);
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
     * 计算角标背景尺寸
     */
    private void measureBackgroundBounds() {
        if (mBadgeText == null) {
            return;
        }
        int backgroundWidth, backgroundHeight;
        //确定角标的宽高
        if (mBadgeText.length() < 1) {
            //文本长度为0，只显示一个小圆点
            backgroundWidth = backgroundHeight = (int) (2F * mBadgeDotRadius + 0.5F);
        } else {
            //根据文本长度计算角标的宽高
            backgroundHeight = (int) (mBadgePaddingTop + mBadgePaddingBottom + mBadgeTextHeight + 0.5F);
            float badgeTextWidth = mBadgeTextPaint.measureText(mBadgeText);
            //角标的宽不能小于高
            backgroundWidth = Math.max(backgroundHeight, (int) (mBadgePaddingLeft + mBadgePaddingRight + badgeTextWidth + 0.5F));
        }

        int left, top, right, bottom;
        //根据对齐方式确定左右边界
        if ((mBadgeGravity & GRAVITY_LEFT) == GRAVITY_LEFT) {//水平居左
            left = Math.max(0, (int) (mBadgeOffsetX - backgroundWidth / 2F + 0.5F));
            right = left + backgroundWidth;
        } else if ((mBadgeGravity & GRAVITY_RIGHT) == GRAVITY_RIGHT) {//水平居右
            right = Math.min(mViewWidth, (int) (mViewWidth + mBadgeOffsetX + backgroundWidth / 2F + 0.5F));
            left = right - backgroundWidth;
        } else if ((mBadgeGravity & GRAVITY_CENTER) == GRAVITY_CENTER) {//水平居中
            left = Math.max(0, (int) (mViewWidth / 2F + mBadgeOffsetX - backgroundWidth / 2F + 0.5F));
            right = left + backgroundWidth;
        } else {//默认水平居左
            left = Math.max(0, (int) (mBadgeOffsetX - backgroundWidth / 2F + 0.5F));
            right = left + backgroundWidth;
        }
        //根据对齐方式确定上下边界
        if ((mBadgeGravity & GRAVITY_TOP) == GRAVITY_TOP) {//垂直居上
            top = Math.max(0, (int) (mBadgeOffsetY - backgroundHeight / 2F + 0.5F));
            bottom = top + backgroundHeight;
        } else if ((mBadgeGravity & GRAVITY_BOTTOM) == GRAVITY_BOTTOM) {//垂直居下
            bottom = Math.min(mViewHeight, (int) (mViewHeight + mBadgeOffsetY + backgroundHeight / 2F + 0.5F));
            top = bottom - backgroundHeight;
        } else if ((mBadgeGravity & GRAVITY_CENTER) == GRAVITY_CENTER) {//垂直居中
            top = Math.max(0, (int) (mViewHeight / 2F + mBadgeOffsetY - backgroundHeight / 2F + 0.5F));
            bottom = top + backgroundHeight;
        } else {//默认垂直居上
            top = Math.max(0, (int) (mBadgeOffsetY - backgroundHeight / 2F + 0.5F));
            bottom = top + backgroundHeight;
        }
        //设置角标的边界值
        mBadgeBackgroundBounds.set(left, top, right, bottom);
    }

    /**
     * 返回角标总宽
     */
    int getBadgeWidth() {
        return mBadgeBackgroundBounds.width();
    }

    /**
     * 返回角标总高
     */
    int getBadgeHeight() {
        return mBadgeBackgroundBounds.height();
    }

    /**
     * 重绘
     */
    public void invalidate() {
        if (this.mTargetView instanceof BadgeView) {
            ((BadgeView) this.mTargetView).refreshBadge();
        } else {
            this.mTargetView.invalidate();
        }
    }

}
