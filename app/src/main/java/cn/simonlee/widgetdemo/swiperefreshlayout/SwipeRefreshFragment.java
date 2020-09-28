package cn.simonlee.widgetdemo.swiperefreshlayout;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.simonlee.widget.lib.fragment.BaseFragment;

import cn.simonlee.widget.swiperefreshlayout.SwipeRefreshLayout;
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
public class SwipeRefreshFragment extends BaseFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener
        , SwipeRefreshLayout.OnRefreshListener, FragmentDispatcher.OnContentParentResizedInterface {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private AlertDialog.Builder mAlertDialogBuilder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //noinspection ConstantConditions
        setContentView(getArguments().getInt("layoutResID", View.NO_ID));
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
        if (getUserVisibleHint() && isVisible()) {
            initView();
        }
    }

    @Override
    public void onContentParentResized(int safeLeft, int safeTop, int safeRight, int safeBottom) {
        View contentView = findViewById(R.id.swiperefresh_iv_top);
        if (contentView != null) {
            ViewGroup.LayoutParams lp = contentView.getLayoutParams();
            if (lp == null) {
                lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, safeTop);
            }
            lp.height = safeTop;
            contentView.setLayoutParams(lp);
        }
    }

    private void initView() {
        if (mSwipeRefreshLayout != null) {
            return;
        }
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
                mSwipeRefreshLayout.setHeaderRefreshable(isChecked);
                break;
            }
            case R.id.swiperefresh_pullup_refreshable: {
                mSwipeRefreshLayout.setFooterRefreshable(isChecked);
                break;
            }
            default: {
                break;
            }
        }
    }

    @Override
    public void onHeaderRefresh(SwipeRefreshLayout parent, boolean isChanged, int state, float offset) {
        if (isChanged && state == SwipeRefreshLayout.STATE_REFRESHING) {
            if (parent.isHeaderRefreshFolded()) {
                showAlertDialog("正在加载中（顶部）");
            }
        }
    }

    @Override
    public void onFooterRefresh(SwipeRefreshLayout parent, boolean isChanged, int state, float offset) {
        if (isChanged && state == SwipeRefreshLayout.STATE_REFRESHING) {
            if (parent.isFooterRefreshFolded()) {
                showAlertDialog("正在加载中（底部）");
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
