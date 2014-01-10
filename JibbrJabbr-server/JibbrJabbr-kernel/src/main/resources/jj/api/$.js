
function makeSelectionResult(selector) {
	
	var elements = global['//select'](selector);
	
}

var selectionPrototype = {
	
	text: function() {
		switch (arguments.length) {
		case 0:
			return this.map(function(el) {
				return el.text();
			}).join('');
			break;
		}
	}
}


module.exports = function() {
	
	// no arguments? it's an error
	
	
	
	// is it a function?
	// - if we are initializing, add it to the ready list and return self
	// - if we are initialized and this is responding to an http request, execute it and return self
	// - otherwise, error
	
	// does it match the pattern for creation?
	// - return the result of the creation function
	
	// otherwise, return the result of the selection function
	return makeSelectionResult(arguments[0]);
	
}