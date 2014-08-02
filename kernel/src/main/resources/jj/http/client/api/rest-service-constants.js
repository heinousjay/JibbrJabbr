// useful constants for defining services
// best way to use it:
// require('jj/globalize')('rest-service-constants', this);

var result = {
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

Object.keys(result).forEach(function(key) {
	result[key.toLowerCase()] = result[key];
});

// special casing the delete keyword
result.del = result.DELETE;

Object.freeze(result);

module.exports = result;