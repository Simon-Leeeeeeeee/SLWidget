package com.simonlee.widget.lib.application;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.List;

/**
 * Application代理，用于获取Application实例、主线运行、以及获取进程信息
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2019-07-17
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ApplicationProxy {

    /**
     * 单例，仅能绑定一个Application实例
     */
    private static ApplicationProxy mInstance;

    /**
     * 当前进程Application实例
     */
    private final Application mApplication;

    /**
     * 主线程
     */
    private final Thread mMainThread;

    /**
     * 主线程Handler
     */
    private final Handler mMainThreadHandler;

    /**
     * 是否为debug模式
     */
    private final boolean isDebug;

    /**
     * 当前进程名称
     */
    private String mProcessName;

    /**
     * 当前进程信息
     */
    private ActivityManager.RunningAppProcessInfo mProcessInfo;

    /**
     * 版本号
     */
    private int mVersionCode;

    /**
     * 版本名
     */
    private String mVersionName;

    /**
     * 应用包信息
     */
    private PackageInfo mPackageInfo;

    private ApplicationProxy(@NonNull Application application) {
        mApplication = application;
        Looper mainLooper = Looper.getMainLooper();
        mMainThread = mainLooper.getThread();
        mMainThreadHandler = new Handler(mainLooper);
        isDebug = ApplicationInfo.FLAG_DEBUGGABLE == (mApplication.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE);
    }

    /**
     * 绑定Application，单例模式，仅能绑定一次
     */
    public static void bind(@NonNull Application application) {
        if (mInstance == null) {
            synchronized (ApplicationProxy.class) {
                if (mInstance == null) {
                    mInstance = new ApplicationProxy(application);
                }
            }
        }
    }

    /**
     * 获取Application实例
     */
    public static Application getApplication() {
        return mInstance.mApplication;
    }

    /**
     * 是否为Debug模式
     */
    public static boolean isDebug() {
        return mInstance.isDebug;
    }

    /**
     * 是否当前为主进程
     */
    public static boolean isMainProcess() {
        String curProcessName = getCurProcessName();
        return curProcessName != null && curProcessName.equals(mInstance.mApplication.getPackageName());
    }

    /**
     * 获取当前进程信息
     */
    public static ActivityManager.RunningAppProcessInfo getCurProcessInfo() {
        if (mInstance.mProcessInfo == null) {
            ActivityManager activityManager = (ActivityManager) mInstance.mApplication.getSystemService(Context.ACTIVITY_SERVICE);
            if (activityManager == null) {
                return null;
            }
            List<ActivityManager.RunningAppProcessInfo> processInfoList = activityManager.getRunningAppProcesses();
            if (processInfoList == null) {
                return null;
            }
            final int curProcessPid = android.os.Process.myPid();
            for (ActivityManager.RunningAppProcessInfo processInfo : processInfoList) {
                if (processInfo.pid == curProcessPid) {
                    mInstance.mProcessInfo = processInfo;
                    break;
                }
            }
        }
        return mInstance.mProcessInfo;
    }

    /**
     * 获取当前进程名
     */
    public static String getCurProcessName() {
        ActivityManager.RunningAppProcessInfo processInfo = getCurProcessInfo();
        if (processInfo != null) {
            return processInfo.processName;
        }
        return null;
    }

    /**
     * 获取应用包信息
     */
    public static PackageInfo getPackageInfo() {
        if (mInstance.mPackageInfo == null) {
            try {
                mInstance.mPackageInfo = mInstance.mApplication.getPackageManager()
                        .getPackageInfo(mInstance.mApplication.getPackageName(), PackageManager.GET_CONFIGURATIONS);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return mInstance.mPackageInfo;
    }

    /**
     * 获取应用版本名
     */
    public static String getVersionName() {
        PackageInfo packageInfo = getPackageInfo();
        if (packageInfo != null) {
            return packageInfo.versionName;
        }
        return null;
    }

    /**
     * 获取应用版本号
     */
    public static int getVersionCode() {
        PackageInfo packageInfo = getPackageInfo();
        if (packageInfo != null) {
            return packageInfo.versionCode;
        }
        return 0;
    }

    /**
     * 判断当前是否在主线程运行
     */
    public static boolean isOnUiThread() {
        return Thread.currentThread() == mInstance.mMainThread;
    }

    /**
     * 在主线程运行
     */
    public static void runOnUiThread(Runnable action) {
        mInstance.mMainThreadHandler.post(action);
    }

    /**
     * 延时在主线程运行
     */
    public static void runOnUiThreadDelay(Runnable action, long delay) {
        mInstance.mMainThreadHandler.postDelayed(action, delay);
    }

}
