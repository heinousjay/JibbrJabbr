var support = require('jj/configuration-support')('jj.script.ScriptExecutionConfiguration');

module.exports = {
	threadCount: support.makeIntProperty('threadCount', function(name, arg) {
		if (arg < 1) {
			support.accumulateError(name, " must be positive");
			return false;
		}
		return true;
	})
}