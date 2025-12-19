package com.example.pacman;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class Pacman {
    private int x, y;
    private int direction = 0;
    private int nextDirection = 0;
    private float pixelX, pixelY;
    private float mouthAngle = 30;
    private int startX, startY;
    private float mouthAnimation = 0;
    private boolean isAlive = true;
    private int lives = 3;
    private float speed = 4.0f;
    private int blockSize;

    public Pacman(int startX, int startY, int blockSize) {
        this.startX = startX;
        this.startY = startY;
        this.blockSize = blockSize;
        reset();
    }

    public void reset() {
        x = startX;
        y = startY;
        direction = 0;
        nextDirection = 0;
        isAlive = true;
        pixelX = x * blockSize;
        pixelY = y * blockSize;
    }

    public void update(GameView gameView) {
        if (!isAlive) return;

        mouthAnimation += 0.2f;
        mouthAngle = 30 + (int)(15 * Math.abs(Math.sin(mouthAnimation)));

        if (blockSize == 0) return;

        if (canMove(nextDirection, gameView)) {
            direction = nextDirection;
        }

        if (canMove(direction, gameView)) {
            switch (direction) {
                case 0: pixelX += speed; break;
                case 1: pixelY += speed; break;
                case 2: pixelX -= speed; break;
                case 3: pixelY -= speed; break;
            }

            int widthInBlocks = gameView.getWidthInBlocks();

            if (pixelX < 0) {
                pixelX = widthInBlocks * blockSize;
                x = widthInBlocks;
            } else if (pixelX >= widthInBlocks * blockSize) {
                pixelX = 0;
                x = -1;
            }

            if (blockSize > 0) {
                x = (int)(pixelX + blockSize/2) / blockSize;
                y = (int)(pixelY + blockSize/2) / blockSize;

                if (x < 0) x = 0;
                if (x >= widthInBlocks) x = widthInBlocks - 1;
                if (y < 0) y = 0;
                if (y >= gameView.getHeightInBlocks()) y = gameView.getHeightInBlocks() - 1;
            }
        }
    }

    private boolean canMove(int dir, GameView gameView) {
        int nextX = x;
        int nextY = y;

        switch (dir) {
            case 0: nextX++; break;
            case 1: nextY++; break;
            case 2: nextX--; break;
            case 3: nextY--; break;
        }

        return !gameView.isWall(nextX, nextY);
    }

    public void draw(Canvas canvas, int offsetX, int offsetY) {
        if (!isAlive) return;

        Paint paint = new Paint();
        paint.setColor(Color.YELLOW);
        paint.setAntiAlias(true);

        int size = blockSize - 4;
        float centerX = offsetX + pixelX + blockSize/2;
        float centerY = offsetY + pixelY + blockSize/2;
        float radius = size/2;

        float startAngle = 0;
        switch (direction) {
            case 0: startAngle = mouthAngle/2; break;
            case 1: startAngle = 90 + mouthAngle/2; break;
            case 2: startAngle = 180 + mouthAngle/2; break;
            case 3: startAngle = 270 + mouthAngle/2; break;
        }

        float sweepAngle = 360 - mouthAngle;

        RectF rect = new RectF(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius
        );
        canvas.drawArc(rect, startAngle, sweepAngle, true, paint);
    }

    public void setNextDirection(int dir) {
        this.nextDirection = dir;
    }

    public void die() {
        isAlive = false;
        lives--;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public float getPixelX() { return pixelX; }
    public float getPixelY() { return pixelY; }
    public int getDirection() { return direction; }
    public boolean isAlive() { return isAlive; }
    public int getLives() { return lives; }
}