
var inject = function() {
	return {
		addConfigurationMultiElement: function(l, r) {
			inject.l = l;
			inject.r = r;
		}
	};
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
			ms = makeSetter(GET, route);
		})
		
		it("adds Routes to the collector in the correct key", function() {
			var beef = "/beef";
			var chief = "/chief";
			ms(beef).to(chief);
			
			expect(inject.l).toBe(base + 'routes');
			expect(inject.r).toBeDefined();
			expect(inject.r.method()).toEqual(GET);
			expect(inject.r.uri()).toEqual(valueOf(beef));
			expect(inject.r.destination()).toEqual(valueOf(chief));
		});
		
		it("validates route uris", function() {
			
			expect(function(){
				ms("nope");
			}).toThrow(new Error("nope is not a route uri"));
			
			expect(function(){
				ms("/not with spaces");
			}).toThrow(new Error("/not with spaces is not a route uri"));
			
			expect(function(){
				ms("/nøtwîth/whatever/weird/stuff");
			}).toThrow(new Error("/nøtwîth/whatever/weird/stuff is not a route uri"));
			
		});
	});
});