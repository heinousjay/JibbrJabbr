if (module.id !== 'deep/module') throw "module id is broken";

exports.doIt = function() {
	
	if (module.id !== 'deep/module') throw "module id is broken";
	
	require('nesting/module').doIt();
}
