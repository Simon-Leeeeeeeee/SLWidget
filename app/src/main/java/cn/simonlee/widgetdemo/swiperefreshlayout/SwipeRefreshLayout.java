package cn.simonlee.widgetdemo.swiperefreshlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

import cn.simonlee.widgetdemo.R;

/**
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2020-05-31
 */
public class SwipeRefreshLayout extends cn.simonlee.widget.swiperefreshlayout.SwipeRefreshLayout {

    private Animation mRotateAnimation;

    private OnRefreshListener mOuterOnRefreshListener;

    private OnRefreshListener mInnerOnRefreshListener = new OnRefreshListener() {
        @Override
        public void onHeaderRefresh(cn.simonlee.widget.swiperefreshlayout.SwipeRefreshLayout parent, boolean isChanged, int state, float offset) {
            if (mOuterOnRefreshListener != null) {
                mOuterOnRefreshListener.onHeaderRefresh(parent, isChanged, state, offset);
            }
            final View headerRefreshView = parent.getHeaderRefreshView();
            final View loadingImage = headerRefreshView.findViewById(R.id.refresh_image);
            final TextView loadingText = headerRefreshView.findViewById(R.id.refresh_text);

            setVisibility(loadingImage, state == STATE_UNABLE ? View.GONE : View.VISIBLE);

            switch (state) {
                case STATE_UNABLE: {
                    if (isChanged) {
                        loadingText.setText("没网了，快充钱");
                    }
                    break;
                }
                case STATE_CLOSE: {
                    if (isChanged) {
                        loadingImage.clearAnimation();
                    }
                    break;
                }
                case STATE_OPEN: {
                    if (isChanged) {
                        loadingText.setText("下拉刷新");
                    }
                    loadingImage.setRotation(-360F * Math.abs(offset) / headerRefreshView.getHeight());
                    break;
                }
                case STATE_READY: {
                    if (isChanged) {
                        loadingText.setText("释放后刷新");
                    }
                    loadingImage.setRotation(-360F * Math.abs(offset) / headerRefreshView.getHeight());
                    break;
                }
                case STATE_REFRESHING: {
                    if (isChanged) {
                        loadingText.setText("正在刷新...");
                        loadingImage.startAnimation(mRotateAnimation);
                    }
                    break;
                }
                case STATE_REFRESH_COMPLETE: {
                    if (isChanged) {
                        loadingText.setText("刷新完毕");
                        loadingImage.clearAnimation();
                    }
                    loadingImage.setRotation(0F);
                    break;
                }
            }
        }

        @Override
        public void onFooterRefresh(cn.simonlee.widget.swiperefreshlayout.SwipeRefreshLayout parent, boolean isChanged, int state, float offset) {
            if (mOuterOnRefreshListener != null) {
                mOuterOnRefreshListener.onFooterRefresh(parent, isChanged, state, offset);
            }
            final View footerRefreshView = parent.getFooterRefreshView();
            final View loadingImage = footerRefreshView.findViewById(R.id.refresh_image);
            final TextView loadingText = footerRefreshView.findViewById(R.id.refresh_text);

            setVisibility(loadingImage, state == STATE_UNABLE ? View.GONE : View.VISIBLE);

            switch (state) {
                case STATE_UNABLE: {
                    if (isChanged) {
                        loadingText.setText("全部加载完啦");
                    }
                    break;
                }
                case STATE_CLOSE: {
                    if (isChanged) {
                        loadingImage.clearAnimation();
                    }
                    break;
                }
                case STATE_OPEN: {
                    if (isChanged) {
                        loadingText.setText("上拉加载更多");
                    }
                    loadingImage.setRotation(-360F * Math.abs(offset) / footerRefreshView.getHeight());
                    break;
                }
                case STATE_READY: {
                    if (isChanged) {
                        loadingText.setText("释放后加载");
                    }
                    loadingImage.setRotation(-360F * Math.abs(offset) / footerRefreshView.getHeight());
                    break;
                }
                case STATE_REFRESHING: {
                    if (isChanged) {
                        loadingText.setText("正在加载...");
                        loadingImage.startAnimation(mRotateAnimation);
                    }
                    break;
                }
                case STATE_REFRESH_COMPLETE: {
                    if (isChanged) {
                        loadingText.setText("加载完毕");
                        loadingImage.clearAnimation();
                    }
                    loadingImage.setRotation(0F);
                    break;
                }
            }
        }
    };

    public SwipeRefreshLayout(Context context) {
        super(context);
        initView();
    }

    public SwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public SwipeRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        setHeaderRefreshView(R.layout.layout_refresh_header);
        setFooterRefreshView(R.layout.layout_refresh_footer);

        super.setOnRefreshListener(mInnerOnRefreshListener);

        mRotateAnimation = new RotateAnimation(0F, 360F, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5F);
        mRotateAnimation.setRepeatCount(-1);
        mRotateAnimation.setDuration(1000);
        mRotateAnimation.setInterpolator(new LinearInterpolator());
    }

    private void setVisibility(View view, int visibility) {
        if (view != null && view.getVisibility() != visibility) {
            view.setVisibility(visibility);
        }
    }

    @Override
    public void setOnRefreshListener(OnRefreshListener listener) {
        mOuterOnRefreshListener = listener;
    }

}
