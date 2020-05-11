package cn.simonlee.widgetdemo.application;

import android.app.Application;
import android.content.Context;

import com.simonlee.widget.lib.application.ApplicationProxy;

/**
 * Application基类
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2019-07-17
 */
public class BaseApplication extends Application {

    /**
     * 私有广播权限，防止接收到非自身广播
     */
    public static final String PERMISSION_BROADCAST = "com.simonlee.widgetdemo.broadcast.permission";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //初始化
        ApplicationProxy.bind(this);
        //注册Activity回调
        ActivityHolder.register();
        //注册异常捕获
        CrashHandler.register( PERMISSION_BROADCAST);
    }

}
