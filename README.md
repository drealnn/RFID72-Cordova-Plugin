# cordova-plugin-RFID72
Phonegap/Cordova based RFID and barcode scanning plugin for the RFID 72 handheld scanner

Developed by Simplicity Solutions Group

https://simplicitysolutionsgroup.com/

## Questions?
Contact us at:

dsledd@simplicitysolutionsgroup.com

jfairfull@simplicitysolutionsgroup.com

## Installing:


## Methods:

```
atid.general {
  scanner_handle_keycode : 2,
	
	onKeyUp : function(successCallback, errorCallback){
		exec(successCallback, errorCallback, "Atid", 'register_keyUp', []);
	},
	onKeyDown : function(successCallback, errorCallback){
		exec(successCallback, errorCallback, "Atid", 'register_keyDown', []);
	},
	playSound : function(soundName, successCallback, errorCallback){
		exec(successCallback, errorCallback, "Atid", 'playSound', [soundName]);
	}
}

atid.barcode {
  startDecode : function(successCallback, errorCallback){
		exec(successCallback, errorCallback, "Barcode", 'scanner_startDecode', []);
	},
	stopDecode : function(successCallback, errorCallback){
		exec(successCallback, errorCallback, "Barcode", 'scanner_stopDecode', []);
	},
	isDecoding : function(successCallback, errorCallback){
		exec(successCallback, errorCallback, "Barcode", 'scanner_isDecoding', []);
	},
	wakeup : function(successCallback, errorCallback){
		exec(successCallback, errorCallback, "Barcode", 'wakeup_scanner', []);
	},
	sleep : function(successCallback, errorCallback){
		exec(successCallback, errorCallback, "Barcode", 'sleep_scanner', []);
	},
	deinitialize : function(successCallback, errorCallback){
		exec(successCallback, errorCallback, "Barcode", 'deinitialize_scanner', []);
	},
	onDecode : function(successCallback, errorCallback){
		exec(successCallback, errorCallback, "Barcode", 'register_decode', []);
	}
}

atid.rfid {
  deinitalize : function(successCallback, errorCallback){
  		exec(successCallback, errorCallback, "Rfid", 'deinitalize', []);
	},
	wakeup : function(successCallback, errorCallback){
		exec(successCallback, errorCallback, "Rfid", 'wakeup', []);
	},
	sleep : function(successCallback, errorCallback){
		exec(successCallback, errorCallback, "Rfid", 'sleep', []);
	},
	pause_scanner : function(successCallback, errorCallback){
		exec(successCallback, errorCallback, "Rfid", 'pause_scanner', []);
	},
	resume_scanner : function(successCallback, errorCallback){
		exec(successCallback, errorCallback, "Rfid", 'resume_scanner', []);
	},
	getActionState : function(successCallback, errorCallback){
		exec(successCallback, errorCallback, "Rfid", 'getActionState', []);
	},
	getPowerRange : function(successCallback, errorCallback){
		exec(successCallback, errorCallback, "Rfid", 'getPowerRange', []);
	},
	getPower : function(successCallback, errorCallback){
		exec(successCallback, errorCallback, "Rfid", 'getPower', []);
	},
	getOperationTime : function(successCallback, errorCallback){
		exec(successCallback, errorCallback, "Rfid", 'getOperationTime', []);
	},
	setPower : function(powerInt, successCallback, errorCallback){
		exec(successCallback, errorCallback, "Rfid", 'setPower', [powerInt]);
	},
	setOperationTime : function(operationTime, successCallback, errorCallback){
		exec(successCallback, errorCallback, "Rfid", 'setOperationTime', [operationTime]);
	},
	setInventoryTime : function(inventoryTime, successCallback, errorCallback){
		exec(successCallback, errorCallback, "Rfid", 'setInventoryTime', [inventoryTime]);
	},
	setIdleTime : function(idleTime, successCallback, errorCallback){
		exec(successCallback, errorCallback, "Rfid", 'setIdleTime', [idleTime]);
	},
	onReaderReadTag : function(successCallback, errorCallback){
		exec(successCallback, errorCallback, "Rfid", 'onReaderReadTag', []);
	},
	onReaderResult :  function(successCallback, errorCallback){
		exec(successCallback, errorCallback, "Rfid", 'onReaderResult', []);
	},
	start_readTagSingle :  function(successCallback, errorCallback){
		exec(successCallback, errorCallback, "Rfid", 'start_readSingle', []);
	},
	start_readTagContinuous :  function(successCallback, errorCallback){
		exec(successCallback, errorCallback, "Rfid", 'start_readContinuous', []);
	},
	start_readTagMemory : function(args, successCallback, errorCallback){
		exec(successCallback, errorCallback, "Rfid", 'start_readMemory', [args]);
	},
	start_writeTagMemory : function(args, successCallback, errorCallback){
		exec(successCallback, errorCallback, "Rfid", 'start_writeMemory', [args]);
	},
	stop_scan :  function(successCallback, errorCallback){
		exec(successCallback, errorCallback, "Rfid", 'stop_read', []);
	},
	isStopped :  function(successCallback, errorCallback){
		exec(successCallback, errorCallback, "Rfid", 'isStopped', []);
	}
}

```
## Notes

