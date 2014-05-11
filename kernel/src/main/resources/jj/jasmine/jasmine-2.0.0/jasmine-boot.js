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


(function() {
	
	function print(thing) {
		java.lang.System.out.print(thing);
	}
	
	jasmine.testJsApiReporter = (function() {

		var noopTimer = {
			start : function() {
			},
			elapsed : function() {
				return 0;
			}
		};

		function JsApiReporter(options) {
			var timer = options.timer || noopTimer, status = "loaded";
			
			this.started = false;
			this.finished = false;

			this.jasmineStarted = function() {
				this.started = true;
				status = 'started';
				timer.start();
			};

			var executionTime;

			this.jasmineDone = function() {
				this.finished = true;
				executionTime = timer.elapsed();
				status = 'done';
			};

			this.status = function() {
				return status;
			};

			var suites = {};
			var currentSuite;
			this.suiteStarted = function(result) {
				print(result.description + '\n');
				storeSuite(result);
				if (currentSuite) result.parentId = currentSuite.id;
				currentSuite = result;
			};

			this.suiteDone = function(result) {
				print('done with ' + result.description + '\n');
				storeSuite(result);
				currentSuite = result.parentId ? suites[result.parentId] : null;
			};

			function storeSuite(result) {
				suites[result.id] = result;
			}

			this.suites = function() {
				return suites;
			};

			var specs = [];
			this.specStarted = function(result) {
				print("-" + result.description + '(' + currentSuite.id + ')');
				result.suiteId = currentSuite.id;
			};

			this.specDone = function(result) {
				print(" --> " + result.status + "\n");
				specs.push(result);
			};

			this.specResults = function(index, length) {
				return specs.slice(index, index + length);
			};

			this.specs = function() {
				return specs;
			};

			this.executionTime = function() {
				return executionTime;
			};

		}

		return JsApiReporter;
	}());
	
	
	var reporter = new jasmine.JsApiReporter({
      timer: new jasmine.Timer()
    });
	//env.addReporter(reporter);
	
	/*
	jasmineRequire.ConsoleReporter = require('console').ConsoleReporter;
	require('console').console(jasmineRequire, jasmine);
	env.addReporter(new jasmine.ConsoleReporter({
		print: print
	}));
	*/
	
	env.addReporter(new jasmine.testJsApiReporter({
      timer: new jasmine.Timer()
    }));
	env.execute();
	
	/*
	
	{
		"suite1": {
			"id" : "suite1",
			"status":"",
			"description":"selecting from the DOM",
			"fullName":"selecting from the DOM"
		},
		"suite2": {
			"id":"suite2",
			"status":"",
			"description":"passing tests are nice, though",
			"fullName":"selecting from the DOM passing tests are nice, though"
		},
		"suite3": {
			"id":"suite3",
			"status":"",
			"description":"this shit is a pain in the ass",
			"fullName":"this shit is a pain in the ass"
		}
	}
	
	[
		{
			"id":"spec1",
			"description":"can confuse me",
			"fullName":"selecting from the DOM can confuse me",
			"failedExpectations" : [
				{
					"matcherName":"toBe",
					"expected":5,
					"actual":4,
					"message":"Expected 4 to be 5.",
					"passed":false
				}
			],
			"status":"failed"
		},
		{
			"id":"spec2",
			"description":"can make you feel so good",
			"fullName":"selecting from the DOM passing tests are nice, though can make you feel so good",
			"failedExpectations":[],
			"status":"passed"
		},
		{
			"id":"spec3",
			"description":"is not really doing what i expected",
			"fullName":"this shit is a pain in the ass is not really doing what i expected",
			"failedExpectations":[],
			"status":"passed"
		}
	]
	 */

	//["started","finished","jasmineStarted","jasmineDone","status","suiteStarted","suiteDone","suites","specStarted","specDone","specResults","specs","executionTime"]
	var suites = reporter.suites();
	var specs = reporter.specs();
	
/*
	function print(indent, suite) {
		java.lang.System.out.println(indent + suite.name);
		if (suite.type === 'spec') {

			//java.lang.System.out.println(indent + " - " + results[suite.id].result);
			//java.lang.System.out.println(indent + " - " + results[suite.id].messages.join("\n"));
		}
		
		suite.children.forEach(function(child) {
			print(indent + '  ', child);
		});
	}

	Object.keys(suites).forEach(function(key) {
		print('', suites[key]);
		
	});
*/
})();
