/**
 * Provides a function that produces REST services objects
 * 
 * 
 * var service = require('jj/rest-service-factory')({
 *   baseUri: 'http://localhost:8080', // this would come from some configuration - TODO provide that
 *   operations: {
 *     find: {
 *       uri: '/find/:thing',
 *       methods: ['get','post']
 *     }
 *   }
 * });
 * 
 * service.find({thing: 'value'}); // makes a GET to http://localhost:8080/find/value
 */
require('globalize')('jj/rest-service-constants', this);
var print = require('jj/print');
var parameterRegex = /:([\w]+)/g;
var http = Packages.io.netty.handler.codec.http;

function mergeObject(base, merge) {
	Object.keys(merge).forEach(function(key) {
		base[key] = merge[key];
	});
	return base;
}

function makeUri(baseUri, operationUri, parameters) {
	var uri = baseUri.charAt(baseUri.length - 1) == '/' ? baseUri.substring(0, baseUri.length - 1) : baseUri;
	uri += operationUri.charAt(0) == '/' ? operationUri : '/' + operationUri;
	var remainingParams = parameters || {};
	var unresolvedParams = [];
	uri = uri.replace(parameterRegex, function(match, paramName) {
		if (paramName in remainingParams) {
			var result = remainingParams[paramName];
			delete remainingParams[paramName];
			return java.net.URLEncoder.encode(result, 'utf-8').replace('+', '%20');
		} else {
			unresolvedParams.push(paramName);
			return match;
		}
	});
	
	if (unresolvedParams.length) {
		throw new Error(uri + " has unresolved params: " + unresolvedParams.join(', '));
	}
	return [uri, remainingParams];
}

function makeUriNoBody(baseUri, operationUri, parameters) {
	
	let [uri, remainingParams] = makeUri(baseUri, operationUri, parameters);
	var qse = new http.QueryStringEncoder(uri);
	
	Object.keys(remainingParams).forEach(function(name) {
		qse.addParam(name, remainingParams[name]);
	});
	
	return qse.toString();
}

// the arguments array, from the original function
function unpackArguments(args) {
	var parameters, headers, body, callback;
	switch (args.length) {
	case 4:
		parameters = args[0];
		headers    = args[1];
		body       = args[2];
		callback   = args[3];
		
		// type check! - presumably, the body can be understood by something downstream
		// i think.  not totally sure how that will work yet
		if (typeof parameters !== 'object' ||
			typeof headers !== 'object' ||
			typeof callback !== 'function'
		) {
			throw new Error("unrecognized parameters");
		}
		
		break;
		
	case 3:
		
		
	case 2:
		// first argument MUST be parameters in this case?
		// 
		if (typeof args[0] === 'object') {
			parameters = args[0];
		} else {
			throw new Error('unrecognized parameters');
		}
		
		
		break;
		
	case 1:
		// either a parameters object or a callback function.  or a body?
		// fuck. need to think this through again
		if (typeof args[0] === 'object') {
			parameters = args[0];
		} else if (typeof args[0] === 'function') {
			callback = args[0];
		} else {
			throw new Error("unrecognized parameters");
		}
		
		break;
		
	case 0:
		// don't want to error out in this case, but nothing to do here
		break;
		
	default:
		throw new Error("unrecognized parameters");
	}
	
	return [parameters, headers, body, callback];
}

function makeServiceCallFunction(baseUri, operation) {
	return function(/* parameters, headers, body, callback */) {
		// normalize the parameters
		let [parameters, headers, body, callback] = unpackArguments(arguments);
		// for now
		var uri = (operation.allowBody) ?
			makeUriNoBody(baseUri, operation.uri, parameters) :
			makeUriNoBody(baseUri, operation.uri, parameters);
		
		var result = operation.method + ' ' + uri ;
		if (typeof callback === 'function') {
			result += ' with callback\n';
		}
		
		result += ('accept: ' + operation.accept + '\n');
		result += ('produce: ' + operation.produce + '\n');
		
		return result;
	}
}

// operations methods accept up to four parameters, although none are required
// an object of name-value pairs to use as parameters is the first parameter.  it is optional
// an object of name-value pairs to use as headers is the second parameter.  it is optional.
// - if the headers parameter is supplied, then the parameters object must be supplied for
// - disambiguation
// a object to use as a body, if the method requires one
// an optional function as a callback.  if the callback is supplied, the call executes
// "pov-asynchronously" otherwise it is "pov-synchronous"
// this only refers to the point of view of the consuming author.  in fact, all http client
// activity is asynchronous

var methods = {
	GET: {
		defaults: {
			accept: JSON,
			produce: FORM,
			allowBody: false
		}
	},
}

module.exports = function(config) {
	var baseUri = config.baseUri || '';
	var result = {};
	Object.keys(config.operations || {}).forEach(function(operation) {
		var operationConfig = config.operations[operation];
		var method = methods[(operationConfig.method || GET)];
		if (method == null) {
			throw new Error('method not recognized: ' + operationConfig.method);
		}
		var actualConfig = mergeObject(mergeObject({}, method.defaults), operationConfig);
		result[operation] = makeServiceCallFunction(baseUri, actualConfig);
	});
	return result;
};