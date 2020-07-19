package com.simonlee.widget.lib.activity;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ContentFrameLayout;
import android.util.AttributeSet;
import android.view.Display;
import android.view.DisplayCutout;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.simonlee.widget.lib.dialog.BaseDialog;
import com.simonlee.widget.lib.permission.PermissionManager;
import com.simonlee.widget.lib.widget.watermark.WaterMarkContentFrameLayout;

import cn.simonlee.widget.watermark.WaterMark;

/**
 * Activity基类
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-07-17
 */
@SuppressWarnings("unused")
public abstract class BaseActivity extends AppCompatActivity implements BaseDialog.DialogResultListener {

    /**
     * 根布局
     */
    private ViewGroup mDecorView;

    /**
     * 自定义的TitleBar
     */
    private View mTitleBar;

    /**
     * 水印，在水印布局中实例化
     */
    private WaterMark mWaterMark;

    /**
     * 被SystemUI占据的区域，如状态栏、导航栏、全面屏手势指示器。取值与SystemUI是否被隐藏无关
     * <p>
     * 注意：
     * <p>
     * 这里的left、top、right、bottom，指的是距离对应边界的高度
     */
    private Rect mSystemUIRect = new Rect();

    /**
     * 被SystemWindow占据的区域，除了SystemUI，还包含了异形屏凹槽区域、软件盘弹出区域。取值以实际占用为准，即SystemUI被隐藏时将会被排除
     * <p>
     * 注意：
     * <p>
     * 这里的left、top、right、bottom，指的是距离对应边界的高度
     * <p>
     * 特例：
     * <p>
     * 窗口模式时，不包含异形屏凹槽区域
     */
    private Rect mSystemWindowRect = new Rect();

    /**
     * 被异形屏凹槽占据的区域
     * <p>
     * 注意：
     * <p>
     * 这里的left、top、right、bottom，指的是距离对应边界的高度
     */
    private Rect mDisplayCutoutRect = new Rect();

    /**
     * 返回Activity实例，尽可能用getActivity()来代替this的使用
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseActivity> T getActivity() {
        return (T) BaseActivity.this;
    }

    /**
     * 返回DecorView
     */
    public ViewGroup getDecorView() {
        if (mDecorView == null) {
            this.mDecorView = (ViewGroup) getWindow().getDecorView();
        }
        return mDecorView;
    }

