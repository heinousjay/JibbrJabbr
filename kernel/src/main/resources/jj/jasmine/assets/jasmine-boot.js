// define the jasmine API
var $$jasmineRequire = module.exports,
jasmine = $$jasmineRequire.core($$jasmineRequire),
$$env = jasmine.getEnv(),
describe = function(description, specDefinitions) {
	return $$env.describe(description, specDefinitions);
},
xdescribe = function(description, specDefinitions) {
	return $$env.xdescribe(description, specDefinitions);
},
it = function(desc, func) {
	return $$env.it(desc, func);
},
xit = function(desc, func) {
	return $$env.xit(desc, func);
},
beforeEach = function(beforeEachFunction) {
	return $$env.beforeEach(beforeEachFunction);
},
afterEach = function(afterEachFunction) {
	return $$env.afterEach(afterEachFunction);
},
expect = function(actual) {
	return $$env.expect(actual);
},
pending = function() {
	return $$env.pending();
},
spyOn = function(obj, methodName) {
	return $$env.spyOn(obj, methodName);
};

/**
 * Expose the interface for adding custom equality testers.
 */
jasmine.addCustomEqualityTester = function(tester) {
  $$env.addCustomEqualityTester(tester);
};

/**
 * Expose the interface for adding custom expectation matchers
 */
jasmine.addMatchers = function(matchers) {
  return $$env.addMatchers(matchers);
};

/**
 * Expose the mock interface for the JavaScript timeout functions
 */
jasmine.clock = function() {
  return $$env.clock;
};
