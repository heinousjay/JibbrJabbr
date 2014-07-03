var support = require('configuration-support');
var base = 'jj.repl.ReplConfiguration.';

module.exports = {
	activate: support.makeBooleanProperty(base, 'activate'),
	port: support.makeIntProperty(base, 'port', function(name, arg) {
		if (arg < 1024 || arg > 65535) {
			throw new TypeError(name + " must be greater than 1023 and less than 65536");
		}
	})
};