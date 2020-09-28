package cn.simonlee.widgetdemo.badge;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;
import cn.simonlee.widget.badgeview.Badge;
import cn.simonlee.widget.badgeview.IBadge;

/**
 * 带角标的ImageView
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-07-19
 */
public class BadgeImageView extends AppCompatImageView implements IBadge {

    private final Badge mBadge;

    public BadgeImageView(Context context) {
        super(context);
        mBadge = new Badge(this, null);
    }

    public BadgeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mBadge = new Badge(this, attrs);
    }

    public BadgeImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mBadge = new Badge(this, attrs);
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        mBadge.drawBadge(canvas);
    }

    @Override
    public Badge getBadge() {
        return mBadge;
    }

}
