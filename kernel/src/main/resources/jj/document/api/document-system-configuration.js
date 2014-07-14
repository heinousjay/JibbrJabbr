
var support = require('jj/configuration-support');
var base = 'jj.document.DocumentConfiguration.';

module.exports = {
	clientDebug: support.makeBooleanProperty(base, 'clientDebug'),
	showParsingErrors: support.makeBooleanProperty(base, 'showParsingErrors'),
	removeComments: support.makeBooleanProperty(base, 'removeComments')
}