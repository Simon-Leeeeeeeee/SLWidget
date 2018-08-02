package cn.simonlee.widgetdemo.swipeback;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Switch;

import java.util.Random;

import cn.simonlee.widgetdemo.BaseActivity;
import cn.simonlee.widgetdemo.MainActivity;
import cn.simonlee.widgetdemo.R;

/**
 * 侧滑返回页面
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-07-27
 */
public class SwipeBackActivity extends BaseActivity implements View.OnClickListener {

    public int mIndex;
    private int mRandomColor;
    private Switch mSwitch_SupportLandscape;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mRandomColor = savedInstanceState.getInt("randomColor", 0);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipeback);

        mIndex = getIntent().getIntExtra("index", 1);

        Toolbar toolbar = getToolbar();
        if (toolbar != null) {
            toolbar.setTitle(getText(R.string.swipeback) + "-" + mIndex);
            toolbar.setNavigationOnClickListener(this);
        }
        mSwitch_SupportLandscape = findViewById(R.id.swipeback_swicth_support_landscape);
        boolean supportLandscape = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("landscape_support", false);
        mSwitch_SupportLandscape.setChecked(supportLandscape);
        mSwitch_SupportLandscape.setOnClickListener(this);
        findViewById(R.id.swipeback_rootlayout).setBackgroundColor(mRandomColor);
        findViewById(R.id.swipeback_btn_next).setOnClickListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (outState != null) {
            outState.putInt("randomColor", mRandomColor);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void supportSwipeBack(int color) {
        if (mRandomColor == 0) {
            Random random = new Random();
            mRandomColor = Color.argb(255, random.nextInt(255), random.nextInt(255), random.nextInt(255));
        }
        super.supportSwipeBack(mRandomColor);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.base_toolbar_navigation: {
                onBackPressed();
                break;
            }
            case R.id.swipeback_btn_next: {
                Intent intent = new Intent(this, SwipeBackActivity.class);
                intent.putExtra("index", mIndex + 1);
                startActivity(intent);
                break;
            }
            case R.id.swipeback_swicth_support_landscape: {
                boolean isChecked = mSwitch_SupportLandscape.isChecked();
                PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("landscape_support", isChecked).apply();
                startActivity(new Intent(this, MainActivity.class));
                break;
            }
        }
    }

}
