
// conceptually, all of this is defined globally


const defaultProducers = {
	GET: null,
	PUT: JSON,
	POST: JSON,
	DELETE, null
};

const GET = 'GET';
const PUT = 'PUT';
const POST = 'POST';
const DELETE  'DELETE';

const JSON = 'JSON';

// the configuration options are merged into a default options set
// this is more conceptual than actual
const defaultOptions = {
	// the path part that will be appended to the service base url.
	// can define parameter substitutions, using {} delimited
	// parameter names
	path: '',
	// one of the constants above
	method: GET,
	// JSON is really all we do right now
	accept: JSON,
	// JSON is really all we do right now
	// "this" doesn't really work here like we would want
	// but the idea makes sense
	produce: defaultProducers[this.method],
	// operations can have parameters bound by default. any
	// parameters passed in at the point of call will be merged
	// into this object
	params: {},
	// if you don't care about the response, you can mark
	// ignoreResult:true and the system won't pause the script
	// to wait for a response, it will fire and forget
	ignoreResult: false
};


// RestService can be called as a constructor or not,
// takes a single parameter, a configuration object
// parameter substitutions work in all parts of the final URL
var lightService = new RestService({
	// the base URL of the service.  can be bound as a parameter
	// and pull from configuration? somehow.  that's a detail
	// that hasn't been worked out
	baseUrl: "http://192.168.1.12/api/{key}",
	// this object defines the operations that will exist on the service
	operations : {
		// status just uses the default settings, so passing an empty
		// object or true is enough to create the operation
		status: true, 
		flashAll: {
			path: '/groups/0/action',
			method: PUT,
			params: {
				alert: 'select'
			},
			ignoreResult: true
		}
	}
});

// this can happen later to provide default parameter values
lightService.bind({
	key: 'jaystestname'
});



// lightService now has methods defined for everything you configured.  the methods take
// a param object
// param objects are processed to pull any parameter substitutions in the defined URL
// first. as keys are matched, they are deleted. whatever object remains is then treated
// appropriately for the method - serialized as a request body or turned into a query string
var status = lightService.status();
// the function calls respond like normal, using the continuation system behind the scenes
// so if we get here, status is now the deserialized response from a GET to
// http://192.168.1.12/api/jaystestname

// error handling is not shown - it's pretty simple, these methods throw exceptions
// so use try-catch.  details to come

// the result to this is ignored, so it will
// return undefined and not pause the script.
lightService.flashAll({
	key: 'jaystestname'
});