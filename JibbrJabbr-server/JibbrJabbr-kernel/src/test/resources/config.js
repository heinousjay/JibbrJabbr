/*
 * configures the application. everything should be inside a function
 * to be called by the system.
 * 
 * the function must return an object which maps subsystem names to 
 * functions that configure said subsystem.  The function will be called
 * as the subsystem starts
 */

function configure() {
	return {
		// configures the socket used
		// for the http server
		httpServerSocket: function(socket) {
			
			// these are the defaults
			
			// SO_KEEPALIVE
			socket.keepAlive(true)
			// TCP_NODELAY
				.tcpNoDelay(true)
			// SO_BACKLOG
				.backlog(1024)
			// SO_TIMEOUT
				.timeout(10000)
			// SO_REUSEADDR
				.reuseAddress(true)
			// SO_SNDBUF
				.sendBufferSize(65536)
			// SO_RCVBUF
				.receiveBufferSize(65536)
			
			// and bind it to interfaces and/or ports
			// if you're on a unix system, you can't
			// bind to port 80 unless you run the server
			// as root... which i don't recommend.  i do
			// recommend binding to a high port and
			// up nginx as a proxy
				.bind(8080)
				.bind('localhost', 8090);
		},
		
		// configures the application server 
		http: function(http) {
			
			http.redirect(GET, "/chat").to("/chat/");
			http.route(GET, "/chat/{room=lobby}").to("/chat/index");
		},
		
		document: function(doc) {
			doc.showParsingErrors(false)
				.removeComments(true)
				.clientDebug(false);
		},
		
		fails: function(fails) {
			fails.fail();
		},
		
		scriptTestInterface: function(config) {
			config.something(true);
		}
	}
}
