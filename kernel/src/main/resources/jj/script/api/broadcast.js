// THIS IS THE WRONG OBJECT!! but right for now. ugh.  actually in this context it's
// fine, since we are going to be inside a current script environment ALWAYS.
// interestingly, i wonder if the only other usage of that method is in the original broadcast function
var env = injectorBridge('jj.script.CurrentScriptEnvironment');

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