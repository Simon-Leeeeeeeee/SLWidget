package cn.simonlee.widgetdemo.swipeback;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.simonlee.widget.lib.widget.titlebar.TitleBar;

import java.util.Random;

import cn.simonlee.widgetdemo.CommonActivity;
import cn.simonlee.widgetdemo.R;
import cn.simonlee.widgetdemo.utils.EmojiUtil;

/**
 * 侧滑返回页面
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-07-27
 */
public class SwipeBackActivity extends CommonActivity implements View.OnClickListener {

    /**
     * 页面编号
     */
    public int mIndex;

    /**
     * 随机颜色值
     */
    private int mRandomColor;

    /**
     * 输入框
     */
    private EditText mEditText_Hello;

    /**
     * Emoji按钮
     */
    private Button mButton_Emoji;

    /**
     * 底部布局容器
     */
    private ViewGroup mLayout_Bottom;

    /**
     * Emoji工具
     */
    private EmojiUtil mEmojiUtil;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (outState != null) {
            outState.putInt("randomColor", mRandomColor);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //获取随机颜色值
        mRandomColor = getRandomColor(savedInstanceState);
        setContentView(R.layout.activity_swipeback);
        //初始化View
        initView();
        //初始化Emoji相关
        initEmojiView();
    }

    /**
     * 初始化View
     */
    private void initView() {
        //页面编号
        mIndex = getIntent().getIntExtra("index", 1);

        //设置标题
        TitleBar titleBar = getTitleBar();
        titleBar.setTitle(getText(R.string.swipeback) + "-" + mIndex);
        //更改窗口背景色
        getWindow().setBackgroundDrawable(new ColorDrawable(mRandomColor));

        findViewById(R.id.swipeback_btn_next).setOnClickListener(this);

        mButton_Emoji = findViewById(R.id.swipeback_btn_emoji);
        mButton_Emoji.setOnClickListener(this);

        mEditText_Hello = findViewById(R.id.swipeback_et_hello);
        mLayout_Bottom = findViewById(R.id.swipeback_layout_bottom);
    }

    /**
     * 初始化Emoji相关
     */
    private void initEmojiView() {
        mEmojiUtil = new EmojiUtil(this, R.layout.layout_emoji);

        mEmojiUtil.setOnShowListener(new EmojiUtil.onShowListener() {
            @Override
            public void onEmojiShowing(boolean showing) {
                mButton_Emoji.setText(showing ? "Input" : "Emoji");
            }
        });

        mEmojiUtil.getEmojiView().findViewById(R.id.emoji_btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //隐藏emoji
                mEmojiUtil.hideEmoji();
            }
        });
    }

    @Override
    protected void resizeContentParent(int safeLeft, int safeTop, int safeRight, int safeBottom) {
        if (mEmojiUtil != null) {
            //底部安全距离为Emoji的高度
            safeBottom = mEmojiUtil.onContentParentResize(safeLeft, safeRight, safeBottom);
        }
        if (safeBottom != mLayout_Bottom.getPaddingBottom()) {
            //设置PaddingBottom，防止与导航栏等重叠
            mLayout_Bottom.setPadding(mLayout_Bottom.getPaddingLeft(), mLayout_Bottom.getPaddingTop(), mLayout_Bottom.getPaddingRight(), safeBottom);
        }
        super.resizeContentParent(safeLeft, safeTop, safeRight, 0);
    }

    private int getRandomColor(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            return savedInstanceState.getInt("randomColor", 0);
        } else {
            Random random = new Random();
            return Color.argb(255, random.nextInt(255), random.nextInt(255), random.nextInt(255));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.swipeback_btn_next: {
                Intent intent = new Intent(this, SwipeBackActivity.class);
                intent.putExtra("index", mIndex + 1);
                startActivity(intent);
                break;
            }
            case R.id.swipeback_btn_emoji: {
                if (!mEmojiUtil.isEmojiShowing()) {
                    mEmojiUtil.showEmoji();
                } else {
                    showInputSoft(mEditText_Hello);
                    // 窗口模式无法监听软键盘变化，需要手动隐藏表情框
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInMultiWindowMode()) {
                        mEmojiUtil.hideEmoji();
                    }
                }
                break;
            }
            default: {
                break;
            }
        }
    }

}
