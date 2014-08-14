var env = {key:'value'};
var inject = function(id) {
	if (id == 'jj.configuration.Environment') {
		return env;
	}
	
	throw new Error();
}

describe('env.js', function() {
	it('delegates entirely to the Environment object', function() {
		expect(module.exports).toBe(env);
	});
});