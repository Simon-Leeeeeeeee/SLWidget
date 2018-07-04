package cn.simonlee.widget.autowraplayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @createdTime 2018-07-04
 */
public class AutoWrapGridLayout extends ViewGroup {

    /**
     * 单元格内对齐方式：左对齐
     */
    public final int GRAVITY_LEFT = 1;
    /**
     * 单元格内对齐方式：顶对齐
     */
    public final int GRAVITY_TOP = 2;
    /**
     * 单元格内对齐方式：右对齐
     */
    public final int GRAVITY_RIGHT = 4;
    /**
     * 单元格内对齐方式：底对齐
     */
    public final int GRAVITY_BOTTOM = 8;
    /**
     * 单元格内对齐方式：居中
     */
    public final int GRAVITY_CENTER = 16;
    /**
     * 单元格内对齐方式：填满
     */
    public final int GRAVITY_FILL = 32;

    /**
     * 单元格内对齐方式
     */
    private int mGridCellGravity;

    /**
     * 单元格宽度
     */
    private int mGridCellWidth;

    /**
     * 单元格高度
     */
    private int mGridCellHeight;

    /**
     * 未指定单元格的宽高
     */
    private boolean noGridCellSize;

    /**
     * AutoWrapGridLayout的宽被单元格平分后的余数。
     * 在每行从左至右每个单元格从余数中取1，尽可能使每个单元格看起来一样宽
     */
    private int mRowSurplusWidth;

    /**
     * 网格线宽
     */
    private int mGridLineWidth;

    /**
     * 网格线颜色
     */
    private int mGridLineColor;

    /**
     * 网格线画笔
     */
    private Paint mGridLinePaint;

    /**
     * firstChildView是否置顶
     */
    private boolean isStickFirst;

    /**
     * 置顶FirstChildView的宽，未置顶则为0
     */
    private int mStickFirstWidth;

    /**
     * 置顶FirstChildView的高，未置顶则为0
     */
    private int mStickFirstHeight;

    /**
     * LayoutParams宽高
     */
    private int mLayoutWidth, mLayoutHeight;

    public AutoWrapGridLayout(Context context) {
        super(context);
    }

