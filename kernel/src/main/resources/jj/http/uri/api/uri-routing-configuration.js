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

function route(destination) {
	return destination;
}

function redirect(destination) {
	return destination;
}

function makeSetter(method, type) {
	return function(uri) {
		
		var errors = validator.validateRouteUri(uri);
		
		if (errors != '') {
			throw new Error(uri + " failed validation\n" + errors);
		}
		
		return {
			to: function(destination) {
				var route = new Route(method, uri, type(destination));
				collector.addConfigurationMultiElement(base + 'routes', route);
			},
			to404: function() {
				var route = new Route(method, uri);
			}
		}
	}
}

module.exports = {
		
	route: {
		get:    makeSetter(GET,    route),
		GET:    makeSetter(GET,    route),
		post:   makeSetter(POST,   route),
		POST:   makeSetter(POST,   route),
		put:    makeSetter(PUT,    route),
		PUT:    makeSetter(PUT,    route),
		del:    makeSetter(DELETE, route),
		DELETE: makeSetter(DELETE, route)
	},
	
	welcomeFile: support.makeStringProperty(base, 'welcomeFile')
}

srh.arrayOfNames().forEach(function(resourceName) {
	module.exports[resourceName] = function(mappedName) {
		return {
			resourceName: resourceName,
			mappedName: mappedName
		}
	}
});