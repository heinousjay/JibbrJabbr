
var support = require('jj/configuration-support')('jj.http.server.HttpServerSocketConfiguration');

module.exports = {
	
	keepAlive: support.makeBooleanProperty('keepAlive'),
	tcpNoDelay: support.makeBooleanProperty('tcpNoDelay'),
	reuseAddress: support.makeBooleanProperty('reuseAddress'),
	backlog: support.makeIntProperty('backlog'),
	timeout: support.makeIntProperty('timeout'),
	sendBufferSize: support.makeIntProperty('sendBufferSize'),
	receiveBufferSize: support.makeIntProperty('receiveBufferSize'),
	bind: function(host, port) {
		var binding = null;
		if (typeof host == 'string' && typeof port == 'number') {
			binding = new Packages.jj.http.server.Binding(host, new java.lang.Integer(parseInt(port)).intValue());
		} else if (typeof host == 'number' && typeof port == 'undefined') {
			binding = new Packages.jj.http.server.Binding(new java.lang.Integer(parseInt(host)).intValue());
		} else {
			throw new TypeError('bind requires an optional host name as a string, and a mandatory integer port');
		}
		support.addToList('bindings', binding);
		return this;
	}
}