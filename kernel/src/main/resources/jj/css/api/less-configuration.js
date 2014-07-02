var support = require('configuration-support');
var base = 'jj.css.LessConfiguration.';

module.exports = {
	
	compress: support.makeBooleanProperty(base, 'compress'),
	
	cleancss: support.makeBooleanProperty(base, 'cleancss'),
	
	maxLineLen: support.makeIntProperty(base, 'maxLineLen', function(name, arg) {
		if (arg < 1) {
			throw new TypeError(name + " must be positive");
		}
	}),
	
	/** no optimizations */
	O0: function() {
		support.addElement(base + "optimization", 0);
		return this;
	},
	/** optimization level 1 */
	O1: function() {
		support.addElement(base + "optimization", 1);
		return this;
	},
	/** optimization level 2 */
	O2: function() {
		support.addElement(base + "optimization", 2);
		return this;
	},
	
	depends: support.makeBooleanProperty(base, 'depends'),
	
	silent: support.makeBooleanProperty(base, 'silent'),
	
	verbose: support.makeBooleanProperty(base, 'verbose'),
	
	lint: support.makeBooleanProperty(base, 'lint'),
	
	color: support.makeBooleanProperty(base, 'color'),
	
	strictImports: support.makeBooleanProperty(base, 'strictImports'),
	
	relativeUrls: support.makeBooleanProperty(base, 'relativeUrls'),
	
	ieCompat: support.makeBooleanProperty(base, 'ieCompat'),
	
	strictMath: support.makeBooleanProperty(base, 'strictMath'),
	
	strictUnits: support.makeBooleanProperty(base, 'strictUnits'),
	
	javascriptEnabled: support.makeBooleanProperty(base, 'javascriptEnabled'),
	
	sourceMaps: support.makeBooleanProperty(base, 'sourceMaps'),
	
	rootpath: support.makeStringProperty(base, 'rootpath')
};