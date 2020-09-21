package cn.simonlee.widgetdemo;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //开启侧滑返回
        mSwipeBackHelper = new SwipeBack(this);
        //占用状态栏位置进行布局
        setStatusBarLayouted(true);
        //状态栏背景色：透明
        setStatusBarColor(Color.TRANSPARENT);
        //占用导航栏位置进行布局
        setNavigationBarLayouted(true);
        //导航栏背景色：透明
        setNavigationBarColor(Color.TRANSPARENT);
        //导航栏暗色图文样式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setNavigationBarDarkStyle(true);
        }
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
        hideInputSoft();//隐藏软键盘
        if (!moveTaskToBack(false)) {//APP切入后台
            super.onBackPressed();//非根Activity，切入后台失败，执行返回操作
        }
    }

}
