package com.example.proyectofinal;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ConfettiView extends View {

    private static final int PARTICLE_COUNT = 60;
    private static final int[] COLORS = {0xFF84F527, 0xFFFFFFFF, 0xFF888888, 0xFF9ED700, 0xFFFFC107};

    private final List<Particle> particles = new ArrayList<>();
    private final Random rnd = new Random();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private ValueAnimator animator;

    public ConfettiView(Context context) {
        super(context);
        init();
    }

    public ConfettiView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particles.add(new Particle());
        }
    }

    public void start() {
        for (Particle p : particles) p.reset();
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(3000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(a -> {
            float dt = 0.016f;
            for (Particle p : particles) p.update(dt);
            invalidate();
        });
        animator.start();
    }

    public void stop() {
        if (animator != null) animator.cancel();
        particles.clear();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stop();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Particle p : particles) {
            paint.setColor(p.color);
            paint.setAlpha((int) (255 * p.alpha));
            canvas.drawRect(p.x, p.y, p.x + p.size, p.y + p.size, paint);
        }
    }

    private class Particle {
        float x, y, size, speed, alpha, sway;
        int color;

        void reset() {
            x = rnd.nextFloat() * 1080;
            y = -rnd.nextFloat() * 2000;
            size = 6 + rnd.nextFloat() * 10;
            speed = 300 + rnd.nextFloat() * 500;
            alpha = 0.6f + rnd.nextFloat() * 0.4f;
            sway = rnd.nextFloat() * 60 - 30;
            color = COLORS[rnd.nextInt(COLORS.length)];
        }

        void update(float dt) {
            y += speed * dt;
            x += (float) Math.sin(y / 100) * sway * dt;
            if (y > 2000) {
                y = -size;
                x = rnd.nextFloat() * 1080;
            }
        }
    }
}
