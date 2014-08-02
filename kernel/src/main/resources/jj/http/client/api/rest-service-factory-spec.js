var require = function(id) {
	return $$realRequire(id);
	
}

describe("mergeObject", function() {
	it('should merge two objects together', function() {
		var object1 = {a: 'b'};
		var object2 = {b: 'c'};
		mergeObject(object1, object2);
		
		expect(object1.a).toBe('b');
		expect(object1.b).toBe('c');
		expect(Object.keys(object1).length).toBe(2);
	});
});

describe("makeUri", function() {
	
	it('concatenates the base and operation URIs', function() {
		let [uri, remainingParams] = makeUri('http://localhost', '/api');
		expect(uri).toBe('http://localhost/api');
		expect(remainingParams).toEqual({});
	});
	
	it('substitutes parameters into the resulting URI', function() {
		let [uri, remainingParams] = makeUri('http://localhost', '/api/:param/trailer', {param:'value'});
		expect(uri).toBe('http://localhost/api/value/trailer');
		expect(remainingParams).toEqual({});
	});
	
	it('returns unsubstituted parameters as remainingParams', function() {
		let [uri, remainingParams] = makeUri('http://localhost', '/api/trailer', {param:'value'});
		expect(uri).toBe('http://localhost/api/trailer');
		expect(remainingParams).toEqual({param:'value'});
	});
});

describe('makeUriNoBody', function() {
	
	it('appends a query string to the result from makeUri', function() {
		let uri = makeUriNoBody('http://localhost', '/api/trailer', {param:'val ue'});
		expect(uri).toEqual('http://localhost/api/trailer?param=val%20ue');
	});
});
