// The config file sits in the root of the app tree.
// if it is not present then the system uses a default configuration instead

function configure() {
	
	return {
		// configures the socket(s) used
		// for the http server
		httpServerSocket: function(socket) {
			
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
				
			// 
				.bind(8080);
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
		}
	}
}
