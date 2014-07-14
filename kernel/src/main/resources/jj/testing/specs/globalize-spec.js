var require = function(id) {
	return id == 'id' ? {
		a: 1,
		b: 2,
		c: 3
	} : null;
}

describe("globalize", function() {
	
	it('accepts an id, calls require with that id, and copies the exports to the given object', function() {
		var container = {};
		module.exports('id', container);
		
		expect(container.a).toBe(1);
		expect(container.b).toBe(2);
		expect(container.c).toBe(3);
	});
});