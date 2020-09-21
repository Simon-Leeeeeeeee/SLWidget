package cn.simonlee.widgetdemo.application;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.os.Looper;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.widget.Toast;

import com.simonlee.widget.lib.application.ApplicationProxy;
import com.simonlee.widget.lib.utils.LogUtil;
import com.simonlee.widget.lib.utils.UncaughtExceptionHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 未捕获异常处理
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2019-10-25
 */
@SuppressWarnings("WeakerAccess")
public class CrashHandler extends UncaughtExceptionHandler {

    /**
     * 崩溃时Toast显示时长，单位毫秒
     */
    private static final int DEFAULT_TOAST_DURATION = 3000;

    /**
     * 异常时显示的Toast信息
     */
    private static final String MSG_EXCEPTION_TOAST = "很抱歉，SLWidget出现异常，即将退出";

    /**
     * 单例，仅能注册一次
     */
    private static CrashHandler mInstance;

    private CrashHandler(@NonNull String broadcastPermission) {
        super(ApplicationProxy.getApplication(), broadcastPermission);
    }

    /**
     * 未捕获异常处理，单例模式，仅能注册一次
     *
     * @param broadcastPermission 私有广播权限，防止三方应用干扰
     */
    public static void register(@NonNull String broadcastPermission) {
        if (mInstance == null) {
            synchronized (CrashHandler.class) {
                if (mInstance == null) {
                    mInstance = new CrashHandler(broadcastPermission);
                }
            }
        }
    }

    @Override
    protected void finishCurProcessActivitys() {
        //关闭当前进程所有Activity
        ActivityHolder.getInstance().finishAllActivitys();
    }

    @Override
    protected void handleUncaughtException(Thread thread, Throwable throwable) {
        LogUtil.e(getClass().getSimpleName(), null, throwable);
        //Toast提示异常
        toastWhenException();
        //记录当前时间
        long currentTimeMillis = System.currentTimeMillis();
        //收集崩溃信息
        String crashInfo = collectCrashInfo(currentTimeMillis, throwable);
        //保存崩溃信息到文件中
        saveCrashInfoToFile(crashInfo, currentTimeMillis);
        //线程休眠，使Toast显示足够时间
        sleepThread(currentTimeMillis);
        //重启APP
//        restartApplication();
    }

