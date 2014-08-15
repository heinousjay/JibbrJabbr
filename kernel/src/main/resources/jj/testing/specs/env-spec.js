var env = {key:'value'};
var inject = function(id) {
	if (id == 'jj.configuration.EnvironmentScriptable') {
		return env;
	}
	
	throw new Error();
}

describe('env.js', function() {
	it('delegates entirely to the EnvironmentScriptable object', function() {
		expect(module.exports).toBe(env);
	});
});