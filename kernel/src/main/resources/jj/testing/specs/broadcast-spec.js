var mockEnv = {};
var mockHost = {};

var inject = function() {
	return mockEnv;
}

describe("broadcast", function() {
	var bc;
	beforeEach(function() {
		bc = module.exports;
	});
	
	it('exports a function', function() {
		expect(typeof bc).toBe('function');
	});
	
	it('requires a function argument', function() {
		var error = new Error('broadcast requires a function argument');
		expect(function() {
			bc();
		}).toThrow(error);
		
		expect(function() {
			bc({});
		}).toThrow(error);
		
		expect(function() {
			bc(1);
		}).toThrow(error);
		
		expect(function() {
			bc("");
		}).toThrow(error);
	});
	
	describe("checks for a WebSocketConnectionHost", function() {
		var error = new Error('cannot broadcast from this context, there is no web socket connection host');
		
		it('throws an error if current returns null', function() {
			mockEnv.current = function() { return null; }
			expect(function() {
				bc(function() {});
			}).toThrow(error);
		});
		
		it('throws an error if current does not have startBroadcasting', function() {
			mockEnv.current = function() { return mockHost; }
			expect(function() {
				bc(function() {});
			}).toThrow(error);
		});
		
		afterEach(function() {
			if (mockEnv.current) {
				delete mockEnv.current;
			}
		});
	})
});