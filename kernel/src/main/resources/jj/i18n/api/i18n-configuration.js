var support = require('jj/configuration-support');
var base = 'jj.i18n.I18NConfiguration.';

module.exports = {
	allowNonISO: support.makeBooleanProperty(base, 'allowNonISO'),
	defaultLocale: support.makeStringProperty(base, 'defaultLocale')
}