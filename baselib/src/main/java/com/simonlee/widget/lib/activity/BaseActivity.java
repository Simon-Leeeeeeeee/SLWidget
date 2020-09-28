package com.simonlee.widget.lib.activity;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.DisplayCutout;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.simonlee.widget.lib.annotation.ImmersiveFlag;
import com.simonlee.widget.lib.annotation.NotchDisplayMode;
import com.simonlee.widget.lib.dialog.BaseDialog;
import com.simonlee.widget.lib.permission.PermissionManager;
import com.simonlee.widget.lib.widget.watermark.WaterMarkContentFrameLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ContentFrameLayout;
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
     * 被SystemUI占据的区域，如状态栏、导航栏、全面屏手势指示器等。取值与SystemUI是否被隐藏无关
     * <p>
     * 注意：
     * <p>
     * 这里的left、top、right、bottom，指的是距离对应窗口边界的值
     */
    private Rect mSystemUIRect = new Rect();

    /**
     * 被SystemWindow占据的区域，除了被SystemUI（状态栏、导航栏、全面屏手势指示器等）实际占据的区域外，还包含了异形屏凹槽区域占据的区域
     * <p>
     * 注意：
     * <p>
     * 这里的left、top、right、bottom，指的是距离对应窗口边界的值
     * <p>
     * 特例：
     * <p>
     * 1. 窗口模式时，不包含异形屏凹槽区域
     * <p>
     * 2. 通过{@link WindowManager.LayoutParams#FLAG_FULLSCREEN}实现全屏时，不包含软键盘区域
     */
    private Rect mSystemWindowRect = new Rect();

    /**
     * 被异形屏凹槽占据的区域
     * <p>
     * 注意：
     * <p>
     * 这里的left、top、right、bottom，指的是距离对应窗口边界的值
     */
    private Rect mDisplayCutoutRect = new Rect();

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
     * 返回Activity实例，尽可能用getActivity()来代替this的使用
     */
    public <T extends BaseActivity> T getActivity() {
        //noinspection unchecked
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
     * 返回ContentView的父容器 (R.id.content)
     * <p>
     * 在Activity中是一个{@link FrameLayout}，在AppCompatActivity中是一个{@link ContentFrameLayout}
     */
    public FrameLayout getContentParent() {
        return getDecorView().findViewById(Window.ID_ANDROID_CONTENT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 监听窗口变化，用于实现布局适配
        setOnApplyWindowInsetsListener();
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
    public <T extends View> T getTitleBar() {
        //noinspection unchecked
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
    public <T extends View> T removeTitleBar() {
        final View titleBar = getTitleBar();
        this.mTitleBar = null;
        if (titleBar != null && titleBar.getParent() != null) {
            //移除原TitleBar
            ((ViewGroup) titleBar.getParent()).removeViewInLayout(titleBar);
        }
        //noinspection unchecked
        return (T) titleBar;
    }

    /**
     * 将TitleBar插入到布局中，这里插入的是DecorView的孙View，介于DecorView与ContentParent之间的FrameLayout
     * <p>
     * Activity的结构：DecorView - LinearLayout - FrameLayout(R.id.content)
     * <p>
     * AppCompatActivity的结构：DecorView - LinearLayout - FrameLayout - FitWindowsFrameLayout/FitWindowsLinearLayout/ActionBarOverlayLayout - ContentFrameLayout(R.id.content)
     * <p>
     * 注意：
     * <p>
     * 窗口模式下会在DecorView - LinearLayout之间多出一层DecorCaptionView
     */
    protected void insertTitleBar() {
        if (mTitleBar == null || mTitleBar.getParent() != null) {
            return;
        }
        ViewGroup subDecorView = (ViewGroup) getContentParent().getParent().getParent();
        //这里要避免将TitleBar插入DecorView中
        if ((subDecorView instanceof FrameLayout) && subDecorView != getDecorView()) {
            //插入TitleBar
            subDecorView.addView(mTitleBar);
        }
    }

    /**
     * 监听窗口变化，用于实现布局适配
     */
    private void setOnApplyWindowInsetsListener() {
        //设置WindowInsets监听
        getDecorView().setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View view, WindowInsets insets) {
                mSystemUIRect.set(
                        insets.getStableInsetLeft(), insets.getStableInsetTop(),
                        insets.getStableInsetRight(), insets.getStableInsetBottom());
                mSystemWindowRect.set(
                        insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                        insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    DisplayCutout cutout = insets.getDisplayCutout();
                    if (cutout == null) {
                        mDisplayCutoutRect.setEmpty();
                    } else {
                        mDisplayCutoutRect.set(
                                cutout.getSafeInsetLeft(), cutout.getSafeInsetTop(),
                                cutout.getSafeInsetRight(), cutout.getSafeInsetBottom());
                    }
                }
                return view.onApplyWindowInsets(insets);
            }
        });
        //设置LayoutChange监听
        getDecorView().addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(final View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                //插入TitleBar
                insertTitleBar();
                //窗口适配，任何布局变化都进行处理
                onApplyWindowInsets(computeWindowInsetsLeft(), computeWindowInsetsTop(), computeWindowInsetsRight(), computeWindowInsetsBottom());
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //窗口适配，页面切回时可能页面异常
        onApplyWindowInsets(computeWindowInsetsLeft(), computeWindowInsetsTop(), computeWindowInsetsRight(), computeWindowInsetsBottom());
    }

    /**
     * 窗口适配，根据异形屏、状态栏、导航栏、全屏、设备朝向等适配
     *
     * @param safeLeft   左侧安全距离，导航栏（横屏）、异形屏凹槽等
     * @param safeTop    顶部安全距离，状态栏、异形屏凹槽等
     * @param safeRight  右侧安全距离，导航栏（横屏）、异形屏凹槽等
     * @param safeBottom 底部安全距离，导航栏（竖屏）、全面屏手势指示器、异形屏凹槽等
     */
    protected void onApplyWindowInsets(int safeLeft, int safeTop, int safeRight, int safeBottom) {
        int titleBarHeight = 0;

        //修正TitleBar的高度和Padding值以适应窗口变化
        resizeTitleBar(safeLeft, safeTop, safeRight);

        //获取TitleBar高度
        if (mTitleBar != null && mTitleBar.getParent() != null && mTitleBar.getVisibility() != View.GONE) {
            titleBarHeight = mTitleBar.getLayoutParams().height;
        }

        //校正WaterMark顶部安全距离，防止与标题栏/状态栏重叠
        int waterMarkSafeTop = titleBarHeight > 0 ? titleBarHeight : (isFullScreen() || isStatusBarHidded() ? 0 : safeTop);
        //修正WaterMark的Padding值以适应窗口变化
        resizeWaterMark(safeLeft, waterMarkSafeTop, safeRight, safeBottom);

        //校正ContentParent顶部安全距离
        int contentSafeTop = titleBarHeight > 0 ? titleBarHeight : safeTop;
        //修正ContentParent的Padding值以适应窗口变化
        resizeContentParent(safeLeft, contentSafeTop, safeRight, safeBottom);
    }

    /**
     * 修正TitleBar的高度和Padding值以适应窗口变化
     */
    protected void resizeTitleBar(int safeLeft, int safeTop, int safeRight) {
        if (mTitleBar != null && mTitleBar.getParent() != null && mTitleBar.getVisibility() != View.GONE) {
            if (safeTop != mTitleBar.getPaddingTop() || safeLeft != mTitleBar.getPaddingLeft() || safeRight != mTitleBar.getPaddingRight()) {
                mTitleBar.setPadding(safeLeft, safeTop, safeRight, 0);
            }
            //指定TitleBar高度为PaddingTop加上ActionBar的高度
            int titleBarHeight = safeTop + getActionBarSize();
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
        }
    }

    /**
     * 修正WaterMark的Padding值以适应窗口变化
     */
    protected void resizeWaterMark(int safeLeft, int safeTop, int safeRight, int safeBottom) {
        getWaterMark().setDrawPaddingTop(safeTop);//非全屏避免水印覆盖状态栏
    }

    /**
     * 修正ContentParent的Padding值以适应窗口变化
     */
    protected void resizeContentParent(int safeLeft, int safeTop, int safeRight, int safeBottom) {
        //调整ContentParent的Padding值
        FrameLayout contentParent = getContentParent();
        if (contentParent instanceof ContentFrameLayout
                && (safeLeft != contentParent.getPaddingLeft()
                || safeTop != contentParent.getPaddingTop()
                || safeRight != contentParent.getPaddingRight()
                || safeBottom != contentParent.getPaddingBottom())) {
            contentParent.setPadding(safeLeft, safeTop, safeRight, safeBottom);
        }
    }

    /**
     * 根据异形屏、状态栏、导航栏、全屏、设备朝向等适配，计算出距离左边窗口边界的安全距离
     */
    public int computeWindowInsetsLeft() {
        if (isNavigationBarLayouted()) {
            if (isNavigationBarHidded()) {
                return mDisplayCutoutRect.left;
            }
            return mSystemWindowRect.left;
        } else if (isStatusBarLayouted()) {
            return mDisplayCutoutRect.left;
        }
        return 0;
    }

    /**
     * 根据异形屏、状态栏、导航栏、全屏、设备朝向等适配，计算出距离上边窗口边界的安全距离
     */
    public int computeWindowInsetsTop() {
        if (isStatusBarLayouted() || isNavigationBarLayouted()) {
            if (isStatusBarHidded()) {
                return mDisplayCutoutRect.top;
            }
            return mSystemWindowRect.top;
        }
        return 0;
    }

    /**
     * 根据异形屏、状态栏、导航栏、全屏、设备朝向等适配，计算出距离右边窗口边界的安全距离
     */
    public int computeWindowInsetsRight() {
        if (isNavigationBarLayouted()) {
            if (isNavigationBarHidded()) {
                return mDisplayCutoutRect.right;
            }
            return mSystemWindowRect.right;
        } else if (isStatusBarLayouted()) {
            return mDisplayCutoutRect.right;
        }
        return 0;
    }

    /**
     * 根据异形屏、状态栏、导航栏、全屏、设备朝向等适配，计算出距离下边窗口边界的安全距离
     */
    public int computeWindowInsetsBottom() {
        if (isNavigationBarLayouted()) {
            if (isNavigationBarHidded()) {
                return mDisplayCutoutRect.bottom;
            }
            return mSystemWindowRect.bottom;
        } else if (isStatusBarLayouted() && isNavigationBarHidded()) {
            return mDisplayCutoutRect.bottom;
        }
        return 0;
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
     * 1. 不代表导航栏是否显示，仅表示导航栏默认高度属性
     * <p>
     * 2. 横屏时，导航栏可能在侧方（虚拟按键），也可能在底部（全面屏手势指示器）
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
     * 2. 横屏时，导航栏可能在视图底部（全屏指示器），也可能在试图侧边（虚拟按键）
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
     * 显示状态栏
     * <p>
     * 注意：若设置了窗口全屏{@link #setFullScreen(boolean)}，需要取消全屏，否则状态栏不会显示
     */
    public void showStatusBar() {
        //窗口模式下不允许设置
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInMultiWindowMode()) {
            return;
        }
        int visibility = getDecorView().getSystemUiVisibility();
        //取消FLAG
        visibility &= ~View.SYSTEM_UI_FLAG_FULLSCREEN;
        //生效
        getDecorView().setSystemUiVisibility(visibility);
    }

    /**
     * 显示导航栏
     */
    public void showNavigationBar() {
        //窗口模式下不允许设置
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInMultiWindowMode()) {
            return;
        }
        int visibility = getDecorView().getSystemUiVisibility();
        //取消FLAG
        visibility &= ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        //生效
        getDecorView().setSystemUiVisibility(visibility);
    }

    /**
     * 隐藏状态栏
     * <p>
     * 注意：
     * <p>
     * 1. 无法避免在【页面切换、弹出软键盘】时失效
     * <p>
     * 2. Flag与StableLayout都将会同步影响导航栏的隐藏控制
     *
     * @param flag         控制状态栏/导航栏的隐藏状态将会在何种情况下失效
     *                     <p>
     *                     FLAG_NONE：当【页面切换、软键盘弹出、下拉状态栏】时失效，若同时隐藏导航栏，当【任意触摸】即失效
     *                     <p>
     *                     FLAG_IMMERSIVE：当【页面切换、软键盘弹出、下拉状态栏】时失效，若同时隐藏导航栏，当【上滑导航栏】也会失效
     *                     <p>
     *                     FLAG_IMMERSIVE_STICKY：当【页面切换、软键盘弹出】时失效
     *                     <p><br/>
     * @param stableLayout 是否保持页面布局，若为true，当状态栏/导航栏隐藏时不会入侵该区域
     */
    public void hideStatusBar(@ImmersiveFlag int flag, boolean stableLayout) {
        //窗口模式下不允许设置
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInMultiWindowMode()) {
            return;
        }
        if (stableLayout) {
            flag |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        }
        int visibility = getDecorView().getSystemUiVisibility();
        //先清除FLAG
        visibility &= ~(View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        //再设置FLAG
        visibility |= (View.SYSTEM_UI_FLAG_FULLSCREEN | flag);
        //生效
        getDecorView().setSystemUiVisibility(visibility);
    }

    /**
     * 隐藏导航栏
     * <p>
     * 注意：
     * <p>
     * 1. 无法避免在【页面切换、弹出软键盘】时失效
     * <p>
     * 2. Flag与StableLayout都将会同步影响状态栏的隐藏控制
     *
     * @param flag         控制导航栏/状态栏的隐藏状态将会在何种情况下失效
     *                     <p>
     *                     FLAG_NONE：当【任意触摸】即失效
     *                     <p>
     *                     FLAG_IMMERSIVE：当【页面切换、软键盘弹出、上滑导航栏】时失效，若同时隐藏状态栏，【下拉状态栏】也失效
     *                     <p>
     *                     FLAG_IMMERSIVE_STICKY：当【页面切换、软键盘弹出】时失效
     *                     <p><br/>
     * @param stableLayout 是否保持页面布局，若为true，当导航栏/状态栏隐藏时不会入侵该区域
     */
    public void hideNavigationBar(@ImmersiveFlag int flag, boolean stableLayout) {
        //窗口模式下不允许设置
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInMultiWindowMode()) {
            return;
        }
        if (stableLayout) {
            flag |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        }
        int visibility = getDecorView().getSystemUiVisibility();
        //先清除FLAG
        visibility &= ~(View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        //再设置FLAG
        visibility |= (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | flag);
        //生效
        getDecorView().setSystemUiVisibility(visibility);
    }

    /**
     * 是否设置全屏显示
     */
    public boolean isFullScreen() {
        return (getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
    }

    /**
     * 设置全屏显示
     * <p>
     * 注意：
     * <p>
     * 状态栏将被隐藏，但导航栏不会
     */
    public void setFullScreen(boolean enable) {
        //窗口模式下不允许设置
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInMultiWindowMode()) {
            return;
        }
        if (enable == isFullScreen()) {
            return;
        }
        if (enable) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    /**
     * 获取异形屏适配模式
     *
     * @return <p>
     * MODE_DEFAULT：默认，非全屏可使用凹槽区域，全屏不可使用
     * <p>
     * MODE_SHORT_EDGES：适配异形屏，全屏、非全屏均可使用凹槽区域
     * <p>
     * MODE_NEVER：不适配异形屏，不可使用凹槽区域
     */
    public @NotchDisplayMode
    int getNotchDisplayMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams attributes = getWindow().getAttributes();
            return attributes.layoutInDisplayCutoutMode;
        } else return NotchDisplayMode.MODE_DEFAULT;
    }

    /**
     * 设置异形屏适配模式
     * <p>
     * MODE_DEFAULT：默认，非全屏可使用凹槽区域，全屏不可使用
     * <p>
     * MODE_SHORT_EDGES：适配异形屏，全屏、非全屏均可使用凹槽区域
     * <p>
     * MODE_NEVER：不适配异形屏，不可使用凹槽区域
     */
    @RequiresApi(Build.VERSION_CODES.P)
    public void setNotchDisplayMode(@NotchDisplayMode int mode) {
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.layoutInDisplayCutoutMode = mode;
        getWindow().setAttributes(attributes);
    }

    /**
     * 隐藏软键盘
     */
    public void hideInputSoft() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(getDecorView().getWindowToken(), 0);
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