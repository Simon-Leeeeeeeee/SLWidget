package cn.simonlee.widgetdemo.swiperefreshlayout;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import cn.simonlee.widget.swiperefreshlayout.SwipeRefreshLayout;
import cn.simonlee.widgetdemo.BaseFragment;
import cn.simonlee.widgetdemo.R;
import cn.simonlee.widgetdemo.ToastHelper;

/**
 * 下拉刷新的Fragment
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2017-08-24
 */
@SuppressWarnings("InflateParams")
public class SwipeRefreshFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private Animation mRotateAnimation;

    private View mHeaderRefreshView;
    private View mFooterRefreshView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private AlertDialog.Builder mAlertDialogBuilder;

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
            mSwipeRefreshLayout = findViewById(R.id.swiperefreshfragment_swiperefresh);
            mSwipeRefreshLayout.setOnRefreshListener(this);
            mHeaderRefreshView = mSwipeRefreshLayout.getHeaderRefreshView();
            mFooterRefreshView = mSwipeRefreshLayout.getFooterRefreshView();
        }

        ((Switch) findViewById(R.id.swiperefresh_pullup_enable)).setOnCheckedChangeListener(this);
        ((Switch) findViewById(R.id.swiperefresh_pullup_folded)).setOnCheckedChangeListener(this);
        ((Switch) findViewById(R.id.swiperefresh_pullup_refreshable)).setOnCheckedChangeListener(this);
        ((Switch) findViewById(R.id.swiperefresh_pulldown_enable)).setOnCheckedChangeListener(this);
        ((Switch) findViewById(R.id.swiperefresh_pulldown_folded)).setOnCheckedChangeListener(this);
        ((Switch) findViewById(R.id.swiperefresh_pulldown_refreshable)).setOnCheckedChangeListener(this);

        findViewById(R.id.swiperefresh_pullup_refresh).setOnClickListener(this);
        findViewById(R.id.swiperefresh_pulldown_refresh).setOnClickListener(this);
        findViewById(R.id.swiperefresh_refresh_complete).setOnClickListener(this);

        if (mRotateAnimation == null) {
            mRotateAnimation = new RotateAnimation(0F, 360F, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5F);
            mRotateAnimation.setRepeatCount(-1);
            mRotateAnimation.setDuration(1000);
            mRotateAnimation.setInterpolator(new LinearInterpolator());
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSwipeRefreshLayout != null &&
                (mSwipeRefreshLayout.getRefreshState() == SwipeRefreshLayout.STATE_REFRESHING_HEADER || mSwipeRefreshLayout.getRefreshState() == SwipeRefreshLayout.STATE_REFRESHING_FOOTER)) {
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.swiperefresh_pullup_refresh: {
                mSwipeRefreshLayout.requestFooterRefresh();
                break;
            }
            case R.id.swiperefresh_pulldown_refresh: {
                mSwipeRefreshLayout.requestHeaderRefresh();
                break;
            }
            case R.id.swiperefresh_refresh_complete: {
                mSwipeRefreshLayout.notifyRefreshComplete();
                break;
            }
            default: {
                break;
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.swiperefresh_pullup_enable: {
                mSwipeRefreshLayout.setFooterEnabled(isChecked);
                break;
            }
            case R.id.swiperefresh_pulldown_enable: {
                mSwipeRefreshLayout.setHeaderEnabled(isChecked);
                break;
            }
            case R.id.swiperefresh_pullup_folded: {
                mSwipeRefreshLayout.setFooterRefreshFolded(isChecked);
                break;
            }
            case R.id.swiperefresh_pulldown_folded: {
                mSwipeRefreshLayout.setHeaderRefreshFolded(isChecked);
                break;
            }
            case R.id.swiperefresh_pullup_refreshable: {
                mSwipeRefreshLayout.setFooterRefreshable(isChecked);
                View loadingImage = mFooterRefreshView.findViewById(R.id.refresh_image);
                TextView loadingText = mFooterRefreshView.findViewById(R.id.refresh_text);
                loadingText.setText(isChecked ? R.string.pullup_refresh : R.string.pullup_norefresh);
                loadingImage.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                break;
            }
            case R.id.swiperefresh_pulldown_refreshable: {
                mSwipeRefreshLayout.setHeaderRefreshable(isChecked);
                View loadingImage = mHeaderRefreshView.findViewById(R.id.refresh_image);
                TextView loadingText = mHeaderRefreshView.findViewById(R.id.refresh_text);
                loadingText.setText(isChecked ? R.string.pulldown_refresh : R.string.pulldown_norefresh);
                loadingImage.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                break;
            }
            default: {
                break;
            }
        }
    }

    @Override
    public void onRefresh(boolean isStateChange, int state, float offsetY, View refreshView, SwipeRefreshLayout parent) {
        if (refreshView == null) {
            return;
        }
        if (refreshView == mHeaderRefreshView && !parent.isHeaderRefreshable()) {
            return;
        }
        if (refreshView == mFooterRefreshView && !parent.isFooterRefreshable()) {
            return;
        }
        View loadingImage = refreshView.findViewById(R.id.refresh_image);
        TextView loadingText = refreshView.findViewById(R.id.refresh_text);
        switch (state) {
            case SwipeRefreshLayout.STATE_CLOSE: {
                if (isStateChange) {
                    loadingImage.clearAnimation();
                }
                break;
            }
            case SwipeRefreshLayout.STATE_ENABLE: {
                if (isStateChange) {
                    loadingText.setText(refreshView == mHeaderRefreshView ? R.string.pulldown_refresh : R.string.pullup_refresh);
                }
                loadingImage.setRotation(-360 * Math.abs(offsetY) / refreshView.getHeight());
                break;
            }
            case SwipeRefreshLayout.STATE_READY: {
                if (isStateChange) {
                    loadingText.setText(R.string.release_refresh);
                }
                loadingImage.setRotation(-360 * Math.abs(offsetY) / refreshView.getHeight());
                break;
            }
            case SwipeRefreshLayout.STATE_REFRESHING_HEADER: {
                if (isStateChange && refreshView == mHeaderRefreshView) {
                    if (parent.isHeaderRefreshFolded()) {
                        showAlertDialog("正在加载中（顶部）");
                    } else {
                        loadingText.setText("正在加载...");
                        loadingImage.startAnimation(mRotateAnimation);
                    }
                }
                break;
            }
            case SwipeRefreshLayout.STATE_REFRESHING_FOOTER: {
                if (isStateChange && refreshView == mFooterRefreshView) {
                    if (parent.isFooterRefreshFolded()) {
                        showAlertDialog("正在加载中（底部）");
                    } else {
                        loadingText.setText("正在加载...");
                        loadingImage.startAnimation(mRotateAnimation);
                    }
                }
                break;
            }
            case SwipeRefreshLayout.STATE_REFRESH_COMPLETE: {
                if (isStateChange) {
                    loadingText.setText("加载完成");
                    loadingImage.clearAnimation();
                    loadingImage.setRotation(0);
                }
                break;
            }
        }

        View childView = parent.getChildView();
        if (childView != null && childView instanceof ScrollView) {
            if (state == SwipeRefreshLayout.STATE_REFRESHING_HEADER && Math.abs(offsetY) == refreshView.getHeight()) {
                int diff = refreshView.getHeight() - childView.getPaddingBottom();
                if (diff > 0) {
                    childView.setPadding(0, 0, 0, refreshView.getHeight());
                }
            } else if (state == SwipeRefreshLayout.STATE_REFRESHING_FOOTER && Math.abs(offsetY) == refreshView.getHeight()) {
                int diff = refreshView.getHeight() - childView.getPaddingTop();
                if (diff > 0) {
                    childView.setPadding(0, diff, 0, 0);
                    childView.scrollBy(0, diff);
                }
            } else if (childView.getPaddingTop() != 0 || childView.getPaddingBottom() != 0) {
                int diff = -childView.getPaddingTop();
                childView.setPadding(0, 0, 0, 0);
                childView.scrollBy(0, diff);
            }
        }
    }

    private void showAlertDialog(String title) {
        if (mAlertDialogBuilder == null) {
            mAlertDialogBuilder = new AlertDialog.Builder(requireContext()).setPositiveButton("加载完毕", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    mSwipeRefreshLayout.notifyRefreshComplete();
                }
            });
        }
        mAlertDialogBuilder.setMessage(title).show();
    }

}
