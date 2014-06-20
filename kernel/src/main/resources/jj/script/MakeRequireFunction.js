(function(module) {
	return function(id) {
		if (!id || typeof id != 'string') {
			throw new TypeError('argument to require must be a valid module identifier');
		}
		var result = module['//requireInner'](id, module.id);
		if (result['getCause']) {
			throw result;
		}
		return result;
}})(module);