    /**
     * Toast提示异常
     */
    private void toastWhenException() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(ApplicationProxy.getApplication(), MSG_EXCEPTION_TOAST, Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }).start();
    }

    /**
     * 收集崩溃信息
     */
    private String collectCrashInfo(long currentTimeMillis, Throwable throwable) {
        JSONObject crashInfo = new JSONObject();
        putJsonKV(crashInfo, "TIME", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date(currentTimeMillis)));// 异常时间
        putJsonKV(crashInfo, "BRAND", Build.BRAND);// 设备品牌
        putJsonKV(crashInfo, "MODEL", Build.MODEL);// 设备机型
        putJsonKV(crashInfo, "SDK", Build.VERSION.SDK_INT);// SDK版本
        putJsonKV(crashInfo, "RELEASE", Build.VERSION.RELEASE);// 固件版本
        putJsonKV(crashInfo, "INCREMENTAL", Build.VERSION.INCREMENTAL);// 基带版本
        putJsonKV(crashInfo, "VERSION", ApplicationProxy.getVersionName());// 应用版本号
        putJsonKV(crashInfo, "PROCESS", ApplicationProxy.getCurProcessName());// 当前进程名
        putJsonKV(crashInfo, "RAM", getMemoryInfo(ApplicationProxy.getApplication()));//内存信息
        putJsonKV(crashInfo, "ROM", getStorageInfo(ApplicationProxy.getApplication()));//存储信息
        putJsonKV(crashInfo, "Exception", getThrowableInfo(throwable));// 异常信息
        return crashInfo.toString();
    }

    /**
     * 保存崩溃信息到文件
     */
    private void saveCrashInfoToFile(String crashInfo, long currentTimeMillis) {
        if (TextUtils.isEmpty(crashInfo)) {
            return;
        }
        File crashFile = getCrashFile(currentTimeMillis);
        if (crashFile == null) {
            return;
        }
        try (FileOutputStream outputStream = new FileOutputStream(crashFile)) {
            outputStream.write(crashInfo.getBytes());
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 休眠当前线程，使Toast显示足够时间
     */
    private void sleepThread(long previousTimeMillis) {
        long sleepMillis = DEFAULT_TOAST_DURATION - (System.currentTimeMillis() - previousTimeMillis);
        if (sleepMillis <= 0) {
            return;
        }
        try {
            Thread.sleep(sleepMillis);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * 获取异常信息
     */
    private String getThrowableInfo(Throwable throwable) {
        Writer info = new StringWriter();
        PrintWriter printWriter = new PrintWriter(info);
        throwable.printStackTrace(printWriter);
        printWriter.flush();
        return info.toString();
    }

    /**
     * 获取储存空间信息
     */
    private String getStorageInfo(Context context) {
        StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
        //每个扇区大小
        long blockSize = statFs.getBlockSizeLong();
        //扇区总数
        long totalCount = statFs.getBlockCountLong();
        //可用扇区数量
        long availCount = statFs.getAvailableBlocksLong();
        return Formatter.formatFileSize(context, blockSize * (totalCount - availCount))//已用存储空间
                + "/"
                + Formatter.formatFileSize(context, blockSize * totalCount);//总存储空间
    }

    /**
     * 获取内存信息
     */
    private String getMemoryInfo(Context context) {
        StringBuilder sb = new StringBuilder();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);
            sb.append("Device Low Memory:");
            //是否低内存
            sb.append(memoryInfo.lowMemory);
            sb.append(", Device Memory:");
            //设备已用物理内存
            sb.append(Formatter.formatFileSize(context, memoryInfo.totalMem - memoryInfo.availMem));
            sb.append('/');
            //设备最大物理内存
            sb.append(Formatter.formatFileSize(context, memoryInfo.totalMem));
            sb.append(", ");
        }
        sb.append("Dalvik Heap:");
        //进程已用Dalvik堆内存
        sb.append(Formatter.formatFileSize(context, Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
        sb.append('/');
        //进程最大可用Dalvik堆内存
        sb.append(Formatter.formatFileSize(context, Runtime.getRuntime().maxMemory()));
        sb.append(", Native Heap:");
        //进程已用Native堆内存
        sb.append(Formatter.formatFileSize(context, Debug.getNativeHeapAllocatedSize()));
        return sb.toString();
    }

    /**
     * 获取崩溃日志文件
     *
     * @param currentTimeMillis 当前系统时间
     */
    private File getCrashFile(long currentTimeMillis) {
        //获取应用文档目录 sdcard/Android/data/[packageName]/files/Documents
        File crashDirectory = getCrashDirectory();
        if (crashDirectory == null) {
            return null;
        }
        //如果不是文件夹
        if (!crashDirectory.isDirectory()) {
            //若存在，则先删除
            if (crashDirectory.exists() && !crashDirectory.delete()) {
                return null;
            }
            //创建文件夹
            if (!crashDirectory.mkdirs()) {
                return null;
            }
        }
        //转换时间戳为字符串
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault()).format(new Date(currentTimeMillis));
        return new File(crashDirectory.getPath() + File.separator + "crash_" + timestamp + ".txt");
    }

    /**
     * 获取崩溃日志文件目录
     */
    private File getCrashDirectory() {
        //获取应用文档目录 sdcard/Android/data/[packageName]/files/Documents
        File documentFile = ApplicationProxy.getApplication().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (documentFile == null) {
            return null;
        }
        //获取崩溃日志目录 sdcard/Android/data/[packageName]/files/Documents/crash
        return new File(documentFile.getPath() + File.separator + "crash");
    }

    /**
     * 保存json键值对
     */
    private void putJsonKV(@NonNull JSONObject jsonObject, @NonNull String key, @Nullable Object value) {
        try {
            jsonObject.put(key, value == null ? "null" : value);
        } catch (JSONException ignored) {
        }
    }

}
