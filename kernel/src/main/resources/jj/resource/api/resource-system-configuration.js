var support = require('jj/configuration-support');
var base = 'jj.resource.ResourceConfiguration.';

module.exports = {
	ioThreads: support.makeIntProperty(base, 'ioThreads'),
	maxFileSizeToLoad: support.makeLongProperty(base, 'maxFileSizeToLoad'),
	watchFiles: support.makeBooleanProperty(base, 'watchFiles')
}