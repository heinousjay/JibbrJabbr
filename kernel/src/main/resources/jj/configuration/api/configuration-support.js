var collector = inject('jj.configuration.ConfigurationCollector');

module.exports = function(base) {

	function addElement(name, value) {
		collector.addConfigurationElement(base + '.' + name, value);
	}

	function accumulateError(name, error) {
		collector.accumulateError(name, error);
		return true;
	}
	
	return {
		addElement: addElement,
	
		makeBooleanProperty: function(name, validator) {
			return function(arg) {
				var error = false;
				if (typeof arg != 'boolean') {
					error = accumulateError(name, 'must be a boolean');
				} else if (typeof validator === 'function') {
					error = !validator(name, arg);
				}
				if (!error) {
					addElement(name, arg);
				}
				return this;
			}
		},
	
		makeIntProperty: function(name, validator) {
			return function(arg) {
				arg = parseInt(arg);
				var error = false;
				if (isNaN(arg)) {
					error = accumulateError(name, 'must be an integer');
				} else if (arg > java.lang.Integer.MAX_VALUE || arg < java.lang.Integer.MIN_VALUE) {
					error = accumulateError(name, 'is out of integer range');
				} else if (typeof validator === 'function') {
					error = !validator(name, arg);
				}
				if (!error) {
					addElement(name, new java.lang.Integer(arg));
				}
				return this;
			}
		},
	
		makeLongProperty: function(name, validator) {
			return function(arg) {
				arg = parseInt(arg);
				var error = false;
				// no need for range checks, javascript stops at 53 bits.  or something
				if (isNaN(arg)) {
					error = accumulateError(name, 'must be a long');
				} else if (typeof validator === 'function') {
					error = !validator(name, arg);
				}
				if (!error) {
					addElement(name, new java.lang.Long(arg));
				}
				return this;
			}
		},
	
		makeStringProperty: function(name, validator) {
			return function(arg) {
				var error = false;
				if (typeof arg != 'string') {
					error = accumulateError(name, 'must be a string');
				} else if (typeof validator === 'function') {
					error = !validator(name, arg);
				}
				
				if (!error) {
					addElement(name, new java.lang.String.valueOf(arg));
				}
				return this;
			}
		},
		
		makeAddToList: function(name, validator) {
			return function(arg) {
				if (typeof validator !== 'function' || validator(name, arg)) {
					collector.addConfigurationMultiElement(base + '.' + name, arg);
				}
				return this;
			}
		},
		
		addToList: function(name, value) {
			collector.addConfigurationMultiElement(base + '.' + name, value);
		},
		
		accumulateError: accumulateError
	}
}
