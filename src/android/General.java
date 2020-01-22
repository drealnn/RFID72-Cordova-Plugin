package com.rfid72.app;

import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;

import android.util.Log;
import android.view.KeyEvent;

import android.view.View;

import android.content.Context;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class General extends CordovaPlugin implements View.OnKeyListener {


    private CallbackContext keyup_callback = null;
    private CallbackContext keydown_callback = null;
    private SoundPool mSound;
    int mSuccess, mFail, mBeep;

    private View currentView = null;
    private String TAG = "RFID72 Native";
    private Context ctx = null;

//Context context=this.cordova.getActivity().getApplicationContext();

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);

        // Events //

        if(action.equalsIgnoreCase("playSound"))
        {
            this.playSound(args.getString(0), callbackContext);
            return true;
        }
        return false;
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        Log.i(TAG, "RFID72 general Initialized");
        ctx = cordova.getActivity().getApplicationContext();
        //final CordovaWebView myWebView = webView;
        /*cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                myWebView.getView().setOnKeyListener(
                        new View.OnKeyListener(){
                            @Override
                            public boolean onKey(View view, int keyCode, KeyEvent event){
                                //boolean val = super.onKey(view, keyCode, event);
                                Log.e(TAG, ""+keyCode);
                                return doKey(view, keyCode, event);
                            }
                        }
                );
            }
        });*/

        this.currentView = webView.getView();

    }


    private void playSound(String soundName, CallbackContext callbackContext)
    {
        switch(soundName){
            case("success"):
                SoundLoader.getInstance(ctx).playSuccess();
                break;
            case("fail"):
                SoundLoader.getInstance(ctx).playFail();
                break;
            case("beep"):
                SoundLoader.getInstance(ctx).playBeep();
                break;
            default:
                callbackContext.error("Can't find provided sound for playSound");
                return;
                

        }
        callbackContext.success("Initiating sound");
    }
}