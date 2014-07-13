
var inject = function() {
	return {
		addConfigurationMultiElement: function(l, r) {
			inject.l = l;
			inject.r = r;
		}
	};
}
var valueOf = java.lang.String.valueOf;

var mocks = {
	'configuration-support' : {
		makeStringProperty:function() {}
	}
};

var require = function(id) {
	return mocks[id];
}

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