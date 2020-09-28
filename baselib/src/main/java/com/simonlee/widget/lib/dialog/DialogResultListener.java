package com.simonlee.widget.lib.dialog;

import android.os.Bundle;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import androidx.annotation.NonNull;

/**
 * Dialog回调监听
 * <p>
 * 1. 泛型T的使用：对话框回调时向监听器注入泛型实例（本质为实现泛型接口的FragmentActivity或Fragment，参阅{@link BaseDialog#getDialogResultListener()}），
 * 回调时通过{@link DialogResultListener#get()} 获取泛型实例，用于解决监听器直接持有外部引用可能的内存泄漏及引用失效的问题
 * <p>
 * 2. 内部管理了一个静态的监听器列表，通过{@link DialogResultListener#keepListener(DialogResultListener)}保存监听器的实例对象
 * 对话框在回调时可通过{@link DialogResultListener#findListenerByTag(String)}取回监听器实例
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2019-05-22
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class DialogResultListener<T> implements BaseDialog.DialogResultListener {

    /**
     * 静态HashMap，用于临时储存监听器，在对话框关闭时取出进行回调
     */
    private static HashMap<String, DialogResultListener> mDialogResultListenerMap = new HashMap<>();


    /**
     * 回调结果接口
     *
     * @param host        对话框持有者，为实现泛型接口的FragmentActivity或Fragment实例
     * @param requestCode 对话框请求码
     * @param resultCode  返回结果码，若Dialog被取消，该值为{@link BaseDialog#CODE_CANCEL}
     * @param data        返回数据
     */
    public abstract void onDialogResult(T host, int requestCode, int resultCode, Bundle data);

    /**
     * 监听标记，用于在回调时取回监听器实例
     */
    private final String mTag = String.valueOf(System.nanoTime()) + hashCode();

    /**
     * 弱引用，用于临时存储实现泛型接口的FragmentActivity或Fragment实例
     */
    private WeakReference<T> mWeakReference;

    /**
     * 获取标记
     */
    protected String getTag() {
        return mTag;
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle data) {
        onDialogResult(get(), requestCode, resultCode, data);
    }

    /**
     * 在回调中通过get方法可以取回实现泛型接口的FragmentActivity/Fragment实例
     */
    protected T get() {
        if (mWeakReference != null) {
            final T t = mWeakReference.get();
            mWeakReference.clear();
            return t;
        }
        return null;
    }

    /**
     * 在对话框回调时调用，用于注入实现泛型接口的FragmentActivity/Fragment实例，详见{@link BaseDialog#getDialogResultListener()}
     */
    protected void inject(T t) {
        if (mWeakReference != null) {
            mWeakReference.clear();
        }
        mWeakReference = new WeakReference<>(t);
    }

    /**
     * 保存监听实例
     */
    protected static void keepListener(@NonNull DialogResultListener listener) {
        mDialogResultListenerMap.put(listener.mTag, listener);
    }

    /**
     * 根据监听标记从列表中获取监听实例，并从列表移除
     */
    protected static DialogResultListener findListenerByTag(String tag) {
        return mDialogResultListenerMap.remove(tag);
    }

}
