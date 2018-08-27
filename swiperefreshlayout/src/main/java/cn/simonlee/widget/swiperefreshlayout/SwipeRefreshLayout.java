package cn.simonlee.widget.swiperefreshlayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
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
 * 1. 解决横向滑动冲突
 * 2. 加入覆盖刷新
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-08-16
 */
@SuppressWarnings({"unused", "ClickableViewAccessibility"})
public class SwipeRefreshLayout extends FrameLayout {

    /**
     * 刷新状态：关闭
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
     * 刷新状态：正在刷新
     */
    public static final int STATE_REFRESH = 3;

    /**
     * 刷新状态：刷新成功
     */
    public static final int STATE_REFRESH_SUCCESS = 4;

    /**
     * 刷新状态：刷新失败
     */
    @Deprecated
    public static final int STATE_REFRESH_FAIL = 5;

    /**
     * 当前刷新状态
     */
    private int mRefreshState;

    /**
     * 当前刷新控件 (mHeaderRefreshView or mFooterRefreshView)
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
     * 标志是否可以下拉刷新
     */
    private boolean mHeaderRefreshable = true;

    /**
     * 标志是否可以上滑刷新
     */
    private boolean mFooterRefreshable = true;

    /**
     * 刷新监听接口
     */
    public interface OnRefreshListener {

