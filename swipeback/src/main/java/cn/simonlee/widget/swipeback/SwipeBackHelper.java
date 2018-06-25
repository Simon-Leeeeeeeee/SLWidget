package cn.simonlee.widget.swipeback;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
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
 * @createdTime 2018-06-19
 * <p>
 * 使用说明：
 * 1.Activity的Style设置必须设置以下两条属性
 * <item name="android:windowBackground">@color/transparent</item>
 * <item name="android:windowIsTranslucent">true</item>
 * 2.状态栏&ActionBar的高度padding自行设置
 * 3.Android5.0以下兼容性未做测试
 * 4.Activity的输入法模式不能设置为adjustPan，原因①无效，②侧滑时布局会下弹。建议为adjustResize
 * 5.入栈的Activity不会调用onStop，因为背景为透明。除非进入后台
 * 6.必须在Activity的dispatchTouchEvent中先调用SwipeBackHelper的dispatchTouchEvent。如若从左侧边滑动返回，会改变TouchEvent的Acition，下放一个ACTION_CANCEL
 * 7.可选：在Activity的onTouchEvent中调用SwipeBackHelper的onTouchEvent，可以在页面任意位置响应侧滑事件
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class SwipeBackHelper implements Animator.AnimatorListener, ValueAnimator.AnimatorUpdateListener, View.OnLayoutChangeListener {

    /**
     * 判断滑动事件的最小距离
     */
    private int mTouchSlop;

    /**
     * 左侧拦截滑动事件的区域
     */
    private float mInterceptRect;

    /**
     * 滑动事件方向
     */
    private int mDragDirection;

    /**
     * 纵向滑动
     */
    private final int vertical = 1;

    /**
     * 横向滑动
     */
    private final int horizontal = 2;

    /**
     * 根视图
     */
    private ViewGroup mDecorView;

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
     * 目标Activity
     */
    private final Activity mSwipeBackActivity;

    /**
     * 触摸点的ID
     */
    private int mTouchPointerId;

    /**
     * 触摸点的xy坐标
     */
    private float mStartX, mStartY;

    public SwipeBackHelper(Activity activity) {
        //目标Activity
        this.mSwipeBackActivity = activity;
        //判断滑动事件的最小距离
        this.mTouchSlop = ViewConfiguration.get(mSwipeBackActivity).getScaledTouchSlop();
        //左侧拦截滑动事件的区域
        this.mInterceptRect = 15 * mSwipeBackActivity.getResources().getDisplayMetrics().density;//15dp

        //获取根View及侧滑返回的View
        this.mDecorView = (ViewGroup) mSwipeBackActivity.getWindow().getDecorView();
        this.mSwipeBackView = getSwipeBackView(mDecorView);

        //插入左侧阴影
        this.mShadowView = getShadowView(mDecorView);

        //判断不是窗口模式
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || !mSwipeBackActivity.isInMultiWindowMode()) {
            //监听DecorView的布局变化
            mDecorView.addOnLayoutChangeListener(this);
            //去除状态栏
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }
            //设置状态栏透明
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
            }
        }
    }

    /**
     * 返回侧滑事件操作的View，可被子类重写
     */
    public ViewGroup getSwipeBackView(ViewGroup decorView) {
        //使用contentView的父View，可包含ActionBar
        return (ViewGroup) decorView.findViewById(Window.ID_ANDROID_CONTENT).getParent();
    }

    /**
     * 返回侧滑时左侧的阴影View，并添加到decorView，可被子类重写
     */
    public View getShadowView(ViewGroup decorView) {
        ShadowView shadowView = new ShadowView(mSwipeBackActivity);
        shadowView.setTranslationX(-decorView.getMeasuredWidth());
        decorView.addView(shadowView, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        //使用contentView的父View，可包含ActionBar
        return shadowView;
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if (oldBottom == 0) {
            return;
        }
        //获取DecorView的可见区域
        Rect visibleDisplayRect = new Rect();
        mDecorView.getWindowVisibleDisplayFrame(visibleDisplayRect);
        for (int i = 0; i < mDecorView.getChildCount(); i++) {
            View child = mDecorView.getChildAt(i);
            if (child instanceof ViewGroup) {
                //获取DecorView的子ViewGroup
                ViewGroup.LayoutParams childLp = child.getLayoutParams();
                //调整子ViewGroup的paddingBottom
                int paddingBottom = bottom - visibleDisplayRect.bottom;
                Log.e("SLWidget", getClass().getName() + ".onLayoutChange() paddingBottom = " + paddingBottom);
                if (childLp instanceof ViewGroup.MarginLayoutParams) {
                    //此处减去bottomMargin，是考虑到导航栏的高度
                    paddingBottom -= ((ViewGroup.MarginLayoutParams) childLp).bottomMargin;
                }
                if (paddingBottom < 0) {
                    paddingBottom = 0;
                }
                if (paddingBottom != child.getPaddingBottom()) {
                    Log.e("SLWidget", getClass().getName() + ".onLayoutChange() setPadding = " + paddingBottom);
                    //调整子ViewGroup的paddingBottom，以保证整个ViewGroup可见
                    child.setPadding(child.getPaddingLeft(), child.getPaddingTop(), child.getPaddingRight(), paddingBottom);
                }
                break;
            }
        }
    }

    /**
     * Activity触摸事件分发，当横向滑动时触发侧滑事件
     * ①当触摸点为拦截区域时进行滑动方向判断
     * ②当子View未消费时，在onTouchEvent中进行滑动方向判断
     */
    public void dispatchTouchEvent(MotionEvent event) {
        int actionIndex = event.getActionIndex();
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                //记录偏移坐标
                mStartX = event.getX(actionIndex);
                mStartY = event.getY(actionIndex);
                //记录当前控制指针ID
                mTouchPointerId = event.getPointerId(actionIndex);
                //重置拖动方向
                mDragDirection = 0;
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
                            } else if (offsetX > mDecorView.getMeasuredWidth()) {
                                offsetX = mDecorView.getMeasuredWidth();
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
                    } else if (offsetX > mDecorView.getMeasuredWidth()) {
                        offsetX = mDecorView.getMeasuredWidth();
                    }
                    startSwipeAnimator(offsetX, 0, mDecorView.getMeasuredWidth(), velocityX);
                }
                break;
            }
        }
    }

    /**
     * Activity触摸事件
     * 当子View未消费时会调用Activity的onTouchEvent，此时进行滑动方向判断
     */
    public void onTouchEvent(MotionEvent event) {
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

    private void HideInputSoft() {
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
            int alpha = (int) ((1F - 1F * translation / mDecorView.getMeasuredWidth()) * 255);
            if (alpha < 0) {
                alpha = 0;
            } else if (alpha > 255) {
                alpha = 255;
            }
            mShadowView.getBackground().setAlpha(alpha);
        }
        mSwipeBackView.setTranslationX(translation);
        mShadowView.setTranslationX(translation - mDecorView.getMeasuredWidth());
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
            mSwipeAnimator = new DecelerateAnimator();
            mSwipeAnimator.addListener(this);
            mSwipeAnimator.addUpdateListener(this);
        }
        mSwipeAnimator.startAnimator(startValue, minFinalValue, maxFinalValue, velocity);
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        float animatedValue = (Float) animation.getAnimatedValue();
        swipeBackEvent((int) animatedValue);
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        //最终移动距离位置超过半宽，结束当前Activity
        if (2 * mSwipeBackView.getTranslationX() >= mDecorView.getMeasuredWidth()) {
            mSwipeBackActivity.finish();
            mSwipeBackActivity.overridePendingTransition(-1, -1);//取消返回动画
        } else {
            mSwipeBackView.setTranslationX(0);
            mShadowView.setTranslationX(-mDecorView.getMeasuredWidth());
        }
    }

    @Override
    public void onAnimationStart(Animator animation) {
    }

    @Override
    public void onAnimationCancel(Animator animation) {
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
    }

}
