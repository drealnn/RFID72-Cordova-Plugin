var cordova = require('cordova'),
    exec = require('cordova/exec');

module.exports =  {
	initialize : function(successCallback, errorCallback){
		return exec(successCallback, errorCallback, "Rfid", 'initialize', []);
	},
	deinitalize : function(successCallback, errorCallback){
		return exec(successCallback, errorCallback, "Rfid", 'deinitalize', []);
	},
	wakeup : function(successCallback, errorCallback){
		return exec(successCallback, errorCallback, "Rfid", 'wakeup', []);
	},
	sleep : function(successCallback, errorCallback){
		return exec(successCallback, errorCallback, "Rfid", 'sleep', []);
	},
	getPowerRange : function(successCallback, errorCallback){
		return exec(successCallback, errorCallback, "Rfid", 'getPowerRange', []);
	},
	getPower : function(successCallback, errorCallback){
		return exec(successCallback, errorCallback, "Rfid", 'getPower', []);
	},
	setPower : function(powerInt, successCallback, errorCallback){
		return exec(successCallback, errorCallback, "Rfid", 'setPower', [powerInt]);
	},
	onReaderReadTag : function(successCallback, errorCallback){
		return exec(successCallback, errorCallback, "Rfid", 'onReaderReadTag', []);
	},
	start_readTagSingle :  function(successCallback, errorCallback, args){
		return exec(successCallback, errorCallback, "Rfid", 'start_readSingle', [args]);
	},
	start_readTagContinuous :  function(successCallback, errorCallback, args){
		return exec(successCallback, errorCallback, "Rfid", 'start_readContinuous', [args]);
	},
	start_readTagMemory : function(args, successCallback, errorCallback){
		return exec(successCallback, errorCallback, "Rfid", 'start_readMemory', [args]);
	},
	start_writeTagMemory : function(args, successCallback, errorCallback){
		return exec(successCallback, errorCallback, "Rfid", 'start_writeMemory', [args]);
	},
	stop_scan :  function(successCallback, errorCallback){
		return exec(successCallback, errorCallback, "Rfid", 'stop_read', []);
	},
	isRunning :  function(successCallback, errorCallback){
		return exec(successCallback, errorCallback, "Rfid", 'is_running', []);
	}


};



