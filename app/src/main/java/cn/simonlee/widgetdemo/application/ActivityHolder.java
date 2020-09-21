package cn.simonlee.widgetdemo.application;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.simonlee.widget.lib.application.ApplicationProxy;

import java.util.LinkedList;

/**
 * Activity管理类，通过注册Activity生命周期监听来实现
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2019-10-15
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ActivityHolder {

    /**
     * 单例，仅能注册一次
     */
    private static ActivityHolder mInstance;

    /**
     * 当前进程所有存活的Activity集合
     */
    private final LinkedList<Activity> mAliveActivityList = new LinkedList<>();

    /**
     * 当前进程所有活跃的Activity集合，一般情况下只有一个处于onResume状态的Activity，特定情况下会有0个或多个
     */
    private final LinkedList<Activity> mActiveActivityList = new LinkedList<>();

    private ActivityHolder() {
        //注册Activity声明周期监听
        ApplicationProxy.getApplication().registerActivityLifecycleCallbacks(getActivityLifecycleCallback());
    }

    /**
     * Activity管理，单例模式，仅能注册一次
     */
    public static void register() {
        if (mInstance == null) {
            synchronized (ActivityHolder.class) {
                if (mInstance == null) {
                    mInstance = new ActivityHolder();
                }
            }
        }
    }

    /**
     * 获取实例
     */
    public static ActivityHolder getInstance() {
        return mInstance;
    }

    /**
     * 关闭当前进程所有存活的Activity页面
     */
    public final void finishAllActivitys() {
        for (Activity activity : mAliveActivityList) {
            if (activity != null) {
                activity.finish();
            }
        }
    }

    /**
     * 获取当前进程所有存活的Activity，状态区间[onCreated, onDestroyed)
     */
    public Activity[] getAliveActivitys() {
        return (Activity[]) mAliveActivityList.toArray();
    }

    /**
     * 获取当前进程所有活跃的Activity，状态区间[onStarted, onStoped)
     */
    public Activity[] getActiveActivitys() {
        return (Activity[]) mActiveActivityList.toArray();
    }

    public int getAliveActivityCount() {
        return mAliveActivityList.size();
    }

    public int getActiveActivityCount() {
        return mActiveActivityList.size();
    }

    private Application.ActivityLifecycleCallbacks getActivityLifecycleCallback() {
        return new Application.ActivityLifecycleCallbacks() {

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                mAliveActivityList.add(activity);//会有若干个
            }

            @Override
            public void onActivityStarted(Activity activity) {
                mActiveActivityList.add(activity);
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
                mActiveActivityList.remove(activity);
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                mAliveActivityList.remove(activity);
            }

        };
    }
}