        /**
         * 刷新状态回调
         *
         * @param parent      父容器
         * @param refreshView 刷新视图，用于在回调中处理动画效果
         * @param state       刷新状态值
         * @param offsetY     Y轴偏移量，用于区分是下拉刷新还是上拉加载，以及处理动画进度
         */
        void onRefresh(SwipeRefreshLayout parent, View refreshView, int state, float offsetY);

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
    private final int mTouchSlop;

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
        //判断滑动事件的最小距离
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public SwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        //判断滑动事件的最小距离
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public SwipeRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //判断滑动事件的最小距离
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mHeaderRefreshView != null) {
            //设置偏移量，使下拉刷新控件隐藏到顶部
            mHeaderRefreshView.offsetTopAndBottom(-mHeaderRefreshView.getBottom());
        }
        if (mFooterRefreshView != null) {
            //设置偏移量，使上滑刷新控件隐藏到底部
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
        //当不可用 或 不支持刷新 或 没有childView 或 正在刷新当中，不干预触摸事件。
        if (!isEnabled() || (!isHeaderRefreshable() && !isFooterRefreshable()) || getChildView() == null || mRefreshState == STATE_REFRESH) {
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
//                    isMoveAction = true;
//                    super.dispatchTouchEvent(event);
//                    for (View child : mDelayPressedChildren) {
//                        child.cancelLongPress();
//                    }
//                    setPressed(false);
//                    return true;
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
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                int actionIndex = event.getActionIndex();
                if (event.getPointerId(actionIndex) == mTouchPointerId) {
                    //记录下一个触摸点
                    recordNextTouchPointer(event, actionIndex);
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                for (int index = 0; index < event.getPointerCount(); index++) {
                    if (event.getPointerId(index) == mTouchPointerId) {
                        //当前触摸事件Y坐标
                        float curY = event.getY(index);
                        if (isMoveAction) {//已经开始位移
                            //计算滚动Y坐标
                            int scrollY = (int) ((mPerTouchY - curY) / mDamping + 0.5F) + getScrollY();
                            //记录当前触摸事件Y坐标
                            mPerTouchY = curY;
                            //滚动刷新事件
                            if (scrollToRefresh(scrollY)) {
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
                //发生过刷新事件
                if (mRefreshState != STATE_CLOSE) {
                    //开启回归动画
                    startRegressAnimator(getScrollY(), getScrollY());
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

    @Override
    public void scrollTo(int x, int y) {
        //标志是否发生Y轴滚动
        boolean change = getScrollY() != y;
        if (change && needCancelPressedWhenScroll) {
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
        if (change) {
            if (y > 0) {
                mCurRefreshView = mFooterRefreshView;
            } else if (y < 0) {
                mCurRefreshView = mHeaderRefreshView;
            }
            //通知刷新状态改变
            notifyRefresh(false);
        }
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
     * 滚动刷新事件
     *
     * @param scrollY Y轴滚动位置
     */
    private boolean scrollToRefresh(int scrollY) {
        if (getScrollY() == 0 && scrollY != 0) {//从未滑动变为滑动
            //判断下拉动作
            boolean pullDown = scrollY < 0;
            //当未触摸ChildView或ChildView不可滑动，并且可以刷新，则开始滑动
            if ((!isTouchChild || !canScrollChildVertically(pullDown)) &&
                    ((pullDown && isHeaderRefreshable()) || (!pullDown && isFooterRefreshable()))) {
                scrollTo(0, scrollY);
                return true;
            }
        } else if (scrollY * getScrollY() > 0) {//继续同边滑动
            scrollTo(0, scrollY);
            return true;
        } else if (getScrollY() != 0) {//从滑动变为未滑动
            // Y轴滚动位置强制为0，因为不允许异边滑动，比如从下拉变为上滑会导致childView的滑动事件无法响应
            scrollTo(0, 0);
        }
        return false;
    }

    /**
     * 通知刷新状态改变
     *
     * @param isFinalState 是否为最终状态
     */
    private void notifyRefresh(boolean isFinalState) {
        //当前刷新控件为null，直接返回
        if (mCurRefreshView == null) return;
        //当刷新状态非刷新完成时，根据滚动距离和isFinalState标志更新状态
        if (mRefreshState != STATE_REFRESH_SUCCESS) {
            if (Math.abs(getScrollY()) >= mCurRefreshView.getHeight()) {
                mRefreshState = isFinalState ? STATE_REFRESH : STATE_READY;
            } else {
                mRefreshState = isFinalState ? STATE_CLOSE : STATE_ENABLE;
            }
        } else if (isFinalState) {
            //当刷新状态为刷新完成，且最终状态时，将状态改为关闭
            mRefreshState = STATE_CLOSE;
        }
        if (mOnRefreshListener != null) {
            mOnRefreshListener.onRefresh(this, mCurRefreshView, mRefreshState, getScrollY());
        }
    }

    /**
     * 开始回归动画，根据动画值设置Y轴滚动距离
     *
     * @param startValue 动画的初始值
     * @param endValue   动画的终止值
     */
    private void startRegressAnimator(int startValue, int endValue) {
        if (mRefreshState == STATE_REFRESH_SUCCESS) {
            //当前为刷新完成状态，终止值强制为0
            endValue = 0;
        } else if (endValue <= -mHeaderRefreshView.getHeight()) {
            //终止值小于负的下拉控件高度，说明是下拉。则更改终止值为负的下拉控件高度
            endValue = -mHeaderRefreshView.getHeight();
        } else if (endValue >= mFooterRefreshView.getHeight()) {
            //终止值大于上滑控件高度，说明是上滑。则更改终止值为上滑控件高度
            endValue = mFooterRefreshView.getHeight();
        } else {
            //终止值既不满足上滑也不满足下拉，强制为0
            endValue = 0;
        }
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
            mRegressAnimator.addUpdateListener(animation -> {
                float scrollY = (Float) animation.getAnimatedValue();
                scrollTo(0, (int) scrollY);
            });
        }
        //起始值和终止值不等，开始动画
        if (endValue != startValue) {
            mRegressAnimator.setFlingFrictionRatio(mRefreshState == STATE_REFRESH_SUCCESS ? 0.1F : 1F);
            mRegressAnimator.startAnimator(startValue, endValue, 400);
        } else {
            //起始值和终止值相等，直接通知状态改变
            notifyRefresh(true);
        }
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
     * 判断childView是否可以下拉或者上滑
     * <p>
     * 该方法可以被子类重写，以适应某些特殊的自定义View
     *
     * @param pullDown 为真则为下拉，否则为上滑
     */
    public boolean canScrollChildVertically(boolean pullDown) {
        View view = getChildView();
        return view != null && view.canScrollVertically(pullDown ? -1 : 1);
    }

    /*==========以下是外部接口==========*/

    /**
     * 设置是否可以下拉刷新
     */
    public void setHeaderRefreshable(boolean enable) {
        mHeaderRefreshable = enable;
    }

    /**
     * 设置是否可以上滑刷新
     */
    public void setFooterRefreshable(boolean enable) {
        mFooterRefreshable = enable;
    }

    /**
     * 判断是否可以下拉刷新
     */
    public boolean isHeaderRefreshable() {
        return mHeaderRefreshable && mHeaderRefreshView != null && mOnRefreshListener != null;
    }

    /**
     * 判断底部是否可以上滑刷新
     */
    public boolean isFooterRefreshable() {
        return mFooterRefreshable && mFooterRefreshView != null && mOnRefreshListener != null;
    }

    /**
     * 设置下拉刷新控件
     */
    public void setHeaderRefreshView(View headerRefreshView) {
        setHeaderRefreshView(headerRefreshView, mOnRefreshListener);
    }

    /**
     * 设置下拉刷新控件及回调
     */
    public void setHeaderRefreshView(View headerRefreshView, OnRefreshListener listener) {
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
        setOnRefreshListener(listener);
    }

    /**
     * 设置上滑刷新控件
     */
    public void setFooterRefreshView(View footerRefreshView) {
        setFooterRefreshView(footerRefreshView, mOnRefreshListener);
    }

    /**
     * 设置上滑刷新控件及回调
     */
    public void setFooterRefreshView(View footerRefreshView, OnRefreshListener listener) {
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
        setOnRefreshListener(listener);
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
        if (getScrollY() != 0) {
            mRefreshState = STATE_REFRESH_SUCCESS;
            startRegressAnimator(getScrollY(), 0);
        }
    }

    /**
     * 请求下拉刷新。
     * <p>
     * 注意：
     * <p>
     * 1.不会对childView做任何改变，所以需要自己将childView滚动到顶部，再请求下拉刷新
     * <p>
     * 2.如果要在进入activity的时候自动刷新，请在{@link Activity#onEnterAnimationComplete()}中处理，原因见注释，同时切记使用post。
     */
    public void requestHeaderRefresh() {
        if (isHeaderRefreshable()) {
            startRegressAnimator(getScrollY(), -mHeaderRefreshView.getHeight());
        }
    }

    /**
     * 请求上滑刷新。
     * <p>
     * 注意：
     * <p>
     * 1.不会对childView做任何改变，所以需要自己将childView滚动到底部，再请求上滑刷新
     * <p>
     * 2.如果要在进入activity的时候自动刷新，请在{@link Activity#onEnterAnimationComplete()}中处理，原因见注释，同时切记使用post。
     */
    public void requestFooterRefresh() {
        if (isFooterRefreshable()) {
            startRegressAnimator(getScrollY(), mFooterRefreshView.getHeight());
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
