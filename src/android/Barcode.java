package com.rfid72.app;

import android.content.Context;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;

import com.zebra.adc.decoder.Barcode2DWithSoft;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;


public class Barcode extends CordovaPlugin {

private static final String TAG = "MainActivity";

private Barcode2DWithSoft mScanner =null;
private PowerManager.WakeLock mWakeLock = null;
private SoundPool mSoundPool;
private int mBeepSuccess;
private int mBeepFail;
private Vibrator mVibrator;
private CallbackContext keyup_callback = null;
private CallbackContext keydown_callback = null;
private CallbackContext getDecode_callback = null;
private View currentView = null;
private Context ctx;

//ScanResult mScanResult;

//Context context=this.cordova.getActivity().getApplicationContext();

@Override
public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
    result.setKeepCallback(true);

    if (action.equals("echo")) {
        String message = args.getString(0);
        this.echo(message, callbackContext);
        return true;
    }
    else if (action.equals("deinitalize_scanner")){
        this.deinitalize();
        return true;
    }
    else if (action.equals("wakeup_scanner")){
        Log.i(TAG, "+- wakeup scanner");
        
        //if(mScanner != null)
        //    ATScanManager.wakeUp();
        return true;
    }
    else if (action.equals("sleep_scanner")){
        Log.i(TAG, "+- sleep scanner");
        /*if(mScanner != null) {
            ATScanManager.sleep();
        }*/
        return true;
    }
    else if (action.equals("scanner_startDecode")){
        Log.i(TAG, "++Start Decode");
        //mScanResult = null;
        this.mScanner.scan();
        Log.i(TAG, "--Start Decode");
        
        return true;
    }
    else if (action.equals("scanner_stopDecode")){
        Log.i(TAG, "++Stop Decode");
        this.mScanner.stopScan();
        Log.i(TAG, "--Stop Decode");
        
        return true;
    }
    else if (action.equals("scanner_isDecoding")){
        Log.i(TAG, "++Is Decoding");
        //callbackContext.success("" + this.mScanner.isDecoding());
        Log.i(TAG, "--Is Decoding");
        
        return true;
    }
    // TODO: replace with  android -> javascript async call instead of javascript->android sync call
    /*else if (action.equals("scanner_getDecodeCallback")){
        Log.i(TAG, "Start Decode");
        if (mScanResult != null)
        {
            // TODO: return json instead of string
            callbackContext.success("" + mScanResult.scanResultType + 
                " : " + mScanResult.scanResultBarcode);
            mScanResult = null;
        }
        else
            callbackContext.error("Could not get decode information");
        
        return true;
    }*/
    else if(action.equalsIgnoreCase("register_keyDown")){
            this.keydown_callback = callbackContext;
            return true;
    }
    else if(action.equalsIgnoreCase("register_keyUp")){
            this.keyup_callback = callbackContext;
            return true;
    }
    else if(action.equalsIgnoreCase("register_decode")){
            this.getDecode_callback = callbackContext;
            return true;
    }
    return false;
}

public Barcode2DWithSoft.ScanCallback  ScanBack = new Barcode2DWithSoft.ScanCallback(){
    @Override
    public void onScanComplete(int i, int length, byte[] bytes) {
        PluginResult result;
        if (length < 1) {
            String errorMessage = "";
            if (length == -1) {
                errorMessage = "Scan Cancel";
            } else if (length == 0) {
                errorMessage = "Scan timeout";
            } else {
                errorMessage = "Scan fail";
            }
            Log.e(TAG, errorMessage);
            result = new PluginResult(PluginResult.Status.ERROR, errorMessage);
        }else{
            String barcode = new String(bytes, 0, length, StandardCharsets.US_ASCII);
            try {
                result = new PluginResult(PluginResult.Status.OK, new JSONObject("{\'type\': \'N/A\' , \'barcode\': \'" + barcode + "\' }"));
            } catch(JSONException e){
                e.printStackTrace();
                result = new PluginResult(PluginResult.Status.ERROR, "Error in constructing json for success decode callback");
            }

            SoundLoader.getInstance(ctx).playSuccess();
            SoundLoader.getInstance(ctx).vibrate();
        }
        result.setKeepCallback(true);
        getDecode_callback.sendPluginResult(result);
    }
};

