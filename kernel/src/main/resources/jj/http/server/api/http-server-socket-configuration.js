
var support = require('jj/configuration-support');
var base = 'jj.http.server.HttpServerSocketConfiguration.';
var collector = inject('jj.configuration.ConfigurationCollector');

module.exports = {
	
	keepAlive: support.makeBooleanProperty(base, 'keepAlive'),
	tcpNoDelay: support.makeBooleanProperty(base, 'tcpNoDelay'),
	reuseAddress: support.makeBooleanProperty(base, 'reuseAddress'),
	backlog: support.makeIntProperty(base, 'backlog'),
	timeout: support.makeIntProperty(base, 'timeout'),
	sendBufferSize: support.makeIntProperty(base, 'sendBufferSize'),
	receiveBufferSize: support.makeIntProperty(base, 'receiveBufferSize'),
	bind: function(host, port) {
		var binding = null;
		if (typeof host == 'string' && typeof port == 'number') {
			binding = new Packages.jj.http.server.Binding(host, new java.lang.Integer(parseInt(port)).intValue());
		} else if (typeof host == 'number' && typeof port == 'undefined') {
			binding = new Packages.jj.http.server.Binding(host);
		} else {
			throw new TypeError('bind requires an optional host name as a string, and a mandatory integer port');
		}
		collector.addConfigurationMultiElement(base + "bindings", binding);
		return this;
	}
}