package com.example.m03_bounce;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

// Similar to the powerUp class but with no type
public class DownerUp {
    private float x, y;
    private Paint paint;
    private RectF bounds;
    private float size = 25;

    public DownerUp(float x, float y) {
        this.x = x;
        this.y = y;
        paint = new Paint();
        paint.setColor(Color.RED);
        bounds = new RectF(x - size / 2, y - size / 2, x + size / 2, y + size / 2);
    }

    public void draw(Canvas canvas) {
        canvas.drawOval(bounds, paint);
    }

    // Use built in intersects method (past me WISHES he knew about this method [documentation is truly an amazing thing])
    public boolean checkCollision(Ball b) {
        return RectF.intersects(bounds, b.getBounds());
    }

}
