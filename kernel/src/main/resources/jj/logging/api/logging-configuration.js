var collector = inject('jj.configuration.ConfigurationCollector');
var key = 'jj.logging.LoggingConfiguration.loggingLevels';
var helper = inject('jj.logging.LoggingConfigurator');
var level = Packages.jj.logging.Level;
var names = helper.loggerNames();

for (name in names) {
	(function(name, logger) {
		module.exports[name] = {
			off: function() {
				collector.addConfigurationMappedElement(key, logger, level.Off);
				return module.exports;
			},
			error: function() {
				collector.addConfigurationMappedElement(key, logger, level.Error);
				return module.exports;
			},
			warn: function() {
				collector.addConfigurationMappedElement(key, logger, level.Warn);
				return module.exports;
			},
			info: function() {
				collector.addConfigurationMappedElement(key, logger, level.Info);
				return module.exports;
			},
			debug: function() {
				collector.addConfigurationMappedElement(key, logger, level.Debug);
				return module.exports;
			},
			trace: function() {
				collector.addConfigurationMappedElement(key, logger, level.Trace);
				return module.exports;
			}
		}
	})(name, names[name]);
}
