package com.rfid72.app;

import com.rscja.deviceapi.RFIDWithUHF;


import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.content.Context;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Rfid extends CordovaPlugin {

private static final String TAG = "RFID Native"; 

protected RFIDWithUHF mReader;
private PowerManager.WakeLock mWakeLock = null;
private SoundPool mSoundPool;
private int mBeepSuccess;
private int mBeepFail;
private Vibrator mVibrator;
private Context ctx = null;

private CallbackContext keyup_callback = null;
private CallbackContext keydown_callback = null;
private CallbackContext onReaderReadTag_callback = null;
private CallbackContext onReaderResult_callback = null;
private CallbackContext onReaderStateChanged_callback = null;
private CallbackContext onReaderActionChanged_callback = null;
private View currentView = null;

private boolean loopFlag = false;
private int inventoryFlag = 1;
Handler handler;
private int minPower = 5;
private int maxPower = 30;

private boolean playSounds = true;

//Context context=this.cordova.getActivity().getApplicationContext();

@Override
public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
    result.setKeepCallback(true);
    
    // lifecycle functions //
    if (action.equals("initialize")){
		try {
			mReader = RFIDWithUHF.getInstance();
			if (mReader != null) {
				Boolean status = mReader.init();
				if (status){
					callbackContext.success("successfully initialized RFID device");
					return true;
				} else {
					throw new Exception("Unable to initialize RFID");
				}
			}
		} catch (Exception ex) {
			Log.e(TAG, "Failure to initialize RFID device. Aborting...");
            callbackContext.error("Failure to initialize RFID device");
			//toastMessage(ex.getMessage());
			return true;
		}
    }
    else if (action.equals("deinitalize")){
        this.deinitalize();
        return true;
    }
    else if (action.equals("wakeup")){
        Log.d(TAG, "+- wakeup scanner");

        /*if(mReader != null)
            ATRfidManager.wakeUp();*/

        callbackContext.success("Called wakeUp function");
        return true;
    }
    else if (action.equals("sleep")){
        Log.d(TAG, "+- sleep scanner");
        if (stopInventory()) {
            callbackContext.success("Called sleep function");
        } else {
            callbackContext.error("Error stopping inventory");
        }
        return true;
    }
    else if (action.equals("forceSleep")){
        Log.d(TAG, "+- force sleep scanner");
        if(mReader != null) {
            mReader.free();
            //mReader.disconnect();
        }

        callbackContext.success("Called sleep function");
        return true;
    }
    else if (action.equals("forceWake")){
        Log.d(TAG, "+- force wake scanner");
        if(mReader != null) {
            if(!mReader.init()) {
                Log.e(TAG, "ERROR. wakeUp() - Failed to connect rfid reader");
                callbackContext.error("ERROR. wakeUp() - Failed to connect rfid reader");
            } else {
                Log.i(TAG, "INFO. wakeUp()");
                callbackContext.success("Called wakeUp function");
            }
        }

        
        return true;
    }
    else if (action.equals("pause_scanner")){
        Log.d(TAG, "+- pause scanner");
        if (mReader != null){
            stopInventory(callbackContext);
        }
        //mReader.removeEventListener(this);
        return true;
    }
    else if (action.equals("resume_scanner")){
        Log.d(TAG, "+- resume scanner");
        //if (mReader != null)
            //mReader.setEventListener(this);
        return true;
    }

    // getters and setters //
    
    else if (action.equals("getPowerRange")){
        Log.d(TAG, "++Get Power Range");
        callbackContext.success(new JSONObject("{\'min\': \'" + minPower + "\' , \'max\' : \'" + maxPower + "\' }"));
        Log.d(TAG, "--Get Power Range");

        return true;
    }
    else if (action.equals("getPower")){
        Log.d(TAG, "++Get Power");
        int power = mReader.getPower();
        if (power > -1){
            callbackContext.success("" + power);
        } else {
            callbackContext.error("failed to get power");
        }
        Log.d(TAG, "--Get Power");

        return true;
    }
    else if (action.equals("setPower")){
        Log.d(TAG, "++set power level");
        if (mReader.setPower(args.getInt(0))){
            callbackContext.success("successfully set power level to "+args.getInt(0));
        } else {
            Log.e(TAG, String.format(
                    "ERROR. saveOption() - Failed to set power level [%s]",
                    args.getInt(0)));
            callbackContext.error("failed to set power level");
        }

        Log.d(TAG, "--set power level");
        return true;
    }
    // Reading and Writing //

    else if (action.equals("start_readContinuous"))
    {
        startInventory(callbackContext, args.getJSONObject(0));
        /*if (!args.getBoolean(0)){
            startAction(TagType.Tag6C, true, callbackContext);
        }
        else{
            final CallbackContext myCallbackContext = callbackContext;
            Log.d(TAG, "Starting read continuous on new thread");
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    startAction(TagType.Tag6C, true, myCallbackContext);
                }
            });
        }*/
        return true;
    }
    else if (action.equals("start_readSingle"))
    {
        String strUII = mReader.inventorySingleTag();
        String strEPC = mReader.convertUiiToEPC(strUII);
        Message msg = handler.obtainMessage();
        msg.obj = strEPC;
        handler.sendMessage(msg);
        /*if (!args.getBoolean(0)){
            startAction(TagType.Tag6C, false, callbackContext);
        }
        else{
            final CallbackContext myCallbackContext = callbackContext;
            Log.d(TAG, "Starting read single on new thread");
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    startAction(TagType.Tag6C, false, myCallbackContext);
                }
            });
        }*/
        
        return true;
    }
    else if (action.equals("start_readMemory"))
    {
        //startAction(TagType.Tag6C, args.getJSONObject(0), true, callbackContext);
        return true;
    }
    else if (action.equals("start_writeMemory"))
    {
        try {
            JSONObject params = args.getJSONObject(0);
            // EPC Bank
            RFIDWithUHF.BankEnum bank = params.isNull("bankType") ? RFIDWithUHF.BankEnum.valueOf("UII") : params.getString("bankType").equalsIgnoreCase("EPC") ? RFIDWithUHF.BankEnum.valueOf("UII") : null;
            // point 2 bytes ahead to skip header
            int offset = params.isNull("offset") ? 2 :  params.getInt("offset");
            // write 6 words (2 bytes per word, 96 bits)
            int length = params.isNull("length") ? 6 : params.getInt("length");
            // blank password
            String password =  params.isNull("password") ? "00000000" : params.getString("password");
            // data to write
            String data =  params.isNull("data") ? "" : params.getString("data");

            boolean r =  mReader.writeData_Ex(password,
                    bank,
                    offset,
                    length, data);
            if (r){
                callbackContext.success("successfully wrote to memory");
            } else {
                SoundLoader.getInstance(ctx).playFail();
                Log.e(TAG, "ERROR. startAction() - Failed to write memory to tag");
                callbackContext.error("ERROR. startAction() - Failed to write memory to tag");
            }
        } catch(JSONException e)
        {
            e.printStackTrace();
            callbackContext.error("Failed to write to memory, invalid JSON error.");
        }
        //startAction(TagType.Tag6C, args.getJSONObject(0), false, callbackContext);
        return true;
    }
    else if (action.equals("stop_read"))
    {
       /* final CallbackContext myCallbackContext = callbackContext;
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                stopAction(myCallbackContext);
            }
        });*/
        stopInventory(callbackContext);
        return true;
    }
    else if (action.equals("is_running"))
    {
        callbackContext.success(!(mReader.getReadMode() > -1)+"");
        /*final CallbackContext myCallbackContext = callbackContext;
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                myCallbackContext.success((mReader.getAction() == ActionState.Stop) + "");
            }
        });*/
        
        return true;
    }



    // Events //
    else if(action.equalsIgnoreCase("onReaderReadTag")){
            this.onReaderReadTag_callback = callbackContext;
            return true;
    }
    else if(action.equalsIgnoreCase("disableSounds")){
        playSounds = !args.getBoolean(0);
        return true;
    }
    return false;
}

