package com.tatait.tatamusic.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.tatait.tatamusic.R;

import java.util.Random;

public class AuthCodeView extends View {
    // 点数设置
    public static final int POINT_NUM = 100;
    // 线段数设置
    public static final int LINE_NUM = 2;
    //文本
    private String mTitleText;
    // 文本的颜色
    private int mTitleTextColor;
    // 文本的大小
    private int mTitleTextSize;

    String[] mCheckNum = new String[4];
    Random random = new Random();

    //绘制时控制文本绘制的范围
    private Rect mBound;
    private Paint mPaint;

    public AuthCodeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AuthCodeView(Context context) {
        this(context, null);
    }

    /**
     * 获得我自定义的样式属性
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    public AuthCodeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        /**
         * 获得我们所定义的自定义样式属性
         */
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AuthCodeView, defStyle, 0);

        //获取在attr文件下，名字为AuthCodeView的declare-styleable属性有几个
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                //这个属性可以不要，因为都是随机产生
                case R.styleable.AuthCodeView_titleText:
                    mTitleText = a.getString(attr);
                    break;
                case R.styleable.AuthCodeView_titleTextColor:
                    // 默认颜色设置为黑色
                    mTitleTextColor = a.getColor(attr, Color.BLACK);
                    break;
                case R.styleable.AuthCodeView_titleTextSize:
                    // 默认设置为16sp，TypeValue也可以把sp转化为px
                    mTitleTextSize = a.getDimensionPixelSize(attr, (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics()));
                    break;

            }

        }
        a.recycle();

        mTitleText = randomText();

        /**
         * 获得绘制文本的宽和高
         */
        mPaint = new Paint();
        mPaint.setTextSize(mTitleTextSize);
        mBound = new Rect();
        mPaint.getTextBounds(mTitleText, 0, mTitleText.length(), mBound);

        this.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mTitleText = randomText();
                postInvalidate();
            }

        });

    }

    //随机产生验证码
    public String randomText() {
        StringBuffer sbReturn = new StringBuffer();
        for (int i = 0; i < 4; i++) {
            StringBuffer sb = new StringBuffer();
            int randomInt = random.nextInt(10);
            mCheckNum[i] = sb.append(randomInt).toString();
            sbReturn.append(randomInt);
        }

        return sbReturn.toString();
    }

    //获取验证码
    public String getAuthCode() {
        return mTitleText;
    }

    //重写这个方法，设置自定义view控件的大小
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = 0;
        int height = 0;

        /**
         * 设置宽度
         */
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        int specSize = MeasureSpec.getSize(widthMeasureSpec);
        switch (specMode) {
            case MeasureSpec.EXACTLY:// 明确指定了
                width = getPaddingLeft() + getPaddingRight() + specSize;
                break;
            case MeasureSpec.AT_MOST:// 一般为WARP_CONTENT
                width = getPaddingLeft() + getPaddingRight() + mBound.width();
                break;
        }

        /**
         * 设置高度
         */
        specMode = MeasureSpec.getMode(heightMeasureSpec);
        specSize = MeasureSpec.getSize(heightMeasureSpec);
        switch (specMode) {
            case MeasureSpec.EXACTLY:// 明确指定了
                height = getPaddingTop() + getPaddingBottom() + specSize;
                break;
            case MeasureSpec.AT_MOST:// 一般为WARP_CONTENT
                height = getPaddingTop() + getPaddingBottom() + mBound.height();
                break;
        }

        setMeasuredDimension(width, height);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        //画背景颜色
        mPaint.setColor(Color.BLUE);
        canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), mPaint);

        //划线
        mPaint.setColor(mTitleTextColor);
        int[] line;
        for (int i = 0; i < LINE_NUM; i++) {
            //设置线宽
            mPaint.setStrokeWidth(5);
            line = getLine(getMeasuredHeight(), getMeasuredWidth());
            canvas.drawLine(line[0], line[1], line[2], line[3], mPaint);
        }

        // 绘制小圆点
        int[] point;
        int randomInt;
        for (int i = 0; i < POINT_NUM; i++) {
            //随机获取点的大小
            randomInt = random.nextInt(5);
            point = getPoint(getMeasuredHeight(), getMeasuredWidth());
            canvas.drawCircle(point[0], point[1], randomInt, mPaint);
        }

        //绘制验证控件上的文本
        int dx = 20;
        for (int i = 0; i < 4; i++) {
            canvas.drawText("" + mCheckNum[i], dx, getHeight() / 2 + getPositon(mBound.height() / 2), mPaint);
            dx += (getWidth() / 2 - mBound.width() / 2) + i / 5 + 20;
        }
//		canvas.drawText(mTitleText, getWidth() / 2 - mBound.width() / 2, getHeight() / 2 + mBound.height() / 2, mPaint);
    }

    //计算验证码的绘制y点位置
    private int getPositon(int height) {
        int tempPositoin = (int) (Math.random() * height);
        if (tempPositoin < 20) {
            tempPositoin += 20;
        }
        return tempPositoin;
    }

    // 随机产生点的圆心点坐标
    public static int[] getPoint(int height, int width) {
        int[] tempCheckNum = {0, 0, 0, 0};
        tempCheckNum[0] = (int) (Math.random() * width);
        tempCheckNum[1] = (int) (Math.random() * height);
        return tempCheckNum;
    }

    //随机产生划线的起始点坐标和结束点坐标
    public static int[] getLine(int height, int width) {
        int[] tempCheckNum = {0, 0, 0, 0};
        for (int i = 0; i < 4; i += 2) {
            tempCheckNum[i] = (int) (Math.random() * width);
            tempCheckNum[i + 1] = (int) (Math.random() * height);
        }
        return tempCheckNum;
    }
}