package com.example.proyectofinal;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class AnimatedBorderView extends FrameLayout {

    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Matrix matrix = new Matrix();
    private final RectF rect = new RectF();
    private float angle = 0f;
    private ValueAnimator animator;

    private int borderWidth = 3;
    private float cornerRadius = 24f;
    private int[] gradientColors = {
            Color.parseColor("#84F527"),
            Color.parseColor("#0B0B0B"),
            Color.parseColor("#4084F527"),
            Color.parseColor("#0B0B0B"),
            Color.parseColor("#84F527")
    };

    public AnimatedBorderView(Context context) {
        super(context);
        init();
    }

    public AnimatedBorderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderWidth);
    }

    public void startAnimation() {
        animator = ValueAnimator.ofFloat(0f, 360f);
        animator.setDuration(3000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.addUpdateListener(a -> {
            angle = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    public void stopAnimation() {
        if (animator != null) animator.cancel();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        rect.set(borderWidth / 2f, borderWidth / 2f,
                w - borderWidth / 2f, h - borderWidth / 2f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float cx = rect.centerX();
        float cy = rect.centerY();
        float diagonal = (float) Math.sqrt(cx * cx + cy * cy);

        Shader shader = new LinearGradient(
                cx - diagonal, cy - diagonal,
                cx + diagonal, cy + diagonal,
                gradientColors, null, Shader.TileMode.CLAMP);

        matrix.reset();
        matrix.postRotate(angle, cx, cy);
        matrix.postTranslate(0, -diagonal / 4f);
        shader.setLocalMatrix(matrix);

        borderPaint.setShader(shader);
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint);
    }
}
