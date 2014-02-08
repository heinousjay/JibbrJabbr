describe('ready function does nothing', function() {
	
	var pristine = '';
	
	it('should not add any text in paragraphs', function() {
		
		// spec API call
		executeReadyFunction();
		
		// you can invoke the $ function with selectors to work against
		// the document instance the ready function invokes
		
		expect($('body p').text()).toBe(pristine); // PRISTINE!
	});
});

describe('clientConnected adds a message', function() {
	
	// TODO make this more jasmine-y
	// TODO MAKE THIS RUN! THIS FUCKING ROCKS!
	it('should set a message when connected', function() {
		
		var checkID = '';
		
		// spec API call.  all event execution calls in the API accept a function that
		// takes a single parameter (name it 'on' for me!)
		// this parameter is a bus to register mocks for the event execution so you can
		// make things do as you expect.
		// the system SHOULD try to mock as much as it can with reasonable defaults when it
		// can't find any matches and just report failures so that test devs can fill in
		// the necessary gaps
		var client = connectClient(function(on) {
			// this is dependent on your definition, so if you don't mock an instance, it
			// will reply with null
			on.remoteInvocationWithReturn('addMessageToBody', 'Hi from the server!').replyWith(true);
			
			// creation replies with ids, so if you don't mock this, it will reply with a
			// sequence of the form newId123
			on.creation('<p></p>').replyWithID('magic').andThen(function() {
				checkID = '#magic';
				// what an unreasonable way to initialize something!
			});
		});
		
		// either with or without return
		expect(client.lastMessage()).toBeRemoteInvocation('addMessageToBody', 'Hi from the server!');
		// with a return or fail
		expect(client.lastMessage()).toBeRemoteInvocationWithReturn('addMessageToBody', 'Hi from the server!');
		// with no return or fail
		expect(client.lastMessage()).toBeRemoteInvocationWithNoReturn('addMessageToBody', 'Hi from the server!');
		
		// get the last 3 messages.  no param means all
		var messages = client.lastMessages(3);
		expect(message[0]).toBeCreation('<p></p>');
		// (this flow implies a special API object for execution that simply produces the responses
		// that the code expects if everything is in working order.  that implies mockability for 
		// expected responses! (obviously))
		expect(message[1]).toBeAppend('body', checkID);
		expect(message[2]).toBeBind(checkID, '', 'click');
		expect(message[2].handler).toBeDefined();
		
		// now we can test that handler!
		
		// what you actually get is a wrapper that allows you to set the parameters of the event
		// using an object so it can invoke it properly
		
		// type - this defaults to the type specified during bind (or the first type in the list, if multiples)
		
		// which - this corresponds to the normally which property from the client - which button in the case 
		//         of clicks, which key for key events... you must set this if it's relevant, but that's up to
		//         you
		
		// target - in the system, this is always an id pointing specifically to the element that was the target
		//          of the client event.  if the bind event was initially produced by an id selected object,
		//          then that will be the default value for target.  otherwise, it will be a string of the
		//          form #newId123. you can set this to whatever you wish, of course.
		
		// form - well, this is changing a bit right now so hold tight.  but it's the form, if this was a submit event
		
		// the following are for advanced use, in fact so advanced i can't imagine what. but i'm exposing everything
		// deliberately.  no dark corners in JibbrJabbr
		
		// context - the original selection context of the bind message. this is the selector if the event is set
		//           directly, and the selector of the container(s) for delegated events
		
		// selector - for delegated events only, the selector passed to the on function
		
		// and again this is an event function, so you can pass a mock handler function
		
		message[2].handler({
			type: 'click', // the default
			which: 1, // the mouse button.  if you don't care, don't supply it
			target: checkID, // the default
			context: checkID, // the default
			selector: '' // the default
		}, function(on) {
			// if you need to mock something, go ahead!  I didn't, here
		});
		
		// so if you aren't looking for the mouse button, that was equivalent to
		message[2].handler();
		
		
		
		client.disconnect();
	});
});