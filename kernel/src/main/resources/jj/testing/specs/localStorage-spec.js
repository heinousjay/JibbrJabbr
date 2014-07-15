var cwsc = 'jj.http.server.websocket.CurrentWebSocketConnection';
var cse  = 'jj.script.CurrentScriptEnvironment';

var mocks = {};
var current = null;
mocks[cwsc] = {
	current: function() {
		return current;
	}
};
mocks[cse] = {
	current: function() {},
	preparedContinuation: function() {}
}

var inject = function(id) {
	return mocks[id];
}

var print = $$realRequire('jj/print');
print('here is the result:', typeof JSON.stringify(null));
describe('localStorage', function() {
	
	var contextError = new Error('localStorage operations are not valid from this context, there is no connected client');
	
	describe('store', function() {
		
		it('requires a current WebSocket client', function() {
			
			expect(function() {
				module.exports.store('key', 'value');
			}).toThrow(contextError);
		});
		
		it('requires two arguments', function() {
			var oldCurrent = current;
			current = function() {
				return {};
			}
			
			expect(function() {
				module.exports.store();
			}).toThrow(new Error('store requires a string key argument and a value argument of any stringifiable type'));
			
			expect(function() {
				module.exports.store('jason');
			}).toThrow(new Error('store requires a string key argument and a value argument of any stringifiable type'));
			
			expect(function() {
				module.exports.store(1, 'jason');
			}).toThrow(new Error('store requires a string key argument and a value argument of any stringifiable type'));
			
			current = oldCurrent;
		});
	});
	
	describe('retrieve', function() {

		it('requires a current WebSocket client', function() {

			expect(function() {
				module.exports.retrieve('key');
			}).toThrow(contextError);
		});
		
		it('requires a string argument', function() {
			
			var oldCurrent = current;
			current = function() {
				return {};
			}
			
			expect(function() {
				module.exports.retrieve();
			}).toThrow(new Error('retrieve requires a string key argument'));
			
			expect(function() {
				module.exports.retrieve(1);
			}).toThrow(new Error('retrieve requires a string key argument'));
			
			
			current = oldCurrent;
		});
	});
	
});