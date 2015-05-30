// just to get some motors running
require('jj/repl-configuration').activate(true);

require('jj/resource-system-configuration')
	.ioThreads(10)
	.maxFileSizeToLoad(10240);

require('jj/default-resource-properties');


var route = require('jj/uri-routing-configuration').route;

route.GET('/').to.document("index");
route.GET('/deep/nested').to.document("deep/nested");
