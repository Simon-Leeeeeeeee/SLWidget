package cn.simonlee.widgetdemo.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @createdTime 2018-06-07
 */
public class Fake3DView extends View {

    private Matrix mMatrix = new Matrix();

    //旋转角度
    private float mRotateAngle;

    //视距
    private float mSightDistance;


    private Rect mRect;

    public Fake3DView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mRect = new Rect();
    }

    public Fake3DView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mRect = new Rect();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mRect.set(getPaddingLeft(), getPaddingTop(), getLeft() + getMeasuredWidth(), getPaddingTop() + getMeasuredHeight());
        if (mSightDistance == 0) {
            mSightDistance = getMeasuredHeight() * 4;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        rotateX(mRotateAngle, mSightDistance, mRect);
        canvas.concat(mMatrix);
        super.draw(canvas);
    }

    private void rotateX(float angle, float sightDistance, Rect rect) {
        mMatrix.reset();
        int width = rect.width();
        int height = rect.height();

        float[] src = {0, 0,//左上角
                0, height, //左下角
                width, height, //右下角
                width, 0};//右上角

        float widthTopPersp = (float) (width * (0.5F - 1F / (2F + Math.sin(angle) * height / sightDistance)));
        float widthBottomPersp = (float) (width * (0.5F - 1F / (2F - Math.sin(angle) * height / sightDistance)));

        float heightTopPersp = height / 2F - (float) (Math.cos(angle) / (2F / height + Math.sin(angle) / sightDistance));
        float heightBottomPersp = height / 2F - (float) (Math.cos(angle) / (2F / height - Math.sin(angle) / sightDistance));

        float[] dst = {widthTopPersp, heightTopPersp,//左上角
                widthBottomPersp, height - heightBottomPersp,//左下角
                width - widthBottomPersp, height - heightBottomPersp,//右下角
                width - widthTopPersp, heightTopPersp};//右上角

        mMatrix.setPolyToPoly(src, 0, dst, 0, 4);
        mMatrix.postTranslate(rect.left, rect.top);
        if (heightTopPersp + heightBottomPersp == height) {
            mMatrix.setScale(0, 0);
        }
    }

    public void setRotateAngle(float angle) {
        mRotateAngle = angle / 180 * 3.141592653589F;
        super.invalidate();
    }

    public void setSightDistance(int distance) {
        mSightDistance = getMeasuredHeight() * distance;
        super.invalidate();
    }

}
