
var env = inject('jj.script.CurrentScriptEnvironment');

module.exports = function broadcast(func) {
	
	if (typeof func !== 'function') { throw new Error('broadcast requires a function argument'); }
	
	var host = env.current();
	
	if (host == null || !('startBroadcasting' in host)) { 
		throw new Error('cannot broadcast from this context, there is no web socket connection host'); 
	}
	
	host.startBroadcasting();

	try {
		while(host.nextConnection()) func();
	} finally {
		host.endBroadcasting();
	}
}