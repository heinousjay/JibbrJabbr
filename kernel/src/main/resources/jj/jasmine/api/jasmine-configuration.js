var support = require('jj/configuration-support');
var base = 'jj.jasmine.JasmineConfiguration.';

module.exports = {
	autorunSpecs: support.makeBooleanProperty(base, 'autorunSpecs')
};