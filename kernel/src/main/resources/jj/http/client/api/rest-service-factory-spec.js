var require = function(id) {
	return $$realRequire(id);
	
}
var fakeRestOperation = {};
var inject = function() {
	return fakeRestOperation;
}

describe("rest-service-factory.js", function() {
	
	var rsf = 
		
	beforeEach(function() {
		rsf = module.exports;
	});
	
	it("requires an object parameter", function() {
		expect(function() {
			rsf();
		}).toThrow(new Error(SERVICE_FACTORY_REQUIRES_OBJECT));
		
		expect(function() {
			rsf(1);
		}).toThrow(new Error(SERVICE_FACTORY_REQUIRES_OBJECT));
		
		expect(function() {
			rsf(true);
		}).toThrow(new Error(SERVICE_FACTORY_REQUIRES_OBJECT));
		
		expect(function() {
			rsf("balls");
		}).toThrow(new Error(SERVICE_FACTORY_REQUIRES_OBJECT));
	});
	
	it("requires a baseUri key in the options", function() {
		expect(function() {
			rsf({});
		}).toThrow(new Error(OPTIONS_REQUIRE_BASEURI));
	});
	
	it("requires at least one operation definition", function() {
		expect(function() {
			rsf({
				baseUri: 'baseUri'
			});
		}).toThrow(new Error(OPTIONS_REQUIRE_OPERATIONS));
		
		expect(function() {
			rsf({
				baseUri: 'baseUri',
				operations: []
			});
		}).toThrow(new Error(OPTIONS_REQUIRE_OPERATIONS));

		expect(function() {
			rsf({
				baseUri: 'baseUri',
				operations: true
			});
		}).toThrow(new Error(OPTIONS_REQUIRE_OPERATIONS));

		expect(function() {
			rsf({
				baseUri: 'baseUri',
				operations: 1
			});
		}).toThrow(new Error(OPTIONS_REQUIRE_OPERATIONS));

		expect(function() {
			rsf({
				baseUri: 'baseUri',
				operations: "balls"
			});
		}).toThrow(new Error(OPTIONS_REQUIRE_OPERATIONS));
	});
	
	it("requires operation definitions to be objects", function() {
		
		expect(function() {
			rsf({
				baseUri: 'baseUri',
				operations: {
					op: true
				}
			})
		}).toThrow(new Error(OPERATION_DEFINITIONS_ARE_OBJECTS));
		
		expect(function() {
			rsf({
				baseUri: 'baseUri',
				operations: {
					op: 1
				}
			})
		}).toThrow(new Error(OPERATION_DEFINITIONS_ARE_OBJECTS));

		expect(function() {
			rsf({
				baseUri: 'baseUri',
				operations: {
					op: "balls"
				}
			})
		}).toThrow(new Error(OPERATION_DEFINITIONS_ARE_OBJECTS));
	});
});

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
