package com.wtf.whatsthatfoodapp;


import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class PairsMemoryGameView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener{
    private Context context;
    private Handler handler;
    private SurfaceHolder sfholder;
    private int counter;
    public GameThread gameThread;
    private ArrayList<Integer> imageIndices;
    private ArrayList<Rect> cards;
    private TypedArray food_images;
    private Rect shownCard;
    private Rect secondShownCard;
    private ArrayList<Integer> correctCardsInd;
    private long showingTimer;
    private static final int TIMEOUT = 500;

    public PairsMemoryGameView(Context context, Handler handler/*, Audio audio*/){
        super(context);
        food_images = getResources().obtainTypedArray(R.array.images);
        this.context = context;
        this.handler = handler;
        //this.audio = audio;
        sfholder = getHolder();
        sfholder.addCallback(this);
        setOnTouchListener(this);
        correctCardsInd = new ArrayList<>();
    }
    private void createPairs(){
        int width = this.getRight();
        int height = this.getBottom();
        double separationX = width * 0.05;

        int cardSize = (int) ((width-5*separationX)/4);

        // Cards holds all the rects where the image is going to be drawn
        cards = new ArrayList();

        int top = (int)(height-width+separationX);
        int bottom = top + cardSize;
        for (int row=0;row<4;row++){
            int left = (int)separationX;
            int right = left + cardSize;
            for (int column=0; column<4; column++){
                cards.add(new Rect(left,top,right,bottom));
                left = (int)(right + separationX);
                right = left + cardSize;
            }
            top = (int)(bottom + separationX);
            bottom = top + cardSize;
        }

        //
        Random random = new Random();
        imageIndices = new ArrayList();
        for (int i=0; i<8; i++){
            int ind = random.nextInt(food_images.length());
            imageIndices.add(ind);
            imageIndices.add(ind);
        }

        Collections.shuffle(imageIndices);


    }
    private void drawPairs(Canvas c){
        for (int i = 0; i< cards.size(); i++){
            Bitmap bitmap;
            if (cards.get(i).equals(shownCard) || cards.get(i).equals(secondShownCard)){
                // Cards to be shown
                bitmap = BitmapFactory.decodeResource(getResources(),food_images.getResourceId(imageIndices.get(i),-1));
            } else if (correctCardsInd.contains(i) && cards.get(i) != shownCard && cards.get(i) != secondShownCard){
                // Cards correct (disappeared)
                bitmap = Bitmap.createBitmap(cards.get(i).width(),cards.get(i).height(), Bitmap.Config.ARGB_8888);;
            } else {
                // Cards hidden
                bitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher);
            }
            c.drawBitmap(bitmap,null,cards.get(i),null);
        }
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder){
        counter = 0;

        Canvas c = holder.lockCanvas();
        c.drawColor(Color.BLACK);

        createPairs();
        drawPairs(c);

        holder.unlockCanvasAndPost(c);

        gameThread = new GameThread();
        gameThread.start();
        gameThread.setRunning(true);
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
        gameThread.setRunning(false);
        while (true){
            try {
                gameThread.join();
                break;
            } catch (InterruptedException e){
                Log.d("PairsGame","gameThread Interrupted Exception");
            }
        }
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder,int format, int width, int height){}


    private void updateCanvas(Canvas c){
        if (c == null){
            return;
        }
        c.drawColor(Color.BLACK);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize((float)(getWidth()*0.1));
        paint.setFakeBoldText(true);
        c.drawText(""+counter,(float)getWidth()/2-paint.measureText(""+counter)/2,(float)(getHeight()-getHeight()*0.9),paint);
        drawPairs(c);
        checkTimeout();
    }

    private void checkTimeout(){
        if (shownCard != null && System.currentTimeMillis() - showingTimer > TIMEOUT){
            // Reset Timeout
            shownCard = null;
            secondShownCard = null;
        }
    }

    public boolean onTouch(View v, MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            double x = event.getX();
            double y = event.getY();

            for (Rect card : cards){
                // If touching a card
                if (card.contains((int) x,(int) y) && !correctCardsInd.contains(cards.indexOf(card))){
                    if (shownCard == null){
                        showingTimer = System.currentTimeMillis();
                        shownCard = card;
                    } else  if (secondShownCard == null && cards.indexOf(card) != cards.indexOf(shownCard)){
                        // If no second shownCard display and shownCard != card touched
                        secondShownCard = card;
                        // If both cards are equal (have the same picture)
                        if (imageIndices.get(cards.indexOf(secondShownCard)) == imageIndices.get(cards.indexOf(shownCard))){
                            correctCardsInd.add(cards.indexOf(secondShownCard));
                            correctCardsInd.add(cards.indexOf(shownCard));
                            counter ++;
                            if (counter == cards.size()/2){
                                // If all cards have been discovered
                                stop(true);
                            }
                        }
                        showingTimer = System.currentTimeMillis();
                    }
                }
            }
        }
        return true;
    }

    public void stop(boolean showDialog){
        gameThread.setRunning(false);

        Bundle data = new Bundle();
        if (showDialog){
            data.putString("function", "stop");
        } else {
            data.putString("function","none");
        }
        Message msg = new Message();
        msg.setData(data);
        handler.sendMessage(msg);
    }

    public class GameThread extends Thread {
        // Game thread (separated from main UI thread)
        private boolean running = false;
        private double lastTime = 0;

        @Override
        public void run(){
            Canvas c = null;
            lastTime = System.currentTimeMillis();
            while (this.running) {
                double now = System.currentTimeMillis();
                if ((now - lastTime) > 50.0 / 1000) {
                    lastTime = now;
                    try {
                        c = sfholder.lockCanvas();

                        synchronized (sfholder) {
                            updateCanvas(c);
                        }
                    } finally {
                        if (c != null) {
                            sfholder.unlockCanvasAndPost(c);
                        }
                    }
                }
            }
        }
        public  void setRunning(boolean running){
            this.running = running;
        }
    }
}