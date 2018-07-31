package cn.simonlee.widgetdemo;

import cn.simonlee.widget.swipeback.ActivityStackManager;

/**
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-07-30
 */
public class Application extends android.app.Application {

//    private static Application mApplication;

    @Override
    public void onCreate() {
        super.onCreate();
//        mApplication = this;
        registerActivityLifecycleCallbacks(new ActivityStackManager());
    }

//    public static Application getInstance() {
//        return mApplication;
//    }

}
