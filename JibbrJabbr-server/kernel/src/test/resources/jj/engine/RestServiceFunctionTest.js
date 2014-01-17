(function() {
	
	var failed = false;
	try {
		new RestService();
	} catch (e) {
		failed = e;
	}
	if (!failed) fail("should have failed to create a RestService with no configuration object");
	
	failed = false;
	try {
		new RestService({});
	} catch (e) {
		failed = e;
	}
	if (!failed) fail("should have failed to create a RestService with an invalid configuration object");
	
	new RestService({
		baseUrl: '',
		operations: {
			hi: {
				
			}
		}
	});
	
	
})();