@Override
public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
	try {
	    this.ctx = cordova.getActivity().getApplicationContext();
        handler = new ReadTagHandler();
		mReader = RFIDWithUHF.getInstance();
		if (mReader != null) {
			boolean status = mReader.init();
			if (status){
				//callbackContext.success("successfully initialized RFID device");
				//mReader.setEventListener(this);
				this.currentView = webView.getView();
				Log.i(TAG, "RFID device initialized");
			} else {
				throw new Exception("Unable to initialize RFID");
			}
		}
	} catch (Exception ex) {
		Log.e(TAG, "Failure to initialize RFID device. Aborting...");
		//callbackContext.error("Failure to initialize RFID device");
		//toastMessage(ex.getMessage());
		return;
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
    stopInventory();
}


private void deinitalize(){
    Log.d(TAG, "+++ onDeinitalize");
    stopInventory();
    if (mReader != null) {
        mReader.free();
        //mReader.free();
	}

    Log.d(TAG, "--- onDeinitalize");
}

private boolean stopInventory(CallbackContext callbackContext){
    if (stopInventory()){
        callbackContext.success("Success");
        return true;
    } else {
        callbackContext.error("failure to stop inventory");
        return false;
    }
}

private boolean stopInventory() {
    if (loopFlag) {
        loopFlag = false;
        if (mReader != null && mReader.stopInventory()) {
            return true;
        } else {
            Log.e(TAG, "Failure to stop inventory.");
            return false;
        }
    }
    return true;
}

