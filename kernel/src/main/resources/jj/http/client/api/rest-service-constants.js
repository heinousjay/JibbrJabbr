// useful constants for defining services
// best way to use it:
// require('api/globalize')('rest-service-constants', this);

module.exports = {
	GET: 'GET',
	POST: 'POST',
	PUT: 'PUT',
	DELETE: 'DELETE',
	JSON: 'application/json',
	FORM: 'application/x-www-form-urlencoded',
	XML: 'text/xml',
	HTML: 'text/html',
	TEXT: 'text/plain'
}

Object.keys(module.exports).forEach(function(key) {
	module.exports[key.toLowerCase()] = module.exports[key];
});

// special casing the delete keyword
module.exports.del = module.exports.DELETE;