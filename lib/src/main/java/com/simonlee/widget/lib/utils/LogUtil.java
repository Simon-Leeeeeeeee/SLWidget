package com.simonlee.widget.lib.utils;

import android.support.annotation.NonNull;
import android.util.Log;

import com.simonlee.widget.lib.application.ApplicationProxy;

/**
 * 日志工具
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2019-07-02
 */
@SuppressWarnings("unused")
public class LogUtil {

    /**
     * 日志级别：V
     */
    private static final int LEVEL_V = 0;

    /**
     * 日志级别：D
     */
    private static final int LEVEL_D = 1;

    /**
     * 日志级别：I
     */
    private static final int LEVEL_I = 2;

    /**
     * 日志级别：W
     */
    private static final int LEVEL_W = 3;

    /**
     * 日志级别：E
     */
    private static final int LEVEL_E = 4;

    /**
     * 是否为Debug模式
     */
    private static final boolean DEBUG = ApplicationProxy.isDebug();

    /**
     * 每条日志的最大字符串长度（防止过长不显示）
     */
    private static final int LOG_MAX_LENGTH = 960;

    /**
     * 日志TAG
     */
    private static String TAG = "SLWidget";

    /**
     * 设置日志TAG
     */
    public static void setTag(@NonNull String tag) {
        TAG = tag;
    }

    public static void v(String msg) {
        print(null, msg, LEVEL_V);
    }

    public static void v(Throwable throwable) {
        print(null, Log.getStackTraceString(throwable), LEVEL_V);
    }

    public static void v(String flag, String msg) {
        print(flag, msg, LEVEL_V);
    }

    public static void v(String flag, Throwable throwable) {
        print(flag, Log.getStackTraceString(throwable), LEVEL_V);
    }

    public static void v(String flag, String msg, Throwable throwable) {
        print(flag, msg + '\n' + Log.getStackTraceString(throwable), LEVEL_V);
    }

    public static void d(String msg) {
        print(null, msg, LEVEL_D);
    }

    public static void d(Throwable throwable) {
        print(null, Log.getStackTraceString(throwable), LEVEL_D);
    }

    public static void d(String flag, String msg) {
        print(flag, msg, LEVEL_D);
    }

    public static void d(String flag, Throwable throwable) {
        print(flag, Log.getStackTraceString(throwable), LEVEL_D);
    }

    public static void d(String flag, String msg, Throwable throwable) {
        print(flag, msg + '\n' + Log.getStackTraceString(throwable), LEVEL_D);
    }

    public static void i(String msg) {
        print(null, msg, LEVEL_I);
    }

    public static void i(Throwable throwable) {
        print(null, Log.getStackTraceString(throwable), LEVEL_I);
    }

    public static void i(String flag, String msg) {
        print(flag, msg, LEVEL_I);
    }

    public static void i(String flag, Throwable throwable) {
        print(flag, Log.getStackTraceString(throwable), LEVEL_I);
    }

    public static void i(String flag, String msg, Throwable throwable) {
        print(flag, msg + '\n' + Log.getStackTraceString(throwable), LEVEL_I);
    }

    public static void w(String msg) {
        print(null, msg, LEVEL_W);
    }

    public static void w(Throwable throwable) {
        print(null, Log.getStackTraceString(throwable), LEVEL_W);
    }

    public static void w(String flag, String msg) {
        print(flag, msg, LEVEL_W);
    }

    public static void w(String flag, Throwable throwable) {
        print(flag, Log.getStackTraceString(throwable), LEVEL_W);
    }

    public static void w(String flag, String msg, Throwable throwable) {
        print(flag, msg + '\n' + Log.getStackTraceString(throwable), LEVEL_W);
    }

    public static void e(String msg) {
        print(null, msg, LEVEL_E);
    }

    public static void e(Throwable throwable) {
        print(null, Log.getStackTraceString(throwable), LEVEL_E);
    }

    public static void e(String flag, String msg) {
        print(flag, msg, LEVEL_E);
    }

    public static void e(String flag, Throwable throwable) {
        print(flag, Log.getStackTraceString(throwable), LEVEL_E);
    }

    public static void e(String flag, String msg, Throwable throwable) {
        print(flag, msg + '\n' + Log.getStackTraceString(throwable), LEVEL_E);
    }

    /**
     * 打印日志
     *
     * @param flag     日志标记，建议为对应的Class名
     * @param msg      消息
     * @param logLevel 日志级别
     */
    private static void print(String flag, String msg, int logLevel) {
        if (DEBUG) {
            msg = makeMessage(flag, msg);
            while (msg.length() > LOG_MAX_LENGTH) {
                log(msg.substring(0, LOG_MAX_LENGTH), logLevel);
                msg = msg.substring(LOG_MAX_LENGTH);
            }
            log(msg, logLevel);
        }
    }

    /**
     * 输出日志
     *
     * @param msg      消息
     * @param logLevel 日志级别
     */
    private static void log(String msg, int logLevel) {
        switch (logLevel) {
            case LEVEL_E: {
                Log.e(TAG, msg);
                break;
            }
            case LEVEL_W: {
                Log.w(TAG, msg);
                break;
            }
            case LEVEL_I: {
                Log.i(TAG, msg);
                break;
            }
            case LEVEL_D: {
                Log.d(TAG, msg);
                break;
            }
            case LEVEL_V:
            default: {
                Log.v(TAG, msg);
                break;
            }
        }
    }

    /**
     * 获取包含Tag、类名、方法名、行号的日志
     */
    private static String makeMessage(String flag, String msg) {
        /*
         * getStackTrace获取程序运行的堆栈信息
         * 0对应的是makeMessage方法块
         * 1对应的是调用makeMessage的方法块:print
         * 2对应的是调用print的方法块:v/d/i/w/e
         * 3对应的即为打印日志所在方法块
         * */
        StackTraceElement[] stackTraceElements = new Throwable().getStackTrace();
        StackTraceElement stackTrace = (stackTraceElements != null && stackTraceElements.length > 3) ? stackTraceElements[3] : null;
        if (stackTrace == null) {
            return '[' + flag + "][no stackTrace info]" + msg;
        } else {
            String className = stackTrace.getClassName();
            String methodName = stackTrace.getMethodName();
            int lineNumber = stackTrace.getLineNumber();
            return '[' + flag + "] at " + className + '.' + methodName + "()<" + lineNumber + "> " + msg;
        }
    }

}
