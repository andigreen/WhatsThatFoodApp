package com.wtf.whatsthatfoodapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.lang.ref.WeakReference;

public class PairsMemoryGameActivity extends Activity {
    private PairsMemoryGameView gameView;
    private Handler handler;
    private AlertDialog alertDialog;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        handler = new MyHandler(this);

        play();
    }

    private void play(){
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.game_top_rl);

        gameView = new PairsMemoryGameView(this,handler);
        gameView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        rl.addView(gameView);
    }
    private void displayWinDialog(){
        DialogInterface.OnClickListener positiveBtn = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this).setTitle("Well Done !!!")
                .setPositiveButton("Menu",positiveBtn).setCancelable(false).setMessage("Credits:\nFood icons made by Madebyoliver and Pixel Buddha from www.flaticon.com");
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    @Override
    public void onBackPressed(){
        // Override to do nothing
    }

    private static class MyHandler extends Handler {
        private final WeakReference<PairsMemoryGameActivity> weakReference;

        public MyHandler (PairsMemoryGameActivity gameActivity){
            weakReference = new WeakReference<>(gameActivity);
        }
        @Override
        public void handleMessage(Message msg){
            PairsMemoryGameActivity gameActivity = weakReference.get();
            String function = msg.getData().getString("function");
            if (function.equals("stop")){
                gameActivity.displayWinDialog();
                }
        }
    }
}
