// this is the minimal chat example.  it ain't pretty!

// the API is going to move behind modules soon, so there is another
// line coming:
// var $ = require('$');

// we need some id generation for testing
var seed = 0, broadcast = require('api/broadcast');

// this registers a function to run whenever the document is 
// requested, similar to listening to document.ready on the client
// using jQuery
$(function() {
	
	// we connect to the submission of the chat form
	// event hookup in the ready event is deferred til
	// client connection
	// the form is an object stored under the form property of the event object
	$('#in').on('submit', function(e) {

		// first, clear the field.  this is kinda ++minimal, but classy
		$('#say').val('');

		// generate an id (this is really only for testing purposes, 
		// and i have some id-generation helpers in mind for the API)
		// and i am also considering an ID-free testing API for this stuff
		var id = 'line-' + (++seed);

		// and then broadcast to all connected clients.  basically, broadcast executes its function argument
		// once in the context of all connected clients. soon there will also be a predicate system.
		broadcast(function() {

			// to add a div to the chat list with the context
			// being the thing that was submitted
			$('#out').append($('<div></div>', { id: id }).text(e.form.say));
		});
	});
});