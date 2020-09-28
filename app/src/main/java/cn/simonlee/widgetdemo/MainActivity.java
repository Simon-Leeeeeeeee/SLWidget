package cn.simonlee.widgetdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.simonlee.widget.lib.widget.titlebar.TitleBar;

import cn.simonlee.widgetdemo.autowraplayout.AutoWrapActivity;
import cn.simonlee.widgetdemo.badge.BadgeActivity;
import cn.simonlee.widgetdemo.scrollpicker.ScrollPickerActivity;
import cn.simonlee.widgetdemo.slidingtablayout.SlidingTabActivity;
import cn.simonlee.widgetdemo.swipeback.SwipeBackActivity;
import cn.simonlee.widgetdemo.swiperefreshlayout.SwipeRefreshActivity;

/**
 * 主界面
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-07-18
 */
public class MainActivity extends CommonActivity implements View.OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        TitleBar titleBar = getTitleBar();
        //设置标题
        titleBar.setTitle(R.string.app_name);
        //取消导航按钮
        titleBar.setNaviVisibility(View.GONE);

        //设置监听
        findViewById(R.id.main_layout_badgeview).setOnClickListener(this);
        findViewById(R.id.main_layout_scrollpicker).setOnClickListener(this);
        findViewById(R.id.main_layout_autowraplayout).setOnClickListener(this);
        findViewById(R.id.main_layout_swipeback).setOnClickListener(this);
        findViewById(R.id.main_layout_swiperefreshlayout).setOnClickListener(this);
        findViewById(R.id.main_layout_slidingtablayout).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_layout_badgeview: {
                startActivity(new Intent(this, BadgeActivity.class));
                break;
            }
            case R.id.main_layout_scrollpicker: {
                startActivity(new Intent(this, ScrollPickerActivity.class));
                break;
            }
            case R.id.main_layout_autowraplayout: {
                startActivity(new Intent(this, AutoWrapActivity.class));
                break;
            }
            case R.id.main_layout_swipeback: {
                startActivity(new Intent(this, SwipeBackActivity.class));
                break;
            }
            case R.id.main_layout_swiperefreshlayout: {
                startActivity(new Intent(this, SwipeRefreshActivity.class));
                break;
            }
            case R.id.main_layout_slidingtablayout: {
                startActivity(new Intent(this, SlidingTabActivity.class));
                break;
            }
            default: {
                break;
            }
        }
    }

}