    /**
     * 返回ContentView的父容器（R.id.content）
     */
    public FrameLayout getContentParent() {
        return getDecorView().findViewById(Window.ID_ANDROID_CONTENT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDecorView().addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(final View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                // DecorView布局变化监听回调
                onDecorViewLayoutChange(left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom);
            }
        });
        // 添加DecorView布局变化监听
        getDecorView().setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View view, WindowInsets insets) {
                onDecorViewApplyWindowInsets(insets);
                return view.onApplyWindowInsets(insets);
            }
        });
    }

    /**
     * 当窗口发生变化时将会被调用
     * <p>
     * 包含：
     * <p>
     * 状态栏显隐变化、导航栏显隐变化、软件盘变化、异形屏凹槽变化
     */
    private void onDecorViewApplyWindowInsets(WindowInsets insets) {
        mSystemUIRect.set(insets.getStableInsetLeft(), insets.getStableInsetTop(), insets.getStableInsetRight(), insets.getStableInsetBottom());
        mSystemWindowRect.set(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(), insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            DisplayCutout cutout = insets.getDisplayCutout();
            if (cutout != null) {
                mDisplayCutoutRect.set(cutout.getSafeInsetLeft(), cutout.getSafeInsetTop(), cutout.getSafeInsetRight(), cutout.getSafeInsetBottom());
            } else {
                mDisplayCutoutRect.setEmpty();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 修复【页面切换】操作时，隐藏SystemUI会失效的问题
        if (isSystemUIAlwaysHidded()) {
            hideSystemUIAlways();
        }
    }

    /**
     * 当布局发生变化时将会被调用
     */
    protected void onDecorViewLayoutChange(int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        //获取软键盘高度
        int inputSoftHeight = getInputSoftHeight();
        //软键盘高度变化
        fixInputSoftAdjustResize(inputSoftHeight);
        //插入TitleBar
        insertTitleBar();
        //修正TitleBar的高度和paddingTop以适配状态栏及异形屏
        fitTitleBarHeight();
        //当开启SystemUI常隐，且当前输入法已隐藏，采用post方式来再次隐藏SystemUI
        if (isSystemUIAlwaysHidded() && getContentParent().getPaddingBottom() <= getRealNavigationBarHeight()) {
            getDecorView().post(new Runnable() {
                @Override
                public void run() {
                    hideSystemUIAlways();
                }
            });
        }
    }

    /**
     * 获取软键盘高度
     * <p>
     * 注意：这里实际上获取的是contentParent下部不可见部分的高度，一般情况下不可见原因即为软键盘的遮挡
     */
    public int getInputSoftHeight() {
        //获取contentParent
        FrameLayout contentParent = getContentParent();
        //获取contentParent的可见区域
        Rect visibleRect = new Rect();
        contentParent.getWindowVisibleDisplayFrame(visibleRect);
        //计算contentParent的不可见高度
        int invisibleHeight = contentParent.getBottom() - visibleRect.bottom;
        //当导航栏位置被布局占用时，需要特殊处理
        if (isNavigationBarLayouted() && invisibleHeight > 0
                && invisibleHeight <= getRealNavigationBarHeight()) {
            return 0;
        }
        return invisibleHeight;
    }

    /**
     * 软键盘高度变化回调
     *
     * <p>
     * 注意：这里的软键盘的高度实际为ContentParent下部不可见部分的高度，一般情况下不可见原因即为软键盘的遮挡
     *
     * @param inputSoftHeight 软键盘高度
     */
    protected void fixInputSoftAdjustResize(int inputSoftHeight) {
        //仅当状态栏位置被布局占用或导航栏位置被布局占用时进行修复
        if (isStatusBarLayouted() || isNavigationBarLayouted()) {
            //获取contentParent
            FrameLayout contentParent = getContentParent();
            if (inputSoftHeight != contentParent.getPaddingBottom()) {
                //设置paddingBottom，限定child在可见区域内
                contentParent.setPadding(contentParent.getPaddingLeft(), contentParent.getPaddingTop(), contentParent.getPaddingRight(), inputSoftHeight);
            }
        }
    }

    /**
     * 替换水印布局
     */
    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        //将R.id.content替换为水印布局
        if (getWaterMark() == null && ContentFrameLayout.class.getName().equals(name)) {
            //继承自ContentFrameLayout的水印布局
            WaterMarkContentFrameLayout waterMarkContentFrameLayout = new WaterMarkContentFrameLayout(this, attrs);
            //获取水印
            mWaterMark = waterMarkContentFrameLayout.getWaterMark();
            //替换ContentFrameLayout
            return waterMarkContentFrameLayout;
        }
        return super.onCreateView(name, context, attrs);
    }

    /**
     * 返回WaterMark，用于操作水印。
     */
    public WaterMark getWaterMark() {
        return mWaterMark;
    }

    /**
     * 获取TitleBar
     */
    @SuppressWarnings("unchecked")
    public <T extends View> T getTitleBar() {
        return (T) mTitleBar;
    }

    /**
     * 设置TitleBar
     *
     * @param titleBar 自定义TitleBar
     */
    public void setTitleBar(@NonNull View titleBar) {
        if (titleBar == mTitleBar) {
            //同一个实例，直接返回
            return;
        }
        if (titleBar.getParent() != null) {
            //新TitleBar已有父容器，直接抛出异常
            throw new IllegalStateException("The titleBar already has a parent.");
        }
        if (mTitleBar != null && mTitleBar.getParent() != null) {
            //移除原TitleBar
            ((ViewGroup) mTitleBar.getParent()).removeViewInLayout(mTitleBar);
        }
        //替换TitleBar
        this.mTitleBar = titleBar;
        //插入布局
        insertTitleBar();
    }

    /**
     * 移除TitleBar，并返回实例
     */
    @SuppressWarnings("unchecked")
    public <T extends View> T removeTitleBar() {
        final View titleBar = getTitleBar();
        this.mTitleBar = null;
        if (titleBar != null && titleBar.getParent() != null) {
            //移除原TitleBar
            ((ViewGroup) titleBar.getParent()).removeViewInLayout(titleBar);
        }
        return (T) titleBar;
    }

    /**
     * 将TitleBar插入到布局中，这里插入的是DecorView的孙View，介于DecorView与ContentParent之间的一个FrameLayout
     * <p>
     * AppCompatActivity的结构：DecorView - LinearLayout - FrameLayout - FitWindowsFrameLayout/FitWindowsLinearLayout - ContentParent
     */
    protected void insertTitleBar() {
        if (mTitleBar == null || mTitleBar.getParent() != null) {
            return;
        }
        FrameLayout subDecorView = (FrameLayout) getContentParent().getParent().getParent();
        //Activity的结构：DecorView - LinearLayout - ContentParent，这里要避免将TitleBar插入DecorView中
        if (subDecorView != getDecorView()) {
            //插入TitleBar
            subDecorView.addView(mTitleBar);
        }
    }

    /**
     * 修正TitleBar的高度和paddingTop以适应状态栏及异形屏变化，修正ContentView的上边距以适应TitleBar高度变化
     */
    protected void fitTitleBarHeight() {
        //根据异形屏适配、全屏适配、横竖屏适配、状态栏适配来计算TitleBar的paddingTop
        int paddingTop = computeTitleBarPaddingTop();
        if (mTitleBar == null || mTitleBar.getParent() == null || mTitleBar.getVisibility() == View.GONE) {
            //TitleBar高度变化
            onTitleBarHeightResize(paddingTop);
        } else {
            //根据异形屏适配、全屏适配、横竖屏适配、导航栏适配来计算TitleBar的paddingLeft、paddingRight
            int paddingLeft = computeTitleBarPaddingLeft();
            int paddingRight = computeTitleBarPaddingRight();
            //注意点：横屏，异形屏，支持，占用布局，会导致左右进行占用
            if (paddingTop != mTitleBar.getPaddingTop() || paddingLeft != mTitleBar.getPaddingLeft() || paddingRight != mTitleBar.getPaddingRight()) {
                mTitleBar.setPadding(paddingLeft, paddingTop, paddingRight, 0);
            }
            //指定TitleBar高度为PaddingTop加上ActionBar的高度
            int titleBarHeight = paddingTop + getActionBarSize();
            //获取TitleBar布局参数
            ViewGroup.LayoutParams titleBarLayoutParams = mTitleBar.getLayoutParams();
            //指定TitleBar布局宽高
            if (titleBarLayoutParams == null) {
                titleBarLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, titleBarHeight);
                mTitleBar.setLayoutParams(titleBarLayoutParams);
            } else if (titleBarLayoutParams.height != titleBarHeight) {
                titleBarLayoutParams.height = titleBarHeight;
                mTitleBar.setLayoutParams(titleBarLayoutParams);
            }
            //TitleBar高度变化
            onTitleBarHeightResize(titleBarHeight);
        }
    }

    /**
     * 根据异形屏适配、全屏适配、横竖屏适配、导航栏适配来计算TitleBar的paddingLeft值，避免TitleBar与导航栏/异形凹槽重叠
     */
    protected int computeTitleBarPaddingLeft() {
        if (isNavigationBarLayouted()) {
            return mSystemWindowRect.left;
        } else if (isStatusBarLayouted() && isSupportNotchDisplay()) {
            return mSystemWindowRect.left;
        }
        return 0;
    }

    /**
     * 根据异形屏适配、全屏适配、横竖屏适配、导航栏适配来计算TitleBar的paddingRight值，避免TitleBar与导航栏/异形凹槽重叠
     */
    protected int computeTitleBarPaddingRight() {
        if (isNavigationBarLayouted()) {
            return mSystemWindowRect.right;
        } else if (isStatusBarLayouted() && isSupportNotchDisplay()) {
            return mSystemWindowRect.right;
        }
        return 0;
    }

    /**
     * 根据异形屏适配、全屏适配、横竖屏适配、状态栏适配来计算TitleBar的paddingTop值，避免TitleBar与状态栏/异形凹槽重叠
     */
    protected int computeTitleBarPaddingTop() {
        if (isStatusBarLayouted() || isNavigationBarLayouted()) {
            return mSystemWindowRect.top;
        }
        return 0;
    }

    /**
     * TitleBar高度变化，需要调整ContentView的PaddingTop，避免与TitleBar重叠
     *
     * @param titleBarHeight TitleBar的高度
     */
    protected void onTitleBarHeightResize(int titleBarHeight) {
        FrameLayout contentParent = getContentParent();
        if (contentParent != null && contentParent.getPaddingTop() != titleBarHeight) {
            contentParent.setPadding(contentParent.getPaddingLeft(), titleBarHeight, contentParent.getPaddingRight(), contentParent.getPaddingBottom());
        }
        //调整水印上边距，使与ContentView上部对齐
        if (getWaterMark() != null) {
            getWaterMark().setDrawPaddingTop(titleBarHeight);
        }
    }

    /**
     * 获取状态栏的高度，单位px
     * <p>
     * 注意：
     * <p>
     * 不代表状态栏是否显示，仅表示状态栏默认高度属性
     */
    public int getStatusBarHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        try {
            return getResources().getDimensionPixelSize(resourceId);
        } catch (Resources.NotFoundException e) {
            return 0;
        }
    }

    /**
     * 获取导航栏高度，单位px
     * <p>
     * 注意：
     * <p>
     * 不代表导航栏是否显示，仅表示导航栏默认高度属性
     */
    public int getNavigationBarHeight() {
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        try {
            return getResources().getDimensionPixelSize(resourceId);
        } catch (Resources.NotFoundException e) {
            return 0;
        }
    }

    /**
     * 获取导航栏实际高度，单位px
     * <p>
     * 注意：
     * <p>
     * 通过设置FLAG来隐藏导航栏，返回的高度保持不变；通过启用全面屏手势来改变导航栏，返回高度发生变化
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public int getRealNavigationBarHeight() {
        WindowManager windowManager = getWindowManager();
        Point appSize = new Point();
        Point deviceSize = new Point();
        Display defaultDisplay = windowManager.getDefaultDisplay();

        defaultDisplay.getSize(appSize);
        defaultDisplay.getRealSize(deviceSize);

        return deviceSize.y - appSize.y;
    }

    /**
     * 设置状态栏背景色
     *
     * @param backgroundColor 状态栏背景色，可设置透明度
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public void setStatusBarColor(@ColorInt int backgroundColor) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(backgroundColor);
    }

    /**
     * 设置导航栏背景色
     *
     * @param backgroundColor 导航栏背景色
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public void setNavigationBarColor(@ColorInt int backgroundColor) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setNavigationBarColor(backgroundColor);
    }

    /**
     * 设置状态栏暗色图文样式
     *
     * @param darkStyle 是否暗色图文样式
     */
    @RequiresApi(Build.VERSION_CODES.M)
    public void setStatusBarDarkStyle(boolean darkStyle) {
        int visibility = getDecorView().getSystemUiVisibility();
        if (darkStyle) {
            //设置FLAG
            visibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
            //取消FLAG
            visibility &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        //生效
        getDecorView().setSystemUiVisibility(visibility);
    }

    /**
     * 设置导航栏暗色图文样式
     *
     * @param darkStyle 是否暗色图文样式
     */
    @RequiresApi(Build.VERSION_CODES.O)
    public void setNavigationBarDarkStyle(boolean darkStyle) {
        int visibility = getDecorView().getSystemUiVisibility();
        if (darkStyle) {
            //设置FLAG
            visibility |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        } else {
            //取消FLAG
            visibility &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        //生效
        getDecorView().setSystemUiVisibility(visibility);
    }

    /**
     * 是否占用状态栏位置进行布局
     */
    public boolean isStatusBarLayouted() {
        return (getDecorView().getSystemUiVisibility() & View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN) != 0;
    }

    /**
     * 占用状态栏位置进行布局，但不控制状态栏是否隐藏
     *
     * @param layouted 是否占用状态栏位置进行布局
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    public void setStatusBarLayouted(boolean layouted) {
        //窗口模式下不允许设置
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInMultiWindowMode()) {
            return;
        }
        int visibility = getDecorView().getSystemUiVisibility();
        if (layouted) {
            //设置FLAG
            visibility |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        } else {
            //取消FLAG
            visibility &= ~View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        }
        //生效
        getDecorView().setSystemUiVisibility(visibility);
    }

    /**
     * 是否占用导航栏位置进行布局
     */
    public boolean isNavigationBarLayouted() {
        return (getDecorView().getSystemUiVisibility() & View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION) != 0;
    }

    /**
     * 占用导航栏位置进行布局，但不控制导航栏是否隐藏
     * <p>
     * 注意：
     * <p>
     * 1. 若布局占用导航栏位置，状态栏位置也会被布局占用
     * <p>
     * 2. 横屏时，导航栏可能在视图底部（全屏指示器），也可能在试图侧边（虚拟按键），不建议占用
     *
     * @param layouted 是否占用导航栏位置进行布局
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    public void setNavigationBarLayouted(boolean layouted) {
        //窗口模式下不允许设置
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInMultiWindowMode()) {
            return;
        }
        int visibility = getDecorView().getSystemUiVisibility();
        if (layouted) {
            //设置FLAG
            visibility |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        } else {
            //取消FLAG
            visibility &= ~View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }
        //生效
        getDecorView().setSystemUiVisibility(visibility);
    }

    /**
     * 状态栏是否被隐藏
     */
    public boolean isStatusBarHidded() {
        return (getDecorView().getSystemUiVisibility() & View.SYSTEM_UI_FLAG_FULLSCREEN) != 0;
    }

    /**
     * 导航栏是否被隐藏
     */
    public boolean isNavigationBarHidded() {
        return (getDecorView().getSystemUiVisibility() & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0;
    }

    /**
     * 显示状态栏和导航栏
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    public void showSystemUI() {
        //窗口模式下不允许设置
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInMultiWindowMode()) {
            return;
        }
        int visibility = getDecorView().getSystemUiVisibility();
        final int removeFlag = View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_IMMERSIVE;
        //取消FLAG
        visibility &= ~removeFlag;
        //生效
        getDecorView().setSystemUiVisibility(visibility);
    }

    /**
     * 隐藏状态栏和导航栏一次，当【任意操作】时失效
     * <p>
     * 注意：
     * <p>
     * 在刘海屏设备上，状态栏区域将为纯黑。若要适配，需调用{@link #supportNotchDisplay(boolean)}
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    public void hideSystemUI() {
        //窗口模式下不允许设置
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInMultiWindowMode()) {
            return;
        }
        int visibility = getDecorView().getSystemUiVisibility();
        final int addFlag = View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        final int removeFlag = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_IMMERSIVE;
        //设置FLAG
        visibility |= addFlag;
        //取消FLAG
        visibility &= ~removeFlag;
        //生效
        getDecorView().setSystemUiVisibility(visibility);
    }

    /**
     * 隐藏状态栏和导航栏，当【软键盘/页面切换】时失效
     * <p>
     * 注意：
     * <p>
     * 在刘海屏设备上，状态栏区域将为纯黑。若要适配，需调用{@link #supportNotchDisplay(boolean)}
     */
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    public void hideSystemUISticky() {
        //窗口模式下不允许设置
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInMultiWindowMode()) {
            return;
        }
        int visibility = getDecorView().getSystemUiVisibility();
        final int addFlag = View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        final int removeFlag = View.SYSTEM_UI_FLAG_IMMERSIVE;
        //设置FLAG
        visibility |= addFlag;
        //取消FLAG
        visibility &= ~removeFlag;
        //生效
        getDecorView().setSystemUiVisibility(visibility);
    }

    /**
     * 始终隐藏状态栏和导航栏
     * <p>
     * 注意：
     * <p>
     * 1. 在刘海屏设备上，状态栏区域将为纯黑。若要适配，需调用{@link #supportNotchDisplay(boolean)}
     * <p>
     * 2. 此方法同{@link #hideSystemUISticky()}一样，当【软键盘/页面切换】时失效，
     * 但是新增了一个{@link View#SYSTEM_UI_FLAG_IMMERSIVE}标记，
     * 可以在恰当时候通过判断该标记予以恢复
     */
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    public void hideSystemUIAlways() {
        //窗口模式下不允许设置
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInMultiWindowMode()) {
            return;
        }
        int visibility = getDecorView().getSystemUiVisibility();
        final int addFlag = View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_IMMERSIVE;
        //设置FLAG
        visibility |= addFlag;
        //生效
        getDecorView().setSystemUiVisibility(visibility);
    }

    /**
     * 是否始终隐藏状态栏和导航栏
     */
    public boolean isSystemUIAlwaysHidded() {
        return (getDecorView().getSystemUiVisibility() & View.SYSTEM_UI_FLAG_IMMERSIVE) != 0;
    }

    /**
     * 适配刘海屏，仅当页面全屏显示时需要适配
     */
    @RequiresApi(Build.VERSION_CODES.P)
    public void supportNotchDisplay(boolean supported) {
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        if (supported) {
            //全屏/非全屏均可使用刘海区域
            attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        } else {
            //非全屏可使用刘海区域，全屏不可使用
            attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
        }
        getWindow().setAttributes(attributes);
    }

    /**
     * 是否已适配异形屏
     */
    public boolean isSupportNotchDisplay() {
        //判断Android P
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams attributes = getWindow().getAttributes();
            return attributes.layoutInDisplayCutoutMode == WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        return false;
    }

    /**
     * 返回异形屏缺口，非异形屏返回null
     */
    @Nullable
    public DisplayCutout getDisplayCutout() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowInsets windowInsets = getDecorView().getRootWindowInsets();
            return windowInsets != null ? windowInsets.getDisplayCutout() : null;
        }
        return null;
    }

    /**
     * 获取ActionBarSize属性，用于设置TitleBar高度
     */
    public int getActionBarSize() {
        TypedArray values = obtainStyledAttributes(new int[]{android.R.attr.actionBarSize});
        try {
            return values.getDimensionPixelSize(0, 0);//第一个参数数组索引，第二个参数 默认值
        } finally {
            values.recycle();
        }
    }

    /**
     * 隐藏软键盘
     */
    public void hideInputSoft() {
        View currentFocus = getDecorView().findFocus();
        if (currentFocus == null) {
            currentFocus = getCurrentFocus();
        }
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow((currentFocus == null ? getDecorView() : currentFocus).getWindowToken(), 0);
        }
    }

    /**
     * 显示软键盘
     */
    public void showInputSoft(@NonNull EditText editText) {
        View currentFocus = getDecorView().findFocus();
        if (currentFocus == null) {
            currentFocus = getCurrentFocus();
        }
        if (editText != currentFocus) {
            editText.requestFocus();
        }
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.showSoftInput(editText, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //PermissionManager代理权限请求后的返回处理
        if (!PermissionManager.delegateRequestPermissionsResult(this, requestCode, permissions, grantResults)) {
            //PermissionManager未处理该回调，则调用父类方法
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle data) {
    }

}