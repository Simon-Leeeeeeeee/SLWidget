package cn.simonlee.widgetdemo.swiperefreshlayout;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ScrollView;
import android.widget.TextView;

import cn.simonlee.widget.swiperefreshlayout.SwipeRefreshLayout;
import cn.simonlee.widgetdemo.BaseFragment;
import cn.simonlee.widgetdemo.R;

/**
 * 下拉刷新的Fragment
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2017-08-24
 */
@SuppressWarnings("InflateParams")
public class SwipeRefreshFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, Animation.AnimationListener {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Animation mRotateAnimation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            setContentView(bundle.getInt("layoutResID", View.NO_ID));
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getUserVisibleHint()) {
            initView();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint() && isAdded()) {
            initView();
        }
    }

    @Override
    public void onEnterAnimationComplete() {
        if (isVisible() && mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.requestHeaderRefresh();
        }
    }

    private void initView() {
        if (mSwipeRefreshLayout == null) {
            mSwipeRefreshLayout = findViewById(R.id.swiperefreshlayout_swiperefreshlayout);
            if (mSwipeRefreshLayout != null) {
                mSwipeRefreshLayout.setHeaderRefreshView(getLayoutInflater().inflate(R.layout.layout_refresh_header, null), this);
                mSwipeRefreshLayout.setFooterRefreshView(getLayoutInflater().inflate(R.layout.layout_refresh_footer, null), this);
            }
        }

        if (mRotateAnimation == null) {
            mRotateAnimation = new RotateAnimation(0F, 360F, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5F);
            mRotateAnimation.setRepeatCount(1);
            mRotateAnimation.setDuration(1000);
            mRotateAnimation.setInterpolator(new LinearInterpolator());
            mRotateAnimation.setAnimationListener(this);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.getRefreshState() == SwipeRefreshLayout.STATE_REFRESH) {
            View curRefreshView = mSwipeRefreshLayout.getCurRefreshView();
            if (curRefreshView != null) {
                View loadingImage = curRefreshView.findViewById(R.id.refresh_image);
                if (loadingImage != null && loadingImage.getAnimation() == null) {
                    loadingImage.startAnimation(mRotateAnimation);
                }
            }
        }
    }

    @Override
    public void onRefresh(SwipeRefreshLayout parent, View refreshView, int state, float offsetY) {
        if (refreshView == null) return;
        View loadingImage = refreshView.findViewById(R.id.refresh_image);
        TextView loadingText = refreshView.findViewById(R.id.refresh_text);
        switch (state) {
            case SwipeRefreshLayout.STATE_CLOSE: {
                loadingImage.clearAnimation();
                break;
            }
            case SwipeRefreshLayout.STATE_ENABLE: {
                if (offsetY < 0) {
                    loadingText.setText("下拉刷新");
                } else if (offsetY > 0) {
                    loadingText.setText("上拉刷新");
                }
                loadingImage.setRotation(-360 * Math.abs(offsetY) / refreshView.getHeight());
                break;
            }
            case SwipeRefreshLayout.STATE_READY: {
                loadingText.setText("释放刷新");
                loadingImage.setRotation(-360 * Math.abs(offsetY) / refreshView.getHeight());
                break;
            }
            case SwipeRefreshLayout.STATE_REFRESH: {
                loadingText.setText("正在加载...");
                loadingImage.startAnimation(mRotateAnimation);
                break;
            }
            case SwipeRefreshLayout.STATE_REFRESH_SUCCESS: {
                loadingText.setText("加载完成");
                loadingImage.setRotation(-360 * Math.abs(offsetY) / refreshView.getHeight());
                break;
            }
        }

        View childView = parent.getChildView();
        if (childView != null && childView instanceof ScrollView) {
            int padding = state == SwipeRefreshLayout.STATE_REFRESH ? refreshView.getHeight() : 0;
            if (offsetY < 0) {
                childView.setPadding(0, 0, 0, padding);
            } else if (offsetY > 0) {
                childView.setPadding(0, padding, 0, 0);
                childView.scrollBy(0, padding);
            } else {
                childView.setPadding(0, 0, 0, 0);
            }
        }
    }

    @Override
    public void onAnimationStart(Animation animation) {
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.notifyRefreshComplete();
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
