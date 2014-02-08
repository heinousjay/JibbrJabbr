var showMessage = require('module1');

$(function) {
	// even empty, the ready function is required or you're not hooked up on the server
	
	// is it possible?! to be able to dynamically redefine the functions that are stubbed from the client
	// so that during the ready function execution, they can be the actual bodies, and then during the
	// connected executions, they are the stubs?
	
	// it would be on the author to correctly use functions that will work in both contexts, but that
	// seems like it's a fair enough deal, they'll know what they mean and it would be a pretty snazzy
	// way of handling the shared rendering logic in a transparent and non-surprising manner
	
	// includes all the original conditions for the rpc mechanism, and one addition; 
	// the functions executing on the server cannot rely on code that does not meet all of the other conditions.
	// that is to say you can't call a function willBeStubbed defined like

	// var notGoingToBeStubbed = function() {
	//     // ... do work
	// }
	
	// function willBeStubbed() {
	//     notGoingToBeStubbed(); // this is bad
	//     window.location.reload(); // this is also bad
	//     $('body').empty().text('goodbye'); // this will work fine though
	// }
	
	// note that such functions can still be called during connected execution
}

//$.clientConnected
clientConnected(function() {
	showMessage('Hi from the server!');
});
