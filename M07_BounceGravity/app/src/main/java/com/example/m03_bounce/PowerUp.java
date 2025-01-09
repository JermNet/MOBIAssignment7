package com.example.m03_bounce;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

// A powerup class, kind of similar to the rectangle since the effects are applied in bbview (not best way to do it, but it works)
public class PowerUp {
    private float x, y;
    private PowerUpType type;
    private Paint paint;
    private RectF bounds;
    private float size = 25;

    public PowerUp(float x, float y, PowerUpType type) {
        this.x = x;
        this.y = y;
        this.type = type;
        paint = new Paint();
        paint.setColor(Color.GREEN);
        bounds = new RectF(x - size / 2, y - size / 2, x + size / 2, y + size / 2);
    }

    public void draw(Canvas canvas) {
        canvas.drawOval(bounds, paint);
    }

    // Use built in intersects method (past me WISHES he knew about this method [documentation is truly an amazing thing])
    public boolean checkCollision(Ball b) {
        return RectF.intersects(bounds, b.getBounds());
    }

    // Get type from the enum
    public PowerUpType getType() {
        return type;
    }

}
