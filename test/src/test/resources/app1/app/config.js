require('jj/document-system-configuration')
	.clientDebug(false)
	.showParsingErrors(true)
	.removeComments(true);

require('jj/default-resource-properties');

var route = require('jj/uri-routing-configuration').route;

route.GET('/').to.document("index");
route.GET('/index').to.document("index");
route.GET('/animal').to.document("animal");