    public AutoWrapGridLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public AutoWrapGridLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attributeSet) {
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.AutoWrapGridLayout);

        this.mGridCellGravity = typedArray.getInt(R.styleable.AutoWrapGridLayout_gridCellGravity, GRAVITY_FILL);
        this.mGridCellWidth = typedArray.getDimensionPixelSize(R.styleable.AutoWrapGridLayout_gridCellWidth, 0);
        this.mGridCellHeight = typedArray.getDimensionPixelSize(R.styleable.AutoWrapGridLayout_gridCellHeight, 0);
        this.noGridCellSize = mGridCellWidth <= 0 || mGridCellWidth <= 0;
        this.mGridLineWidth = typedArray.getDimensionPixelSize(R.styleable.AutoWrapGridLayout_gridLineWidth, 1);
        this.mGridLineColor = typedArray.getColor(R.styleable.AutoWrapGridLayout_gridLineColor, 0);
        this.isStickFirst = typedArray.getBoolean(R.styleable.AutoWrapGridLayout_stickFirst, false);

        typedArray.recycle();
        mGridLinePaint = new Paint();
        mGridLinePaint.setColor(mGridLineColor);
    }

    @Override
    public void setLayoutParams(LayoutParams params) {
        mLayoutWidth = params.width;
        mLayoutHeight = params.height;
        super.setLayoutParams(params);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int paddingWidth = getPaddingLeft() + getPaddingRight();
        final int paddingHeight = getPaddingTop() + getPaddingBottom();
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        //单元格个数
        int gridCellCount = 0;
        mStickFirstWidth = mStickFirstHeight = 0;
        for (int index = 0; index < getChildCount(); index++) {//测量每个子View，并记录宽高等数据
            View child = getChildAt(index);
            if (child == null || child.getVisibility() == View.GONE) {
                continue;
            }
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            if (isStickFirst && index == 0) {
                //记录置顶的firstChild宽高
                MarginLayoutParams childLP = (MarginLayoutParams) child.getLayoutParams();
                mStickFirstWidth = childLP.leftMargin + childLP.rightMargin + child.getMeasuredWidth();
                mStickFirstHeight = childLP.topMargin + childLP.bottomMargin + child.getMeasuredHeight();
            } else {
                gridCellCount++;
                //未指定单元格的尺寸，以childView的宽高最大值为准
                if (noGridCellSize) {
                    MarginLayoutParams childLP = (MarginLayoutParams) child.getLayoutParams();
                    int childMarginWidth = childLP.leftMargin + childLP.rightMargin;
                    int childMarginHeight = childLP.topMargin + childLP.bottomMargin;
                    mGridCellWidth = Math.max(mGridCellWidth, childMarginWidth + child.getMeasuredWidth());
                    mGridCellHeight = Math.max(mGridCellHeight, childMarginHeight + child.getMeasuredHeight());
                }
            }
        }
        //宽为wrap_content,需要根据子view来计算实际宽度
        if (mLayoutWidth == LayoutParams.WRAP_CONTENT && widthMode != MeasureSpec.EXACTLY) {
            //计算需要的宽度
            int maxGridWidth = Math.max(mStickFirstWidth, gridCellCount * (mGridCellWidth + mGridLineWidth) - mGridLineWidth);
            //不能操作最大值
            widthSize = Math.min(widthSize, paddingWidth + maxGridWidth);
            View firstChild = getChildAt(0);
            //重新测量置顶的firstChild
            if (isStickFirst && firstChild != null && firstChild.getVisibility() != View.GONE) {
                //改变宽度模式重新测量置顶的firstChild，使其match_parent生效
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
                measureChildWithMargins(firstChild, widthMeasureSpec, 0, heightMeasureSpec, 0);
            }
        }
        //计算单元格的列数
        int columnCount = (widthSize - paddingWidth + mGridLineWidth) / (mGridCellWidth + mGridLineWidth);
        //校正单元格的宽度，使之填满整个View
        mGridCellWidth = (widthSize - paddingWidth + mGridLineWidth) / columnCount - mGridLineWidth;
        //宽被单元格平分后的余数
        mRowSurplusWidth = widthSize - paddingWidth + mGridLineWidth - (mGridCellWidth + mGridLineWidth) * columnCount;
        //计算单元格的行数
        int rowCount = (int) Math.ceil(1F * gridCellCount / columnCount);
        //高为wrap_content,根据子view来计算实际高度
        if (mLayoutHeight == LayoutParams.WRAP_CONTENT && heightMode != MeasureSpec.EXACTLY) {
            //计算需要的高度
            int maxChildHeight = mStickFirstHeight + rowCount * (mGridCellHeight + mGridLineWidth);
            //firstChild未置顶，需要减去一行网格线宽
            if (mStickFirstHeight <= 0 && rowCount > 0) {
                maxChildHeight -= mGridLineWidth;
            }
            //不能操作最大值
            heightSize = Math.min(heightSize, paddingHeight + maxChildHeight);
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int paddingLeft = getPaddingLeft();
        final int paddingRight = getPaddingRight();
        //布局的左边
        int layoutLeft = paddingLeft;
        //布局的上边
        int layoutTop = getPaddingTop();
        //每行剩余宽度的余量
        int rowSurplusWidth = mRowSurplusWidth;

        for (int index = 0; index < getChildCount(); index++) {
            View child = getChildAt(index);
            if (child == null || child.getVisibility() == View.GONE) {
                continue;
            }
            //childView的布局参数，用于获取margin值
            MarginLayoutParams childLP = (MarginLayoutParams) child.getLayoutParams();
            if (isStickFirst && index == 0) {
                //firstChild置顶，并使布局上边下移
                child.layout(layoutLeft + childLP.leftMargin, layoutTop + childLP.topMargin, layoutLeft + childLP.leftMargin + child.getMeasuredWidth(), layoutTop + childLP.topMargin + child.getMeasuredHeight());
                layoutTop += child.getMeasuredHeight() + childLP.topMargin + childLP.bottomMargin + mGridLineWidth;
            } else {
                if (layoutLeft > paddingLeft && layoutLeft + mGridCellWidth > right - paddingRight) {
                    //当前行已经排满，进行换行，左边重置为paddingLeft，上边加一行的高度，每行剩余宽度也重置为mRowSurplusWidth
                    layoutLeft = paddingLeft;
                    layoutTop += mGridCellHeight + mGridLineWidth;
                    rowSurplusWidth = mRowSurplusWidth;
                }
                //当前单元格的宽度，剩余宽度未取完则+1
                int gridCellWidth = mGridCellWidth + (rowSurplusWidth-- > 0 ? 1 : 0);
                //当前单元格中child的上下左右值
                int childLeft = layoutLeft + childLP.leftMargin;
                int childTop = layoutTop + childLP.topMargin;
                int childRight = layoutLeft + gridCellWidth - childLP.rightMargin;
                int childBottom = layoutTop + mGridCellHeight - childLP.bottomMargin;
                //根据水平对齐方式调整左右值
                if ((mGridCellGravity & GRAVITY_FILL) == GRAVITY_FILL) {//填满
//                    childRight = layoutLeft + gridCellWidth - childLP.rightMargin;
                } else if ((mGridCellGravity & GRAVITY_LEFT) == GRAVITY_LEFT) {//水平居左
                    childRight = Math.min(childRight, childLeft + child.getMeasuredWidth());
                } else if ((mGridCellGravity & GRAVITY_RIGHT) == GRAVITY_RIGHT) {//水平居右
                    childLeft = Math.max(childLeft, childRight - child.getMeasuredWidth());
                } else if ((mGridCellGravity & GRAVITY_CENTER) == GRAVITY_CENTER) {//水平居中
                    childLeft = (int) Math.max(childLeft, (childLeft + childRight - child.getMeasuredWidth()) / 2F + 0.5F);
                    childRight = Math.min(childRight, childLeft + child.getMeasuredWidth());
                }
                //根据垂直对齐方式调整上下值
                if ((mGridCellGravity & GRAVITY_FILL) == GRAVITY_FILL) {//填满
//                    childBottom = layoutTop + mGridCellHeight - childLP.bottomMargin;
                } else if ((mGridCellGravity & GRAVITY_TOP) == GRAVITY_TOP) {//垂直居上
                    childBottom = Math.min(childBottom, childTop + child.getMeasuredHeight());
                } else if ((mGridCellGravity & GRAVITY_BOTTOM) == GRAVITY_BOTTOM) {//垂直居下
                    childTop = Math.max(childTop, childBottom - child.getMeasuredHeight());
                } else if ((mGridCellGravity & GRAVITY_CENTER) == GRAVITY_CENTER) {//垂直居中
                    childTop = (int) Math.max(childTop, (childTop + childBottom - child.getMeasuredHeight()) / 2F + 0.5F);
                    childBottom = Math.min(childBottom, childTop + child.getMeasuredHeight());
                }
                child.layout(childLeft, childTop, childRight, childBottom);
                //childView布局后对左边增加一个单元格的宽度
                layoutLeft += gridCellWidth + mGridLineWidth;
            }
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        //网格线的颜色为透明，则不进行绘制
        if (mGridLineColor >>> 24 <= 0) {
            return super.drawChild(canvas, child, drawingTime);
        }
        if (isStickFirst && child == getChildAt(0)) {
            //绘制置顶的firstChild下边网格线
            int gridLineY = getPaddingTop() + mStickFirstHeight;
            canvas.drawRect(0, gridLineY, getWidth(), gridLineY + mGridLineWidth, mGridLinePaint);
        } else {
            //计算当前child所在的行
            int childRow = (int) Math.ceil(1F * (child.getBottom() - getPaddingTop() - mStickFirstHeight) / (mGridCellHeight + mGridLineWidth));
            //计算当前child所在的列
            int childColumn = (int) Math.ceil(1F * (child.getRight() - getPaddingLeft()) / (mGridCellWidth + mGridLineWidth));
            //计算当前child所在单元格的底边
            int gridCellBottom = getPaddingTop() + mStickFirstHeight + childRow * (mGridCellHeight + mGridLineWidth);
            //firstChild未置顶，需要减去一个网格线宽
            if (mStickFirstHeight <= 0) {
                gridCellBottom -= mGridLineWidth;
            }
            //计算当前child所在单元格的右边
            int gridCellRight = getPaddingLeft() + childColumn * (mGridCellWidth + mGridLineWidth) - mGridLineWidth;
            //宽存在余量，需要校正单元格的右边
            if (mRowSurplusWidth > 0) {
                gridCellRight += Math.min(mRowSurplusWidth, childColumn);
            }
            //当前child为该行的第一个单元格，绘制下边网格线
            if (child.getLeft() < mGridCellWidth) {
                canvas.drawRect(0, gridCellBottom, getWidth(), gridCellBottom + mGridLineWidth, mGridLinePaint);
            }
            //绘制child右边网格线
            canvas.drawRect(gridCellRight, gridCellBottom - mGridCellHeight, gridCellRight + mGridLineWidth, gridCellBottom, mGridLinePaint);
        }
        return super.drawChild(canvas, child, drawingTime);
    }

    /**
     * 设置单元格尺寸宽度，单位px
     */
    public void setGridCellSize(int width, int height) {
        this.mGridCellWidth = width;
        this.mGridCellHeight = height;
        this.noGridCellSize = mGridCellWidth <= 0 || mGridCellWidth <= 0;
        if (mLayoutWidth == ViewGroup.LayoutParams.WRAP_CONTENT || mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
            super.requestLayout();
        } else {
            super.invalidate();
        }
    }

    /**
     * 获取单元格宽，单位px
     */
    public int getGridCellWidth() {
        return mGridCellWidth;
    }

    /**
     * 获取单元格高，单位px
     */
    public int getGridCellHeight() {
        return mGridCellHeight;
    }

    /**
     * 设置网格线宽度，单位px
     */
    public void setGridLineWidth(int width) {
        this.mGridLineWidth = width;
        if (mLayoutWidth == ViewGroup.LayoutParams.WRAP_CONTENT || mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
            super.requestLayout();
        } else {
            super.invalidate();
        }
    }

    /**
     * 获取网格线宽度，单位px
     */
    public int getGridLineWidth() {
        return mGridLineWidth;
    }

    /**
     * 设置网格线颜色
     */
    public void setGridLineColor(int color) {
        this.mGridLineColor = color;
        mGridLinePaint.setColor(mGridLineColor);
        super.invalidate();
    }

    /**
     * 获取网格线颜色
     */
    public int getGridLineColor() {
        return mGridLineColor;
    }

    /**
     * 设置第一个子View是否置顶
     */
    public void setStickFirst(boolean stick) {
        this.isStickFirst = stick;
        if (mLayoutWidth == ViewGroup.LayoutParams.WRAP_CONTENT || mLayoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
            super.requestLayout();
        } else {
            super.invalidate();
        }
    }

    /**
     * 获取第一个子View是否置顶
     */
    public boolean getStickFirst() {
        return isStickFirst;
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams lp) {
        if (lp instanceof MarginLayoutParams) {
            return new MarginLayoutParams((MarginLayoutParams) lp);
        } else {
            return new MarginLayoutParams(lp);
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected boolean checkLayoutParams(LayoutParams p) {
        return p instanceof MarginLayoutParams;
    }

}
