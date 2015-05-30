var dehtml = (function() {
	return function(input) {
		return input.replace(/[&"<]/g, function(match) {
			switch (match) {
			case '&': return '&amp;';
			case '"': return '&quot;';
			case '<': return '&lt;';
			}
		});
	};
})();

var linkify = require('helpers/linkify');

var smileyify = require('helpers/smileys');

var broadcast = require('jj/broadcast');

//var localStorage = require('jj/local-storage');

var console = require('jj/console');

var messages = (function() {
	// need some unique ids per line
	var idBase = "chat-line";
	var id = (function(at) {
		var src = 0;
		return function() {
			return idBase + (at || (src++));
		}
	})();
	
	// and a message store
	var messages = {};
	// where the oldest message we have is
	var lwm = 0;
	// where the next message goes
	var hwm = 0;
	// hwm - lwm == count
	
	var emote = /^\/me /;
	
	return {
		add: function(content, user, skipProcessing) {
			
			var type = 'say';
			if (emote.test(content)) {
				type = 'emote';
				content = content.substring(4);
			}
			
			var newId = id();
			content = skipProcessing ? content : smileyify(linkify(dehtml(content)));
			
			messages[newId] = {
				type: type,
				id: newId,
				user: user,
				content: content
			};
			++hwm;
			while (hwm - lwm > maxMessages) {
				delete messages[idBase + (lwm++)];
			}
			return messages[newId];
		},
		find: function(id) {
			return messages[id];
		},
		forEach: function(func) {
			for (var i = lwm; i < hwm; ++i) {
				func(messages[idBase + i]);
			}
		}
	}
})();

function processUserName(name) {
	return dehtml(
		name.trim().substring(0, Math.min(name.length, users.maxlength)).trim()
	);
}

var users = (function() {
	var maxlength = 16;
	var list = {};
	var id = (function() {
		var idBase = "user";
		// TODO use Clock
		var idStart = java.lang.System.currentTimeMillis();
		return function() {
			return idBase + (idStart++);
		}
	})();
	return {
		connected: function(user) {
			var userName = processUserName(user.name);
			var userId = user.id || id();
			console.log('user connected', userName, userId);
			if (!(userId in list)) {
				console.log('did not find the user, creating new');
				list[userId] = {
					id: userId,
					name: userName
				};
			}
			var message = messages.add('/me has joined the party', list[userId], true);
			broadcast(showMessage.bind(null, message));
			return list[userId];
		},
		disconnected: function(user) {
			var result = list[user.id];
			delete list[user.id];
			var message = messages.add('/me has left the party', result, true);
			broadcast(showMessage.bind(null, message));
			return result;
		},
		find: function(id) {
			return list[id];
		},
		forEach: function(func) {
			Object.keys(list).forEach(function(key) {
				func(list[key]);
			});
		},
		maxlength: maxlength
	}
})();

var topic = 'Welcome to JayChat!';
var USER_KEY = 'jaychat.user';

var command = (function() {

	var maxTopicLength = 100;
	
	var commands = {
		'help' : function(params) {
			
			return true;
		},
		'lights': function(params) {
			if (params == "out") {
				broadcast(lightsOut);
				return true;
			}
		},
		'title' : function(params) {
			params = params.split(/\s+/);
			var color = params[0];
			var duration = parseInt(params[1]);
			if (color && !isNaN(duration)) {
				broadcast(changeTitleColor.bind(null, color, duration));
			}
			// for now we always succeed
			return true;
		},
		'bg' : function(params) {
			params = params.split(/\s+/);
			var color = params[0];
			var duration = parseInt(params[1]);
			if (color && !isNaN(duration)) {
				broadcast(changeBackground.bind(null, color, duration));
			}
			// for now we always succeed
			return true;
		},
		'nick' : function(params) {
			var user = clientStorage.user;
			var oldName = user.name;
			user.name = processUserName(params);
			// you must store it again, it's all JSON.stringified in and out
			clientStorage.user = user;
			fStore(USER_KEY, user);
			// and send it out to everyone
			// with an announcement!
			var message = messages.add('/me is now known as ' + user.name, {id: user.id, name: oldName}, true);
			broadcast(function() {
				showMessage(message);
				userChangedName(user);
			});
			// and return true, we always succeed
			return true;
		},
		'topic' : function(params) {
			topic = 
				smileyify(
					linkify(
						dehtml(
							params.trim().substring(0, Math.min(params.length, maxTopicLength)).trim()
						)
					)
				);
			broadcast(changeTopic.bind(null, topic));
			// for now, we always succeed
			return true;
		}
	};
	
	var match = /^\/(\w+)\s/;
	
	return function(content) {
		var result = match.exec(content);
		if (result && (result[1] in commands)) {
			var params = content.substring(result[1].length + 2);
			return commands[result[1]](params);
		}
		return false;
	}
})();


