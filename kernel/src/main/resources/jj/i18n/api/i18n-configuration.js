var support = require('jj/configuration-support')('jj.i18n.I18NConfiguration');

module.exports = {
	allowNonISO: support.makeBooleanProperty('allowNonISO'),
	defaultLocale: support.makeStringProperty('defaultLocale')
}