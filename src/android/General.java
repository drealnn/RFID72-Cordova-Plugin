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


public class General extends CordovaPlugin {


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

        if(action.equalsIgnoreCase("register_keyDown")){
            this.keydown_callback = callbackContext;
            return true;
        }
        else if(action.equalsIgnoreCase("register_keyUp")){
            this.keyup_callback = callbackContext;
            return true;
        }
        else if(action.equalsIgnoreCase("playSound"))
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
        this.currentView = webView.getView();
        this.currentView.setOnKeyListener(
                new View.OnKeyListener(){
                    @Override
                    public boolean onKey(View view, int keyCode, KeyEvent event){
                        //boolean val = super.onKey(view, keyCode, event);
                        return doKey(view, keyCode, event);
                    }
                }
        );

    }



// ####### Key event functions

    public boolean doKey(View v, int keyCode, KeyEvent event) {

        Log.i(TAG, "triggering key event");
        if (event.getAction() == KeyEvent.ACTION_UP) {
            return KeyUp(keyCode, event);
        }
        else if (event.getAction() == KeyEvent.ACTION_DOWN) {
            return KeyDown(keyCode, event);
        }
        return false;
    }

    private boolean KeyDown(int keyCode, KeyEvent event){
        //if(keydown_callback == null){
        //  return true;
        //}
        //PluginResult result = new PluginResult(PluginResult.Status.OK, Integer.toString(keyCode));
        //result.setKeepCallback(true);
        //keydown_callback.sendPluginResult(result);

        Log.e(TAG, "Key down in native");
        if (this.keydown_callback == null)
            return false;

        try {

            String str = String.format("{\'keyCode\': \'%s\', \'repeatCount\' : \'%s\' }", keyCode + "", event.getRepeatCount() + "");
            PluginResult result = new PluginResult(PluginResult.Status.OK, new JSONObject(str));
            result.setKeepCallback(true);
            this.keydown_callback.sendPluginResult(result);
            
        } catch(Exception e)
        {
            e.printStackTrace();
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Error in handling key event");
            result.setKeepCallback(true);
            this.keydown_callback.sendPluginResult(result);
            
        }

        return false;
    }

    private boolean KeyUp(int keyCode, KeyEvent event){
        //if(keyup_callback == null){
        //  return true;
        //}
        //PluginResult result = new PluginResult(PluginResult.Status.OK, Integer.toString(keyCode));
        //result.setKeepCallback(true);
        //keyup_callback.sendPluginResult(result);
        if (this.keyup_callback == null)
            return false;

        try {

            String str = String.format("{\'keyCode\': \'%s\', \'repeatCount\' : \'%s\' }", keyCode + "", event.getRepeatCount() + "");
            PluginResult result = new PluginResult(PluginResult.Status.OK, new JSONObject(str));
            result.setKeepCallback(true);
            this.keyup_callback.sendPluginResult(result);
            
        } catch(Exception e)
        {
            e.printStackTrace();
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Error in handling key event");
            result.setKeepCallback(true);
            this.keyup_callback.sendPluginResult(result);
            
        }

        return false;

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

