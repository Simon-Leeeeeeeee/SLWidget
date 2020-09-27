package com.simonlee.widget.lib.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.IntRange;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.simonlee.widget.lib.R;
import com.simonlee.widget.lib.permission.PermissionManager;

/**
 * Dialog基类
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2019-05-20
 */
@SuppressWarnings("unused")
public abstract class BaseDialog extends DialogFragment {

    /**
     * 对话框回调的Listener标记
     */
    public static final String KEY_LISTENERTAG = "baseDialog:listenerTag";

    /**
     * 对话框持有者的Fragment标记，通过Tag确定Fragment。参见{@link #findHost()}
     */
    public static final String KEY_FRAGMENTTAG = "baseDialog:fragmentTag";

    /**
     * 结果代码：标志Dialog被取消
     * <p>
     * 包括三种情形：返回键取消、点击Dialog外部区域取消、调用{@link Dialog#cancel()}取消，参见{@link #isCanceled}
     */
    public static final int CODE_CANCEL = -1;

    /**
     * 布局View
     */
    private View mContentView;

    /**
     * 标志Dialog是否被取消
     * <p>
     * 若为true，{@link DialogResultListener#onDialogResult(int, int, Bundle)}回调的resultCode将会强制为{@link #CODE_CANCEL}
     * <p>
     * 包括三种情形：返回键取消、点击Dialog外部区域取消、调用{@link #cancel()}取消
     */
    private boolean isCanceled = false;

    /**
     * 标志是否可以点击外部区域取消对话框
     */
    private boolean mOutsideCancelable = false;

    /**
     * 请求代码
     */
    private int mRequestCode;

    /**
     * 结果代码
     */
    private int mResultCode;

    /**
     * 隐藏对话框时回调的结果
     */
    private Bundle mResultBundle;

