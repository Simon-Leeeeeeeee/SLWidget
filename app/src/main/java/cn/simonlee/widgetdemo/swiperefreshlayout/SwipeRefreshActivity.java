package cn.simonlee.widgetdemo.swiperefreshlayout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import cn.simonlee.widgetdemo.BaseActivity;
import cn.simonlee.widgetdemo.BaseFragment;
import cn.simonlee.widgetdemo.R;

/**
 * 刷新页面
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-08-16
 */
public class SwipeRefreshActivity extends BaseActivity implements View.OnClickListener, ViewPager.OnPageChangeListener {

    private ViewPager mViewPager;
    private View mCurrentLable;
    private View mLable_Button, mLable_ScrollView, mLable_ViewPager;
    private SwipeRefreshPagerAdapter mSwipeRefreshPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swiperefresh);
        initView();
    }

    private void initView() {
        Toolbar toolbar = getToolbar();
        if (toolbar != null) {
            toolbar.setTitle(R.string.swiperefreshlayout);
            toolbar.setNavigationOnClickListener(this);
        }

        mViewPager = findViewById(R.id.swiperefresh_viewpager);

        mSwipeRefreshPagerAdapter = new SwipeRefreshPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSwipeRefreshPagerAdapter);
        mViewPager.addOnPageChangeListener(this);

        mLable_Button = findViewById(R.id.swiperefreshlayout_table_button);
        mLable_ScrollView = findViewById(R.id.swiperefreshlayout_table_scrollview);
        mLable_ViewPager = findViewById(R.id.swiperefreshlayout_table_viewpager);

        mLable_Button.setOnClickListener(this);
        mLable_ScrollView.setOnClickListener(this);
        mLable_ViewPager.setOnClickListener(this);

        mCurrentLable = mLable_Button;
        mCurrentLable.setSelected(true);
    }

    @Override
    public void onEnterAnimationComplete() {
        Fragment curFragment = mSwipeRefreshPagerAdapter.getCurrentFragment();
        if (curFragment instanceof BaseFragment) {
            ((BaseFragment) curFragment).onEnterAnimationComplete();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.base_toolbar_navigation: {
                onBackPressed();
                break;
            }
            case R.id.swiperefreshlayout_table_button: {
                mViewPager.setCurrentItem(0);
                break;
            }
            case R.id.swiperefreshlayout_table_scrollview: {
                mViewPager.setCurrentItem(1);
                break;
            }
            case R.id.swiperefreshlayout_table_viewpager: {
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
