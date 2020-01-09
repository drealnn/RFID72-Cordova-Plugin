var cordova = require('cordova'),
    exec = require('cordova/exec');

module.exports =  {
	scanner_handle_keycode : 280,
	onKeyUp : function(successCallback, errorCallback){
		return exec(successCallback, errorCallback, "General", 'register_keyUp', []);
	},
	onKeyDown : function(successCallback, errorCallback){
		return exec(successCallback, errorCallback, "General", 'register_keyDown', []);
	},
	playSound : function(soundName, successCallback, errorCallback){
		return exec(successCallback, errorCallback, "General", 'playSound', [soundName]);
	}
}