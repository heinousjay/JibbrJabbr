var support = require('jj/configuration-support');
var base = 'jj.script.ScriptExecutionConfiguration.';

module.exports = {
	threadCount: support.makeIntProperty(base, 'threadCount', function(name, arg) {
		if (arg < 1) {
			throw new TypeError(name + " must be positive");
		}
	})
}