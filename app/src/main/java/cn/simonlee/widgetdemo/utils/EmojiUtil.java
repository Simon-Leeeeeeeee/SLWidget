package cn.simonlee.widgetdemo.utils;

import android.content.res.Configuration;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.simonlee.widget.lib.activity.BaseActivity;
import com.simonlee.widget.lib.utils.SharedPreferencesProxy;

import androidx.annotation.LayoutRes;

/**
 * Emoji工具
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2020-06-28
 */
public class EmojiUtil {

    /**
     * SharedPreferences存储Emoji高度值的key
     */
    private static final String KEY_SP_EMOJI_HEIGHT = "KEY_SP_EMOJI_HEIGHT";

    /**
     * SharedPreferences存储横屏时Emoji高度值的key
     */
    private static final String KEY_SP_EMOJI_HEIGHT_LANDSCAPE = "KEY_SP_EMOJI_HEIGHT_LANDSCAPE";

    /**
     * SharedPreferences工具
     */
    private final SharedPreferencesProxy mSharedPreferencesProxy = new SharedPreferencesProxy();

    private final BaseActivity mActivity;

    /**
     * 当前屏幕方向
     */
    private final int mOrientation;

    /**
     * Emoji布局
     */
    private final View mEmojiView;

    /**
     * Emoji高度
     */
    private int mEmojiHeight;

    /**
     * 显示监听
     */
    private onShowListener mOnShowListener;

    /**
     * 显示标志
     */
    private boolean isEmojiShowing = false;

    public EmojiUtil(BaseActivity activity, @LayoutRes int layoutResID) {
        this.mActivity = activity;

        //屏幕方向
        this.mOrientation = mActivity.getResources().getConfiguration().orientation;

        //Emoji布局
        this.mEmojiView = mActivity.getLayoutInflater().inflate(layoutResID, null);
    }

    /**
     * 设置Emoji显示监听
     */
    public void setOnShowListener(onShowListener onShowListener) {
        this.mOnShowListener = onShowListener;
    }

    /**
     * 返回当前Emoji是否正在显示
     */
    public boolean isEmojiShowing() {
        return isEmojiShowing;
    }

    /**
     * 显示Emoji，同时会隐藏软键盘
     */
    public void showEmoji() {
        isEmojiShowing = true;
        mOnShowListener.onEmojiShowing(true);
        mActivity.hideInputSoft();
        mActivity.getDecorView().requestLayout();
    }

    /**
     * 隐藏Emoji
     */
    public void hideEmoji() {
        isEmojiShowing = false;
        mOnShowListener.onEmojiShowing(false);
        setEmojiVisibility(View.GONE);
        mActivity.getDecorView().requestLayout();
    }

