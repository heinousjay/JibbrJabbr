// useful constants for defining services
// best way to use it:
// require('globalize')('rest-service-constants', this);

module.exports = {
	GET: 'get',
	POST: 'post',
	PUT: 'put',
	DELETE: 'del',
	JSON: 'application/json',
	XML: 'text/xml',
	HTML: 'text/html',
	TEXT: 'text/plain'
}

Object.keys(module.exports).forEach(function(key) {
	module.exports[key.toLowerCase()] = module.exports[key];
});

// special casing the delete keyword
module.exports.del = module.exports.DELETE;