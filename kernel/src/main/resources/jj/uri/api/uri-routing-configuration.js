
//var support = require('configuration-support');
var base = 'jj.uri.RouterConfiguration.';
var collector = inject('jj.configuration.ConfigurationCollector');
var support = require('configuration-support');

var GET    = Packages.io.netty.handler.codec.http.HttpMethod.GET;
var POST   = Packages.io.netty.handler.codec.http.HttpMethod.POST;
var PUT    = Packages.io.netty.handler.codec.http.HttpMethod.PUT;
var DELETE = Packages.io.netty.handler.codec.http.HttpMethod.DELETE;

function route(destination) {
	return destination;
}

function redirect(destination) {
	return destination;
}

function makeSetter(method, type) {
	return function(uri) {
		return {
			to: function(destination) {
				var route = new Packages.jj.uri.Route(method, uri, type(destination));
				collector.addConfigurationMultiElement(base + 'routes', route);
			},
			to404: function() {
				var route = new Packages.jj.uri.Route(method, java.net.URI.create(uri));
			}
		}
	}
}

module.exports = {
		
	route: {
		get: makeSetter(GET, route),
		GET: makeSetter(GET, route),
		post: makeSetter(POST, route),
		POST: makeSetter(POST, route),
		put: makeSetter(PUT, route),
		PUT: makeSetter(PUT, route),
		del: makeSetter(DELETE, route),
		DELETE: makeSetter(DELETE, route)
	},
	
	welcomeFile: support.makeStringProperty(base, 'welcomeFile')
}
