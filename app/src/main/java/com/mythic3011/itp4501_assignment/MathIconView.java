package com.mythic3011.itp4501_assignment;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

/**
 * Custom view for displaying mathematical symbols that rotate.
 * This view draws an integral symbol, a derivative symbol, a summation symbol, and a limit symbol.
 * The symbols rotate around the center of the view at a constant speed.
 */
public class MathIconView extends View {

    private static final float ROTATION_SPEED = 1f; // Speed of rotation
    private Paint paint; // Paint object for drawing symbols
    private float rotation; // Current rotation angle of the symbols

    /**
     * Constructor for creating the view programmatically.
     *
     * @param context The context of the application.
     */
    public MathIconView(Context context) {
        super(context);
        init();
    }

    /**
     * Constructor for inflating the view from XML.
     *
     * @param context The context of the application.
     * @param attrs   The attributes set specified in XML.
     */
    public MathIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Initializes the paint object for drawing.
     * Sets anti-aliasing to true, stroke style, and stroke width.
     */
    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
    }

    /**
     * Draws the mathematical symbols on the canvas.
     * Symbols are rotated around the center of the view.
     *
     * @param canvas The canvas on which to draw the symbols.
     */
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        int radius = Math.min(width, height) / 4;

        canvas.save();
        canvas.rotate(rotation, centerX, centerY);

        drawIntegralSymbol(canvas, centerX - radius, centerY - radius, radius * 2, radius * 2);
        drawDerivativeSymbol(canvas, centerX + radius / 2f, centerY - radius, radius);
        drawSummationSymbol(canvas, centerX - radius, centerY + radius / 2f, radius);
        drawLimitSymbol(canvas, centerX + radius / 2f, centerY + radius, radius);
        canvas.restore();

        rotation += ROTATION_SPEED;
        if (rotation >= 360) {
            rotation = 0;
        }

        invalidate();
    }

    /**
     * Draws an integral symbol on the canvas.
     *
     * @param canvas The canvas on which to draw.
     * @param left   The left position of the symbol.
     * @param top    The top position of the symbol.
     * @param width  The width of the symbol.
     * @param height The height of the symbol.
     */
    private void drawIntegralSymbol(Canvas canvas, float left, float top, float width, float height) {
        Path path = new Path();
        path.moveTo(left + width * 0.3f, top);
        path.cubicTo(left, top + height * 0.3f, left + width * 0.7f, top + height * 0.7f, left + width * 0.4f, top + height);
        canvas.drawPath(path, paint);
    }

    /**
     * Draws a derivative symbol on the canvas.
     *
     * @param canvas The canvas on which to draw.
     * @param x      The x-coordinate of the symbol's position.
     * @param y      The y-coordinate of the symbol's position.
     * @param size   The size of the symbol.
     */
    private void drawDerivativeSymbol(Canvas canvas, float x, float y, float size) {
        canvas.drawText("d", x, y + size * 0.4f, paint);
        canvas.drawLine(x, y + size * 0.5f, x + size * 0.6f, y + size * 0.5f, paint);
        canvas.drawText("dx", x, y + size, paint);
    }

    /**
     * Draws a summation symbol on the canvas.
     *
     * @param canvas The canvas on which to draw.
     * @param x      The x-coordinate of the symbol's position.
     * @param y      The y-coordinate of the symbol's position.
     * @param size   The size of the symbol.
     */
    private void drawSummationSymbol(Canvas canvas, float x, float y, float size) {
        canvas.drawLine(x, y, x + size, y, paint);
        canvas.drawLine(x, y, x + size * 0.5f, y + size * 0.5f, paint);
        canvas.drawLine(x + size * 0.5f, y + size * 0.5f, x, y + size, paint);
        canvas.drawLine(x, y + size, x + size, y + size, paint);
    }

    /**
     * Draws a limit symbol on the canvas.
     * This method is responsible for drawing the mathematical limit symbol ("lim") followed by "x→∞" to represent the concept of limits approaching infinity.
     * The symbol and text are drawn at specified coordinates with a predefined size, which affects the overall scale of the drawing.
     *
     * @param canvas The canvas on which to draw the limit symbol.
     * @param x      The x-coordinate of the starting point for drawing the limit symbol.
     * @param y      The y-coordinate of the starting point for drawing the limit symbol.
     * @param size   The size of the text and line, influencing the scale of the symbol.
     */
    private void drawLimitSymbol(Canvas canvas, float x, float y, float size) {
        canvas.drawText("lim", x, y, paint); // Draws the "lim" text.
        canvas.drawLine(x, y + size * 0.2f, x + size * 0.6f, y + size * 0.2f, paint); // Draws a line under "lim" to signify the limit operation.
        canvas.drawText("x→∞", x, y + size * 0.5f, paint); // Draws the "x→∞" text, indicating the variable x approaches infinity.
    }
}