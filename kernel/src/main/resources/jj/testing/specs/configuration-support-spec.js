var location, value;

var valueOf = java.lang.String.valueOf;

// replacing inject('jj.configuration.ConfigurationCollector')
function inject(obj) {
	return {
		addConfigurationElement: function(l, v) {
			location = l;
			value = v;
		}
	}
}

describe("function addElement", function() {
	it("passes location and value to the collector", function() {
		addElement("location", "value");
		expect(location).toBe("location");
		expect(value).toBe("value");
	});
});

describe("function makeBooleanProperty", function() {
	
	it("rejects values that are not booleans", function() {
		var testFunc = module.exports.makeBooleanProperty('base', 'name');
		
		expect(function() {
			testFunc("not a boolean")
		}).toThrow(new TypeError("name must be a boolean"));
		
		expect(function() {
			testFunc(1)
		}).toThrow(new TypeError("name must be a boolean"));
	});
	
	it("sets boolean values to the collector", function() {
		var testFunc = module.exports.makeBooleanProperty('base', 'name');
		
		testFunc(true);
		expect(location).toBe("basename");
		expect(value).toBe(true);

		testFunc(false);
		expect(location).toBe("basename");
		expect(value).toBe(false);
		
	});
	
	it("calls a given validator function", function() {
		var name, arg;
		
		function validator(n, a) {
			name = n;
			arg = a;
		}
		
		var testFunc = module.exports.makeBooleanProperty('base', 'name', validator);
		
		testFunc(true);
		expect(name).toBe("name");
		expect(arg).toBe(true);
	});
});

describe("function makeIntProperty", function() {
	
	it("rejects values that are not integers", function() {
		var testFunc = module.exports.makeIntProperty('base', 'name');
		
		expect(function() {
			testFunc("not an int")
		}).toThrow(new TypeError("name must be an integer"));
		
		expect(function() {
			testFunc(true)
		}).toThrow(new TypeError("name must be an integer"));
		
		expect(function() {
			testFunc(java.lang.Integer.MAX_VALUE + 1)
		}).toThrow(new TypeError("name is out of integer range"));
		
		expect(function() {
			testFunc(java.lang.Integer.MIN_VALUE - 1)
		}).toThrow(new TypeError("name is out of integer range"));
	});
	
	it("sets integer values to the collector", function() {
		var testFunc = module.exports.makeIntProperty('base', 'name');
		
		var val = 1;
		testFunc(val);
		expect(location).toBe("basename");
		expect(value).toBeCloseTo(val, 0);

		val = 5000;
		testFunc(val);
		expect(location).toBe("basename");
		expect(value).toBeCloseTo(val, 0);
		
	});
	
	it("calls a given validator function", function() {
		var name, arg;
		
		function validator(n, a) {
			name = n;
			arg = a;
		}
		
		var testFunc = module.exports.makeIntProperty('base', 'name', validator);
		
		testFunc(1);
		expect(name).toBe("name");
		expect(arg).toBe(1);
	});
});

describe("function makeLongProperty", function() {
	
	it("rejects values that are not longs", function() {
		var testFunc = module.exports.makeLongProperty('base', 'name');
		
		expect(function() {
			testFunc("not an int")
		}).toThrow(new TypeError("name must be an long"));
		
		expect(function() {
			testFunc(true)
		}).toThrow(new TypeError("name must be an long"));
	});
	
	it("sets long values to the collector", function() {
		var testFunc = module.exports.makeLongProperty('base', 'name');
		
		var val = 1;
		testFunc(val);
		expect(location).toBe("basename");
		expect(value).toBeCloseTo(val, 0);

		val = 5000;
		testFunc(val);
		expect(location).toBe("basename");
		expect(value).toBeCloseTo(val, 0);
		
	});
	
	it("calls a given validator function", function() {
		var name, arg;
		
		function validator(n, a) {
			name = n;
			arg = a;
		}
		
		var testFunc = module.exports.makeLongProperty('base', 'name', validator);
		
		testFunc(1);
		expect(name).toBe("name");
		expect(arg).toBe(1);
	});
});

describe("function makeStringProperty", function() {
	
	it("sets values to the collector", function() {
		var testFunc = module.exports.makeStringProperty('base', 'name');

		testFunc("hi");
		expect(location).toEqual("basename");
		expect(value).toEqual(valueOf("hi"));

		testFunc(1);
		expect(location).toEqual("basename");
		expect(value).toEqual(valueOf(1));

		testFunc(true);
		expect(location).toEqual("basename");
		expect(value).toEqual(valueOf("true"));
		
	});
	
	it("calls a given validator function", function() {
		var name, arg;
		
		function validator(n, a) {
			name = n;
			arg = a;
		}
		
		var testFunc = module.exports.makeStringProperty('base', 'name', validator);
		
		testFunc(1);
		expect(name).toEqual("name");
		expect(arg).toEqual(1);
	});
});