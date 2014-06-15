
//var support = require('configuration-support');
var base = 'jj.uri.RouterConfiguration.';
var collector = inject('jj.configuration.ConfigurationCollector');

var GET    = Packages.io.netty.handler.codec.http.HttpMethod.GET;
var POST   = Packages.io.netty.handler.codec.http.HttpMethod.POST;
var PUT    = Packages.io.netty.handler.codec.http.HttpMethod.PUT;
var DELETE = Packages.io.netty.handler.codec.http.HttpMethod.DELETE;

function route(destination) {
	return java.net.URI.create(destination);
}

function redirect(destination) {
	return route(destination);
}

function makeSetter(method, type) {
	return function(uri) {
		return {
			to: function(destination) {
				var route = new Packages.jj.uri.Route(method, java.net.URI.create(uri), type(destination));
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
	redirect: {
		get: makeSetter(GET, redirect),
		GET: makeSetter(GET, redirect),
		post: makeSetter(POST, redirect),
		POST: makeSetter(POST, redirect),
		put: makeSetter(PUT, redirect),
		PUT: makeSetter(PUT, redirect),
		del: makeSetter(DELETE, redirect),
		DELETE: makeSetter(DELETE, redirect)
	}
}
