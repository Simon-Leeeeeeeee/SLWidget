package com.simonlee.widget.lib.permission;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.PermissionChecker;

import com.simonlee.widget.lib.application.ApplicationProxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 权限管理类
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2019-07-17
 */
@SuppressWarnings({"unused", "JavadocReference", "UseSparseArrays", "WeakerAccess", "RedundantSuppression"})
public class PermissionManager {

    /**
     * 检查权限
     *
     * @return 返回未授权的权限列表
     */
    @NonNull
    public static List<String> checkPermission(@NonNull Context context, @NonNull String[] permissions) {
        //未授权的权限列表
        List<String> ungrantedPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (PermissionChecker.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                //未授权
                ungrantedPermissions.add(permission);
            }
        }
        return ungrantedPermissions;
    }

    /**
     * 检查权限，并进行回调
     *
     * @param context  上下文
     * @param callback 权限请求回调
     */
    public static void checkPermission(@NonNull Context context, @NonNull Callback callback) {
        //检查权限
        List<String> ungrantedPermissions = checkPermission(context, callback.getRequestPermissions());
        if (ungrantedPermissions.isEmpty()) {
            //权限通过
            callback.onRequestPassed();
        } else {
            //权限失败
            callback.onRequestFailed(ungrantedPermissions.toArray(new String[0]));
        }
    }

    /**
     * 检查并请求权限，最后进行回调
     * <p>
     * 注意：请求权限时会将callback存入一个静态HashMap，该callback应该在{@link Activity#onRequestPermissionsResult(int, String[], int[])}
     * 回调时通过调用{@link #delegateRequestPermissionsResult(Activity, int, String[], int[])}方法取出，否则将造成内存泄漏
     *
     * @param activity 用于请求权限，必须重写{@link Activity#onRequestPermissionsResult(int, String[], int[])}方法，
     *                 并确保一定会调用{@link #delegateRequestPermissionsResult(Activity, int, String[], int[])}
     * @param callback 权限请求回调
     */
    public static void requestPermissions(@NonNull Activity activity, @NonNull Callback callback) {
        requestPermissions(activity, null, callback);
    }

    /**
     * 检查并请求权限，最后进行回调
     * <p>
     * 注意：请求权限时会将callback存入一个静态HashMap，该callback必须在{@link Fragment#onRequestPermissionsResult(int, String[], int[])}
     * 回调时通过调用{@link #delegateRequestPermissionsResult(Fragment, int, String[], int[])}方法取出，否则将造成内存泄漏
     *
     * @param activity 用于请求权限，必须重写{@link Fragment#onRequestPermissionsResult(int, String[], int[])}方法，
     *                 并确保一定会调用{@link #delegateRequestPermissionsResult(Fragment, int, String[], int[])}
     * @param callback 权限请求回调
     */
    public static void requestPermissions(@NonNull Fragment fragment, @NonNull Callback callback) {
        requestPermissions(null, fragment, callback);
    }

    /**
     * 检查并请求权限，最后进行回调
     *
     * @param activity 通过Activity进行请求，应确保该Activity重写了{@link Activity#onRequestPermissionsResult(int, String[], int[])}方法，
     *                 并且一定会调用{@link #delegateRequestPermissionsResult(Activity, int, String[], int[])}
     * @param fragment 通过Fragment进行请求应确保该Fragment重写了{@link Fragment#onRequestPermissionsResult(int, String[], int[])}方法，
     *                 *                 并且一定会调用{@link #delegateRequestPermissionsResult(Fragment, int, String[], int[])}
     * @param callback 权限请求回调
     */
    private static void requestPermissions(Activity activity, Fragment fragment, @NonNull Callback callback) {
        Context context = activity != null ? activity : fragment.requireContext();
        List<String> ungrantedPermissions = new ArrayList<>();//未授权的权限列表
        for (String permission : callback.getRequestPermissions()) {
            if (PermissionChecker.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                //未授权
                ungrantedPermissions.add(permission);
            }
        }
        if (ungrantedPermissions.size() > 0) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || context.getApplicationInfo().targetSdkVersion < Build.VERSION_CODES.M) {
                //SDK23以下或target小于23直接回调权限申请被拒
                callback.onRequestRejected(ungrantedPermissions.toArray(new String[0]), false);
            } else {
                //临时保存Callback，并返回一个RequestCode，取值范围[0x8000,0xFFFF]
                int requestCode = Callback.keepCallBack(callback);
                //发起权限请求
                if (activity != null) {
                    activity.requestPermissions(ungrantedPermissions.toArray(new String[0]), requestCode);
                } else {
                    fragment.requestPermissions(ungrantedPermissions.toArray(new String[0]), requestCode);
                }
            }
        } else {
            //权限正常，直接回调
            callback.onRequestPassed();
        }
    }

    /**
     * 代理请求权限的回调处理，必须重写{@link Activity#onRequestPermissionsResult(int, String[], int[])}方法，并确保一定会调用此方法
     *
     * @param callBackKey  Callback的key值，应该是{@link Callback#keepCallBack(Callback)}返回的key值
     * @param permissions  请求的权限列表
     * @param grantResults 返回的结果列表
     * @return 是否拦截处理了该权限请求回调
     */
    public static boolean delegateRequestPermissionsResult(@NonNull Activity activity, int callBackKey, String[] permissions, int[] grantResults) {
        return delegateRequestPermissionsResult(activity, null, callBackKey, permissions, grantResults);
    }

    /**
     * 代理请求权限的回调处理，必须重写{@link Fragment#onRequestPermissionsResult(int, String[], int[])}方法，并确保一定会调用此方法
     *
     * @param callBackKey  Callback的key值，应该是{@link Callback#keepCallBack(Callback)}返回的key值
     * @param permissions  请求的权限列表
     * @param grantResults 返回的结果列表
     * @return 是否拦截处理了该权限请求回调
     */
    public static boolean delegateRequestPermissionsResult(@NonNull Fragment fragment, int callBackKey, String[] permissions, int[] grantResults) {
        return delegateRequestPermissionsResult(null, fragment, callBackKey, permissions, grantResults);
    }


    /**
     * 代理请求权限的回调处理
     *
     * @param callBackKey  Callback的key值，应该是{@link Callback#keepCallBack(Callback)}返回的key值
     * @param permissions  请求的权限列表
     * @param grantResults 返回的结果列表
     * @return 是否拦截处理了该权限请求回调
     */
    private static boolean delegateRequestPermissionsResult(Activity activity, Fragment fragment, int callBackKey, String[] permissions, int[] grantResults) {
        Callback callBack = Callback.removeCallBack(callBackKey);
        if (callBack == null) {
            return false;
        }
        //拒绝的权限列表
        List<String> rejectedPermissions;
        if (permissions.length == 0) {
            //当同时发起两个权限请求时，后发起的请求会直接跳过权限请求回调一个空数组，这里要重新检查权限
            Context context = activity != null ? activity : fragment.requireContext();
            rejectedPermissions = checkPermission(context, callBack.getRequestPermissions());
        } else {
            rejectedPermissions = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (i < grantResults.length) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        rejectedPermissions.add(permissions[i]);
                    }
                } else {
                    rejectedPermissions.add(permissions[i]);
                }
            }
        }
        if (rejectedPermissions.isEmpty()) {
            //权限获取成功
            callBack.onRequestPassed();
        } else {
            //标记是否可以再次请求权限
            boolean canRequestAgain = true;
            for (String permission : rejectedPermissions) {
                if ((activity != null && !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission))
                        || (fragment != null && !fragment.shouldShowRequestPermissionRationale(permission))) {
                    //已勾选拒绝后不再提示，不可再次请求权限
                    canRequestAgain = false;
                    break;
                }
            }
            //权限请求被拒，建议弹窗说明为什么需要这些权限，并引导用户再次请求权限，或跳转权限设置页面
            callBack.onRequestRejected(rejectedPermissions.toArray(new String[0]), canRequestAgain);
        }
        return true;
    }

    /**
     * 跳转权限设置页面
     * <p>
     * 建议在{@link Callback#onRequestRejected(String[])}方法中调用
     */
    public static void startPermissionSettingActivity(@NonNull Context context) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivity(intent);
    }

    /**
     * 跳转权限设置页面，并在返回APP后再次检查权限并回调
     * <p>
     * 建议在{@link Callback#onRequestRejected(String[])}方法中调用
     */
    public static void startPermissionSettingActivity(@NonNull Context context, @NonNull Callback callBack) {
        Application application;
        if (!(context instanceof Application)) {
            context = context.getApplicationContext();
        }
        if (context instanceof Application) {
            application = (Application) context;
        } else {
            application = ApplicationProxy.getApplication();
        }
        if (application != null) {
            final int key = Callback.keepCallBack(callBack);
            application.registerActivityLifecycleCallbacks(new ActivityLifecycleCallback(key, application));
        }
        startPermissionSettingActivity(context);
    }

    /**
     * 用于跳转权限设置页面时监听Activity声明周期变化
     * <p>
     * 当Activity进入前台，重新检查权限，然后进行回调；
     * 当Activity被finish，不再回调
     */
    private static class ActivityLifecycleCallback implements Application.ActivityLifecycleCallbacks {

        private final int mKey;
        private final Application mApplication;

        private ActivityLifecycleCallback(int key, Application application) {
            this.mKey = key;
            this.mApplication = application;
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivityResumed(Activity activity) {
            Callback callback = Callback.removeCallBack(mKey);
            if (callback != null) {
                PermissionManager.checkPermission(mApplication, callback);
            }
            mApplication.unregisterActivityLifecycleCallbacks(this);
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
            if (activity.isFinishing()) {
                Callback.removeCallBack(mKey);
                mApplication.unregisterActivityLifecycleCallbacks(this);
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }
    }

    /**
     * 权限请求回调
     */
    public static abstract class Callback {

        /**
         * 静态HashMap，用于临时储存回调，在用户处理权限申请后进行回调
         */
        private static HashMap<Integer, Callback> mCallBackMap = new HashMap<>();

        private static volatile int keyIndex;

        /**
         * 临时保存CallBack对象到HashMap中，并返回对应Key值
         *
         * @return Key值，用作为权限请求的RequestCode，仅使用低16位，取值范围[0x8000,0xFFFF]
         */
        private synchronized static int keepCallBack(@NonNull Callback callback) {
            int key;
            //低15位循环自增
            if (++keyIndex > 0x7FFF) {
                keyIndex = 0;
            }
            //第16位置为1
            key = keyIndex & 0x8000;
            //出队
            mCallBackMap.remove(key);
            //入队
            mCallBackMap.put(key, callback);
            return key;
        }

        /**
         * 根据key值从HashMap中移除监听
         */
        private synchronized static Callback removeCallBack(int key) {
            return mCallBackMap.remove(key);
        }

        /**
         * 清空临时保存Callback的HashMap
         */
        private synchronized static void clearCallbackMap() {
            mCallBackMap.clear();
        }

        /**
         * 请求的权限数组
         */
        private final String[] mRequestPermissions;

        /**
         * @param requestPermissions 请求的权限数组
         */
        public Callback(@NonNull String[] requestPermissions) {
            this.mRequestPermissions = requestPermissions;
        }

        /**
         * 返回请求的权限数组
         */
        protected String[] getRequestPermissions() {
            return mRequestPermissions;
        }

        /**
         * 权限请求通过
         */
        public abstract void onRequestPassed();

        /**
         * 权限请求失败，用户拒绝再次请求权限，或拒绝跳转权限设置页面，或跳转设置页面后未勾选所需权限
         *
         * @param ungrantedPermissions 未授权的权限数组
         */
        public abstract void onRequestFailed(@NonNull String[] ungrantedPermissions);

        /**
         * 权限请求被拒，建议弹窗说明为什么需要这些权限，并引导用户再次请求权限，或跳转权限设置页面
         *
         * @param rejectedPermissions 被拒绝的权限数组
         * @param canRequestAgain     是否可以再次发起请求
         */
        public abstract void onRequestRejected(@NonNull String[] rejectedPermissions, boolean canRequestAgain);

    }

}