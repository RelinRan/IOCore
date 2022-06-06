package androidx.io.core.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.io.core.R;

/**
 * 进度圆圈
 */
public class CircleProgress extends View {

    private int progressColor = Color.parseColor("#62D4E1");
    private int backgroundColor = Color.parseColor("#E5E5E5");
    private int textColor = Color.parseColor("#62D4E1");
    private float strokeWidth = density(8);
    private float radius;
    private float cx, cy;
    private Paint paint;
    private long max = 100;
    private long progress = 0;
    private boolean showText = true;
    private int textSize = (int) density(12);

    public CircleProgress(Context context) {
        super(context);
        initAttributeSet(context, null);
    }

    public CircleProgress(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttributeSet(context, attrs);
    }

    public CircleProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttributeSet(context, attrs);
    }

    private void initAttributeSet(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CircleProgress);
            progressColor = context.getResources().getColor(R.color.io_core_circle_progress_color);
            progressColor = array.getColor(R.styleable.CircleProgress_circleProgressColor, progressColor);
            backgroundColor = array.getColor(R.styleable.CircleProgress_circleBackgroundColor, backgroundColor);
            textColor = context.getResources().getColor(R.color.io_core_circle_progress_text_color);
            textColor = array.getColor(R.styleable.CircleProgress_android_textColor, textColor);
            textSize = array.getDimensionPixelSize(R.styleable.CircleProgress_android_textSize, textSize);
            strokeWidth = array.getDimension(R.styleable.CircleProgress_circleStrokeWidth, strokeWidth);
            max = array.getInt(R.styleable.CircleProgress_android_max, (int) max);
            progress = array.getInt(R.styleable.CircleProgress_android_progress, (int) progress);
            int textVisibility = array.getInt(R.styleable.CircleProgress_circleTextVisibility, 0);
            showText = textVisibility == View.VISIBLE ? true : false;
            array.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        cx = width / 2;
        cy = height / 2;
        radius = width < height ? width / 2 : height / 2;
        radius -= strokeWidth;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(backgroundColor);
        paint.setStyle(Paint.Style.STROKE);
        //背景
        paint.setStrokeWidth(strokeWidth);
        canvas.drawCircle(cx, cy, radius, paint);
        //进度
        paint.setColor(progressColor);
        paint.setStrokeCap(Paint.Cap.ROUND);
        RectF rectF = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);
        float scale = progress * 1.0F / max * 1.0F;
        canvas.drawArc(rectF, -90, 360 * scale, false, paint);
        //文字
        int value = (int) (scale * 100);
        if (isShowText()) {
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(textSize);
            paint.setColor(textColor);
            String text = value + "%";
            Rect bounds = new Rect();
            paint.getTextBounds(text, 0, text.length(), bounds);
            canvas.drawText(text, cx - bounds.width() / 2, cy + bounds.height() / 2, paint);
        }
    }

    public boolean isShowText() {
        return showText;
    }

    public void setShowText(boolean showText) {
        this.showText = showText;
        invalidate();
    }

    /**
     * 设置进度颜色
     *
     * @param progressColor
     */
    public void setProgressColor(int progressColor) {
        this.progressColor = progressColor;
        invalidate();
    }

    /**
     * 设置进度背景颜色
     *
     * @param backgroundColor
     */
    public void setProgressBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        invalidate();
    }

    /**
     * 设置最大值
     *
     * @param max
     */
    public void setMax(long max) {
        this.max = max;
        invalidate();
    }

    /**
     * 设置进度值
     *
     * @param progress
     */
    public void setProgress(long progress) {
        this.progress = progress;
        invalidate();
    }

    /**
     * 设置线条宽度
     *
     * @param strokeWidth
     */
    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = density(strokeWidth);
        invalidate();
    }

    /**
     * dp数值
     *
     * @param value
     * @return
     */
    public float density(int value) {
        return getResources().getDisplayMetrics().density * value;
    }

}
