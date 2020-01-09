var cordova = require('cordova'),
    exec = require('cordova/exec');

module.exports =  {
	echoObject : function(str, callback) {
	    exec(callback, function(err) {
		callback('Nothing to echo.');
	    }, "Barcode", "echo", [str]);
	},
	startDecode : function(successCallback, errorCallback){
		return exec(successCallback, errorCallback, "Barcode", 'scanner_startDecode', []);
	},
	stopDecode : function(successCallback, errorCallback){
		return exec(successCallback, errorCallback, "Barcode", 'scanner_stopDecode', []);
	},
	isDecoding : function(successCallback, errorCallback){
		return exec(successCallback, errorCallback, "Barcode", 'scanner_isDecoding', []);
	},
	wakeup : function(successCallback, errorCallback){
		return exec(successCallback, errorCallback, "Barcode", 'wakeup_scanner', []);
	},
	sleep : function(successCallback, errorCallback){
		return exec(successCallback, errorCallback, "Barcode", 'sleep_scanner', []);
	},
	deinitialize : function(successCallback, errorCallback){
		return exec(successCallback, errorCallback, "Barcode", 'deinitialize_scanner', []);
	},
	onDecode : function(successCallback, errorCallback){
		return exec(successCallback, errorCallback, "Barcode", 'register_decode', []);
	}

};



