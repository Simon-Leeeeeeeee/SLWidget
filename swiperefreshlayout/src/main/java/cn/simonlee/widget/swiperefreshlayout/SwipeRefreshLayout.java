package cn.simonlee.widget.swiperefreshlayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
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

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;

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
@SuppressWarnings({"unused", "ClickableViewAccessibility", "BooleanMethodIsAlwaysInverted"})
public class SwipeRefreshLayout extends FrameLayout {

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
     * 标记当Scroll时取消按压状态
     */
    private boolean needCancelPressedWhenScroll;

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
     * 触摸事件的x,y坐标
     */
    private float mTouchDownX, mTouchDownY;

    /**
     * 上一个触摸事件的y坐标
     */
    private float mPerTouchY;

    /**
     * 触摸拖动的总偏移量
     */
    private float mTotalOffsetY;

    /**
     * 标志是否垂直触摸移动（手指在屏幕上拖动）
     */
    private boolean isMoveAction;

    /**
     * 标志ACTION_DOWN事件中是否触摸到ChildView
     */
    private boolean isTouchChild;

    /**
     * 被延时设置按下状态的View集合。在ACTION_DOWN事件中被赋值，在ScrollTo中被取消选中状态
     */
    private List<View> mDelayPressedChildren = new ArrayList<>();

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
        //当不可用 或 没有childView 或 没有指定刷新 或 正在刷新中 或 正在结束刷新，不干预触摸事件。
        if ((!isHeaderEnabled() && !isFooterEnabled()) || getChildView() == null || mRefreshState == STATE_REFRESHING || mRefreshState == STATE_REFRESH_COMPLETE) {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                mDelayPressedChildren.clear();
                needCancelPressedWhenScroll = true;
            }
            return super.dispatchTouchEvent(event);
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                //记录触摸点
                recordTouchPointer(event, event.getActionIndex());
                //清空集合
                mDelayPressedChildren.clear();
                //集合所有被延时设置按下状态的View，包括自身
                collectDelayPressedView(this, mTouchDownX + getLeft(), mTouchDownY + getTop(), isInScrollingContainer());
                //标志是否触摸到了ChildView
                isTouchChild = pointInView(getChildView(), mTouchDownX + getScrollX(), mTouchDownY + getScrollY());
                //取消回归动画
                if (mRegressAnimator != null && mRegressAnimator.isStarted()) {
                    mRegressAnimator.cancel();
                    isMoveAction = true;
                    //计算总偏移量
                    mTotalOffsetY = getScrollY() * mDamping;
                    super.dispatchTouchEvent(event);
                    for (View child : mDelayPressedChildren) {
                        child.cancelLongPress();
                    }
                    setPressed(false);
                    return true;
                }
                //标志当Scroll时需要取消按压状态
                needCancelPressedWhenScroll = true;
                //标志未发生移动
                isMoveAction = false;
                break;
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
                        float curY = event.getY(index);
                        if (isMoveAction) {//已经开始位移
                            if (getScrollY() == 0) {//重置总偏移量
                                mTotalOffsetY = mPerTouchY - curY;
                            } else {//累加总偏移量
                                mTotalOffsetY += mPerTouchY - curY;
                            }
                            //记录当前触摸事件Y坐标
                            mPerTouchY = curY;
                            //计算Y轴滚动偏移量
                            int scrollY = (int) (mTotalOffsetY / mDamping + (mTotalOffsetY < 0 ? -0.5F : 0.5F));
                            //判断是否可以拉开刷新
                            if (!canScrollRefresh(scrollY)) {
                                scrollToRefresh(0, false, false);
                            } else {
                                scrollToRefresh(scrollY, false, false);
                                return true;
                            }
                        } else if (Math.abs(mTouchDownY - curY) >= mTouchSlop) {//判断即将发生位移
                            //更新标志
                            isMoveAction = true;
                            //记录当前触摸事件Y坐标
                            mPerTouchY = curY;
                        }
                        break;
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                isMoveAction = false;
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
                    startRegressAnimator(endValue);
                    //下发一个取消事件给子View
                    MotionEvent cancelEvent = MotionEvent.obtain(event);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
                    super.dispatchTouchEvent(cancelEvent);
                    return true;
                }
                break;
            }
        }
        super.dispatchTouchEvent(event);
        return true;
    }

    /**
     * 记录触摸坐标及ID
     */
    private void recordTouchPointer(MotionEvent event, int actionIndex) {
        //记录触摸坐标
        mTouchDownX = event.getX(actionIndex);
        mTouchDownY = event.getY(actionIndex);
        mPerTouchY = mTouchDownY;
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
        if (getScrollY() == 0) {//当前未滚动
            if (!isTouchChild || !canScrollChildVertically(scrollY)) {//child不可滚动
                //判断是否可以展开
                return scrollY < 0 ? isHeaderEnabled() : isFooterEnabled();
            }
        }
        //滚动变向则不展开
        return scrollY * getScrollY() > 0;
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
            //根据偏移量改变刷新状态
            changeState(isFinalState);
        }
        //触摸移动中，根据偏移量指定刷新状态
        adjustChildScrollPadding();
        //通知刷新状态改变
        notifyOnRefresh();
    }

    /**
     * 根据偏移量改变刷新状态
     *
     * @param isFinalState 是否为最终状态
     */
    private void changeState(boolean isFinalState) {
        final int scrollY = getScrollY();

        if (scrollY != 0) {
            // 根据滑动距离变更刷新控件
            changeRefreshView(scrollY > 0 ? mFooterRefreshView : mHeaderRefreshView);
        }

        final boolean isReady = mCurRefreshView != null && Math.abs(scrollY) >= mCurRefreshView.getHeight();

        switch (mRefreshState) {
            case STATE_CLOSE: {// 当前状态关闭
                if (scrollY != 0) {
                    changeRefreshState(isReady ? STATE_READY : STATE_OPEN);
                }
                break;
            }
            case STATE_OPEN: {// 当前状态打开
                if (scrollY == 0) {
                    changeRefreshState(STATE_CLOSE);
                } else if (!isFinalState) {
                    changeRefreshState(isReady ? STATE_READY : STATE_OPEN);
                } else if (isReady) {
                    changeRefreshState(STATE_REFRESHING);
                } else {
                    changeRefreshState(STATE_CLOSE);
                }
                break;
            }
            case STATE_READY: {// 当前状态就绪
                if (isFinalState) {
                    changeRefreshState(STATE_REFRESHING);
                } else if (scrollY == 0) {
                    changeRefreshState(STATE_CLOSE);
                } else {
                    changeRefreshState(isReady ? STATE_READY : STATE_OPEN);
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
     * 通知刷新状态改变
     */
    private void notifyOnRefresh() {
        if (mOnRefreshListener != null && mCurRefreshView != null) {
            mOnRefreshListener.onRefresh(this, mCurRefreshView, getScrollY(), mRefreshState, isChanged);
        }
        isChanged = false;
    }

    @Override
    public void scrollTo(int x, int y) {
        if (needCancelPressedWhenScroll) {
            needCancelPressedWhenScroll = false;
            //取消所有被延时设置按下状态的View的延时设置
            for (View child : mDelayPressedChildren) {
                child.cancelLongPress();
            }
            mDelayPressedChildren.clear();
            //设置为未按下状态
            setPressed(false);
        }
        super.scrollTo(x, y);
    }

    private void adjustChildScrollPadding() {
        final int scrollY = getScrollY();
        if (!canScrollChildVertically()) {
            return;
        }
        View child = getChildView();
        if (mRefreshState == STATE_REFRESHING) {
            if (scrollY < 0) {//Header
                int diff = scrollY + child.getPaddingTop();
                if (diff != 0) {
                    child.setPadding(child.getPaddingLeft(), 0, child.getPaddingRight(), -scrollY);
                }
            } else {//Footer
                int diff = scrollY - child.getPaddingTop();
                if (diff != 0) {
                    child.setPadding(child.getPaddingLeft(), scrollY, child.getPaddingRight(), 0);
                    child.scrollBy(0, diff);
                }
            }
        } else if (child.getPaddingTop() != 0 || child.getPaddingBottom() != 0) {
            int diff = -child.getPaddingTop();
            child.setPadding(child.getPaddingLeft(), 0, child.getPaddingRight(), 0);
            child.scrollBy(0, diff);
        }
    }

    private void changeRefreshView(View refreshView) {
        if (mCurRefreshView != refreshView) {
            mCurRefreshView = refreshView;
            isChanged = true;
        }
    }

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
        if (mRegressAnimator == null) {
            //初始化回归动画
            initRegressAnimator();
        } else {
            //先取消动画
            mRegressAnimator.cancel();
        }
        mRegressAnimator.setFlingFrictionRatio(mRefreshState == STATE_REFRESH_COMPLETE ? 0.1F : 1.3F);
        mRegressAnimator.startAnimator(getScrollY(), endValue, 400);
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
     * 递归集合所有被延时设置按下状态的View
     *
     * @param view              目标view
     * @param touchDownX        按下时的X坐标，相对目标view的父容器
     * @param touchDownY        按下时的Y坐标，相对目标view的父容器
     * @param delayPressedState 是否被延时设置
     */
    private void collectDelayPressedView(View view, float touchDownX, float touchDownY, boolean delayPressedState) {
        //判断view是否可见
        if (view != null && view.getVisibility() == VISIBLE) {
            //判断触摸点是否在view上
            if (pointInView(view, touchDownX, touchDownY)) {
                if (delayPressedState) {
                    //如果被延时设置，则加入集合
                    mDelayPressedChildren.add(view);
                }
                //判断是否含有childView
                if (view instanceof ViewGroup) {
                    ViewGroup viewGroup = ((ViewGroup) view);
                    //校正childView相对触摸坐标
                    touchDownX += view.getScrollX() - view.getLeft();
                    touchDownY += view.getScrollY() - view.getTop();
                    //更新延时设置标志
                    delayPressedState |= viewGroup.shouldDelayChildPressedState();
                    for (int index = 0; index < viewGroup.getChildCount(); index++) {
                        //递归集合childView
                        collectDelayPressedView(viewGroup.getChildAt(index), touchDownX, touchDownY, delayPressedState);
                    }
                }
            }
        }
    }

    /**
     * 判断childView是否可以滑动
     * <p>
     * 该方法可以被子类重写，以适应某些特殊的自定义View
     */
    protected boolean canScrollChildVertically() {
        View view = getChildView();
        return view != null && (view.canScrollVertically(1) || view.canScrollVertically(-1));
    }

    /**
     * 判断childView是否可以下拉或者上滑
     * <p>
     * 该方法可以被子类重写，以适应某些特殊的自定义View
     *
     * @param direction 负值表示为下拉动作，否则为上拉
     */
    protected boolean canScrollChildVertically(int direction) {
        View view = getChildView();
        return view != null && view.canScrollVertically(direction);
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
        if (mCurRefreshView == null || mRefreshState != STATE_REFRESHING) {
            return;
        }
        if ((mCurRefreshView != mHeaderRefreshView || !isHeaderRefreshable()) && (mCurRefreshView != mFooterRefreshView || !isFooterRefreshable())) {
            return;
        }
        changeRefreshState(STATE_REFRESH_COMPLETE);
        notifyOnRefresh();
        startRegressAnimator(0);
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
         * 刷新状态回调
         *
         * @param parent      父容器
         * @param refreshView 刷新视图
         * @param offsetY     偏移量
         * @param state       状态值 {@link #STATE_CLOSE}、{@link #STATE_OPEN}、{@link #STATE_READY}、{@link #STATE_REFRESHING}、{@link #STATE_REFRESH_COMPLETE}
         * @param isChanged   状态或视图是否改变
         */
        void onRefresh(SwipeRefreshLayout parent, View refreshView, float offsetY, int state, boolean isChanged);

    }

}
