// including these scripts for the SystemScriptsTest but we configure off
require('jj/jasmine-configuration').autorunSpecs(false);
require('jj/repl-configuration').activate(true).port(33445); // on an abnormal port to avoid startup conflicts

require('jj/i18n-configuration')
	.allowNonISO(true)
	.defaultLocale('en-gb');

// the http-server-socket module returns an object that can configure
// its namesake
require('jj/http-server-socket-configuration')
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

require('jj/less-configuration')
	.compress(true)
	.cleancss(true)
	.maxLineLen(1024)
	.O2()
	.depends(true)
	.silent(true)
	.verbose(true)
	.lint(true)
	.color(false)
	.strictImports(true)
	.relativeUrls(true)
	.ieCompat(false)
	.strictMath(true)
	.strictUnits(true)
	.javascriptEnabled(false)
	.sourceMaps(false)
	.rootpath("images/");

require('jj/document-system-configuration')
	.clientDebug(true)
	.showParsingErrors(true)
	.removeComments(false);

require('jj/resource-system-configuration')
	.ioThreads(10)
	.maxFileSizeToLoad(102400000000) // test a bigun
	.watchFiles(false);  // just to check it

require('jj/default-resource-properties')
	.extension('stupid').is({mimeType: 'text/stupid'}); // configure normally

// configuration of routes is in a separate include because it makes scoping prettier
require('routes');

require('jj/logging-configuration')
	.netty.off()
	.access.off()
	.scriptSystem.off()
	.resourceSystem.off()
	.server.debug()
	.script('index').trace()
	.script('d3/index').trace()
	.script('chat/index').off();
