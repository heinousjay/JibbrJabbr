
describe("a failing spec should fail the suite", function() {
	
	describe("even if it is nested several suites deep", function() {
		
		describe("because the children influence the status of the parent", function() {
			
			it("can't work!", function() {
				expect(1).toBe(0);
			});
			
			it("this success should have no influence", function() {
				expect(1).toBe(1);
			});
		});
		
		it("doesn't matter that this spec passes either", function() {
			expect("life").toBeDefined();
		});
		
		xit("pending has no influence");
	});
	
	it("shouldn't matter if there are passing specs at any level", function() {
		expect(true).toBe(true);
	});
});

describe("failing suites should fail the run", function() {
	it("doesn't matter that this spec passes", function() {
		expect(true).toBe(true);
	});
});