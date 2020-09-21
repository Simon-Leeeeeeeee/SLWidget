package cn.simonlee.widget.watermark;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

/**
 * 用法：
 * <p>
 * 1.自定义View，实现IWaterMark接口；
 * 2.在构造方法中创建WaterMark实例，在getWaterMark()方法中返回WaterMark实例；
 * 3.重写dispatchDraw(Canvas canvas)方法，调用WaterMark的drawWaterMark(canvas)方法。
 *
 * <p>
 * 自定义属性：
 * watermark_text                水印文本
 * watermark_textSize            水印字体大小
 * watermark_textColor           水印字体颜色
 * <p>
 * watermark_textBackgroundColor 水印底色
 * watermark_degrees             水印旋转角度
 * <p>
 * watermark_density             水印密度，取值0-1
 * <p>
 * watermark_rowSpacing          水印行距
 * watermark_columnSpacing       水印列距
 * watermark_maxSpacing_row      水印最大行距
 * watermark_minSpacing_row      水印最小行距
 * watermark_maxSpacing_column   水印最大列距
 * watermark_minSpacing_column   水印最小列距
 * <p>
 * watermark_drawPadding         水印区域边距
 * watermark_drawPaddingLeft     水印区域左边距
 * watermark_drawPaddingTop      水印区域上边距
 * watermark_drawPaddingRight    水印区域右边距
 * watermark_drawPaddingBottom   水印区域下边距
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2019-06-15
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class WaterMark {

    /**
     * 目标View
     */
    private final View mTargetView;

    /**
     * 水印画笔
     */
    private Paint mTextPaint;

    /**
     * 水印背景色画笔
     */
    private Paint mTextBackgroundPaint;

    /**
     * 水印文本
     */
    private String mWaterMarkText;

    /**
     * 水印字体大小
     */
    private float mTextSize;

    /**
     * 水印字体颜色
     */
    private int mTextColor;

    /**
     * 水印背景色
     */
    private int mTextBackgroundColor;

    /**
     * 水印倾斜角度
     */
    private int mDegrees = -30;

    /**
     * 水印文本宽度
     */
    private float mTextWidth;

    /**
     * 水印文本高度
     */
    private float mTextHeight;

    /**
     * 水印行距
     */
    private int mRowSpacing;

    /**
     * 水印列距
     */
    private int mColumnSpacing;

    /**
     * 水印密度
     * 当{@link #mRowSpacing}或{@link #mColumnSpacing}未指定时生效
     * 受{@link #mMinRowSpacing}、{@link #mMaxRowSpacing}、{@link #mMinColumnSpacing}、{@link #mMaxColumnSpacing}约束
     */
    private float mDensity;

    /**
     * 最小行距，用于约束{@link #mDensity}
     */
    private float mMinRowSpacing;

    /**
     * 最大行距，用于约束{@link #mDensity}
     */
    private float mMaxRowSpacing;

    /**
     * 最小列距，用于约束{@link #mDensity}
     */
    private float mMinColumnSpacing;

    /**
     * 最大列距，用于约束{@link #mDensity}
     */
    private float mMaxColumnSpacing;

    /**
     * 水印区域上边距
     */
    private int mDrawPaddingTop;

    /**
     * 水印区域左边距
     */
    private int mDrawPaddingLeft;

    /**
     * 水印区域右边距
     */
    private int mDrawPaddingRight;

    /**
     * 水印区域下边距
     */
    private int mDrawPaddingBottom;

    /**
     * 文本基线高度（用于计算文本绘制点）
     */
    private float mTextBaseLine;

    /**
     * 是否开启水印
     */
    private boolean isEnabled = true;

    public WaterMark(View targetView, AttributeSet attributeSet, int defStyleAttr) {
        this.mTargetView = targetView;
        //初始化
        initWaterMark(mTargetView.getContext(), attributeSet, defStyleAttr);
        //IDE编辑模式下，显示预览效果
        if (mTargetView.isInEditMode()) {
            mWaterMarkText = "example";
        }
    }

    /**
     * 初始化相关自定义属性设置
     */
    private void initWaterMark(Context context, AttributeSet attributeSet, int defStyleAttr) {
        //dp转px的系数
        float densityDP = context.getResources().getDisplayMetrics().density;
        //sp转px的系数
        float densitySP = context.getResources().getDisplayMetrics().scaledDensity;

        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.WaterMark, defStyleAttr, 0);

        //水印文本
        this.mWaterMarkText = typedArray.getString(R.styleable.WaterMark_watermark_text);
        //水印字号
        this.mTextSize = typedArray.getDimension(R.styleable.WaterMark_watermark_textSize, 14 * densitySP);
        //水印字色
        this.mTextColor = typedArray.getColor(R.styleable.WaterMark_watermark_textColor, 0x11000000);
        //水印底色
        this.mTextBackgroundColor = typedArray.getColor(R.styleable.WaterMark_watermark_textBackgroundColor, Color.TRANSPARENT);
        //水印旋转角度
        this.mDegrees = typedArray.getInt(R.styleable.WaterMark_watermark_degrees, -30);

        //水印密度
        this.mDensity = typedArray.getFloat(R.styleable.WaterMark_watermark_density, 0.9F);

        //行距
        this.mRowSpacing = typedArray.getDimensionPixelSize(R.styleable.WaterMark_watermark_rowSpacing, -1);
        //列距
        this.mColumnSpacing = typedArray.getDimensionPixelSize(R.styleable.WaterMark_watermark_columnSpacing, -1);

        //最小行距
        this.mMinRowSpacing = typedArray.getDimension(R.styleable.WaterMark_watermark_minSpacing_row, 40 * densityDP);
        //最大行距
        this.mMaxRowSpacing = typedArray.getDimension(R.styleable.WaterMark_watermark_maxSpacing_row, 100 * densityDP);

        //最小列距
        this.mMinColumnSpacing = typedArray.getDimension(R.styleable.WaterMark_watermark_minSpacing_column, 40 * densityDP);
        //最大列距
        this.mMaxColumnSpacing = typedArray.getDimension(R.styleable.WaterMark_watermark_maxSpacing_column, 150 * densityDP);

        //角标内边距
        int drawPadding = typedArray.getDimensionPixelSize(R.styleable.WaterMark_watermark_drawPadding, -1);
        //角标内左边距
        this.mDrawPaddingLeft = typedArray.getDimensionPixelSize(R.styleable.WaterMark_watermark_drawPaddingLeft, drawPadding);
        //角标内上边距
        this.mDrawPaddingTop = typedArray.getDimensionPixelSize(R.styleable.WaterMark_watermark_drawPaddingTop, drawPadding);
        //角标内右边距
        this.mDrawPaddingRight = typedArray.getDimensionPixelSize(R.styleable.WaterMark_watermark_drawPaddingRight, drawPadding);
        //角标内下边距
        this.mDrawPaddingBottom = typedArray.getDimensionPixelSize(R.styleable.WaterMark_watermark_drawPaddingBottom, drawPadding);

        typedArray.recycle();
        //初始化水印画笔
        initTextPaint();
        //初始化水印背景色画笔
        initTextBackgroundPaint();
    }

    /**
     * 初始化水印画笔，计算文本宽高、计算文本基线
     */
    private void initTextPaint() {
        mTextPaint = new Paint();
        //防抖动
        mTextPaint.setDither(true);
        //抗锯齿
        mTextPaint.setAntiAlias(true);
        //亚像素
        mTextPaint.setSubpixelText(true);
        //字体加粗
        mTextPaint.setFakeBoldText(true);

        //字体颜色
        mTextPaint.setColor(mTextColor);
        //字体大小
        mTextPaint.setTextSize(mTextSize);
        //等宽字体
        mTextPaint.setTypeface(Typeface.MONOSPACE);
        //对齐方式，左对齐
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        //计算文本的宽高
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        this.mTextHeight = Math.abs(fontMetrics.descent - fontMetrics.ascent);
        if (mWaterMarkText != null) {
            this.mTextWidth = mTextPaint.measureText(mWaterMarkText);
            //获取文本尺寸
            Rect textBounds = new Rect();//用于计算每行文本边界区域
            mTextPaint.getTextBounds(mWaterMarkText, 0, mWaterMarkText.length(), textBounds);
            //根据文本尺寸计算基线位置
            mTextBaseLine = (mTextHeight - textBounds.top - textBounds.bottom) / 2F;
        }
    }

    /**
     * 初始化水印背景色画笔
     */
    private void initTextBackgroundPaint() {
        mTextBackgroundPaint = new Paint();
        //防抖动
        mTextBackgroundPaint.setDither(true);
        //抗锯齿
        mTextBackgroundPaint.setAntiAlias(true);
        //亚像素
        mTextBackgroundPaint.setSubpixelText(true);
        //设置颜色
        mTextBackgroundPaint.setColor(mTextBackgroundColor);
    }

    /**
     * 绘制水印
     * 在目标View的dispatchDraw方法中最后调用，保证水印在最顶层显示
     */
    public void drawWaterMark(Canvas canvas) {
        if (!isEnabled || mWaterMarkText == null || mWaterMarkText.length() < 1) {
            return;
        }

        //计算水印区域尺寸
        final int width = mTargetView.getWidth() - mDrawPaddingLeft - mDrawPaddingRight;
        final int height = mTargetView.getHeight() - mDrawPaddingTop - mDrawPaddingBottom;

        if (width < 1 || height < 1) {
            return;
        }

        //保存画布状态
        canvas.save();

        //根据drawpadding校正画布顶点坐标
        canvas.translate(mDrawPaddingLeft, mDrawPaddingTop);

        //去除水印区域外部的绘制
        canvas.clipRect(0, 0, width, height);

        //将旋转角度限定在[-90,90)范围内
        mDegrees = ((mDegrees - 90) % 180 + 180) % 180 - 90;

        //旋转画布
        canvas.rotate(mDegrees);
        //旋转角度转换为弧度
        final double radians = Math.abs(Math.toRadians(mDegrees));

        /*
         * 画布的顶点默认为左上角(0, 0)，经过绕顶点旋转后，画布无法完全覆盖整个View
         * 为了确保画布恰好覆盖住整个View，需要重新计算画布尺寸，以及确立新的顶点坐标
         * */

        //重新计算宽高尺寸
        final float canvasWidth = (float) (Math.sin(radians) * height + Math.cos(radians) * width);
        final float canvasHeight = (float) (Math.sin(radians) * width + Math.cos(radians) * height);

        //确认新的顶点坐标
        final float originX = (mDegrees >= 0) ? 0 : (float) (-Math.sin(radians) * height);
        final float originY = (mDegrees >= 0) ? (float) (-Math.sin(radians) * width) : 0;
        //平移画布，使顶点坐标由(originX, originY)变为(0, 0),便于后续计算
        canvas.translate(originX, originY);

        //获取真实行距
        final float realRowSpacing = getRealRowSpacing(canvasWidth);
        //获取真实列距
        final float realColumnSpacing = getRealColumnSpacing(canvasHeight);

        /*
         * 水印应该以View的中心点为基准进行绘制，以确保当View的大小仅能容下一个水印时，这个水印是居中显示的
         * 因此需要计算第一个水印的起始绘制点，以确保必有一个水印在View的正中心
         * 此外，水印的绘制应该错位绘制，所以起始绘制点的横坐标应该区分是否错位，根据水印距中心水印的行数来判断是否错位
         * */

        //计算第一个水印的绘制点横坐标
        float startDrawX = ((canvasWidth - mTextWidth) / 2F) % (mTextWidth + realColumnSpacing);
        //计算错位行第一个水印的绘制点横坐标
        float startDrawX_jagged = ((canvasWidth + realColumnSpacing) / 2F) % (mTextWidth + realColumnSpacing);
        //对第一个水印的横坐标进行校正
        if (startDrawX > realColumnSpacing) {
            startDrawX -= mTextWidth + realColumnSpacing;
        }
        //对错位行第一个水印的横坐标进行校正
        if (startDrawX_jagged > realColumnSpacing) {
            startDrawX_jagged -= mTextWidth + realColumnSpacing;
        }

        //计算第一个水印的绘制点纵坐标
        float startDrawY = ((canvasHeight - mTextHeight) / 2F) % (mTextHeight + realRowSpacing);
        //计算第一个水印距中心水印的行数
        int row = (int) (((canvasHeight - mTextHeight) / 2F) / (mTextHeight + realRowSpacing));
        //对第一个水印的纵坐标和距中心水印的行数进行校正
        if (startDrawY > realRowSpacing) {
            startDrawY -= mTextHeight + realRowSpacing;
            row++;
        }

        //行循环：一行一行绘制
        for (float drawY = startDrawY; drawY < canvasHeight; drawY += mTextHeight + realRowSpacing) {
            //画布在绘制文本时不以文本顶部为基准进行绘制，而是以基线为准，这里要加上基线高度
            float baseY = drawY + mTextBaseLine;
            //根据行数的奇偶来判断每行的起始横坐标是否需要错位
            float drawX = row++ % 2 == 0 ? startDrawX : startDrawX_jagged;
            //列循环：在行内一个一个绘制
            for (; drawX < canvasWidth; drawX += mTextWidth + realColumnSpacing) {
                if (Color.TRANSPARENT != mTextBackgroundColor) {
                    //绘制水印背景色
                    canvas.drawRect(drawX, drawY, drawX + mTextWidth, drawY + mTextHeight, mTextBackgroundPaint);
                }
                //绘制水印文本
                canvas.drawText(mWaterMarkText, drawX, baseY, mTextPaint);
            }
        }
        //回复画布状态
        canvas.restore();
    }

    /**
     * 获取实际行距，当有指定行距值时直接返回，否则根据画布宽度&密度计算
     *
     * @param canvasWidth 画布宽度
     * @return 实际行距
     */
    private float getRealRowSpacing(float canvasWidth) {
        if (mRowSpacing >= 0) {
            return mRowSpacing;
        } else {
            final float density = mDensity < 0 ? 0 : (mDensity > 1 ? 1 : mDensity);
            float rowSpacing = (canvasWidth - mTextWidth) * (1 - density);
            if (mMinRowSpacing >= 0) {
                rowSpacing = Math.max(rowSpacing, mMinRowSpacing);
            }
            if (mMaxRowSpacing >= 0) {
                rowSpacing = Math.min(rowSpacing, mMaxRowSpacing);
            }
            return rowSpacing;
        }
    }

    /**
     * 获取实际列距，当有指定列距值时直接返回，否则根据画布高度&密度计算
     *
     * @param canvasHeight 画布高度
     * @return 实际列距
     */
    private float getRealColumnSpacing(float canvasHeight) {
        if (mColumnSpacing >= 0) {
            return mColumnSpacing;
        } else {
            final float density = mDensity < 0 ? 0 : (mDensity > 1 ? 1 : mDensity);
            float columnSpacing = (canvasHeight - mTextHeight) * (1 - density);
            if (mMinColumnSpacing >= 0) {
                columnSpacing = Math.max(columnSpacing, mMinColumnSpacing);
            }
            if (mMaxColumnSpacing >= 0) {
                columnSpacing = Math.min(columnSpacing, mMaxColumnSpacing);
            }
            return columnSpacing;
        }
    }

    /**
     * 设置水印文本，并计算宽度以及基线位置
     *
     * @param waterMark 水印文本
     */
    public void setText(String waterMark) {
        this.mWaterMarkText = waterMark;
        if (mWaterMarkText != null) {
            //计算文本的宽度
            this.mTextWidth = mTextPaint.measureText(mWaterMarkText);
            //获取文本尺寸
            Rect textBounds = new Rect();//用于计算每行文本边界区域
            mTextPaint.getTextBounds(mWaterMarkText, 0, mWaterMarkText.length(), textBounds);
            //根据文本尺寸计算基线位置
            mTextBaseLine = (mTextHeight - textBounds.top - textBounds.bottom) / 2F;
        }
    }

    /**
     * 返回水印文本
     *
     * @return 水印文本
     */
    public String getText() {
        return mWaterMarkText;
    }

    /**
     * 设置水印文本字号，并计算宽度、高度以及基线位置
     *
     * @param textSize 水印文本字号
     */
    public void setTextSize(float textSize) {
        this.mTextSize = textSize;
        mTextPaint.setTextSize(textSize);
        //计算文本的宽高
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        this.mTextHeight = Math.abs(fontMetrics.descent - fontMetrics.ascent);
        if (mWaterMarkText != null) {
            this.mTextWidth = mTextPaint.measureText(mWaterMarkText);
            //获取文本尺寸
            Rect textBounds = new Rect();//用于计算每行文本边界区域
            mTextPaint.getTextBounds(mWaterMarkText, 0, mWaterMarkText.length(), textBounds);
            //根据文本尺寸计算基线位置
            mTextBaseLine = (mTextHeight - textBounds.top - textBounds.bottom) / 2F;
        }
    }

    /**
     * 返回水印文本字号
     *
     * @return 水印文本字号
     */
    public float getTextSize() {
        return mTextSize;
    }

    /**
     * 设置水印字体颜色
     *
     * @param textColor 字体颜色
     */
    public void setTextColor(int textColor) {
        this.mTextColor = textColor;
        mTextPaint.setColor(mTextColor);
    }

    /**
     * 返回水印字体颜色
     *
     * @return 字体颜色
     */
    public int getTextColor() {
        return mTextColor;
    }

    /**
     * 设置水印背景颜色
     *
     * @param textBackgroundColor 水印背景颜色
     */
    public void setTextBackgroundColor(int textBackgroundColor) {
        this.mTextBackgroundColor = textBackgroundColor;
        mTextBackgroundPaint.setColor(mTextBackgroundColor);
    }

    /**
     * 返回水印背景颜色
     *
     * @return 水印背景颜色
     */
    public int getTextBackgroundColor() {
        return mTextBackgroundColor;
    }

    /**
     * 设置水印旋转角度
     *
     * @param degrees 旋转角度
     */
    public void setWaterMarkDegrees(int degrees) {
        this.mDegrees = degrees;
    }

    /**
     * 返回水印旋转角度
     *
     * @return 水印旋转角度
     */
    public int getWaterMarkDegrees() {
        return mDegrees;
    }

    /**
     * 返回水印行距，单位px
     *
     * @return 水印行距
     */
    public int getRowSpacing() {
        return mRowSpacing;
    }

    /**
     * 设置水印行距，单位px
     *
     * @param rowSpacing 水印行距
     */
    public void setRowSpacing(int rowSpacing) {
        this.mRowSpacing = rowSpacing;
    }

    /**
     * 返回水印列距，单位px
     *
     * @return 水印列距
     */
    public int getColumnSpacing() {
        return mColumnSpacing;
    }

    /**
     * 设置水印列距，单位px
     *
     * @param columnSpacing 水印列距
     */
    public void setColumnSpacing(int columnSpacing) {
        this.mColumnSpacing = columnSpacing;
    }

    /**
     * 返回 水印密度
     *
     * @return 水印密度
     */
    public float getDensity() {
        return mDensity;
    }

    /**
     * 设置水印密度，当{@link #mRowSpacing}或{@link #mColumnSpacing}未指定时生效
     * 受行距/列距的最大值/最小值约束
     *
     * @param density 水印密度
     */
    public void setDensity(float density) {
        this.mDensity = density;
    }

    /**
     * 返回水印最小行距，单位px
     *
     * @return 水印最小行距
     */
    public float getMinRowSpacing() {
        return mMinRowSpacing;
    }

    /**
     * 设置水印最小行距，用于约束{@link #mDensity}，单位px
     *
     * @param minRowSpacing 水印最小行距
     */
    public void setMinRowSpacing(float minRowSpacing) {
        this.mMinRowSpacing = minRowSpacing;
    }

    /**
     * 返回水印最大行距，单位px
     *
     * @return 水印最大行距
     */
    public float getMaxRowSpacing() {
        return mMaxRowSpacing;
    }

    /**
     * 设置水印最大行距，用于约束{@link #mDensity}，单位px
     *
     * @param maxRowSpacing 水印最大行距
     */
    public void setMaxRowSpacing(float maxRowSpacing) {
        this.mMaxRowSpacing = maxRowSpacing;
    }

    /**
     * 返回水印最小列距，单位px
     *
     * @return 水印最小列距
     */
    public float getMinColumnSpacing() {
        return mMinColumnSpacing;
    }

    /**
     * 设置水印最小列距，用于约束{@link #mDensity}，单位px
     *
     * @param minColumnSpacing 水印最小列距
     */
    public void setMinColumnSpacing(float minColumnSpacing) {
        this.mMinColumnSpacing = minColumnSpacing;
    }

    /**
     * 返回水印最大列距，单位px
     *
     * @return 水印最大列距
     */
    public float getMaxColumnSpacing() {
        return mMaxColumnSpacing;
    }

    /**
     * 设置水印最大列距，用于约束{@link #mDensity}，单位px
     *
     * @param maxColumnSpacing 水印最大列距
     */
    public void setMaxColumnSpacing(float maxColumnSpacing) {
        this.mMaxColumnSpacing = maxColumnSpacing;
    }

    /**
     * 返回是否开启水印
     */
    public boolean isEnabled() {
        return isEnabled;
    }

    /**
     * 设置是否开启水印
     */
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    /**
     * 设置水印区域的padding，单位px
     */
    public void setDrawPadding(int padding) {
        setDrawPadding(padding, padding, padding, padding);
    }

    /**
     * 设置水印区域的padding，单位px
     */
    public void setDrawPadding(int left, int top, int right, int bottom) {
        this.mDrawPaddingLeft = left;
        this.mDrawPaddingTop = top;
        this.mDrawPaddingRight = right;
        this.mDrawPaddingBottom = bottom;
    }

    /**
     * 设置水印区域的paddingLeft，单位px
     */
    public void setDrawPaddingLeft(int left) {
        this.mDrawPaddingLeft = left;
    }

    /**
     * 设置水印区域的paddingTop，单位px
     */
    public void setDrawPaddingTop(int top) {
        this.mDrawPaddingTop = top;
    }

    /**
     * 设置水印区域的paddingRight，单位px
     */
    public void setDrawPaddingRight(int right) {
        this.mDrawPaddingRight = right;
    }

    /**
     * 设置水印区域的paddingBottom，单位px
     */
    public void setDrawPaddingBottom(int bottom) {
        this.mDrawPaddingBottom = bottom;
    }

    /**
     * 重绘
     */
    public void invalidate() {
        this.mTargetView.invalidate();
    }

}
