
var support = require('configuration-support');
var base = 'jj.resource.ResourceConfiguration.';

module.exports = {
	ioThreads: support.makeIntProperty(base, 'ioThreads')
}