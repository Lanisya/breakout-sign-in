package com.gt05.lanisya.Breakoutgame;

import android.graphics.RectF;

public class Paddle {

    private RectF rect;

    private float length;
    private float height;

    private float x;
    private float y;

    private int screenX;
    private int screenY;

    private float paddleSpeed;

    public final int STOPPED = 0;
    public final int LEFT = 1;
    public final int RIGHT = 2;

    private int paddleMoving = STOPPED;

    public Paddle(int screenX, int screenY){

        length = 130;
        height = 20;

        this.screenX = screenX;
        this.screenY = screenY;

        x = (screenX / 2) - (length / 2);
        y = screenY - 20;

        rect = new RectF(x, y, x + length, y + height);

        paddleSpeed = 350;
    }

    public RectF getRect(){
        return rect;
    }

    public void setMovementState(int state) {
        paddleMoving = state;
    }

    public void update(long fps) {
        if (paddleMoving == LEFT && x > 0) {
            x = x - paddleSpeed / fps;
        }

        if (paddleMoving == RIGHT && x < screenX - length) {
            x = x + paddleSpeed / fps;
        }

        rect.left = x;
        rect.right = x + length;
    }

    public void reset(int x, int y) {
        this.x = (screenX / 2) - (length / 2);
        this.y = screenY - 20;

        rect.left = x / 2 - length / 2;
        rect.right = x / 2 + length / 2;
    }

}

