package com.simonlee.widget.lib.utils;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Process;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

/**
 * 未捕获异常处理工具抽象类，当异常发生时，通过全局广播主动结束所有进程
 * <p>
 * 注意{@link #killCurProcess()}方法可能需要复写
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2019-02-07
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class UncaughtExceptionHandler {

    /**
     * 杀进程广播的后缀，拼接包名使用
     */
    private static final String SUFFIX_ACTION_KILLPROCESS = ".broadcast.action.killProcess";

    /**
     * Application实例
     */
    private final Application mApplication;

    /**
     * 私有广播权限，防止三方应用干扰
     */
    private final String mBroadcastPermission;

    /**
     * 杀进程广播Action
     */
    private final String mProcessKillAction;

    /**
     * 结束当前进程所有Activity
     */
    protected abstract void finishCurProcessActivitys();

    /**
     * 处理未捕获的异常
     *
     * @param thread    线程
     * @param throwable 异常
     */
    protected abstract void handleUncaughtException(Thread thread, Throwable throwable);

    /**
     * @param application         Application实例
     * @param broadcastPermission 私有广播权限，防止三方应用干扰
     */
    protected UncaughtExceptionHandler(@NonNull Application application, @NonNull String broadcastPermission) {
        //Application实例
        this.mApplication = application;
        //广播权限
        this.mBroadcastPermission = broadcastPermission;
        //杀进程广播Action
        this.mProcessKillAction = mApplication.getPackageName() + SUFFIX_ACTION_KILLPROCESS;
        //拼接杀进程广播
        IntentFilter intentFilter = new IntentFilter(mProcessKillAction);
        //注册杀进程广播，设置权限
        mApplication.registerReceiver(getKillProcessReceiver(), intentFilter, mBroadcastPermission, null);
        //开启异常处理
        Thread.setDefaultUncaughtExceptionHandler(getUncaughtExceptionHandler());
    }

    /**
     * 返回杀进程广播实例
     */
    private BroadcastReceiver getKillProcessReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //杀死当前进程
                killCurProcess();
            }
        };
    }

    /**
     * 返回异常处理实例
     */
    private Thread.UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                //处理未捕获的异常
                handleUncaughtException(thread, throwable);
                //发送杀进程广播
                mApplication.sendBroadcast(new Intent(mProcessKillAction), mBroadcastPermission);
                //杀死当前进程
                killCurProcess();
            }
        };
    }

    /**
     * 杀死当前进程
     * <p>
     * 注意：要考虑应用存在多进程以及后台服务的情况，若清理不完全，可能导致应用自动重启
     */
    @CallSuper
    protected void killCurProcess() {
        //结束当前进程所有Activity（防止自动重启）
        finishCurProcessActivitys();
        //杀死当前进程
        Process.killProcess(Process.myPid());
    }

    /**
     * 发生异常后调用，用于重启应用
     * <p>
     * 注意：要在{@link #handleUncaughtException(Thread, Throwable)}中调用
     */
    protected final void restartApplication() {
        Intent intent = mApplication.getPackageManager().getLaunchIntentForPackage(mApplication.getPackageName());
        //noinspection ConstantConditions
        intent.setPackage(null);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        AlarmManager alarmManager = (AlarmManager) mApplication.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis(), PendingIntent.getActivity(mApplication, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
    }

}
