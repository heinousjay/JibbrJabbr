var location, value;

var valueOf = java.lang.String.valueOf;
var collectorMock = {
	addConfigurationElement: function() {},
	accumulateError: function() {}
}

// replacing inject('jj.configuration.ConfigurationCollector')
function inject(obj) {
	return collectorMock;
}

describe("configuration-support.js", function() {
	
	var base = "base";
	var name = "name";
	var basename = base + '.' + name;
	
	function failingValidator(n, arg) {
		module.exports(base).accumulateError(n, "error");
		return false;
	}

	function passingValidator(n, arg) {
		return true;
	}
	
	beforeEach(function() {
		spyOn(collectorMock, 'accumulateError');
		spyOn(collectorMock, 'addConfigurationElement');
	});
	
	describe("function makeBooleanProperty", function() {
		
		var testFunc;
		var mustBeABoolean = 'must be a boolean';
		
		beforeEach(function() {
			testFunc = module.exports(base).makeBooleanProperty(name);
		});
		
		it("rejects string values", function() {
			testFunc("not a boolean");
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeABoolean);
		});
		
		it("rejects number values", function() {
			testFunc(1);
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeABoolean);
		});
		
		it("rejects object values", function() {
			testFunc({});
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeABoolean);
		});
		
		it("rejects array values", function() {
			testFunc([]);
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeABoolean);
		});
		
		it("rejects null values", function() {
			testFunc(null);
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeABoolean);
		});
		
		it("accumulates validator failures", function() {
			module.exports(base).makeBooleanProperty(name, failingValidator)(true);
			expect(collector.addConfigurationElement).not.toHaveBeenCalled();
			expect(collector.accumulateError).toHaveBeenCalledWith(name, 'error');
		});
		
		it("calls a passing validator function", function() {
			module.exports(base).makeBooleanProperty(name, passingValidator)(true);
			expect(collector.addConfigurationElement).toHaveBeenCalledWith(basename, true);
		});
		
		it("sets boolean values to the collector", function() {
			
			testFunc(true);
			expect(collector.addConfigurationElement).toHaveBeenCalledWith(basename, true);
	
			testFunc(false);
			expect(collector.addConfigurationElement).toHaveBeenCalledWith(basename, false);
			
		});
	});
	
	describe("function makeIntProperty", function() {
		
		var testFunc;
		var mustBeAnInteger = 'must be an integer';
		var isOutOfIntegerRange = 'is out of integer range';
		
		beforeEach(function() {
			testFunc = module.exports(base).makeIntProperty(name);
		});
		
		it("rejects string values", function() {
			testFunc("not an int");
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeAnInteger);
		});
		
		it("rejects boolean values", function() {
			testFunc(true);
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeAnInteger);
		});
		
		it("rejects object values", function() {
			testFunc({});
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeAnInteger);
		});
		
		it("rejects array values", function() {
			testFunc([]);
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeAnInteger);
		});
		
		it("rejects null values", function() {
			testFunc(null);
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeAnInteger);
		});
		
		it("rejects too large values", function() {
			testFunc(java.lang.Integer.MAX_VALUE + 1);
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, isOutOfIntegerRange);
		});
		
		it("rejects too small values", function() {
			testFunc(java.lang.Integer.MIN_VALUE - 1)
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, isOutOfIntegerRange);
		});
		
		it("accumulates validator failures", function() {
			module.exports(base).makeIntProperty(name, failingValidator)(1);
			expect(collector.addConfigurationElement).not.toHaveBeenCalled();
			expect(collector.accumulateError).toHaveBeenCalledWith(name, 'error');
		});
		
		it("calls a passing validator function", function() {
			var val = 1;
			module.exports(base).makeIntProperty(name, passingValidator)(val);
			var args = collector.addConfigurationElement.calls.mostRecent().args;
			expect(args[0]).toBe(basename)
			expect(args[1]).toBeCloseTo(val);
		});
		
		it("sets integer values to the collector", function() {
			
			var val = 1;
			testFunc(val);
			var args = collector.addConfigurationElement.calls.mostRecent().args;
			expect(args[0]).toBe(basename)
			expect(args[1]).toBeCloseTo(val);
	
			val = 5000;
			testFunc(val);
			var args = collector.addConfigurationElement.calls.mostRecent().args;
			expect(args[0]).toBe(basename)
			expect(args[1]).toBeCloseTo(val);
			
		});
	});
	
	describe("function makeLongProperty", function() {
		
		var testFunc;
		var mustBeALong = 'must be a long';
		
		beforeEach(function() {
			testFunc = module.exports(base).makeLongProperty(name);
		});
		
		it("rejects string values", function() {
			testFunc("not an int");
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeALong);
		});
		
		it("rejects boolean values", function() {
			testFunc(true);
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeALong);
		});
		
		it("rejects object values", function() {
			testFunc({});
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeALong);
		});
		
		it("rejects array values", function() {
			testFunc([]);
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeALong);
		});
		
		it("rejects null values", function() {
			testFunc(null);
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeALong);
		});
		
		it("accumulates validator failures", function() {
			module.exports(base).makeLongProperty(name, failingValidator)(1);
			expect(collector.addConfigurationElement).not.toHaveBeenCalled();
			expect(collector.accumulateError).toHaveBeenCalledWith(name, 'error');
		});
		
		it("calls a passing validator function", function() {
			var val = 123428892398;
			module.exports(base).makeLongProperty(name, passingValidator)(val);
			var args = collector.addConfigurationElement.calls.mostRecent().args;
			expect(args[0]).toBe(basename)
			expect(args[1]).toBeCloseTo(val);
		});
		
		it("sets long values to the collector", function() {
			var testFunc = module.exports(base).makeLongProperty(name);
			
			var val = 1;
			testFunc(val);
			var args = collector.addConfigurationElement.calls.mostRecent().args;
			expect(args[0]).toBe(basename)
			expect(args[1]).toBeCloseTo(val);
	
			val = 5000;
			testFunc(val);
			var args = collector.addConfigurationElement.calls.mostRecent().args;
			expect(args[0]).toBe(basename)
			expect(args[1]).toBeCloseTo(val);
			
		});
	});
	
	describe("function makeStringProperty", function() {

		var testFunc;
		var mustBeAString = 'must be a string';
		
		beforeEach(function() {
			testFunc = module.exports(base).makeStringProperty(name);
		});
		
		it("rejects numeric values", function() {
			testFunc(1);
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeAString);
		});
		
		it("rejects boolean values", function() {
			testFunc(true);
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeAString);
		});
		
		it("rejects object values", function() {
			testFunc({});
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeAString);
		});
		
		it("rejects array values", function() {
			testFunc([]);
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeAString);
		});
		
		it("rejects null values", function() {
			testFunc(null);
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeAString);
		});
		
		it("accumulates validator failures", function() {
			module.exports(base).makeStringProperty(name, failingValidator)("1");
			expect(collector.addConfigurationElement).not.toHaveBeenCalled();
			expect(collector.accumulateError).toHaveBeenCalledWith(name, 'error');
		});
		
		it("calls a passing validator function", function() {
			var val = "1";
			module.exports(base).makeStringProperty(name, passingValidator)(val);
			var args = collector.addConfigurationElement.calls.mostRecent().args;
			expect(args[0]).toBe(basename)
			expect(args[1]).toBe(valueOf(val));
		});
		
		it("sets string values to the collector", function() {
			
			var val = "1";
			testFunc(val);
			var args = collector.addConfigurationElement.calls.mostRecent().args;
			expect(args[0]).toBe(basename)
			expect(args[1]).toBe(valueOf(val));
	
			val = "5000";
			testFunc(val);
			var args = collector.addConfigurationElement.calls.mostRecent().args;
			expect(args[0]).toBe(basename)
			expect(args[1]).toBe(valueOf(val));
			
		});
	});
});