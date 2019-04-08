package com.yxc.barchart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import org.joda.time.LocalDate;

import java.util.List;

/**
 * @author yxc
 * @date 2019/4/6
 * <p>
 * 这个ItemDecoration 是BarChartAdapter专用的，里面直接用到了BarChartAdapter
 */
public class BarChartItemDecoration extends RecyclerView.ItemDecoration {

    private static final String TAG = "DifWidthDecoration";

    private Context mContext;
    private int mOrientation;

    private Paint mDashPaint;
    private Paint mLinePaint;
    private Paint mTextPaint;
    private Paint mBarChartPaint;
    private Paint mBarBorderPaint;

    private BarChartAdapter mAdapter;
    private List<BarEntry> mEntries;
    private int contentPaddingBottom = DisplayUtil.dip2px(15);//底部的 X轴刻度所占的高度
    private int maxYAxisPaddingTop = DisplayUtil.dip2px(10);//顶部显示的预留空间
    private int mBarChartColor;

    private YAxis mYAxis;
    private XAxis mXAxis;

    private boolean enableCharValueDisplay = true;
    private boolean enableYAxisZero = true;
    private boolean enableYAxisGridLine = true;
    private boolean enableRightYAxisLabel = true;
    private boolean enableLeftYAxisLabel = true;
    private boolean enableBarBorder = true;

    private int mBarBorderColor;
    private float mBarBorderWidth;

    public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;
    public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;


    public BarChartItemDecoration(Context context, int orientation, YAxis yAxis, XAxis xAxis) {
        this.mContext = context;
        this.mOrientation = orientation;
        this.mYAxis = yAxis;
        this.mXAxis = xAxis;
        mBarChartColor = ColorUtil.getResourcesColor(mContext, R.color.pink);
        mBarBorderColor = ColorUtil.getResourcesColor(mContext, R.color.black_80_transparent);
        mBarBorderWidth = DisplayUtil.dip2px(0.5f);
        setOrientation(orientation);
        initPaint();
        initDathPaint();
        initTextPaint();
        initBarChartPaint();
        initBarBorderPaint();
    }

    public void setYAxis(YAxis mYAxis) {
        this.mYAxis = mYAxis;
    }

