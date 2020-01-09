package com.rfid72.app;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;

import java.util.HashMap;
import java.util.Map;

public class SoundLoader {
    private static Map<Context, SoundLoader> instance = new HashMap<>();
    static SoundLoader getInstance(Context ctx){
        if (instance.get(ctx) == null){
            instance.put(ctx, new SoundLoader(ctx));
        }
        return instance.get(ctx);
    }

    private SoundPool mSound;
    private Vibrator mVibrator;
    private int mSuccess, mFail, mBeep;

    SoundLoader(Context ctx){
        mSound = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);

        int successResource = ctx.getResources().getIdentifier("barcodebeep", "raw", ctx.getPackageName());
        int failResource =  ctx.getResources().getIdentifier("serror", "raw", ctx.getPackageName());
        int beepResource = ctx.getResources().getIdentifier("barcodebeep", "raw", ctx.getPackageName());

        mSuccess = mSound.load(ctx, successResource, 1);
        mFail = mSound.load(ctx, failResource, 1);
        mBeep = mSound.load(ctx, beepResource, 1);
    }

    void playSuccess() {
        //mSound.playSuccess();
        mSound.play(mSuccess, 1, 1, 0, 0, 1);
    }

    void playFail() {
        //mSound.playFail();
        mSound.play(mFail, 1, 1, 0, 0, 1);
    }

    void playBeep() {
        mSound.play(mBeep, 1, 1, 0, 0, 1);
    }

    void vibrate(){
        this.mVibrator.vibrate(100);
    }

}
