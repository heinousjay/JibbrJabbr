/**
 * Provides a function that produces REST services objects
 * 
 * 
 * var service = require('rest-service-factory')({
 *   baseUri: 'http://localhost:8080', // this would come from some configuration - TODO provide that
 *   operations: {
 *     find: {
 *       uri: '/find/:thing',
 *       methods: ['get','post'],
 *       produce: 
 *     }
 *   }
 * });
 * 
 * service.find({thing: 'value'}); // makes a GET to http://localhost:8080/find/value
 */
require('globalize')('rest-service-constants', this);
var print = require('print');
var parameterRegex = /:([\w]+)/g;

function mergeObject(base, merge) {
	Object.keys(merge).forEach(function(key) {
		base[key] = merge[key];
	});
	return base;
}

function makeUri(baseUri, operationUri, parameters) {
	var uri = baseUri.charAt(baseUri.length - 1) == '/' ? baseUri.substring(0, baseUri.length - 1) : baseUri;
	uri += operationUri.charAt(0) == '/' ? operationUri : '/' + operationUri;
	var myParams = parameters || {};
	var unresolvedParams = [];
	uri = uri.replace(parameterRegex, function(match, paramName) {
		if (paramName in myParams) {
			var result = myParams[paramName];
			delete myParams[paramName];
			return result;
		} else {
			unresolvedParams.push(paramName);
			return match;
		}
	});
	
	if (unresolvedParams.length) {
		throw new Error(uri + " has unresolved params: " + unresolvedParams.join(', '));
	}
	return [uri, myParams];
}

var methods = {
	get: {
		api: function(baseUri, operation) {
			return function(params, callback) {
				// this one is weird - if you destructure a result with the same internal
				// name as one of your containing function parameters, the function parameter
				// will always be undefined. WTF
				let [uri, remainingParams] = makeUri(baseUri, operation.uri, params);
				var result = '';
				if (typeof callback == 'function') {
					
					result += ('get ' + uri + ' with callback!\n');
				} else {
					result += ('get ' + uri + ' without callback!\n');
				}
				result += ('accept: ' + operation.accept + '\n');
				result += ('produce: ' + operation.produce + '\n');
				
				if (operation.allowBody) {
					// whatever?
				} else {
					if (Object.keys(remainingParams).length > 0) {
						throw new Error('get request without body has additional parameters: ' + Object.keys(remainingParams).join(', '));
					}
				}
				return result;
			}
		},
		defaults: {
			accept: JSON,
			produce: JSON,
			allowBody: false
		}
	},
	
}

module.exports = function(config) {
	var baseUri = config.baseUri || '';
	var result = {};
	Object.keys(config.operations || {}).forEach(function(operation) {
		var operationConfig = config.operations[operation];
		var method = methods[(operationConfig.method || GET).toLowerCase()];
		if (method == null) {
			throw new Error('method not recognized: ' + operationConfig.method);
		}
		var actualConfig = mergeObject(mergeObject({}, method.defaults), operationConfig);
		result[operation] = method.api(baseUri, actualConfig);
	});
	return result;
};