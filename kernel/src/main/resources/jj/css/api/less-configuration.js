var support = require('jj/configuration-support')('jj.css.LessConfiguration');

module.exports = {
	
	compress: support.makeBooleanProperty('compress'),
	
	cleancss: support.makeBooleanProperty('cleancss'),
	
	maxLineLen: support.makeIntProperty('maxLineLen', function(name, arg) {
		if (arg < 1) {
			return support.accumulateError('maxLineLen', 'must be positive');
		}
	}),
	
	/** no optimizations */
	O0: function() {
		support.addElement("optimization", 0);
		return this;
	},
	/** optimization level 1 */
	O1: function() {
		support.addElement("optimization", 1);
		return this;
	},
	/** optimization level 2 */
	O2: function() {
		support.addElement("optimization", 2);
		return this;
	},
	
	depends: support.makeBooleanProperty('depends'),
	
	silent: support.makeBooleanProperty('silent'),
	
	verbose: support.makeBooleanProperty('verbose'),
	
	lint: support.makeBooleanProperty('lint'),
	
	color: support.makeBooleanProperty('color'),
	
	strictImports: support.makeBooleanProperty('strictImports'),
	
	relativeUrls: support.makeBooleanProperty('relativeUrls'),
	
	ieCompat: support.makeBooleanProperty('ieCompat'),
	
	strictMath: support.makeBooleanProperty('strictMath'),
	
	strictUnits: support.makeBooleanProperty('strictUnits'),
	
	javascriptEnabled: support.makeBooleanProperty('javascriptEnabled'),
	
	sourceMaps: support.makeBooleanProperty('sourceMaps'),
	
	rootpath: support.makeStringProperty('rootpath')
};