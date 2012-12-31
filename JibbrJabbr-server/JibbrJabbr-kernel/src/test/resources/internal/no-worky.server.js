var lightService = new RestService({
	// the base URL of the service.  can be bound as a parameter
	// and pull from configuration? somehow.  that's a detail
	// that hasn't been worked out
	baseUrl: "http://192.168.1.12/api/jaystestname",
	// this object defines the operations that will exist on the service
	operations : {
		// status just uses the default settings, so passing an empty
		// object or true is enough to create the operation
		status: true, 
		flashAll: {
			path: '/groups/0/action',
			method: 'PUT',
			params: {
				alert: 'select'
			},
			ignoreResult: true
		}
	}
});

lightService.flashAll();

$(function() {
	// nothing to do, nothing to do
});