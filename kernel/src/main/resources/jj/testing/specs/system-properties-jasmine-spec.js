var sysprop = {key:'value'};
var inject = function(id) {
	if (id == 'jj.configuration.SystemPropertiesScriptable') {
		return sysprop;
	}
	
	throw new Error();
}

describe('system-properties.js', function() {
	it('delegates entirely to the SystemPropertiesScriptable object', function() {
		expect(module.exports).toBe(sysprop);
	});
});