// now we define a ready handler which will run every time someone requests the page
$(function(e) {
	
	// e.request contains information about the request.
	
	// defining a ready function inside a ready function is an error
	// but not one that i am catching yet. just don't do it
	
	console.log("prerendering the topic");
	$('#topic').html(topic);
	
	// prints to the console on the server.
	console.log("prerendering the messages");
	// addMessage is defined in index.shared.js
	// used here and in the index.js definition
	// of showMessage, which also scrolls the 
	// chatbox to the message enter.
	messages.forEach(addMessage);
	
	console.log("prerendering the list of users");
	users.forEach(function(user) {
		$('#users').append($('<div>', {id: user.id, 'class': 'user'}).html(user.name));
	});
	
	// first we define a function that responds to events from the client
	// this could also be defined in the page scope but as a matter of 
	// style keeping everything scoped as tightly as possible makes sense.
	// much like access in OOP, it's vastly simpler to loosen scope if needed 
	// rather than tighten if desired
	var chat = function(e) {
		
		// the form that was submitted is available as an object on the form
		// property of the event object
		var content = e.form.chat;
		
		// clear out the input for our lovely user
		// "setter" methods are fire-and-forget, so 
		// the script will continue executing here
		$('#userInput').val('');
		
		
		if (command(content)) {
			return;
		}
		
		// no message,nothing to do
		if (content) {
			
			// is it a command? execute if so
			if (!command(content)) {
				
				// if not, we'll build a message bundle!
				// clientStorage is an object that stores key-value pairs
				// per-client.  it functions like localStorage in the
				// browser - here we're reading the stored user
				var message = messages.add(content, clientStorage.user)
				
				// broadcast is a built-in function that will apply its function argument on each
				// connection to this script - every execution of an event occurs on behalf of
				// some client, which is held as a current client.  broadcast uses magical
				// plumbing to loop through all connected clients and switch context, then
				// execute its argument.
				
				broadcast(showMessage.bind(null, message));
			}
		}
	}
	
	$('#chatbox-form').on('submit', chat);
	
	// the RPC mechanism for client-server is by triggering events, which can be custom or
	// browser native.  these are a little weird, what we're doing here is
	// waiting for the tooltip to render in certain scenarios, and populating its contents
	// with meaningful information.  it's pretty simple!
	$('#users').on('powerTipPreRender', function(event) {
		var user = users.find(event.target.attr('id'));
		$('#powerTip').html('<h3 class="title">' + user.name + '</h3><p>Is that not special?</p>');
	});
	
	$('#chatbox').on('powerTipPreRender', function(event) {
		var message = messages.find(event.target.attr('id'));
		$('#powerTip').html('<h3 class="title">' + message.user.name + '</h3><p>said me!</p>');
	});
	
});

// registering for server-side events is best done outside of the ready function
// it will still work if you do it inside, but they will re-register every time
// someone requests the html page.

// clientConnected is called when a client connects
// within this event we are in the scope of that particular client
clientConnected(function() {
	
	function finish() {
		user = users.connected(user);
		fStore(USER_KEY, user);
		clientStorage.user = user;
		// you can broadcast from here as well
		broadcast(function() {
			// this is defined in index.js and called remotely 
			userSignedOn(user);
		});
	}
	
	function maybeAcceptName(e) {
		var found = false;
		var name = (e.form.userName || '').trim();
		if (name) {
			users.forEach(function(user) {
				if (user.name == name) found = true;
			});
			
			if (found) {
				sorryTryAnother();
			} else {
				user.name = name;
				$('#user-name-form').off('submit', maybeAcceptName);
				closeUserNameModal();
				finish();
			}
		}
	}
	
	var user = fRetrieve(USER_KEY) || {};
	
	if (user.id) {
		finish();
	} else {
		showUserNameModal();
		$('#user-name-form').on('submit', maybeAcceptName);
	}	
});

// clientDisconnected is called when a client disconnects
clientDisconnected(function() {
	
	// once again, need to save this outside
	// of the broadcast since the clientStorage 
	// context will change on each call to the
	// function being broadcast
	var user = users.disconnected(clientStorage.user);
	
	console.log('disconnected ' + JSON.stringify(user));
	// we can avoid notifying ourselves.
	// which is what ultimately happens
	// anyway, but this avoids even trying
	// to dispatch the messages
	// should either make broadcast smart enough
	// to not switch to a particular client
	// in this scenario, or add a param to
	// broadcast to say "skip this client"
	if (user) { // should always be true
		clientStorage.skipFlag = true;
		broadcast(function() {
			if (!clientStorage.skipFlag) {
				// this is defined in index.js and called remotely 
				userSignedOff(user);
			}
		});
	}
	// clientStorage may not in fact go out
	// of scope at this point since these
	// connections can be disrupted. some
	// sort of reconnection algorithm is 
	// almost certainly in the pipeline
	// i feel like this sort of thing
	// should live in a persistent store
	// (couchDB is probably ideal for this)
	// and clients will get an id on first
	// connection to a given page instance
	// (or server instance?  like a session)
	delete clientStorage.skipFlag;
});

// other plans for future host objects 
// - connection object with enter/exit context methods to allow client --> client messaging,
//   to be passed along in the clientConnected/clientDisconnected events
