var collector = inject('jj.configuration.ConfigurationCollector');

module.exports = function(base) {

	function addElement(name, value) {
		collector.addConfigurationElement(base + '.' + name, value);
	}
	
	const ERROR_DEPTH = 1;
	const INNER_DEPTH = 2;
	const VALIDATOR_DEPTH = 3;
	var errorDepth = ERROR_DEPTH;
	function accumulateError(name, error, depth) {
		try {
			throw new Error();
		} catch (e) {
			collector.accumulateError(name, error + ": " + e.stack.split(/\n/g)[depth || errorDepth].trim());
		}
		errorDepth = ERROR_DEPTH;
		return true;
	}
	
	var self = {
		addElement: addElement,
	
		makeBooleanProperty: function(name, validator) {
			return function(arg) {
				var error = false;
				if (typeof arg != 'boolean') {
					errorDepth = INNER_DEPTH;
					error = accumulateError(name, 'must be a boolean');
				} else if (typeof validator === 'function') {
					errorDepth = VALIDATOR_DEPTH;
					error = validator(name, arg);
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
					errorDepth = INNER_DEPTH;
					error = accumulateError(name, 'must be an integer');
				} else if (arg > java.lang.Integer.MAX_VALUE || arg < java.lang.Integer.MIN_VALUE) {
					errorDepth = INNER_DEPTH;
					error = accumulateError(name, 'is out of integer range');
				} else if (typeof validator === 'function') {
					errorDepth = VALIDATOR_DEPTH;
					error = validator(name, arg);
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
					errorDepth = INNER_DEPTH;
					error = accumulateError(name, 'must be a long');
				} else if (typeof validator === 'function') {
					errorDepth = VALIDATOR_DEPTH;
					error = validator(name, arg);
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
					errorDepth = INNER_DEPTH;
					error = accumulateError(name, 'must be a string');
				} else if (typeof validator === 'function') {
					errorDepth = VALIDATOR_DEPTH;
					error = validator(name, arg);
				}
				
				if (!error) {
					addElement(name, new java.lang.String.valueOf(arg));
				}
				return this;
			}
		},
		
		makeAddToList: function(name, validator) {
			return function(arg) {
				var error = false;
				if (typeof validator === 'function') {
					errorDepth = VALIDATOR_DEPTH;
					error = validator(name, arg);
				}
				if (!error) {
					self.addToList(name, arg);
				}
				return this;
			}
		},
		
		addToList: function(name, value) {
			collector.addConfigurationMultiElement(base + '.' + name, value);
		},
		
		makeAddToMap: function(name, validator) {
			return function(key, value) {
				var error = false;
				if (typeof validator === 'function') {
					errorDepth = VALIDATOR_DEPTH;
					error = validator(name, key, value);
				}
				if (!error) {
					self.addToMap(name, key, value);
				}
				return this;
			}
		},
		
		addToMap: function(name, key, value) {
			collector.addConfigurationMappedElement(base + '.' + name, key, value);
		},
		
		accumulateError: accumulateError
	};
	
	return self;
}
