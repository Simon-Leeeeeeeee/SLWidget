package cn.simonlee.widget.scrollpicker;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

/**
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-05-11
 */
@SuppressWarnings("unused")
public class ScrollPickerView extends View implements ValueAnimator.AnimatorUpdateListener {

    /**
     * dp&sp转px的系数
     */
    private float mDensityDP, mDensitySP;

    /**
     * LayoutParams宽度
     */
    private int mLayoutWidth;

    /**
     * LayoutParams高度
     */
    private int mLayoutHeight;

    /**
     * 显示行数，仅高度为wrap_content时有效。默认值5
     */
    private int mTextRows;

    /**
     * 文本的行高
     */
    private float mRowHeight;

    /**
     * 文本的行距。默认4dp
     */
    private float mRowSpacing;

    /**
     * item的高度，等于mRowHeight+mRowSpacing
     */
    private float mItemHeight;

    /**
     * 字体大小。默认16sp
     */
    private float mTextSize;

    /**
     * 选中项的缩放比例。默认2
     */
    private float mTextRatio;

    /**
     * 文本格式，当宽为wrap_content时用于计算宽度
     */
    private String mTextFormat;

    /**
     * 选中项的字体颜色
     */
    private int mTextColor_Selected;

    /**
     * 未选中项的字体颜色
     */
    private int mTextColor_Unselected;

    /**
     * 是否开启循环
     */
    private boolean mLoopEnable;

    /**
     * 中部item的position
     */
    private int mMiddleItemPostion;

    /**
     * 中部item的偏移量，取值范围( -mItenHeight/2F , mItenHeight/2F ]
     */
    private float mMiddleItemOffset;

    /**
     * 绘制区域中点的Y坐标
     */
    private float mCenterY;

    /**
     * 总的累计偏移量，指针上移，position增大，偏移量增加
     */
    private float mTotalOffset;

    /**
     * 文本对齐方式
     */
    private int mGravity;

    /**
     * 文本对齐方式，居左
     */
    public static final int GRAVITY_LEFT = 3;

    /**
     * 文本对齐方式，居右
     */
    public static final int GRAVITY_RIGHT = 5;

    /**
     * 文本对齐方式，居中
     */
    public static final int GRAVITY_CENTER = 17;

    /**
     * 文本绘制起始点的X坐标
     */
    private float mDrawingOriginX;

    /**
     * 存储每行文本边界值，用于计算文本的高度
     */
    private Rect mTextBounds;

    /**
     * 记录触摸事件的Y坐标
     */
    private float mStartY;

    /**
     * 触摸移动最小距离
     */
    private int mTouchSlop;

    /**
     * 触摸点的ID
     */
    private int mTouchPointerId;

    /**
     * 是否触摸移动（手指在屏幕上拖动）
     */
    private boolean isMoveAction;

    /**
     * 是否切换了触摸点（多点触摸中的手指切换）
     */
    private boolean isSwitchTouchPointer;

    /**
     * 用于记录指定的position
     */
    private Integer mSpecifyPosition;

    private Matrix mMatrix;

    /**
     * 减速动画
     */
    private DecelerateAnimator mDecelerateAnimator;

    /**
     * 线性颜色选择器
     */
    private LinearGradient mLinearShader;

    /**
     * 速度追踪器，结束触摸事件时计算手势速度，用于减速动画
     */
    private VelocityTracker mVelocityTracker;

    private TextPaint mTextPaint;

    private PickAdapter mAdapter;

    private OnItemSelectedListener mItemSelectedListener;

    public interface OnItemSelectedListener {
        /**
         * 选中时的回调
         */
        void onItemSelected(View view, int position);
    }

