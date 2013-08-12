/*
 * configures the application. everything should be inside a function
 * named configure, which the system will call when it's ready to rock.
 * configure should return an object which maps subsystem names to 
 * functions that configure that subsystem.  The function will be called
 * as the subsystem starts
 */

function configure() {
	return {
		// configures the socket used
		// for the http server
		httpServerSocket: function(socket) {
			
			// these are the defaults
			
			// SO_KEEPALIVE
			socket.keepAlive(true);
			// TCP_NODELAY
			socket.tcpNoDelay(true);
			// SO_BACKLOG
			socket.backlog(1024);
			// SO_TIMEOUT
			socket.timeout(10000);
			// SO_REUSEADDR
			socket.reuseAddress(true);
			// SO_SNDBUF
			socket.sendBufferSize(65536);
			// SO_RCVBUF
			socket.receiveBufferSize(65536);
			
			// and bind it to interfaces and/or ports
			// if you're on a unix system, you can't
			// bind to port 80 unless you run the server
			// as root... which i don't recommend.  i do
			// recommend binding to a high port and
			// up nginx as a proxy
			socket.bind(8080);
			socket.bind("192.168.1.11", 8090);
		},
		
		// configures the application server 
		http: function(http) {
			http.redirect(GET, "/chat").to("/chat/");
			http.route(GET, "/chat/{room=lobby}").to("/chat/index");
		}
	}
}
