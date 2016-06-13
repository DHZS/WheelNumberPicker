package com.sunny.wheelnumberpicker.lib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.TextView;

import java.text.DecimalFormat;

/**
 * WheelNumberPicker
 * 转轮数字选择器
 * Created by Sunny  An on 2016/5/18.
 */
public class WheelNumberPicker extends TextView {

    /**
     * wheel转动监听器
     */
    public interface OnNumberChangedListener {
        /**
         * 顺时针旋转
         */
        void onClockWise();

        /**
         * 逆时针旋转
         */
        void onAntiClockWise();

        /**
         * 数字改变
         *
         * @param number 改变的数字
         */
        void onNumberChanged(float number);

        /**
         * 设置完成
         *
         * @param number 改变的数字
         */
        void onFinished(float number);
    }

    private static final String TAG = "TAG::WheelNumberPicker";
    private static DecimalFormat df = new DecimalFormat("#.0000");

    private static final String DEFAULT_BIG_CIRCLE_COLOR = "#F44336";
    private static final String DEFAULT_SMALL_CIRCLE_COLOR = "#55FFFFFF";
    private static final String DEFAULT_INDICATOR_COLOR = "#55FFFFFF";
    private static final int DEFAULT_RING_WIDTH_IN_DP = 20;
    private static final int DEFAULT_NUM_OF_BLOCKS = 30;
    private static final float DEFAULT_INITIAL_NUMBER = 0F;
    private static final float DEFAULT_BLOCK_SIZE = 1F;
    private static final String DEFAULT_UNIT_TEXT = "";
    private static final float DEFAULT_UNIT_TEXT_SCALE = 0.5F;

    private Context context;
    private int bigCircleColor;
    private int smallCircleColor;
    private int indicatorColor;
    private int ringWidthInPx;
    private int numOfBlocks;
    private float number;
    //一个格子加或减多少
    private float blockSize;
    private float minNumber;
    private float maxNumber;
    private String unitText;
    private int unitTextSize;

    //当前滑动角度
    float thisAngle;
    //系数,解决指示器在第三象限错误
    private float a = 1;

    private Paint paintBig, paintSmall, paintIndicator;
    //起始向量坐标
    private float lastX, lastY;
    //圆半径
    private float r;
    //滑动的最小角度
    private float minAngle;
    //number是否改变了
    private boolean hasChanged;

    private OnNumberChangedListener onNumberChangedListener;

    public WheelNumberPicker(Context context) {
        super(context);
        this.context = context;
        setGravity(Gravity.CENTER);
        setClickable(true);
    }

