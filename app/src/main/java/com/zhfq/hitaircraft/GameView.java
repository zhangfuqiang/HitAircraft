package com.zhfq.hitaircraft;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2016/4/19.
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback2, Runnable, View.OnTouchListener {
    private SurfaceHolder holder;
    private Bitmap myPlane;
    private Bitmap explosion;
    private Bitmap bullet;
    private Bitmap enemy;
    private Bitmap backgroud;
    private Bitmap cacheBitmap;
    private int displayX;
    private int displayY;
    private ArrayList<GameImage> gameImageList = new ArrayList<>();
    private ArrayList<Bullet> bullets = new ArrayList<>();
    private long score = 0;
    private SoundPool soundPool;

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        this.setOnTouchListener(this);
    }

    private void init() {
        myPlane = BitmapFactory.decodeResource(getResources(), R.mipmap.my_plane);
        explosion = BitmapFactory.decodeResource(getResources(), R.mipmap.explosion);
        bullet = BitmapFactory.decodeResource(getResources(), R.mipmap.bullet);
        enemy = BitmapFactory.decodeResource(getResources(), R.mipmap.enemy);
        backgroud = BitmapFactory.decodeResource(getResources(), R.mipmap.game_background);
        cacheBitmap = Bitmap.createBitmap(displayX, displayY, Bitmap.Config.ARGB_8888);
        gameImageList.add(new BackgroundImage(backgroud));
        gameImageList.add(new MyPlane(myPlane));
        gameImageList.add(new Enemy(enemy, explosion));

        soundPool = new SoundPool(3, AudioManager.STREAM_SYSTEM, 5);
        soundPool.load(getContext(), R.raw.shot, 1);
        soundPool.load(getContext(), R.raw.bomb, 1);
    }

    MyPlane selectedPlane;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                for (GameImage gameImage : gameImageList) {
                    if (gameImage instanceof MyPlane) {
                        MyPlane myPlane = (MyPlane) gameImage;
                        if (myPlane.getX() < event.getX() && myPlane.getY() < event.getY()
                                && myPlane.getX() + myPlane.getWidth() > event.getX()
                                && myPlane.getY() + myPlane.getHeight() > event.getY()) {
                            selectedPlane = myPlane;
                        } else {
                            selectedPlane = null;
                        }
                        break;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (selectedPlane != null) {
                    selectedPlane.setX((int) event.getX() - selectedPlane.getWidth() / 2);
                    selectedPlane.setY((int) event.getY() - selectedPlane.getHeight() / 2);
                }
                break;
            case MotionEvent.ACTION_UP:
                selectedPlane = null;
                break;
        }
        return true;
    }

    private class BackgroundImage implements GameImage {
        private Bitmap background;
        private Bitmap mBackgroundBitmap;
        private int height = 0;

        private BackgroundImage(Bitmap background) {
            this.background = background;
            mBackgroundBitmap = Bitmap.createBitmap(displayX, displayY, Bitmap.Config.ARGB_8888);
        }

        @Override
        public Bitmap getBitmap() {
            Canvas canvas = new Canvas(mBackgroundBitmap);
            Paint p = new Paint();

            canvas.drawBitmap(background,
                    new Rect(0, 0, background.getWidth(), background.getHeight()),
                    new Rect(0, height, displayX, displayY + height), p);
            canvas.drawBitmap(background,
                    new Rect(0, 0, background.getWidth(), background.getHeight()),
                    new Rect(0, -displayY + height, displayX, height), p);
            height++;
            if (height == displayY) {
                height = 0;
            }
            return mBackgroundBitmap;
        }

        @Override
        public int getX() {
            return 0;
        }

        @Override
        public int getY() {
            return 0;
        }
    }

    private class MyPlane implements GameImage {
        private List<Bitmap> planeList = new ArrayList<>();
        private int x;
        private int y;
        private int width;
        private int height;

        public MyPlane(Bitmap myPlane) {
            width = myPlane.getWidth() / 4;
            height = myPlane.getHeight();
            planeList.add(Bitmap.createBitmap(myPlane, 0, 0, width, height));
            planeList.add(Bitmap.createBitmap(myPlane, width, 0, width, height));
            planeList.add(Bitmap.createBitmap(myPlane, width * 2, 0, width, height));
            planeList.add(Bitmap.createBitmap(myPlane, width * 3, 0, width, height));

            x = (displayX - myPlane.getWidth() / 4) / 2;
            y = displayY - myPlane.getHeight() - 30;
        }

        private int index = 0;
        private int num = 0;
        @Override
        public Bitmap getBitmap() {
            Bitmap bitmap = planeList.get(index);
            if (num == 8) {
                index++;
                if (index == planeList.size()) {
                    index = 0;
                }
                num = 0;
            }
            num++;
            return bitmap;
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }

    private class Enemy implements GameImage {
        private int x;
        private int y;
        private int width;
        private int height;
        private List<Bitmap> enemyList = new ArrayList<>();
        private List<Bitmap> explosionList = new ArrayList<>();

        public Enemy(Bitmap enemy, Bitmap explosion) {
            width = enemy.getWidth() / 4;
            height = enemy.getHeight();

            enemyList.add(Bitmap.createBitmap(enemy, 0, 0, width, height));
            enemyList.add(Bitmap.createBitmap(enemy, width, 0, width, height));
            enemyList.add(Bitmap.createBitmap(enemy, width * 2, 0, width, height));
            enemyList.add(Bitmap.createBitmap(enemy, width * 3, 0, width, height));

            int explosionWidth = explosion.getWidth() / 4;
            int explosionHeight = explosion.getHeight() / 2;
            explosionList.add(Bitmap.createBitmap(explosion, 0, 0, explosionWidth, explosionHeight));
            explosionList.add(Bitmap.createBitmap(explosion, explosionWidth, 0, explosionWidth, explosionHeight));
            explosionList.add(Bitmap.createBitmap(explosion, explosionWidth * 2, 0, explosionWidth, explosionHeight));
            explosionList.add(Bitmap.createBitmap(explosion, explosionWidth * 3, 0, explosionWidth, explosionHeight));

            explosionList.add(Bitmap.createBitmap(explosion, 0, 0, explosionWidth, explosionHeight));
            explosionList.add(Bitmap.createBitmap(explosion, explosionWidth, explosionHeight, explosionWidth, explosionHeight));
            explosionList.add(Bitmap.createBitmap(explosion, explosionWidth * 2, explosionHeight, explosionWidth, explosionHeight));
            explosionList.add(Bitmap.createBitmap(explosion, explosionWidth * 3, explosionHeight, explosionWidth, explosionHeight));

            Random random = new Random();
            y = -enemy.getHeight();
            x = random.nextInt(displayX - width);
        }

        private int index = 0;
        private int num = 0;
        @Override
        public Bitmap getBitmap() {
            Bitmap bitmap = enemyList.get(index);
            if (num == 8) {
                index++;
                if (index == 8 && dead) {
                    gameImageList.remove(this);
                }
                if (index == enemyList.size()) {
                    index = 0;
                }
                num = 0;
            }
            y += 2;
            num++;
            if (y > displayY) {
                gameImageList.remove(this);
            }
            return bitmap;
        }

        private boolean dead = false;
        public void attacked(ArrayList<Bullet> bullets) {
            if (!dead) {
                for (Bullet bullet : (List<Bullet>) bullets.clone()) {
                    if (bullet.getX() > x && bullet.getY() > y
                            && bullet.getX() < x + width && bullet.getY() < y + height) {
                        bullets.remove(bullet);
                        dead = true;
                        enemyList = explosionList;
                        score += 10;
                        break;
                    }
                }
            }
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }
    }

    private class Bullet implements GameImage {
        private Bitmap bullet;
        private MyPlane myPlane;
        private int x;
        private int y;

        public Bullet(MyPlane myPlane, Bitmap bullet) {
            this.myPlane = myPlane;
            this.bullet = bullet;

            x = (myPlane.getX() + myPlane.getWidth() / 2) - 20;
            y = myPlane.getY() - bullet.getHeight();
        }

        @Override
        public Bitmap getBitmap() {
            y -= 30;
            if (y < -10) {
                bullets.remove(this);
            }
            return bullet;
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }
    }

    private boolean state;
    @Override
    public void run() {
        Paint paint = new Paint();
        Paint scroePaint = new Paint();
        int enemyCount = 0;
        int bulletCount = 0;
        try {
            while (state) {
                ArrayList<GameImage> cloneList = (ArrayList<GameImage>) gameImageList.clone();
                Canvas c = new Canvas(cacheBitmap);
                bulletCount++;
                if (bulletCount == 10) {
                    if (selectedPlane != null) {
                        bullets.add(new Bullet(selectedPlane, bullet));
                    }
                    bulletCount = 0;
                }
                for (GameImage gameImage : cloneList) {
                    if (gameImage instanceof Enemy) {
                        ((Enemy) gameImage).attacked(bullets);
                    }
                    c.drawBitmap(gameImage.getBitmap(), gameImage.getX(), gameImage.getY(), paint);
                }

                for (Bullet bullet : (List<Bullet>)bullets.clone()) {
                    c.drawBitmap(bullet.getBitmap(), bullet.getX(), bullet.getY(), paint);
                }

                // 分数
                scroePaint.setColor(Color.YELLOW);
                scroePaint.setTextSize(50);
                scroePaint.setAntiAlias(true);
                c.drawText("scroe: " + score , 0, 0, scroePaint);
                enemyCount++;
                if (enemyCount == 70) {
                    enemyCount = 0;
                    gameImageList.add(new Enemy(enemy, explosion));
                }
                Canvas canvas = holder.lockCanvas();
                canvas.drawBitmap(cacheBitmap, 0, 0, paint);
                holder.unlockCanvasAndPost(canvas);
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void surfaceRedrawNeeded(SurfaceHolder holder) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        displayX = width;
        displayY = height;
        init();
        this.holder = holder;
        state = true;
        new Thread(this).start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        state = false;
    }

    private interface GameImage {
        Bitmap getBitmap();

        int getX();

        int getY();
    }
}
