package com.gt05.lanisya.Breakoutgame;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.io.IOException;

class BreakoutView extends SurfaceView implements Runnable {

    InterstitialAd mInterstitialAd;
    Context context;
    Thread gameThread = null;

    SurfaceHolder ourHolder;

    volatile boolean playing;

    boolean paused = true;

    Canvas canvas;
    Paint paint;

    long fps;

    private long timeThisFrame;

    int screenX;
    int screenY;

    Paddle paddle;

    Ball ball;

    Brick[] bricks = new Brick[200];
    int numBricks = 0;

    SoundPool soundPool;
    int beep1ID = -1;
    int beep2ID = -1;
    int beep3ID = -1;
    int loseLifeID = -1;
    int explodeID = -1;

    int score = 0;

    int lives = 3;

    public BreakoutView(Context context) {
        super(context);

        this.context = context;

        //Interstitial
        mInterstitialAd = new InterstitialAd(context);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Log.d("MainActivity", "onAdClosed");
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });

        ourHolder = getHolder();
        paint = new Paint();

        Display display = ((AppCompatActivity) context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        screenX = size.x;
        screenY = size.y;

        Log.d(BreakoutView.class.getSimpleName(), String.valueOf(screenX));
        Log.d(BreakoutView.class.getSimpleName(), String.valueOf(screenY));

        paddle = new Paddle(screenX, screenY);

        ball = new Ball(screenX, screenY);

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);

        try {
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            descriptor = assetManager.openFd("beep1.ogg");
            beep1ID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("beep2.ogg");
            beep2ID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("beep3.ogg");
            beep3ID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("loseLife.ogg");
            loseLifeID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("explode.ogg");
            explodeID = soundPool.load(descriptor, 0);

        } catch (IOException e) {
            Log.e("error", "failed to load sound files");
        }

        createBricksAndRestart();

    }

    public void showInterAds() {
        Log.d("MainActivity", "showInterstitial");

        ((AppCompatActivity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
            }
        });
    }

    public void createBricksAndRestart() {

        ball.reset(screenX, screenY);
        paddle.reset(screenX, screenY);

        int brickWidth = screenX / 8;
        int brickHeight = screenY / 10;

        numBricks = 0;
        for (int column = 0; column < 8; column++) {
            for (int row = 0; row < 3; row++) {
                bricks[numBricks] = new Brick(row, column, brickWidth, brickHeight);
                numBricks++;
            }
        }

        if (lives == 0) {
            score = 0;
            lives = 3;
            showInterAds();
        }
    }

    @Override
    public void run() {
        while (playing) {
            long startFrameTime = System.currentTimeMillis();

            if (!paused) {
                update();
            }

            draw();

            timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame >= 1) {
                fps = 1000 / timeThisFrame;
            }

        }

    }

    public void update() {

        paddle.update(fps);

        ball.update(fps);

        for (int i = 0; i < numBricks; i++) {
            if (bricks[i].getVisibility()) {
                if (RectF.intersects(bricks[i].getRect(), ball.getRect())) {
                    bricks[i].setInvisible();
                    ball.reverseYVelocity();
                    score = score + 10;
                    soundPool.play(explodeID, 1, 1, 0, 0, 1);
                }
            }
        }

        if (RectF.intersects(paddle.getRect(), ball.getRect())) {
            ball.setRandomXVelocity();
            ball.reverseYVelocity();
            ball.clearObstacleY(paddle.getRect().top - 2);
            soundPool.play(beep1ID, 1, 1, 0, 0, 1);
        }

        if (ball.getRect().bottom > screenY) {
            ball.reverseYVelocity();
            ball.clearObstacleY(screenY - 2);

            lives--;
            soundPool.play(loseLifeID, 1, 1, 0, 0, 1);

            if (lives == 0) {
                paused = true;
                createBricksAndRestart();
            }
        }

        if (ball.getRect().top < 0)

        {
            ball.reverseYVelocity();
            ball.clearObstacleY(12);

            soundPool.play(beep2ID, 1, 1, 0, 0, 1);
        }

        if (ball.getRect().left < 0)

        {
            ball.reverseXVelocity();
            ball.clearObstacleX(2);
            soundPool.play(beep3ID, 1, 1, 0, 0, 1);
        }

        if (ball.getRect().right > screenX - 10) {

            ball.reverseXVelocity();
            ball.clearObstacleX(screenX - 22);

            soundPool.play(beep3ID, 1, 1, 0, 0, 1);
        }

        if (score == numBricks * 10)
        {
            paused = true;
            createBricksAndRestart();
        }

    }

    public void draw() {

        if (ourHolder.getSurface().isValid()) {
            canvas = ourHolder.lockCanvas();

            canvas.drawColor(Color.argb(255, 26, 128, 182));

            paint.setColor(Color.argb(255, 255, 255, 255));

            canvas.drawRect(paddle.getRect(), paint);

            canvas.drawRect(ball.getRect(), paint);

            paint.setColor(Color.argb(255, 249, 129, 0));

            for (int i = 0; i < numBricks; i++) {
                if (bricks[i].getVisibility()) {
                    canvas.drawRect(bricks[i].getRect(), paint);
                }
            }

            paint.setColor(Color.argb(255, 255, 255, 255));

            paint.setTextSize(40);
            canvas.drawText("Score: " + score + "   Lives: " + lives, 10, 50, paint);

            if (score == numBricks * 10) {
                paint.setTextSize(90);
                canvas.drawText("YOU HAVE WON!", 10, screenY / 2, paint);
            }

            if (lives <= 0) {
                paint.setTextSize(90);
                canvas.drawText("YOU HAVE LOST!", 10, screenY / 2, paint);
            }

            ourHolder.unlockCanvasAndPost(canvas);
        }
    }

    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Log.e("Error:", "joining thread");
        }
    }

    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            // Player has touched the screen
            case MotionEvent.ACTION_DOWN:
                paused = false;

                if (motionEvent.getX() > screenX / 2) {
                    paddle.setMovementState(paddle.RIGHT);
                }
                else {
                    paddle.setMovementState(paddle.LEFT);
                }

                break;

            case MotionEvent.ACTION_UP:
                paddle.setMovementState(paddle.STOPPED);
                break;
        }

        return true;
    }

}