    public ScrollPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public ScrollPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attributeSet) {
        mDensityDP = context.getResources().getDisplayMetrics().density;//DP密度
        mDensitySP = context.getResources().getDisplayMetrics().scaledDensity;//SP密度

        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.ScrollPickerView);

        this.mTextRows = typedArray.getInteger(R.styleable.ScrollPickerView_rows, 5);
        this.mTextSize = typedArray.getDimension(R.styleable.ScrollPickerView_textSize, 16 * mDensityDP);
        this.mTextRatio = typedArray.getFloat(R.styleable.ScrollPickerView_textRatio, 2F);
        this.mRowSpacing = typedArray.getDimension(R.styleable.ScrollPickerView_spacing, 0);
        this.mTextFormat = typedArray.getString(R.styleable.ScrollPickerView_textFormat);

        this.mTextColor_Selected = typedArray.getColor(R.styleable.ScrollPickerView_textColor_selected, 0xFFDD8822);
        this.mTextColor_Unselected = typedArray.getColor(R.styleable.ScrollPickerView_textColor_unselected, 0xFFFFDD99);

        this.mLoopEnable = typedArray.getBoolean(R.styleable.ScrollPickerView_loop, true);

        this.mGravity = typedArray.getInt(R.styleable.ScrollPickerView_gravity, GRAVITY_LEFT);
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        typedArray.recycle();

        //初始化画笔工具
        initTextPaint();
        //计算行高
        measureTextHeight();

        mMatrix = new Matrix();//用户记录偏移量并设置给颜色渐变工具
        mTextBounds = new Rect();//用于计算每行文本边界区域
        //减速动画
        mDecelerateAnimator = new DecelerateAnimator(this.getContext());
        mDecelerateAnimator.addUpdateListener(this);
    }

    /**
     * 初始化画笔工具
     */
    private void initTextPaint() {
        mTextPaint = new TextPaint();
        //防抖动
        mTextPaint.setDither(true);
        //抗锯齿
        mTextPaint.setAntiAlias(true);
        //不要文本缓存
        mTextPaint.setLinearText(true);
        //设置亚像素
        mTextPaint.setSubpixelText(true);
        //字体加粗
        mTextPaint.setFakeBoldText(true);

        //设置字体大小
        mTextPaint.setTextSize(mTextSize);
        //等宽字体
        mTextPaint.setTypeface(Typeface.MONOSPACE);
        //设置对齐方式
        switch (mGravity) {
            case GRAVITY_LEFT: {
                mTextPaint.setTextAlign(Paint.Align.LEFT);
                break;
            }
            case GRAVITY_CENTER: {
                mTextPaint.setTextAlign(Paint.Align.CENTER);
                break;
            }
            case GRAVITY_RIGHT: {
                mTextPaint.setTextAlign(Paint.Align.RIGHT);
                break;
            }
        }
    }

    /**
     * 计算行高
     */
    private void measureTextHeight() {
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        //确定行高
        this.mRowHeight = Math.abs(fontMetrics.descent - fontMetrics.ascent) * (mTextRatio > 1 ? mTextRatio : 1);
        //行距不得小于负行高的一半
        if (mRowSpacing < -mRowHeight / 2F) {
            mRowSpacing = -mRowHeight / 2F;
        }
        mItemHeight = mRowHeight + mRowSpacing;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener itemSelectedListener) {
        this.mItemSelectedListener = itemSelectedListener;
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        mLayoutWidth = params.width;
        mLayoutHeight = params.height;
        super.setLayoutParams(params);
    }

    /**
     * 计算PickerView的高宽，会多次调用，包括隐藏导航键也会调用
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (mLayoutWidth == ViewGroup.LayoutParams.WRAP_CONTENT && widthMode != MeasureSpec.EXACTLY) {//宽为WRAP
            if (mTextFormat != null) {
                widthSize = (int) (mTextPaint.measureText(mTextFormat) * (mTextRatio > 1 ? mTextRatio : 1));
            } else {
                widthSize = 200;
            }
        }
        if (mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT && heightMode != MeasureSpec.EXACTLY) {//高为WRAP
            heightSize = (int) (mRowHeight * mTextRows + mRowSpacing * (mTextRows - mTextRows % 2));
        }
        setMeasuredDimension(widthSize, heightSize);
        if (widthSize > 0 && heightSize > 0) {
            measureOriginal();//计算初始状态下显示的行数、首行偏移量
            setPaintShader();//设置颜色线性渐变
        }
    }

    /**
     * 计算初始状态
     */
    private void measureOriginal() {
        //计算绘制区域高度
        int drawHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        //计算中心的Y值
        mCenterY = drawHeight / 2F + getPaddingTop();
        //根据对齐方式计算绘制起点
        switch (mGravity) {
            case GRAVITY_LEFT: {
                mDrawingOriginX = getPaddingLeft();
                break;
            }
            case GRAVITY_CENTER: {
                mDrawingOriginX = (getMeasuredWidth() + getPaddingLeft() - getPaddingRight()) / 2F;
                break;
            }
            case GRAVITY_RIGHT: {
                mDrawingOriginX = getMeasuredWidth() - getPaddingRight();
                break;
            }
        }
    }

    /**
     * 设置颜色线性渐变
     */
    private void setPaintShader() {
        mLinearShader = new LinearGradient(0F, mCenterY - (0.5F * mRowHeight + mItemHeight), 0F, mCenterY + (0.5F * mRowHeight + mItemHeight),
                new int[]{mTextColor_Unselected, mTextColor_Selected, mTextColor_Unselected}
                , new float[]{0F, 0.5F, 1F}, LinearGradient.TileMode.CLAMP);
        mTextPaint.setShader(mLinearShader);
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {
        if (mAdapter == null) {
            return super.onTouchEvent(event);
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        int actionIndex = event.getActionIndex();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                isSwitchTouchPointer = false;
                //当前有减速动画未结束，则取消该动画，并直接进入滑动状态
                if (mDecelerateAnimator.isStarted()) {
                    isMoveAction = true;
                    mDecelerateAnimator.cancel();
                } else {
                    isMoveAction = false;
                }
                //记录偏移坐标
                mStartY = event.getY(actionIndex);
                //记录当前控制指针ID
                mTouchPointerId = event.getPointerId(actionIndex);
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                //如果抬起的指针是当前控制指针，则进行切换
                if (event.getPointerId(actionIndex) == mTouchPointerId) {
                    mVelocityTracker.clear();
                    //从列表中选择一个指针（非当前抬起的指针）作为下一个控制指针
                    for (int index = 0; index < event.getPointerCount(); index++) {
                        if (index != actionIndex) {
                            //重置偏移坐标
                            mStartY = event.getY(index);
                            //重置触摸ID
                            mTouchPointerId = event.getPointerId(index);
                            //标记进行过手指切换
                            isSwitchTouchPointer = true;
                            break;
                        }
                    }
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                //只响应当前控制指针的移动操作
                for (int index = 0; index < event.getPointerCount(); index++) {
                    if (event.getPointerId(index) == mTouchPointerId) {
                        //计算偏移量，指针上移偏移量为正
                        float offset = mStartY - event.getY(index);
                        if (isMoveAction) {
                            //已是滑动状态，累加偏移量，记录偏移坐标，请求重绘
                            mTotalOffset += offset;
                            mStartY = event.getY(index);
                            super.invalidate();
                        } else if (Math.abs(offset) >= mTouchSlop) {
                            //进入滑动状态，重置偏移坐标，标记当前为滑动状态
                            mStartY = event.getY(index);
                            isMoveAction = true;
                        }
                        break;
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                //计算偏移量，指针上移偏移量为正
                float offset = mStartY - event.getY(actionIndex);
                if (isMoveAction) {
                    isMoveAction = false;
                    //计算手势速度
                    mVelocityTracker.computeCurrentVelocity(1000);
                    float velocityY = -mVelocityTracker.getYVelocity(mTouchPointerId);
                    //累加偏移量
                    mTotalOffset += offset;
                    //开启减速动画
                    startDecelerateAnimator(mTotalOffset, velocityY, 0, mItemHeight);
                } else if (!isSwitchTouchPointer && Math.abs(offset) < mTouchSlop) {
                    //计算触摸点相对于中心位置的偏移距离
                    float distance = event.getY(actionIndex) - mCenterY;
                    //开启减速动画
                    startDecelerateAnimator(mTotalOffset, 0, distance, mItemHeight);
                }
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            }
        }
        return true;
    }

    /**
     * 开始减速动画
     *
     * @param startValue 初始位移值
     * @param velocity   初始速度
     * @param distance   移动距离
     * @param modulus    距离的模
     */
    private void startDecelerateAnimator(float startValue, float velocity, float distance, float modulus) {
        float minValue = -1;
        float maxValue = mLoopEnable ? -1 : (mAdapter.getCount() - 1) * mItemHeight + 1;
        if (distance != 0) {
            mDecelerateAnimator.startAnimator_Distance(startValue, minValue, maxValue, distance, modulus);
        } else {
            mDecelerateAnimator.startAnimator_Velocity(startValue, minValue, maxValue, velocity, modulus);
        }
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        mTotalOffset = (Float) animation.getAnimatedValue();
        super.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!isInEditMode() && mAdapter == null) {
            return;
        }
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();

        //根据padding限定绘制区域
        canvas.clipRect(paddingLeft, paddingTop, measuredWidth - paddingRight, measuredHeight - paddingBottom);

        //计算中部item的position及偏移量
        calculateMiddleItem();
        //绘制上半部分的item
        int curPosition = mMiddleItemPostion - 1;
        float curOffset = mCenterY + mMiddleItemOffset - mRowHeight / 2F - mItemHeight;
        while (curOffset > paddingTop - mRowHeight) {
            //绘制文本
            drawText(canvas, curPosition, curOffset);
            curOffset -= mItemHeight;
            curPosition--;
        }

        //绘制中部及下半部分的item
        curPosition = mMiddleItemPostion;
        curOffset = mCenterY + mMiddleItemOffset - mRowHeight / 2F;
        while (curOffset < measuredHeight - paddingBottom) {
            //绘制文本
            drawText(canvas, curPosition, curOffset);
            //下一个
            curOffset += mItemHeight;
            curPosition++;
        }
        //动画结束，进行选中回调
        if (!isMoveAction && !mDecelerateAnimator.isStarted() && mItemSelectedListener != null) {
            //根据当前position对偏移量进行校正
            mTotalOffset = mMiddleItemPostion * mItemHeight;
            //回调监听
            mItemSelectedListener.onItemSelected(this, mMiddleItemPostion);
        }
    }

    /**
     * 根据总偏移量计算中部item的偏移量及position
     * 偏移量的取值范围为(-mItenHeight/2F , mItenHeight/2F]
     */
    private void calculateMiddleItem() {
        //计算偏移了多少个完整item
        int count = mSpecifyPosition != null ? mSpecifyPosition : (int) (mTotalOffset / mItemHeight);
        if (mSpecifyPosition != null) {
            if (mDecelerateAnimator.isStarted()) {
                mDecelerateAnimator.cancel();
            }
            mTotalOffset = mSpecifyPosition * mItemHeight;
            mMiddleItemOffset = 0;
            mSpecifyPosition = null;
        } else {
            //对偏移量取余，注意这里不用取余运算符，因为可能造成严重错误！
            float offsetRem = mTotalOffset - mItemHeight * count;//取值范围( -mItenHeight , mItenHeight )
            if (offsetRem >= mItemHeight / 2F) {
                count++;
                mMiddleItemOffset = mItemHeight - offsetRem;
            } else if (offsetRem >= -mItemHeight / 2F) {
                mMiddleItemOffset = -offsetRem;
            } else {
                count--;
                mMiddleItemOffset = -mItemHeight - offsetRem;
            }
        }
        mMiddleItemPostion = getRealPosition(count);
    }

    /**
     * 绘制文本
     */
    private void drawText(Canvas canvas, int position, float offsetY) {
        //对position取模
        position = getRealPosition(position);
        //position未越界
        if (isInEditMode() || (position >= 0 && position < mAdapter.getCount())) {
            //获取文本
            String text = getDrawingText(position);
            if (text != null) {
                canvas.save();
                //平移画布
                canvas.translate(0, offsetY);
                //操作线性颜色渐变
                mMatrix.setTranslate(0, -offsetY);
                mLinearShader.setLocalMatrix(mMatrix);
                //计算缩放比例
                float scaling = getScaling(offsetY);
                canvas.scale(scaling, scaling, mDrawingOriginX, mRowHeight / 2F);
                //获取文本尺寸
                mTextPaint.getTextBounds(text, 0, text.length(), mTextBounds);
                //根据文本尺寸计算基线位置
                float baseLineY = (mRowHeight - mTextBounds.top - mTextBounds.bottom) / 2F;
                //绘制文本
                canvas.drawText(text, mDrawingOriginX, baseLineY, mTextPaint);
                canvas.restore();
            }
        }
    }

    /**
     * 循环模式下对position取模
     */
    private int getRealPosition(int position) {
        if (mLoopEnable && mAdapter != null) {
            position = position % mAdapter.getCount();
            if (position < 0) {
                position = position + mAdapter.getCount();
            }
        }
        return position;
    }

    /**
     * 根据获取要绘制的文本内容
     */
    private String getDrawingText(int position) {
        if (isInEditMode()) {
            return mTextFormat != null ? mTextFormat : String.valueOf("item" + position);
        }
        if (position >= 0 && position < mAdapter.getCount()) {
            return mAdapter.getItem(position);
        }
        return null;
    }

    /**
     * 根据偏移量计算缩放比例
     */
    private float getScaling(float offsetY) {
        float abs = Math.abs(offsetY + mRowHeight / 2F - mCenterY);
        if (abs < mItemHeight) {
            return (1 - abs / mItemHeight) * (mTextRatio - 1F) + 1F;
        } else {
            return 1F;
        }
    }

    /**
     * 设置适配器、重置极值
     */
    public void setAdapter(PickAdapter adapter) {
        this.mAdapter = adapter;
        super.invalidate();
    }

    /**
     * 设置当前选中项
     */
    public void setSelectedPosition(int position) {
        if (mAdapter == null) return;
        if (position < 0 || position >= mAdapter.getCount()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if (mDecelerateAnimator.isStarted()) {
            mDecelerateAnimator.cancel();
        }
        // 如果在onMeasure之前设置选中项，mItemHeight为0，无法得到正确偏移量，因此这里不能直接计算mTotalOffset
        mSpecifyPosition = position;
        super.invalidate();
    }

    /**
     * 获取当前选中项
     */
    public int getSelectedPosition() {
        if (isMoveAction || mAdapter == null || mDecelerateAnimator.isStarted()) {
            return -1;
        }
        return mMiddleItemPostion;
    }

    /**
     * 设置文本对齐方式，计算文本绘制起始点的X坐标
     */
    public void setGravity(int gravity) {
        switch (gravity) {
            case GRAVITY_LEFT: {
                mTextPaint.setTextAlign(Paint.Align.LEFT);
                mDrawingOriginX = getPaddingLeft();
                break;
            }
            case GRAVITY_CENTER: {
                mTextPaint.setTextAlign(Paint.Align.CENTER);
                mDrawingOriginX = (getMeasuredWidth() + getPaddingLeft() - getPaddingRight()) / 2F;
                break;
            }
            case GRAVITY_RIGHT: {
                mTextPaint.setTextAlign(Paint.Align.RIGHT);
                mDrawingOriginX = getMeasuredWidth() - getPaddingRight();
                break;
            }
            default:
                return;
        }
        mGravity = gravity;
        super.invalidate();
    }

    public boolean isLoopEnable() {
        return mLoopEnable;
    }

    public void setLoopEnable(boolean enable) {
        if (mLoopEnable != enable) {
            mLoopEnable = enable;
            //循环将关闭且正在减速动画
            if (!mLoopEnable && mDecelerateAnimator.isStarted() && mAdapter != null) {
                //停止减速动画，并指定position以确保item对齐
                mDecelerateAnimator.cancel();
                //防止position越界
                mSpecifyPosition = mMiddleItemPostion < 0 ? 0 : (mMiddleItemPostion >= mAdapter.getCount() ? mAdapter.getCount() - 1 : mMiddleItemPostion);
            }
            super.invalidate();
        }
    }

    /**
     * 设置文本显示的行数，仅当高为WRAP_CONTENT时有效
     */
    public void setTextRows(int rows) {
        if (mTextRows != rows) {
            mTextRows = rows;
            if (mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
                super.requestLayout();
            }
        }
    }

    /**
     * 设置文本字体大小，单位px
     *
     * @param textSize 必须大于0
     */
    public void setTextSize(int textSize) {
        if (textSize > 0 && mTextSize != textSize) {
            mTextSize = textSize;
            mTextPaint.setTextSize(mTextSize);
            measureTextHeight();
            reInvalidate();
        }
    }

    /**
     * 设置文本行间距，单位px
     */
    public void setRowSpacing(int rowSpacing) {
        if (mRowSpacing != rowSpacing) {
            mRowSpacing = rowSpacing;
            measureTextHeight();
            reInvalidate();
        }
    }

    /**
     * 设置放大倍数
     */
    public void setTextRatio(float textRatio) {
        if (mTextRatio != textRatio) {
            mTextRatio = textRatio;
            measureTextHeight();
            reInvalidate();
        }
    }

    private void reInvalidate() {
        if (mDecelerateAnimator.isStarted()) {
            mDecelerateAnimator.cancel();
        }
        mSpecifyPosition = mMiddleItemPostion;
        if (mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
            super.requestLayout();
        } else {
            super.invalidate();
        }
    }

}
