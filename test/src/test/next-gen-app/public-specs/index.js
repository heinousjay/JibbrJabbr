describe('addMessageToBody test', function() {
	
	var message = 'message!';
	
	it('adds a message to the body', function() {
		addMessageToBody(message);
		expect($('p').text()).toBe(message);
	});
});