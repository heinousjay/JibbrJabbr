
if (module.id !== 'deep/nested') throw "module id is broken";

$(function() {
	
	var failed = false;
	try {
		require('not-a-module');
		failed = 'did not fail!';
	} catch (e) {
		failed = e.message == 'module "not-a-module" cannot be found' ? false : 'bad message ' + e.message;
	}
	
	if (failed) throw new Error(failed);
	
	var notAnId = true;
	try {
		require(notAnId);
		failed = 'did not fail!';
	} catch (e) {
		failed = e.message == 'true is not a valid module identifier' ? false : 'bad message ' + e.message;
	}
	
	if (failed) throw new Error(failed);
	
	require('module')();
});