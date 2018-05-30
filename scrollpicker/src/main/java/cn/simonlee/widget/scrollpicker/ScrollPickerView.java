package cn.simonlee.widget.scrollpicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
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
public class ScrollPickerView extends View {

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
     * item0的position
     */
    private int mFirstItemPostion;

    /**
     * item0的偏移量，取值范围 (-mRowHeight , mRowSpacing]
     */
    private float mFirstItemOffset;

    /**
     * 正常状态下，选中item与item0的position差值
     */
    private int mOriginalPositionOffset;

    /**
     * 正常状态下，item0离上边界的偏移值，取值范围 (-mRowHeight , mRowSpacing]
     */
    private float mOriginalFirstItemOffset;

    /**
     * 绘制区域中点的Y坐标
     */
    private float mCenterY;

    /**
     * 偏移量极小值
     */
    private float mMinOffset;

    /**
     * 偏移量极大值
     */
    private float mMaxOffset;

    /**
     * 所有item累加的总距离
     */
    private float mTotalDistance;

    /**
     * 总的累计偏移量
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
     * 用于记录指定选中的position
     */
    private Integer mSpecifyPosition;

    private Matrix mMatrix;

    /**
     * 滑动辅助器
     */
    private OverScroller mOverScroller;

    /**
     * 线性颜色选择器
     */
    private LinearGradient mLinearShader;

    /**
     * 速度追踪器，结束触摸事件时计算手势速度，用于滑动动画
     */
    private VelocityTracker mVelocityTracker;

    private TextPaint mTextPaint;

    private PickAdapter mAdapter;

    private OnItemSelectedListener mItemSelectedListener;

