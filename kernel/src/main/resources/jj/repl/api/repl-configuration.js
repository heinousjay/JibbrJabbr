var support = require('jj/configuration-support')('jj.repl.ReplConfiguration');

module.exports = {
	activate: support.makeBooleanProperty('activate'),
	port: support.makeIntProperty('port', function(name, arg) {
		if (arg < 1024 || arg > 65535) {
			support.accumulateError(name, " must be greater than 1023 and less than 65536");
		}
	})
};