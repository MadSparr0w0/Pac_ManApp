package com.example.pacman;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import java.util.Random;

public class Ghost {
    private int x, y;
    private int startX, startY;
    private int direction = 0;
    private float pixelX, pixelY;
    private int color;
    private boolean isScared = false;
    private long scaredUntil = 0;
    private Random random = new Random();
    private String name;
    private float waveOffset = 0;
    private float speed = 4.5f;
    private int targetX, targetY;
    private int scatterCounter = 0;
    private int blockSize;

    public Ghost(int startX, int startY, int color, String name, int blockSize) {
        this.startX = startX;
        this.startY = startY;
        this.color = color;
        this.name = name;
        this.blockSize = blockSize;
        reset();
    }

    public void reset() {
        x = startX;
        y = startY;
        isScared = false;
        pixelX = x * blockSize;
        pixelY = y * blockSize;
        scatterCounter = 0;
    }

    public void update(GameView gameView) {
        waveOffset += 0.1f;
        if (waveOffset > 2 * Math.PI) {
            waveOffset -= 2 * Math.PI;
        }

        if (isScared && System.currentTimeMillis() > scaredUntil) {
            isScared = false;
        }

        Pacman pacman = gameView.getPacman();
        if (pacman != null) {
            setTarget(pacman, gameView);
        }

        scatterCounter++;
        if (scatterCounter > 100) {
            scatterCounter = 0;
        }

        boolean shouldChangeDir = !canMove(direction, gameView) || random.nextInt(100) < 20;
        if (shouldChangeDir || scatterCounter % 30 == 0) {
            chooseNewDirection(gameView);
        }

        float currentSpeed = isScared ? speed * 0.7f : speed;
        if (canMove(direction, gameView)) {
            switch (direction) {
                case 0: pixelX += currentSpeed; break;
                case 1: pixelY += currentSpeed; break;
                case 2: pixelX -= currentSpeed; break;
                case 3: pixelY -= currentSpeed; break;
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

    private void setTarget(Pacman pacman, GameView gameView) {
        int width = gameView.getWidthInBlocks();
        int height = gameView.getHeightInBlocks();

        if (isScared) {
            targetX = random.nextInt(width);
            targetY = random.nextInt(height);
            return;
        }

        switch (name) {
            case "Blinky":
                targetX = pacman.getX();
                targetY = pacman.getY();
                break;
            case "Pinky":
                targetX = (pacman.getX() + 4) % width;
                targetY = (pacman.getY() + 4) % height;
                break;
            case "Inky":
                if (random.nextInt(100) < 70) {
                    targetX = pacman.getX();
                    targetY = pacman.getY();
                } else {
                    targetX = random.nextInt(width);
                    targetY = random.nextInt(height);
                }
                break;
            case "Clyde":
                int dist = Math.abs(pacman.getX() - x) + Math.abs(pacman.getY() - y);
                if (dist < 8) {
                    targetX = 0;
                    targetY = height - 1;
                } else {
                    targetX = pacman.getX();
                    targetY = pacman.getY();
                }
                break;
        }

        if (targetX < 0) targetX = 0;
        if (targetX >= width) targetX = width - 1;
        if (targetY < 0) targetY = 0;
        if (targetY >= height) targetY = height - 1;
    }

    private void chooseNewDirection(GameView gameView) {
        int[] directions = {0, 1, 2, 3};
        java.util.ArrayList<Integer> possible = new java.util.ArrayList<>();

        for (int dir : directions) {
            if (canMove(dir, gameView)) {
                possible.add(dir);
            }
        }

        if (possible.isEmpty()) {
            return;
        }

        if (isScared) {
            direction = possible.get(random.nextInt(possible.size()));
            return;
        }

        int bestDir = possible.get(0);
        int minDist = Integer.MAX_VALUE;

        for (int dir : possible) {
            int nextX = x;
            int nextY = y;
            switch (dir) {
                case 0: nextX++; break;
                case 1: nextY++; break;
                case 2: nextX--; break;
                case 3: nextY--; break;
            }

            int dist = Math.abs(nextX - targetX) + Math.abs(nextY - targetY);
            if (dist < minDist) {
                minDist = dist;
                bestDir = dir;
            }
        }

        direction = bestDir;
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
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        if (isScared) {
            paint.setColor(Color.BLUE);
        } else {
            paint.setColor(color);
        }

        int size = blockSize - 4;
        float centerX = offsetX + pixelX + blockSize/2;
        float centerY = offsetY + pixelY + blockSize/2;
        float radius = size/2;

        canvas.drawCircle(centerX, centerY, radius, paint);

        Path body = new Path();
        body.moveTo(offsetX + pixelX, centerY);

        int waveCount = 4;
        float waveWidth = size / (float)waveCount;
        float waveHeight = size / 10f;

        for (int i = 0; i <= waveCount; i++) {
            float xPos = offsetX + pixelX + i * waveWidth;
            float yOffset = (float)Math.sin(waveOffset + i * Math.PI) * waveHeight;
            float yPos = offsetY + pixelY + size - waveHeight + yOffset;

            if (i == 0) body.moveTo(xPos, yPos);
            else body.lineTo(xPos, yPos);
        }

        body.lineTo(offsetX + pixelX + size, centerY);
        body.close();

        canvas.drawPath(body, paint);

        paint.setColor(Color.WHITE);
        int eyeSize = size / 4;
        int eyeY = (int)(centerY - size/8);

        canvas.drawCircle(centerX - size/4, eyeY, eyeSize, paint);
        canvas.drawCircle(centerX + size/4, eyeY, eyeSize, paint);

        paint.setColor(Color.BLUE);
        int pupilSize = size / 8;

        float pupilX1 = centerX - size/4;
        float pupilX2 = centerX + size/4;
        float pupilY = eyeY;

        switch (direction) {
            case 0: pupilX1 += size/12; pupilX2 += size/12; break;
            case 1: pupilY += size/12; break;
            case 2: pupilX1 -= size/12; pupilX2 -= size/12; break;
            case 3: pupilY -= size/12; break;
        }

        canvas.drawCircle(pupilX1, pupilY, pupilSize, paint);
        canvas.drawCircle(pupilX2, pupilY, pupilSize, paint);
    }

    public void scare() {
        isScared = true;
        scaredUntil = System.currentTimeMillis() + 7000;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public float getPixelX() { return pixelX; }
    public float getPixelY() { return pixelY; }
    public boolean isScared() { return isScared; }
    public String getName() { return name; }
}