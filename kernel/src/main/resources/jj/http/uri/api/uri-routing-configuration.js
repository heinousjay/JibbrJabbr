var base = 'jj.http.uri.RouterConfiguration.';
var collector = inject('jj.configuration.ConfigurationCollector');
var validator = inject('jj.http.uri.RouteUriValidator');
var srh = inject('jj.http.uri.ServableResourceHelper');
var support = require('jj/configuration-support');

var GET    = Packages.io.netty.handler.codec.http.HttpMethod.GET;
var POST   = Packages.io.netty.handler.codec.http.HttpMethod.POST;
var PUT    = Packages.io.netty.handler.codec.http.HttpMethod.PUT;
var DELETE = Packages.io.netty.handler.codec.http.HttpMethod.DELETE;
var Route  = Packages.jj.http.uri.Route;

function makeSetter(method) {
	return function(uri) {
		
		var errors = validator.validateRouteUri(uri);
		
		if (errors != '') {
			throw new Error(uri + " failed validation\n" + errors);
		}

		var to = {};

		srh.arrayOfNames().forEach(function(resourceName) {
			to[resourceName] = function(mappedName) {
				var route = new Route(method, uri, resourceName, mappedName || '');
				collector.addConfigurationMultiElement(base + 'routes', route);
			}
		});
		
		return {
			to: to
		}
	}
}

module.exports = {
		
	route: {
		get:    makeSetter(GET),
		GET:    makeSetter(GET),
		post:   makeSetter(POST),
		POST:   makeSetter(POST),
		put:    makeSetter(PUT),
		PUT:    makeSetter(PUT),
		del:    makeSetter(DELETE),
		DELETE: makeSetter(DELETE)
	},
	
	welcomeFile: support.makeStringProperty(base, 'welcomeFile')
}



