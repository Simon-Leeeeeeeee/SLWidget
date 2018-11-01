package cn.simonlee.widget.slidingtablayout;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.HorizontalScrollView;

/**
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-10-23
 */
@SuppressWarnings("unused")
public class SlidingTabLayout extends HorizontalScrollView {

    /**
     * 指示器左对齐
     */
    public static final int GRAVITY_LEFT = 1;
    /**
     * 指示器顶对齐
     */
    public static final int GRAVITY_TOP = 2;
    /**
     * 指示器右对齐
     */
    public static final int GRAVITY_RIGHT = 4;
    /**
     * 指示器底对齐
     */
    public static final int GRAVITY_BOTTOM = 8;
    /**
     * 指示器居中
     */
    public static final int GRAVITY_CENTER = 16;

    /**
     * 指示器图像
     */
    private Drawable mIndicatorDrawable;

    /**
     * 指示器所占区域
     */
    private Rect mIndicatorRect = new Rect();

    /**
     * 指示器指定宽度
     */
    private int mIndicatorWidth;

    /**
     * 指示器指定高度
     */
    private int mIndicatorHeight;

    /**
     * 指示器对齐方式
     */
    private int mIndicatorGravity;

    /**
     * 指示器左边距
     */
    private int mIndicatorMarginLeft;

    /**
     * 指示器上边距
     */
    private int mIndicatorMarginTop;

    /**
     * 指示器右边距
     */
    private int mIndicatorMarginRight;

    /**
     * 指示器下边距
     */
    private int mIndicatorMarginBottom;

    /**
     * 当前选中标签的position
     */
    private int mCurSelection;

    /**
     * 指示器当前滑动位置的标签
     */
    private int mSlideTabPosition;

    /**
     * 指示器相对于当前滑动位置标签的偏移量，取值范围[-1,1]
     */
    private float mSlideTabOffsetPercent;

    /**
     * 当前触摸标签（用于在触摸事件中分发标签点击事件）
     */
    private View mTouchTab;

    /**
     * 当前触摸标签的position
     */
    private int mTouchTabPosition;

    /**
     * 触摸事件拖拽的最小距离
     */
    private int mTouchSlop;

    /**
     * 用于标记当前触摸DOWN事件时是否正在scroll
     */
    private boolean isScrolling;

    /**
     * 触摸事件的X坐标，用于判断是否发生拖拽
     */
    private int mLastMotionX;

    /**
     * 触摸事件的PointerId
     */
    private int mActivePointerId = -1;

    /**
     * 指示器的滑动动画
     */
    private ValueAnimator mSlideAnimation;

    /**
     * API小于21时，用于标志是否裁切padding区域不进行绘制
     */
    private boolean isClipToPadding = true;

    /**
     * 标签点击事件监听
     */
    private OnTabClickListener mOnTabClickListener;

    /**
     * 标签点击事件接口
     */
    public interface OnTabClickListener {
        void onTabClick(SlidingTabLayout parent, View tab, int position);
    }

    public SlidingTabLayout(Context context) {
        super(context);
        initView(context, null);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attributeSet) {
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.SlidingTabLayout);

        //指示器图像
        this.mIndicatorDrawable = typedArray.getDrawable(R.styleable.SlidingTabLayout_slidingtab_indicator_src);

        //指示器对齐方式
        this.mIndicatorGravity = typedArray.getInt(R.styleable.SlidingTabLayout_slidingtab_indicator_gravity, GRAVITY_CENTER);

        //指示器宽高
        this.mIndicatorWidth = typedArray.getDimensionPixelSize(R.styleable.SlidingTabLayout_slidingtab_indicator_width, 0);
        this.mIndicatorHeight = typedArray.getDimensionPixelSize(R.styleable.SlidingTabLayout_slidingtab_indicator_height, 0);

        //指示器边距，默认值0dp
        int indicatorMargin = typedArray.getDimensionPixelSize(R.styleable.SlidingTabLayout_slidingtab_indicator_margin, 0);
        //指示器左边距
        this.mIndicatorMarginLeft = typedArray.getDimensionPixelSize(R.styleable.SlidingTabLayout_slidingtab_indicator_marginLeft, indicatorMargin);
        //指示器上边距
        this.mIndicatorMarginTop = typedArray.getDimensionPixelSize(R.styleable.SlidingTabLayout_slidingtab_indicator_marginTop, indicatorMargin);
        //指示器右边距
        this.mIndicatorMarginRight = typedArray.getDimensionPixelSize(R.styleable.SlidingTabLayout_slidingtab_indicator_marginRight, indicatorMargin);
        //指示器下边距
        this.mIndicatorMarginBottom = typedArray.getDimensionPixelSize(R.styleable.SlidingTabLayout_slidingtab_indicator_marginBottom, indicatorMargin);

