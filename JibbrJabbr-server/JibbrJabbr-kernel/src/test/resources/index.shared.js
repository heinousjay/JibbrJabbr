// this file is shared between the client and server.
// it's mainly suitable for functions that perform
// common manipulations of data or the DOM, and constants
// there isn't a whole lot to say here

var maxMessages = 50;

var addMessage = (function() {
	// this is more of a demonstration than a practical function
	var handlers = {
		say: function doSay(message) {
			var newLine =
				$('<div>', {
					id : message.id
				}).html('&lt;' + message.user + '&gt; ')
				.append($('<span>').addClass('chat-text').html(message.content));
			$('#chatbox').append(newLine);
			return newLine;
		},
		emote: function doEmote(message) {
			var newLine = 
				$('<div>', {
					id : message.id
				}).html('* ' + message.user + ' ')
				.append($('<span>').addClass('chat-text').html(message.content));
			$('#chatbox').append(newLine);
			
			return newLine;
		}
	};
	
	return function(message) {
		return handlers[message.type](message);
	}
})();
