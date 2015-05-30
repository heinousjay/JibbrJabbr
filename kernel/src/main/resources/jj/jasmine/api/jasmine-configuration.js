var support = require('jj/configuration-support')('jj.jasmine.JasmineConfiguration');

module.exports = {
	autorunSpecs: support.makeBooleanProperty('autorunSpecs')
};