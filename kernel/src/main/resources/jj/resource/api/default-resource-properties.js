// provides a default set of resource configurations, if overriding is desired

var props = require('jj/resource-configurations');
var rp = require('jj/resource-properties');

Object.keys(props).forEach(function(ext) {
	rp.extension(ext).is(props[ext]);
});