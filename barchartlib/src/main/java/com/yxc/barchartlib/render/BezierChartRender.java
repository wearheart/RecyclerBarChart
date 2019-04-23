package com.yxc.barchartlib.render;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.yxc.barchartlib.bezier.ControlPoint;
import com.yxc.barchartlib.component.YAxis;
import com.yxc.barchartlib.entrys.BarEntry;
import com.yxc.barchartlib.formatter.ValueFormatter;
import com.yxc.barchartlib.util.BarChartAttrs;
import com.yxc.barchartlib.util.ChartComputeUtil;
import com.yxc.barchartlib.util.DecimalUtil;
import com.yxc.barchartlib.util.DisplayUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yxc
 * @since 2019/4/14
 */
final public class BezierChartRender {
    private BarChartAttrs mBarChartAttrs;
    private Paint mBarChartPaint;
    private Paint mTextPaint;
    private Paint mTextMarkPaint;
    private Paint mBezierFillPaint;
    private ValueFormatter mBarChartValueFormatter;
    private ValueFormatter mChartValueMarkFormatter;

    public void setChartValueMarkFormatter(ValueFormatter mChartValueMarkFormatter) {
        this.mChartValueMarkFormatter = mChartValueMarkFormatter;
    }

    public void setBarChartValueFormatter(ValueFormatter mBarChartValueFormatter) {
        this.mBarChartValueFormatter = mBarChartValueFormatter;
    }

    public BezierChartRender(BarChartAttrs barChartAttrs, ValueFormatter barChartValueFormatter, ValueFormatter chartValueMarkFormatter) {
        this.mBarChartAttrs = barChartAttrs;
        initBarChartPaint();
        initTextPaint();
        initTextMarkPaint();
        initBezierFillPaint();
        this.mBarChartValueFormatter = barChartValueFormatter;
        this.mChartValueMarkFormatter = chartValueMarkFormatter;
    }

    private void initTextPaint() {
        mTextPaint = new Paint();
        mTextPaint.reset();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setStrokeWidth(1);
        mTextPaint.setColor(mBarChartAttrs.barChartValueTxtColor);
        mTextPaint.setTextSize(DisplayUtil.dip2px(12));
    }

    private void initTextMarkPaint() {
        mTextMarkPaint = new Paint();
        mTextMarkPaint.reset();
        mTextMarkPaint.setAntiAlias(true);
        mTextMarkPaint.setStyle(Paint.Style.FILL);
        mTextMarkPaint.setStrokeWidth(1);
        mTextMarkPaint.setColor(Color.WHITE);
        mTextMarkPaint.setTextSize(DisplayUtil.dip2px(12));
    }

    private void initBarChartPaint() {
        mBarChartPaint = new Paint();
        mBarChartPaint.reset();
        mBarChartPaint.setAntiAlias(true);
        mBarChartPaint.setStyle(Paint.Style.STROKE);
        mBarChartPaint.setStrokeWidth(DisplayUtil.dip2px(2));
        mBarChartPaint.setColor(mBarChartAttrs.barChartColor);
    }

    private void initBezierFillPaint() {
        mBezierFillPaint = new Paint();
        mBezierFillPaint.reset();
        mBezierFillPaint.setAntiAlias(true);
        mBezierFillPaint.setStyle(Paint.Style.FILL);
        mBezierFillPaint.setColor(mBarChartAttrs.bezierFillColor);
        mBezierFillPaint.setAlpha(mBarChartAttrs.bezierFillAlpha);
    }

    //绘制柱状图顶部value文字
    final public void drawBarChartValue(Canvas canvas, @NonNull RecyclerView parent, YAxis mYAxis) {
        if (mBarChartAttrs.enableCharValueDisplay) {
            float bottom = parent.getHeight() - parent.getPaddingBottom() - mBarChartAttrs.contentPaddingBottom;
            float parentRight = parent.getWidth() - parent.getPaddingRight();
            float parentLeft = parent.getPaddingLeft();
            float realYAxisLabelHeight = bottom - mBarChartAttrs.maxYAxisPaddingTop - parent.getPaddingTop();
            int childCount = parent.getChildCount();

            View child;
            for (int i = 0; i < childCount; i++) {
                child = parent.getChildAt(i);
                BarEntry barEntry = (BarEntry) child.getTag();
                float width = child.getWidth();
                float childCenter = child.getLeft() + width / 2;
                int height = (int) (barEntry.getY() / mYAxis.getAxisMaximum() * realYAxisLabelHeight);
                float top = bottom - height;
                String valueStr = mBarChartValueFormatter.getBarLabel(barEntry);
                float txtY = top - mBarChartAttrs.barChartValuePaddingBottom;
                drawText(canvas, parentLeft, parentRight, valueStr, childCenter, txtY, mTextPaint);
            }
        }
    }


