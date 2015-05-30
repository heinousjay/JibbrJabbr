var console = inject('jj.script.api.ScriptConsole');

function arrayFromArgs(args) {
	// have to do it in two steps or it fails, silently
	var map = Array.prototype.map.bind(args);
	return map(function(arg) {
		return JSON.stringify(arg);
	});
}

module.exports = {
	trace: function() {
		console.trace(arrayFromArgs(arguments));
	},
	debug: function() {
		console.debug(arrayFromArgs(arguments));
	},
	log: function() {
		console.info(arrayFromArgs(arguments));
	},
	info: function() {
		console.info(arrayFromArgs(arguments));
	},
	warn: function() {
		console.warn(arrayFromArgs(arguments));
	},
	error: function() {
		console.error(arrayFromArgs(arguments));
	}
};
