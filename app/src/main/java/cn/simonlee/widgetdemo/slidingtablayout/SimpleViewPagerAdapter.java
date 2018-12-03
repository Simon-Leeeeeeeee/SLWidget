package cn.simonlee.widgetdemo.slidingtablayout;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.LinkedList;

import cn.simonlee.widgetdemo.R;

/**
 * 简单的ViewePager适配器
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-08-16
 */
public class SimpleViewPagerAdapter extends PagerAdapter {

    private int mCount;

    private LinkedList<TextView> mViewCache = new LinkedList<>();

    public SimpleViewPagerAdapter(int count) {
        this.mCount = count;
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        TextView contentView;
        if (mViewCache.size() > 0) {
            contentView = mViewCache.removeFirst();
        } else {
            contentView = new TextView(container.getContext());
            contentView.setGravity(Gravity.CENTER);
            contentView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36);
        }
        String content = container.getContext().getResources().getString(R.string.number_tab, position);
        contentView.setText(content);
        container.addView(contentView);
        return contentView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        TextView contentView = (TextView) object;
        container.removeView(contentView);
        this.mViewCache.add(contentView);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

}
