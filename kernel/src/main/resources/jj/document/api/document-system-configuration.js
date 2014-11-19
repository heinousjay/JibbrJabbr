
var support = require('jj/configuration-support')('jj.document.DocumentConfiguration');

module.exports = {
	clientDebug: support.makeBooleanProperty('clientDebug'),
	showParsingErrors: support.makeBooleanProperty('showParsingErrors'),
	removeComments: support.makeBooleanProperty('removeComments')
}