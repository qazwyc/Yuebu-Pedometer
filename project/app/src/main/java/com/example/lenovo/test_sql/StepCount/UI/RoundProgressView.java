package com.example.lenovo.test_sql.StepCount.UI;

/**
 * Created by lenovo on 2016/12/6/006.
 */
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/****
 * 圆圈进度控件
 */
public class RoundProgressView extends View {
    /**最外围的颜色值*/
    private int mOutRoundColor = Color.argb(0, 255, 255, 255);
    /**中心圆的颜色值*/
    private int mCenterRoundColor = Color.argb(255, 255, 255, 255);
    /**进度的颜色*/
    private int mProgressRoundColor = Color.argb(255, 118, 181, 66);
    /**进度的背景颜色*/
    private int mProgressRoundBgColor = Color.argb(100, 255, 255, 255);
    /**进度条的宽度*/
    private int mProgressWidth = 10;

    private int mWidth,mHeight;
    private int mPaddingX;

    private float mProgress = 0.5f;
    private float mMax = 1.0f;

    private Paint mPaint = new Paint();

    public RoundProgressView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public RoundProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundProgressView(Context context) {
        super(context);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mWidth = getWidth();
        mHeight = getHeight();

        if(mWidth > mHeight){
            mPaddingX = (mWidth-mHeight)/2;
            mWidth = mHeight;
        }

        int halfWidth = mWidth/8;
        mPaint.setAntiAlias(true); // 消除锯齿
        mPaint.setStrokeWidth(mProgressWidth);
        mPaint.setStyle(Style.STROKE);
        mPaint.setColor(mProgressRoundBgColor);
        RectF oval = new RectF(new Rect(halfWidth+mPaddingX, halfWidth, halfWidth*7+mPaddingX, halfWidth*7));
        canvas.drawArc(oval, 0, 360, false, mPaint);

        mPaint.setColor(mProgressRoundColor);
        canvas.drawArc(oval, 90, 360*mProgress/mMax, false, mPaint);

        halfWidth = mWidth/6;
        mPaint.setStyle(Style.FILL);
        mPaint.setColor(mCenterRoundColor);
        oval = new RectF(new Rect(halfWidth+mPaddingX, halfWidth, halfWidth*5+mPaddingX, halfWidth*5));
        canvas.drawArc(oval, 0, 360, false, mPaint);
    }

    public void setMax(float mMax) {
        this.mMax = mMax;
    }

    public void setProgress(float mProgress) {
        this.mProgress = mProgress;
        invalidate();
    }

    public float getMax() {
        return mMax;
    }

    public float getProgress() {
        return mProgress;
    }

}