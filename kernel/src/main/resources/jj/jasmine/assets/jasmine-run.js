

(function() {
	
	var resultCollector = $$realInject('jj.jasmine.JasmineResultCollector');
	
	function JJApiReporter() {

		this.jasmineStarted = function() {
			resultCollector.jasmineStarted();
		};

		this.jasmineDone = function() {
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

	}
	
	$$env.addReporter(new JJApiReporter());
	$$env.execute();
})();
