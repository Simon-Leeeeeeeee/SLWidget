package cn.simonlee.widgetdemo;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.simonlee.widget.lib.activity.BaseActivity;
import com.simonlee.widget.lib.widget.titlebar.TitleBar;

import cn.simonlee.widgetdemo.swipeback.SwipeBack;

/**
 * 公共Activity
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2019-11-09
 */
public abstract class CommonActivity extends BaseActivity {

    /**
     * 侧滑工具
     */
    private SwipeBack mSwipeBackHelper;

    /**
     * ContentLayout的背景View，用于遮挡输入法弹出时可能出现的黑色背景
     */
    private View mContentBackgroundView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //开启侧滑返回
        mSwipeBackHelper = new SwipeBack(this);
        //占用状态栏位置进行布局
        setStatusBarLayouted(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            //支持异形屏
            supportNotchDisplay(true);
        }
        //设置状态栏透明
        setStatusBarColor(Color.TRANSPARENT);
        //标题栏
        initTitleBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //水印
        getWaterMark().setText(getString(R.string.app_name));
    }

    /**
     * 初始化TitleBar
     */
    public void initTitleBar() {
        //构造TitleBar实例
        TitleBar titleBar = new TitleBar(this);
        //设置返回键监听
        titleBar.setNaviOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //设置TitleBar
        setTitleBar(titleBar);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (mSwipeBackHelper != null && mSwipeBackHelper.dispatchTouchEvent(event)) {
            //触摸事件被消耗，直接返回
            return true;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mSwipeBackHelper != null) {
            mSwipeBackHelper.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        hideInputSoft();//隐藏输入法
        if (!moveTaskToBack(false)) {//APP切入后台
            super.onBackPressed();//非根Activity，切入后台失败，执行返回操作
        }
    }

    @Override
    protected void fixInputSoftAdjustResize(int inputSoftHeight) {
        super.fixInputSoftAdjustResize(inputSoftHeight);
        //修复输入法弹出时可能出现的黑色背景
        View contentBackgroundView = getContentBackgroundView();
        /*调整contentBackgroundView的Y轴偏移量，用于覆盖不可见区域出现的黑色
          不可见区域：当输入法弹出时所在的区域，在弹出动画结束前，该区域显示为window背景色
          黑色：当设置{@code <item name="android:windowBackground">@android:color/transparent</item>}后，window背景色即为黑色
         */
        contentBackgroundView.setTranslationY(contentBackgroundView.getHeight() - inputSoftHeight);
    }

    /**
     * ContentLayout的背景View，用于遮挡输入法弹出时可能出现的黑色背景
     */
    public View getContentBackgroundView() {
        if (mContentBackgroundView == null) {
            ViewGroup contentLayoutParent = (ViewGroup) getContentLayout().getParent();
            //Window背景View
            mContentBackgroundView = new View(this);
            //设置默认背景色
            mContentBackgroundView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorWindowBackground));
            //插入到布局中
            contentLayoutParent.addView(mContentBackgroundView, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        return mContentBackgroundView;
    }

}
