// just to get some motors running
require('jj/repl-configuration').activate(true);

require('jj/resource-system-configuration')
	.ioThreads(10)
	.maxFileSizeToLoad(10240);