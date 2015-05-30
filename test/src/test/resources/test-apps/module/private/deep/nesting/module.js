"use strict";

if (module.id !== 'deep/nesting/module') throw "module id is broken";

exports.doIt = function() {
	
	if (module.id !== 'deep/nesting/module') throw "module id is broken";
	
	// just to close the loop, should be possible even though it required us
	// (and using an absolute path to the resource)
	require('/deep/module');
	// and require myself as well, because i am perverse.
	// do so with a self-relative path
	if (require('./module') !== module.exports) {
		throw new Error('Could not include myself');
	}
	
	if (require('module').doIt !== exports.doIt || require('/' + module.id).doIt !== module.exports.doIt) {
		throw new Error('My insides are screwy');
	}
	
	$('title').text(require('values').value);
}