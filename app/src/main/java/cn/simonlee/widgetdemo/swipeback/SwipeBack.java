package cn.simonlee.widgetdemo.swipeback;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;

import cn.simonlee.widget.swipeback.SwipeBackHelper;

/**
 * 针对部分机型出现{@link #convertToTranslucent()}仅生效一次的问题，采用延迟强制解决
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2020-03-27
 */
public class SwipeBack extends SwipeBackHelper implements Handler.Callback {

    /**
     * 用于延时处理
     */
    private final Handler mHandler;

    /**
     * 开始透明转换时间，单位纳秒
     */
    private long startTime;

    /**
     * 透明转换完成花费时间，单位纳秒
     */
    private long spendTime;

    public SwipeBack(@NonNull Activity activity) {
        super(activity);
        mHandler = new Handler(this);
    }

    @Override
    protected void convertToTranslucent() {
        super.convertToTranslucent();
        if (!isTranslucentCompleted()) {
            //窗口透明转换未完成，记录时间
            startTime = System.nanoTime();
            if (spendTime > 0) {
                //开启延时任务，spendTime后强制标记为true
                mHandler.sendEmptyMessageDelayed(0, spendTime / 1000000);
            }
        }
    }

    @Override
    protected void convertFromTranslucent() {
        //窗口将转不透明，结束计时，并移除延时任务
        startTime = 0;
        mHandler.removeMessages(0);
        super.convertFromTranslucent();
    }

    @Override
    protected void markTranslucentCompleted(boolean completed) {
        //窗口转为透明，且延时任务开启
        if (completed && startTime > 0) {
            //计算转换耗时，移除延时任务
            spendTime = Math.max(spendTime, System.nanoTime() - startTime);
            startTime = 0;
            mHandler.removeMessages(0);
        }
        super.markTranslucentCompleted(completed);
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (startTime > 0) {
            //强制标记转换完成
            super.markTranslucentCompleted(true);
        }
        return true;
    }

}