        typedArray.recycle();
        //触摸事件拖拽的最小距离
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        View tabsContainer = getChildAt(0);
        if (tabsContainer != null) {
            int measuredWidthAndState, measuredHeightAndState;
            ViewGroup.LayoutParams lp = tabsContainer.getLayoutParams();
            if (lp.width == LayoutParams.MATCH_PARENT && measuredWidth > tabsContainer.getMeasuredWidth()) {//tabsContainer宽度不够，让其充满SlidingTabLayout
                measuredWidthAndState = MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY);
            } else if (lp.width != LayoutParams.MATCH_PARENT && lp.width != LayoutParams.WRAP_CONTENT) {//tabsContainer指定了宽度值，使其生效
                measuredWidthAndState = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
            } else if (lp.height != LayoutParams.WRAP_CONTENT) {//高度不是自适应，说明需要重新测量高度，宽度不做调整
                measuredWidthAndState = tabsContainer.getMeasuredWidthAndState();
            } else {
                return;
            }
            if (lp.height == LayoutParams.MATCH_PARENT) {//tabsContainer指定最大高度，使其生效
                measuredHeightAndState = MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY);
            } else if (lp.height != LayoutParams.WRAP_CONTENT) {//tabsContainer指定高度值，使其生效
                measuredHeightAndState = MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);
            } else {
                measuredHeightAndState = tabsContainer.getMeasuredHeightAndState();
            }
            tabsContainer.measure(measuredWidthAndState, measuredHeightAndState);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //第一次布局完成时，默认选中第一项，并绘制指示器
        selectTab(mCurSelection, true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mIndicatorDrawable == null) {
            return;
        }
        if (isInEditMode()) {//IDE编辑模式，校正指示器显示区域以预览
            reviseIndicatorRect();
        } else if (mIndicatorRect.isEmpty()) {
            return;
        }
        //根据Margin和Gravity确定指示器图像左右边界
        int drawableLeft = mIndicatorRect.left + mIndicatorMarginLeft;
        int drawableRight = mIndicatorRect.right - mIndicatorMarginRight;
        if (mIndicatorWidth > 0) {
            if ((mIndicatorGravity & GRAVITY_LEFT) == GRAVITY_LEFT) {//水平居左
                drawableRight = drawableLeft + mIndicatorWidth;
            } else if ((mIndicatorGravity & GRAVITY_RIGHT) == GRAVITY_RIGHT) {//水平居右
                drawableLeft = drawableRight - mIndicatorWidth;
            } else {//水平居中
                drawableLeft = (int) ((drawableLeft + drawableRight - mIndicatorWidth) / 2F + 0.5F);
                drawableRight = drawableLeft + mIndicatorWidth;
            }
        }
        if (drawableLeft >= drawableRight) {
            return;
        }
        final boolean isClipToPadding = isClipToPadding();
        if (isClipToPadding) {//padding区域不绘制的话，就要对画布进行剪裁
            canvas.save();
            canvas.clipRect(getScrollX() + getPaddingLeft(), 0, getScrollX() + getWidth() - getPaddingRight(), getHeight());
        }
        mIndicatorDrawable.setBounds(drawableLeft, mIndicatorRect.top, drawableRight, mIndicatorRect.bottom);
        mIndicatorDrawable.draw(canvas);
        if (isClipToPadding) {
            canvas.restore();
        }
    }

    /**
     * 判断是否裁切padding区域不进行绘制
     */
    private boolean isClipToPadding() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return getClipToPadding();
        }
        return isClipToPadding;
    }

    @Override
    public void setClipToPadding(boolean clipToPadding) {
        super.setClipToPadding(clipToPadding);
        this.isClipToPadding = clipToPadding;
    }

    /**
     * 设置当前选中标签
     *
     * @param tabPosition 制定标签的Position
     * @param slide       是否同时滑动指示器
     */
    public void selectTab(int tabPosition, boolean slide) {
        View child = getChildAt(0);
        if (child instanceof ViewGroup) {
            ViewGroup tabsContainer = (ViewGroup) child;
            int tabCount = tabsContainer.getChildCount();
            for (int index = 0; index < tabCount; index++) {
                tabsContainer.getChildAt(index).setSelected(index == tabPosition);
            }
        }
        if (slide) {
            slideTo(tabPosition, 0);
        }
        mCurSelection = tabPosition;
    }

    /**
     * 滑动显示器到指定位置
     *
     * @param tabPosition   指定标签Position
     * @param offsetPercent 指定标签的偏移量，取值范围[-1,1]
     */
    public void slideTo(int tabPosition, float offsetPercent) {
        if (offsetPercent < -1 || offsetPercent > 1) {
            throw new IllegalArgumentException("The range of tabOffsetPercent must be [-1, 1]");
        }
        mSlideTabPosition = tabPosition;
        mSlideTabOffsetPercent = offsetPercent;
        //校正指示器的显示区域
        reviseIndicatorRect();
        //校正横向滚动距离，以确保指示器显示在屏幕中
        reviseScrollX();
    }

    /**
     * 校正指示器的显示区域
     */
    private void reviseIndicatorRect() {
        View child = getChildAt(0);
        //标签容器不存在，指示器显示区域置空
        if (!(child instanceof ViewGroup) || child.getHeight() == 0 || child.getWidth() == 0) {
            mIndicatorRect.setEmpty();
            return;
        }
        ViewGroup tabsContainer = (ViewGroup) child;

        //Step.1 确定开始标签和结束标签
        View beginTab = tabsContainer.getChildAt(mSlideTabPosition);
        View endTab = null;
        if (mSlideTabOffsetPercent > 0) {
            endTab = tabsContainer.getChildAt(mSlideTabPosition + 1);
        } else if (mSlideTabOffsetPercent < 0) {
            endTab = tabsContainer.getChildAt(mSlideTabPosition - 1);
        }

        //Step.2 根据标签确定指示器左右边界
        int indicatorLeft;
        int indicatorRight;
        if ((beginTab == null || beginTab.getVisibility() == GONE) && (endTab == null || endTab.getVisibility() == GONE)) {
            mIndicatorRect.setEmpty();
            return;
        } else if (endTab == null) {//仅开始标签存在，且未隐藏
            indicatorLeft = (int) (beginTab.getLeft() + beginTab.getWidth() * mSlideTabOffsetPercent + 0.5F);
            indicatorRight = indicatorLeft + beginTab.getWidth();
        } else if (beginTab == null) {//仅结束标签存在，且未隐藏
            float endTabOffsetPercent = mSlideTabOffsetPercent + (mSlideTabOffsetPercent > 0 ? -1 : 1);
            indicatorLeft = (int) (endTab.getLeft() + endTab.getWidth() * endTabOffsetPercent + 0.5F);
            indicatorRight = indicatorLeft + endTab.getWidth();
        } else {//开始标签和结束标签都存在，且不同时为GONE
            int beginTabLeft = beginTab.getLeft();
            int beginTabRight = beginTab.getRight();
            int endTabLeft = endTab.getLeft();
            int endTabRight = endTab.getRight();
            if (beginTab.getVisibility() == GONE) {//开始标签被隐藏，边界范围限定在结束标签内
                beginTabLeft = beginTabRight = mSlideTabOffsetPercent > 0 ? endTabLeft : endTabRight;
            } else if (endTab.getVisibility() == GONE) {//结束标签被隐藏，边界范围限定在开始标签内
                endTabLeft = endTabRight = mSlideTabOffsetPercent > 0 ? beginTabRight : beginTabLeft;
            }
            indicatorLeft = (int) (beginTabLeft + (endTabLeft - beginTabLeft) * Math.abs(mSlideTabOffsetPercent) + 0.5F);
            indicatorRight = (int) (beginTabRight + (endTabRight - beginTabRight) * Math.abs(mSlideTabOffsetPercent) + 0.5F);
        }
        mIndicatorRect.left = tabsContainer.getLeft() + indicatorLeft;
        mIndicatorRect.right = tabsContainer.getLeft() + indicatorRight;

        //Step.3 根据Margin和Gravity确定指示器上下边界
        int indicatorTop = mIndicatorMarginTop;
        int indicatorBottom = getHeight() - mIndicatorMarginBottom;
        if (mIndicatorHeight > 0) {
            if ((mIndicatorGravity & GRAVITY_TOP) == GRAVITY_TOP) {//垂直居上
                indicatorBottom = indicatorTop + mIndicatorHeight;
            } else if ((mIndicatorGravity & GRAVITY_BOTTOM) == GRAVITY_BOTTOM) {//垂直居下
                indicatorTop = indicatorBottom - mIndicatorHeight;
            } else {//垂直居中
                indicatorTop = (int) ((indicatorTop + indicatorBottom - mIndicatorHeight) / 2F + 0.5F);
                indicatorBottom = indicatorTop + mIndicatorHeight;
            }
        }
        mIndicatorRect.top = indicatorTop;
        mIndicatorRect.bottom = indicatorBottom;
    }

    /**
     * 校正横向滚动距离，以确保指示器显示在屏幕中
     */
    private void reviseScrollX() {
        if (mIndicatorRect.isEmpty() || getChildCount() <= 0) {
            return;
        }
        //确定滚动距离的极值
        int minScrollX = 0;
        int maxScrollX = getChildAt(0).getWidth() - getWidth() + getPaddingLeft() + getPaddingRight();

        int indicatorLeft = mIndicatorRect.left;
        int indicatorRight = mIndicatorRect.right;
        if (isClipToPadding()) {//排除padding裁切
            indicatorLeft -= getPaddingLeft();
            indicatorRight += getPaddingRight();
        }

        if (getScrollX() > minScrollX && getScrollX() > indicatorLeft) {//当前可以向左滚动，且需要向左滚动
            scrollTo(indicatorLeft, getScrollY());
        } else if (getScrollX() < maxScrollX && getScrollX() < indicatorRight - getWidth()) {//当前可以向右滚动，且需要向右滚动
            scrollTo(indicatorRight - getWidth(), getScrollY());
        } else if (mIndicatorDrawable != null) {//不需要滚动或者无法滚动，则直接请求重绘
            invalidate();
        }
    }

    /**
     * 开始指示器滑动动画
     *
     * @param tabPosition 目标标签的postion
     * @param duration    动画持续时长
     */
    public void startSlideAnimation(float tabPosition, long duration) {
        if (mSlideAnimation == null) {
            setSlideAnimation(null);
        }
        mSlideAnimation.setDuration(duration);
        //从当前位置滑动到目标位置
        mSlideAnimation.setFloatValues(mSlideTabPosition + mSlideTabOffsetPercent, tabPosition);
        mSlideAnimation.start();
    }

    /**
     * 取消指示器滑动动画
     */
    public void cancelSlideAnimation() {
        if (mSlideAnimation != null) {
            mSlideAnimation.cancel();
        }
    }

    /**
     * 设置指示器滑动动画
     *
     * @param interpolator 动画插值器
     */
    public void setSlideAnimation(Interpolator interpolator) {
        if (mSlideAnimation == null) {
            mSlideAnimation = new ValueAnimator();
            //设置动画进度回调
            mSlideAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    //获取当前标签位置
                    float curTabAndOffset = (float) animation.getAnimatedValue();
                    int position = (int) curTabAndOffset;
                    //滑动到该标签位置
                    slideTo(position, curTabAndOffset - position);
                }

            });
        }
        //设置插值器
        if (mSlideAnimation.getInterpolator() != interpolator) {
            mSlideAnimation.setInterpolator(interpolator);
        }
    }

    /**
     * 设置标签点击事件监听，
     * <p>
     * 注意：如果有给标签单独设置onclick点击事件或者clickable，回调将失效
     */
    public void setOnTabClickListener(OnTabClickListener listener) {
        mOnTabClickListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercept = super.onInterceptTouchEvent(ev);
        if (MotionEvent.ACTION_DOWN == ev.getAction()) {
            //判断是否正在scroll，由HorizontalScrollView源码可知：DOWN事件中，只有scroll未结束时才会返回true
            mTouchTab = null;
            isScrolling = intercept;
        }
        return intercept;
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent ev) {
        boolean expend = super.onTouchEvent(ev);
        if (getChildCount() != 1) {
            return expend;
        }
        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                mTouchTab = null;
                mLastMotionX = (int) ev.getX();
                mActivePointerId = ev.getPointerId(0);
                if (isScrolling) {//DOWN时在scroll中则没有触摸标签
                    break;
                }
                View child = getChildAt(0);
                if (!(child instanceof ViewGroup)) {
                    break;
                }
                ViewGroup tabsContainer = (ViewGroup) child;
                int tabCount = tabsContainer.getChildCount();
                int clickPointX = mLastMotionX + getScrollX() - tabsContainer.getLeft();
                for (int index = tabCount - 1; index >= 0; index--) {
                    final View tab = tabsContainer.getChildAt(index);
                    if (clickPointX < tab.getRight() && clickPointX >= tab.getLeft()) {
                        mTouchTab = tab;
                        mTouchTabPosition = index;
                        break;
                    }
                }
                if (mTouchTab != null) {//给当前触摸标签设置按压状态
                    mTouchTab.setPressed(true);
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                if (mTouchTab == null) {
                    break;
                }
                final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                if (ev.getPointerId(pointerIndex) == mActivePointerId) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    //触摸ID切换
                    mLastMotionX = (int) ev.getX(newPointerIndex);
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (mTouchTab == null || mActivePointerId == -1) {
                    break;
                }
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex != -1 && Math.abs(mLastMotionX - (int) ev.getX(pointerIndex)) > mTouchSlop) {
                    //判断触摸事件位移已达到拖拽判定，将触摸标签置null
                    mTouchTab = null;
                    setPressed(false);//取消按压状态
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (mTouchTab != null && mOnTabClickListener != null) {
                    mOnTabClickListener.onTabClick(this, mTouchTab, mTouchTabPosition);
                }
            }
            case MotionEvent.ACTION_CANCEL: {
                setPressed(false);//取消按压状态
                break;
            }
        }
        return expend;
    }

    public void setIndicatorGravity(int gravity) {
        if (mIndicatorGravity != gravity) {
            this.mIndicatorGravity = gravity;
            invalidate();
        }
    }

    public void setIndicatorMarginLeft(int left) {
        if (mIndicatorMarginLeft != left) {
            this.mIndicatorMarginLeft = left;
            invalidate();
        }
    }

    public void setIndicatorMarginTop(int top) {
        if (mIndicatorMarginTop != top) {
            this.mIndicatorMarginTop = top;
            invalidate();
        }
    }

    public void setIndicatorMarginRight(int right) {
        if (mIndicatorMarginRight != right) {
            this.mIndicatorMarginRight = right;
            invalidate();
        }
    }

    public void setIndicatorMarginBottom(int bottom) {
        if (mIndicatorMarginBottom != bottom) {
            this.mIndicatorMarginBottom = bottom;
            invalidate();
        }
    }

    public void setIndicatorMargin(int left, int top, int right, int bottom) {
        boolean change = false;
        if (mIndicatorMarginLeft != left) {
            change = true;
            this.mIndicatorMarginLeft = left;
        }
        if (mIndicatorMarginLeft != top) {
            change = true;
            this.mIndicatorMarginTop = top;
        }
        if (mIndicatorMarginLeft != right) {
            change = true;
            this.mIndicatorMarginRight = right;
        }
        if (mIndicatorMarginLeft != bottom) {
            change = true;
            this.mIndicatorMarginBottom = bottom;
        }
        if (change) {
            invalidate();
        }
    }

    public void setIndicatorDrawable(Drawable drawable) {
        if (mIndicatorDrawable != drawable) {
            this.mIndicatorDrawable = drawable;
            invalidate();
        }
    }

    public void setIndicatorHeight(int height) {
        if (mIndicatorHeight != height) {
            this.mIndicatorHeight = height;
            invalidate();
        }
    }

    public void setIndicatorWidth(int width) {
        if (mIndicatorWidth != width) {
            this.mIndicatorWidth = width;
            invalidate();
        }
    }

    public int getCurSelection() {
        return mCurSelection;
    }

    public int getSlideTabPosition() {
        return mSlideTabPosition;
    }

    public float getSlideTabOffsetPercent() {
        return mSlideTabOffsetPercent;
    }

    public Drawable getIndicatorDrawable() {
        return mIndicatorDrawable;
    }

    public OnTabClickListener getOnTabClickListener() {
        return mOnTabClickListener;
    }

}
