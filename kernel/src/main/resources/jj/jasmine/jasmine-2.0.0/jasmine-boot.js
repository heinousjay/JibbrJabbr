// define the jasmine API
var jasmineRequire = require('jasmine'),
jasmine = jasmineRequire.core(jasmineRequire),
env = jasmine.getEnv(),
describe = function(description, specDefinitions) {
	return env.describe(description, specDefinitions);
},
xdescribe = function(description, specDefinitions) {
	return env.xdescribe(description, specDefinitions);
},
it = function(desc, func) {
	return env.it(desc, func);
},
xit = function(desc, func) {
	return env.xit(desc, func);
},
beforeEach = function(beforeEachFunction) {
	return env.beforeEach(beforeEachFunction);
},
afterEach = function(afterEachFunction) {
	return env.afterEach(afterEachFunction);
},
expect = function(actual) {
	return env.expect(actual);
},
pending = function() {
	return env.pending();
},
spyOn = function(obj, methodName) {
	return env.spyOn(obj, methodName);
};

/**
 * Expose the interface for adding custom equality testers.
 */
jasmine.addCustomEqualityTester = function(tester) {
  env.addCustomEqualityTester(tester);
};

/**
 * Expose the interface for adding custom expectation matchers
 */
jasmine.addMatchers = function(matchers) {
  return env.addMatchers(matchers);
};

/**
 * Expose the mock interface for the JavaScript timeout functions
 */
jasmine.clock = function() {
  return env.clock;
};


describe('selecting from the DOM', function() {
	
	it('can select elements', function() {
		var body = null;
		expect(body).not.toBeNull();
		
		throw new TypeError("eff yeah");
	});
	
	it('can confuse me', function() {
		expect(2 + 2).toBe(5);
	});
	
	describe('passing tests are nice, though', function() {
		
		it('can make you feel so good', function() {
			expect(true).toBe(true);
		});
	});
});

describe('this shit is a pain in the ass', function() {
	
	it('is not really doing what i expected', function() {
		expect(1).toBe(1);
	});
});


