package cn.simonlee.widget.badgeview;

import android.graphics.drawable.Drawable;

/**
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-05-25
 */
public interface Badge {

    /**
     * 浮标相对目标View左对齐
     */
    int GRAVITY_LEFT = 1;
    /**
     * 浮标相对目标View顶对齐
     */
    int GRAVITY_TOP = 2;
    /**
     * 浮标相对目标View右对齐
     */
    int GRAVITY_RIGHT = 4;
    /**
     * 浮标相对目标View底对齐
     */
    int GRAVITY_BOTTOM = 8;
    /**
     * 浮标相对目标View居中
     */
    int GRAVITY_CENTER = 16;

    /**
     * 获取浮标文本
     */
    String getBadgeText();

    /**
     * 设置浮标文本
     *
     * @param badgeText 为null时不显示，长度为0时显示小圆点
     */
    void setBadgeText(String badgeText);

    /**
     * 设置浮标字体颜色
     */
    void setBadgeTextColor(int color);

    /**
     * 设置浮标字体大小，单位SP
     */
    void setBadgeTextSize(float size);

    /**
     * 设置是否字体加粗
     */
    void setBadgeBoldText(boolean boldEnable);

    /**
     * 设置浮标背景
     */
    void setBadgeBackground(Drawable drawable);

    /**
     * 设置浮标内边距，单位DP
     */
    void setBadgePadding(float padding);

    /**
     * 设置浮标内边距，单位DP
     */
    void setBadgePadding(Float paddingLeft, Float paddingTop, Float paddingRight, Float paddingBottom);

    /**
     * 设置浮标外边距，单位DP
     */
    void setBadgeMargin(float margin);

    /**
     * 设置浮标外边距，单位DP
     */
    void setBadgeMargin(Float marginLeft, Float marginTop, Float marginRight, Float marginBottom);

    /**
     * 设置浮标对齐方式
     */
    void setBadgeGravity(int gravity);

}