@Override
public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    this.ctx = cordova.getActivity().getApplicationContext();
    mScanner = Barcode2DWithSoft.getInstance();
    boolean result = false;
    if(mScanner != null) {
        result = mScanner.open(ctx);
        Log.i(TAG,"open="+result);
        if(result){
//                mScanner.setParameter(324, 1);
//                mScanner.setParameter(300, 0); // Snapshot Aiming
//                mScanner.setParameter(361, 0); // Image Capture Illumination

            // interleaved 2 of 5
            mScanner.setParameter(6, 1);
            mScanner.setParameter(22, 0);
            mScanner.setParameter(23, 55);
            mScanner.setParameter(402, 1);

            mScanner.setScanCallback(ScanBack);
        } else {
            Log.e(TAG, "Barcode initialization failure");
        }
    }
    this.currentView = webView.getView();
    Log.i(TAG, "Scanning device initialized");
}

public class InitScannerTask extends AsyncTask<String, Integer, Boolean> {
    @Override
    protected Boolean doInBackground(String... params) {
        boolean result = false;
        if(mScanner != null) {
            result = mScanner.open(ctx);
            Log.i(TAG,"open="+result);
        }
        return result;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if(result){
//                mScanner.setParameter(324, 1);
//                mScanner.setParameter(300, 0); // Snapshot Aiming
//                mScanner.setParameter(361, 0); // Image Capture Illumination

            // interleaved 2 of 5
            mScanner.setParameter(6, 1);
            mScanner.setParameter(22, 0);
            mScanner.setParameter(23, 55);
            mScanner.setParameter(402, 1);
        } else {
            Log.e(TAG, "Barcode initialization failure");
        }
    }

    @Override
    protected void onPreExecute() {
        // TODO Auto-generated method stub
        super.onPreExecute();
    }

}

private void echo(String message, CallbackContext callbackContext) {
    if (message != null && message.length() > 0) {
        callbackContext.success(message);
    } else {
        callbackContext.error("Expected one non-empty string argument.");
    }
}

@Override
public void onDestroy(){
    super.onDestroy();
    deinitalize();
}

@Override
public void onPause(boolean multitasking) {
    super.onPause(multitasking);
    mScanner.stopScan();
}

@Override
public void onResume(boolean multitasking){
    super.onResume(multitasking);
    //initScanner();
}


private void deinitalize(){
    Log.i(TAG, "+++ onDeinitalize");
    mScanner.stopScan();
    mScanner.close();
    Log.i(TAG, "--- onDeinitalize");
}

/*@Override
public void onDecodeEvent(BarcodeType type, String barcode) {

    Log.i(TAG, "onDecodeEvent(" + type + ", [" + barcode
            + "])");

    mScanResult = new ScanResult();
    mScanResult.scanResultType = type;
    mScanResult.scanResultBarcode = barcode;


    
    if(type != BarcodeType.NoRead){
        //int position = this.adapterBarcode.addItem(type, barcode);
        //this.lstBarcodeList.setSelection(position);
        String str = "{\'type\': \'" + type + "\' , \'barcode\': \'" + barcode + "\' }";
        PluginResult result;
        try {
            result = new PluginResult(PluginResult.Status.OK, new JSONObject(str));
        } catch(JSONException e){
            e.printStackTrace();
            result = new PluginResult(PluginResult.Status.ERROR, "Error in constructing json for success decode callback");
        }
        result.setKeepCallback(true);
        this.getDecode_callback.sendPluginResult(result);
        //this.getDecode_callback = null;
        return;
         //result.setKeepCallback(false);
        //beep(true);
    }else{
        PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Barcode not found");
        result.setKeepCallback(true);
        this.getDecode_callback.sendPluginResult(result);
        //this.getDecode_callback = null;
         //result.setKeepCallback(false);
        //beep(false);
        return;
    }

    
}

@Override
public void onStateChanged(EventType state) {
    
    Log.i(TAG, "EventType : " + state.toString());
}

/*
// Beep & Vibrate
private void beep(boolean isSuccess) {
    Log.i(TAG, "@@@@ DEBUG. Play beep....!!!!");
    try{
        if(isSuccess){
            this.mSoundPool.play(mBeepSuccess, 1, 1, 0, 0, 1);
            this.mVibrator.vibrate(100);
        }else{
            this.mSoundPool.play(mBeepFail, 1, 1, 0, 0, 1);
        }
    }catch(Exception ex){
    }
}
*/

/*private class ScanResult {
   public CallbackContext scanResultCallback; 
   public BarcodeType scanResultType;
   public String scanResultBarcode;



}
*/
}

