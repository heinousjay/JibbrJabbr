global['//makeRequire'] = function(module) {
	return function(id) {
		if (!id || typeof id != 'string') {
			throw new TypeError('argument to require must be a valid module identifier');
		}
		var result = global['//require'](id, module.id);
		if (result['getCause']) {
			throw new Error(result.getMessage());
		}
		return result;
	}
}
