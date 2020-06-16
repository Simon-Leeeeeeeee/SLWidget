package cn.simonlee.widget.swiperefreshlayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.List;

/**
 * 可兼容任意View的刷新布局
 * <p>
 * TODO
 * 1. 动画时间&阻尼可控
 * 2. 支持hintView
 * 3. 加入覆盖刷新（即原生SwipeRefreshLayout效果）
 * 4. 解决横向滑动冲突
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-08-16
 */
@SuppressWarnings({"FieldCanBeLocal", "ClickableViewAccessibility", "JavadocReference", "BooleanMethodIsAlwaysInverted", "RedundantIfStatement", "unused"})
public class SwipeRefreshLayout extends FrameLayout {

    /**
     * 刷新状态：不可用
     */
    public static final int STATE_UNABLE = -1;

    /**
     * 刷新状态：已关闭
     */
    public static final int STATE_CLOSE = 0;

    /**
     * 刷新状态：已展开（距离还不够）
     */
    public static final int STATE_OPEN = 1;

    /**
     * 刷新状态：已就绪（松手刷新）
     */
    public static final int STATE_READY = 2;

    /**
     * 刷新状态：刷新中
     */
    public static final int STATE_REFRESHING = 3;

    /**
     * 刷新状态：刷新完成
     */
    public static final int STATE_REFRESH_COMPLETE = 4;

    /**
     * 当前刷新状态
     */
    private int mRefreshState;

    /**
     * 标志刷新状态是否改变
     */
    private boolean isChanged = false;

    /**
     * 当前刷新控件 (HeaderRefreshView or FooterRefreshView)
     */
    private View mCurRefreshView;

    /**
     * 下拉刷新控件
     */
    private View mHeaderRefreshView;

    /**
     * 上滑刷新控件
     */
    private View mFooterRefreshView;

    /**
     * 刷新监听器
     */
    private OnRefreshListener mOnRefreshListener;

    /**
     * 标志顶部是否可以下拉
     */
    private boolean isHeaderEnabled;

    /**
     * 标志底部是否可以上拉
     */
    private boolean isFooterEnabled;

    /**
     * 标志顶部是否可以刷新
     */
    private boolean isHeaderRefreshable;

    /**
     * 标志底部是否可以刷新
     */
    private boolean isFooterRefreshable;

    /**
     * 标志顶部刷新是否折叠
     */
    private boolean isHeaderRefreshFolded;

    /**
     * 标志底部刷新是否折叠
     */
    private boolean isFooterRefreshFolded;

    /**
     * 回归动画
     */
    private DecelerateAnimator mRegressAnimator;

    /**
     * 标志回归动画被取消
     */
    private boolean isRegressAnimatorCanceled;

    /**
     * 判断滑动事件的最小距离
     */
    private int mTouchSlop;

    /**
     * 触摸点的ID
     */
    private int mTouchPointerId;

    /**
     * 刷新时的阻尼系数
     */
    private float mDamping = 2F;

    /**
     * 触摸事件的x,y坐标，相对View自身的左顶点
     */
    private float mTouchDownX, mTouchDownY;

    /**
     * 上一个触摸事件的y坐标
     */
    private float mPrevY;

    /**
     * 标志是否垂直触摸移动（手指在屏幕上拖动）
     */
    private boolean isBeingMoved;

    /**
     * ACTION_DOWN事件中所有被触摸到的child集合
     */
    private List<View> mTouchedChildren = new ArrayList<>();

    /**
     * 当前触摸事件的消费者
     */
    private View mCurConsumer;

