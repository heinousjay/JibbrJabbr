
//var support = require('configuration-support');
var base = 'jj.uri.URIConfiguration.';
var collector = inject('jj.configuration.ConfigurationCollector');

var GET    = Packages.io.netty.handler.codec.http.HttpMethod.GET;
var POST   = Packages.io.netty.handler.codec.http.HttpMethod.POST;
var PUT    = Packages.io.netty.handler.codec.http.HttpMethod.PUT;
var DELETE = Packages.io.netty.handler.codec.http.HttpMethod.DELETE;

module.exports = {
		
	route: {
		get: function(uri) {
			return {
				to: function(destination) {
					var route = new Packages.jj.uri.Route(GET, java.net.URI.create(uri), java.net.URI.create(destination));
					collector.addConfigurationMultiElement(base + 'routes', route);
				}
			}
		}
	},
	redirect: {
		get: function() {
			// whatever
		}
	}
}

module.exports.route.GET = module.exports.route.get;