/*
 * Mantou Earth - Live your wallpaper with live earth
 * Copyright (C) 2015-2019 XiNGRZ <xxx@oxo.ooo>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ooo.oxo.apps.earth.widget;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class ScalingLayout extends FrameLayout {

    private static final float SCALE_FACTOR = 0.75f;

    private static final int GRID_ALPHA = 0x30;

    private final Paint grid;

    private final float slop;

    private final GestureDetector scrolling;

    private final ScaleGestureDetector scaling;

    private final float[] splitX = new float[1];
    private final float[] splitY = new float[1];

    private final ValueAnimator gridAnimator;

    private View child;

    private float translationX = 0f;
    private float translationY = 0f;

    private float offsetLong = 0f;
    private float offsetShort = 0f;

    private int activeGridX = -1;
    private int activeGridY = -1;

    private float scale = 0f;

    private boolean editMode = false;

    private OnScalingChangeListener onScalingChangeListener;

    public ScalingLayout(Context context) {
        this(context, null);
    }

    public ScalingLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScalingLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setWillNotDraw(false);

        final float density = getResources().getDisplayMetrics().density;

        this.grid = new Paint();
        grid.setColor(Color.WHITE);
        grid.setAlpha(GRID_ALPHA);
        grid.setStyle(Paint.Style.STROKE);
        grid.setStrokeWidth(1 * density);
        grid.setAntiAlias(true);

        this.slop = ViewConfiguration.get(context).getScaledTouchSlop();

        this.scrolling = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (editMode && child != null) {
                    handleScroll(distanceX, distanceY);
                    return true;
                } else {
                    return false;
                }
            }

        });

        this.scaling = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return editMode && child != null;
            }

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                handleScale(detector.getScaleFactor());
                return true;
            }

        });

        this.scrolling.setOnDoubleTapListener(new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return editMode || performClick();
            }

        });

        gridAnimator = new ValueAnimator();
        gridAnimator.setDuration(200);
        gridAnimator.addUpdateListener(animation -> {
            grid.setAlpha((int) animation.getAnimatedValue());
            invalidate();
        });
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        this.child = child;
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                showGrid();
                break;
            case MotionEvent.ACTION_UP:
                hideGrid();
                break;
        }
        final boolean scrolled = scrolling.onTouchEvent(event);
        final boolean scaled = scaling.onTouchEvent(event);
        return scrolled || scaled || super.onTouchEvent(event);
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int wh) {
        super.onSizeChanged(w, h, ow, wh);

        for (int i = 0; i < splitX.length; i++) {
            splitX[i] = (float) w / (splitX.length + 1f) * (i + 1f);
        }

        for (int i = 0; i < splitY.length; i++) {
            splitY[i] = (float) h / (splitY.length + 1f) * (i + 1f);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        updateOffset();
        super.onLayout(changed, left, top, right, bottom);
    }

    private void handleScroll(float dx, float dy) {
        translationX -= dx * SCALE_FACTOR;
        translationY -= dy * SCALE_FACTOR;

        float actualTranslationX = translationX;
        float actualTranslationY = translationY;

        final float centerX = getWidth() / 2f;
        final float absoluteX = centerX + translationX;

        for (int i = 0; i < splitX.length; i++) {
            final float x = splitX[i];
            if (Math.abs(absoluteX - x) < slop) {
                actualTranslationX = x - centerX;
                activeGridX = i;
                break;
            } else {
                activeGridX = -1;
            }
        }

        final float centerY = getHeight() / 2f;
        final float absoluteY = centerY + translationY;

        for (int i = 0; i < splitY.length; i++) {
            final float y = splitY[i];
            if (Math.abs(absoluteY - y) < slop) {
                actualTranslationY = y - centerY;
                activeGridY = i;
                break;
            } else {
                activeGridY = -1;
            }
        }

        child.setTranslationX(actualTranslationX);
        child.setTranslationY(actualTranslationY);

        if (getWidth() > getHeight()) {
            offsetLong = actualTranslationX;
            offsetShort = actualTranslationY;
        } else {
            offsetShort = actualTranslationX;
            offsetLong = actualTranslationY;
        }

        invalidate();
        notifyScalingChanged();
    }

    private void handleScale(float factor) {
        scale = child.getScaleX() * factor;

        child.setScaleX(scale);
        child.setScaleY(scale);

        invalidate();
        notifyScalingChanged();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (child == null || !editMode) {
            return;
        }

        if (activeGridX != -1 && activeGridX < splitX.length) {
            final float x = splitX[activeGridX];
            canvas.drawLine(x, 0, x, getHeight(), grid);
        }

        if (activeGridY != -1 && activeGridY < splitY.length) {
            final float y = splitY[activeGridY];
            canvas.drawLine(0, y, getWidth(), y, grid);
        }
    }

    private void updateOffset() {
        if (getWidth() > getHeight()) {
            translationX = offsetLong;
            translationY = offsetShort;
        } else {
            translationX = offsetShort;
            translationY = offsetLong;
        }

        child.setTranslationX(translationX);
        child.setTranslationY(translationY);
    }

    public float getOffsetLong() {
        return offsetLong;
    }

    public void setOffsetLong(float offsetLong) {
        if (this.offsetLong == offsetLong) return;
        this.offsetLong = offsetLong;
        updateOffset();
        invalidate();
        notifyScalingChanged();
    }

    public float getOffsetShort() {
        return offsetShort;
    }

    public void setOffsetShort(float offsetShort) {
        if (this.offsetShort == offsetShort) return;
        this.offsetShort = offsetShort;
        updateOffset();
        invalidate();
        notifyScalingChanged();
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        if (this.scale == scale) return;
        this.scale = scale;
        child.setScaleX(scale);
        child.setScaleY(scale);
        invalidate();
        notifyScalingChanged();
    }

    public void enterEditMode() {
        editMode = true;
    }

    public void exitEditMode() {
        editMode = false;
    }

    public boolean isEditMode() {
        return editMode;
    }

    private void showGrid() {
        gridAnimator.cancel();
        grid.setAlpha(GRID_ALPHA);
    }

    private void hideGrid() {
        gridAnimator.cancel();
        gridAnimator.setIntValues(grid.getAlpha(), 0x00);
        gridAnimator.start();
    }

    private void notifyScalingChanged() {
        synchronized (this) {
            if (onScalingChangeListener != null) {
                onScalingChangeListener.onScalingChanged(this, offsetLong, offsetShort, scale);
            }
        }
    }

    public void setOnScalingChangeListener(OnScalingChangeListener onScalingChangeListener) {
        this.onScalingChangeListener = onScalingChangeListener;
    }

    public interface OnScalingChangeListener {

        void onScalingChanged(ScalingLayout layout, float offsetLong, float offsetShort, float scale);

    }

}
