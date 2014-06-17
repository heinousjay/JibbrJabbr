
// the http-server-socket module returns an object that can configure
// its namesake
require('http-server-socket-configuration')
	//SO_KEEPALIVE
	.keepAlive(true)
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
	
	// bind each [host], port combination
	// you want listening.
	// the command line argument httpPort
	// overrides this configuration
	// the default is to bind to all addresses
	// on port 8080, which is also what this does
	.bind(8080)
	.bind('localhost', 8090);

require('document-system-configuration')
	.clientDebug(true)
	.showParsingErrors(true)
	.removeComments(false);

require('resource-system-configuration')
	.ioThreads(10);

// this is a silly helper, it just copies the exports
// of the given script into the given object
var globalize = require('globalize');

// so bam.  life is easy.  although in this case, there
// are only two pieces exposed
globalize('uri-routing-configuration', this);

welcomeFile('root');

//for example!
route.GET('/chat/').to('/chat/list');
route.POST('/chat/:room').to('/chat/room');
route.PUT('/chat/:room/*secret').to('/chat/room');
route.DELETE('/chat/:room/*secret').to('/chat/room');