    //设置屏幕方向
    public void setOrientation(int orientation) {
        if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
            throw new IllegalArgumentException("invalid orientation");
        }
        this.mOrientation = orientation;
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        mAdapter = (BarChartAdapter) parent.getAdapter();
        mEntries = mAdapter.getEntries();
        if (mOrientation == HORIZONTAL_LIST) {
            //横向 list 画竖线
            drawLeftYAxisLabel(c, parent, mYAxis);//画左边的刻度，会设定RecyclerView的 leftPadding
            drawRightYAxisLabel(c, parent, mYAxis);//画右边的刻度，会设定RecyclerView的 rightPadding
            drawVerticalLine(c, parent, mXAxis);
            drawGridLine(c, parent, mYAxis);
            drawBarChart(c, parent, state);
            drawBarBorder(c, parent);
        } else if (mOrientation == VERTICAL_LIST) {
            //竖向list 画横线
//            drawHorizontalLine(c, parent, mXAxis);
        }
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
//        super.onDraw(c, parent, state);
//        mAdapter = (BarChartAdapter) parent.getAdapter();
//        mEntries = mAdapter.getEntries();
//
//        if (mOrientation == HORIZONTAL_LIST) {
//            //横向 list 画竖线
//            drawVerticalLine(c, parent, mXAxis);
//            drawGridLine(c, parent, mYAxis);
//            drawRightYAxisLabel(c, parent, mYAxis);
//            drawBarChart(c, parent, state);
//            drawBarBorder(c, parent);
//        } else if (mOrientation == VERTICAL_LIST) {
//            //竖向list 画横线
//            drawHorizontalLine(c, parent, mXAxis);
//        }
    }


    private void drawBarBorder(@NonNull Canvas canvas, @NonNull RecyclerView parent) {
        if (enableBarBorder) {
            RectF rectF = new RectF();
            int start = parent.getPaddingLeft();
            int top = parent.getPaddingTop();
            int end = parent.getRight() - parent.getPaddingRight();
            int bottom = parent.getHeight() - parent.getPaddingBottom() - contentPaddingBottom;//底部有0的刻度是不是不用画，就画折线了。

            rectF.set(start, top, end, bottom);
            mBarBorderPaint.setStrokeWidth(mBarBorderWidth);
            canvas.drawRect(rectF, mBarBorderPaint);
        }
    }

    //绘制柱状图
    private void drawBarChart(Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int bottom = parent.getHeight() - parent.getPaddingBottom() - contentPaddingBottom;
        int parentRight = parent.getWidth() - parent.getPaddingRight();
        int parentLeft = parent.getPaddingLeft();

        Log.d("BarChart", "parentLeft:" + parentLeft);

        int realYAxisLabelHeight = bottom - maxYAxisPaddingTop;
        final int childCount = parent.getChildCount();

        View child;
        for (int i = 0; i < childCount; i++) {
            child = parent.getChildAt(i);
            BarEntry barEntry = (BarEntry) child.getTag();

            int valueInt = (int) barEntry.value;
            ChartRectF rectF = new ChartRectF();
            int width = child.getWidth();
            int barChartWidth = width * 2 / 3;//柱子的宽度
            int start = child.getLeft() + barChartWidth / 4;
            int end = start + barChartWidth;
            Log.d("BarChart", "i =" + i + " start: " + start + " end:" + end);
            int height = (int) (barEntry.value / mYAxis.maxLabel * realYAxisLabelHeight);
            int top = bottom - height;

            mTextPaint.setTextSize(DisplayUtil.sp2px(mContext, 10));
            String valueStr = Integer.toString(valueInt);

            float txtX = getTxtX(child, width, valueStr);
            float txtY = top - DisplayUtil.dip2px(3);
            int txtStart = 0;
            int txtEnd = valueStr.length();

            if (end <= parentLeft) {//continue 会闪，原因是end == parentLeft 没有过滤掉，显示出来柱状图了。
                continue;
            } else if (start < parentLeft && end > parentLeft) {//左边部分滑入的时候，处理柱状图、文字的显示
                start = parentLeft;
                rectF.set(start, top, end, bottom);
                canvas.drawRect(rectF, mBarChartPaint);
                int displaySize = valueStr.length() * (end - parentLeft) / barChartWidth;//比如要显示  "123456"的末两位，需要从 length - displaySize的位置开始显示。
                txtStart = valueStr.length() - displaySize;
                txtX = Math.max(txtX, parentLeft);
                displayCharValue(enableCharValueDisplay, canvas, valueStr, txtStart, txtEnd, txtX, txtY);
            } else if (end < parentRight) {
                rectF.set(start, top, end, bottom);
                canvas.drawRect(rectF, mBarChartPaint);
                displayCharValue(enableCharValueDisplay, canvas, valueStr, txtStart, txtEnd, txtX, txtY);
            } else if (start < parentRight) {//右边部分滑出的时候，处理柱状图，文字的显示
                int distance = (parentRight - start);
                end = start + distance;
                rectF.set(start, top, end, bottom);
                canvas.drawRect(rectF, mBarChartPaint);
                txtX = getTxtX(child, width, valueStr);
                txtEnd = valueStr.length() * (end - start) / barChartWidth;
                displayCharValue(enableCharValueDisplay, canvas, valueStr, txtStart, txtEnd, txtX, txtY);
            }
        }
    }

    //获取文字显示的起始 X 坐标。
    private float getTxtX(View child, int width, String valueStr) {
        float txtDistance = width - mTextPaint.measureText(valueStr);
        float txtX = child.getLeft();
        if (txtDistance > 0) {
            txtX = txtX + txtDistance / 2;
        }
        return txtX;
    }

    //控制char上的value是否显示
    private void displayCharValue(boolean enableCharValueDisplay, Canvas canvas, String valueStr, int start, int end, float x, float y) {
        if (enableCharValueDisplay) {
            canvas.drawText(valueStr, start, end, x, y, mTextPaint);
        }
    }


    private void initPaint() {
        mLinePaint = new Paint();
        mLinePaint.reset();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(1);
        mLinePaint.setColor(Color.GRAY);
    }

    private void initDathPaint() {
        mDashPaint = new Paint();
        mDashPaint.reset();
        mDashPaint.setAntiAlias(true);
        mDashPaint.setStyle(Paint.Style.STROKE);
        mDashPaint.setStrokeWidth(1);
        mDashPaint.setColor(Color.GRAY);
    }

    private void initTextPaint() {
        mTextPaint = new Paint();
        mTextPaint.reset();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setStrokeWidth(1);
        mTextPaint.setColor(Color.GRAY);
        mTextPaint.setTextSize(mXAxis.txtSize);
    }

    private void initBarChartPaint() {
        mBarChartPaint = new Paint();
        mBarChartPaint.reset();
        mBarChartPaint.setAntiAlias(true);
        mBarChartPaint.setStyle(Paint.Style.FILL);
        mBarChartPaint.setColor(mBarChartColor);
    }

    private void initBarBorderPaint() {
        mBarBorderPaint = new Paint();
        mBarBorderPaint.reset();
        mBarBorderPaint.setAntiAlias(true);
        mBarBorderPaint.setStyle(Paint.Style.STROKE);
        mBarBorderPaint.setStrokeWidth(mBarBorderWidth);
        mBarBorderPaint.setColor(mBarBorderColor);
    }

    //绘制 Y轴刻度线
    private void drawGridLine(Canvas canvas, RecyclerView parent, YAxis yAxis) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();
        mLinePaint.setColor(ColorUtil.getResourcesColor(mContext, R.color.black_20_transparent));
        int top = parent.getPaddingTop();
        int bottom = parent.getHeight() - parent.getPaddingBottom();
        int distance = bottom - contentPaddingBottom - maxYAxisPaddingTop;
        int lineNums = yAxis.labelSize;
        int lineDistance = distance / lineNums;
        int gridLine = top + maxYAxisPaddingTop;
        for (int i = 0; i <= lineNums; i++) {
            if (i > 0) {
                gridLine = gridLine + lineDistance;
            }
            Path path = new Path();
            path.moveTo(left, gridLine);
            path.lineTo(right, gridLine);

            boolean enable = false;
            if (i == lineNums && enableYAxisZero) {
                enable = true;
            } else {
                enable = enableYAxisGridLine;//允许画 Y轴刻度
            }
            if (enable) {
                canvas.drawPath(path, mLinePaint);
            }
        }
    }


    //绘制左边的刻度
    private void drawLeftYAxisLabel(Canvas canvas, RecyclerView parent, YAxis yAxis) {
        if (enableLeftYAxisLabel){
            int right = parent.getWidth();
            int top = parent.getPaddingTop();
            int bottom = parent.getHeight() - parent.getPaddingBottom();
            int distance = bottom - contentPaddingBottom - (top + maxYAxisPaddingTop);
            int max = yAxis.maxLabel;
            int lineNums = yAxis.labelSize;
            int lineDistance = distance / lineNums;
            int label = max;
            mTextPaint.setTextSize(yAxis.labelTxtSize);

            String maxStr = Integer.toString(max);

            float textWidth = mTextPaint.measureText(maxStr) + DisplayUtil.dip2px(2);
            parent.setPadding((int) textWidth, parent.getPaddingTop(), parent.getPaddingRight(), parent.getPaddingBottom());
            int labelDistance = max / lineNums;
            int gridLine = top + maxYAxisPaddingTop;

            for (int i = 0; i <= lineNums; i++) {
                if (i > 0) {
                    gridLine = gridLine + lineDistance;
                    label = label - labelDistance;
                }
                String labelStr = Integer.toString(label);
                canvas.drawText(labelStr, textWidth - mTextPaint.measureText(labelStr) - DisplayUtil.dip2px(2),
                        gridLine + DisplayUtil.dip2px(3), mTextPaint);
            }
        }
    }

    //绘制右边的刻度
    private void drawRightYAxisLabel(Canvas canvas, RecyclerView parent, YAxis yAxis) {
        if (enableRightYAxisLabel){
            int right = parent.getWidth();
            int top = parent.getPaddingTop();
            int bottom = parent.getHeight() - parent.getPaddingBottom();
            int distance = bottom - contentPaddingBottom - (top + maxYAxisPaddingTop);
            int max = yAxis.maxLabel;
            int lineNums = yAxis.labelSize;
            int lineDistance = distance / lineNums;
            int label = max;
            mTextPaint.setTextSize(yAxis.labelTxtSize);
            String maxStr = Integer.toString(max);
            float textWidth = mTextPaint.measureText(maxStr) + DisplayUtil.dip2px(3);
            parent.setPadding(parent.getPaddingLeft(), parent.getPaddingTop(), (int) textWidth, parent.getPaddingBottom());

            int labelDistance = max / lineNums;
            int gridLine = top + maxYAxisPaddingTop;

            for (int i = 0; i <= lineNums; i++) {
                if (i > 0) {
                    gridLine = gridLine + lineDistance;
                    label = label - labelDistance;
                }
                String labelStr = Integer.toString(label);
                canvas.drawText(labelStr, right - parent.getPaddingRight() + DisplayUtil.dip2px(2),
                        gridLine + DisplayUtil.dip2px(3), mTextPaint);
            }
        }
    }

    //绘制网格 纵轴线
    private void drawVerticalLine(Canvas canvas, RecyclerView parent, XAxis xAxis) {
        int parentTop = parent.getPaddingTop();
        int parentBottom = parent.getHeight() - parent.getPaddingBottom();
        int parentLeft = parent.getPaddingLeft();
        final int childCount = parent.getChildCount();
        mTextPaint.setTextSize(xAxis.txtSize);
        int parentRight = parent.getWidth() - parent.getPaddingRight();

        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            int adapterPosition = parent.getChildAdapterPosition(child);
            int type = parent.getAdapter().getItemViewType(adapterPosition);
            final RecyclerView.LayoutParams params =
                    (RecyclerView.LayoutParams) child.getLayoutParams();
            final int x = child.getRight();
            if (x > parentRight || x < parentLeft) {//超出的时候就不要画了
                continue;
            }
            BarEntry barEntry = mEntries.get(adapterPosition);
            LocalDate localDate = barEntry.localDate;
            String dateStr = localDate.getDayOfMonth() + "日";

            if (type == BarEntry.TYPE_FIRST || type == BarEntry.TYPE_SPECIAL) {
                if (type == BarEntry.TYPE_SPECIAL) {
                    canvas.drawText(dateStr, x - DisplayUtil.dip2px(3) - mTextPaint.measureText(dateStr),
                            parentBottom - DisplayUtil.dip2px(1), mTextPaint);
                }
                boolean isNextSecondType = isNextEntrySecondType(adapterPosition);
                mLinePaint.setColor(xAxis.barEntryTypeFirstColor);

                Path path = new Path();
                if (isNextSecondType) {
                    path.moveTo(x, parentBottom - contentPaddingBottom);
                } else {
                    path.moveTo(x, parentBottom);
                }
                path.lineTo(x, parentTop);
                canvas.drawPath(path, mLinePaint);
            } else if (type == BarEntry.TYPE_SECOND) {
                //拿到child 的布局信息
                PathEffect pathEffect = new DashPathEffect(new float[]{5, 5, 5, 5}, 1);
                mDashPaint.setPathEffect(pathEffect);
                mDashPaint.setColor(xAxis.barEntryTypeSecondColor);
                Path path = new Path();
                path.moveTo(x, parentBottom - DisplayUtil.dip2px(1));
                path.lineTo(x, parentTop);
                canvas.drawPath(path, mDashPaint);
                canvas.drawText(dateStr, x - DisplayUtil.dip2px(3) - mTextPaint.measureText(dateStr), parentBottom - DisplayUtil.dip2px(1), mTextPaint);
            } else if (type == BarEntry.TYPE_THIRD) {
                //拿到child 的布局信息
                PathEffect pathEffect = new DashPathEffect(new float[]{5, 5, 5, 5}, 1);
                mDashPaint.setPathEffect(pathEffect);
                mDashPaint.setColor(xAxis.barEntryTypeThirdColor);
                Path path = new Path();
                path.moveTo(x, parentBottom - contentPaddingBottom);
                path.lineTo(x, parentTop);
                canvas.drawPath(path, mDashPaint);
            }
        }
    }

    //画月线的时候，下一组、下下组需要写日期。
    private boolean isNextEntrySecondType(int adapterPosition) {
        BarEntry barEntryNext;
        boolean isNextSecondType = false;
        if (adapterPosition + 1 < mEntries.size()) {
            barEntryNext = mEntries.get(adapterPosition + 1);
            if (barEntryNext.type == BarEntry.TYPE_SECOND) {
                isNextSecondType = true;
            }
        }
        if (adapterPosition + 2 < mEntries.size()) {
            barEntryNext = mEntries.get(adapterPosition + 2);
            if (barEntryNext.type == BarEntry.TYPE_SECOND) {
                isNextSecondType = true;
            }
        }
        return isNextSecondType;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.set(0, 0, 0, 0);
    }

    public void setEnableCharValueDisplay(boolean enableCharValueDisplay) {
        this.enableCharValueDisplay = enableCharValueDisplay;
    }

    public void setEnableYAxisZero(boolean enableYAxisZero) {
        this.enableYAxisZero = enableYAxisZero;
    }


    public void setEnableYAxisGridLine(boolean enableYAxisGridLine) {
        this.enableYAxisGridLine = enableYAxisGridLine;
    }


    public void setEnableRightYAxisLabel(boolean enableRightYAxisLabel) {
        this.enableRightYAxisLabel = enableRightYAxisLabel;
    }

    public void setEnableLeftYAxisLabel(boolean enableLeftYAxisLabel) {
        this.enableLeftYAxisLabel = enableLeftYAxisLabel;
    }

    public void setEnableBarBorder(boolean enableBarBorder) {
        this.enableBarBorder = enableBarBorder;
    }

    public void setBarBorderWidth(float mBarBorderWidth) {
        this.mBarBorderWidth = mBarBorderWidth;
        enableBarBorder = true;
    }

}