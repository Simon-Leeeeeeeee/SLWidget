package cn.simonlee.widget.swipeback;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Activity侧滑返回支持，状态栏透明
 * <p>
 * 原理：
 * <p>
 * 1. 在构造方法中设置透明状态栏，利用反射将窗口转为不透明，同时监听布局变化以解决adjustResize失效问题
 * <p>
 * 2. 在Activity的dispatchTouchEvent方法中拦截触摸事件，满足条件则进行侧滑
 * <p>
 * 3. 侧滑事件前，利用反射将窗口转为透明；侧滑取消后，利用反射将窗口转为不透明
 * <p>
 * <p>
 * 使用说明：
 * <p>
 * 1. 仅支持SDK19(Android4.4)及以上
 * <p>
 * 2. 因状态栏透明，布局会从屏幕顶端开始绘制，Toolbar高度需自行调整
 * <p>
 * 3. 状态栏透明会导致输入法的adjustPan模式失效，建议设置为adjustResize
 * <p>
 * 4. 必须设置以下属性，否则侧滑时无法透视下层Activity
 * <p>
 * <item name="android:windowBackground">@android:color/transparent</item>
 * <p>
 * 5. SDK21(Android5.0)以下必须设置以下属性，否则无法通过反射将窗口转为透明
 * <p>
 * <item name="android:windowIsTranslucent">true</item>
 * <p>
 * 6. 侧滑时会利用反射将窗口转为透明，此时会引起下层Activity生命周期变化，留意可能因此导致的严重问题
 * <p>
 * (a) onDestory -> onCreat -> onStart -> (onResume -> onPause) -> onStop
 * <p>
 * (b) onRestart -> onStart -> (onResume -> onPause) -> onStop
 * <p>
 * 7. 当顶层Activity方向与下层Activity方向不一致时侧滑会失效（下层方向未锁定除外），建议关闭该层Activity侧滑功能。
 * <p>
 * 示例场景：视频APP的播放页面和下层页面。
 * <p>
 * 8. 如需动态支持横竖屏切换，屏幕方向需指定为"behind"跟随栈底Activity方向，同时在onCreate中判断若不支持横竖屏切换，则锁定屏幕方向（避免behind失效）。
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-06-19
 */
