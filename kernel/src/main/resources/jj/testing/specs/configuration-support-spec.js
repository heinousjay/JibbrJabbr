var location, value;

var valueOf = java.lang.String.valueOf;
var collectorMock = {
	addConfigurationElement: function() {},
	addConfigurationMultiElement: function() {},
	addConfigurationMappedElement: function() {},
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
	
	var s;
	
	function failingValidator(n, arg) {
		s.accumulateError(n, "failingValidator");
		return true;
	}

	var passingValidator;
	
	beforeEach(function() {
		s = module.exports(base);
		spyOn(collectorMock, 'accumulateError');
		spyOn(collectorMock, 'addConfigurationElement');
		spyOn(collectorMock, 'addConfigurationMultiElement');
		spyOn(collectorMock, 'addConfigurationMappedElement');
		passingValidator = jasmine.createSpy('passingValidator');
	});
	
	describe("function makeBooleanProperty", function() {
		
		var testFunc;
		var mustBeABoolean = 'must be a boolean: at configuration-support-spec.js:';
		
		beforeEach(function() {
			testFunc = s.makeBooleanProperty(name);
		});
		
		it("rejects string values", function() {
			testFunc("not a boolean");
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeABoolean + '50');
		});
		
		it("rejects number values", function() {
			testFunc(1);
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeABoolean + '55');
		});
		
		it("rejects object values", function() {
			testFunc({});
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeABoolean + '60');
		});
		
		it("rejects array values", function() {
			testFunc([]);
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeABoolean + '65');
		});
		
		it("rejects null values", function() {
			testFunc(null);
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeABoolean + '70');
		});
		
		it("accumulates validator failures", function() {
			s.makeBooleanProperty(name, failingValidator)(true);
			expect(collector.addConfigurationElement).not.toHaveBeenCalled();
			expect(collector.accumulateError).toHaveBeenCalledWith(name, 'failingValidator: at configuration-support-spec.js:75');
		});
		
		it("calls a passing validator function", function() {
			s.makeBooleanProperty(name, passingValidator)(true);
			expect(passingValidator).toHaveBeenCalledWith(name, true);
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
		var mustBeAnInteger = 'must be an integer: at configuration-support-spec.js:';
		var isOutOfIntegerRange = 'is out of integer range: at configuration-support-spec.js:';
		
		beforeEach(function() {
			testFunc = s.makeIntProperty(name);
		});
		
		it("rejects string values", function() {
			testFunc("not an int");
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeAnInteger + '108');
		});
		
		it("rejects boolean values", function() {
			testFunc(true);
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeAnInteger + '113');
		});
		
		it("rejects object values", function() {
			testFunc({});
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeAnInteger + '118');
		});
		
		it("rejects array values", function() {
			testFunc([]);
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeAnInteger + '123');
		});
		
		it("rejects null values", function() {
			testFunc(null);
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeAnInteger + '128');
		});
		
		it("rejects too large values", function() {
			testFunc(java.lang.Integer.MAX_VALUE + 1);
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, isOutOfIntegerRange + '133');
		});
		
		it("rejects too small values", function() {
			testFunc(java.lang.Integer.MIN_VALUE - 1)
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, isOutOfIntegerRange + '138');
		});
		
		it("accumulates validator failures", function() {
			s.makeIntProperty(name, failingValidator)(1);
			expect(collector.addConfigurationElement).not.toHaveBeenCalled();
			expect(collector.accumulateError).toHaveBeenCalledWith(name, 'failingValidator: at configuration-support-spec.js:143');
		});
		
		it("calls a passing validator function", function() {
			var val = 1;
			s.makeIntProperty(name, passingValidator)(val);
			expect(passingValidator).toHaveBeenCalledWith(name, val);
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
		var mustBeALong = 'must be a long: at configuration-support-spec.js:';
		
		beforeEach(function() {
			testFunc = s.makeLongProperty(name);
		});
		
		it("rejects string values", function() {
			testFunc("not an int");
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeALong + '184');
		});
		
		it("rejects boolean values", function() {
			testFunc(true);
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeALong + '189');
		});
		
		it("rejects object values", function() {
			testFunc({});
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeALong + '194');
		});
		
		it("rejects array values", function() {
			testFunc([]);
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeALong + '199');
		});
		
		it("rejects null values", function() {
			testFunc(null);
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeALong + '204');
		});
		
		it("accumulates validator failures", function() {
			s.makeLongProperty(name, failingValidator)(1);
			expect(collector.addConfigurationElement).not.toHaveBeenCalled();
			expect(collector.accumulateError).toHaveBeenCalledWith(name, 'failingValidator: at configuration-support-spec.js:209');
		});
		
		it("calls a passing validator function", function() {
			var val = 123428892398;
			s.makeLongProperty(name, passingValidator)(val);
			expect(passingValidator).toHaveBeenCalledWith(name, val);
			var args = collector.addConfigurationElement.calls.mostRecent().args;
			expect(args[0]).toBe(basename)
			expect(args[1]).toBeCloseTo(val);
		});
		
		it("sets long values to the collector", function() {
			var testFunc = s.makeLongProperty(name);
			
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
		var mustBeAString = 'must be a string: at configuration-support-spec.js:';
		
		beforeEach(function() {
			testFunc = s.makeStringProperty(name);
		});
		
		it("rejects numeric values", function() {
			testFunc(1);
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeAString + '251');
		});
		
		it("rejects boolean values", function() {
			testFunc(true);
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeAString + '256');
		});
		
		it("rejects object values", function() {
			testFunc({});
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeAString + '261');
		});
		
		it("rejects array values", function() {
			testFunc([]);
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeAString + '266');
		});
		
		it("rejects null values", function() {
			testFunc(null);
			expect(collectorMock.accumulateError).toHaveBeenCalledWith(name, mustBeAString + '271');
		});
		
		it("accumulates validator failures", function() {
			s.makeStringProperty(name, failingValidator)("1");
			expect(collector.addConfigurationElement).not.toHaveBeenCalled();
			expect(collector.accumulateError).toHaveBeenCalledWith(name, 'failingValidator: at configuration-support-spec.js:276');
		});
		
		it("calls a passing validator function", function() {
			var val = "1";
			s.makeStringProperty(name, passingValidator)(val);
			expect(passingValidator).toHaveBeenCalledWith(name, val);
			var args = collector.addConfigurationElement.calls.mostRecent().args;
			expect(args[0]).toBe(basename);
			expect(args[1]).toBe(valueOf(val));
		});
		
		it("sets string values to the collector", function() {
			
			var val = "1";
			testFunc(val);
			var args = collector.addConfigurationElement.calls.mostRecent().args;
			expect(args[0]).toBe(basename);
			expect(args[1]).toBe(valueOf(val));
	
			val = "5000";
			testFunc(val);
			var args = collector.addConfigurationElement.calls.mostRecent().args;
			expect(args[0]).toBe(basename);
			expect(args[1]).toBe(valueOf(val));
			
		});
	});
	
	describe('function makeAddToList', function() {
		
		it("accumulates validator failures", function() {
			s.makeAddToList(name, failingValidator)("1");
			expect(collector.addConfigurationMultiElement).not.toHaveBeenCalled();
			expect(collector.accumulateError).toHaveBeenCalledWith(name, 'failingValidator: at configuration-support-spec.js:310');
		});
		
		it("calls a passing validator function", function() {
			var val = "1";
			s.makeAddToList(name, passingValidator)(val);
			expect(passingValidator).toHaveBeenCalledWith(name, val);
			var args = collector.addConfigurationMultiElement.calls.mostRecent().args;
			expect(args[0]).toBe(basename);
			expect(args[1]).toBe(val);
		});
		
		it("adds values to the collector", function() {
			
			var val = "1";
			s.makeAddToList(name, null)(val);
			var args = collector.addConfigurationMultiElement.calls.mostRecent().args;
			expect(args[0]).toBe(basename);
			expect(args[1]).toBe(val);
	
			val = "5000";
			s.makeAddToList(name, null)(val);
			var args = collector.addConfigurationMultiElement.calls.mostRecent().args;
			expect(args[0]).toBe(basename);
			expect(args[1]).toBe(val);
			
		});
	});
	
	describe('function makeAddToMap', function() {
		
		it("accumulates validator failures", function() {
			s.makeAddToMap(name, failingValidator)("1", "2");
			expect(collector.addConfigurationMappedElement).not.toHaveBeenCalled();
			expect(collector.accumulateError).toHaveBeenCalledWith(name, 'failingValidator: at configuration-support-spec.js:344');
		});
		
		it("calls a passing validator function", function() {
			var key = "key";
			var val = "1";
			s.makeAddToMap(name, passingValidator)(key, val);
			expect(passingValidator).toHaveBeenCalledWith(name, key, val);
			var args = collector.addConfigurationMappedElement.calls.mostRecent().args;
			expect(args[0]).toBe(basename);
			expect(args[1]).toBe(key);
			expect(args[2]).toBe(val);
		});
		
		it("adds values to the collector", function() {
			
			var key = "key";
			var val = "1";
			s.makeAddToMap(name, null)(key, val);
			var args = collector.addConfigurationMappedElement.calls.mostRecent().args;
			expect(args[0]).toBe(basename);
			expect(args[1]).toBe(key);
			expect(args[2]).toBe(val);
	
			val = "5000";
			s.makeAddToMap(name, null)(key, val);
			var args = collector.addConfigurationMappedElement.calls.mostRecent().args;
			expect(args[0]).toBe(basename);
			expect(args[1]).toBe(key);
			expect(args[2]).toBe(val);
			
		});
	});
	
	describe('function accumulateError', function() {
		
		it('adds the correct stack line', function() {
			// pass in the depth explicitly
			s.accumulateError('name', 'error', 1);
			expect(collector.accumulateError).toHaveBeenCalledWith(name, 'error: at configuration-support-spec.js:384');
		});
	});
});