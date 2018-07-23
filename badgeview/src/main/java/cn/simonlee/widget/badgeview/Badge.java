package cn.simonlee.widget.badgeview;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.DrawableRes;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-05-29
 * <p>
 * 用法：
 * 1.自定义View，在构造方法中创建Badge实例;
 * 2.重写dispatchDraw(Canvas canvas)方法，在里面调用Badge.dispatchDraw(canvas);
 * 3.添加一个getBadge()方法，返回Badge实例。
 */
@SuppressWarnings("unused")
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
     * 角标外边距
     */
    private float mBadgeMarginLeft, mBadgeMarginTop, mBadgeMarginRight, mBadgeMarginBottom;

    /**
     * 角标小圆点半径，默认值10dp
     */
    private float mBadgeDotRadius;

    /**
     * 角标对齐方式
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
     * 角标背景边界值
     */
    private RectF mBadgeBackgroundBounds;

    /**
     * 文本高度
     */
    private float mBadgeTextHeight;

    /**
     * 目标View的宽高值
     */
    private int mViewWidth, mViewHeight;

    /**
     * 目标View的布局宽高
     */
    private int mLayoutWidth, mLayoutHeight;

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
        //IDE编辑模式下，显示预览效果
        if (mTargetView.isInEditMode()) {
            mBadgeText = "99+";
        }
        //初始化变量
        initBadge(mTargetView.getContext(), attributeSet);
    }

    /**
     * 初始化变量
     */
    private void initBadge(Context context, AttributeSet attributeSet) {
        mDensityDP = context.getResources().getDisplayMetrics().density;//DP密度
        mDensitySP = context.getResources().getDisplayMetrics().scaledDensity;//SP密度

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

        //角标外边距
        float badgeMargin = typedArray.getDimension(R.styleable.Badge_badge_margin, 0);
        //角标外左边距，默认值0dp
        this.mBadgeMarginLeft = typedArray.getDimension(R.styleable.Badge_badge_marginLeft, badgeMargin);
        //角标外上边距，默认值0dp
        this.mBadgeMarginTop = typedArray.getDimension(R.styleable.Badge_badge_marginTop, badgeMargin);
        //角标外右边距，默认值0dp
        this.mBadgeMarginRight = typedArray.getDimension(R.styleable.Badge_badge_marginRight, badgeMargin);
        //角标外下边距，默认值0dp
        this.mBadgeMarginBottom = typedArray.getDimension(R.styleable.Badge_badge_marginBottom, badgeMargin);

        //字体大小
        this.mBadgeTextSize = typedArray.getDimension(R.styleable.Badge_badge_textSize, 12 * mDensitySP);
        //字体颜色
        this.mBadgeTextColor = typedArray.getColor(R.styleable.Badge_badge_textColor, 0xFFFFFFFF);
        //字体加粗
        this.mBoldTextEnable = typedArray.getBoolean(R.styleable.Badge_badge_boldText, true);

        //角标小圆点半径
        this.mBadgeDotRadius = typedArray.getDimension(R.styleable.Badge_badge_dotRadius, 10 * mDensityDP);
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
            if (mLayoutWidth == ViewGroup.LayoutParams.WRAP_CONTENT || mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
                mTargetView.requestLayout();
            } else {
                mTargetView.invalidate();
            }
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
            if (mLayoutWidth == ViewGroup.LayoutParams.WRAP_CONTENT || mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
                mTargetView.requestLayout();
            } else {
                mTargetView.invalidate();
            }
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
            if (mLayoutWidth == ViewGroup.LayoutParams.WRAP_CONTENT || mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
                mTargetView.requestLayout();
            } else {
                mTargetView.invalidate();
            }
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
     * 设置角标内边距，单位px
     */
    public void setBadgePadding(float padding) {
        if (mBadgePaddingLeft != padding || mBadgePaddingTop != padding || mBadgePaddingRight != padding || mBadgePaddingBottom != padding) {
            mBadgePaddingLeft = mBadgePaddingTop = mBadgePaddingRight = mBadgePaddingBottom = padding;
            measureBackgroundBounds();
            if (mLayoutWidth == ViewGroup.LayoutParams.WRAP_CONTENT || mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
                mTargetView.requestLayout();
            } else {
                mTargetView.invalidate();
            }
        }
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
            if (mLayoutWidth == ViewGroup.LayoutParams.WRAP_CONTENT || mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
                mTargetView.requestLayout();
            } else {
                mTargetView.invalidate();
            }
        }
    }

    /**
     * 设置角标内边距-左，单位px
     */
    public void setBadgePaddingLeft(float paddingLeft) {
        if (mBadgePaddingLeft != paddingLeft) {
            mBadgePaddingLeft = paddingLeft;
            measureBackgroundBounds();
            if (mLayoutWidth == ViewGroup.LayoutParams.WRAP_CONTENT || mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
                mTargetView.requestLayout();
            } else {
                mTargetView.invalidate();
            }
        }
    }

    /**
     * 设置角标内边距-上，单位px
     */
    public void setBadgePaddingTop(float paddingTop) {
        if (mBadgePaddingTop != paddingTop) {
            mBadgePaddingTop = paddingTop;
            measureBackgroundBounds();
            if (mLayoutWidth == ViewGroup.LayoutParams.WRAP_CONTENT || mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
                mTargetView.requestLayout();
            } else {
                mTargetView.invalidate();
            }
        }
    }

    /**
     * 设置角标内边距-右，单位px
     */
    public void setBadgePaddingRight(float paddingRight) {
        if (mBadgePaddingRight != paddingRight) {
            mBadgePaddingRight = paddingRight;
            measureBackgroundBounds();
            if (mLayoutWidth == ViewGroup.LayoutParams.WRAP_CONTENT || mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
                mTargetView.requestLayout();
            } else {
                mTargetView.invalidate();
            }
        }
    }

    /**
     * 设置角标内边距-下，单位px
     */
    public void setBadgePaddingBottom(float paddingBottom) {
        if (mBadgePaddingBottom != paddingBottom) {
            mBadgePaddingBottom = paddingBottom;
            measureBackgroundBounds();
            if (mLayoutWidth == ViewGroup.LayoutParams.WRAP_CONTENT || mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
                mTargetView.requestLayout();
            } else {
                mTargetView.invalidate();
            }
        }
    }

    /**
     * 设置角标外边距，单位px
     */
    public void setBadgeMargin(float marginLeft, float marginTop, float marginRight, float marginBottom) {
        if (mBadgeMarginLeft != marginLeft || mBadgeMarginTop != marginTop || mBadgeMarginRight != marginRight || mBadgeMarginBottom != marginBottom) {
            mBadgeMarginLeft = marginLeft;
            mBadgeMarginTop = marginTop;
            mBadgeMarginRight = marginRight;
            mBadgeMarginBottom = marginBottom;
            measureBackgroundBounds();
            if (mLayoutWidth == ViewGroup.LayoutParams.WRAP_CONTENT || mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
                mTargetView.requestLayout();
            } else {
                mTargetView.invalidate();
            }
        }
    }

    /**
     * 设置角标外边距-左，单位px
     */
    public void setBadgeMarginLeft(float marginLeft) {
        if (mBadgeMarginLeft != marginLeft) {
            mBadgeMarginLeft = marginLeft;
            measureBackgroundBounds();
            if (mLayoutWidth == ViewGroup.LayoutParams.WRAP_CONTENT || mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
                mTargetView.requestLayout();
            } else {
                mTargetView.invalidate();
            }
        }
    }

    /**
     * 设置角标外边距-上，单位px
     */
    public void setBadgeMarginTop(float marginTop) {
        if (mBadgeMarginTop != marginTop) {
            mBadgeMarginTop = marginTop;
            measureBackgroundBounds();
            if (mLayoutWidth == ViewGroup.LayoutParams.WRAP_CONTENT || mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
                mTargetView.requestLayout();
            } else {
                mTargetView.invalidate();
            }
        }
    }

    /**
     * 设置角标外边距-右，单位px
     */
    public void setBadgeMarginRight(float marginRight) {
        if (mBadgeMarginRight != marginRight) {
            mBadgeMarginRight = marginRight;
            measureBackgroundBounds();
            if (mLayoutWidth == ViewGroup.LayoutParams.WRAP_CONTENT || mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
                mTargetView.requestLayout();
            } else {
                mTargetView.invalidate();
            }
        }
    }

    /**
     * 设置角标外边距-下，单位px
     */
    public void setBadgeMarginBottom(float marginBottom) {
        if (mBadgeMarginBottom != marginBottom) {
            mBadgeMarginBottom = marginBottom;
            measureBackgroundBounds();
            if (mLayoutWidth == ViewGroup.LayoutParams.WRAP_CONTENT || mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
                mTargetView.requestLayout();
            } else {
                mTargetView.invalidate();
            }
        }
    }

    /**
     * 设置小圆点半径，单位px
     *
     * @param dotRadius 必须大于0
     */
    public void setBadgeDotRadius(float dotRadius) {
        if (dotRadius > 0 && mBadgeDotRadius != dotRadius) {
            mBadgeDotRadius = dotRadius;
            if (mBadgeText != null && mBadgeText.length() < 1) {
                if (mLayoutWidth == ViewGroup.LayoutParams.WRAP_CONTENT || mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
                    mTargetView.requestLayout();
                } else {
                    mTargetView.invalidate();
                }
            }
        }
    }

    /**
     * 设置角标对齐方式
     */
    public void setBadgeGravity(int gravity) {
        if (mLayoutWidth == ViewGroup.LayoutParams.WRAP_CONTENT) {
            gravity |= GRAVITY_LEFT;//宽度自适应时，强制左对齐
        }
        if (mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
            gravity |= GRAVITY_TOP;//高度自适应时，强制上对齐
        }
        if (mBadgeGravity != gravity) {
            mBadgeGravity = gravity;
            measureBackgroundBounds();
            mTargetView.invalidate();
        }
    }

    /**
     * 返回角标对齐方式
     */
    public int getBadgeGravity() {
        return mBadgeGravity;
    }

    /**
     * 在目标View的dispatchDraw方法中最后调用，绘制角标
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
     * 计算角标背景尺寸
     */
    private void measureBackgroundBounds() {
        if (mBadgeText == null) {
            return;
        }
        float offsetX = mBadgeMarginLeft, offsetY = mBadgeMarginTop;
        float backgroundWidth, backgroundHeight;
        //先计算角标的宽高
        if (mBadgeText.length() < 1) {
            //文本长度为0，只显示一个小圆点
            backgroundWidth = backgroundHeight = mBadgeDotRadius;
        } else {
            //根据文本长度计算角标的宽高
            backgroundHeight = mBadgePaddingTop + mBadgePaddingBottom + mBadgeTextHeight;
            float badgeTextWidth = mBadgeTextPaint.measureText(mBadgeText);
            //角标的宽不能小于高
            backgroundWidth = Math.max(backgroundHeight, mBadgePaddingLeft + mBadgePaddingRight + badgeTextWidth);
        }

        //计算水平方向的偏移量
        if ((mBadgeGravity & GRAVITY_LEFT) == GRAVITY_LEFT) {//水平居左
//            offsetX = mBadgeMarginLeft;
        } else if ((mBadgeGravity & GRAVITY_RIGHT) == GRAVITY_RIGHT) {//水平居右
            offsetX = mViewWidth - backgroundWidth - mBadgeMarginRight;
        } else if ((mBadgeGravity & GRAVITY_CENTER) == GRAVITY_CENTER) {//水平居中
            offsetX = (mViewWidth - backgroundWidth) / 2 + mBadgeMarginLeft - mBadgeMarginRight;
        }
        //计算垂直方向的偏移量
        if ((mBadgeGravity & GRAVITY_TOP) == GRAVITY_TOP) {//垂直居上
//            offsetY = mBadgeMarginTop;
        } else if ((mBadgeGravity & GRAVITY_BOTTOM) == GRAVITY_BOTTOM) {//垂直居下
            offsetY = mViewHeight - backgroundHeight - mBadgeMarginBottom;
        } else if ((mBadgeGravity & GRAVITY_CENTER) == GRAVITY_CENTER) {//垂直居中
            offsetY = (mViewHeight - backgroundHeight) / 2 + mBadgeMarginTop - mBadgeMarginBottom;
        }
        //设置角标的边界值
        mBadgeBackgroundBounds.set(offsetX, offsetY, backgroundWidth + offsetX, backgroundHeight + offsetY);
    }

    /**
     * 设置宽高属性
     */
    void setLayoutParams(int layoutWidth, int layoutHeight) {
        mLayoutWidth = layoutWidth;
        mLayoutHeight = layoutHeight;
        setBadgeGravity(mBadgeGravity);
    }

    /**
     * 返回角标总宽
     */
    int getBadgeWidth() {
        return (int) Math.ceil(mBadgeMarginLeft + mBadgeMarginRight + mBadgeBackgroundBounds.width());
    }

    /**
     * 返回角标总高
     */
    int getBadgeHeight() {
        return (int) Math.ceil(mBadgeMarginTop + mBadgeMarginBottom + mBadgeBackgroundBounds.height());
    }

}
