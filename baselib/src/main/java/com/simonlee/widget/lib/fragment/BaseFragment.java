package com.simonlee.widget.lib.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import com.simonlee.widget.lib.activity.BaseActivity;
import com.simonlee.widget.lib.dialog.BaseDialog;
import com.simonlee.widget.lib.permission.PermissionManager;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

/**
 * Fragment基类
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2019-06-24
 */
@SuppressWarnings({"unused"})
public abstract class BaseFragment extends Fragment implements BaseDialog.DialogResultListener {

    /**
     * 根View
     */
    private FrameLayout mRootView;

    /**
     * Activity布局中的TitleBar
     */
    private ViewGroup mTitleBar;

    /**
     * 设置布局
     */
    protected void setContentView(@LayoutRes int layoutResID) {
        if (mRootView == null) {
            mRootView = new FrameLayout(requireContext());
        } else {
            mRootView.removeAllViewsInLayout();
        }
        LayoutInflater.from(mRootView.getContext()).inflate(layoutResID, mRootView);
    }

    /**
     * 设置布局
     */
    protected void setContentView(View contentView) {
        if (mRootView == null) {
            mRootView = new FrameLayout(requireContext());
        } else {
            mRootView.removeAllViewsInLayout();
        }
        mRootView.addView(contentView);
    }

    /**
     * 设置布局
     */
    protected void setContentView(View contentView, ViewGroup.LayoutParams layoutParams) {
        if (mRootView == null) {
            mRootView = new FrameLayout(requireContext());
        } else {
            mRootView.removeAllViewsInLayout();
        }
        mRootView.addView(contentView, layoutParams);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = new FrameLayout(container.getContext());
        } else {
            ViewParent parent = mRootView.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(mRootView);
            }
        }
        return mRootView;
    }

    /**
     * 返回Activity布局中的TitleBar
     */
    @SuppressWarnings("unchecked")
    public <T extends ViewGroup> T getTitleBar() {
        if (mTitleBar == null) {
            Activity activity = getActivity();
            if (activity instanceof BaseActivity) {
                mTitleBar = ((BaseActivity) activity).getTitleBar();
            }
        }
        return (T) mTitleBar;
    }

    public final <T extends View> T findViewById(@IdRes int id) {
        return mRootView.findViewById(id);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //PermissionManager代理权限请求后的返回处理
        if (!PermissionManager.delegateRequestPermissionsResult(this, requestCode, permissions, grantResults)) {
            //PermissionManager未处理该回调，则调用父类方法
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle data) {
    }

}
