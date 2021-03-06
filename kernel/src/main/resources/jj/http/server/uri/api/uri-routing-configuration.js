
var validator = inject('jj.http.server.uri.RouteUriValidator');
var resourceNames = inject('jj.http.server.ServableResources').arrayOfNames();
var support = require('jj/configuration-support')('jj.http.server.uri.RouterConfiguration');

var GET    = Packages.io.netty.handler.codec.http.HttpMethod.GET;
var POST   = Packages.io.netty.handler.codec.http.HttpMethod.POST;
var PUT    = Packages.io.netty.handler.codec.http.HttpMethod.PUT;
var DELETE = Packages.io.netty.handler.codec.http.HttpMethod.DELETE;
var Route  = Packages.jj.http.server.uri.Route;

function makeSetter(method) {
	return function(uri) {
		
		var errors = validator.validateRouteUri(uri);
		
		if (errors != '') {
			support.accumulateError('routes', uri + " failed validation\n" + errors);
			return {
				to: function() {}
			}
			
		} else {

			var to = {};
	
			resourceNames.forEach(function(resourceName) {
				to[resourceName] = function(mappedName) {
					var route = new Route(method, uri, resourceName, mappedName || '');
					support.addToList('routes', route);
				}
			});
			
			return {
				to: to
			}
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
	
	welcomeFile: support.makeStringProperty('welcomeFile')
}