    /**
     * 所有child触摸事件监听
     */
    private OnTouchListener mChildOnTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            mCurConsumer = view;
            return false;
        }
    };

    public SwipeRefreshLayout(Context context) {
        super(context);
        initSwipeRefreshLayout(context, null);
    }

    public SwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSwipeRefreshLayout(context, attrs);
    }

    public SwipeRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initSwipeRefreshLayout(context, attrs);
    }

    /**
     * 初始化控件
     */
    private void initSwipeRefreshLayout(Context context, AttributeSet attributeSet) {
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.SwipeRefreshLayout);
        //顶部是否可用
        this.isHeaderEnabled = typedArray.getBoolean(R.styleable.SwipeRefreshLayout_swiperefresh_header_enabled, true);
        //底部是否可用
        this.isFooterEnabled = typedArray.getBoolean(R.styleable.SwipeRefreshLayout_swiperefresh_footer_enabled, true);

        //顶部是否可刷新
        this.isHeaderRefreshable = typedArray.getBoolean(R.styleable.SwipeRefreshLayout_swiperefresh_header_refreshable, true);
        //底部是否可刷新
        this.isFooterRefreshable = typedArray.getBoolean(R.styleable.SwipeRefreshLayout_swiperefresh_footer_refreshable, true);

        //顶部是否折叠刷新
        this.isHeaderRefreshFolded = typedArray.getBoolean(R.styleable.SwipeRefreshLayout_swiperefresh_header_folded, false);
        //底部是否折叠刷新
        this.isFooterRefreshFolded = typedArray.getBoolean(R.styleable.SwipeRefreshLayout_swiperefresh_footer_folded, false);

        //顶部刷新控件
        int headerLayoutID = typedArray.getResourceId(R.styleable.SwipeRefreshLayout_swiperefresh_header_layout, NO_ID);
        setHeaderRefreshView(headerLayoutID);

        //底部刷新控件
        int footerLayoutID = typedArray.getResourceId(R.styleable.SwipeRefreshLayout_swiperefresh_footer_layout, NO_ID);
        setFooterRefreshView(footerLayoutID);

        typedArray.recycle();
        //判断滑动事件的最小距离
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mHeaderRefreshView != null) {
            //设置偏移量，使顶部刷新控件隐藏到顶部
            mHeaderRefreshView.offsetTopAndBottom(-mHeaderRefreshView.getBottom());
        }
        if (mFooterRefreshView != null) {
            //设置偏移量，使底部刷新控件隐藏到底部
            mFooterRefreshView.offsetTopAndBottom(getHeight() - mFooterRefreshView.getTop());
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        //除刷新控件外，只能有一个childView
        if (getChildView() != null) {
            throw new IllegalStateException("SwipeRefreshLayout can host only one direct child");
        }
        //不显示childView的滑动越界阴影
        child.setOverScrollMode(OVER_SCROLL_NEVER);
        super.addView(child, index, params);
    }

    @Override
    protected boolean addViewInLayout(View child, int index, ViewGroup.LayoutParams params, boolean preventRequestLayout) {
        //除刷新控件外，只能有一个childView
        if (getChildView() != null) {
            throw new IllegalStateException("SwipeRefreshLayout can host only one direct child");
        }
        //不显示childView的滑动越界阴影
        child.setOverScrollMode(OVER_SCROLL_NEVER);
        return super.addViewInLayout(child, index, params, preventRequestLayout);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (getChildView() == null) {
            return super.dispatchTouchEvent(event);
        }
        //是否在动画中
        final boolean isBeingRegressed = mRegressAnimator != null && mRegressAnimator.isStarted();
        //是否为锁定状态
        final boolean isLockedState = mRefreshState == STATE_REFRESHING || mRefreshState == STATE_REFRESH_COMPLETE;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                isBeingMoved = false;
                //记录触摸点
                recordTouchPointer(event, event.getActionIndex());
                //当前事件消费者置空
                mCurConsumer = null;
                //清空集合
                mTouchedChildren.clear();
                //遍历所有被触摸到的child
                listTouchedChildren(getChildView(), mTouchDownX + getScrollX(), mTouchDownY + getScrollY(), isInScrollingContainer());
                //正常下发触摸DOWN事件
                super.dispatchTouchEvent(event);
                if (isBeingRegressed) {//回归动画中
                    isBeingMoved = true;
                    //取消Pressed状态
                    cancelPressedState();
                    //如果非锁定状态则取消回归动画
                    if (!isLockedState) {
                        mRegressAnimator.cancel();
                    }
                }
                //事件已下发，直接返回true
                return true;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                //重新记录触摸点
                recordTouchPointer(event, event.getActionIndex());
                //校正触摸坐标，使ScrollY为0时，子View重新接收到触摸事件，Move距离为0
                event.offsetLocation(0, getScrollY());
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                int actionIndex = event.getActionIndex();
                if (event.getPointerId(actionIndex) == mTouchPointerId) {
                    //记录下一个触摸点
                    recordNextTouchPointer(event, actionIndex);
                }
                //校正触摸坐标，使ScrollY为0时，子View重新接收到触摸事件，Move距离为0
                event.offsetLocation(0, getScrollY());
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                for (int index = 0; index < event.getPointerCount(); index++) {
                    if (event.getPointerId(index) == mTouchPointerId) {
                        //当前触摸事件Y坐标
                        final float curY = event.getY(index);
                        if (!isBeingRegressed) {//未在回归动画中
                            if (isBeingMoved && !isLockedState) {//已满足拖拽，且非锁定状态
                                //计算Y轴滚动偏移量
                                float offsetY = (mPrevY - curY) / mDamping + getScrollY();
                                //四舍五入转int
                                int scrollY = (int) (offsetY + (offsetY < 0 ? -0.5F : 0.5F));
                                //判断是否可以拉开刷新
                                if (!canScrollRefresh(scrollY)) {
                                    scrollToRefresh(0, false, false);
                                } else {
                                    scrollToRefresh(scrollY, false, false);
                                    //记录当前触摸事件Y坐标
                                    mPrevY = curY;
                                    //移动事件发生，拦截MOVE事件
                                    return true;
                                }
                            } else if (!isBeingMoved && Math.abs(mTouchDownY - curY) > mTouchSlop) {
                                isBeingMoved = true;
                                //复制一次MOVE事件进行下发，使得下次MOVE事件到来时，可以准确回调消费触摸事件的child
                                super.dispatchTouchEvent(MotionEvent.obtain(event));
                            }
                        }
                        //记录当前触摸事件Y坐标
                        mPrevY = curY;
                        break;
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                //非动画中，非锁定状态，已满足拖拽
                if (!isBeingRegressed && !isLockedState && isBeingMoved) {
                    isBeingMoved = false;
                    int scrollY = getScrollY();
                    //判断是否可以拉开刷新
                    if (!canScrollRefresh(scrollY)) {
                        scrollToRefresh(0, false, true);
                    } else {
                        scrollToRefresh(scrollY, false, false);
                        int endValue = 0;
                        if (isHeaderRefreshable() && scrollY <= -mHeaderRefreshView.getHeight()) {
                            endValue = isHeaderRefreshFolded() ? 0 : -mHeaderRefreshView.getHeight();
                        } else if (isFooterRefreshable() && scrollY >= mFooterRefreshView.getHeight()) {
                            endValue = isFooterRefreshFolded() ? 0 : mFooterRefreshView.getHeight();
                        }
                        //开始动画
                        startRegressAnimator(endValue);
                        //复制一次CANCEL事件进行下发
                        MotionEvent cancelEvent = MotionEvent.obtain(event);
                        cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
                        super.dispatchTouchEvent(cancelEvent);
                        //拦截UP事件
                        return true;
                    }
                }
                break;
            }
        }
        super.dispatchTouchEvent(event);
        return true;
    }

    /**
     * 取消Pressed状态
     */
    private void cancelPressedState() {
        cancelLongPress();
        setPressed(false);
        //取消childPressed状态
        for (View child : mTouchedChildren) {
            child.cancelLongPress();
            child.setPressed(false);
        }
        //清空集合
        mTouchedChildren.clear();
    }

    /**
     * 遍历所有被触摸到的child
     *
     * @param view                   目标view
     * @param localX                 触摸X坐标，相对目标view的父容器左顶点
     * @param localY                 触摸Y坐标，相对目标view的父容器左顶点
     * @param isInScrollingContainer 是否在一个可滑动容器中，这将使目标View在延时一定时间后被置为Pressed状态，参阅{@link View#isInScrollingContainer()}
     */
    private void listTouchedChildren(View view, float localX, float localY, boolean isInScrollingContainer) {
        if (view == null || view.getVisibility() != VISIBLE || !pointInView(view, localX, localY)) {
            //不可见或者未触摸
            return;
        }
        //设置触摸监听，用于识别消费触摸事件的child
        view.setOnTouchListener(mChildOnTouchListener);
        if (isInScrollingContainer) {
            //如果被延时设置，则加入集合
            mTouchedChildren.add(view);
        }
        //递归集合child
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = ((ViewGroup) view);
            //更新相对触摸坐标
            localX += view.getScrollX() - view.getLeft();
            localY += view.getScrollY() - view.getTop();
            //更新延时设置标志
            isInScrollingContainer |= viewGroup.shouldDelayChildPressedState();
            for (int index = 0; index < viewGroup.getChildCount(); index++) {
                listTouchedChildren(viewGroup.getChildAt(index), localX, localY, isInScrollingContainer);
            }
        }
    }

    /**
     * 判断坐标是否落在目标view上
     *
     * @param view   目标view
     * @param localX X坐标，相对目标view的父容器
     * @param localY Y坐标，相对目标view的父容器
     */
    private boolean pointInView(View view, float localX, float localY) {
        return view != null && localX >= view.getLeft() && localY >= view.getTop() && localX < view.getRight() && localY < view.getBottom();
    }

    /**
     * 记录触摸坐标及ID
     */
    private void recordTouchPointer(MotionEvent event, int actionIndex) {
        //记录触摸坐标，相对View自身的左顶点
        mTouchDownX = event.getX(actionIndex);
        mTouchDownY = event.getY(actionIndex);
        mPrevY = mTouchDownY;
        //记录触摸ID
        mTouchPointerId = event.getPointerId(actionIndex);
    }

    /**
     * 记录下一个触摸坐标及ID
     */
    private void recordNextTouchPointer(MotionEvent event, int curActionIndex) {
        for (int index = 0; index < event.getPointerCount(); index++) {
            if (index != curActionIndex) {
                recordTouchPointer(event, index);
                return;
            }
        }
    }

    /**
     * 判断是否可以拉开刷新
     *
     * @param scrollY Y轴偏移量
     */
    private boolean canScrollRefresh(int scrollY) {
        if (scrollY == 0) {
            return false;
        }
        if (getScrollY() == 0 && !canConsumeVerticallyScroll(scrollY)) {
            return scrollY < 0 ? isHeaderEnabled() : isFooterEnabled();
        }
        //滚动变向则不展开
        return scrollY * getScrollY() > 0;
    }

    /**
     * 判断下拉或者上滑事件是否可被child消费
     * <p>
     * 该方法可以被子类重写，以适应某些特殊的自定义View
     *
     * @param direction 负值表示为下拉动作，否则为上拉
     */
    protected boolean canConsumeVerticallyScroll(int direction) {
        return mCurConsumer != null && mCurConsumer.canScrollVertically(direction);
    }

    /**
     * 拉开刷新视图
     *
     * @param scrollY      偏移量
     * @param lockState    是否锁定状态，动画过程中需要锁定
     * @param isFinalState 是否为最终状态
     */
    public void scrollToRefresh(int scrollY, boolean lockState, boolean isFinalState) {
        if (getScrollY() != scrollY) {
            //改变偏移量
            scrollTo(getScrollX(), scrollY);
        }
        if (!lockState) {
            resetRefreshView();
            //根据偏移量改变刷新状态
            resetRefreshState(isFinalState);
        }
        //触摸移动中，根据偏移量指定刷新状态
        adjustChildScrollPadding();
        //通知刷新状态改变
        notifyOnRefresh();
    }

    /**
     * 根据偏移量改变刷新控件
     */
    private void resetRefreshView() {
        final int scrollY = getScrollY();
        if (scrollY != 0) {
            // 根据滑动距离变更刷新控件
            changeRefreshView(scrollY > 0 ? mFooterRefreshView : mHeaderRefreshView);
        }
    }

    /**
     * 根据偏移量改变刷新状态
     *
     * @param isFinalState 是否为最终状态
     */
    private void resetRefreshState(boolean isFinalState) {
        final int scrollY = getScrollY();

        //当前视图是否可刷新
        final boolean isRefreshable = isCurRefreshViewRefreshable();
        //当前拉开距离是否就绪
        final boolean isReady = isRefreshable && Math.abs(scrollY) >= mCurRefreshView.getHeight();

        switch (mRefreshState) {
            case STATE_UNABLE: {// 当前状态不可用
            }
            case STATE_CLOSE: {// 当前状态关闭
            }
            case STATE_OPEN: {// 当前状态打开
                if (scrollY == 0) {
                    changeRefreshState(STATE_CLOSE);
                } else if (!isRefreshable) {
                    changeRefreshState(STATE_UNABLE);
                } else if (!isFinalState) {
                    changeRefreshState(isReady ? STATE_READY : STATE_OPEN);
                } else if (isReady) {
                    changeRefreshState(STATE_REFRESHING);
                }
                break;
            }
            case STATE_READY: {// 当前状态就绪
                if (!isRefreshable) {
                    changeRefreshState(STATE_UNABLE);
                } else if (isFinalState) {
                    changeRefreshState(STATE_REFRESHING);
                } else if (scrollY == 0) {
                    changeRefreshState(STATE_CLOSE);
                } else if (!isReady) {
                    changeRefreshState(STATE_OPEN);
                }
                break;
            }
            case STATE_REFRESH_COMPLETE: {// 当前状态已完成
                if (scrollY == 0 || isFinalState) {
                    changeRefreshState(STATE_CLOSE);
                }
                break;
            }
            case STATE_REFRESHING: // 当前状态刷新中
                // 不做任何状态更改，因为刷新中是不可以滑动的
            default: {
                break;
            }
        }
    }

    /**
     * 判断当前刷新视图是否可刷新
     */
    private boolean isCurRefreshViewRefreshable() {
        if (mCurRefreshView == null) {
            return false;
        } else if (mCurRefreshView == mHeaderRefreshView) {
            return isHeaderRefreshable();
        } else if (mCurRefreshView == mFooterRefreshView) {
            return isFooterRefreshable();
        }
        return false;
    }

    /**
     * 通知刷新状态改变
     */
    private void notifyOnRefresh() {
        if (mOnRefreshListener != null && mCurRefreshView != null) {
            if (mCurRefreshView == mHeaderRefreshView) {
                mOnRefreshListener.onHeaderRefresh(this, isChanged, mRefreshState, getScrollY());
            } else if (mCurRefreshView == mFooterRefreshView) {
                mOnRefreshListener.onFooterRefresh(this, isChanged, mRefreshState, getScrollY());
            }
        }
        isChanged = false;
    }

    @Override
    public void scrollTo(int x, int y) {
        if (!mTouchedChildren.isEmpty()) {
            cancelPressedState();
        }
        super.scrollTo(x, y);
    }

    /**
     * 调整child的padding值。因为容器滑动后child会产生偏移，部分区域会滑出View区域导致不可见
     */
    protected void adjustChildScrollPadding() {
        if (!hasVerticallyScrollChild()) {
            return;
        }
        View child = getChildView();
        final int scrollY = getScrollY();
        if (mRefreshState == STATE_REFRESHING) {
            if (scrollY < 0) {//Header
                int diff = scrollY + child.getPaddingBottom();
                if (diff != 0) {
                    child.setPadding(child.getPaddingLeft(), 0, child.getPaddingRight(), -scrollY);
                }
            } else {//Footer
                int diff = scrollY - child.getPaddingTop();
                if (diff != 0) {
                    child.setPadding(child.getPaddingLeft(), scrollY, child.getPaddingRight(), 0);
                    if (child instanceof ScrollView) {
                        child.scrollBy(0, diff);
                    }
                }
            }
        } else if (child.getPaddingTop() != 0 || child.getPaddingBottom() != 0) {
            int diff = -child.getPaddingTop();
            child.setPadding(child.getPaddingLeft(), 0, child.getPaddingRight(), 0);
            if (child instanceof ScrollView) {
                child.scrollBy(0, diff);
            }
        }
    }

    /**
     * 改变当前刷新视图
     */
    private void changeRefreshView(View refreshView) {
        if (mCurRefreshView != refreshView) {
            mCurRefreshView = refreshView;
            isChanged = true;
        }
    }

    /**
     * 改变当前刷新状态
     */
    private void changeRefreshState(int refreshState) {
        if (mRefreshState != refreshState) {
            mRefreshState = refreshState;
            isChanged = true;
        }
    }

    /**
     * 初始化回归动画
     */
    private void initRegressAnimator() {
        if (mRegressAnimator == null) {
            //初始化回归动画，关闭弹性效果
            mRegressAnimator = new DecelerateAnimator(this.getContext(), false);
            //设置动画周期监听
            mRegressAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    isRegressAnimatorCanceled = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!isRegressAnimatorCanceled) {
                        //动画完成，通知状态改变
                        float finalValue = mRegressAnimator.getFinalValue();
                        scrollToRefresh((int) (finalValue + (finalValue < 0 ? -0.5F : 0.5F)), false, true);
                    }
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    isRegressAnimatorCanceled = false;
                }

            });
            //设置动画更新监听
            mRegressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (Float) animation.getAnimatedValue();
                    scrollToRefresh((int) (value + (value < 0 ? -0.5F : 0.5F)), true, false);
                }
            });
        }
    }

    /**
     * 根据指定状态或当前偏移量计算最终偏移量，并开始回归动画
     */
    private void startRegressAnimator(int endValue) {
        startRegressAnimatorDelayed(endValue, 0);
    }

    /**
     * 根据指定状态或当前偏移量计算最终偏移量，并延迟开始回归动画
     */
    private void startRegressAnimatorDelayed(final int endValue, long delayMillis) {
        if (mRegressAnimator == null) {
            //初始化回归动画
            initRegressAnimator();
        } else {
            //先取消动画
            mRegressAnimator.cancel();
        }
        postDelayed(new Runnable() {
            @Override
            public void run() {
                mRegressAnimator.setFlingFrictionRatio(mRefreshState == STATE_REFRESH_COMPLETE ? 0.2F : 1.3F);
                mRegressAnimator.startAnimator(getScrollY(), endValue, mRefreshState == STATE_REFRESH_COMPLETE ? 500 : 400);
            }
        }, delayMillis);
    }

    /**
     * 判断自身是否会被延时设置按下状态
     */
    private boolean isInScrollingContainer() {
        ViewParent parent = this;
        while (parent instanceof ViewGroup) {
            if (((ViewGroup) parent).shouldDelayChildPressedState()) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    /**
     * 判断childView是否支持垂直滑动
     * <p>
     * 该方法可以被子类重写，以适应某些特殊的自定义View
     */
    protected boolean hasVerticallyScrollChild() {
        View view = getChildView();
        if (view == null) {
            return false;
        }
        if (view instanceof ScrollView) {
            return true;
        }
        if (view instanceof ListView) {
            return true;
        }
        try {
            Class<?> RecyclerViewClass_v7 = Class.forName("android.support.v7.widget.RecyclerView");
            if (RecyclerViewClass_v7.isInstance(view)) {
                return true;
            }
        } catch (Exception ignored) {
        }
        try {
            Class<?> RecyclerViewClass_x = Class.forName("androidx.recyclerview.widget.RecyclerView");
            if (RecyclerViewClass_x.isInstance(view)) {
                return true;
            }
        } catch (Exception ignored) {
        }
        if (view.canScrollVertically(1)) {
            return true;
        }
        if (view.canScrollVertically(-1)) {
            return true;
        }
        return false;
    }

    /**
     * 判断顶部是否可用（可以被拉开，但不一定可刷新）
     */
    public boolean isHeaderEnabled() {
        return isEnabled() && isHeaderEnabled && mHeaderRefreshView != null;
    }

    /*==========以下是外部接口==========*/

    /**
     * 设置顶部是否可用（可以被拉开，但不一定可刷新）
     */
    public void setHeaderEnabled(boolean enable) {
        isHeaderEnabled = enable;
    }

    /**
     * 判断底部是否可用（可以被拉开，但不一定可刷新）
     */
    public boolean isFooterEnabled() {
        return isEnabled() && isFooterEnabled && mFooterRefreshView != null;
    }

    /**
     * 设置底部是否可用（可以被拉开，但不一定可刷新）
     */
    public void setFooterEnabled(boolean enable) {
        isFooterEnabled = enable;
    }

    /**
     * 判断顶部是否可以刷新
     */
    public boolean isHeaderRefreshable() {
        return isHeaderEnabled() && isHeaderRefreshable && mOnRefreshListener != null;
    }

    /**
     * 设置顶部是否可以刷新
     */
    public void setHeaderRefreshable(boolean enable) {
        if (enable) {
            setHeaderEnabled(true);
        }
        isHeaderRefreshable = enable;
    }

    /**
     * 判断底部是否可以刷新
     */
    public boolean isFooterRefreshable() {
        return isFooterEnabled() && isFooterRefreshable && mOnRefreshListener != null;
    }

    /**
     * 设置底部是否可以刷新
     */
    public void setFooterRefreshable(boolean enable) {
        if (enable) {
            setFooterEnabled(true);
        }
        isFooterRefreshable = enable;
    }

    /**
     * 判断顶部刷新是否折叠
     */
    public boolean isHeaderRefreshFolded() {
        return isHeaderRefreshFolded;
    }

    /**
     * 设置顶部刷新是否折叠
     */
    public void setHeaderRefreshFolded(boolean folded) {
        isHeaderRefreshFolded = folded;
    }

    /**
     * 判断底部刷新是否折叠
     */
    public boolean isFooterRefreshFolded() {
        return isFooterRefreshFolded;
    }

    /**
     * 设置底部刷新是否折叠
     */
    public void setFooterRefreshFolded(boolean folded) {
        isFooterRefreshFolded = folded;
    }

    /**
     * 获取顶部刷新控件
     */
    public View getHeaderRefreshView() {
        return mHeaderRefreshView;
    }

    /**
     * 设置顶部刷新控件
     */
    public void setHeaderRefreshView(@LayoutRes int headerLayoutID) {
        View headerRefreshView = headerLayoutID == NO_ID ? null : LayoutInflater.from(getContext()).inflate(headerLayoutID, this, false);
        setHeaderRefreshView(headerRefreshView);
    }

    /**
     * 设置顶部刷新控件
     */
    public void setHeaderRefreshView(View headerRefreshView) {
        if (headerRefreshView != mHeaderRefreshView) {
            if (mHeaderRefreshView != null) {
                removeView(mHeaderRefreshView);
            }
            if (headerRefreshView != null) {
                ViewGroup.LayoutParams params = headerRefreshView.getLayoutParams();
                if (params == null) {
                    params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                }
                super.addView(headerRefreshView, 0, params);
            }
            mHeaderRefreshView = headerRefreshView;
        }
    }

    /**
     * 获取底部刷新控件
     */
    public View getFooterRefreshView() {
        return mFooterRefreshView;
    }

    /**
     * 设置底部刷新控件
     */
    public void setFooterRefreshView(@LayoutRes int footerLayoutID) {
        View footerRefreshView = footerLayoutID == NO_ID ? null : LayoutInflater.from(getContext()).inflate(footerLayoutID, this, false);
        setFooterRefreshView(footerRefreshView);
    }

    /**
     * 设置底部刷新控件
     */
    public void setFooterRefreshView(View footerRefreshView) {
        if (footerRefreshView != mFooterRefreshView) {
            if (mFooterRefreshView != null) {
                removeView(mFooterRefreshView);
            }
            if (footerRefreshView != null) {
                ViewGroup.LayoutParams params = footerRefreshView.getLayoutParams();
                if (params == null) {
                    params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                }
                super.addView(footerRefreshView, 0, params);
            }
            mFooterRefreshView = footerRefreshView;
        }
    }

    /**
     * 设置刷新回调
     */
    public void setOnRefreshListener(OnRefreshListener listener) {
        this.mOnRefreshListener = listener;
    }

    /**
     * 通知刷新完成，并开始回归动画
     */
    public void notifyRefreshComplete() {
        notifyRefreshCompleteDelayed(0);
    }

    /**
     * 通知刷新完成，并延时开始回归动画
     */
    public void notifyRefreshCompleteDelayed(long delayMillis) {
        if (mCurRefreshView == null || mRefreshState != STATE_REFRESHING) {
            return;
        }
        changeRefreshState(STATE_REFRESH_COMPLETE);
        notifyOnRefresh();
        startRegressAnimatorDelayed(0, delayMillis);
    }

    /**
     * 请求顶部刷新
     * <p>
     * 注意：
     * <p>
     * 1.不会对childView做任何改变，所以需要自己将childView滚动到顶部，再请求顶部刷新
     * <p>
     * 2.如果要在进入activity的时候自动刷新，请在{@link Activity#onEnterAnimationComplete()}中处理，原因参见该API注释。
     */
    public void requestHeaderRefresh() {
        if (isHeaderRefreshable() && mRefreshState != STATE_REFRESH_COMPLETE && mRefreshState != STATE_REFRESHING) {
            mCurRefreshView = mHeaderRefreshView;
            changeRefreshState(STATE_REFRESHING);
            notifyOnRefresh();
            startRegressAnimator(isHeaderRefreshFolded() ? 0 : -mHeaderRefreshView.getHeight());
        }
    }

    /**
     * 请求底部刷新
     * <p>
     * 注意：
     * <p>
     * 1.不会对childView做任何改变，所以需要自己将childView滚动到底部，再请求底部刷新
     * <p>
     * 2.如果要在进入activity的时候自动刷新，请在{@link Activity#onEnterAnimationComplete()}中处理，原因参见该API注释。
     */
    public void requestFooterRefresh() {
        if (isFooterRefreshable() && mRefreshState != STATE_REFRESH_COMPLETE && mRefreshState != STATE_REFRESHING) {
            mCurRefreshView = mFooterRefreshView;
            changeRefreshState(STATE_REFRESHING);
            notifyOnRefresh();
            startRegressAnimator(isFooterRefreshFolded() ? 0 : mFooterRefreshView.getHeight());
        }
    }

    /**
     * 获取当前刷新控件
     */
    public View getCurRefreshView() {
        return mCurRefreshView;
    }

    /**
     * 获取childView（排除刷新控件）
     */
    public View getChildView() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child != mHeaderRefreshView && child != mFooterRefreshView) {
                return child;
            }
        }
        return null;
    }

    /**
     * 获取当前刷新状态
     */
    public int getRefreshState() {
        return mRefreshState;
    }

    /**
     * 刷新监听接口
     */
    public interface OnRefreshListener {

        /**
         * 头部刷新状态回调
         *
         * @param parent    父容器
         * @param offset    偏移量
         * @param state     状态值 {@link #STATE_CLOSE}、{@link #STATE_OPEN}、{@link #STATE_READY}、{@link #STATE_REFRESHING}、{@link #STATE_REFRESH_COMPLETE}
         * @param isChanged 状态或视图是否改变
         */
        void onHeaderRefresh(SwipeRefreshLayout parent, boolean isChanged, int state, float offset);

        /**
         * 底部刷新状态回调
         *
         * @param parent    父容器
         * @param offset    偏移量
         * @param state     状态值 {@link #STATE_CLOSE}、{@link #STATE_OPEN}、{@link #STATE_READY}、{@link #STATE_REFRESHING}、{@link #STATE_REFRESH_COMPLETE}
         * @param isChanged 状态或视图是否改变
         */
        void onFooterRefresh(SwipeRefreshLayout parent, boolean isChanged, int state, float offset);
    }

}