private void startInventory(CallbackContext callbackContext, JSONObject options){
    Map<String, Object> optionsMap =  new HashMap<>();
    try {
        optionsMap.put("returnOnStop", options.optString("returnOnStop"));
        optionsMap.put("returnDistinct", options.optString("returnDistinct"));
        List<String> EPCFilter = new ArrayList<>();
        JSONArray arr = options.optJSONArray("epcFilter");
        if (arr != null){
            if (arr.length() > 0) {
                for (int i = 0; i < arr.length(); i++) {
                    EPCFilter.add(arr.getString(i));
                }
                optionsMap.put("epcFilter", EPCFilter);
            }
        }
    } catch (JSONException e){
        Log.e(TAG, "startInventory: Invalid params");
        optionsMap = new HashMap<>();
    }
    if (mReader.startInventoryTag(0,0)) {
        loopFlag = true;
        cordova.getThreadPool().execute(new TagThread(optionsMap));
        callbackContext.success("successfully reading continuously");
    } else {
        mReader.stopInventory();
        Log.e(TAG, "Failure to start inventory.");
        callbackContext.error("Failure to start inventory");
    }
}

class ReadTagHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
        String msgResult = msg.obj + "";
        String[] strs = msgResult.split("@");
        String tag = strs[0];
        String rssi = strs.length > 1 ? strs[1] : "0";
        if (onReaderReadTag_callback == null) {
            return;
        }

        try {
            String str = "{\'tag\':\'" + tag + "\' , \'rssi\': \'" + rssi + "\'}";
            PluginResult result = new PluginResult(PluginResult.Status.OK, new JSONObject(str));
            result.setKeepCallback(true);
            onReaderReadTag_callback.sendPluginResult(result);
            if (playSounds){
                SoundLoader.getInstance(ctx).playBeep();
            }
        } catch(Exception e)
        {
            e.printStackTrace();
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Error in handling onReaderReadTag event");
            result.setKeepCallback(true);
            onReaderReadTag_callback.sendPluginResult(result);
        }
    }
}

class TagThread implements Runnable {
    Map<String, Object> options = new HashMap<>();
    Set<String> epcFilterSet = new HashSet<>();
    Map<String, String> epcRssiMap = new HashMap<>();
    public TagThread(){
    }

    public TagThread(Map<String, Object> options){
        this.options = options;
        if (this.options.get("epcFilter") != null){
            List<String> epcTags = (List<String>) this.options.get("epcFilter");
            if (epcTags != null) {
                epcFilterSet.addAll(epcTags);
            }
        }
    }

    private boolean isEnabled(String setting){
        return this.options.get(setting) != null && !this.options.get(setting).equals("false");
    }

    private void sendTag(String epc, String rssi){
        if (!epcFilterSet.isEmpty() && !epcFilterSet.contains(epc)){
            return;
        }
        Message msg = handler.obtainMessage();
        msg.obj = epc + "@" + rssi;
        handler.sendMessage(msg);
    }

