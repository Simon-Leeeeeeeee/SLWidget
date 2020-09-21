package cn.simonlee.widgetdemo.swipeback;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.simonlee.widget.lib.widget.titlebar.TitleBar;

import java.util.Random;

import cn.simonlee.widgetdemo.CommonActivity;
import cn.simonlee.widgetdemo.R;

/**
 * 侧滑返回页面
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-07-27
 */
public class SwipeBackActivity extends CommonActivity implements View.OnClickListener {

    /**
     * 页面编号
     */
    public int mIndex;

    /**
     * 随机颜色值
     */
    private int mRandomColor;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (outState != null) {
            outState.putInt("randomColor", mRandomColor);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //获取随机颜色值
        mRandomColor = getRandomColor(savedInstanceState);
        setContentView(R.layout.activity_swipeback);
        initView();
    }

    private void initView() {
        //页面编号
        mIndex = getIntent().getIntExtra("index", 1);

        //设置标题
        TitleBar titleBar = getTitleBar();
        titleBar.setTitle(getText(R.string.swipeback) + "-" + mIndex);
        //更改窗口背景色
        getWindow().setBackgroundDrawable(new ColorDrawable(mRandomColor));

        findViewById(R.id.swipeback_btn_next).setOnClickListener(this);
    }

    private int getRandomColor(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            return savedInstanceState.getInt("randomColor", 0);
        } else {
            Random random = new Random();
            return Color.argb(255, random.nextInt(255), random.nextInt(255), random.nextInt(255));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.swipeback_btn_next: {
                Intent intent = new Intent(this, SwipeBackActivity.class);
                intent.putExtra("index", mIndex + 1);
                startActivity(intent);
                break;
            }
            default: {
                break;
            }
        }
    }

}
