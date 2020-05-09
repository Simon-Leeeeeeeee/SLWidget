package com.simonlee.widget.lib.dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

/**
 * Dialog构建工具
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2019-05-22
 */
@SuppressWarnings("unused")
public class DialogBuild {

    /**
     * 对话框class
     */
    private final Class<? extends BaseDialog> mDialogClass;

    /**
     * 对话框持有者，只能是FragmentActivity或Fragment
     */
    @NonNull
    private final Object mHost;

    /**
     * 对话框持有者（Fragment）标记
     */
    private final String mFragmentTag;

    /**
     * Fragment管理器
     */
    private final FragmentManager mFragmentManager;

    /**
     * 请求码
     */
    private Integer mRequestCode;

    /**
     * 是否可以取消
     */
    private Boolean mCancelable;

    /**
     * 是否可以点击对话框外部区域取消
     */
    private Boolean mOutsideCancelable;

    /**
     * 参数
     */
    private Bundle mArguments;

    /**
     * 回调监听器
     */
    private DialogResultListener mDialogResultListener;

    public DialogBuild(@NonNull Fragment hostFragment, @NonNull Class<? extends BaseDialog> dialogClass) {
        this(hostFragment, hostFragment.getTag(), hostFragment.getFragmentManager(), dialogClass);
    }

    public DialogBuild(@NonNull FragmentActivity hostActivity, @NonNull Class<? extends BaseDialog> dialogClass) {
        this(hostActivity, null, hostActivity.getSupportFragmentManager(), dialogClass);
    }

    private DialogBuild(@NonNull Object host, String fragmentTag, FragmentManager fragmentManager, @NonNull Class<? extends BaseDialog> dialogClass) {
        this.mHost = host;
        this.mFragmentTag = fragmentTag;
        this.mFragmentManager = fragmentManager;
        this.mDialogClass = dialogClass;
    }

    /**
     * 显示对话框
     */
    public final void show() {
        BaseDialog dialog;
        try {
            dialog = mDialogClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        if (mRequestCode != null) {
            dialog.setRequestCode(mRequestCode);
        }
        if (mCancelable != null) {
            dialog.setCancelable(mCancelable);
        }
        if (mOutsideCancelable != null) {
            dialog.setCanceledOnTouchOutside(mOutsideCancelable);
        }
        if (mDialogResultListener != null && mDialogResultListener != mHost) {
            //将监听添加到一个静态列表中储存，对话框回调时会移除
            DialogResultListener.keepListener(mDialogResultListener);
            if (mArguments == null) {
                mArguments = new Bundle();
            }
            mArguments.putString(BaseDialog.KEY_LISTENERTAG, mDialogResultListener.getTag());
        } else if (mRequestCode != null && mRequestCode != 0) {
            //Host会接收回调结果，必须实现DialogResultListener接口
            if (!(mHost instanceof BaseDialog.DialogResultListener)) {
                throw new IllegalStateException(mHost.getClass().getName() + " must be implements DialogResultListener!");
            }
        }
        //Dialog根据Tag确定持有者（Fragment），因此Fragment必须有Tag
        if (mHost instanceof Fragment) {
            if (TextUtils.isEmpty(mFragmentTag)) {
                throw new IllegalStateException(mHost.getClass().getName() + " must set the tag!");
            }
            if (mArguments == null) {
                mArguments = new Bundle();
            }
            mArguments.putString(BaseDialog.KEY_FRAGMENTTAG, mFragmentTag);
        }
        dialog.setArguments(mArguments);
        dialog.show(mFragmentManager, dialog.getClass().getName());
    }

    /**
     * 设置请求码
     */
    public DialogBuild setRequestCode(int requestCode) {
        this.mRequestCode = requestCode;
        return this;
    }

    /**
     * 设置是否可以取消
     */
    public DialogBuild setCancelable(boolean cancelable) {
        this.mCancelable = cancelable;
        return this;
    }

    /**
     * 设置是否可以点击对话框外部区域取消
     */
    public DialogBuild setCanceledOnTouchOutside(boolean cancelable) {
        this.mOutsideCancelable = cancelable;
        return this;
    }

    /**
     * 设置参数
     */
    public DialogBuild setArguments(Bundle arguments) {
        this.mArguments = arguments;
        return this;
    }

    /**
     * 设置回调监听器，这里要注意：
     * <p>
     * 1. 匿名内部类会隐式持有外部类的引用，可能导致内存泄漏
     * <p>
     * 2. 匿名内部类持有的引用可能因为销毁重建而指向错误的对象
     */
    public DialogBuild setOnDialogResultListener(DialogResultListener<?> listener) {
        this.mDialogResultListener = listener;
        return this;
    }

}
