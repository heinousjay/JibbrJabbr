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
require('jj/globalize')('jj/rest-service-constants', this);
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
	
	return new String(qse.toString()); // have to force the conversion? UGLY
}

function makeServiceCallFunction(baseUri, operation) {
	return function(args) {
		
		
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

// operations methods accept up to two parameters, both optional
// the first parameter is an object with the following keys
//   params - an object of name-value pairs, or a function that returns same.  the values can be a scalar or an array, and will be stringified
//   headers - an object of name-value pairs, or a function that returns same. the values can be a scalar or an array, and will be stringified
//   body - a string used as the body of the request, or a function that returns something that will be stringified 
// the second parameter is a function used as a callback.  if present, the service is executed "pov-asynchronously" and the
// callback is notified upon completion. the callback will be called with a single parameter, which is an object to be defined that
// will include something.. i don't know what!
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