var helper = {
	subscribe: function() {},
	unsubscribe: function() {}
};

function inject() {
	return helper;
}

describe("server-events", function() {
	// we need a function. here's a function
	function testFunc() { return false; }
	
	describe("on", function() {
		
		it("requires a string argument and a function argument", function() {
			expect(function() {
				module.exports.on();
			}).toThrow(new Error("on requires a string argument and a function argument"));
			
			expect(function() {
				module.exports.on("hey");
			}).toThrow(new Error("on requires a string argument and a function argument"));
			
			expect(function() {
				module.exports.on(function() {});
			}).toThrow(new Error("on requires a string argument and a function argument"));
		});
		
		it("passes parameters to the helper", function() {
			spyOn(helper, "subscribe");
			module.exports.on('hi', testFunc);
			expect(helper.subscribe).toHaveBeenCalledWith('hi', testFunc);
		});
		
		it("reports an incorrect event name", function() {
			spyOn(helper, "subscribe").and.returnValue(NotAnEventClass);
			expect(function() {
				module.exports.on('hi', testFunc);
			}).toThrow(new Error("hi is not an event class"));
		});
		
		it("reports handlers that are already bound", function() {
			spyOn(helper, "subscribe").and.returnValue(AlreadyBound);
			expect(function() {
				module.exports.on('hi', testFunc);
			}).toThrow(new Error("hi already has a binding to the given function"));
		});
		
		it("returns the exports object for chaining", function() {
			expect(module.exports.on('hi', testFunc)).toBe(module.exports);
		});
	});
	
	describe("off", function() {
		
		it("requires a string argument and a function argument", function() {
			expect(function() {
				module.exports.off();
			}).toThrow(new Error("off requires a string argument and a function argument"));
			
			expect(function() {
				module.exports.off("hey");
			}).toThrow(new Error("off requires a string argument and a function argument"));
			
			expect(function() {
				module.exports.off(function() {});
			}).toThrow(new Error("off requires a string argument and a function argument"));
		});
		
		it("passes parameters to the helper", function() {
			spyOn(helper, "unsubscribe");
			module.exports.off('hi', testFunc);
			expect(helper.unsubscribe).toHaveBeenCalledWith('hi', testFunc);
		});
		
		it("reports events that were not bound", function() {
			spyOn(helper, "unsubscribe").and.returnValue(NotBound);
			expect(function() {
				module.exports.off('hi', testFunc);
			}).toThrow(new Error("hi was not previously bound to the given function"));
		});
		
		it("returns the exports object for chaining", function() {
			expect(module.exports.off('hi', testFunc)).toBe(module.exports);
		});
	});
	
	describe("once", function() {
		
		it("requires a string argument and a function argument", function() {
			expect(function() {
				module.exports.once();
			}).toThrow(new Error("once requires a string argument and a function argument"));
			
			expect(function() {
				module.exports.once("hey");
			}).toThrow(new Error("once requires a string argument and a function argument"));
			
			expect(function() {
				module.exports.once(function() {});
			}).toThrow(new Error("once requires a string argument and a function argument"));
		});
		
		it("registers a callback and then unregisters it when it has been called", function() {
			spyOn(helper, "subscribe");
			spyOn(helper, "unsubscribe");
			
			module.exports.once("hi", testFunc);
			
			expect(helper.unsubscribe).not.toHaveBeenCalled();
			
			expect(helper.subscribe.calls.mostRecent().args[0]).toEqual("hi");
			helper.subscribe.calls.mostRecent().args[1]();
			
			expect(helper.unsubscribe.calls.mostRecent().args).toEqual(helper.subscribe.calls.mostRecent().args)
		});
		
		// otherwise it's just calling on so it has the same errors
		
		it("reports an incorrect event name", function() {
			spyOn(helper, "subscribe").and.returnValue(NotAnEventClass);
			expect(function() {
				module.exports.once('hi', testFunc);
			}).toThrow(new Error("hi is not an event class"));
		});
		
		it("reports handlers that are already bound", function() {
			spyOn(helper, "subscribe").and.returnValue(AlreadyBound);
			expect(function() {
				module.exports.once('hi', testFunc);
			}).toThrow(new Error("hi already has a binding to the given function"));
		});
		
		it("returns the exports object for chaining", function() {
			expect(module.exports.once('hi', testFunc)).toBe(module.exports);
		});
	});
	
});
