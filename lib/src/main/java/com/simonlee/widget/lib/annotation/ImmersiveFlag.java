package com.simonlee.widget.lib.annotation;

import android.support.annotation.IntDef;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.simonlee.widget.lib.annotation.ImmersiveFlag.FLAG_NONE;
import static com.simonlee.widget.lib.annotation.ImmersiveFlag.FLAG_IMMERSIVE;
import static com.simonlee.widget.lib.annotation.ImmersiveFlag.FLAG_IMMERSIVE_STICKY;

/**
 * SystemUI隐藏标记
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2020-07-19
 */

@IntDef({FLAG_NONE, FLAG_IMMERSIVE, FLAG_IMMERSIVE_STICKY})
@Retention(RetentionPolicy.SOURCE)
public @interface ImmersiveFlag {

    /**
     * 隐藏状态栏时，当【页面切换、软键盘弹出、下拉状态栏】时失效
     * <p>
     * 隐藏导航栏时，当【任意触摸】时失效
     */
    int FLAG_NONE = 0;

    /**
     * 隐藏状态栏时，当【页面切换、软键盘弹出、下拉状态栏】时失效
     * <p>
     * 隐藏导航栏时，当【页面切换、软键盘弹出、上滑导航栏】时失效
     */
    int FLAG_IMMERSIVE = View.SYSTEM_UI_FLAG_IMMERSIVE;

    /**
     * 隐藏状态栏/导航栏时，当【页面切换、软键盘弹出】时失效
     */
    int FLAG_IMMERSIVE_STICKY = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

}
