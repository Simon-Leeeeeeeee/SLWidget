package cn.simonlee.widget.swipeback;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Activity侧滑返回
 * <p>
 * 原理：
 * <p>
 * 侧滑事件前，利用反射将窗口转为透明；侧滑取消后，利用反射将窗口转为不透明
 * <p>
 * 注意：
 * <p>
 * 1. 仅支持SDK19(Android4.4)及以上
 * <p>
 * 2. 沉浸式状态栏需自行处理
 * <p>
 * 3. SDK21(Android5.0)以下必须在style中设置以下属性，否则{@link #convertToTranslucent()}无效
 * <p>
 * {@code <item name="android:windowIsTranslucent">true</item>}
 * <p>
 * 4. Window背景将会被置为transparent，由R.id.content的Parent来代替实现Window背景，详见{@link #getDecorView()}
 * <p>
 * 5. 侧滑会引起下层Activity生命周期变化，务必留意可能因此导致的问题
 * <p>
 * (a) onDestory -> onCreat -> onStart -> (onResume -> onPause) -> onStop
 * <p>
 * (b) onRestart -> onStart -> (onResume -> onPause) -> onStop
 * <p>
 * 6. 当顶层Activity方向与下层Activity方向不一致时侧滑会失效（下层方向未锁定除外），建议关闭该层Activity侧滑功能
 * <p>
 * 示例场景：视频APP的横屏播放页面和下层竖屏页面
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-06-19
 */
@SuppressWarnings({"unused", "WeakerAccess", "PrivateApi", "JavaReflectionMemberAccess", "JavaReflectionInvocation", "BooleanMethodIsAlwaysInverted", "FieldCanBeLocal"})
@android.support.annotation.RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class SwipeBackHelper {

    /**
     * 未滑动
     */
    protected final int DIRECTION_NONE = 0;

    /**
     * 纵向滑动
     */
    protected final int DIRECTION_VERTICAL = 1;

    /**
     * 横向滑动
     */
    protected final int DIRECTION_HORIZONTAL = 2;

    /**
     * 当前屏幕方向
     */
    protected final int mOrientation;

    /**
     * 判断滑动事件的最小距离
     */
    protected final int mTouchSlop;

    /**
     * 记录侧滑事件开始的xy坐标
     */
    protected float mStartX, mStartY;

    /**
     * 目标Activity
     */
    private Activity mActivity;

    /**
     * 滑动事件方向
     */
    private int mDragDirection;

    /**
     * 屏幕左侧的侧滑触发距离
     */
    private float mSwipeBackEnableDistance;

    /**
     * 根视图
     */
    private ViewGroup mDecorView;

    /**
     * 侧滑操作的视图
     */
    private ViewGroup mSwipeBackView;

    /**
     * 阴影视图
     */
    private View mShadowView;

    /**
     * 滑动速度追踪
     */
    private VelocityTracker mVelocityTracker;

    /**
     * 滑动减速动画
     */
    private DecelerateAnimator mSwipeBackAnimator;

    /**
     * 当前触摸ID
     */
    private int mCurTouchPointerId;

    /**
     * 标志是否允许侧滑
     */
    private boolean isSwipeBackEnabled;

    /**
     * Window转为透明的监听器
     */
    private Object mTranslucentConversionListener;

    /**
     * Window转为透明的监听类
     */
    private Class mTranslucentConversionListenerClass;

    /**
     * 标志Window转为透明完成
     */
    private boolean isTranslucentCompleted;

    /**
     * 动画监听器，处理动画开始、取消、结束三种事件
     */
    private Animator.AnimatorListener mAnimatorListener;

    /**
     * 动画更新监听器，处理动画中的更新事件
     */
    private ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener;

    /**
     * 实例化SwipeBackHelper
     */
    public SwipeBackHelper(@NonNull Activity activity) {
        this(activity, activity);
    }

    /**
     * 实例化SwipeBackHelper
     */
    protected SwipeBackHelper(@NonNull Context context, Activity activity) {
        //判断滑动事件的最小距离
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        //屏幕方向
        this.mOrientation = context.getResources().getConfiguration().orientation;
        //默认开启侧滑功能
        setSwipeBackEnabled(true);
        //设置屏幕左侧的侧滑触发距离，默认18dp
        setSwipeBackEnableDistance(20 * context.getResources().getDisplayMetrics().density);
        //目标Activity
        this.mActivity = activity;
    }

    /**
     * 设置屏幕左侧的侧滑触发距离，单位px
     */
    public void setSwipeBackEnableDistance(float distance) {
        mSwipeBackEnableDistance = distance;
    }

    /**
     * 是否开启侧滑功能
     */
    public boolean isSwipeBackEnabled() {
        return isSwipeBackEnabled;
    }

    /**
     * 设置是否开启侧滑功能
     */
    public void setSwipeBackEnabled(boolean enabled) {
        isSwipeBackEnabled = enabled;
        if (!enabled) {
            //关闭侧滑，需要将View归位
            onSwipeBackEvent(0);
        }
    }

    /**
     * 在{@link Activity#dispatchTouchEvent(MotionEvent)}中调用，仅响应侧滑触发区域内的侧滑事件
     * <p>
     * 注意：
     * <p>
     * 1. 无论有无子View消费触摸事件，均可触发侧滑
     * <p>
     * 2. 此方法必须在Activity的super.dispatchTouchEvent(MotionEvent)之前调用。若返回true则表示触摸事件被消耗，直接返回true，不再调用super.dispatchTouchEvent(MotionEvent)
     * <p>
     * 3. 若要响应非侧滑触发区域内的侧滑事件，需配合{@link #onTouchEvent(MotionEvent)} (MotionEvent)}使用
     *
     * @return 是否消耗该触摸事件
     */
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!isSwipeBackEnabled() || getActivity().isTaskRoot()) {
            //侧滑未启用，或者当前为根Activity侧滑不可用
            return false;
        }
        //滑动速度追踪
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        //touch事件的index
        int actionIndex = event.getActionIndex();
        switch (event.getActionMasked()) {
            //首次按下事件
            case MotionEvent.ACTION_DOWN: {
                //标记滑动方向：未滑动
                mDragDirection = DIRECTION_NONE;
                //记录起始触摸坐标
                mStartX = event.getX(actionIndex);
                mStartY = event.getY(actionIndex);
                //记录当前触摸ID
                mCurTouchPointerId = event.getPointerId(actionIndex);
                //判断当前正在侧滑动画中
                if (mSwipeBackAnimator != null && mSwipeBackAnimator.isStarted()) {
                    //取消动画
                    mSwipeBackAnimator.cancel();
                    //标记滑动方向：横向滑动
                    mDragDirection = DIRECTION_HORIZONTAL;
                    //校正起始触摸坐标
                    mStartX -= getSwipeBackView().getTranslationX();
                    return true;
                }
                //起始触摸点距离满足侧滑
                else if (mStartX < mSwipeBackEnableDistance) {
                    convertToTranslucent();//通过反射将窗口转为透明
                    prepareSwipeViews();//预备侧滑操作相关的视图
                }
                break;
            }
            //多指触摸时的手指抬起事件
            case MotionEvent.ACTION_POINTER_UP: {
                //横向滑动时当前触摸ID抬起，则需切换触摸ID
                if (mDragDirection == DIRECTION_HORIZONTAL && event.getPointerId(actionIndex) == mCurTouchPointerId) {
                    //清除速度追踪
                    mVelocityTracker.clear();
                    for (int index = 0; index < event.getPointerCount(); index++) {
                        //触摸ID
                        int touchPointerId = event.getPointerId(index);
                        //选择一个非当前抬起的触摸ID作为下一个触摸ID
                        if (touchPointerId != mCurTouchPointerId) {
                            //重置偏移坐标
                            mStartX = event.getX(index) - getSwipeBackView().getTranslationX();
                            mStartY = event.getY(index);
                            //重置触摸ID
                            mCurTouchPointerId = touchPointerId;
                            break;
                        }
                    }
                }
                break;
            }
            //触摸移动事件
            case MotionEvent.ACTION_MOVE: {
                for (int index = 0; index < event.getPointerCount(); index++) {
                    //只响应当前触摸ID的移动操作
                    if (event.getPointerId(index) == mCurTouchPointerId) {
                        //横向滑动事件
                        if (mDragDirection == DIRECTION_HORIZONTAL) {
                            //滑动返回事件
                            onSwipeBackEvent((int) (event.getX(index) - mStartX + 0.5F));
                            return true;
                        }
                        //未滑动，且起始触摸点距离满足侧滑
                        else if (mDragDirection == DIRECTION_NONE && mStartX < mSwipeBackEnableDistance) {
                            //判断滑动方向
                            mDragDirection = resolveDragDirection(event.getX(index), event.getY(index));
                            if (mDragDirection == DIRECTION_HORIZONTAL) {
                                //隐藏输入框
                                HideInputSoft();
                                //下放一个触摸取消事件，传递给子View
                                event.setAction(MotionEvent.ACTION_CANCEL);
                            }
                        }
                        break;
                    }
                }
                break;
            }
            //手指抬起事件
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                //横向滑动事件
                if (mDragDirection == DIRECTION_HORIZONTAL) {
                    //计算横向手势速度
                    mVelocityTracker.computeCurrentVelocity(1000);
                    //开始侧滑动画
                    startSwipeBackAnimator(event.getX(actionIndex) - mStartX, mVelocityTracker.getXVelocity());
                } else {
                    //将窗口转为不透明
                    convertFromTranslucent();
                }
                //释放滑动速度追踪
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            }
        }
        return false;
    }

    /**
     * 在{@link Activity#onTouchEvent(MotionEvent)}中调用，开启非侧滑触发区域内的侧滑事件
     * <p>
     * 注意：
     * <p>
     * 1. 需配合{@link #dispatchTouchEvent(MotionEvent)}使用
     * <p>
     * 2. 仅当无View消费触摸事件时才会触发
     */
    public void onTouchEvent(MotionEvent event) {
        if (!isSwipeBackEnabled() || getActivity().isTaskRoot()) {
            //侧滑未启用，或者当前为根Activity侧滑不可用
            return;
        }
        if (mDragDirection != DIRECTION_NONE || mStartX < mSwipeBackEnableDistance) {
            //当已产生滑动或触点在侧滑触发区域内时，无需处理（因为dispatchTouchEvent已处理妥当）
            return;
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                convertToTranslucent();//通过反射将窗口转为透明
                prepareSwipeViews();//预备侧滑操作相关的视图
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                for (int index = 0; index < event.getPointerCount(); index++) {
                    //只响应当前触摸ID的移动操作
                    if (event.getPointerId(index) == mCurTouchPointerId) {
                        //判断滑动方向
                        mDragDirection = resolveDragDirection(event.getX(index), event.getY(index));
                        if (mDragDirection == DIRECTION_HORIZONTAL) {
                            //隐藏输入框
                            HideInputSoft();
                        }
                        break;
                    }
                }
                break;
            }
        }
    }

    /**
     * 侧滑即将发生，预备侧滑操作相关的视图，提升用户体验
     */
    private void prepareSwipeViews() {
        mSwipeBackView = getSwipeBackView();
        mShadowView = getShadowView();
    }

    /**
     * 判断触摸事件方向
     *
     * @param eventX 触摸事件X坐标
     * @param eventY 触摸事件Y坐标
     */
    protected int resolveDragDirection(float eventX, float eventY) {
        //X轴移动距离
        float distanceX = Math.abs(eventX - mStartX);
        //Y轴移动距离
        float distanceY = Math.abs(eventY - mStartY);
        //发生了滑动
        if (distanceX >= mTouchSlop || distanceY >= mTouchSlop) {
            if (distanceX > distanceY) {
                //更新触摸的起始坐标，使滑动体验不至于很突兀
                mStartX = eventX;
                mStartY = eventY;
                //触摸事件方向：横向
                return DIRECTION_HORIZONTAL;
            } else {
                //触摸事件方向：纵向
                return DIRECTION_VERTICAL;
            }
        } else {
            //触摸事件方向：无
            return DIRECTION_NONE;
        }
    }

    /**
     * 侧滑事件处理
     *
     * @param offsetX X轴偏移量
     */
    private void onSwipeBackEvent(int offsetX) {
        if (!isTranslucentCompleted()) {
            //窗口还未转透明，暂不处理
            return;
        }
        //防止偏移量越界
        offsetX = Math.max(0, Math.min(getDecorView().getWidth(), offsetX));
        //设置侧滑事件操作视图的偏移量
        getSwipeBackView().setTranslationX(offsetX);
        View shadowView = getShadowView();
        if (shadowView != null) {
            //设置阴影视图的偏移量
            shadowView.setTranslationX(offsetX - shadowView.getWidth());
            if (shadowView.getBackground() != null) {
                int alpha = (int) ((1F - 1F * offsetX / shadowView.getWidth()) * 255);
                //设置阴影视图的背景透明度
                shadowView.getBackground().setAlpha(Math.max(0, Math.min(255, alpha)));
            }
        }
    }

    /**
     * 开始侧滑返回动画
     *
     * @param offsetX   开始动画时的X轴偏移量
     * @param velocityX 开始动画时的X方向滑动速度
     */
    private void startSwipeBackAnimator(float offsetX, float velocityX) {
        //防止偏移量越界
        offsetX = Math.max(0, Math.min(getDecorView().getWidth(), offsetX));
        if (mSwipeBackAnimator == null) {
            mSwipeBackAnimator = new DecelerateAnimator(getActivity().getApplicationContext(), false);
            mSwipeBackAnimator.addListener(getAnimatorListener());
            mSwipeBackAnimator.addUpdateListener(getAnimatorUpdateListener());
        }
        mSwipeBackAnimator.setFlingFrictionRatio(9F);
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            mSwipeBackAnimator.startAnimator(offsetX, 0, getDecorView().getWidth(), velocityX * 8F);
        } else {
            mSwipeBackAnimator.startAnimator(offsetX, 0, getDecorView().getWidth(), velocityX * 4F);
        }
    }

    /**
     * 利用反射将window转为不透明
     */
    protected void convertFromTranslucent() {
        Activity swipeBackActivity = getActivity();
        if (swipeBackActivity.isTaskRoot()) {
            //当前为根Activity，不允许窗口透明转换
            return;
        }
        try {
            Method convertFromTranslucent = Activity.class.getDeclaredMethod("convertFromTranslucent");
            convertFromTranslucent.setAccessible(true);
            convertFromTranslucent.invoke(swipeBackActivity);
        } catch (Throwable ignored) {
        }
    }

    /**
     * 利用反射将window转为透明
     */
    protected void convertToTranslucent() {
        Activity swipeBackActivity = getActivity();
        if (swipeBackActivity.isTaskRoot()) {
            //当前为根Activity，不允许窗口透明转换
            return;
        }
        //获取透明转换监听类
        Class listenerClass = getTranslucentConversionListenerClass();
        //获取透明转换监听对象，回调时标记转换完成
        Object listener = getTranslucentConversionListener(listenerClass);
        //若监听器为null，直接标记透明转换已完成，否则标记未完成
        setTranslucentCompleted(listener == null);
        try {
            // Android5.0开始，窗口透明转换API有改动，这里要做区分
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Object options = null;
                try {
                    //反射获取ActivityOptions对象
                    Method getActivityOptions = Activity.class.getDeclaredMethod("getActivityOptions");
                    getActivityOptions.setAccessible(true);
                    options = getActivityOptions.invoke(swipeBackActivity);
                } catch (Exception ignored) {
                }
                Method convertToTranslucent = Activity.class.getDeclaredMethod("convertToTranslucent", listenerClass, ActivityOptions.class);
                convertToTranslucent.setAccessible(true);
                convertToTranslucent.invoke(swipeBackActivity, listener, options);
            } else {
                Method convertToTranslucent = Activity.class.getDeclaredMethod("convertToTranslucent", listenerClass);
                convertToTranslucent.setAccessible(true);
                convertToTranslucent.invoke(swipeBackActivity, listener);
            }
        } catch (Throwable ignored) {
            setTranslucentCompleted(true);
        }
    }

    /**
     * 判断透明转换是否完成
     */
    protected boolean isTranslucentCompleted() {
        return isTranslucentCompleted;
    }

    /**
     * 标记透明转换是否完成
     */
    protected void setTranslucentCompleted(boolean completed) {
        isTranslucentCompleted = completed;
    }

    /**
     * 获取Window透明转换监听的class
     */
    private Class getTranslucentConversionListenerClass() {
        if (mTranslucentConversionListenerClass == null) {
            for (Class clazz : Activity.class.getDeclaredClasses()) {
                if (clazz.getSimpleName().equals("TranslucentConversionListener")) {
                    return mTranslucentConversionListenerClass = clazz;
                }
            }
        }
        return mTranslucentConversionListenerClass;
    }

    /**
     * 获取Window透明转换监听器，在回调时通过{@link #setTranslucentCompleted(boolean)}标记转换已完成
     */
    private Object getTranslucentConversionListener(Class translucentConversionListenerClass) {
        if (mTranslucentConversionListener == null && translucentConversionListenerClass != null) {
            mTranslucentConversionListener = Proxy.newProxyInstance(translucentConversionListenerClass.getClassLoader()
                    , new Class[]{translucentConversionListenerClass}
                    , new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) {
                            //标记转换已完成
                            setTranslucentCompleted(true);
                            return null;
                        }
                    });
        }
        return mTranslucentConversionListener;
    }

    /**
     * 获取绑定的Activity
     */
    public Activity getActivity() {
        return mActivity;
    }

    /**
     * 获取根View
     */
    public ViewGroup getDecorView() {
        if (mDecorView == null) {
            mDecorView = (ViewGroup) getActivity().getWindow().getDecorView();
        }
        return mDecorView;
    }

    /**
     * 返回侧滑事件操作的视图
     */
    public ViewGroup getSwipeBackView() {
        if (mSwipeBackView == null) {
            ViewParent view = getDecorView().findViewById(Window.ID_ANDROID_CONTENT);
            while (view.getParent() != getDecorView()) {
                view = view.getParent();
            }
            mSwipeBackView = (ViewGroup) view;

            //1. 将窗口背景设置给SwipeBackView
            TypedArray typedArray = getActivity().getTheme().obtainStyledAttributes(new int[]{android.R.attr.windowBackground});
            mSwipeBackView.setBackground(typedArray.getDrawable(0));
            typedArray.recycle();

            //2. 将窗口设为透明
            getActivity().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return mSwipeBackView;
    }

    /**
     * 返回侧滑时左侧的阴影视图
     */
    public View getShadowView() {
        if (mShadowView == null) {
            mShadowView = new ShadowView(getDecorView().getContext());
            mShadowView.setTranslationX(-getSwipeBackView().getWidth());
            ((ViewGroup) getSwipeBackView().getParent()).addView(mShadowView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        return mShadowView;
    }

    /**
     * 隐藏软键盘
     */
    public void HideInputSoft() {
        View focusView = getDecorView().findFocus();
        if (focusView == null) {
            focusView = getActivity().getCurrentFocus();
        }
        if (focusView instanceof EditText) {
            focusView.clearFocus();
        }
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow((focusView == null ? getDecorView() : focusView).getWindowToken(), 0);
        }
    }

    /**
     * 返回动画更新监听器，处理动画中的更新事件
     */
    private ValueAnimator.AnimatorUpdateListener getAnimatorUpdateListener() {
        if (mAnimatorUpdateListener == null) {
            mAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float translation = (Float) animation.getAnimatedValue();
                    onSwipeBackEvent((int) (translation + 0.5F));
                }
            };
        }
        return mAnimatorUpdateListener;
    }

    /**
     * 返回动画监听器，处理动画开始、取消、结束三种事件
     */
    private Animator.AnimatorListener getAnimatorListener() {
        if (mAnimatorListener == null) {
            mAnimatorListener = new AnimatorListenerAdapter() {
                /**
                 * 标志侧滑动画是否被取消
                 */
                private boolean isAnimationCancel;

                @Override
                public void onAnimationStart(Animator animation) {
                    isAnimationCancel = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    isAnimationCancel = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!isAnimationCancel) {
                        //判断最终移动距离位置是否超过半宽
                        if (2 * getSwipeBackView().getTranslationX() >= getDecorView().getWidth()) {
                            getShadowView().setVisibility(View.GONE);
                            //结束当前Activity
                            getActivity().finish();
                            //取消返回动画
                            getActivity().overridePendingTransition(-1, -1);
                        } else {
                            //关闭侧滑，需要将View归位
                            onSwipeBackEvent(0);
                            //窗口转为不透明
                            convertFromTranslucent();
                        }
                    }
                }
            };
        }
        return mAnimatorListener;
    }

}