    public void run() {
        String[] res;
        while (loopFlag) {
            res = mReader.readTagFromBuffer();
            if (res != null) {
                String epc = mReader.convertUiiToEPC(res[1]);
                String rssi = res[2];
                if (this.isEnabled("returnDistinct")){
                    if (this.epcRssiMap.get(epc) != null){
                        if (!this.isEnabled("returnOnStop")) {
                            sendTag(epc, rssi);
                        }
                    }
                } else {
                    if (!this.isEnabled("returnOnStop")) {
                        sendTag(epc, rssi);
                    }
                }
                this.epcRssiMap.put(epc, rssi);
                Log.i("data","EPC:"+res[1]);
            }
        } // end while
        if (this.isEnabled("returnOnStop")){
            for (Map.Entry<String, String> entry : this.epcRssiMap.entrySet()) {
                sendTag(entry.getKey(), entry.getValue());
            }
        }

    }
}

/*protected void startAction(TagType tagType, boolean isContinuous, CallbackContext callbackContext) {

    ResultCode res = null;

    if (isContinuous) {
        // Multi Reading
        switch (tagType) {
        case Tag6C:
            if ((res = mReader.inventory6cTag()) != ResultCode.NoError) {
                Log.e(TAG,
                        String.format(
                                "ERROR. startAction() - Failed to start inventory 6C tag [%s]",
                                res));

                 callbackContext.error(String.format(
                                "ERROR. startAction() - Failed to start inventory 6C tag [%s]",
                                res));
                return;
            }
            break;
        case Tag6B:
            if ((res = mReader.inventory6bTag()) != ResultCode.NoError) {
                Log.e(TAG,
                        String.format(
                                "ERROR. startAction() - Failed to start inventory 6B tag [%s]",
                                res));
               
                return;
            }
            break;
        }
    } else {
        // Single Reading
        switch (tagType) {
        case Tag6C:
            if ((res = mReader.readEpc6cTag()) != ResultCode.NoError) {
                Log.e(TAG,
                        String.format(
                                "ERROR. startAction() - Failed to start read 6C tag [%s]",
                                res));
                callbackContext.error(String.format(
                                "ERROR. startAction() - Failed to start read 6C tag [%s]",
                                res));
                return;
            }
            break;
        case Tag6B:
            if ((res = mReader.readEpc6bTag()) != ResultCode.NoError) {
                Log.e(TAG,
                        String.format(
                                "ERROR. startAction() - Failed to start read 6B tag [%s]",
                                res));
                
                return;
            }
            break;
        }
    }
    callbackContext.success(isContinuous ? "successfully reading continuously: " + res : "successfully read single: " + res);
    Log.i(TAG, "INFO. startAction()");
}

protected void startAction(TagType tagType, JSONObject params, boolean isRead, CallbackContext callbackContext) {
    ResultCode res;
    BankType bank;
    int offset;
    int length;
    String data;
    String password;

    // TODO: set up logic for multiple banktype selection
    try {
        bank = params.isNull("bankType") ? BankType.EPC : params.getString("bankType").equalsIgnoreCase("EPC") ? BankType.EPC : null;
        offset = params.isNull("offset") ? 2 :  params.getInt("offset");
        length = params.isNull("length") ? 2 : params.getInt("length");
        password =  params.isNull("password") ? "" : params.getString("password");
        data =  params.isNull("data") ? "" : params.getString("data");
        
    } catch(JSONException e)
    {
        e.printStackTrace();
        callbackContext.error("Failed to read/write to memory, invalid JSON error.");
        return;
    }

    switch (tagType) {
        case Tag6C:
            //bank = getBank();
            if (isRead){
                if ((res = mReader.readMemory6c(bank, offset, length, password)) != ResultCode.NoError) {
                    Log.e(TAG,
                            String.format(
                                    "ERROR. startAction() - Failed to read memory 6C tag [%s]",
                                    res));
                    
                    callbackContext.error(String.format(
                                    "ERROR. startAction() - Failed to read memory 6C tag [%s]",
                                    res));
                    return;
                }

                callbackContext.success("successfully read memory : " + res);
            }
            else {
                if ((res = mReader.writeMemory6c(bank, offset, data, password)) != ResultCode.NoError) {
                    Log.e(TAG,
                            String.format(
                                    "ERROR. startAction() - Failed to write memory 6C tag [%s]",
                                    res));
                    
                    callbackContext.error(String.format(
                                    "ERROR. startAction() - Failed to write memory 6C tag [%s]",
                                    res));
                    return;
                }
                callbackContext.success("successfully wrote to memory : " + res);
            }
            break;
        case Tag6B:
            if ((res = mReader.readMemory6b(offset, length)) != ResultCode.NoError) {
                Log.e(TAG,
                        String.format(
                                "ERROR. startAction() - Failed to read memory 6B tag [%s]",
                                res));
                
                return;
            }
            break;
        }
        
        Log.i(TAG, "INFO. startAction()");
}

protected void stopAction(CallbackContext callbackContext) {

    ResultCode res;

    if ((res = mReader.stop()) != ResultCode.NoError) {
        Log.e(TAG, String.format(
                "ERROR. stopAction() - Failed to stop operation [%s]", res));
        callbackContext.error(String.format(
                "ERROR. stopAction() - Failed to stop operation [%s]", res));
        return;
    }

    callbackContext.success("successfully stopped reading operation");

    Log.i(TAG, "INFO. stopAction()");
}


/*
    ####### onReader event functions 
*/