    public WheelNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setGravity(Gravity.CENTER);
        setClickable(true);

        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.WheelNumberPicker);
        bigCircleColor = typedArray.getColor(R.styleable.WheelNumberPicker_bigCircleColor, Color.parseColor(DEFAULT_BIG_CIRCLE_COLOR));
        smallCircleColor = typedArray.getColor(R.styleable.WheelNumberPicker_smallCircleColor, Color.parseColor(DEFAULT_SMALL_CIRCLE_COLOR));
        indicatorColor = typedArray.getColor(R.styleable.WheelNumberPicker_indicatorColor, Color.parseColor(DEFAULT_INDICATOR_COLOR));
        ringWidthInPx = typedArray.getDimensionPixelSize(R.styleable.WheelNumberPicker_ringWidth, dip2px(DEFAULT_RING_WIDTH_IN_DP));
        numOfBlocks = typedArray.getInteger(R.styleable.WheelNumberPicker_numOfBlocks, DEFAULT_NUM_OF_BLOCKS);
        number = typedArray.getFloat(R.styleable.WheelNumberPicker_initialNumber, DEFAULT_INITIAL_NUMBER);
        blockSize = typedArray.getFloat(R.styleable.WheelNumberPicker_blockSize, DEFAULT_BLOCK_SIZE);
        minNumber = typedArray.getFloat(R.styleable.WheelNumberPicker_minNumber, -Float.MAX_VALUE);
        maxNumber = typedArray.getFloat(R.styleable.WheelNumberPicker_maxNumber, Float.MAX_VALUE);
        unitText = typedArray.getString(R.styleable.WheelNumberPicker_unitText);
        if (unitText == null) {
            unitText = DEFAULT_UNIT_TEXT;
        }
        unitTextSize = typedArray.getDimensionPixelSize(R.styleable.WheelNumberPicker_unitTextSize, (int) (getTextSize() * DEFAULT_UNIT_TEXT_SCALE));
        typedArray.recycle();

        initPaint();
        setNumOfBlocks(numOfBlocks);
        showNumber(number);
    }

    private void initPaint() {

        paintBig = new Paint();
        paintBig.setColor(bigCircleColor);
        paintBig.setStyle(Paint.Style.FILL);
        paintBig.setAntiAlias(true);

        paintSmall = new Paint(paintBig);
        paintSmall.setColor(smallCircleColor);

        paintIndicator = new Paint(paintSmall);
        paintIndicator.setColor(indicatorColor);
    }


    /**
     * 设置一个圆分成多少块
     */
    public void setNumOfBlocks(int numOfBlocks) {
        this.numOfBlocks = numOfBlocks;
        minAngle = (float) (2 * Math.PI / numOfBlocks);
    }


    public int getBigCircleColor() {
        return bigCircleColor;
    }

    public void setBigCircleColor(int bigCircleColor) {
        this.bigCircleColor = bigCircleColor;
        initPaint();
        invalidate();
    }

    public int getSmallCircleColor() {
        return smallCircleColor;
    }

    public void setSmallCircleColor(int smallCircleColor) {
        this.smallCircleColor = smallCircleColor;
        initPaint();
        invalidate();
    }

    public int getIndicatorColor() {
        return indicatorColor;
    }

    public void setIndicatorColor(int indicatorColor) {
        this.indicatorColor = indicatorColor;
        initPaint();
        invalidate();
    }

    public int getRingWidthInPx() {
        return ringWidthInPx;
    }

    public void setRingWidthInPx(int ringWidthInPx) {
        this.ringWidthInPx = ringWidthInPx;
        invalidate();
    }

    public int getNumOfBlocks() {
        return numOfBlocks;
    }

    public float getNumber() {
        return number;
    }

    public void setNumber(float number) {
        this.number = number;
        showNumber(number);
    }

    public float getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(float blockSize) {
        this.blockSize = blockSize;
    }


    public float getMinNumber() {
        return minNumber;
    }

    public void setMinNumber(float minNumber) {
        this.minNumber = minNumber;
    }

    public float getMaxNumber() {
        return maxNumber;
    }

    public void setMaxNumber(float maxNumber) {
        this.maxNumber = maxNumber;
    }

    public OnNumberChangedListener getOnNumberChangedListener() {
        return onNumberChangedListener;
    }

    public void setOnNumberChangedListener(OnNumberChangedListener onNumberChangedListener) {
        this.onNumberChangedListener = onNumberChangedListener;
    }


    public String getUnitText() {
        return unitText;
    }

    public void setUnitText(String unitText) {
        this.unitText = unitText;
        showNumber(number);
    }

    public int getUnitTextSize() {
        return unitTextSize;
    }

    public void setUnitTextSize(int unitTextSize) {
        this.unitTextSize = unitTextSize;
        showNumber(number);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //位移原点到圆心
        canvas.translate(r, r);
        //画大圆
        canvas.drawOval(new RectF(-r, -r, r, r), paintBig);
        //画小圆
        float smallR = r - ringWidthInPx;
        canvas.drawOval(new RectF(-smallR, -smallR, smallR, smallR), paintSmall);

        //原点到指示器圆心距离
        float oToIndicator = r - 0.5F * ringWidthInPx;
        float x = (float) (Math.cos(thisAngle) * oToIndicator);
        float y = (float) (-a * Math.sin(thisAngle) * oToIndicator);
        //画指示器
        float indicatorR = 0.5F * ringWidthInPx;
        canvas.drawOval(new RectF(x - indicatorR, y - indicatorR, x + indicatorR, y + indicatorR), paintIndicator);
        //还原原点
        canvas.translate(-r, -r);

        super.onDraw(canvas);

    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        r = getWidth() / 2;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float thisX = event.getX() - r;
        float thisY = -event.getY() + r;


        thisAngle = (float) Math.asin(thisY / distance(thisX, thisY, 0, 0));
        a = 1;
        //扩充到第二三象限
        if (thisY >= 0 && thisX <= 0) {
            thisAngle = (float) (Math.PI - thisAngle);
        } else if (thisY < 0 && thisX < 0) {
            a = -1;
            thisAngle = (float) (thisAngle - Math.PI);
        }


        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            lastX = thisX;
            lastY = thisY;
            hasChanged = false;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE && angle(lastX, lastY, thisX, thisY) >= minAngle) {
            //参数，逆时针为-1，顺时针为1
            int b;
            //AxB 叉乘判断旋转方向   x1y2-x2y1  >0逆时针  <0顺时针
            if ((lastX * thisY - thisX * lastY) > 0) {
                if (onNumberChangedListener != null) {
                    onNumberChangedListener.onAntiClockWise();
                }
                b = -1;
            } else {
                if (onNumberChangedListener != null) {
                    onNumberChangedListener.onClockWise();
                }
                b = 1;
            }
            //保证在范围内
            float add = number + b * blockSize;
            if (add < minNumber) {
                number = Float.parseFloat(df.format(minNumber));
            } else if (add > maxNumber) {
                number = Float.parseFloat(df.format(maxNumber));
            } else {
                number = Float.parseFloat(df.format(add));
            }
            if (onNumberChangedListener != null) {
                onNumberChangedListener.onNumberChanged(number);
            }
            showNumber(number);
            lastX = thisX;
            lastY = thisY;
            hasChanged = true;
        } else if (event.getAction() == MotionEvent.ACTION_UP && hasChanged) {
            hasChanged = false;
            if (onNumberChangedListener != null) {
                onNumberChangedListener.onFinished(number);
            }
        }
        invalidate();
        return super.onTouchEvent(event);

    }


    private float distance(float x0, float y0, float x1, float y1) {
        return (float) Math.sqrt(Math.pow(x0 - x1, 2) + Math.pow(y0 - y1, 2));
    }

    private float angle(float x0, float y0, float x1, float y1) {
        return (float) Math.acos((x0 * x1 + y0 * y1) / (distance(x0, y0, 0, 0) * distance(x1, y1, 0, 0)));
    }


    private void log(String format, Object... args) {
        Log.v(TAG, String.format(format, args));
    }

    private int dip2px(float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }


    /**
     * delete extra zero
     * for example  change 6.00 to 6
     */
    private String noZero(String number) {
        if ((number.endsWith("0") && number.contains(".")) || number.endsWith(".")) {
            return noZero(number.substring(0, number.length() - 1));
        } else {
            return number;
        }
    }

    private void showNumber(float number) {
        SpannableString ss = new SpannableString(noZero(number + "") + unitText);
        ss.setSpan(new AbsoluteSizeSpan(unitTextSize), ss.length() - unitText.length(), ss.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        setText(ss);
    }

}