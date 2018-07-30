package cn.simonlee.widget.swipeback;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
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

/**
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-06-19
 * <p>
 * 使用说明：
 * 1. 使用前必须判断SDK，仅支持SDK19(Android4.4)及以上
 * <p>
 * 2. Activity的Style设置必须设置以下两条属性
 * <item name="android:windowBackground">@color/transparent</item>
 * <item name="android:windowIsTranslucent">true</item>
 * <p>
 * 3.在android8.0及以上不能固定屏幕方向，因为会与windowIsTranslucent冲突导致程序崩溃。
 * 解决办法：栈底Activity固定屏幕方向，windowIsTranslucent为false，其他Activity的screenOrientation设置为behind跟随栈底Activity方向
 * 注意：栈底固定屏幕方向的Activity若被杀死，其他Activity可能会自动旋转
 * <p>
 * 4.ContentView会从屏幕顶端开始绘制，被状态栏&ActionBar覆盖。padding自行调整
 * <p>
 * 5.Activity的输入法模式不能设置为adjustPan，原因①无效，②侧滑时布局会下弹。建议为adjustResize
 * <p>
 * 6.因为背景为透明，入栈的Activity不会调用onStop，需要注意在onPause中停止不必要的动作。
 * <p>
 * 7.必须在Activity的dispatchTouchEvent中优先调用SwipeBackHelper的dispatchTouchEvent。因为若从左侧边滑动返回，会改变触摸事件的动作，下放一个ACTION_CANCEL
 * <p>
 * 8.可选：在Activity的onTouchEvent中调用SwipeBackHelper的onTouchEvent，可以在页面任意位置响应侧滑事件
 */