    /**
     * 返回DialogFragment实例，尽可能用getDialogFragment()来代替this的使用
     */
    public <T extends BaseDialog> T getDialogFragment() {
        //noinspection unchecked
        return (T) BaseDialog.this;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.BaseDialogTheme);
        super.onCreate(savedInstanceState);
        //当dialog被销毁重建时需要恢复数据
        if (savedInstanceState != null) {
            this.mRequestCode = savedInstanceState.getInt("baseDialog:requestCode", 0);
            this.mResultCode = savedInstanceState.getInt("baseDialog:resultCode", 0);
            this.mResultBundle = savedInstanceState.getParcelable("baseDialog:resultData");
            //如果不允许取消，外部点击也不允许取消
            this.mOutsideCancelable = isCancelable() && savedInstanceState.getBoolean("baseDialog:outsideCancelable", false);
        }
    }

    /**
     * 当dialog即将销毁重建前保存数据
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.mRequestCode != 0) {
            outState.putInt("baseDialog:requestCode", this.mRequestCode);
        }
        if (this.mResultCode != 0) {
            outState.putInt("baseDialog:resultCode", this.mResultCode);
        }
        if (this.mResultBundle != null) {
            outState.putParcelable("baseDialog:resultData", this.mResultBundle);
        }
        if (this.mOutsideCancelable) {
            outState.putBoolean("baseDialog:outsideCancelable", true);
        }
    }

    /**
     * 设置布局
     */
    protected void setContentView(@LayoutRes int layoutResID) {
        this.setContentView(LayoutInflater.from(requireContext()).inflate(layoutResID, null));
    }

    /**
     * 设置布局
     */
    protected void setContentView(View contentView) {
        this.mContentView = contentView;
        if (getDialog() != null) {
            getDialog().setContentView(mContentView);
        }
    }

    /**
     * 设置布局
     */
    protected void setContentView(View contentView, @NonNull ViewGroup.LayoutParams layoutParams) {
        this.mContentView = contentView;
        if (getDialog() != null) {
            getDialog().setContentView(mContentView, layoutParams);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mContentView != null) {
            ViewParent parent = mContentView.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(mContentView);
            }
        }
        return mContentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //设置Dialog是否可点击外部区域取消
        setCanceledOnTouchOutside(mOutsideCancelable && isCancelable());
    }

    public final <T extends View> T findViewById(@IdRes int id) {
        //noinspection ConstantConditions
        return getDialog().getWindow().getDecorView().findViewById(id);
    }

    /**
     * 取消Dialog
     */
    public void cancel() {
        getDialog().cancel();
    }

    /**
     * 当Dialog被取消时触发，{@link DialogResultListener#onDialogResult(int, int, Bundle)}回调的resultCode将会强制为{@link #CODE_CANCEL}
     * <p>
     * 包括三种情形：返回键取消、点击Dialog外部区域取消、调用{@link #cancel()}取消
     */
    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        //标记对话框被取消
        isCanceled = true;
    }

    /**
     * 当对话框被隐藏时触发
     * <p>
     * 包括三种情形：
     * <p>
     * 1. Dialog被隐藏
     * <p>
     * {@link #onDismiss(DialogInterface)} -> {@link #onStop()}
     * -> {@link #onDestroyView()} -> {@link #onDestroy()} -> {@link #onDetach()}
     * <p>
     * 2. Dialog被取消
     * <p>
     * {@link #onCancel(DialogInterface)} -> {@link #onDismiss(DialogInterface)} -> {@link #onStop()}
     * -> {@link #onDestroyView()} -> {@link #onDestroy()} -> {@link #onDetach()}
     * <p>
     * 3. Dialog被销毁重建
     * <p>
     * {@link #onStop()} -> {@link #onSaveInstanceState(Bundle)}
     * -> {@link #onDestroyView()} -> {@link #onDestroy()} -> {@link #onDetach()} -> {@link #onDismiss(DialogInterface)}
     */
    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (isAdded()) {//回调结果
            callbackResult();
        }
    }

    /**
     * 对话框回调
     */
    private void callbackResult() {
        //获取回调监听器
        DialogResultListener dialogResultListener = getDialogResultListener();
        if (dialogResultListener == null) {
            return;
        }
        //回调结果浅复制
        Bundle result = mResultBundle == null ? null : new Bundle(mResultBundle);
        //对话框回调，当对话框被取消时，结果代码强制为CODE_CANCEL
        dialogResultListener.onDialogResult(mRequestCode, isCanceled ? CODE_CANCEL : mResultCode, result);
    }

    /**
     * 获取回调监听器
     */
    protected DialogResultListener getDialogResultListener() {
        //获取监听TAG
        String listenerTag = getArguments() == null ? null : getArguments().getString(KEY_LISTENERTAG);
        //TAG不为空，根据TAG查找监听器
        if (!TextUtils.isEmpty(listenerTag)) {
            //查找监听器
            com.simonlee.widget.lib.dialog.DialogResultListener listener
                    = com.simonlee.widget.lib.dialog.DialogResultListener.findListenerByTag(listenerTag);
            if (listener != null) {
                //noinspection unchecked
                listener.inject(findHost());
            }
            return listener;
        }
        //TAG为空，requestCode不为0，通过Activity或Fragment实现DialogResultListener接口进行回调
        else if (mRequestCode != 0) {
            //获取Dialog持有者（Activity或Fragment）
            Object host = findHost();
            //Dialog持有者必须实现DialogResultListener接口
            if (host instanceof DialogResultListener) {
                return ((DialogResultListener) host);
            } else if (host != null) {
                throw new IllegalStateException(host.getClass().getName() + " must be implements DialogResultListener!");
            }
        }
        return null;
    }

    /**
     * 获取Dialog持有者，Activity或Fragment
     */
    protected Object findHost() {
        //获取fragmentTag
        String fragmentTag = getArguments() == null ? null : getArguments().getString(KEY_FRAGMENTTAG);
        //确定Dialog持有者（Fragment/Activity）
        if (TextUtils.isEmpty(fragmentTag)) {
            return getActivity();
        } else {
            //根据tag查找目标fragment
            return getFragmentManager() == null ? null : getFragmentManager().findFragmentByTag(fragmentTag);
        }
    }

    /**
     * 设置对话框是否可以取消
     */
    @Override
    public void setCancelable(boolean cancelable) {
        super.setCancelable(cancelable);
        if (!cancelable) {
            //对话框不可取消时，设置点击外部区域也不可取消
            setCanceledOnTouchOutside(false);
        }
    }

    /**
     * 设置是否可以点击外部区域来取消对话框
     */
    public void setCanceledOnTouchOutside(boolean outsideCancelable) {
        this.mOutsideCancelable = outsideCancelable;
        if (outsideCancelable) {
            //因为点击外部区域可取消，Dialog应设置为可取消的
            setCancelable(true);
        }
        if (getDialog() != null) {
            getDialog().setCanceledOnTouchOutside(outsideCancelable);
        }
    }

    /**
     * 设置取消对话框时的回调结果
     *
     * @param resultCode 结果代码
     */
    public final void setResult(@IntRange(from = 0) int resultCode) {
        if (resultCode == CODE_CANCEL) {
            throw new IllegalStateException("ResultCode can not be CODE_CANCEL(was " + CODE_CANCEL + ") !");
        }
        this.mResultCode = resultCode;
        this.mResultBundle = null;
    }

    /**
     * 设置取消对话框时的回调结果
     *
     * @param data 回调结果
     */
    public final void setResult(Bundle data) {
        this.mResultCode = 0;
        this.mResultBundle = data;
    }

    /**
     * 设置取消对话框时的回调结果
     *
     * @param resultCode 结果代码
     * @param data       回调结果
     */
    public final void setResult(@IntRange(from = 0) int resultCode, Bundle data) {
        if (resultCode == CODE_CANCEL) {
            throw new IllegalStateException("ResultCode can not be CODE_CANCEL(was " + CODE_CANCEL + ") !");
        }
        this.mResultCode = resultCode;
        this.mResultBundle = data;
    }

    /**
     * 返回对话框结果代码
     */
    public int getResultCode() {
        return mResultCode;
    }

    /**
     * 返回对话框请求代码
     */
    public int getRequestCode() {
        return mRequestCode;
    }

    /**
     * 设置对话框请求代码
     * <p>
     * 若requestCode不为0，Dialog结束时一定会回调，具体逻辑详见{@link #callbackResult()}
     */
    public final void setRequestCode(int requestCode) {
        this.mRequestCode = requestCode;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //PermissionManager代理权限请求后的返回处理
        if (!PermissionManager.delegateRequestPermissionsResult(this, requestCode, permissions, grantResults)) {
            //PermissionManager未处理该回调，则调用父类方法
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * 对话框回调监听
     * <p>
     * 建议通过Activity或Fragment实现此接口进行回调，参见{@link #getDialogResultListener()}
     * <p>
     * 不建议Dialog直接持有DialogResultListener对象，因为Dialog一旦被销毁重建，持有的DialogResultListener对象为null
     */
    public interface DialogResultListener {

        /**
         * 回调结果接口
         *
         * @param requestCode 对话框请求码
         * @param resultCode  返回结果码，若Dialog被取消，该值为{@link #CODE_CANCEL}
         * @param data        返回数据
         */
        void onDialogResult(int requestCode, int resultCode, Bundle data);
    }

}
