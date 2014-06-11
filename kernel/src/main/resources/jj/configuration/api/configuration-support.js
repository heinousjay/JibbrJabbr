var collector = inject('jj.configuration.ConfigurationCollector');

module.exports = {

	makeBooleanProperty: function(base, name) {
		return function(arg) {
			if (typeof arg != 'boolean') { throw new TypeError(name + ' must be a boolean'); }
			collector.addConfigurationElement(base + name, arg);
			return this;
		}
	},

	makeIntProperty: function(base, name) {
		return function(arg) {
			arg = parseInt(arg);
			if (isNaN(arg)) { throw new TypeError(name + ' must be an int'); }
			collector.addConfigurationElement(base + name, new java.lang.Integer(arg));
			return this;
		}
	}
}
