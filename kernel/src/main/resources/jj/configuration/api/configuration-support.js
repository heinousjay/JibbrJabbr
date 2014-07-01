var collector = inject('jj.configuration.ConfigurationCollector');

function addElement(location, value) {
	collector.addConfigurationElement(location, value);
}

module.exports = {
		
	addElement: addElement,

	makeBooleanProperty: function(base, name, validator) {
		return function(arg) {
			if (typeof arg != 'boolean') { throw new TypeError(name + ' must be a boolean'); }
			if (typeof validator == 'function') { validator(name, arg); }
			addElement(base + name, arg);
			return this;
		}
	},

	makeIntProperty: function(base, name, validator) {
		return function(arg) {
			arg = parseInt(arg);
			if (isNaN(arg)) { throw new TypeError(name + ' must be an int'); }
			if (typeof validator == 'function') { validator(name, arg); }
			addElement(base + name, new java.lang.Integer(arg));
			return this;
		}
	},

	makeStringProperty: function(base, name, validator) {
		return function(arg) {
			if (typeof validator == 'function') { validator(name, arg); }
			addElement(base + name, new java.lang.String.valueOf(arg));
			return this;
		}
	}
}
