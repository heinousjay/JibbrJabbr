describe("test function mergeObject", function() {
	it('should merge two objects together', function() {
		var object1 = {a: 'b'};
		var object2 = {b: 'c'};
		mergeObject(object1, object2);
		
		expect(object1.a).toBe('b');
		expect(object1.b).toBe('c');
		expect(Object.keys(object1).length).toBe(2);
	});
});

describe("test function unpackArguments", function() {
	
	it('should handle no args correctly', function() {
		let [parameters, headers, body, callback] = unpackArguments([]);
		expect(parameters).toBeUndefined();
		expect(headers).toBeUndefined();
		expect(body).toBeUndefined();
		expect(callback).toBeUndefined();
	});
	
	it('should handle one parameter argument correctly', function() {
		var params = {hi:'there'};
		let [parameters, headers, body, callback] = unpackArguments([params]);
		expect(parameters).toBe(params);
		expect(headers).toBeUndefined();
		expect(body).toBeUndefined();
		expect(callback).toBeUndefined();
	});
	
	it('should handle one callback argument correctly', function() {
		var cb = function() {}
		let [parameters, headers, body, callback] = unpackArguments([cb]);
		expect(parameters).toBeUndefined();
		expect(headers).toBeUndefined();
		expect(body).toBeUndefined();
		expect(callback).toBe(cb);
	});
	
	it('should handle parameter,callback arguments correctly', function() {
		var params = {hi:'there'};
		var cb = function() {}
		let [parameters, headers, body, callback] = unpackArguments([params, cb]);
		expect(parameters).toBe(params);
		expect(headers).toBeUndefined();
		expect(body).toBeUndefined();
		expect(callback).toBe(cb);
	});
	
	it('should handle all arguments correctly', function() {
		var params = {hi:'there'};
		var h = {hello:'back'};
		var b = 'body';
		var cb = function() {}
		let [parameters, headers, body, callback] = unpackArguments([params, h, b, cb]);
		expect(parameters).toBe(params);
		expect(headers).toBe(h);
		expect(body).toBe(b);
		expect(callback).toBe(cb);
	})
//	
//	(function testParametersAndCallbackArgs() {
//		print('testParametersAndCallbackArgs');
//		let [parameters, headers, body, callback] = unpackArguments([{}, function() {}]);
//		print("parameters", parameters, "headers", headers, "body", body, "callback", callback);
//	})();
});



// none of this belongs here - it should be in a jasmine integration test.
// in fact the entire introduction.js suite should be in there to prove
// passing works!
describe("Manually ticking the Jasmine Clock", function() {
	var timerCallback;

	beforeEach(function() {
		timerCallback = jasmine.createSpy("timerCallback");
		jasmine.clock().install();
	});

	afterEach(function() {
		jasmine.clock().uninstall();
	});
	
	it("causes a timeout to be called synchronously", function() {
		setTimeout(function() {
			timerCallback();
		}, 100);

		expect(timerCallback).not.toHaveBeenCalled();

		jasmine.clock().tick(101);

		expect(timerCallback).toHaveBeenCalled();
	});

	it("causes an interval to be called synchronously", function() {
		setInterval(function() {
			timerCallback();
		}, 100);

		expect(timerCallback).not.toHaveBeenCalled();

		jasmine.clock().tick(101);
		expect(timerCallback.calls.count()).toEqual(1);

		jasmine.clock().tick(50);
		expect(timerCallback.calls.count()).toEqual(1);

		jasmine.clock().tick(50);
		expect(timerCallback.calls.count()).toEqual(2);
	});
});