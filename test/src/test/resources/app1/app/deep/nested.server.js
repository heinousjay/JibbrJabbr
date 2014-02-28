
if (module.id !== 'deep/nested') throw "module id is broken";

$(function() {
	require('module')();
});