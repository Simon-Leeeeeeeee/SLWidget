package com.simonlee.widget.lib.activity;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
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
import android.view.DisplayCutout;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.simonlee.widget.lib.NotchDisplayMode;
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
        //设置窗口变化监听
        setWindowInsetsListener();
    }

    /**
     * 设置窗口变化监听，用于异形屏、状态栏、导航栏、全屏、软件盘等适配
     */
    private void setWindowInsetsListener() {
        // 监听窗口变化
        getDecorView().setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View view, WindowInsets insets) {
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
                return view.onApplyWindowInsets(insets);
            }
        });
        // 监听布局变化
        getDecorView().addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(final View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                //注意窗口变化回调放在这里的原因：窗口变化的回调不够频繁，避免布局变化时不能很好适配
                onApplyWindowInsets(computeWindowInsetsLeft(), computeWindowInsetsTop(), computeWindowInsetsRight(), computeWindowInsetsBottom());
            }
        });
    }

    /**
     * 窗口变化回调，用于异形屏、状态栏、导航栏、全屏、软件盘等适配
     *
     * @param left   左侧安全距离，导航栏（横屏）、异形屏凹槽等
     * @param top    顶部安全距离，状态栏、异形屏凹槽等
     * @param right  右侧安全距离，导航栏（横屏）、异形屏凹槽等
     * @param bottom 底部安全距离，导航栏（竖屏）、全面屏手势指示器、异形屏凹槽等
     */
    protected void onApplyWindowInsets(int left, int top, int right, int bottom) {
        //插入TitleBar
        insertTitleBar();
        //修正TitleBar的高度和Padding值以适应窗口变化
        resizeTitleBar(left, top, right);
        //修正ContentParent的Padding值以适应窗口变化
        resizeContentParent(left, top, right, bottom);
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
     * 修正TitleBar的高度和Padding值以适应窗口变化
     */
    protected void resizeTitleBar(int paddingLeft, int paddingTop, int paddingRight) {
        if (mTitleBar != null && mTitleBar.getParent() != null && mTitleBar.getVisibility() != View.GONE) {
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
            } else if (titleBarLayoutParams.height != titleBarHeight) {
                titleBarLayoutParams.height = titleBarHeight;
            }
            mTitleBar.setLayoutParams(titleBarLayoutParams);
        }
    }

    /**
     * 修正ContentParent的Padding值以适应窗口变化
     */
    protected void resizeContentParent(int paddingLeft, int paddingTop, int paddingRight, int paddingBottom) {
        if (mTitleBar != null && mTitleBar.getParent() != null && mTitleBar.getVisibility() != View.GONE) {
            //调整paddingTop为titleBar的高度，避免与TitleBar重叠
            paddingTop = mTitleBar.getLayoutParams().height;
        }
        //调整水印上边距
        if (getWaterMark() != null) {
            getWaterMark().setDrawPaddingTop(paddingTop);
        }
        //调整ContentParent的padding值
        FrameLayout contentParent = getContentParent();
        if (contentParent != null
                && (paddingLeft != contentParent.getPaddingLeft()
                || paddingTop != contentParent.getPaddingTop()
                || paddingRight != contentParent.getPaddingRight()
                || paddingBottom != contentParent.getPaddingBottom())) {
            contentParent.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        }
    }

    /**
     * 根据异形屏、状态栏、导航栏、全屏、软件盘等适配，计算出左边距安全距离
     */
    protected int computeWindowInsetsLeft() {
        if (isNavigationBarLayouted()) {
            return mSystemWindowRect.left;
        } else if (isStatusBarLayouted() && isSupportNotchDisplay()) {
            return mSystemWindowRect.left;
        }
        return 0;
    }

    /**
     * 根据异形屏、状态栏、导航栏、全屏、软件盘等适配，计算出上边距安全距离
     */
    protected int computeWindowInsetsTop() {
        if (isStatusBarLayouted() || isNavigationBarLayouted()) {
            return mSystemWindowRect.top;
        }
        return 0;
    }

    /**
     * 根据异形屏、状态栏、导航栏、全屏、软件盘等适配，计算出右边距安全距离
     */
    protected int computeWindowInsetsRight() {
        if (isNavigationBarLayouted()) {
            return mSystemWindowRect.right;
        } else if (isStatusBarLayouted() && isSupportNotchDisplay()) {
            return mSystemWindowRect.right;
        }
        return 0;
    }

    /**
     * 根据异形屏、状态栏、导航栏、全屏、软件盘等适配，计算出下边距安全距离
     */
    protected int computeWindowInsetsBottom() {
        if (isNavigationBarLayouted()) {
            return mSystemWindowRect.bottom;
        } else if (isStatusBarLayouted()) {
            return mSystemWindowRect.bottom - mSystemUIRect.bottom;
        }
        return 0;
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