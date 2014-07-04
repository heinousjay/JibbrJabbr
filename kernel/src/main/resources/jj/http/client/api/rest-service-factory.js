/**
 * Provides a function that produces REST service 
 * 
 * 
 * var service = require('rest-service-factory')({
 *   baseUri: 'http://localhost:8080', // this would come from some configuration - TODO provide that
 *   operations: {
 *     find: {
 *       method: 'get', // not case sensitive
 *       uri: '/find/:thing', // appended to baseUri, : parameters are substituted
 *     }
 *   }
 * });
 * 
 * service.find({thing: 'value'}); // makes a GET to http://localhost:8080/find/value
 */
module.exports = function(config) {
	
};