
// commonjs module, so only what's assigned to exports is going to be exposed

var amp = /&/g;
var quot = /"/g;
var lt = /</g;

exports.dehtml = function(input) {
	java.lang.System.err.println("********************************************************");
	java.lang.System.err.println("********************************************************");
	java.lang.System.err.println("**********                                    **********");
	java.lang.System.err.println("********************************************************");
	java.lang.System.err.println("********************************************************");
	java.lang.System.err.println("*   hi! from helpers.js dehtml function.  enable me!   *");
	java.lang.System.err.println("********************************************************");
	java.lang.System.err.println("********************************************************");
	java.lang.System.err.println("**********                                    **********");
	java.lang.System.err.println("********************************************************");
	java.lang.System.err.println("********************************************************");
	// for the moment, this  is trouble but
	// uncomment when this will run!
	//require('./helpers2').printHelpersId();
	return input.replace(amp, '&amp;').replace(lt, '&lt;').replace(quot, '&quot;');
};

exports.printModuleId = function() {
	print('hi from ' + module.id);
};
