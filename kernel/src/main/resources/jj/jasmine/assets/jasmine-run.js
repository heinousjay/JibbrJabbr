

(function() {
	
	var resultCollector = $$realInject('jj.jasmine.JasmineResultCollector');
	
	function JsApiReporter() {
		var timer = {
			start: function() {},
			elapsed: function() {}
		}

		var executionTime;

		this.jasmineStarted = function() {
			timer.start();
		};

		this.jasmineDone = function() {
			executionTime = timer.elapsed();
			resultCollector.jasmineDone();
		};

		this.suiteStarted = function(suite) {
			// id,status,description,fullName
			resultCollector.suiteStarted(suite.id, suite.description);
		};

		this.suiteDone = function(suite) {
			// id,status,description,fullName
			resultCollector.suiteDone(suite.id, suite.description);
		};
		
		this.specStarted = function(spec) {
			// id,description,fullName,failedExpectations
			resultCollector.specStarted(spec.id, spec.description);
		};

		// this does not get called for specs that don't pass.  wtf?
		this.specDone = function(spec) {
			// id,description,fullName,failedExpectations,status
			spec.failedExpectations.forEach(function(fe) {
				resultCollector.specExpectationFailed(spec.id, fe.message);
			});
			resultCollector.specDone(spec.id, spec.status);
			
		};

		this.executionTime = function() {
			return executionTime;
		};

	}
	
	env.addReporter(new JsApiReporter());
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
