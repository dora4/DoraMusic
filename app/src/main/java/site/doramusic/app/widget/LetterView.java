package site.doramusic.app.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.Locale;

import site.doramusic.app.R;

public class LetterView extends View {

    private TextPaint mPaint;
    private float mTextSize;
    private int mTextColor;
    private int mHoverTextColor;
    private Drawable mBackgroundDrawable;
    private Drawable mHoverBackgroundDrawable;
    private String[] mLetters = {"A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z", "#"};
    private boolean mTextAllCaps;
    private int mSelected = -1;
    private final int DEFAULT_TEXT_COLOR = 0xFF000000;
    private final int DEFAULT_HOVER_TEXT_COLOR = 0xFF000000;
    private DisplayMetrics mMetrics;
    private Locale mLocale;
    private OnLetterChangeListener mOnLetterChangeListener;

    public LetterView(Context context) {
        this(context, null);
    }

    public LetterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LetterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mMetrics = getResources().getDisplayMetrics();
        mLocale = getResources().getConfiguration().locale;
        mBackgroundDrawable = getBackground();
        initAttrs(context, attrs, defStyleAttr);
        initPaint();
    }

    private void initAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LetterView, defStyleAttr, 0);
        mLetters = parseLetters(a.getString(R.styleable.LetterView_letterview_letters));
        mTextSize = a.getDimension(R.styleable.LetterView_letterview_textSize,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15, mMetrics));
        mTextColor = a.getColor(R.styleable.LetterView_letterview_textColor, DEFAULT_TEXT_COLOR);
        mHoverTextColor = a
                .getColor(R.styleable.LetterView_letterview_hoverTextColor, DEFAULT_HOVER_TEXT_COLOR);
        mHoverBackgroundDrawable = a.getDrawable(R.styleable.LetterView_letterview_hoverBackgroundDrawable);
        mTextAllCaps = a.getBoolean(R.styleable.LetterView_letterview_textAllCaps, false);
        a.recycle();
    }

    private String[] parseLetters(String letters) {
        String[] values;
        if (letters == null) {
            values = mLetters;
        } else {
            int length = letters.length();
            values = new String[length];
            if (!TextUtils.isEmpty(letters)) {
                for (int i = 0; i < letters.length(); i++) {
                    String letter = String.valueOf(letters.charAt(i));
                    if (mTextAllCaps) {
                        letter = letter.toUpperCase(mLocale);
                    }
                    values[i] = letter;
                }
            }
        }
        return values;
    }

    private void initPaint() {
        mPaint = new TextPaint();
        mPaint.setTextSize(mTextSize);
        mPaint.setColor(mTextColor);
        mPaint.setTypeface(Typeface.DEFAULT);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mLetters.length > 0) {
            int width = getWidth();
            int height = getHeight();
            int singleHeight = height / mLetters.length;
            for (int i = 0; i < mLetters.length; i++) {
                if (i == mSelected) {
                    mPaint.setTypeface(Typeface.DEFAULT_BOLD);
                    mPaint.setColor(mHoverTextColor);
                } else {
                    mPaint.setTypeface(Typeface.DEFAULT);
                    mPaint.setColor(mTextColor);
                }
                float x = width / 2 - mPaint.measureText(mLetters[i]) / 2;
                float y = singleHeight * (i + 1);
                canvas.drawText(mLetters[i], x, y, mPaint);
            }
        }
    }

    public void setLetters(String[] letters) {
        if (letters != mLetters) {
            this.mLetters = letters;
            invalidateView();
        }
    }

    public void setTextSize(float size) {
        if (size != mTextSize) {
            this.mTextSize = size;
            invalidateView();
        }
    }

    public void setTextColor(int color) {
        if (color != mTextColor) {
            this.mTextColor = color;
            invalidateView();
        }
    }

    public void setTextColorResource(int resId) {
        setTextColor(getResources().getColor(resId));
    }

    public void setHoverTextColor(int color) {
        if (color != mHoverTextColor) {
            this.mHoverTextColor = color;
            invalidateView();
        }
    }

    public void setHoverTextColorResource(int resId) {
        setHoverTextColor(getResources().getColor(resId));
    }

    public void setHoverBackgroundDrawable(Drawable drawable) {
        if (!drawable.getConstantState().equals(mHoverBackgroundDrawable.getConstantState())) {
            this.mHoverBackgroundDrawable = drawable;
            invalidateView();
        }
    }

    public void setTextAllCaps(boolean caps) {
        if (caps != mTextAllCaps) {
            mTextAllCaps = caps;
            invalidateView();
        }
    }

    public float getTextSize() {
        return mTextSize;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public int getHoverTextColor() {
        return mHoverTextColor;
    }

    public Drawable getHoverBackgroundDrawable() {
        return mHoverBackgroundDrawable;
    }

    public boolean isTextAllCaps() {
        return mTextAllCaps;
    }

    public void setOnLetterChangeListener(OnLetterChangeListener l) {
        mOnLetterChangeListener = l;
    }

    public interface OnLetterChangeListener {
        void onChanged(String letter);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = measureWidth(widthMeasureSpec);
        setMeasuredDimension(measuredWidth, MeasureSpec.getSize(heightMeasureSpec));
    }

    private int measureWidth(int widthMeasureSpec) {
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST) {
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, mMetrics);
        }
        return MeasureSpec.getSize(widthMeasureSpec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int height = getHeight();
        float y = event.getY();
        int index = (int) (y * mLetters.length / height);
        switch (action) {
            case MotionEvent.ACTION_UP:
                setBackgroundDrawable(mBackgroundDrawable);
                mSelected = -1;
                invalidateView();
                break;
            default:
                setBackgroundDrawable(mHoverBackgroundDrawable);
                if (index < mLetters.length && index >= 0) {
                    mSelected = index;
                    invalidateView();
                    if (mOnLetterChangeListener != null) {
                        mOnLetterChangeListener.onChanged(mLetters[index]);
                    }
                }
                break;
        }
        return true;
    }

    /**
     * The refresh view operation of the automatic processing thread.
     */
    private void invalidateView() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }
}

