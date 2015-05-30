var support = require('jj/configuration-support')('jj.logging.LoggingConfiguration');
var key = 'loggingLevels';
var helper = inject('jj.logging.LoggingConfigurator');
var level = Packages.jj.logging.Level;
var levels = [level.Off, level.Error, level.Warn, level.Info, level.Debug, level.Trace];
var names = helper.loggerNames();

function makeLevelSetter(logger, level) {
	return function() {
		support.addToMap(key, logger, level);
		return module.exports;
	}
}

for (name in names) {
	(function(name, logger) {
		var x = module.exports[name] = {};
		levels.forEach(function(level) {
			x[level.name().toLowerCase()] = makeLevelSetter(logger, level);
		});
	})(name, names[name]);
}

module.exports.script = function(path) {
	var x = {};
	levels.forEach(function(level) {
		x[level.name().toLowerCase()] = makeLevelSetter("script@" + path, level);
	});
	return x;
}
