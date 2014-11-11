var validator = {
	validateRouteUri: function() {
		
	}
}


var inject = function(id) {
	return {
		'jj.http.server.ServableResourceHelper' : {
			arrayOfNames: function() {
				return ['static', 'script', 'stylesheet', 'document'];
			}
		},
		'jj.http.server.uri.RouteUriValidator' : validator
	}[id];
}
var valueOf = java.lang.String.valueOf;
var names = [];
var stringProperty = function() {}
var support = {
	makeStringProperty:function(name) {
		names.push(name);
		return stringProperty;
	},
	accumulateError: function() {},
	addToList: function() {}
};
var mocks = {
	'jj/configuration-support' : function(base) { return support; }
};

var require = function(id) {
	return mocks[id];
}

describe("exports", function() {
	
	describe("welcomeFile", function() {
		
		it("sets up using makeStringProperty", function() {
			expect(names[0]).toBe("welcomeFile");
		});
		
		it("delegates to stringProperty", function() {
			
			expect(module.exports.welcomeFile).toBe(stringProperty);
		});
	});
	
	describe("route", function() {
		
		it("contains routing methods", function() {
			var r = module.exports.route;
			expect(r.get).toBeDefined();
			expect(r.GET).toBeDefined();
			expect(r.post).toBeDefined();
			expect(r.POST).toBeDefined();
			expect(r.put).toBeDefined();
			expect(r.PUT).toBeDefined();
			expect(r.del).toBeDefined();
			expect(r.DELETE).toBeDefined();
		});
	});
	
});

describe("makeSetter", function() {
	
	beforeEach(function() {
		inject.l = inject.r = null;
	});
	
	describe("to", function() {
		
		var ms;
		
		beforeEach(function() {
			ms = makeSetter(GET);
		})
		
		it("adds valid Routes to the collector in the correct key", function() {
			
			spyOn(validator, 'validateRouteUri').and.returnValue('');
			spyOn(support, 'addToList');
			
			var beef = "/beef";
			var chief = "/chief";
			ms(beef).to.document(chief);
			
			expect(support.addToList).toHaveBeenCalled();
			expect(support.addToList.calls.mostRecent().args[0]).toBe('routes');
			var r = support.addToList.calls.mostRecent().args[1];
			expect(r.method()).toEqual(GET);
			expect(r.uri()).toEqual(valueOf(beef));
			expect(r.resourceName()).toEqual(valueOf('document'));
			expect(r.mapping()).toEqual(valueOf(chief));
		});
		
		it("errors on validation failure", function() {
			
			spyOn(validator, 'validateRouteUri').and.returnValue('nope');
			spyOn(support, 'accumulateError');
			var result = ms("nope");
			expect(result).toBeDefined();
			expect(typeof result.to).toBe('function');
			expect(support.accumulateError).toHaveBeenCalled();
		});
	});
});