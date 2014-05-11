describe('selecting from the DOM', function() {
	
	it('can select elements', function() {
		var body = $('body');
		expect(body).not.toBeNull();
		
		var p = body.select('p');
		expect(String(p.text())).toBe("1 2 3");
	});
	
	it('can confuse me', function() {
		expect(2 + 2).toBe(5);
	});
	
	describe('passing tests are nice, though', function() {
		
		it('can make you feel so good', function() {
			expect(true).toBe(true);
		});
	});
});

describe('this shit is a pain in the ass', function() {
	
	it('is not really doing what i expected', function() {
		expect(1).toBe(1);
	});
});


(function() {

	var reporter = new jasmine.JsApiReporter();
	jasmine.getEnv().addReporter(reporter);

	jasmine.getEnv().execute();


	// id,name,type,children
	var suites = reporter.suites();
	// messages,result
	var results = reporter.results();

	function print(indent, suite) {
		java.lang.System.out.println(indent + suite.name);
		if (suite.type === 'spec') {

			java.lang.System.out.println(indent + " - " + results[suite.id].result);
			java.lang.System.out.println(indent + " - " + results[suite.id].messages.join("\n"));
		}
		
		suite.children.forEach(function(child) {
			print(indent + "  ", child);
		});
		
		//java.lang.System.out.println();
	}

	Object.keys(suites).forEach(function(key) {
		print('', suites[key]);
		
	});

	})();