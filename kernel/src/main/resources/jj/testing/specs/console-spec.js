var mockConsole = {
	trace: function() {},
	debug: function() {},
	info: function() {},
	warn: function() {},
	error: function() {}
}

var inject = function(id) {
	return {
		'jj.script.api.ScriptConsole': mockConsole
	}[id];
}

describe("console.js", function() {
	var c;
	beforeEach(function() {
		spyOn(mockConsole, 'trace');
		spyOn(mockConsole, 'debug');
		spyOn(mockConsole, 'info');
		spyOn(mockConsole, 'warn');
		spyOn(mockConsole, 'error');
		
		c = module.exports;
	});
	
	it("traces", function() {
		c.trace(1, 2, "jason");
		expect(mockConsole.trace).toHaveBeenCalledWith(["1", "2", "\"jason\""]);
	});
	
	it("debugs", function() {
		c.debug({jason: 'made this'});
		expect(mockConsole.debug).toHaveBeenCalledWith(["{\"jason\":\"made this\"}"]);
	});
	
	it("logs", function() {
		c.log("simple");
		expect(mockConsole.info).toHaveBeenCalledWith(["\"simple\""]);
	});
	
	it("infos", function() {
		c.info("simple");
		expect(mockConsole.info).toHaveBeenCalledWith(["\"simple\""]);
	});
	
	it("warns", function() {
		c.warn("simple");
		expect(mockConsole.warn).toHaveBeenCalledWith(["\"simple\""]);
	});
	
	it("errors", function() {
		c.error("simple");
		expect(mockConsole.error).toHaveBeenCalledWith(["\"simple\""]);
	});
});