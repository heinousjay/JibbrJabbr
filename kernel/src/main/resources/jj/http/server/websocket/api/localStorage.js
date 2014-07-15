
// system dependencies
var connection = inject('jj.http.server.websocket.CurrentWebSocketConnection');
var env        = inject('jj.script.CurrentScriptEnvironment');
var JJMessage   = Packages.jj.jjmessage.JJMessage;

function assertValidContext() {
	if (connection.current() == null) {
		throw new Error('localStorage operations are not valid from this context, there is no connected client');
	}
}

// unfortunately, it's not possible to provide a browser-like API
// because of the necessity of the continuation.  
module.exports = {
	store: function(key, value) {
		assertValidContext();
		
		if (!(typeof key === 'string') || typeof value === 'undefined') {
			throw new Error('store requires a string key argument and a value argument of any stringifiable type');
		}
		
		connection.current().send(JJMessage.makeStore(key, JSON.stringify(value)));
		
		return value;
	},
	retrieve: function(key) {
		assertValidContext();
		
		if (!(typeof key === 'string')) {
			throw new Error('retrieve requires a string key argument');
		}
		
		// lesson learned! have to return the value from env.preparedContinuation
		// in order for it to properly continue
		return env.preparedContinuation(JJMessage.makeRetrieve(key));
	}
};