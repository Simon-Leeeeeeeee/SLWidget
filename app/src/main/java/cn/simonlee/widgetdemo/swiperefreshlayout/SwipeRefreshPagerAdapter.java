package cn.simonlee.widgetdemo.swiperefreshlayout;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.PagerAdapter;
import cn.simonlee.widgetdemo.R;

/**
 * 刷新页面的ViewPager适配器
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-08-24
 */
@SuppressWarnings("CommitTransaction")
public class SwipeRefreshPagerAdapter extends PagerAdapter {

    private final FragmentManager mFragmentManager;
    private FragmentTransaction mCurTransaction = null;
    private Fragment mCurrentFragment = null;

    SwipeRefreshPagerAdapter(FragmentManager fragmentManager) {
        this.mFragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        if (this.mCurTransaction == null) {
            this.mCurTransaction = this.mFragmentManager.beginTransaction();
        }

        String name = makeFragmentName(container.getId(), position);
        Fragment fragment = this.mFragmentManager.findFragmentByTag(name);
        if (fragment != null) {
            this.mCurTransaction.attach(fragment);
        } else {
            fragment = this.getItem(position);
            this.mCurTransaction.add(container.getId(), fragment, name);
        }

        if (fragment != this.mCurrentFragment) {
            fragment.setMenuVisibility(false);
            fragment.setUserVisibleHint(false);
        }
        return fragment;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @Nullable Object object) {
        if (this.mCurTransaction == null) {
            this.mCurTransaction = this.mFragmentManager.beginTransaction();
        }
        this.mCurTransaction.detach((Fragment) object);
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @Nullable Object object) {
        Fragment fragment = (Fragment) object;

        if (fragment != this.mCurrentFragment) {
            if (this.mCurrentFragment != null) {
                this.mCurrentFragment.setMenuVisibility(false);
                this.mCurrentFragment.setUserVisibleHint(false);
            }

            if (fragment != null) {
                fragment.setMenuVisibility(true);
                fragment.setUserVisibleHint(true);
            }
            this.mCurrentFragment = fragment;
        }
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @Nullable Object object) {
        return object != null && ((Fragment) object).getView() == view;
    }

    @Override
    public void startUpdate(@NonNull ViewGroup container) {
        if (container.getId() == -1) {
            throw new IllegalStateException("ViewPager with adapter " + this + " requires a view id");
        }
    }

    @Override
    public void finishUpdate(@NonNull ViewGroup container) {
        if (this.mCurTransaction != null) {
            this.mCurTransaction.commitNowAllowingStateLoss();
            this.mCurTransaction = null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    private Fragment getItem(int position) {
        int layoutResID = position == 0 ? R.layout.fragment_swiperefresh_button :
                (position == 1 ? R.layout.fragment_swiperefresh_scrollview : R.layout.fragment_swiperefresh_viewpager);

        Bundle bundle = new Bundle();
        bundle.putInt("layoutResID", layoutResID);

        Fragment fragment = new SwipeRefreshFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private String makeFragmentName(int viewId, long id) {
        return "android:switcher:" + viewId + ":" + id;
    }

    public Fragment getCurrentFragment() {
        return mCurrentFragment;
    }

}
