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
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2018-07-04
 */
public class AutoWrapGridLayout extends ViewGroup {

    /**
     * 单元格内对齐方式：左对齐
     */
    public static final int GRAVITY_LEFT = 1;
    /**
     * 单元格内对齐方式：顶对齐
     */
    public static final int GRAVITY_TOP = 2;
    /**
     * 单元格内对齐方式：右对齐
     */
    public static final int GRAVITY_RIGHT = 4;
    /**
     * 单元格内对齐方式：底对齐
     */
    public static final int GRAVITY_BOTTOM = 8;
    /**
     * 单元格内对齐方式：居中
     */
    public static final int GRAVITY_CENTER = 16;
    /**
     * 单元格内对齐方式：填满
     */
    public static final int GRAVITY_FILL = 32;

    /**
     * 单元格内对齐方式
     */
    private int mGridCellGravity;

    /**
     * 单元格实际宽高
     */
    private int mGridCellWidth, mGridCellHeight;

    /**
     * 单元格指定宽高
     */
    private int mAppointGridCellWidth, mAppointGridCellHeight;

    /**
     * 网格的宽被单元格平分后的余量。
     * 每行从左至右每个单元格从余数中取1，尽可能使每个单元格看起来一样宽
     */
    private int mRemnantWidth;

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
     * 单元格的行列数
     */
    private int mRowCount, mColumnCount;

    /**
     * padding宽高
     */
    private int mPaddingWidth, mPaddingHeight;

    /**
     * 网格绘制的起始顶点
     */
    private int mGridDrawTop;

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

        this.mGridCellGravity = typedArray.getInt(R.styleable.AutoWrapGridLayout_autowrap_gridCellGravity, GRAVITY_FILL);
        this.mAppointGridCellWidth = typedArray.getDimensionPixelSize(R.styleable.AutoWrapGridLayout_autowrap_gridCellWidth, 0);
        this.mAppointGridCellHeight = typedArray.getDimensionPixelSize(R.styleable.AutoWrapGridLayout_autowrap_gridCellHeight, 0);
        this.mGridLineWidth = typedArray.getDimensionPixelSize(R.styleable.AutoWrapGridLayout_autowrap_gridLineWidth, 1);
        this.mGridLineColor = typedArray.getColor(R.styleable.AutoWrapGridLayout_autowrap_gridLineColor, 0);
        this.isStickFirst = typedArray.getBoolean(R.styleable.AutoWrapGridLayout_autowrap_stickFirst, false);

