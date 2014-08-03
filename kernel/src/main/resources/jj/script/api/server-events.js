var helper = inject('jj.script.api.ServerEventScriptBridge');

var NotAnEventClass = Packages.jj.script.api.ServerEventScriptResult.NotAnEventClass;
var AlreadyBound    = Packages.jj.script.api.ServerEventScriptResult.AlreadyBound;
var NotBound        = Packages.jj.script.api.ServerEventScriptResult.NotBound;
var Success         = Packages.jj.script.api.ServerEventScriptResult.Success;

module.exports = {
		
	on: function(name, callback) {
		
		if (typeof name !== 'string' || typeof callback !== 'function') {
			throw new Error("on requires a string argument and a function argument");
		}
		
		var result = helper.subscribe(name, callback);
		if (result === NotAnEventClass) {
			throw new Error(name + " is not an event class");
		}
		if (result === AlreadyBound) {
			throw new Error(name + " already has a binding to the given function");
		}
		
		return module.exports;
	},
	off: function(name, callback) {
		
		if (typeof name !== 'string' || typeof callback !== 'function') {
			throw new Error("off requires a string argument and a function argument");
		}
		
		var result = helper.unsubscribe(name, callback);
		// should this matter? is it a no-op?
		if (result === NotBound) {
			throw new Error(name + ' was not previously bound to the given function');
		}
		return module.exports;
	},
	once: function(name, callback) {
		
		if (typeof name !== 'string' || typeof callback !== 'function') {
			throw new Error("once requires a string argument and a function argument");
		}
		
		function wrapper(e) {
			callback(e);
			module.exports.off(name, wrapper);
		}
		return module.exports.on(name, wrapper);
	}
}
