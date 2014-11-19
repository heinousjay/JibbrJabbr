var collector = inject('jj.configuration.ConfigurationCollector');
var key = 'jj.logging.LoggingConfiguration.loggingLevels';
var helper = inject('jj.logging.LoggingConfigurator');
var level = Packages.jj.logging.Level;
var levels = [level.Off, level.Error, level.Warn, level.Info, level.Debug, level.Trace];
var names = helper.loggerNames();

function makeLevelSetter(logger, level) {
	return function() {
		collector.addConfigurationMappedElement(key, logger, level);
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
