package com.example.pacman;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import java.util.ArrayList;
import java.util.Random;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private GameThread gameThread;
    private int[][] map;
    private int blockSize;
    private int widthInBlocks = 19;
    private int heightInBlocks = 22;
    private Pacman pacman;
    private ArrayList<Ghost> ghosts;
    private int score = 0;
    private int dotsLeft = 0;
    private GestureDetector gestureDetector;
    private Random random = new Random();
    private boolean gameRunning = true;
    private int lives = 3;
    private Paint scorePaint, lifePaint;
    private long gameOverTime = 0;
    private int offsetX, offsetY;

    public GameView(Context context) {
        super(context);
        init(context);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        getHolder().addCallback(this);
        setFocusable(true);

        createMap();

        gestureDetector = new GestureDetector(context, new GestureListener());
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
    }

    private void createMap() {
        int[][] levelMap = {
                {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                {1,2,2,2,2,2,2,2,2,1,2,2,2,2,2,2,2,2,1},
                {1,3,1,1,2,1,1,1,2,1,2,1,1,1,2,1,1,3,1},
                {1,2,1,1,2,1,1,1,2,1,2,1,1,1,2,1,1,2,1},
                {1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1},
                {1,2,1,1,2,1,2,1,1,1,1,1,2,1,2,1,1,2,1},
                {1,2,2,2,2,1,2,2,2,1,2,2,2,1,2,2,2,2,1},
                {1,1,1,1,2,1,1,1,0,1,0,1,1,1,2,1,1,1,1},
                {0,0,0,1,2,1,0,0,0,0,0,0,0,1,2,1,0,0,0},
                {1,1,1,1,2,1,0,1,1,4,1,1,0,1,2,1,1,1,1},
                {0,0,0,0,2,0,0,1,0,0,0,1,0,0,2,0,0,0,0},
                {1,1,1,1,2,1,0,1,1,1,1,1,0,1,2,1,1,1,1},
                {0,0,0,1,2,1,0,0,0,0,0,0,0,1,2,1,0,0,0},
                {1,1,1,1,2,1,0,1,1,1,1,1,0,1,2,1,1,1,1},
                {1,2,2,2,2,2,2,2,2,1,2,2,2,2,2,2,2,2,1},
                {1,2,1,1,2,1,1,1,2,1,2,1,1,1,2,1,1,2,1},
                {1,3,2,1,2,2,2,2,2,2,2,2,2,2,2,1,2,3,1},
                {1,1,2,1,2,1,2,1,1,1,1,1,2,1,2,1,2,1,1},
                {1,2,2,2,2,1,2,2,2,1,2,2,2,1,2,2,2,2,1},
                {1,2,1,1,1,1,1,1,2,1,2,1,1,1,1,1,1,2,1},
                {1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1},
                {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
        };

        map = new int[widthInBlocks][heightInBlocks];
        dotsLeft = 0;

        for (int x = 0; x < widthInBlocks; x++) {
            for (int y = 0; y < heightInBlocks; y++) {
                map[x][y] = levelMap[y][x];
                if (map[x][y] == 2 || map[x][y] == 3) {
                    dotsLeft++;
                }
            }
        }

        scorePaint = new Paint();
        scorePaint.setColor(Color.WHITE);
        scorePaint.setTextSize(40);
        scorePaint.setAntiAlias(true);

        lifePaint = new Paint();
        lifePaint.setColor(Color.YELLOW);
        lifePaint.setAntiAlias(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (gameThread == null) {
            gameThread = new GameThread(getHolder(), this);
            gameThread.setRunning(true);
            gameThread.start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        blockSize = Math.min(width / widthInBlocks, height / heightInBlocks);
        if (blockSize < 20) blockSize = 20;

        offsetX = (width - widthInBlocks * blockSize) / 2;
        offsetY = (height - heightInBlocks * blockSize) / 2;

        pacman = new Pacman(9, 16, blockSize);
        ghosts = new ArrayList<>();
        ghosts.add(new Ghost(9, 10, Color.RED, "Blinky", blockSize));
        ghosts.add(new Ghost(8, 10, Color.MAGENTA, "Pinky", blockSize));
        ghosts.add(new Ghost(10, 10, Color.CYAN, "Inky", blockSize));
        ghosts.add(new Ghost(9, 11, Color.rgb(255, 165, 0), "Clyde", blockSize));
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        pauseGame();
    }

    public void update() {
        if (!gameRunning) {
            if (gameOverTime > 0 && System.currentTimeMillis() - gameOverTime > 5000) {
                restartGame();
            }
            return;
        }

        pacman.update(this);

        if (map[pacman.getX()][pacman.getY()] == 2) {
            map[pacman.getX()][pacman.getY()] = 0;
            score += 10;
            dotsLeft--;
        } else if (map[pacman.getX()][pacman.getY()] == 3) {
            map[pacman.getX()][pacman.getY()] = 0;
            score += 50;
            dotsLeft--;
            for (Ghost ghost : ghosts) {
                ghost.scare();
            }
        }

        for (Ghost ghost : ghosts) {
            ghost.update(this);

            if (Math.abs(pacman.getX() - ghost.getX()) < 1 &&
                    Math.abs(pacman.getY() - ghost.getY()) < 1) {
                if (ghost.isScared()) {
                    score += 200;
                    ghost.reset();
                } else {
                    lives--;
                    if (lives <= 0) {
                        gameOver();
                    } else {
                        resetGameObjects();
                    }
                }
            }
        }

        if (dotsLeft <= 0) {
            nextLevel();
        }
    }

    private void resetGameObjects() {
        pacman.reset();
        for (Ghost ghost : ghosts) {
            ghost.reset();
        }
    }

    private void restartGame() {
        score = 0;
        lives = 3;
        gameRunning = true;
        gameOverTime = 0;
        createMap();
        resetGameObjects();
    }

    private void nextLevel() {
        lives++;
        createMap();
        resetGameObjects();
    }

    private void gameOver() {
        gameRunning = false;
        gameOverTime = System.currentTimeMillis();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas == null) return;

        canvas.drawColor(Color.BLACK);

        drawMap(canvas);

        for (Ghost ghost : ghosts) {
            ghost.draw(canvas, offsetX, offsetY);
        }

        pacman.draw(canvas, offsetX, offsetY);

        drawUI(canvas);
    }

    private void drawMap(Canvas canvas) {
        if (blockSize == 0) return;

        Paint wallPaint = new Paint();
        wallPaint.setColor(Color.BLUE);
        wallPaint.setStyle(Paint.Style.FILL);

        Paint dotPaint = new Paint();
        dotPaint.setColor(Color.WHITE);
        dotPaint.setAntiAlias(true);

        Paint superDotPaint = new Paint();
        superDotPaint.setColor(Color.YELLOW);
        superDotPaint.setAntiAlias(true);

        Paint gatePaint = new Paint();
        gatePaint.setColor(Color.GRAY);

        for (int x = 0; x < widthInBlocks; x++) {
            for (int y = 0; y < heightInBlocks; y++) {
                int left = offsetX + x * blockSize;
                int top = offsetY + y * blockSize;

                switch (map[x][y]) {
                    case 1:
                        canvas.drawRect(left, top, left + blockSize, top + blockSize, wallPaint);
                        break;
                    case 2:
                        canvas.drawCircle(
                                left + blockSize / 2,
                                top + blockSize / 2,
                                blockSize / 8,
                                dotPaint
                        );
                        break;
                    case 3:
                        canvas.drawCircle(
                                left + blockSize / 2,
                                top + blockSize / 2,
                                blockSize / 4,
                                superDotPaint
                        );
                        break;
                    case 4:
                        canvas.drawRect(left, top, left + blockSize, top + blockSize, gatePaint);
                        break;
                }
            }
        }
    }

    private void drawUI(Canvas canvas) {
        canvas.drawText("Счет: " + score, 20, 50, scorePaint);
        canvas.drawText("Жизни: " + lives, getWidth() - 200, 50, scorePaint);

        if (!gameRunning && gameOverTime > 0) {
            Paint gameOverPaint = new Paint();
            gameOverPaint.setColor(Color.RED);
            gameOverPaint.setTextSize(60);
            gameOverPaint.setTextAlign(Paint.Align.CENTER);
            gameOverPaint.setAntiAlias(true);
            canvas.drawText("ИГРА ОКОНЧЕНА", getWidth() / 2, getHeight() / 2, gameOverPaint);
            canvas.drawText("Новая игра через: " + (5 - (System.currentTimeMillis() - gameOverTime) / 1000) + " сек",
                    getWidth() / 2, getHeight() / 2 + 70, gameOverPaint);
        }
    }

    public boolean isWall(int x, int y) {
        if (x < 0 || y < 0 || x >= widthInBlocks || y >= heightInBlocks) {
            return false;
        }
        return map[x][y] == 1;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public int getWidthInBlocks() {
        return widthInBlocks;
    }

    public int getHeightInBlocks() {
        return heightInBlocks;
    }

    public Pacman getPacman() {
        return pacman;
    }

    public void pauseGame() {
        if (gameThread != null) {
            gameThread.setRunning(false);
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            gameThread = null;
        }
    }

    public void resumeGame() {
        if (gameThread == null) {
            gameThread = new GameThread(getHolder(), this);
            gameThread.setRunning(true);
            gameThread.start();
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();

            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (diffX > 0) {
                    pacman.setNextDirection(0);
                } else {
                    pacman.setNextDirection(2);
                }
            } else {
                if (diffY > 0) {
                    pacman.setNextDirection(1);
                } else {
                    pacman.setNextDirection(3);
                }
            }
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }
}