Methods can be roughly categorized into four sections:

lifecycle (wakeup, sleep, deinitialize),
setters & getters,
read/write,
events

When I was developing this plugin, the documentation for the device was pretty bare. The example android code that came with the SDK package wouldn't work without modification.

The android SDK that is compiled with this plugin seems to be outdated and has an error in the rfid write functionality. For some reason it would only write a limited amount of data to the EPC. I had to implement a hacky solution to get it working by only writing four characters at a time and incrementing an offset.

If anybody has a more up-to-date android SDK and has issue with the current plugin, please email it to me at dsledd@simplicitysolutionsgroup.com.

####RFID: 

Use readTagSingle/readTagContinous to grab the rfid EPC and RSSI.

The difference between readTagSingle/Continuous vs readTagMemory is the latter allows the selection of specific words to be grabbed from the rfid chip based on the given json arguments; however keep in mind both readTagMemory/writeTagMemory don't natively grab the rssi value from its event listener.

The args json object for readTagMemory/writeTagMemory is as follows (each name/value pair is optional, defaults will be used, offset and length are measured in 16 bit words ie. length = 2 : get the leftmost 32 bits):

```
start_readTagMemory
{
  'bankType' : 'EPC'
  'offset' : 2
  'length' : 2
  'password' :  ''
}

start_writeTagMemory
{
  'bankType' : 'EPC'
  'offset' : 2
  'password' : ''
  'data' : ''
}
```

## Example Use
```
$(document).on('deviceready', function(){


      //  ++ barcode decoding ++ //

      $('input[name="barcode-scanner"]').click(function(){
          //console.log('button click');
          $(this).prop("disabled",true);
          
          atid.barcode.isDecoding(function(msg){
              if (msg == 'true')
              {
                  atid.barcode.stopDecode(function(){}, function(){});
              }
              else
              {
                  atid.barcode.startDecode(function(){}, function(){});
              }
              $('input[name="barcode-scanner"]').prop("disabled",false);
          }, function(){});
         
      });

      atid.barcode.onDecode(function(scanResults){
          console.log(scanResults.type + ' ' + scanResults.barcode);
          atid.general.playSound('success');
          //atid.barcode.stopDecode(function(){}, function(){});
      }, function(msg){console.log(msg); atid.general.playSound('fail');});

      //  -- barcode decoding -- //

      //  ++ ATID trigger handle / RFID read tag example ++ //

      atid.general.onKeyUp(function(key){
          if (key.keyCode == atid.general.scanner_handle_keycode && key.repeatCount <= 0){
              atid.rfid.isStopped(function(msg){
                  if (msg == 'false')
                      atid.rfid.stop_scan(printMsg, printMsg);
              });
          }
      }, function(){});

      atid.general.onKeyDown(function(key)
      {
          if (key.keyCode == atid.general.scanner_handle_keycode && key.repeatCount <= 0){
              atid.rfid.isStopped(function(msg){
                  if (msg == 'true')
                      atid.rfid.start_readTagContinuous(printMsg, printMsg);
              });
          }
          
      }, function(){});

      //  -- ATID trigger handle, RFID start scan -- //

      // ++ RFID onReaderRead events ++ //

      // called when start_readTagSingle or start_readTagContinuous is invoked
      atid.rfid.onReaderReadTag(function(data){
          console.log("Tag : " + data.tag + " RSSI: " + data.rssi);
          atid.general.playSound('beep');
      }, printMsg);

      // onReaderResult called when start_readTagMemory is invoked, doesn't contain rssi value
      atid.rfid.onReaderResult(function(data){
          console.log(data);
          atid.general.playSound('beep');
      }, printMsg);

      // -- RFID onReaderRead events -- //

    });
```
