if (module.id !== 'deep/module') throw new Error("module id is broken");

exports = "this does not matter since module.exports is defined below.  exports gets disconnected";

module.exports = function() {
	
	if (module.id !== 'deep/module') throw new Error("module id is broken");
	
	require('nesting/module').doIt();
}

exports = "this does not matter since module.exports is defined above.  exports is now disconnected";

if (module.requireInner) {
	module.exports = function() {
		throw new Error("this should not be exposed");
	}
}