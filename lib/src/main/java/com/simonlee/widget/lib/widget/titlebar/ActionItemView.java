package com.simonlee.widget.lib.widget.titlebar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;

/**
 * {@link TitleBar}中专用的菜单ItemView，本质为TextView
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2019-06-11
 */
public class ActionItemView extends AppCompatTextView {

    /**
     * 绑定的ActionItem
     */
    private final ActionItem mActionItem;

    /**
     * 设定的的DrawablePadding值
     * <p>
     * 仅当文本和图片均存在时，才使该padding生效，目的：使item的padding始终保持一致，以提升用户体验
     */
    private int mCompoundDrawablePadding;

    /**
     * 是否跳过绘制，仅在{@link #onMeasure(int, int)}中的使用，用于控制实际的DrawablePadding值
     */
    private boolean skipInvalidate;

    /**
     * 左侧Icon
     */
    private Drawable mIcon;

    /**
     * 补充的padding值
     * <p>
     * 当Item的宽小于高时，通过补充Padding使item不至于过竖长，以提升用户体验
     */
    private int mSupplyPadding;

    public ActionItemView(Context context) {
        super(context);
        this.mActionItem = null;
    }

    public ActionItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mActionItem = null;
    }

    public ActionItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mActionItem = null;
    }

    protected ActionItemView(Context context, @NonNull ActionItem actionItem) {
        super(context);
        this.mActionItem = actionItem;
        //绑定ActionItem
        bindActionItem(context, actionItem);
    }

    /**
     * 绑定ActionItem
     */
    private void bindActionItem(Context context, @NonNull ActionItem actionItem) {
        //字号15
        setTextSize(15);
        //单行显示
        setSingleLine();
        //居中对齐
        setGravity(Gravity.CENTER);
        //布局属性，宽自适应，高填满
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

        //水波纹点击效果
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
        setBackgroundResource(typedValue.resourceId);

        float density_dp = context.getResources().getDisplayMetrics().density;
        //设置padding为7dp
        int dp7 = (int) (density_dp * 7 + 0.5F);
        setPadding(dp7, dp7, dp7, dp7);
        //设置drawablePadding为1dp
        int dp1 = (int) (density_dp + 0.5F);
        setCompoundDrawablePadding(dp1);

        //指定id
        setId(actionItem.id);

        //设置标题
        if (actionItem.title != null) {
            setText(actionItem.title);
        } else if (actionItem.titleResId != 0) {
            setText(actionItem.titleResId);
        }

        //设置左侧图标
        if (actionItem.icon != null) {
            setCompoundDrawablesWithIntrinsicBounds(actionItem.icon, null, null, null);
        } else if (actionItem.iconResId != 0) {
            setCompoundDrawablesWithIntrinsicBounds(actionItem.iconResId, 0, 0, 0);
        }
    }

    /**
     * 获取对应的ActionItem
     */
    protected ActionItem getActionItem() {
        return mActionItem;
    }

    @Override
    public void setCompoundDrawables(@Nullable Drawable left, @Nullable Drawable top, @Nullable Drawable right, @Nullable Drawable bottom) {
        super.setCompoundDrawables(left, top, right, bottom);
        mIcon = left;//左侧Icon
    }

    @Override
    public void setCompoundDrawableTintList(@Nullable ColorStateList tint) {
        //普通图片不使用着色，因为会直接覆盖整个图片
        if (mIcon != null && !(mIcon instanceof BitmapDrawable)) {
            mIcon.setTintList(tint);//VectorDrawable
        }
    }

    @Override
    public void setCompoundDrawablePadding(int pad) {
        mCompoundDrawablePadding = pad;
        //只有文本存在时，drawablePadding才生效
        if (!TextUtils.isEmpty(getText()) || !TextUtils.isEmpty(getHint())) {
            super.setCompoundDrawablePadding(mCompoundDrawablePadding);
        } else {
            super.setCompoundDrawablePadding(0);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        skipInvalidate = true;
        //只有文本存在时，drawablePadding才生效
        if (!TextUtils.isEmpty(getText()) || !TextUtils.isEmpty(getHint())) {
            super.setCompoundDrawablePadding(mCompoundDrawablePadding);
        } else {
            super.setCompoundDrawablePadding(0);
        }
        skipInvalidate = false;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int measuredWidth = getMeasuredWidth();
        final int measuredHeight = getMeasuredHeight();
        if (measuredWidth < measuredHeight || mSupplyPadding > 0) {
            //重新计算新的Padding补充值
            int newSupplyPadding = Math.max(0, (measuredHeight - (measuredWidth - mSupplyPadding * 2)) / 2);
            if (mSupplyPadding != newSupplyPadding) {
                //校正padding
                setPadding(getPaddingLeft() - mSupplyPadding + newSupplyPadding, getPaddingTop(), getPaddingRight() - mSupplyPadding + newSupplyPadding, getPaddingBottom());
                mSupplyPadding = newSupplyPadding;
                //重新测量Item
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        }
    }

    @Override
    public void invalidate() {
        if (!skipInvalidate) {
            super.invalidate();
        }
    }

    @Override
    public void requestLayout() {
        if (!skipInvalidate) {
            super.requestLayout();
        }
    }

}
