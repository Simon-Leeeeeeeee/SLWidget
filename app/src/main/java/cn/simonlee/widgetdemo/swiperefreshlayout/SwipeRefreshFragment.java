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
import cn.simonlee.widgetdemo.fragment.BaseFragment;
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
public class SwipeRefreshFragment extends BaseFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, SwipeRefreshLayout.OnRefreshListener {

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
        mSwipeRefreshLayout = findViewById(R.id.swiperefreshfragment_swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        ((Switch) findViewById(R.id.swiperefresh_pullup_enable)).setOnCheckedChangeListener(this);
        ((Switch) findViewById(R.id.swiperefresh_pullup_folded)).setOnCheckedChangeListener(this);
        ((Switch) findViewById(R.id.swiperefresh_pullup_refreshable)).setOnCheckedChangeListener(this);
        ((Switch) findViewById(R.id.swiperefresh_pulldown_enable)).setOnCheckedChangeListener(this);
        ((Switch) findViewById(R.id.swiperefresh_pulldown_folded)).setOnCheckedChangeListener(this);
        ((Switch) findViewById(R.id.swiperefresh_pulldown_refreshable)).setOnCheckedChangeListener(this);

        findViewById(R.id.swiperefresh_pullup_refresh).setOnClickListener(this);
        findViewById(R.id.swiperefresh_pulldown_refresh).setOnClickListener(this);
        findViewById(R.id.swiperefresh_refresh_complete).setOnClickListener(this);
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
            case R.id.swiperefresh_pulldown_enable: {
                mSwipeRefreshLayout.setHeaderEnabled(isChecked);
                break;
            }
            case R.id.swiperefresh_pullup_enable: {
                mSwipeRefreshLayout.setFooterEnabled(isChecked);
                break;
            }
            case R.id.swiperefresh_pulldown_folded: {
                mSwipeRefreshLayout.setHeaderRefreshFolded(isChecked);
                break;
            }
            case R.id.swiperefresh_pullup_folded: {
                mSwipeRefreshLayout.setFooterRefreshFolded(isChecked);
                break;
            }
            case R.id.swiperefresh_pulldown_refreshable: {
                setHeaderRefreshable(mSwipeRefreshLayout.getHeaderRefreshView(), isChecked);
                break;
            }
            case R.id.swiperefresh_pullup_refreshable: {
                setFooterRefreshable(mSwipeRefreshLayout.getFooterRefreshView(), isChecked);
                break;
            }
            default: {
                break;
            }
        }
    }

    private void setHeaderRefreshable(View refreshView, boolean enable) {
        mSwipeRefreshLayout.setHeaderRefreshable(enable);
        View loadingImage = refreshView.findViewById(R.id.refresh_image);
        TextView loadingText = refreshView.findViewById(R.id.refresh_text);
        loadingText.setText(enable ? R.string.pulldown_refresh : R.string.pulldown_norefresh);
        loadingImage.setVisibility(enable ? View.VISIBLE : View.GONE);
    }

    private void setFooterRefreshable(View refreshView, boolean enable) {
        mSwipeRefreshLayout.setFooterRefreshable(enable);
        View loadingImage = refreshView.findViewById(R.id.refresh_image);
        TextView loadingText = refreshView.findViewById(R.id.refresh_text);
        loadingText.setText(enable ? R.string.pullup_refresh : R.string.pullup_norefresh);
        loadingImage.setVisibility(enable ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onRefresh(SwipeRefreshLayout parent, View refreshView, float offsetY, int state, boolean isChanged, boolean isFinally) {
        if (refreshView == parent.getHeaderRefreshView() && !parent.isHeaderRefreshable()) {
            return;
        }
        if (refreshView == parent.getFooterRefreshView() && !parent.isFooterRefreshable()) {
            return;
        }
        View loadingImage = refreshView.findViewById(R.id.refresh_image);
        TextView loadingText = refreshView.findViewById(R.id.refresh_text);
        switch (state) {
            case SwipeRefreshLayout.STATE_CLOSE: {
                if (isChanged) {
                    loadingImage.clearAnimation();
                }
                break;
            }
            case SwipeRefreshLayout.STATE_OPEN: {
                if (isChanged) {
                    loadingText.setText(refreshView == parent.getHeaderRefreshView() ? R.string.pulldown_refresh : R.string.pullup_refresh);
                }
                loadingImage.setRotation(-360 * Math.abs(offsetY) / refreshView.getHeight());
                break;
            }
            case SwipeRefreshLayout.STATE_READY: {
                if (isChanged) {
                    loadingText.setText(R.string.release_refresh);
                }
                loadingImage.setRotation(-360 * Math.abs(offsetY) / refreshView.getHeight());
                break;
            }
            case SwipeRefreshLayout.STATE_REFRESHING: {
                if (refreshView == parent.getHeaderRefreshView()) {
                    if (isChanged && !parent.isHeaderRefreshFolded()) {
                        loadingText.setText("正在加载...");
                        loadingImage.startAnimation(mRotateAnimation);
                    }
                    if (isFinally && parent.isHeaderRefreshFolded()) {
                        showAlertDialog("正在加载中（顶部）");
                    }
                } else if (refreshView == parent.getFooterRefreshView()) {
                    if (isChanged && !parent.isFooterRefreshFolded()) {
                        loadingText.setText("正在加载...");
                        loadingImage.startAnimation(mRotateAnimation);
                    }
                    if (isFinally && parent.isFooterRefreshFolded()) {
                        showAlertDialog("正在加载中（底部）");
                    }
                }
                break;
            }
            case SwipeRefreshLayout.STATE_REFRESH_COMPLETE: {
                if (isChanged) {
                    loadingText.setText("加载完成");
                    loadingImage.clearAnimation();
                    loadingImage.setRotation(0);
                }
                break;
            }
        }
        View childView = parent.getChildView();
        if (childView instanceof ScrollView) {
            if (state == SwipeRefreshLayout.STATE_REFRESHING && Math.abs(offsetY) == refreshView.getHeight()) {
                if (refreshView == parent.getHeaderRefreshView()) {
                    int diff = refreshView.getHeight() - childView.getPaddingBottom();
                    if (diff > 0) {
                        childView.setPadding(0, 0, 0, refreshView.getHeight());
                    }
                } else {
                    int diff = refreshView.getHeight() - childView.getPaddingTop();
                    if (diff > 0) {
                        childView.setPadding(0, diff, 0, 0);
                        childView.scrollBy(0, diff);
                    }
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