//@Override
/*public void onReaderActionChanged(ATRfidReader reader, ActionState action) {
    Log.i(TAG, String.format("EVENT. onReaderActionchanged(%s)", action));
    if (action == ActionState.Stop) {
      //  adpTags.shutDown();
    } else {
        //adpTags.start();
    }

    if (this.onReaderActionChanged_callback == null)
        return;
    
    try {
        String str = action + "";
        PluginResult result = new PluginResult(PluginResult.Status.OK, str);
        result.setKeepCallback(true);
        this.onReaderActionChanged_callback.sendPluginResult(result);
        return;
    } catch(Exception e)
    {
        e.printStackTrace();
        PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Error in handling onReaderActionChanged event");
        result.setKeepCallback(true);
        this.onReaderActionChanged_callback.sendPluginResult(result);
        return;
    }

    //enableWidgets(true);
}

@Override
public void onReaderReadTag(ATRfidReader reader, String tag, float rssi, float phase) {

    //adpTags.addItem(tag, rssi);
    //txtCount.setText(String.format("%d", adpTags.getCount()));
    //playSuccess();

    Log.i(TAG,
            String.format("EVENT. onReaderReadTag([%s], %.2f)", tag, rssi));
    if (this.onReaderReadTag_callback == null)
        return;
    
    try {
        String str = "{\'tag\':\'" + tag + "\' , \'rssi\': \'" + rssi + "\' , \'phase\': \'" + phase + "\'}";
        PluginResult result = new PluginResult(PluginResult.Status.OK, new JSONObject(str));
        result.setKeepCallback(true);
        this.onReaderReadTag_callback.sendPluginResult(result);
        return;
    } catch(Exception e)
    {
        e.printStackTrace();
        PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Error in handling onReaderReadTag event");
        result.setKeepCallback(true);
        this.onReaderReadTag_callback.sendPluginResult(result);
        return;
    }
}

@Override
public void onReaderResult(ATRfidReader reader, ResultCode code,
        ActionState action, String epc, String data, float rssi, float phase) {
    Log.i(TAG, String.format("EVENT. onReaderResult(%s, %s, [%s], [%s]",
            code, action, epc, data));

    if (this.onReaderResult_callback == null)
        return;
    
    try {
        String str = String.format("{\'code\': \'%s\', \'action\': \'%s\', \'epc\':\'%s\', \'data\':\'%s\'}", code, action, epc, data);
        PluginResult result = new PluginResult(PluginResult.Status.OK, new JSONObject(str));
        result.setKeepCallback(true);
        this.onReaderResult_callback.sendPluginResult(result);
        return;
    } catch(Exception e)
    {
        e.printStackTrace();
        PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Error in handling onReaderResult event");
        result.setKeepCallback(true);
        this.onReaderResult_callback.sendPluginResult(result);
        return;
    }
}

@Override
public void onReaderStateChanged(ATRfidReader reader, ConnectionState state) {
    Log.i(TAG, String.format("EVENT. onReaderStateChanged(%s)", state));
    if (this.onReaderStateChanged_callback == null)
        return;
    
    try {
        String str = state + "";
        PluginResult result = new PluginResult(PluginResult.Status.OK, str);
        result.setKeepCallback(true);
        this.onReaderStateChanged_callback.sendPluginResult(result);
        return;
    } catch(Exception e)
    {
        e.printStackTrace();
        PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Error in handling onReaderStateChanged event");
        result.setKeepCallback(true);
        this.onReaderStateChanged_callback.sendPluginResult(result);
        return;
    }
}*/


}

