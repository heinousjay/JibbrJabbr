if (module.id !== 'deep/module') throw new Error("module id is broken");

module.exports = function() {
	
	if (module.id !== 'deep/module') throw new Error("module id is broken");
	
	require('nesting/module').doIt();
}
