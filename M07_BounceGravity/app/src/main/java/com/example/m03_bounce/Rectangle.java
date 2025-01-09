package com.example.m03_bounce;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

// Rectangle class for the ball to bounce off and "destroy" to get points
public class Rectangle {
    private float x, y, width, height;
    private Paint paint;
    private RectF bounds;

    public Rectangle(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        paint = new Paint();
        paint.setColor(Color.YELLOW);
        bounds = new RectF(x, y, x + width, y + height);
    }

    public void draw(Canvas canvas) {
        canvas.drawRect(bounds, paint);
    }

    public boolean checkCollision(Ball b) {
        return RectF.intersects(bounds, b.getBounds());
    }

    // Check collision for different directions so that the ball can bounce off
    public boolean isCollisionFromTop(Ball ball) {
        return ball.y + ball.radius <= y && ball.y + ball.radius >= y - ball.speedY;
    }

    public boolean isCollisionFromBottom(Ball ball) {
        return ball.y - ball.radius >= y + height && ball.y - ball.radius <= y + height + ball.speedY;
    }

    public boolean isCollisionFromLeft(Ball ball) {
        return ball.x + ball.radius <= x && ball.x + ball.radius >= x - ball.speedX;
    }

    public boolean isCollisionFromRight(Ball ball) {
        return ball.x - ball.radius >= x + width && ball.x - ball.radius <= x + width + ball.speedX;
    }

}
