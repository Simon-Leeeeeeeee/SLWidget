package cn.simonlee.widgetdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Fragment基类
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-08-24
 */
@SuppressWarnings("unused")
public abstract class BaseFragment extends Fragment {

    private View mRootView;
    private Toolbar mToolbar;

    private Integer layoutResID;

    public final void setContentView(@LayoutRes Integer layoutResID) {
        this.layoutResID = layoutResID;
    }

    @Override
    public Context getContext() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return super.getContext();
        } else {
            return getActivity();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.layoutResID != null && layoutResID != View.NO_ID) {
            if (mRootView == null) {
                mRootView = inflater.inflate(this.layoutResID, container, false);
            } else {
                ViewGroup parent = (ViewGroup) mRootView.getParent();
                if (parent != null && parent != container) {
                    parent.removeView(mRootView);
                }
            }
            return mRootView;
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * 在Activity的进入窗口动画结束时被Activity调用，用于开始进行一些绘制。
     * <p>
     * 详情：{@link Activity#onEnterAnimationComplete()}
     */
    public void onEnterAnimationComplete() {
    }

    public final <T extends View> T findViewById(@IdRes int id) {
        return mRootView.findViewById(id);
    }

    public final void startActivity(Class<?> Class, boolean finishMyself, Intent intent) {
        if (getActivity() != null) {
            startActivityForResult(Class, -1, intent);
            if (finishMyself) getActivity().finish();
        }
    }

    public void startActivityForResult(Class<?> Class, int requestCode, Intent intent) {
        if (getActivity() != null) {
            if (intent != null) {
                if (Class != null) {
                    intent.setClass(getActivity(), Class);
                }
            } else {
                intent = new Intent(getActivity(), Class);
            }
            startActivityForResult(intent, requestCode);
        }
    }

    public final Toolbar getToolBar() {
        if (mToolbar == null && getActivity() != null) {
            mToolbar = getActivity().findViewById(R.id.base_toolbar);
        }
        return mToolbar;
    }

}
