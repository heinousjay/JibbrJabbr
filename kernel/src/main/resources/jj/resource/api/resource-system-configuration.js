var support = require('jj/configuration-support')('jj.resource.ResourceConfiguration');

module.exports = {
	ioThreads: support.makeIntProperty('ioThreads'),
	maxFileSizeToLoad: support.makeLongProperty('maxFileSizeToLoad'),
	watchFiles: support.makeBooleanProperty('watchFiles')
}