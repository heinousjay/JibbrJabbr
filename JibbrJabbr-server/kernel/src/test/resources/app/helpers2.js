exports.lightService = new RestService({
	baseUrl: 'http://192.168.1.12/api/jaystestname',
	operations: {
		status: true
	}
});

var helpers = require('./helpers');

exports.printHelpersId = function() {
	helpers.printModuleId();
}