        typedArray.recycle();
        mGridLinePaint = new Paint();
        mGridLinePaint.setColor(mGridLineColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        //Step.1 初始化临时变量
        View stickFirst = null;//置顶的首行控件
        int childState = 0; //child状态
        int gridCellCount = 0;//单元格数量
        int gridCellWidth = mAppointGridCellWidth;//单元格宽度
        int gridCellHeight = mAppointGridCellHeight;//单元格高度
        final boolean skipMeasure = mAppointGridCellWidth > 0 && mAppointGridCellHeight > 0;//单元格跳过首次测量

        //Step.2 获取padding的宽高，用于后续计算
        mPaddingWidth = getPaddingLeft() + getPaddingRight();
        mPaddingHeight = getPaddingTop() + getPaddingBottom();

        //Step.3 测量置顶的首行控件，并确定单元格数量及尺寸
        for (int index = 0; index < getChildCount(); index++) {
            View child = getChildAt(index);
            if (child == null || child.getVisibility() == View.GONE) {
                continue;
            }
            //测量置顶的首行控件
            if (index == 0 && isStickFirst) {
                stickFirst = child;
                measureChild(child, widthMeasureSpec, heightMeasureSpec, false);
                childState = combineMeasuredStates(childState, child.getMeasuredState());
                continue;
            }
            //未指定单元格尺寸时，测量子控件尺寸，取最大值做为单元格宽高
            if (!skipMeasure) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec, true);
                childState = combineMeasuredStates(childState, child.getMeasuredState());
                final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                if (mAppointGridCellWidth <= 0) {
                    gridCellWidth = Math.max(gridCellWidth, lp.leftMargin + lp.rightMargin + child.getMeasuredWidth());
                }
                if (mAppointGridCellHeight <= 0) {
                    gridCellHeight = Math.max(gridCellHeight, lp.topMargin + lp.bottomMargin + child.getMeasuredHeight());
                }
            }
            gridCellCount++;
        }

        //Step.4 宽不是精确值，需要进行计算
        if (widthMode != MeasureSpec.EXACTLY) {
            //计算单元格最大宽度
            int maxGridWidth = gridCellCount * (gridCellWidth + mGridLineWidth) - mGridLineWidth;
            if (stickFirst != null) {
                //最大宽度与首行控件宽度取极大值
                final MarginLayoutParams lp = (MarginLayoutParams) stickFirst.getLayoutParams();
                maxGridWidth = Math.max(maxGridWidth, stickFirst.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
            }
            if (widthMode == MeasureSpec.AT_MOST) {
                //限定高度和最大高度取极小值
                widthSize = Math.min(widthSize, mPaddingWidth + maxGridWidth);
            } else {
                widthSize = mPaddingWidth + maxGridWidth;
            }
            //改变宽度模式，指定宽值
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
            if (stickFirst != null) {
                //当首行控件的宽为match_content，重新进行测量，使首行控件的宽为最大宽度
                final MarginLayoutParams lp = (MarginLayoutParams) stickFirst.getLayoutParams();
                if (lp.width == LayoutParams.MATCH_PARENT) {
                    int stickFirstWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize - mPaddingWidth - lp.leftMargin - lp.rightMargin, MeasureSpec.EXACTLY);
                    stickFirst.measure(stickFirstWidthMeasureSpec, stickFirst.getMeasuredHeightAndState());
                    childState = combineMeasuredStates(childState, stickFirst.getMeasuredState());
                }
            }
        }

        //Step.5 计算单元格行列数、校正单元格宽度、确定网格宽度余量
        //列数
        mColumnCount = Math.max(1, (widthSize - mPaddingWidth + mGridLineWidth) / (gridCellWidth + mGridLineWidth));
        //行数
        mRowCount = (int) Math.ceil(1F * gridCellCount / mColumnCount);
        //校正单元格宽度
        mGridCellWidth = (widthSize - mPaddingWidth + mGridLineWidth) / mColumnCount - mGridLineWidth;
        //单元格高度
        mGridCellHeight = gridCellHeight;
        //网格宽度余量
        mRemnantWidth = widthSize - mPaddingWidth + mGridLineWidth - (mGridCellWidth + mGridLineWidth) * mColumnCount;

        //Step.6 根据单元格尺寸测量单元格控件
        final boolean fillGravity = (mGridCellGravity & GRAVITY_FILL) == GRAVITY_FILL;//填充单元格
        final int gridCellMeasureSpec_Width = MeasureSpec.makeMeasureSpec(mGridCellWidth, MeasureSpec.EXACTLY);
        final int gridCellMeasureSpec_Height = MeasureSpec.makeMeasureSpec(mGridCellHeight, MeasureSpec.EXACTLY);
        final int gridCellMeasureSpec_WidthPlus = MeasureSpec.makeMeasureSpec(mGridCellWidth + 1, MeasureSpec.EXACTLY);
        int columnNum = 0;//列数
        for (int index = 0; index < getChildCount(); index++) {
            View child = getChildAt(index);
            if (child == null || child.getVisibility() == View.GONE) {
                continue;
            }
            if (index != 0 || !isStickFirst) {
                //若当前列数大于等于网格列数，则换行
                if (columnNum >= mColumnCount) {
                    columnNum = 0;
                }
                final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                final boolean matchWidth = fillGravity || lp.width == LayoutParams.MATCH_PARENT;
                final boolean matchHeight = fillGravity || lp.height == LayoutParams.MATCH_PARENT;

                int childWidthMeasureSpec = 0, childHeightMeasureSpec = 0;
                if (matchWidth && matchHeight) {//宽高都要填满单元格
                    childWidthMeasureSpec = columnNum < mRemnantWidth ? gridCellMeasureSpec_WidthPlus : gridCellMeasureSpec_Width;
                    childHeightMeasureSpec = gridCellMeasureSpec_Height;
                } else if (matchWidth) {//宽度填满单元格
                    childWidthMeasureSpec = columnNum > mRemnantWidth ? gridCellMeasureSpec_Width : gridCellMeasureSpec_WidthPlus;
                    childHeightMeasureSpec = skipMeasure ? getChildHeightMeasureSpec(heightMeasureSpec, lp, true) : child.getMeasuredWidthAndState();
                } else if (matchHeight) {//高度填满单元格
                    childWidthMeasureSpec = skipMeasure ? getChildWidthMeasureSpec(widthMeasureSpec, lp, true) : child.getMeasuredHeightAndState();
                    childHeightMeasureSpec = gridCellMeasureSpec_Height;
                } else if (skipMeasure) {//宽高都不填满单元格，但是前面跳过了测量
                    childWidthMeasureSpec = getChildWidthMeasureSpec(widthMeasureSpec, lp, true);
                    childHeightMeasureSpec = getChildHeightMeasureSpec(heightMeasureSpec, lp, true);
                }
                if (matchWidth || matchHeight || skipMeasure) {
                    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                    childState = combineMeasuredStates(childState, child.getMeasuredState());
                }
                columnNum++;
            }
        }

        //Step.7 高不是精确值，需要进行计算
        if (heightMode != MeasureSpec.EXACTLY) {
            //根据单元格行数计算高度
            int maxHeightSize = mPaddingHeight + mRowCount * (mGridCellHeight + mGridLineWidth) - mGridLineWidth;
            if (stickFirst != null) {
                //加上首行置顶的高度
                final MarginLayoutParams lp = (MarginLayoutParams) stickFirst.getLayoutParams();
                maxHeightSize += stickFirst.getMeasuredHeight() + lp.topMargin + lp.bottomMargin + mGridLineWidth;
            } else if (mRowCount == 0) {
                maxHeightSize = mPaddingHeight;
            }
            if (heightMode == MeasureSpec.AT_MOST) {
                //限定高度和最大高度取极小值
                heightSize = Math.min(heightSize, maxHeightSize);
            } else {
                heightSize = maxHeightSize;
            }
        }
        setMeasuredDimension(resolveSizeAndState(widthSize, widthMeasureSpec, childState), resolveSizeAndState(heightSize, heightMeasureSpec, childState << MEASURED_HEIGHT_STATE_SHIFT));
    }

    /**
     * 计算child尺寸，忽略MATCH_PARENT属性（非单元格的宽为MATCH_PARENT除外）
     */
    protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec, boolean isGridCell) {
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        final int childWidthMeasureSpec = getChildWidthMeasureSpec(parentWidthMeasureSpec, lp, isGridCell);
        final int childHeightMeasureSpec = getChildHeightMeasureSpec(parentHeightMeasureSpec, lp, isGridCell);
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    private int getChildHeightMeasureSpec(int parentHeightMeasureSpec, MarginLayoutParams lp, boolean isGridCell) {
        final int childHeightDimension = lp.height != LayoutParams.MATCH_PARENT ? lp.height : (isGridCell ? LayoutParams.WRAP_CONTENT : lp.height);
        return getChildMeasureSpec(parentHeightMeasureSpec, mPaddingHeight + lp.topMargin + lp.bottomMargin, childHeightDimension);
    }

    private int getChildWidthMeasureSpec(int parentWidthMeasureSpec, MarginLayoutParams lp, boolean isGridCell) {
        final int childWidthDimension = lp.width != LayoutParams.MATCH_PARENT ? lp.width : (isGridCell ? LayoutParams.WRAP_CONTENT : lp.width);
        return getChildMeasureSpec(parentWidthMeasureSpec, mPaddingWidth + lp.leftMargin + lp.rightMargin, childWidthDimension);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        //布局的左边
        int layoutLeft = getPaddingLeft();
        //布局的上边
        int layoutTop = getPaddingTop();

        //列数
        int columnNum = 0;
        for (int index = 0; index < getChildCount(); index++) {
            View child = getChildAt(index);
            if (child == null || child.getVisibility() == View.GONE) {
                continue;
            }
            //childView的布局参数，用于获取margin值
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            if (isStickFirst && index == 0) {
                //firstChild置顶
                child.layout(layoutLeft + lp.leftMargin, layoutTop + lp.topMargin,
                        layoutLeft + lp.leftMargin + child.getMeasuredWidth(), layoutTop + lp.topMargin + child.getMeasuredHeight());
                //使布局上边下移
                layoutTop += child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin + mGridLineWidth;
            } else {
                //若当前列数大于网格列数，则换行
                if (columnNum >= mColumnCount) {
                    columnNum = 0;
                    layoutLeft = getPaddingLeft();
                    layoutTop += mGridCellHeight + mGridLineWidth;
                }
                //当前单元格的实际宽度
                int gridCellWidth = columnNum < mRemnantWidth ? (mGridCellWidth + 1) : mGridCellWidth;

                //当前单元格中child的上下左右值
                int childLeft = layoutLeft + lp.leftMargin;
                int childTop = layoutTop + lp.topMargin;
                int childRight = layoutLeft + gridCellWidth - lp.rightMargin;
                int childBottom = layoutTop + mGridCellHeight - lp.bottomMargin;
                if ((mGridCellGravity & GRAVITY_FILL) != GRAVITY_FILL) {//非填满对齐方式
                    //根据水平对齐方式调整左右值
                    if ((mGridCellGravity & GRAVITY_LEFT) == GRAVITY_LEFT) {//水平居左
                        childRight = Math.min(childRight, childLeft + child.getMeasuredWidth());
                    } else if ((mGridCellGravity & GRAVITY_RIGHT) == GRAVITY_RIGHT) {//水平居右
                        childLeft = Math.max(childLeft, childRight - child.getMeasuredWidth());
                    } else if ((mGridCellGravity & GRAVITY_CENTER) == GRAVITY_CENTER) {//水平居中
                        childLeft = Math.max(childLeft, (int) ((childLeft + childRight - child.getMeasuredWidth()) / 2F + 0.5F));
                        childRight = Math.min(childRight, childLeft + child.getMeasuredWidth());
                    }
                    //根据垂直对齐方式调整上下值
                    if ((mGridCellGravity & GRAVITY_TOP) == GRAVITY_TOP) {//垂直居上
                        childBottom = Math.min(childBottom, childTop + child.getMeasuredHeight());
                    } else if ((mGridCellGravity & GRAVITY_BOTTOM) == GRAVITY_BOTTOM) {//垂直居下
                        childTop = Math.max(childTop, childBottom - child.getMeasuredHeight());
                    } else if ((mGridCellGravity & GRAVITY_CENTER) == GRAVITY_CENTER) {//垂直居中
                        childTop = (int) Math.max(childTop, (childTop + childBottom - child.getMeasuredHeight()) / 2F + 0.5F);
                        childBottom = Math.min(childBottom, childTop + child.getMeasuredHeight());
                    }
                }
                child.layout(childLeft, childTop, childRight, childBottom);
                columnNum++;
                layoutLeft += gridCellWidth + mGridLineWidth;
            }
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        mGridDrawTop = 0;
        if (isStickFirst) {
            View child = getChildAt(0);
            if (child != null && child.getVisibility() != View.GONE) {
                final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                mGridDrawTop = child.getBottom() + lp.bottomMargin + mGridLineWidth;
            }
        }
        super.dispatchDraw(canvas);
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        //网格线的颜色为透明，则不进行绘制
        if (mGridLineWidth <= 0 || mGridLineColor >>> 24 <= 0 || child.getVisibility() == GONE) {
            return super.drawChild(canvas, child, drawingTime);
        }
        if (isStickFirst && child == getChildAt(0)) {
            //绘制置顶的firstChild下边网格线
            canvas.drawRect(0, mGridDrawTop - mGridLineWidth, getWidth(), mGridDrawTop, mGridLinePaint);
        } else {
            //计算当前child所在的行
            int rowNum = (child.getBottom() - mGridDrawTop) / (mGridCellHeight + mGridLineWidth);
            //计算当前child所在的列（要考虑网格宽度余量的影响）
            int columnNum = (child.getRight() - getPaddingLeft()) / (mGridCellWidth + mGridLineWidth + 1);
            if (columnNum >= mRemnantWidth) {
                columnNum = (child.getRight() - getPaddingLeft() - mRemnantWidth) / (mGridCellWidth + mGridLineWidth);
            }
            //计算当前child所在单元格的底边
            int gridCellBottom = mGridDrawTop + rowNum * (mGridCellHeight + mGridLineWidth) + mGridCellHeight;
            //计算当前child所在单元格的右边
            int gridCellRight = getPaddingLeft() + columnNum * (mGridCellWidth + mGridLineWidth) + mGridCellWidth;
            //宽存在余量，需要校正单元格的右边
            if (mRemnantWidth > 0) {
                gridCellRight += Math.min(mRemnantWidth, columnNum + 1);
            }
            //当前child为该行的第一个单元格且不是最后一行，绘制下边网格线
            if (columnNum == 0 && rowNum < mRowCount - 1) {
                canvas.drawRect(0, gridCellBottom, getWidth(), gridCellBottom + mGridLineWidth, mGridLinePaint);
            }
            //当前child不是最后一列，绘制child右边网格线
            if (columnNum < mColumnCount - 1) {
                canvas.drawRect(gridCellRight, gridCellBottom - mGridCellHeight, gridCellRight + mGridLineWidth, gridCellBottom, mGridLinePaint);
            }
        }
        return super.drawChild(canvas, child, drawingTime);
    }

    /**
     * 设置单元格尺寸宽度，单位px
     */
    public void setGridCellSize(int width, int height) {
        if (mAppointGridCellWidth != width || mAppointGridCellHeight != height) {
            mAppointGridCellWidth = width;
            mAppointGridCellHeight = height;
            super.requestLayout();
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
        if (mGridLineWidth != width) {
            mGridLineWidth = width;
            super.requestLayout();
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
        if (mGridLineColor != color) {
            mGridLineColor = color;
            mGridLinePaint.setColor(mGridLineColor);
            super.invalidate();
        }
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
        if (isStickFirst != stick) {
            isStickFirst = stick;
            super.requestLayout();
        }
    }

    /**
     * 获取第一个子View是否置顶
     */
    public boolean isStickFirst() {
        return isStickFirst;
    }

    /**
     * 设置单元格对齐方式
     */
    public void setGridCellGravity(int gravity) {
        if (mGridCellGravity != gravity) {
            mGridCellGravity = gravity;
            super.requestLayout();
        }
    }

    /**
     * 获取单元格对齐方式
     */
    public int getGridCellGravity() {
        return mGridCellGravity;
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
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