    private void drawText(Canvas canvas, float parentLeft, float parentRight,
                          String valueStr, float childCenter, float txtY, Paint paint) {
//        Log.d("BarChartRender", " valueStr:" + valueStr);
        float widthText = paint.measureText(valueStr);
        float txtXLeft = getTxtX(childCenter, valueStr);
        float txtXRight = txtXLeft + widthText;

        int txtStart = 0;
        int txtEnd = valueStr.length();

        if (DecimalUtil.smallOrEquals(txtXRight, parentLeft)) {//continue 会闪，原因是end == parentLeft 没有过滤掉，显示出来柱状图了。
        } else if (txtXLeft < parentLeft && txtXRight > parentLeft) {//左边部分滑入的时候，处理柱状图、文字的显示
            int displaySize = (int) (valueStr.length() * (txtXRight - parentLeft) / widthText);//比如要显示  "123456"的末两位，需要从 length - displaySize的位置开始显示。
            txtStart = valueStr.length() - displaySize;
            txtXLeft = Math.max(txtXLeft, parentLeft);
            canvas.drawText(valueStr, txtStart, txtEnd, txtXLeft, txtY, paint);
        } else if (DecimalUtil.bigOrEquals(txtXLeft, parentLeft) && DecimalUtil.smallOrEquals(txtXRight, parentRight)) {//中间的
            canvas.drawText(valueStr, txtStart, txtEnd, txtXLeft, txtY, paint);
        } else if (txtXLeft <= parentRight && txtXRight > parentRight) {//右边部分滑出的时候，处理柱状图，文字的显示
            txtXLeft = getTxtX(childCenter, valueStr);
            txtEnd = (int) (valueStr.length() * (parentRight - txtXLeft) / widthText);
            canvas.drawText(valueStr, txtStart, txtEnd, txtXLeft, txtY, paint);
        }
    }

    //获取文字显示的起始 X 坐标
    private float getTxtX(float center, String valueStr) {
        return center - mTextPaint.measureText(valueStr) / 2;
    }

    private PointF getChildPointF(RecyclerView parent, View child, YAxis mYAxis, BarChartAttrs mBarChartAttrs) {
        BarEntry barEntry = (BarEntry) child.getTag();
        RectF rectF = ChartComputeUtil.getBarChartRectF(child, parent, mYAxis, mBarChartAttrs, barEntry);
        float pointX = (rectF.left + rectF.right) / 2;
        float pointY = rectF.top;
        PointF pointF = new PointF(pointX, pointY);
        return pointF;
    }

//    LinearGradient mLinearGradient;
    public void drawBezierChart(Canvas canvas, RecyclerView parent, YAxis mYAxis) {
        float bottom = parent.getHeight() - parent.getPaddingBottom() - mBarChartAttrs.contentPaddingBottom;
        final int childCount = parent.getChildCount();
        List<PointF> originPointFList = new ArrayList<>();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            PointF pointF = getChildPointF(parent, child, mYAxis, mBarChartAttrs);
            originPointFList.add(i, pointF);
        }

        List<ControlPoint> controlList = ControlPoint.getControlPointList(originPointFList, mBarChartAttrs.bezierIntensity);

//        mLinearGradient = new LinearGradient(
//                0,
//                0,
//                0,
//                parent.getMeasuredHeight(),
//                new int[]{
//                        0xffffffff,
//                        ColorUtil.getResourcesColor(parent.getContext(), R.color.pink),
//                        ColorUtil.getResourcesColor(parent.getContext(), R.color.colorPrimary),
//                        ColorUtil.getResourcesColor(parent.getContext(), R.color.colorPrimary),
//                        ColorUtil.getResourcesColor(parent.getContext(), R.color.colorPrimary)},
//                null,
//                Shader.TileMode.CLAMP
//        );
        Path cubicPath = new Path();
        Path cubicFillPath = new Path();
        //贝塞尔曲线获取控制点
        for (int i = 0; i < controlList.size(); i++) {
            if (i == 0) {
                cubicPath.moveTo(originPointFList.get(i).x, originPointFList.get(i).y);
            }
            //画三价贝塞尔曲线
            cubicPath.cubicTo(
                    controlList.get(i).getConPoint1().x, controlList.get(i).getConPoint1().y,
                    controlList.get(i).getConPoint2().x, controlList.get(i).getConPoint2().y,
                    originPointFList.get(i + 1).x, originPointFList.get(i + 1).y
            );
        }


        if (mBarChartAttrs.enableBezierLineFill) {
            cubicFillPath.reset();
            cubicFillPath.moveTo(originPointFList.get(0).x, originPointFList.get(0).y);
            cubicFillPath.addPath(cubicPath);
            // create a new path, this is bad for performance
            drawCubicFill(canvas,  originPointFList, cubicFillPath, bottom);
        }
//        mBarChartPaint.setShader(mLinearGradient);
        canvas.drawPath(cubicPath, mBarChartPaint);
        canvas.save();
    }

    private void drawCubicFill(Canvas c, List<PointF> pointFList, Path spline, float bottom) {
        spline.lineTo(pointFList.get(pointFList.size() - 1).x, bottom);
        spline.lineTo(pointFList.get(0).x, bottom);
        spline.close();
        drawFilledPath(c, spline, mBarChartAttrs.bezierFillColor, mBarChartAttrs.bezierFillAlpha);
    }


    /**
     * Draws the provided path in filled mode with the provided color and alpha.
     * Special thanks to Angelo Suzuki (https://github.com/tinsukE) for this.

     */
    private void drawFilledPath(Canvas canvas, Path filledPath, int fillColor, int fillAlpha) {
        int color = (fillAlpha << 24) | (fillColor & 0xffffff);
        // save
        Paint.Style previous = mBezierFillPaint.getStyle();
        int previousColor = mBezierFillPaint.getColor();
        // set
        mBezierFillPaint.setStyle(Paint.Style.FILL);
        mBezierFillPaint.setColor(color);

//        mBezierFillPaint.setShader(mLinearGradient);
        canvas.drawPath(filledPath, mBezierFillPaint);

        // restore
        mBezierFillPaint.setColor(previousColor);
        mBezierFillPaint.setStyle(previous);
    }

}
