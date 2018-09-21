package cn.simonlee.widget.swiperefreshlayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
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
@SuppressWarnings({"unused", "ClickableViewAccessibility"})
public class SwipeRefreshLayout extends FrameLayout {

    /**
     * 刷新状态：已关闭
     */
    public static final int STATE_CLOSE = 0;

    /**
     * 刷新状态：已激活
     */
    public static final int STATE_ENABLE = 1;

    /**
     * 刷新状态：已就绪
     */
    public static final int STATE_READY = 2;

    /**
     * 刷新状态：顶部刷新中
     */
    public static final int STATE_REFRESHING_HEADER = 3;

    /**
     * 刷新状态：底部刷新中
     */
    public static final int STATE_REFRESHING_FOOTER = 4;

    /**
     * 刷新状态：刷新完成
     */
    public static final int STATE_REFRESH_COMPLETE = 5;

    /**
     * 当前刷新状态
     */
    private int mRefreshState;

    /**
     * 标志刷新状态是否改变
     */
    private boolean isStateChange = false;

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
     * 刷新监听接口
     */
    public interface OnRefreshListener {

        /**
         * 顶部刷新状态回调
         *
         * @param parent        父容器
         * @param offsetY       偏移量
         * @param state         状态值 {@link #STATE_CLOSE}、{@link #STATE_ENABLE}、{@link #STATE_READY}、{@link #STATE_REFRESHING_HEADER}、{@link #STATE_REFRESH_COMPLETE}
         * @param isStateChange 状态是否改变
         * @param isFinalState  是否最终状态
         */
        void onHeaderRefresh(SwipeRefreshLayout parent, float offsetY, int state, boolean isStateChange, boolean isFinalState);

        /**
         * 顶部刷新状态回调
         *
         * @param parent        父容器
         * @param offsetY       偏移量
         * @param state         状态值 {@link #STATE_CLOSE}、{@link #STATE_ENABLE}、{@link #STATE_READY}、{@link #STATE_REFRESHING_HEADER}、{@link #STATE_REFRESH_COMPLETE}
         * @param isStateChange 状态是否改变
         * @param isFinalState  是否最终状态
         */
        void onFooterRefresh(SwipeRefreshLayout parent, float offsetY, int state, boolean isStateChange, boolean isFinalState);

    }

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
        mHeaderRefreshView = headerLayoutID == NO_ID ? null : LayoutInflater.from(context).inflate(headerLayoutID, this, false);
        if (mHeaderRefreshView != null) {
            ViewGroup.LayoutParams params = mHeaderRefreshView.getLayoutParams();
            if (params == null) {
                params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            }
            super.addViewInLayout(mHeaderRefreshView, 0, params);
        }