    public interface OnItemSelectedListener {
        /**
         * 选中时的回调
         */
        void onItemSelected(View view, int position, String value);
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
        //滑动辅助器
        mOverScroller = new OverScroller(mDensityDP);
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
            measureExtremOffset();//计算偏移量的极值
            setPaintShader();//设置颜色线性渐变
        }
    }

    /**
     * 计算初始状态
     */
    private void measureOriginal() {
        //计算绘制区域高度
        int drawHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        //减去选中行高度后对折得到的剩余高度
        float halfSurplusHeight = (drawHeight - mRowHeight) / 2;
//      TODO measuredHeight不足一行的处理
        //剩余高度能够填入的行数
        mOriginalPositionOffset = (int) (halfSurplusHeight / mItemHeight);
        //对剩余高度取余 注意这里不用取余运算符，因为可能造成严重错误！
        float halfSurplusRem = halfSurplusHeight - mItemHeight * mOriginalPositionOffset;

        if (mRowSpacing < 0) {
            halfSurplusRem -= (mRowSpacing + mRowHeight);
            mOriginalPositionOffset++;
        }
        if (halfSurplusRem > mRowSpacing) {//剩余高度取余后大于行距，第一个item压线了
            //第一行离上边界的偏移值，负值
            mOriginalFirstItemOffset = halfSurplusRem - mRowSpacing - mRowHeight;
            mOriginalPositionOffset++;
        } else {//第一个item未压线
            //第一行离上边界的偏移值，非负值
            mOriginalFirstItemOffset = halfSurplusRem;
        }
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
     * 计算偏移量的极值
     */
    private void measureExtremOffset() {
        if (isInEditMode() || mAdapter == null) {
            mMinOffset = mMaxOffset = 0;
        } else {
            mMinOffset = mOriginalFirstItemOffset - mCenterY + mRowHeight / 2F;
            mMaxOffset = (mAdapter.getCount() - 1 - mOriginalPositionOffset) * mItemHeight;
            mTotalDistance = mAdapter.getCount() * mItemHeight;
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
                //当前有滑动动画未结束，则取消该动画，并直接进入滑动状态
                if (!mOverScroller.isFinished()) {
                    mOverScroller.finish();
                    isMoveAction = true;
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
                    //根据手势速度开启滑动动画
                    mOverScroller.startScroll_Velocity(mTotalOffset, mMinOffset, mLoopEnable ? mMinOffset : mMaxOffset, velocityY, mItemHeight);
                    super.invalidate();
                } else if (!isSwitchTouchPointer && Math.abs(offset) < mTouchSlop) {
                    //计算触摸点相对于中心位置的偏移距离
                    float distance = event.getY(actionIndex) - mCenterY;
                    //调用滑动动画方法，移动到目标位置
                    mOverScroller.startScroll_Value(mTotalOffset, mMinOffset, mLoopEnable ? mMinOffset : mMaxOffset, distance, mItemHeight);
                    super.invalidate();
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

    @Override
    public void computeScroll() {
        //滑动动画未结束
        if (!isMoveAction && !mOverScroller.isFinished()) {
            //计算当前偏移量
            mTotalOffset = mOverScroller.getCurValue();
            //动画不会结束则预请求下一次刷新
            if (!mOverScroller.isFinished()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    super.postInvalidateOnAnimation();
                } else {
                    super.postInvalidate();
                }
            }
        }
        if (mSpecifyPosition != null) {
            mTotalOffset = (mSpecifyPosition % mAdapter.getCount() - mOriginalPositionOffset) * mItemHeight;
            mSpecifyPosition = null;
        }
        if (mLoopEnable && mAdapter != null) {
            //循环模式时 对总偏移量进行校正
            if (mTotalOffset < mMinOffset) {
                mTotalOffset += mTotalDistance;
            } else if (mTotalOffset > (mTotalDistance - mMinOffset)) {
                mTotalOffset -= mTotalDistance;
            }
        }
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

        //计算第一行的position及偏移量
        calculateFirstItem();
        int curPosition = mFirstItemPostion;
        float curOffset = mFirstItemOffset + paddingTop;
        //根据padding限定绘制区域
        canvas.clipRect(paddingLeft, paddingTop, measuredWidth - paddingRight, measuredHeight - paddingBottom);
        while (curOffset < measuredHeight - paddingBottom) {
            //对position取模
            curPosition = getRealPosition(curPosition);
            //position未越界
            if (isInEditMode() || (curPosition >= 0 && curPosition < mAdapter.getCount())) {
                //获取文本
                String text = getDrawingText(curPosition);
                //绘制文本
                drawText(canvas, text, curOffset);
            }
            curOffset += mItemHeight;
            curPosition++;
        }
        //动画即将结束，进行选中回调
        if (!isMoveAction && mOverScroller.isFinished() && mItemSelectedListener != null) {
            int selectPosition = getRealPosition(mFirstItemPostion + mOriginalPositionOffset);
            mItemSelectedListener.onItemSelected(this, selectPosition, getDrawingText(selectPosition));
        }
    }

    /**
     * 根据总偏移量计算item0的偏移量及position
     */
    private void calculateFirstItem() {
        //计算偏移了多少个完整item
        int count = (int) (mTotalOffset / mItemHeight);
        //对偏移量取余 注意这里不用取余运算符，因为可能造成严重错误！
        float offsetRem = mTotalOffset - mItemHeight * count;
        //计算item0的上边相对于View上边的偏移量
        float offset = offsetRem - mOriginalFirstItemOffset;

        if (offset < -mRowSpacing) {//偏移量小于负间距，position减1
            mFirstItemPostion = count - 1;
        } else if (offset < mRowHeight) {
            mFirstItemPostion = count;
        } else {//偏移量大于等于行高，position加1
            mFirstItemPostion = count + 1;
        }

        //对剩余偏移量再次取余 注意这里不用取余运算符，因为可能造成严重错误！
        offset += 2 * mItemHeight;
        offsetRem = offset - (int) (offset / mItemHeight) * mItemHeight;

        if (offsetRem < -mRowSpacing) {
            mFirstItemOffset = -offsetRem - mRowHeight - mRowSpacing;
        } else if (offsetRem < mRowHeight) {
            mFirstItemOffset = -offsetRem;
        } else {
            mFirstItemOffset = -offsetRem + mItemHeight;
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
     * 绘制文本
     */
    private void drawText(Canvas canvas, String text, float offsetY) {
        canvas.save();
        //平移画布
        canvas.translate(0, offsetY);
        //操作线性颜色渐变
        mMatrix.setTranslate(0, -offsetY);
        mLinearShader.setLocalMatrix(mMatrix);
        //根据偏移量计算缩放比例
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
        measureExtremOffset();
    }

    /**
     * 设置当前选中项
     */
    public void setSelectedPosition(int position) {
        if (mAdapter == null) return;
        if (position < 0 || position >= mAdapter.getCount()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if (!mOverScroller.isFinished()) {
            mOverScroller.finish();
        }
        //这里不直接计算偏移量是因为，如果在onMeasure完成之前调用setSelectedPosition会有偏差
        mSpecifyPosition = position;
        super.invalidate();
    }

    /**
     * 获取当前选中项
     */
    public int getSelectedPosition() {
        if (isMoveAction || mAdapter == null || !mOverScroller.isFinished()) {
            return -1;
        }
        return getRealPosition(mFirstItemPostion + mOriginalPositionOffset);
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

    public void setLoopable(boolean isChecked) {
        //TODO
//        if (mLoopEnable != isChecked) {
//            mLoopEnable = isChecked;
//            super.invalidate();
//        }
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
     * 设置文本字体大小，单位sp
     */
    public void setTextSize(int textSize) {
        mTextSize = textSize * mDensitySP;
        mTextPaint.setTextSize(mTextSize);
        measureTextHeight();
        if (mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
            super.requestLayout();
        } else {
            super.invalidate();
        }
    }

    /**
     * 设置文本行间距，单位dp
     */
    public void setRowSpacing(int rowSpacing) {
        mRowSpacing = rowSpacing * mDensityDP;
        measureTextHeight();
        if (mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
            super.requestLayout();
        } else {
            super.invalidate();
        }
    }

    /**
     * 设置放大倍数
     */
    public void setTextRatio(float textRatio) {
        mTextRatio = textRatio;
        measureTextHeight();
        if (mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
            super.requestLayout();
        } else {
            super.invalidate();
        }
    }

}
