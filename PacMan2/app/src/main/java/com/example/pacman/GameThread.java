package com.example.pacman;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class GameThread extends Thread {
    private SurfaceHolder surfaceHolder;
    private GameView gameView;
    private boolean running;

    public GameThread(SurfaceHolder holder, GameView gameView) {
        this.surfaceHolder = holder;
        this.gameView = gameView;
    }

    public void setRunning(boolean isRunning) {
        this.running = isRunning;
    }

    @Override
    public void run() {
        long startTime;
        long timeMillis;
        long waitTime;
        long targetTime = 1000 / 30;

        while (running) {
            startTime = System.nanoTime();

            Canvas canvas = null;
            try {
                canvas = surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {
                    if (canvas != null) {
                        gameView.update();
                        gameView.draw(canvas);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            timeMillis = (System.nanoTime() - startTime) / 1000000;
            waitTime = targetTime - timeMillis;

            try {
                if (waitTime > 0) {
                    sleep(waitTime);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}