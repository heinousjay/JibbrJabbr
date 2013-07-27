/*
 * configures the application
 */

/*
 * 
keepAlive	true	 
tcpNoDelay	true	 Setting to improve TCP protocol performance
backlog	 	Camel 2.9.6/2.10.4/2.11: Allows to configure a backlog for netty consumer (server). Note the backlog is just a best effort depending on the OS. Setting this option to a value such as 200, 500 or 1000, tells the TCP stack how long the "accept" queue can be. If this option is not configured, then the backlog depends on OS setting.
broadcast	false	 Setting to choose Multicast over UDP
connectTimeout	10000	 Time to wait for a socket connection to be available. Value is in millis.
reuseAddress	true	 Setting to facilitate socket multiplexing
sync	true	 Setting to set endpoint as one-way or request-response
synchronous	false	Camel 2.10: Whether Asynchronous Routing Engine is not in use. false then the Asynchronous Routing Engine is used, true to force processing synchronous.
ssl	false	 Setting to specify whether SSL encryption is applied to this endpoint
sslClientCertHeaders	false	Camel 2.12: When enabled and in SSL mode, then the Netty consumer will enrich the Camel Message with headers having information about the client certificate such as subject name, issuer name, serial number, and the valid date range.
sendBufferSize	65536 bytes	 The TCP/UDP buffer sizes to be used during outbound communication. Size is bytes.
receiveBufferSize	65536 bytes	 The TCP/UDP buffer sizes to be used during inbound communication. Size is bytes.
 * 
 */

atHttpStartup(
	function(http, socket) {
		
		// SO_KEEPALIVE
		socket.keepAlive(true);
		// TCP_NODELAY
		socket.tcpNoDelay(true);
		// SO_BACKLOG
		socket.backlog(10);
		// SO_TIMEOUT
		socket.timeout(10000);
		// SO_REUSEADDR
		socket.reuseAddress(true);
		// SO_SNDBUF
		socket.sendBufferSize(65536);
		// SO_RCVBUF
		socket.receiveBufferSize(65536);
		
		http.bind(8080);
		http.bind("192.168.1.11", 8090);
	}	
);
