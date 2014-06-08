
var support = require('configuration-support');
var base = 'jj.http.server.HttpServerSocketConfiguration.';
	
module.exports = {
	
	keepAlive: support.makeBooleanProperty(base, 'keepAlive'),
	tcpNoDelay: support.makeBooleanProperty(base, 'tcpNoDelay'),
	reuseAddress: support.makeBooleanProperty(base, 'reuseAddress'),
	backlog: support.makeIntProperty(base, 'backlog'),
	timeout: support.makeIntProperty(base, 'timeout'),
	sendBufferSize: support.makeIntProperty(base, 'sendBufferSize'),
	receiveBufferSize: support.makeIntProperty(base, 'receiveBufferSize'),
	bind: function(host, port) {
		
		return this;
	}
}