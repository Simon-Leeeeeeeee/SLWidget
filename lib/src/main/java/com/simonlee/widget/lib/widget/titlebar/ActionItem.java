package com.simonlee.widget.lib.widget.titlebar;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.StringRes;

/**
 * {@link TitleBar}中专用的菜单Item
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2019-06-11
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ActionItem {

    /**
     * item的id
     */
    protected final int id;

    /**
     * item的标题资源id
     */
    int titleResId;

    /**
     * item的图标资源id
     */
    int iconResId;

    /**
     * item的标题文本
     */
    CharSequence title;

    /**
     * item的图标
     */
    Drawable icon;

    /**
     * item对应的View
     */
    ActionItemView actionItemView;

    /**
     * 点击事件
     */
    View.OnClickListener onClickListener;

    /**
     * 构造方法
     *
     * @param titleResId 标题资源id
     * @param iconResId  图标资源id
     */
    public ActionItem(@StringRes int titleResId, @DrawableRes int iconResId) {
        this(View.NO_ID, titleResId, iconResId);
    }

    /**
     * 构造方法
     *
     * @param id         item的id
     * @param titleResId 标题资源id
     * @param iconResId  图标资源id
     */
    public ActionItem(@IdRes int id, @StringRes int titleResId, @DrawableRes int iconResId) {
        this.id = id;
        this.titleResId = titleResId;
        this.iconResId = iconResId;
    }

    /**
     * 构造方法
     *
     * @param title 标题文本
     */
    public ActionItem(CharSequence title) {
        this(View.NO_ID, title, null);
    }

    /**
     * 构造方法
     *
     * @param icon 图标
     */
    public ActionItem(Drawable icon) {
        this(View.NO_ID, null, icon);
    }

    /**
     * 构造方法
     *
     * @param title 标题文本
     * @param icon  图标
     */
    public ActionItem(CharSequence title, Drawable icon) {
        this(View.NO_ID, title, icon);
    }

    /**
     * 构造方法
     *
     * @param id    item的id
     * @param title 标题文本
     */
    public ActionItem(@IdRes int id, CharSequence title) {
        this(id, title, null);
    }

    /**
     * 构造方法
     *
     * @param id   item的id
     * @param icon 图标
     */
    public ActionItem(@IdRes int id, Drawable icon) {
        this(id, null, icon);
    }

    /**
     * 构造方法
     *
     * @param id    item的id
     * @param title 标题文本
     * @param icon  图标
     */
    public ActionItem(@IdRes int id, CharSequence title, Drawable icon) {
        this.id = id;
        this.title = title;
        this.icon = icon;
    }

    /**
     * 返回item对应的View
     */
    protected ActionItemView getActionItemView(Context context) {
        if (actionItemView == null) {
            actionItemView = new ActionItemView(context, this);
        }
        return actionItemView;
    }

    /**
     * item是否已被添加
     */
    public boolean isAdded() {
        if (actionItemView != null) {
            return actionItemView.getParent() != null;
        }
        return false;
    }

    /**
     * 设置图标
     */
    public void setIcon(@DrawableRes int iconRes) {
        if (actionItemView != null) {
            actionItemView.setCompoundDrawablesWithIntrinsicBounds(iconRes, 0, 0, 0);
        }
        this.icon = null;
        this.iconResId = iconRes;
    }

    /**
     * 设置图标
     */
    public void setIcon(Drawable icon) {
        if (actionItemView != null) {
            actionItemView.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        }
        this.icon = icon;
        this.iconResId = 0;
    }

    public int getId() {
        return id;
    }

    public CharSequence getTitle() {
        if (actionItemView != null) {
            return actionItemView.getText();
        } else if (titleResId == 0) {
            return title;
        } else {
            return "String resource ID #0x" + Integer.toHexString(titleResId);
        }
    }

    /**
     * 设置标题
     */
    public void setTitle(@StringRes int titleRes) {
        if (actionItemView != null) {
            actionItemView.setText(titleRes);
        }
        this.title = null;
        this.titleResId = titleRes;
    }

    /**
     * 设置标题
     */
    public void setTitle(CharSequence title) {
        if (actionItemView != null) {
            actionItemView.setText(title);
        }
        this.title = title;
        this.titleResId = 0;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

}
