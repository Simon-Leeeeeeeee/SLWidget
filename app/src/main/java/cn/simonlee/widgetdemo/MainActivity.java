package cn.simonlee.widgetdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import cn.simonlee.widgetdemo.autowraplayout.AutoWrapActivity;
import cn.simonlee.widgetdemo.badge.BadgeActivity;
import cn.simonlee.widgetdemo.scrollpicker.ScrollPickerActivity;
import cn.simonlee.widgetdemo.swipeback.SwipeBackActivity;

/**
 * 主界面
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-07-18
 */
public class MainActivity extends BaseActivity implements View.OnClickListener, Toolbar.OnMenuItemClickListener {

    private BroadcastReceiver mBroadcastReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        supportLandscape();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = getToolbar();
        if (toolbar != null) {
            toolbar.setNavigationIcon(null);
//            toolbar.inflateMenu(R.menu.menu_about);
//            toolbar.setOnMenuItemClickListener(this);
        }
        findViewById(R.id.main_layout_badgeview).setOnClickListener(this);
        findViewById(R.id.main_layout_scrollpicker).setOnClickListener(this);
        findViewById(R.id.main_layout_autowraplayout).setOnClickListener(this);
        findViewById(R.id.main_layout_swipeback).setOnClickListener(this);
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    startActivity(new Intent(this, SwipeBackActivity.class));
                } else {
                    ToastHelper.showToast(this, R.string.swipeback_nonsupport, ToastHelper.LENGTH_SHORT);
                }
                break;
            }
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
//        AboutDialog aboutDialog = new AboutDialog();
//        aboutDialog.show(getFragmentManager(), "AboutDialog");
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(getBroadcastReceiver(), new IntentFilter("cn.simonlee.widgetdemo.SCREEN_ORIENTATION_SUPPORT_LANDSCAPE"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }

    private void supportLandscape() {
        boolean supportLandscape = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("landscape_support", false);
        if (supportLandscape) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

    private BroadcastReceiver getBroadcastReceiver() {
        if (mBroadcastReceiver == null) {
            mBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    boolean landscape_support = intent.getBooleanExtra("landscape_support", false);
                    PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putBoolean("landscape_support", landscape_support).apply();
                    setRequestedOrientation(landscape_support ? ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            };
        }
        return mBroadcastReceiver;
    }

}
