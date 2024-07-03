package com.mythic3011.itp4501_assignment;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class MathIconView extends View {

    private Paint paint;
    private float rotation = 0;
    private static final float ROTATION_SPEED = 1f;

    public MathIconView(Context context) {
        super(context);
        init();
    }

    public MathIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        int radius = Math.min(width, height) / 4;

        // Rotate the canvas
        canvas.save();
        canvas.rotate(rotation, centerX, centerY);

        // Draw integral symbol
        drawIntegralSymbol(canvas, centerX - radius, centerY - radius, radius * 2, radius * 2);

        // Draw derivative symbol (d/dx)
        drawDerivativeSymbol(canvas, centerX + radius / 2f, centerY - radius, radius);

        // Draw summation symbol (Σ)
        drawSummationSymbol(canvas, centerX - radius, centerY + radius / 2f, radius);

        // Draw limit symbol (lim)
        drawLimitSymbol(canvas, centerX + radius / 2f, centerY + radius, radius);

        canvas.restore();

        // Update rotation for next frame
        rotation += ROTATION_SPEED;
        if (rotation >= 360) {
            rotation = 0;
        }

        // Request next frame
        invalidate();
    }

    private void drawIntegralSymbol(Canvas canvas, float left, float top, float width, float height) {
        Path path = new Path();
        path.moveTo(left + width * 0.3f, top);
        path.cubicTo(left, top + height * 0.3f, left + width * 0.7f, top + height * 0.7f, left + width * 0.4f, top + height);
        canvas.drawPath(path, paint);
    }

    private void drawDerivativeSymbol(Canvas canvas, float x, float y, float size) {
        canvas.drawText("d", x, y + size * 0.4f, paint);
        canvas.drawLine(x, y + size * 0.5f, x + size * 0.6f, y + size * 0.5f, paint);
        canvas.drawText("dx", x, y + size, paint);
    }

    private void drawSummationSymbol(Canvas canvas, float x, float y, float size) {
        canvas.drawLine(x, y, x + size, y, paint);
        canvas.drawLine(x, y, x + size * 0.5f, y + size * 0.5f, paint);
        canvas.drawLine(x + size * 0.5f, y + size * 0.5f, x, y + size, paint);
        canvas.drawLine(x, y + size, x + size, y + size, paint);
    }

    private void drawLimitSymbol(Canvas canvas, float x, float y, float size) {
        canvas.drawText("lim", x, y, paint);
        canvas.drawLine(x, y + size * 0.2f, x + size * 0.6f, y + size * 0.2f, paint);
        canvas.drawText("x→∞", x, y + size * 0.5f, paint);
    }
}