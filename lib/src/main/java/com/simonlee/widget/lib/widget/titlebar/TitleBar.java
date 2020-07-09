package com.simonlee.widget.lib.widget.titlebar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.constraint.ConstraintLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.simonlee.widget.lib.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 自定义TitleBar，由左侧导航View+中部标题栏/输入框+右侧菜单三部分组成
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2019-06-11
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class TitleBar extends ConstraintLayout implements View.OnClickListener {

    /**
     * 标题对齐方式：左
     */
    public static final int GRAVITY_LEFT = 0;

    /**
     * 标题对齐方式：中（相对TitleView的左右View）
     */
    public static final int GRAVITY_CENTER = 1;

    /**
     * 标题对齐方式：右
     */
    public static final int GRAVITY_RIGHT = 2;

    /**
     * 标题对齐方式：中（相对TitleView的Parent，即TitleBar）
     */
    public static final int GRAVITY_CENTER_REAL = 3;

    /**
     * 标题对齐方式
     */
    private int mTitleGravity;

    /**
     * 导航图标（返回、关闭）
     */
    private ActionItemView mNaviActionView;

    /**
     * 主标题
     */
    private TextView mTitleTextView;

    /**
     * 子标题
     */
    private TextView mSubTitleTextView;

    /**
     * 输入框
     */
    private EditText mSearchEditText;

    /**
     * 清除按钮
     */
    private ImageView mClearImageView;

    /**
     * 右侧菜单布局
     */
    private LinearLayout mActionItemLayout;

    /**
     * TitleBar主题色
     */
    private int mPrimaryColor;

    /**
     * TitleBar主题色着色器
     */
    private ColorStateList mPrimaryColorTint;

    /**
     * 搜索框文本变化监听
     */
    private TextWatcher mTextWatcher;

    /**
     * 菜单点击监听
     */
    private OnActionItemClickListener mOnActionItemClickListener;

    public TitleBar(Context context) {
        super(context);
        //初始化TitleBar
        initTitleBar(context, null, 0);
    }

    public TitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        //初始化TitleBar
        initTitleBar(context, attrs, 0);
    }

    public TitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //初始化TitleBar
        initTitleBar(context, attrs, defStyleAttr);
    }

    /**
     * 初始化TitleBar
     */
    private void initTitleBar(Context context, AttributeSet attributeSet, int defStyleAttr) {
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.TitleBar, defStyleAttr, 0);

        //加载默认布局
        inflate(context, R.layout.titlebar_inner, this);

        //获取View
        this.mNaviActionView = findViewById(R.id.titlebar_text_navi);
        this.mTitleTextView = findViewById(R.id.titlebar_text_title);
        this.mSubTitleTextView = findViewById(R.id.titlebar_text_subtitle);
        this.mSearchEditText = findViewById(R.id.titlebar_edit_search);
        this.mClearImageView = findViewById(R.id.titlebar_image_clear);
        this.mActionItemLayout = findViewById(R.id.titlebar_layout_menus);

        //设置导航图标
        if (mNaviActionView != null) {
            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
            mNaviActionView.setBackgroundResource(typedValue.resourceId);//水波纹点击效果
            Drawable naviIcon = typedArray.getDrawable(R.styleable.TitleBar_naviIcon);//导航图标
            if (naviIcon != null) {
                mNaviActionView.setCompoundDrawablesWithIntrinsicBounds(naviIcon, null, null, null);
            }
            mNaviActionView.setText(typedArray.getText(R.styleable.TitleBar_naviText));//导航文本
        }

        //标题对齐方式
        mTitleGravity = typedArray.getInt(R.styleable.TitleBar_title_gravity, GRAVITY_LEFT);
        //标题文本颜色
        int titleTextColor = typedArray.getColor(R.styleable.TitleBar_title_textColor, Color.TRANSPARENT);

        //设置标题
        if (mTitleTextView != null) {
            if (mTitleGravity != GRAVITY_CENTER_REAL) {//当对齐方式为相对TitleBar居中时，在onMeasure时动态调整HorizontalBias
                LayoutParams lp = (LayoutParams) mTitleTextView.getLayoutParams();
                lp.horizontalBias = mTitleGravity / 2F;//除以2刚好对应(左0中0.5右1)
            }
            if (titleTextColor != Color.TRANSPARENT) {
                mTitleTextView.setTextColor(titleTextColor);//标题字体颜色
            }
            mTitleTextView.setText(typedArray.getText(R.styleable.TitleBar_title_text));//标题
        }

        //设置子标题
        if (mSubTitleTextView != null) {
            if (mTitleGravity != GRAVITY_CENTER_REAL) {//当对齐方式为相对TitleBar居中时，在onMeasure时动态调整HorizontalBias
                LayoutParams lp = (LayoutParams) mSubTitleTextView.getLayoutParams();
                lp.horizontalBias = mTitleGravity / 2F;//除以2刚好对应(左0中0.5右1)
            }
            if (titleTextColor != 0) {
                mSubTitleTextView.setTextColor(titleTextColor);//子标题字体颜色
            }
            mSubTitleTextView.setText(typedArray.getText(R.styleable.TitleBar_subtitle_text));//子标题
        }

        //设置搜索框
        if (mSearchEditText != null) {
            mSearchEditText.setHint(typedArray.getText(R.styleable.TitleBar_search_hint));//搜索提示语
            int searchHintColor = typedArray.getColor(R.styleable.TitleBar_search_hintColor, Color.TRANSPARENT);
            if (searchHintColor != Color.TRANSPARENT) {
                mSearchEditText.setHintTextColor(searchHintColor);//提示语字体颜色
            }
            int searchTextColor = typedArray.getColor(R.styleable.TitleBar_search_textColor, Color.TRANSPARENT);
            if (searchTextColor != Color.TRANSPARENT) {
                mSearchEditText.setTextColor(searchTextColor);//搜索文本颜色
            }
            //输入监听
            mSearchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    if (mTextWatcher != null) {
                        mTextWatcher.beforeTextChanged(s, start, count, after);
                    }
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (mTextWatcher != null) {
                        mTextWatcher.onTextChanged(s, start, before, count);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    setVisibility(mClearImageView, TextUtils.isEmpty(s) ? GONE : VISIBLE);
                    if (mTextWatcher != null) {
                        mTextWatcher.afterTextChanged(s);
                    }
                }
            });
        }

        //设置清除按钮
        if (mClearImageView != null) {
            mClearImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSearchEditText != null) {
                        mSearchEditText.setText(null);
                    }
                }
            });
        }

        //未设置背景时，默认与状态栏同色
        if (!typedArray.hasValue(R.styleable.TitleBar_android_background)) {
            int[] colorAattrs = new int[]{android.R.attr.statusBarColor, android.R.attr.colorPrimary};
            TypedArray colorTypedArray = context.getTheme().obtainStyledAttributes(colorAattrs);
            int color = colorTypedArray.getColor(0, Color.TRANSPARENT);//android.R.attr.statusBarColor
            if (color == Color.TRANSPARENT) {
                color = colorTypedArray.getColor(1, Color.TRANSPARENT);//android.R.attr.colorPrimary
            }
            setBackgroundColor(color);
            colorTypedArray.recycle();
        }

        //获取主题色，默认白色
        int primaryColor = typedArray.getColor(R.styleable.TitleBar_primaryColor, Color.WHITE);
        //设置主题色
        setPrimaryColor(primaryColor);

        //设置TitleBar模式（0：标题模式，1：搜索模式）
        if (typedArray.getInt(R.styleable.TitleBar_mode, 0) == 0) {
            changeToTitleMode();//标题模式
        } else {
            changeToSearchMode();//搜索模式
        }

        typedArray.recycle();
        //设置底部阴影效果
        setElevation(4 * context.getResources().getDisplayMetrics().density);
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        //当TitleBar高度为wrap_content时，指定为actionBarSize
        if (params.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            TypedArray typedArray = getContext().obtainStyledAttributes(new int[]{android.R.attr.actionBarSize});
            params.height = typedArray.getDimensionPixelSize(0, ViewGroup.LayoutParams.WRAP_CONTENT);
            typedArray.recycle();
        }
        super.setLayoutParams(params);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (adjustTitleViewHorizontalBias(mTitleTextView) || adjustTitleViewHorizontalBias(mSubTitleTextView)) {
            //调整了标题HorizontalBias，需要重新测量child
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /**
     * 动态调整标题的HorizontalBias，使标题相对TitleBar居中
     *
     * @return 是否进行了调整
     */
    private boolean adjustTitleViewHorizontalBias(@NonNull TextView titleView) {
        if (mTitleGravity != GRAVITY_CENTER_REAL) {
            return false;
        }
        if (titleView.getVisibility() == GONE) {
            return false;
        }
        LayoutParams lp = (LayoutParams) titleView.getLayoutParams();
        int goneLeftMargin = lp.goneLeftMargin;
        int goneRightMargin = lp.goneRightMargin;
        int titleWidth = titleView.getMeasuredWidth();
        int left = mNaviActionView.getVisibility() != GONE ? mNaviActionView.getMeasuredWidth() : goneLeftMargin;
        int right = mActionItemLayout.getVisibility() != GONE ? mActionItemLayout.getMeasuredWidth() : goneRightMargin;
        int surplusWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight() - left - titleWidth - right;
        if (surplusWidth > 0) {
            float bias = Math.max(0, Math.min(1, (surplusWidth + right - left) / (2F * surplusWidth)));
            if (bias != lp.horizontalBias) {
                lp.horizontalBias = bias;
                titleView.setLayoutParams(lp);
                return true;
            }
        } else if (lp.horizontalBias != 0.5F) {
            lp.horizontalBias = 0.5F;
            titleView.setLayoutParams(lp);
            return true;
        }
        return false;
    }

    /**
     * 设置View的可见性
     */
    private void setVisibility(View view, int visibility) {
        if (view != null && view.getVisibility() != visibility) {
            view.setVisibility(visibility);
        }
    }

    /**
     * 判断TextView是否为空文本
     */
    private boolean isEmptyText(TextView textView) {
        return textView == null || TextUtils.isEmpty(textView.getText());
    }

    /**
     * 设置TitleBar主题色，注意不包含搜索框提示文本颜色
     */
    public void setPrimaryColor(@ColorInt int primaryColor) {
        if (mPrimaryColor == primaryColor) {
            return;
        }
        mPrimaryColor = primaryColor;
        //着色器
        mPrimaryColorTint = ColorStateList.valueOf(mPrimaryColor);

        if (mNaviActionView != null) {
            mNaviActionView.setTextColor(mPrimaryColor);//字体颜色
            mNaviActionView.setCompoundDrawableTintList(mPrimaryColorTint);//主题色
        }

        setTitleColor(mPrimaryColor);

        if (mSearchEditText != null) {
            mSearchEditText.setTextColor(mPrimaryColor);
            mSearchEditText.setBackgroundTintList(mPrimaryColorTint);//主题色
        }

        int actionNum = mActionItemLayout != null ? mActionItemLayout.getChildCount() : 0;
        for (int index = 0; index < actionNum; index++) {
            ActionItemView actionItemView = (ActionItemView) mActionItemLayout.getChildAt(index);
            actionItemView.setTextColor(mPrimaryColor);
            actionItemView.setCompoundDrawableTintList(mPrimaryColorTint);
        }
    }

    /**
     * 设置导航按钮的图标
     */
    public void setNaviIcon(Drawable naviIcon) {
        if (mNaviActionView != null) {
            mNaviActionView.setCompoundDrawablesWithIntrinsicBounds(naviIcon, null, null, null);
        }
    }

    /**
     * 设置导航按钮的图标
     */
    public void setNaviIcon(@DrawableRes int resId) {
        if (mNaviActionView != null) {
            mNaviActionView.setCompoundDrawablesWithIntrinsicBounds(resId, 0, 0, 0);
        }
    }

    /**
     * 设置导航按钮文本
     */
    public void setNaviText(CharSequence text) {
        if (mNaviActionView != null) {
            mNaviActionView.setText(text);
        }
    }

    /**
     * 设置导航按钮文本
     */
    public void setNaviText(@StringRes int resId) {
        if (mNaviActionView != null) {
            mNaviActionView.setText(resId);
        }
    }

    /**
     * 设置导航按钮的可见性
     */
    public void setNaviVisibility(@Visibility int visibility) {
        setVisibility(mNaviActionView, visibility);
    }

    /**
     * 设置导航键的监听 case: R.id.titlebar_text_navi
     */
    public void setNaviOnClickListener(@Nullable View.OnClickListener listener) {
        if (mNaviActionView != null) {
            mNaviActionView.setOnClickListener(listener);
        }
    }

    /**
     * 切换为标题模式
     */
    public void changeToTitleMode() {
        setVisibility(mTitleTextView, VISIBLE);
        setVisibility(mSubTitleTextView, isEmptyText(mSubTitleTextView) ? GONE : VISIBLE);
        setVisibility(mSearchEditText, GONE);
        setVisibility(mClearImageView, GONE);
    }

    /**
     * 切换为搜索模式
     */
    public void changeToSearchMode() {
        setVisibility(mTitleTextView, GONE);
        setVisibility(mSubTitleTextView, GONE);
        setVisibility(mSearchEditText, VISIBLE);
        setVisibility(mClearImageView, VISIBLE);
    }

    /**
     * 设置标题右侧图标
     */
    public void setTitleIcon(Drawable icon) {
        if (mTitleTextView != null) {
            mTitleTextView.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
        }
    }

    /**
     * 设置标题右侧图标
     */
    public void setTitleIcon(@DrawableRes int iconRes) {
        if (mTitleTextView != null) {
            mTitleTextView.setCompoundDrawablesWithIntrinsicBounds(0, iconRes, 0, 0);
        }
    }

    /**
     * 设置标题文本
     */
    public void setTitle(CharSequence title) {
        if (mTitleTextView != null) {
            mTitleTextView.setText(title);
        }
    }

    /**
     * 设置标题文本
     */
    public void setTitle(@StringRes int resId) {
        if (mTitleTextView != null) {
            mTitleTextView.setText(resId);
        }
    }

    /**
     * 设置子标题文本
     */
    public void setSubTitle(CharSequence subTitle) {
        if (mSubTitleTextView != null) {
            mSubTitleTextView.setText(subTitle);
            if (mTitleTextView != null && mTitleTextView.getVisibility() == VISIBLE) {
                setVisibility(mSubTitleTextView, TextUtils.isEmpty(mSubTitleTextView.getText()) ? GONE : VISIBLE);
            }
        }
    }

    /**
     * 设置子标题文本
     */
    public void setSubTitle(@StringRes int resId) {
        if (mSubTitleTextView != null) {
            mSubTitleTextView.setText(resId);
            if (mTitleTextView != null && mTitleTextView.getVisibility() == VISIBLE) {
                setVisibility(mSubTitleTextView, TextUtils.isEmpty(mSubTitleTextView.getText()) ? GONE : VISIBLE);
            }
        }
    }

    /**
     * 设置标题字色
     */
    public void setTitleColor(@ColorInt int color) {
        if (mTitleTextView != null) {
            mTitleTextView.setTextColor(color);
        }
        if (mSubTitleTextView != null) {
            mSubTitleTextView.setTextColor(color);
        }
    }

    /**
     * 设置标题对齐方式（左0：中1：右2）
     */
    public void setTitleGravity(@Gravity int gravity) {
        mTitleGravity = gravity;
        if (mTitleGravity == GRAVITY_CENTER_REAL) {
            //当对齐方式为相对TitleBar居中时，需要动态调整HorizontalBias
            adjustTitleViewHorizontalBias(mTitleTextView);
            adjustTitleViewHorizontalBias(mSubTitleTextView);
        } else {
            //设置标题
            if (mTitleTextView != null) {
                LayoutParams lp = (LayoutParams) mTitleTextView.getLayoutParams();
                lp.horizontalBias = gravity / 2F;//除以2刚好对应(左0中0.5右1)
                mTitleTextView.setLayoutParams(lp);
            }
            //设置子标题
            if (mSubTitleTextView != null) {
                LayoutParams lp = (LayoutParams) mSubTitleTextView.getLayoutParams();
                lp.horizontalBias = gravity / 2F;//除以2刚好对应(左0中0.5右1)
                mSubTitleTextView.setLayoutParams(lp);
            }
        }
    }

    /**
     * 设置搜索提示文本
     */
    public void setSearchHint(CharSequence hint) {
        if (mSearchEditText != null) {
            mSearchEditText.setHint(hint);
        }
    }

    /**
     * 设置搜索提示字色
     */
    public void setSearchHintColor(@ColorInt int color) {
        if (mSearchEditText != null) {
            mSearchEditText.setHintTextColor(color);
        }
    }

    /**
     * 设置搜索字色
     */
    public void setSearchTextColor(@ColorInt int color) {
        if (mSearchEditText != null) {
            mSearchEditText.setTextColor(color);
        }
    }

    /**
     * 设置搜索框文本变化监听
     */
    public void setOnTextChangedListener(@Nullable TextWatcher listener) {
        this.mTextWatcher = listener;
    }

    /**
     * 设置搜索框Action事件监听
     */
    public void setOnEditorActionListener(@Nullable TextView.OnEditorActionListener listener) {
        if (mSearchEditText != null) {
            mSearchEditText.setOnEditorActionListener(listener);
        }
    }

    /**
     * 设置操作菜单点击监听
     */
    public void setOnActionItemClickListener(@Nullable OnActionItemClickListener listener) {
        this.mOnActionItemClickListener = listener;
    }

    /**
     * 右侧菜单添加一个Item
     */
    public void addActionItem(ActionItem actionItem) {
        addActionItem(actionItem, -1);
    }

    /**
     * 右侧菜单添加一个Item
     */
    public void addActionItem(ActionItem actionItem, int index) {
        ActionItemView actionItemView = actionItem.getActionItemView(getContext());
        if (actionItemView.getParent() != null) {
            throw new RuntimeException("This ActionItem has been added!");
        }
        actionItemView.setTextColor(mPrimaryColor);
        actionItemView.setCompoundDrawableTintList(mPrimaryColorTint);
        actionItemView.setOnClickListener(this);
        if (index > mActionItemLayout.getChildCount()) {
            index = mActionItemLayout.getChildCount();
        }
        mActionItemLayout.addView(actionItemView, index);
        if (mActionItemLayout.getChildCount() > 0) {
            setVisibility(mActionItemLayout, VISIBLE);
        }
    }

    /**
     * 右侧菜单移除对应Item
     */
    public void removeActionItem(ActionItem actionItem) {
        mActionItemLayout.removeView(actionItem.actionItemView);
        if (mActionItemLayout.getChildCount() < 1) {
            setVisibility(mActionItemLayout, GONE);
        }
    }

    /**
     * 右侧菜单移除对应Item
     */
    public void removeActionItem(int index) {
        if (index >= 0 && index < mActionItemLayout.getChildCount()) {
            mActionItemLayout.removeViewAt(index);
            if (mActionItemLayout.getChildCount() < 1) {
                setVisibility(mActionItemLayout, GONE);
            }
        }
    }

    /**
     * 移除所有操作菜单
     */
    public void clearActionItem() {
        mActionItemLayout.removeAllViews();
        setVisibility(mActionItemLayout, GONE);
    }

    @Override
    public void onClick(View v) {
        if (mOnActionItemClickListener != null && v instanceof ActionItemView) {
            mOnActionItemClickListener.onActionItemClick(((ActionItemView) v).getAcionItem());
        }
    }

    /**
     * 操作菜单点击监听
     */
    public interface OnActionItemClickListener {
        /**
         * 操作菜单点击回调
         */
        void onActionItemClick(ActionItem actionItem);
    }

    @IntDef({VISIBLE, INVISIBLE, GONE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Visibility {
    }

    @IntDef({GRAVITY_LEFT, GRAVITY_CENTER, GRAVITY_RIGHT, GRAVITY_CENTER_REAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Gravity {
    }

}