    /**
     * 返回Emoji布局
     */
    public View getEmojiView() {
        if (mEmojiView.getParent() == null) {
            ViewGroup subDecorView = (ViewGroup) mActivity.getContentParent().getParent().getParent();
            //这里要避免将TitleBar插入DecorView中
            if ((subDecorView instanceof FrameLayout) && subDecorView != mActivity.getDecorView()) {
                mEmojiView.setVisibility(View.GONE);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getEmojiHeight());
                layoutParams.gravity = Gravity.BOTTOM;
                subDecorView.addView(mEmojiView, layoutParams);
            }
        }
        return mEmojiView;
    }

    /**
     * 获取Emoji高度
     */
    private int getEmojiHeight() {
        if (mEmojiHeight == 0) {
            final int decorViewHeight = mActivity.getDecorView().getHeight();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && mActivity.isInMultiWindowMode()) {
                //窗口模式时固定返回窗口当前高度的40%
                return (int) (decorViewHeight * 0.4F);
            } else if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                //返回竖屏模式下的高度值
                mEmojiHeight = mSharedPreferencesProxy.getInt(KEY_SP_EMOJI_HEIGHT, (int) (decorViewHeight * 0.4F));
            } else {
                //返回横屏模式下的高度值
                mEmojiHeight = mSharedPreferencesProxy.getInt(KEY_SP_EMOJI_HEIGHT_LANDSCAPE, (int) (decorViewHeight * 0.4F));
            }
        }
        return mEmojiHeight;
    }

    /**
     * 根据contentParent安全距离的变化进行适配，并返回Emoji适配后的底部安全距离
     *
     * @param safeLeft   左侧安全距离，考虑异形屏、导航键的适配
     * @param safeRight  右侧安全距离，考虑异形屏、导航键的适配
     * @param safeBottom 底部安全距离，当此安全距离超出窗口1/4即认为弹出了软键盘
     */
    public int onContentParentResize(int safeLeft, int safeRight, int safeBottom) {
        //高度超过1/4就认为软键盘为弹出状态
        if (safeBottom * 4 > mActivity.getDecorView().getHeight()) {
            //记录软键盘高度
            recordInputSoftHeight(safeBottom);
            //Emoji已标记为显示
            if (isEmojiShowing) {
                if (getEmojiView().getVisibility() == View.VISIBLE) {
                    //Emoji已正常显示，此时弹出软键盘，需要隐藏Emoji。
                    // 备注：若Emoji还未显示，说明刚调用了showEmoji，软键盘虽然是弹出状态，但软键盘即将关闭，因此不予隐藏Emoji
                    hideEmoji();
                }
            }
            //软键盘非弹出状态，且需要显示Emoji
        } else if (isEmojiShowing) {
            //重置Emoji的高度（以及左右安全距离），并指定为新的底部安全距离
            safeBottom = resizeEmoji(safeLeft, safeRight, safeBottom);
            if (getEmojiView().getVisibility() != View.VISIBLE) {
                //显示Emoji
                setEmojiVisibility(View.VISIBLE);
            }
        }
        return safeBottom;
    }

    /**
     * 记录软键盘高度
     */
    private void recordInputSoftHeight(int inputSoftHeight) {
        if (mEmojiHeight == inputSoftHeight) {
            return;
        }
        mEmojiHeight = inputSoftHeight;

        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            mSharedPreferencesProxy.putInt(KEY_SP_EMOJI_HEIGHT, mEmojiHeight).apply();
        } else {
            mSharedPreferencesProxy.putInt(KEY_SP_EMOJI_HEIGHT_LANDSCAPE, mEmojiHeight).apply();
        }
    }

    /**
     * 重置Emoji的高度（以及左右安全距离），返回高度值
     *
     * @param safeLeft   左侧安全距离，考虑异形屏、导航键的适配
     * @param safeRight  右侧安全距离，考虑异形屏、导航键的适配
     * @param safeBottom 底部安全距离，考虑异形屏、导航键的适配
     */
    private int resizeEmoji(int safeLeft, int safeRight, int safeBottom) {
        int emojiHeight = getEmojiHeight();
        View emojiView = getEmojiView();
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) emojiView.getLayoutParams();
        if (layoutParams.height != emojiHeight || layoutParams.leftMargin != safeLeft || layoutParams.rightMargin != safeRight) {
            layoutParams.height = emojiHeight;
            layoutParams.leftMargin = safeLeft;
            layoutParams.rightMargin = safeRight;
            emojiView.setLayoutParams(layoutParams);
        }
        if (safeBottom != emojiView.getPaddingBottom()) {
            emojiView.setPadding(emojiView.getPaddingLeft(), emojiView.getPaddingTop(), emojiView.getPaddingRight(), safeBottom);
        }
        return emojiHeight;
    }

    /**
     * 设置Emoji显示/隐藏
     */
    private void setEmojiVisibility(int visibility) {
        View emojiView = getEmojiView();
        if (emojiView.getVisibility() != visibility) {
            emojiView.setVisibility(visibility);
        }
    }

    public interface onShowListener {

        void onEmojiShowing(boolean showing);

    }

}
