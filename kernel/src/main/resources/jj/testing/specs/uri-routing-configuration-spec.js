var validator = {
	validateRouteUri: function() {
		
	}
}


var inject = function(id) {
	return {
		'jj.configuration.ConfigurationCollector' : {
			addConfigurationMultiElement: function(l, r) {
				inject.l = l;
				inject.r = r;
			}
		},
		'jj.http.uri.ServableResourceHelper' : {
			arrayOfNames: function() {
				return ['static', 'script', 'stylesheet', 'document'];
			}
		},
		'jj.http.uri.RouteUriValidator' : validator
	}[id];
}
var valueOf = java.lang.String.valueOf;
var bases = [];
var names = [];
var stringProperty = function() {}
var mocks = {
	'jj/configuration-support' : {
		makeStringProperty:function(base, name) {
			bases.push(base);
			names.push(name);
			return stringProperty;
		}
	}
};

var require = function(id) {
	return mocks[id];
}

describe("exports", function() {
	
	describe("welcomeFile", function() {
		
		it("sets up using makeStringProperty", function() {
			expect(bases[0]).toBe(base);
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
			
			var beef = "/beef";
			var chief = "/chief";
			ms(beef).to.document(chief);
			
			expect(inject.l).toBe(base + 'routes');
			expect(inject.r).toBeDefined();
			expect(inject.r.method()).toEqual(GET);
			expect(inject.r.uri()).toEqual(valueOf(beef));
			expect(inject.r.resourceName()).toEqual(valueOf('document'));
			expect(inject.r.mapping()).toEqual(valueOf(chief));
		});
		
		it("errors on validation failure", function() {
			
			spyOn(validator, 'validateRouteUri').and.returnValue('nope');
			
			expect(function(){
				ms("nope");
			}).toThrow(new Error("nope failed validation\nnope"));
			
		});
	});
});