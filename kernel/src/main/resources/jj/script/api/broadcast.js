
var env = inject('jj.script.CurrentScriptEnvironment');

module.exports = function broadcast(func) {
	
	// TODO - error module!
	if (typeof func !== 'function') { throw new Error('broadcast requires a function'); }
	
	var host = env.currentWebSocketConnectionHost();
	
	if (host == null) { throw new Error('cannot broadcast without a web socket connection host in context'); }
	
	host.startBroadcasting();

	try {
		while(host.nextConnection()) func();
	} finally {
		host.endBroadcasting();
	}
}