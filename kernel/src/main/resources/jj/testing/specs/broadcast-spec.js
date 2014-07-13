var mockEnv = {};

var inject = function() {
	return mockEnv;
}

describe("broadcast", function() {
	
	it('requires a function argument', function() {
		var error = new Error('broadcast requires a function argument');
		expect(function() {
			module.exports();
		}).toThrow(error);
		
		expect(function() {
			module.exports({});
		}).toThrow(error);
		
		expect(function() {
			module.exports(1);
		}).toThrow(error);
		
		expect(function() {
			module.exports("");
		}).toThrow(error);
	});
});