@SuppressWarnings("unused")
@android.support.annotation.RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class SwipeBackHelper implements Animator.AnimatorListener, ValueAnimator.AnimatorUpdateListener, View.OnLayoutChangeListener {

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
     * 窗口背景视图，用于解决当窗口透明时，输入法&导航栏可能造成的透视
     */
    private View mWindowBackGroundView;

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
     * 窗口背景颜色，当设置窗口透明时，输入法及导航栏的弹出会透视到前一个Activity
     */
    private int mWindowBackgroundColor = 0xFFFFFFFF;//Color.WHITE

    @SuppressLint("NewApi")
    public SwipeBackHelper(Activity activity) {
        this(activity, false);
    }

    @android.support.annotation.RequiresApi(api = Build.VERSION_CODES.M)
    public SwipeBackHelper(Activity activity, boolean darkStatusBar) {
        //目标Activity
        this.mSwipeBackActivity = activity;
        //获取根View
        this.mDecorView = (ViewGroup) activity.getWindow().getDecorView();
        //设置状态栏透明
        this.isStatusBarTransparent = setStatusBarTransparent(darkStatusBar);
        //判断滑动事件的最小距离
        this.mTouchSlop = ViewConfiguration.get(activity).getScaledTouchSlop();
        //左侧拦截滑动事件的区域
        this.mInterceptRect = 15 * activity.getResources().getDisplayMetrics().density;//15dp
        //窗口如果是透明的，需要新增背景View，防止输入法&导航栏透视
        if (isWindowTranslucent()) {
            this.mWindowBackGroundView = getWindowBackGroundView(mDecorView);
            //监听DecorView的布局变化
            mDecorView.addOnLayoutChangeListener(this);
        }
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
        mDecorView.addOnLayoutChangeListener(this);
        return true;
    }

    /**
     * 状态栏是否透明
     */
    public boolean isStatusBarTransparent() {
        return isStatusBarTransparent;
    }

    /**
     * 判断窗口是否透明
     */
    public boolean isWindowTranslucent() {
        TypedArray typedArray = mSwipeBackActivity.obtainStyledAttributes(new int[]{android.R.attr.windowIsTranslucent});
        try {
            return typedArray.getBoolean(0, false);
        } finally {
            typedArray.recycle();
        }
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
     * 返回侧滑时左侧的阴影视图，并添加到decorView
     */
    public View getShadowView(ViewGroup decorView) {
        if (mShadowView == null) {
            mShadowView = new ShadowView(mSwipeBackActivity);
            mShadowView.setTranslationX(-decorView.getWidth());
            decorView.addView(mShadowView, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        return mShadowView;
    }

    /**
     * 返回窗口背景视图，用于防止输入法&导航栏的透视
     */
    public View getWindowBackGroundView(ViewGroup decorView) {
        if (mWindowBackGroundView == null) {
            mWindowBackGroundView = new View(mSwipeBackActivity);
            mWindowBackGroundView.setTranslationY(decorView.getHeight());
            mWindowBackGroundView.setBackgroundColor(mWindowBackgroundColor);
            decorView.addView(mWindowBackGroundView, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        return mWindowBackGroundView;
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        //获取DecorView的可见区域
        Rect visibleDisplayRect = new Rect();
        mDecorView.getWindowVisibleDisplayFrame(visibleDisplayRect);
        //窗口透明则调整mWindowBackGroundView的Y轴偏移量，遮蔽不可见区域（不可见区域常见为输入法&导航栏，可能会造成透视）
        if (isWindowTranslucent()) {
            mWindowBackGroundView = getWindowBackGroundView(mDecorView);
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

    /**
     * Activity触摸事件分发，当横向滑动时触发侧滑事件
     * ①当触摸点为拦截区域时进行滑动方向判断
     * ②当子View未消费时，在onTouchEvent中进行滑动方向判断
     */
    public void dispatchTouchEvent(MotionEvent event) {
        if (!isSwipeBackEnabled || mSwipeBackActivity.isTaskRoot()) {
            return;
        }
        this.mSwipeBackView = getSwipeBackView(mDecorView);
        this.mShadowView = getShadowView(mDecorView);
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
                            } else if (offsetX > mDecorView.getWidth()) {
                                offsetX = mDecorView.getWidth();
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
                    if (mVelocityTracker != null) {
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                    }
                    float offsetX = event.getX(actionIndex) - mStartX;
                    //处理偏移量越界的情况
                    if (offsetX < 0) {
                        offsetX = 0;
                    } else if (offsetX > mDecorView.getWidth()) {
                        offsetX = mDecorView.getWidth();
                    }
                    startSwipeAnimator(offsetX, 0, mDecorView.getWidth(), velocityX);
                }
                //重置拖动方向
                mDragDirection = 0;
                break;
            }
        }
    }

    /**
     * Activity触摸事件
     * 当子View未消费时会调用Activity的onTouchEvent，此时进行滑动方向判断
     */
    public void onTouchEvent(MotionEvent event) {
        if (!isSwipeBackEnabled || mSwipeBackActivity.isTaskRoot()) {
            return;
        }
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
    }

    /**
     * 设置是否开启侧滑返回
     */
    public void setSwipeBackEnabled(boolean enabled) {
        isSwipeBackEnabled = enabled;
        if (!enabled) {
            mSwipeBackView.setTranslationX(0);
            mShadowView.setTranslationX(-mDecorView.getWidth());
        }
    }

    /**
     * 设置窗口背景颜色，防止输入法及导航栏可能造成的透视
     */
    public void setWindowBackgroundColor(int color) {
        mWindowBackgroundColor = color;
        if (mWindowBackGroundView != null) {
            mWindowBackGroundView.setBackgroundColor(mWindowBackgroundColor);
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
     * 侧滑返回事件
     *
     * @param translation 移动距离
     */
    public void swipeBackEvent(int translation) {
        if (mShadowView.getBackground() != null) {
            int alpha = (int) ((1F - 1F * translation / mDecorView.getWidth()) * 255);
            if (alpha < 0) {
                alpha = 0;
            } else if (alpha > 255) {
                alpha = 255;
            }
            mShadowView.getBackground().setAlpha(alpha);
        }
        mShadowView.setTranslationX(translation - mDecorView.getWidth());
        mSwipeBackView.setTranslationX(translation);
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
            mSwipeAnimator.addListener(this);
            mSwipeAnimator.addUpdateListener(this);
        }
        mSwipeAnimator.startAnimator(startValue, minFinalValue, maxFinalValue, velocity * 3F);
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
            if (2 * mSwipeBackView.getTranslationX() >= mDecorView.getWidth()) {
                mShadowView.setVisibility(View.GONE);
                mSwipeBackActivity.finish();
                mSwipeBackActivity.overridePendingTransition(-1, -1);//取消返回动画
            } else {
                mShadowView.setTranslationX(-mDecorView.getWidth());
                mSwipeBackView.setTranslationX(0);
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
