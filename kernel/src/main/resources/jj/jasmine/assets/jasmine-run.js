

(function() {
	
	// ARG NO!
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
				print('** ' + result.description + ' beginning\n');
				storeSuite(result);
				if (currentSuite) result.parentId = currentSuite.id;
				currentSuite = result;
			};

			this.suiteDone = function(result) {
				print('** ' + result.description + ' completed\n');
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
				print("- " + result.description + '(' + currentSuite.id + ')');
				result.suiteId = currentSuite.id;
			};

			this.specDone = function(result) {
				print(" --> " + result.status + "\n");
				specs.push(result);
				if (result.status == 'failed') {
					result.failedExpectations.forEach(function(failedExpectation) {
						print('  ' + failedExpectation.message + '\n');
					});
				}
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
