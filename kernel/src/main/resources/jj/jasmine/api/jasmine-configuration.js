var support = require('api/configuration-support');
var base = 'jj.jasmine.JasmineConfiguration.';

module.exports = {
	autorunSpecs: support.makeBooleanProperty(base, 'autorunSpecs')
};