var support = require('configuration-support');
var base = 'jj.css.LessConfiguration.';

module.exports = {
	depends: support.makeBooleanProperty(base, 'depends'),
	compress: support.makeBooleanProperty(base, 'compress'),
	cleancss: support.makeBooleanProperty(base, 'cleancss'),
	// must be positive!
	maxLineLen: support.makeIntProperty(base, 'maxLineLen'),
	// to-do - only accept 0,1,2
	optimization: support.makeIntProperty(base, 'optimization'),
	depends: support.makeBooleanProperty(base, 'depends'),
	silent: support.makeBooleanProperty(base, 'silent'),
	verbose: support.makeBooleanProperty(base, 'verbose'),
	lint: support.makeBooleanProperty(base, 'lint'),
	color: support.makeBooleanProperty(base, 'color'),
	strictImports: support.makeBooleanProperty(base, 'strictImports'),
	relativeUrls: support.makeBooleanProperty(base, 'relativeUrls'),
	ieCompat: support.makeBooleanProperty(base, 'ieCompat'),
	strictMath: support.makeBooleanProperty(base, 'strictMath'),
	strictUnits: support.makeBooleanProperty(base, 'strictUnits')
};