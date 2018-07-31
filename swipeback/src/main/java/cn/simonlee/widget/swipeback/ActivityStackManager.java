package cn.simonlee.widget.swipeback;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;

import java.lang.reflect.Method;
import java.util.Stack;

/**
 * Activity生命周期管理，在Application中注册即可。
 * <p>
 * 1. 解决"android:windowIsTranslucent"属性带来的性能问题，次层的Activity会通过反射将窗口转为不透明，使底层Activity进入onStop状态
 * 2. 解决旋转屏幕对Activity栈产生的影响，防止底层Activity被唤醒
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-07-31
 */
@SuppressLint("PrivateApi")
public class ActivityStackManager implements Application.ActivityLifecycleCallbacks {

    private final Stack<Activity> mActivityStack = new Stack<>();

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            //activity不是由状态恢复导致，直接入栈
            mActivityStack.add(activity);
        } else {
            //activity是由状态恢复导致，获取该activity在栈中的位置
            int index = savedInstanceState.getInt("index", -1);
            if (index < 0 || index >= mActivityStack.size()) {
                //index越界，直接入栈
                mActivityStack.add(activity);
            } else {
                //替换栈中对应位置的实例，因为经过横竖屏切换，虽然是同一个activity界面，但是实例已经变换了
                mActivityStack.set(index, activity);
            }
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        //activity状态保存，存入当前activity在栈中的位置
        outState.putInt("index", mActivityStack.indexOf(activity));
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Activity topActivity = getTopActivity();
        if (topActivity == activity) {
            convertActivityToTranslucent(activity);//将顶层activity转为透明
        } else if (topActivity != null && topActivity.isFinishing() && getSecondActivity() == activity) {//顶层activity即将finish，将次层activity转为透明
            convertActivityToTranslucent(activity);
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Activity topActivity = getTopActivity();
        if (topActivity == activity && !activity.isChangingConfigurations() && !activity.isFinishing()) {
            //顶层activity进入pause状态，且不是由横竖屏切换或者即将finish导致，说明该activity将入栈成为次层activity，则将其转为不透明
            convertActivityFromTranslucent(activity);
        }
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (!activity.isChangingConfigurations()) {
            //activity被回收不是因为横竖屏切换，则从队列移除该activity
            mActivityStack.remove(activity);
        }
    }

    /**
     * 获取顶层activity
     */
    public Activity getTopActivity() {
        if (mActivityStack.size() > 0) {
            return mActivityStack.get(mActivityStack.size() - 1);
        }
        return null;
    }

    /**
     * 获取次层activity
     */
    public Activity getSecondActivity() {
        if (mActivityStack.size() > 1) {
            return mActivityStack.get(mActivityStack.size() - 2);
        }
        return null;
    }

    /**
     * 利用反射将一个activity转为透明
     */
    public void convertActivityToTranslucent(Activity activity) {
        if (activity.isTaskRoot()) return;
        try {
            Class[] classes = Activity.class.getDeclaredClasses();
            Class<?> translucentConversionListenerClazz = null;
            for (Class clazz : classes) {
                if (clazz.getSimpleName().contains("TranslucentConversionListener")) {
                    translucentConversionListenerClazz = clazz;
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Object options = null;
                try {
                    Method getActivityOptions = Activity.class.getDeclaredMethod("getActivityOptions");
                    getActivityOptions.setAccessible(true);
                    options = getActivityOptions.invoke(this);
                } catch (Exception ignored) {
                }
                Method convertToTranslucent = Activity.class.getDeclaredMethod("convertToTranslucent", translucentConversionListenerClazz, ActivityOptions.class);
                convertToTranslucent.setAccessible(true);
                convertToTranslucent.invoke(activity, null, options);
            } else {
                Method convertToTranslucent = Activity.class.getDeclaredMethod("convertToTranslucent", translucentConversionListenerClazz);
                convertToTranslucent.setAccessible(true);
                convertToTranslucent.invoke(activity, new Object[]{null});
            }
        } catch (Throwable ignored) {
        }
    }

    /**
     * 利用反射将一个activity转为不透明
     */
    public void convertActivityFromTranslucent(Activity activity) {
        if (activity.isTaskRoot()) return;
        try {
            Method convertFromTranslucent = Activity.class.getDeclaredMethod("convertFromTranslucent");
            convertFromTranslucent.setAccessible(true);
            convertFromTranslucent.invoke(activity);
        } catch (Throwable t) {
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

}
