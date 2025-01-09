package com.example.m03_bounce;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
public class BouncingBallView extends View implements SensorEventListener {

    private ArrayList<Ball> balls = new ArrayList<>();
    private ArrayList<Rectangle> rectangles = new ArrayList<>();
    // ArrayList for the other cool things that I have added
    private ArrayList<PowerUp> powerUps = new ArrayList<>();
    private ArrayList<DownerUp> downerUps = new ArrayList<>();
    private ArrayList<String> effects = new ArrayList<>();
    private Box box;
    // Score!
    private int score = 0;
    // Health!
    private int health = 10;
    // A time for the last power up spawn, and for the time a power up lasts
    private long lastPowerUpSpawn = System.currentTimeMillis();
    private long powerUpDuration = 5000;
    // Cooldown for loosing health
    private long lastLostHealth = 0;
    private long lostHealthCooldown = 1000;
    // Check if the game is over
    private boolean gameOver = false;
    // Store here for logging to screen
    double ax = 0;
    double ay = 0;
    double az = 0;
    // Double score bool
    private boolean doubleScore = false;

    public BouncingBallView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.v("BouncingBallView", "Constructor BouncingBallView");
        // Create the box
        box = new Box(Color.BLACK);
        balls.add(new Ball(Color.GREEN));
        Log.w("BouncingBallLog", "Just added a bouncing ball");
    }

    // I kinda forgot about the on touch part, so it just changes the color of the ball, I went too gravity mode
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Random random = new Random();
            for (Ball ball : balls) {
                ball.setColor(Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
            }
            invalidate();
            return true;
        }
        return super.onTouchEvent(event);
    }

    // Called back to draw the view. Also called after invalidate().
    @Override
    protected void onDraw(Canvas canvas) {
        Log.v("BouncingBallView", "onDraw");
        // If the game is over, draw text to let the player know, and stop updates
        box.draw(canvas);
        if (gameOver) {
            Paint goText = new Paint();
            goText.setColor(Color.YELLOW);
            goText.setTextSize(100);
            goText.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Game Over", (float) getWidth()/2, (float) getHeight()/2, goText);
            canvas.drawText("Score: " + score, (float) getWidth()/2, (float) (getHeight()/2)+100, goText);
            return;
        }

        // Check if health is 0 or less
        if (health <= 0) {
            gameOver = true;
            invalidate();
            return;
        }

        // Set amount of downerUps so the game isn't too hard
        if (downerUps.size() < 3) {
            spawnDownerUp();
        }

        // Draw all of the balls and update their movement
        for (Ball b : balls) {
            b.draw(canvas);
            b.moveWithCollisionDetection(box);
        }

        // Randomly spawn powerups and rectangles
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPowerUpSpawn > 6000 && powerUps.size() < 100 && rectangles.size() < 100) {
            spawnPowerUp();
            spawnRectangle();
            lastPowerUpSpawn = currentTime;
        }

        // Draw the power ups, checking for collision as well (much like with the rectangles way back when)
        // Using an Iterator since I was having issues with the ArrayList. For some reason, this would crash when collecting a PowerUp
        // but it was very inconsistent, this fixed that so it was likely something to do with the ArrayList's .remove()
        for (Iterator<PowerUp> iterator = powerUps.iterator(); iterator.hasNext(); ) {
            PowerUp p = iterator.next();
            p.draw(canvas);
            for (Ball b : balls) {
                if (p.checkCollision(b)) {
                    applyPowerUp(p, b);
                    iterator.remove();
                    break;
                }
            }
        }
        for (Iterator<Rectangle> iterator = rectangles.iterator(); iterator.hasNext(); ) {
            Rectangle r = iterator.next();
            r.draw(canvas);
            for (Ball b : balls) {
                if (r.checkCollision(b)) {
                    if (doubleScore) {
                        score += 2;
                    } else {
                        score += 1;

                    }
                    // Bounce ball depending on direction. It's a bit subtle, but adds a nice pop when a ball bounces off a rectangle
                    if (r.isCollisionFromTop(b) || r.isCollisionFromBottom(b)) {
                        b.speedY = -b.speedY;
                    } else if (r.isCollisionFromLeft(b) || r.isCollisionFromRight(b)) {
                        b.speedX = -b.speedX;
                    }
                    iterator.remove();
                    break;
                }
            }
        }

        // Since they don't get removed, downerUps can just be n a standard loop
        for (DownerUp d : downerUps) {
            d.draw(canvas);
            for (Ball b : balls) {
                if (d.checkCollision(b)) {
                    long currentDownerTime = System.currentTimeMillis();
                    if (currentDownerTime - lastLostHealth > lostHealthCooldown) {
                        health--;
                        lastLostHealth = currentDownerTime;
                    }
                }
            }
        }

        // Display all the effects that are currently active as well as the score
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40);
        int y = 150;
        for (String e : effects) {
            canvas.drawText(e, 20, y, textPaint);
            y += 40;
        }
        canvas.drawText("Score: " + score, 20, 50, textPaint);
        canvas.drawText("Health: " + health, 20, 100, textPaint);
        this.invalidate();
    }

    // Called back when the view is first created or its size changes.
    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        // Set the movement bounds for the ball
        box.set(0, 0, w, h);
        Log.w("BouncingBallLog", "onSizeChanged w=" + w + " h=" + h);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.v("onSensorChanged", "event=" + event.toString());
        // Lots of sensor types...get which one, unpack accordingly
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // x, y and z of the Accelerometer
            ax = -event.values[0];
            ay = event.values[1];
            az = event.values[2];
            for (Ball b : balls) {
                b.setAcc(ax, ay, az);
            }
            Log.v("onSensorChanged", "ax=" + ax + " ay=" + ay + " az=" + az);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.v("onAccuracyChanged", "event=" + sensor.toString());
    }

    // Spawn the power ups (randomly), if you're seeing the full version of this comment,
    // I was lazy and kept this in bb view instead of a separate class
    public void spawnPowerUp() {
        Random rand = new Random();
        int x = rand.nextInt(box.xMax);
        int y = rand.nextInt(box.yMax);
        // Get the values from the enum
        PowerUpType[] types = PowerUpType.values();
        PowerUpType randomType = types[rand.nextInt(types.length)];
        powerUps.add(new PowerUp(x, y, randomType));

    }

    public void spawnDownerUp() {
        Random rand = new Random();
        int x = rand.nextInt(box.xMax);
        int y = rand.nextInt(box.yMax);
        downerUps.add(new DownerUp(x, y));
    }

    // Spawn rectangles with some extra muk so they spawn on the edges only
    public void spawnRectangle() {
        Random rand = new Random();
        float width = 200;
        float height = 50;
        float x = 0;
        float y = 0;

        // I wanted to spawn rectangles ONLY on the edges of the bbview, so this randomly selects a side
        int side = rand.nextInt(4);
        switch (side) {
            // Top
            case 0:
                x = rand.nextInt(box.xMax - (int) width);
                y = 0;
                height = 50;
                width = 100;
                break;
            // Bottom
            case 1:
                x = rand.nextInt(box.xMax - (int) width);
                y = box.yMax - height;
                height = 50;
                width = 100;
                break;
            // Left
            case 2:
                x = 0;
                y = rand.nextInt(box.yMax - (int) height);
                width = 50;
                height = 100;
                break;
            // Right
            case 3:
                x = box.xMax - width;
                y = rand.nextInt(box.yMax - (int) height);
                width = 50;
                height = 100;
                break;
        }
        rectangles.add(new Rectangle(x, y, width, height));
    }

    // Apply the actual effects depending on the enum value
    public void applyPowerUp(PowerUp p, Ball b) {
        switch (p.getType()) {
            case SPEED_UP:
                if (!effects.contains("Speed Up!")) {
                    b.speedX *= 1.5;
                    b.speedY *= 1.5;
                    effects.add("Speed Up!");
                    Log.v("Effects", "Speed Up!");

                }
                unapplyPowerUp("Speed Up!", p.getType());
                break;
            case SLOW_DOWN:
                if (!effects.contains("Slow Down!")) {
                    b.speedX *= 0.5;
                    b.speedY *= 0.5;
                    effects.add("Slow Down!");
                    Log.v("Effects", "Slow Down!");

                }
                unapplyPowerUp("Slow Down!", p.getType());
                break;
            case DOUBLE_POINTS:
                if (!effects.contains("Double Points!")) {
                    doubleScore = true;
                    effects.add("Double Points!");
                    Log.v("Effects", "Double Points!");
                }
                unapplyPowerUp("Double Points!", p.getType());
                break;
            case DOUBLE_BALL:
                if (!effects.contains("Double Ball!")) {
                    balls.add(new Ball(Color.BLUE, (float) b.x, (float) b.y, (float) b.speedX, (float) b.speedY));
                    effects.add("Double Ball!");
                    Log.v("Effects", "Double Ball!");

                }
                unapplyPowerUp("Double Ball!", p.getType());
                break;

        }
    }

    // Using the handler method (depreciated, but works for this), the effects are undone after a specific time
    public void unapplyPowerUp(String effect, PowerUpType type) {
        new Handler().postDelayed(() -> {
            switch (type) {
                case SPEED_UP:
                    if (effects.contains(effect)) {
                        for (Ball b : balls) {
                            b.speedX /= 1.5;
                        }
                        effects.remove(effect);
                        Log.v("Effects", "Reverted Speed Up!");
                    }
                    break;
                case SLOW_DOWN:
                    if (effects.contains(effect)) {
                        for (Ball b : balls) {
                            b.speedX /= 0.5;
                        }
                        effects.remove(effect);
                        Log.v("Effects", "Reverted Slow Down!");
                    }
                    break;
                case DOUBLE_POINTS:
                    if (effects.contains(effect)) {
                        doubleScore = false;
                        effects.remove(effect);
                        Log.v("Effects", "Reverted Double Points!");
                    }
                    break;
                case DOUBLE_BALL:
                    if (effects.contains(effect)) {
                        if (balls.size() > 1) {
                            balls.remove(balls.size() - 1);
                        }
                        effects.remove(effect);
                        Log.v("Effects", "Reverted Double Ball!");
                    }
                    break;
            }
        }, powerUpDuration);
    }
}
