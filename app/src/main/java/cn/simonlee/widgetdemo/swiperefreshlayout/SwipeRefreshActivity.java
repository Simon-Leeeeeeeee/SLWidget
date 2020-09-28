package cn.simonlee.widgetdemo.swiperefreshlayout;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.simonlee.widget.lib.widget.titlebar.TitleBar;

import androidx.viewpager.widget.ViewPager;
import cn.simonlee.widgetdemo.CommonActivity;
import cn.simonlee.widgetdemo.R;

/**
 * 刷新页面
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-08-16
 */
public class SwipeRefreshActivity extends CommonActivity implements View.OnClickListener, ViewPager.OnPageChangeListener {

    private ViewPager mViewPager;
    private View mCurrentLable;
    private View mLable_Button, mLable_ScrollView, mLable_ViewPager;
    private ViewGroup mLayout_Lable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swiperefresh);
        initView();
    }

    private void initView() {
        TitleBar titleBar = getTitleBar();
        titleBar.setTitle(R.string.swiperefreshlayout);
        titleBar.setBackgroundColor(Color.TRANSPARENT);

        mViewPager = findViewById(R.id.swiperefresh_viewpager);

        mViewPager.setAdapter(new SwipeRefreshPagerAdapter(getSupportFragmentManager()));
        mViewPager.addOnPageChangeListener(this);

        mLayout_Lable = findViewById(R.id.swiperefreshlayout_layout_lable);

        mLable_Button = findViewById(R.id.swiperefreshlayout_lable_button);
        mLable_ScrollView = findViewById(R.id.swiperefreshlayout_lable_scrollview);
        mLable_ViewPager = findViewById(R.id.swiperefreshlayout_lable_viewpager);

        mLable_Button.setOnClickListener(this);
        mLable_ScrollView.setOnClickListener(this);
        mLable_ViewPager.setOnClickListener(this);

        mCurrentLable = mLable_Button;
        mCurrentLable.setSelected(true);
    }

    @Override
    protected void resizeContentParent(int safeLeft, int safeTop, int safeRight, int safeBottom) {
        super.resizeContentParent(safeLeft, 0, safeRight, 0);
        mLayout_Lable.setPadding(0, 0, 0, safeBottom);
        FragmentDispatcher.dispatchContentParentResized(getSupportFragmentManager(), safeLeft, safeTop, safeRight, safeBottom);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.swiperefreshlayout_lable_button: {
                mViewPager.setCurrentItem(0);
                break;
            }
            case R.id.swiperefreshlayout_lable_scrollview: {
                mViewPager.setCurrentItem(1);
                break;
            }
            case R.id.swiperefreshlayout_lable_viewpager: {
                mViewPager.setCurrentItem(2);
                break;
            }
        }
    }

    @Override
    public void onPageSelected(int position) {
        View lable = position == 0 ? mLable_Button : (position == 1 ? mLable_ScrollView : mLable_ViewPager);
        if (lable != mCurrentLable) {
            mCurrentLable.setSelected(false);
            mCurrentLable = lable;
        }
        mCurrentLable.setSelected(true);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

}
