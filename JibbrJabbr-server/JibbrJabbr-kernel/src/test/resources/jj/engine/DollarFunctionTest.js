describe('selecting from the DOM', function() {
	
	it('can select elements', function() {
		var body = $('body');
		expect(body).not.toBeNull();
		
		var p = body.select('p');
		expect(p.length).toBe(3);
		
		expect('my balls').toBe('squished');
		
		java.lang.System.out.println('hmmm');
	});
	
});



var reporter = new jasmine.JsApiReporter();
jasmine.getEnv().addReporter();

jasmine.getEnv().execute();

java.lang.System.out.println(reporter.results());