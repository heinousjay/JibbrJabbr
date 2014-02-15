// commonjs module, so only what's assigned to exports is going to be exposed

var amp = /&/g;
var quot = /"/g;
var lt = /</g;

exports.dehtml = function(input) {
	require('./helpers2').printHelpersId();
	return input.replace(amp, '&amp;').replace(lt, '&lt;').replace(quot, '&quot;');
};

exports.printModuleId = function() {
	java.lang.System.err.println('hi from ' + module.id);
};