@SuppressWarnings("unused")
@android.support.annotation.RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class SwipeBackHelper {

    /**
     * 目标Activity
     */
    private final Activity mSwipeBackActivity;

    /**
     * 根视图
     */
    private final ViewGroup mDecorView;

    /**
     * 判断滑动事件的最小距离
     */
    private final int mTouchSlop;

    /**
     * 左侧拦截滑动事件的区域
     */
    private final float mInterceptRect;

    /**
     * 标志状态栏是否透明
     */
    private final boolean isStatusBarTransparent;

    /**
     * 纵向滑动
     */
    private final int vertical = 1;

    /**
     * 横向滑动
     */
    private final int horizontal = 2;

    /**
     * 当前屏幕方向
     */
    private final int mOrientation;

    /**
     * 滑动事件方向
     */
    private int mDragDirection;

    /**
     * 阴影视图
     */
    private View mShadowView;

    /**
     * 侧滑操作的视图
     */
    private ViewGroup mSwipeBackView;

    /**
     * 滑动速度追踪
     */
    private VelocityTracker mVelocityTracker;

    /**
     * 滑动减速动画
     */
    private DecelerateAnimator mSwipeAnimator;

    /**
     * 触摸点的ID
     */
    private int mTouchPointerId;

    /**
     * 触摸点的xy坐标
     */
    private float mStartX, mStartY;

    /**
     * 标志侧滑动画是否被取消
     */
    private boolean isAnimationCancel;

    /**
     * 标志是否允许侧滑
     */
    private boolean isSwipeBackEnabled = true;

    /**
     * 窗口背景颜色，用于覆盖当输入法及导航栏变化时底部的黑色，默认不处理
     * <p>
     * 因{android:windowBackground}透明，输入法及导航栏变化时底部为黑色
     */
    private int mWindowBackgroundColor;

    /**
     * 窗口背景视图，用于覆盖当输入法及导航栏变化时底部的黑色，默认不处理
     * <p>
     * 因{android:windowBackground}透明，输入法及导航栏变化时底部为黑色
     */
    private View mWindowBackGroundView;

    /**
     * Activity转为透明的回调
     */
    private Object mTranslucentConversionListener;

    /**
     * Activity转为透明的回调类
     */
    private Class mTranslucentConversionListenerClass;

    /**
     * 标志Activity转为透明完成
     */
    private boolean isTranslucentComplete;

    /**
     * 布局和动画的监听，使用内部类的方式避免暴露回调接口
     */
    private PrivateListener mPrivateListener = new PrivateListener();

    @SuppressLint("NewApi")
    public SwipeBackHelper(Activity activity) {
        this(activity, false);
    }

    @android.support.annotation.RequiresApi(api = Build.VERSION_CODES.M)
    public SwipeBackHelper(Activity activity, boolean darkStatusBar) {
        //目标Activity
        this.mSwipeBackActivity = activity;
        this.mOrientation = activity.getResources().getConfiguration().orientation;
        //获取根View
        this.mDecorView = (ViewGroup) activity.getWindow().getDecorView();
        //设置状态栏透明
        this.isStatusBarTransparent = setStatusBarTransparent(darkStatusBar);
        //判断滑动事件的最小距离
        this.mTouchSlop = ViewConfiguration.get(activity).getScaledTouchSlop();
        //左侧拦截滑动事件的区域
        this.mInterceptRect = 18 * activity.getResources().getDisplayMetrics().density;//18dp
        //设置Activity不透明
        convertFromTranslucent(mSwipeBackActivity);
    }

    /**
     * 设置状态栏透明，并监听布局变化
     */
    private boolean setStatusBarTransparent(boolean darkStatusBar) {
        boolean isInMultiWindowMode = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && mSwipeBackActivity.isInMultiWindowMode();
        //窗口模式或者SDK小于19，不设置状态栏透明
        if (isInMultiWindowMode || Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return false;
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mSwipeBackActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            int systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            if (darkStatusBar && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //设置状态栏文字&图标暗色
                systemUiVisibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            //去除状态栏背景
            mDecorView.setSystemUiVisibility(systemUiVisibility);
            //设置状态栏透明
            mSwipeBackActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            mSwipeBackActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            mSwipeBackActivity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        //监听DecorView的布局变化
        mDecorView.addOnLayoutChangeListener(mPrivateListener);
        return true;
    }

    /**
     * 状态栏是否透明
     */
    public boolean isStatusBarTransparent() {
        return isStatusBarTransparent;
    }

    /**
     * 返回侧滑事件操作的视图
     */
    public ViewGroup getSwipeBackView(ViewGroup decorView) {
        if (mSwipeBackView == null) {
            //使用contentView的父View，可包含ActionBar
            mSwipeBackView = (ViewGroup) decorView.findViewById(Window.ID_ANDROID_CONTENT).getParent();
        }
        return mSwipeBackView;
    }

    /**
     * 返回侧滑时左侧的阴影视图
     * <p>
     * 被子类重写时，注意要添加到swipeBackView的父容器中
     */
    public View getShadowView(ViewGroup swipeBackView) {
        if (mShadowView == null) {
            mShadowView = new ShadowView(mSwipeBackActivity);
            mShadowView.setTranslationX(-swipeBackView.getWidth());
            ((ViewGroup) swipeBackView.getParent()).addView(mShadowView, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        return mShadowView;
    }

    /**
     * 返回窗口背景视图，用于覆盖当输入法及导航栏变化时底部的黑色
     * <p>
     * 因{android:windowBackground}透明，输入法及导航栏变化时底部为黑色
     */
    public View getWindowBackGroundView(ViewGroup decorView) {
        if (mWindowBackGroundView == null && mWindowBackgroundColor >>> 24 > 0) {
            mWindowBackGroundView = new View(mSwipeBackActivity);
            mWindowBackGroundView.setTranslationY(decorView.getHeight());
            mWindowBackGroundView.setBackgroundColor(mWindowBackgroundColor);
            decorView.addView(mWindowBackGroundView, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        return mWindowBackGroundView;
    }

    /**
     * 设置窗口背景颜色，用于覆盖当输入法及导航栏变化时底部的黑色
     * <p>
     * 因{android:windowBackground}透明，输入法及导航栏变化时底部为黑色
     */
    public void setWindowBackgroundColor(int color) {
        mWindowBackgroundColor = color;
        if (mWindowBackGroundView != null) {
            mWindowBackGroundView.setBackgroundColor(mWindowBackgroundColor);
        }
    }

    /**
     * Activity触摸事件分发，当横向滑动时触发侧滑返回，同时触摸事件改变为取消下发给childView
     */
    public void dispatchTouchEvent(MotionEvent event) {
        if (!isSwipeBackEnabled || mSwipeBackActivity.isTaskRoot()) {
            return;
        }
        this.mSwipeBackView = getSwipeBackView(mDecorView);
        this.mShadowView = getShadowView(mSwipeBackView);
        int actionIndex = event.getActionIndex();
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                if (mSwipeAnimator != null && mSwipeAnimator.isStarted()) {
                    mDragDirection = horizontal;
                    mSwipeAnimator.cancel();
                    mStartX = event.getX(actionIndex) - mSwipeBackView.getTranslationX();
                } else {
                    //记录偏移坐标
                    mStartX = event.getX(actionIndex);
                    mStartY = event.getY(actionIndex);
                    //记录当前控制指针ID
                    mTouchPointerId = event.getPointerId(actionIndex);
                    //重置拖动方向
                    mDragDirection = 0;
                    if (mStartX <= mInterceptRect) {
                        convertToTranslucent(mSwipeBackActivity);
                    }
                }
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
                            mStartX = event.getX(index) - mSwipeBackView.getTranslationX();
                            mStartY = event.getY(index);
                            //重置触摸ID
                            mTouchPointerId = event.getPointerId(index);
                            break;
                        }
                    }
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                for (int index = 0; index < event.getPointerCount(); index++) {
                    //只响应当前控制指针的移动操作
                    if (event.getPointerId(index) == mTouchPointerId) {
                        if (mDragDirection == horizontal) {//横向滑动事件
                            int offsetX = (int) (event.getX(index) - mStartX);
                            //处理偏移量越界的情况
                            if (offsetX < 0) {
                                offsetX = 0;
                                mStartX = event.getX(index);
                            } else if (offsetX > mShadowView.getWidth()) {
                                offsetX = mShadowView.getWidth();
                                mStartX = event.getX(index) - offsetX;
                            }
                            //滑动返回事件
                            swipeBackEvent(offsetX);
                        } else if (mDragDirection == 0 && mStartX <= mInterceptRect) {//还未产生滑动，且触点在拦截区域内
                            if (Math.abs(event.getX(index) - mStartX) >= mTouchSlop * 0.8F) {//横向滑动，系数0.8为增加横向检测的灵敏度
                                mStartX = event.getX(index);
                                mDragDirection = horizontal;
                                //下放一个触摸取消事件，传递给子View
                                event.setAction(MotionEvent.ACTION_CANCEL);
                                HideInputSoft();
                            } else if (Math.abs(event.getY(index) - mStartY) >= mTouchSlop) {//纵向滑动
                                mDragDirection = vertical;
                            }
                        }
                        break;
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (mDragDirection == horizontal) {//横向滑动事件
                    //计算横向手势速度
                    mVelocityTracker.computeCurrentVelocity(1000);
                    float velocityX = mVelocityTracker.getXVelocity();
                    float offsetX = event.getX(actionIndex) - mStartX;
                    //处理偏移量越界的情况
                    if (offsetX < 0) {
                        offsetX = 0;
                    } else if (offsetX > mShadowView.getWidth()) {
                        offsetX = mShadowView.getWidth();
                    }
                    startSwipeAnimator(offsetX, 0, mShadowView.getWidth(), velocityX);
                } else {
                    convertFromTranslucent(mSwipeBackActivity);
                }
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                //重置拖动方向
                mDragDirection = 0;
                break;
            }
        }
    }

    /**
     * Activity触摸事件，当子View未消费时进行滑动方向判断
     */
    public void onTouchEvent(MotionEvent event) {
        if (!isSwipeBackEnabled || mSwipeBackActivity.isTaskRoot()) {
            return;
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                convertToTranslucent(mSwipeBackActivity);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                //还未产生滑动，触点不在拦截区域内
                if (mDragDirection == 0 && mStartX > mInterceptRect) {
                    for (int index = 0; index < event.getPointerCount(); index++) {
                        //只响应当前控制指针的移动操作
                        if (event.getPointerId(index) == mTouchPointerId) {
                            if (Math.abs(event.getY(index) - mStartY) >= mTouchSlop) {//纵向滑动
                                mDragDirection = vertical;
                            } else if (Math.abs(event.getX(index) - mStartX) >= mTouchSlop) {
                                mStartX = event.getX(index);
                                mDragDirection = horizontal;
                                HideInputSoft();
                            }
                            break;
                        }
                    }
                }
                break;
            }
        }
    }

    /**
     * 利用反射将activity转为透明
     */
    private void convertToTranslucent(Activity activity) {
        if (activity.isTaskRoot()) return;
        isTranslucentComplete = false;
        try {
            if (mTranslucentConversionListenerClass == null) {
                Class[] clazzArray = Activity.class.getDeclaredClasses();
                for (Class clazz : clazzArray) {
                    if (clazz.getSimpleName().contains("TranslucentConversionListener")) {
                        mTranslucentConversionListenerClass = clazz;
                    }
                }
            }
            if (mTranslucentConversionListener == null && mTranslucentConversionListenerClass != null) {
                InvocationHandler invocationHandler = new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        isTranslucentComplete = true;
                        return null;
                    }
                };
                mTranslucentConversionListener = Proxy.newProxyInstance(mTranslucentConversionListenerClass.getClassLoader(), new Class[]{mTranslucentConversionListenerClass}, invocationHandler);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Object options = null;
                try {
                    Method getActivityOptions = Activity.class.getDeclaredMethod("getActivityOptions");
                    getActivityOptions.setAccessible(true);
                    options = getActivityOptions.invoke(this);
                } catch (Exception ignored) {
                }
                Method convertToTranslucent = Activity.class.getDeclaredMethod("convertToTranslucent", mTranslucentConversionListenerClass, ActivityOptions.class);
                convertToTranslucent.setAccessible(true);
                convertToTranslucent.invoke(activity, mTranslucentConversionListener, options);
            } else {
                Method convertToTranslucent = Activity.class.getDeclaredMethod("convertToTranslucent", mTranslucentConversionListenerClass);
                convertToTranslucent.setAccessible(true);
                convertToTranslucent.invoke(activity, mTranslucentConversionListener);
            }
        } catch (Throwable ignored) {
            isTranslucentComplete = true;
        }
        if (mTranslucentConversionListener == null) {
            isTranslucentComplete = true;
        }
    }

    /**
     * 利用反射将activity转为不透明
     */
    private void convertFromTranslucent(Activity activity) {
        if (activity.isTaskRoot()) return;
        try {
            Method convertFromTranslucent = Activity.class.getDeclaredMethod("convertFromTranslucent");
            convertFromTranslucent.setAccessible(true);
            convertFromTranslucent.invoke(activity);
        } catch (Throwable t) {
        }
    }

    /**
     * 设置是否开启侧滑返回
     */
    public void setSwipeBackEnabled(boolean enabled) {
        isSwipeBackEnabled = enabled;
        if (!enabled) {
            mSwipeBackView.setTranslationX(0);
            mShadowView.setTranslationX(-mShadowView.getWidth());
        }
    }

    /**
     * 反转状态栏字体&图标颜色
     */
    @android.support.annotation.RequiresApi(api = Build.VERSION_CODES.M)
    public void toggleStatusBarColor(boolean darkStatusBar) {
        int systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        if (darkStatusBar) {
            systemUiVisibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        mDecorView.setSystemUiVisibility(systemUiVisibility);
    }

    /**
     * 隐藏输入法
     */
    public void HideInputSoft() {
        View view = mSwipeBackActivity.getCurrentFocus();
        if (view != null) {
            if (view instanceof EditText) {
                view.clearFocus();
            }
            InputMethodManager inputMethodManager = (InputMethodManager) mSwipeBackActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    /**
     * 开始侧滑返回动画
     *
     * @param startValue    初始位移值
     * @param minFinalValue 终点位移值（小值）
     * @param maxFinalValue 终点位移值（大值）
     * @param velocity      滑动速度
     */
    private void startSwipeAnimator(float startValue, float minFinalValue, float maxFinalValue, float velocity) {
        if (mSwipeAnimator == null) {
            mSwipeAnimator = new DecelerateAnimator(mSwipeBackActivity, false);
            mSwipeAnimator.growFlingFriction(9F);
            mSwipeAnimator.addListener(mPrivateListener);
            mSwipeAnimator.addUpdateListener(mPrivateListener);
        }
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            mSwipeAnimator.startAnimator(startValue, minFinalValue, maxFinalValue, velocity * 8F);
        } else {
            mSwipeAnimator.startAnimator(startValue, minFinalValue, maxFinalValue, velocity * 4F);
        }
    }

    /**
     * 侧滑返回事件
     *
     * @param translation 移动距离
     */
    private void swipeBackEvent(int translation) {
        if (!isTranslucentComplete) return;
        if (mShadowView.getBackground() != null) {
            int alpha = (int) ((1F - 1F * translation / mShadowView.getWidth()) * 255);
            if (alpha < 0) {
                alpha = 0;
            } else if (alpha > 255) {
                alpha = 255;
            }
            mShadowView.getBackground().setAlpha(alpha);
        }
        mShadowView.setTranslationX(translation - mShadowView.getWidth());
        mSwipeBackView.setTranslationX(translation);
    }

    private class PrivateListener implements Animator.AnimatorListener, ValueAnimator.AnimatorUpdateListener, View.OnLayoutChangeListener {

        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            //获取DecorView的可见区域
            Rect visibleDisplayRect = new Rect();
            mDecorView.getWindowVisibleDisplayFrame(visibleDisplayRect);
            //调整mWindowBackGroundView的Y轴偏移量，用于覆盖不可见区域出现的黑色（不可见区域常见为当输入法及导航栏变化时的背景）
            mWindowBackGroundView = getWindowBackGroundView(mDecorView);
            if (mWindowBackGroundView != null) {
                mWindowBackGroundView.setTranslationY(visibleDisplayRect.bottom);
            }
            //状态栏透明情况下，输入法的adjustResize不会生效，这里手动调整View的高度以适配
            if (isStatusBarTransparent()) {
                for (int i = 0; i < mDecorView.getChildCount(); i++) {
                    View child = mDecorView.getChildAt(i);
                    if (child instanceof ViewGroup) {
                        //获取DecorView的子ViewGroup
                        ViewGroup.LayoutParams childLp = child.getLayoutParams();
                        //调整子ViewGroup的paddingBottom
                        int paddingBottom = bottom - visibleDisplayRect.bottom;
                        if (childLp instanceof ViewGroup.MarginLayoutParams) {
                            //此处减去bottomMargin，是考虑到导航栏的高度
                            paddingBottom -= ((ViewGroup.MarginLayoutParams) childLp).bottomMargin;
                        }
                        if (paddingBottom < 0) {
                            paddingBottom = 0;
                        }
                        if (paddingBottom != child.getPaddingBottom()) {
                            //调整子ViewGroup的paddingBottom，以保证整个ViewGroup可见
                            child.setPadding(child.getPaddingLeft(), child.getPaddingTop(), child.getPaddingRight(), paddingBottom);
                        }
                        break;
                    }
                }
            }
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float translation = (Float) animation.getAnimatedValue();
            swipeBackEvent((int) translation);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (!isAnimationCancel) {
                //最终移动距离位置超过半宽，结束当前Activity
                if (mShadowView.getWidth() + 2 * mShadowView.getTranslationX() >= 0) {
                    mShadowView.setVisibility(View.GONE);
                    mSwipeBackActivity.finish();
                    mSwipeBackActivity.overridePendingTransition(-1, -1);//取消返回动画
                } else {
                    mShadowView.setTranslationX(-mShadowView.getWidth());
                    mSwipeBackView.setTranslationX(0);
                    convertFromTranslucent(mSwipeBackActivity);
                }
            }
        }

        @Override
        public void onAnimationStart(Animator animation) {
            isAnimationCancel = false;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            isAnimationCancel = true;
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }

    }

}
