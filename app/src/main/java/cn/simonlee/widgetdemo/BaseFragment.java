package cn.simonlee.widgetdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

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

    private Toolbar mToolbar;

    private int mLayoutResID = View.NO_ID;

    public final void setContentView(@LayoutRes int layoutResID) {
        this.mLayoutResID = layoutResID;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getView() != null) {
            ViewParent parent = getView().getParent();
            if (parent != null && parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(getView());
            }
            return getView();
        } else if (mLayoutResID != View.NO_ID) {
            return inflater.inflate(this.mLayoutResID, container, false);
        } else {
            return super.onCreateView(inflater, container, savedInstanceState);
        }
    }

    public final Toolbar getToolBar() {
        if (mToolbar == null && getActivity() != null) {
            mToolbar = getActivity().findViewById(R.id.base_toolbar);
        }
        return mToolbar;
    }

    @SuppressWarnings("ConstantConditions")
    public final <T extends View> T findViewById(@IdRes int id) {
        return getView().findViewById(id);
    }

    /**
     * 在Activity的进入窗口动画结束时被Activity调用，用于开始进行一些绘制。
     * <p>
     * 详情：{@link Activity#onEnterAnimationComplete()}
     */
    public void onEnterAnimationComplete() {
    }

    public final void startActivity(Class<?> Class, boolean finishMyself, Intent intent) {
        if (getActivity() != null) {
            startActivityForResult(Class, -1, intent);
            if (finishMyself) {
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.activity_fade_enter, R.anim.activity_fade_exit);
            }
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

}