        //底部刷新控件
        int footerLayoutID = typedArray.getResourceId(R.styleable.SwipeRefreshLayout_swiperefresh_footer_layout, NO_ID);
        mFooterRefreshView = footerLayoutID == NO_ID ? null : LayoutInflater.from(context).inflate(footerLayoutID, this, false);
        if (mFooterRefreshView != null) {
            ViewGroup.LayoutParams params = mFooterRefreshView.getLayoutParams();
            if (params == null) {
                params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            }
            super.addViewInLayout(mFooterRefreshView, 0, params);
        }

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
        if ((!isHeaderEnabled() && !isFooterEnabled()) || getChildView() == null || mRefreshState == STATE_REFRESHING_HEADER
                || mRefreshState == STATE_REFRESHING_FOOTER || mRefreshState == STATE_REFRESH_COMPLETE) {
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
                            mPerTouchY = curY;
                            //计算Y轴滚动偏移量
                            int scrollY = (int) (mTotalOffsetY / mDamping + (mTotalOffsetY < 0 ? -0.5F : 0.5F));
                            //记录当前触摸事件Y坐标
                            //判断是否可以拉开
                            if (canScrollExtensible(scrollY)) {
                                scrollToExtensible(scrollY);
                                return true;
                            } else {
                                scrollToExtensible(0);
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
                //发生过刷新事件
                if (mRefreshState != STATE_CLOSE) {
                    //开启回归动画
                    startRegressAnimator();
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
     * 判断是否可以拉开
     *
     * @param scrollY Y轴偏移量
     */
    private boolean canScrollExtensible(int scrollY) {
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
     * @param scrollY 拉开偏移量
     */
    public void scrollToExtensible(int scrollY) {
        if (getScrollY() == scrollY) {
            return;
        }
        //通知刷新状态改变
        scrollTo(getScrollX(), scrollY);
        notifyRefresh(false);
    }

    private void changeRefreshState(int refreshState) {
        if (mRefreshState != refreshState) {
            mRefreshState = refreshState;
            isStateChange = true;
        }
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

    /**
     * 通知刷新状态改变，如果将是最终状态，则对状态进行调整
     *
     * @param isFinalState 是否为最终状态
     */
    private void notifyRefresh(boolean isFinalState) {
        final int scrollY = getScrollY();
        //指定当前刷新视图
        if (scrollY > 0) {
            mCurRefreshView = mFooterRefreshView;
        } else if (scrollY < 0) {
            mCurRefreshView = mHeaderRefreshView;
        }
        if (isFinalState) {//动画完毕的最终状态
            if (mRefreshState == STATE_ENABLE || mRefreshState == STATE_REFRESH_COMPLETE) {
                changeRefreshState(STATE_CLOSE);
            } else if (mRefreshState == STATE_READY) {
                if (mCurRefreshView == mHeaderRefreshView && isHeaderRefreshable()) {
                    changeRefreshState(STATE_REFRESHING_HEADER);
                } else if (mCurRefreshView == mFooterRefreshView && isFooterRefreshable()) {
                    changeRefreshState(STATE_REFRESHING_FOOTER);
                } else {
                    changeRefreshState(STATE_CLOSE);
                }
            } else if (mRefreshState == STATE_REFRESHING_HEADER) {
                mCurRefreshView = mHeaderRefreshView;
            } else if (mRefreshState == STATE_REFRESHING_FOOTER) {
                mCurRefreshView = mFooterRefreshView;
            }
        } else if (isMoveAction) {//触摸移动中，根据偏移量指定刷新状态
            changeRefreshState(scrollY == 0 ? STATE_CLOSE : Math.abs(scrollY) < mCurRefreshView.getHeight() ? STATE_ENABLE : STATE_READY);
        }
        if (mOnRefreshListener != null && mCurRefreshView != null) {
            if (mCurRefreshView == mHeaderRefreshView) {
                mOnRefreshListener.onHeaderRefresh(this, getScrollY(), mRefreshState, isStateChange, isFinalState);
            } else if (mCurRefreshView == mFooterRefreshView) {
                mOnRefreshListener.onFooterRefresh(this, getScrollY(), mRefreshState, isStateChange, isFinalState);
            }
        }
        isStateChange = false;
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
                        notifyRefresh(true);
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
                    float scrollY = (Float) animation.getAnimatedValue();
                    scrollToExtensible((int) (scrollY + (scrollY < 0 ? -0.5F : 0.5F)));
                }
            });
        }
    }

    /**
     * 根据指定状态或当前偏移量计算最终偏移量，并开始回归动画
     */
    private void startRegressAnimator() {
        if (mRegressAnimator != null) {
            //先取消动画
            mRegressAnimator.cancel();
        }
        int endValue;
        if (mRefreshState == STATE_REFRESH_COMPLETE) {//指定状态为刷新完成
            endValue = 0;
        } else if (mRefreshState == STATE_REFRESHING_HEADER) {//指定状态为顶部刷新
            endValue = isHeaderRefreshFolded() ? 0 : -mHeaderRefreshView.getHeight();
        } else if (mRefreshState == STATE_REFRESHING_FOOTER) {//指定状态为底部刷新
            endValue = isFooterRefreshFolded() ? 0 : mFooterRefreshView.getHeight();
        } else if (isHeaderRefreshable() && getScrollY() <= -mHeaderRefreshView.getHeight()) {
            endValue = isHeaderRefreshFolded() ? 0 : -mHeaderRefreshView.getHeight();
        } else if (isFooterRefreshable() && getScrollY() >= mFooterRefreshView.getHeight()) {
            endValue = isFooterRefreshFolded() ? 0 : mFooterRefreshView.getHeight();
        } else {
            endValue = 0;
        }
        if (mRegressAnimator == null) {
            //初始化回归动画
            initRegressAnimator();
        }
        mRegressAnimator.setFlingFrictionRatio(mRefreshState == STATE_REFRESH_COMPLETE ? 0.1F : 1.3F);
        mRegressAnimator.startAnimator(getScrollY(), endValue, 400);
    }

    /**
     * 判断自身是否会被延时设置按下状态
     */
    private boolean isInScrollingContainer() {
        ViewParent parent = this;
        while (parent != null && parent instanceof ViewGroup) {
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

    /*==========以下是外部接口==========*/

    /**
     * 设置顶部是否可用（可以被拉开，但不一定可刷新）
     */
    public void setHeaderEnabled(boolean enable) {
        isHeaderEnabled = enable;
    }

    /**
     * 设置底部是否可用（可以被拉开，但不一定可刷新）
     */
    public void setFooterEnabled(boolean enable) {
        isFooterEnabled = enable;
    }

    /**
     * 判断顶部是否可用（可以被拉开，但不一定可刷新）
     */
    public boolean isHeaderEnabled() {
        return isEnabled() && isHeaderEnabled && mHeaderRefreshView != null;
    }

    /**
     * 判断底部是否可用（可以被拉开，但不一定可刷新）
     */
    public boolean isFooterEnabled() {
        return isEnabled() && isFooterEnabled && mFooterRefreshView != null;
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
     * 设置底部是否可以刷新
     */
    public void setFooterRefreshable(boolean enable) {
        if (enable) {
            setFooterEnabled(true);
        }
        isFooterRefreshable = enable;
    }

    /**
     * 判断顶部是否可以刷新
     */
    public boolean isHeaderRefreshable() {
        return isHeaderEnabled() && isHeaderRefreshable && mOnRefreshListener != null;
    }

    /**
     * 判断底部是否可以刷新
     */
    public boolean isFooterRefreshable() {
        return isFooterEnabled() && isFooterRefreshable && mOnRefreshListener != null;
    }

    /**
     * 设置顶部刷新是否折叠
     */
    public void setHeaderRefreshFolded(boolean folded) {
        isHeaderRefreshFolded = folded;
    }

    /**
     * 设置底部刷新是否折叠
     */
    public void setFooterRefreshFolded(boolean folded) {
        isFooterRefreshFolded = folded;
    }

    /**
     * 判断顶部刷新是否折叠
     */
    public boolean isHeaderRefreshFolded() {
        return isHeaderRefreshFolded;
    }

    /**
     * 判断底部刷新是否折叠
     */
    public boolean isFooterRefreshFolded() {
        return isFooterRefreshFolded;
    }

    /**
     * 获取顶部刷新控件
     */
    public View getHeaderRefreshView() {
        return mHeaderRefreshView;
    }

    /**
     * 获取底部刷新控件
     */
    public View getFooterRefreshView() {
        return mFooterRefreshView;
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
     * 通知刷新完成。将会设置状态为刷新完成，并开始一个回归动画
     */
    public void notifyRefreshComplete() {
        changeRefreshState(STATE_REFRESH_COMPLETE);
        if (getScrollY() == 0) {
            //防止不会回调STATE_REFRESH_COMPLETE状态。因为不会发生实际偏移，动画会直接结束，状态会被强制转为STATE_CLOSE
            notifyRefresh(false);
        }
        startRegressAnimator();
    }

    /**
     * 请求顶部刷新。
     * <p>
     * 注意：
     * <p>
     * 1.不会对childView做任何改变，所以需要自己将childView滚动到顶部，再请求顶部刷新
     * <p>
     * 2.如果要在进入activity的时候自动刷新，请在{@link Activity#onEnterAnimationComplete()}中处理，原因参见该API注释。
     */
    public void requestHeaderRefresh() {
        if (isHeaderRefreshable() && mRefreshState != STATE_REFRESHING_HEADER && mRefreshState != STATE_REFRESHING_FOOTER && mRefreshState != STATE_REFRESH_COMPLETE) {
            changeRefreshState(STATE_REFRESHING_HEADER);
            startRegressAnimator();
        }
    }

    /**
     * 请求底部刷新。
     * <p>
     * 注意：
     * <p>
     * 1.不会对childView做任何改变，所以需要自己将childView滚动到底部，再请求底部刷新
     * <p>
     * 2.如果要在进入activity的时候自动刷新，请在{@link Activity#onEnterAnimationComplete()}中处理，原因参见该API注释。
     */
    public void requestFooterRefresh() {
        if (isFooterRefreshable() && mRefreshState != STATE_REFRESHING_HEADER && mRefreshState != STATE_REFRESHING_FOOTER && mRefreshState != STATE_REFRESH_COMPLETE) {
            changeRefreshState(STATE_REFRESHING_FOOTER);
            startRegressAnimator();
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

}
