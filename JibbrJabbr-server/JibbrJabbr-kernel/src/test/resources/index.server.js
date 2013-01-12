
var dehtml = require('./helpers').dehtml;

var linkify = (function() {
	var maxlength = 30;
	// the daring fireball link finder
    var http = /\b((?:(https?:\/\/)|www\d{0,3}[.]|[a-z0-9.\-]+[.][a-z]{2,4}\/)(?:[^\s()<>]+|\(([^\s()<>]+|(\([^\s()<>]+\)))*\))+(?:\(([^\s()<>]+|(\([^\s()<>]+\)))*\)|[^\s`!()\[\]{};:'".,<>?«»“”‘’]))/gi;
    // is it a pic? we can lightbox them
    var pic = /(?:gif|jpg|jpeg|png)$/;
    var youtube = /(?:youtube.com\/watch\?v=|youtu.be\/)([\w\d-]+)/;
    var youtubeTime = /(?:t=(?:(\d+)m)?(?:([\d.]+)s))/;
    
    return function(input) {
        return input.replace(http, function(result, match, scheme) {
        	
        	var href = result;
        	if (!scheme) {
        		href = "http://" + result;
        	}
        	
        	var target = pic.test(href) ? 'class="fancybox"' : 'target="_blank"';
        	
        	var key = youtube.exec(href);
        	if (key) {
        		var time = youtubeTime.exec(href);
        		var secs = 0;
        		if (time) {
        			var min = parseInt(time[1]);
        			secs = parseInt(time[2]);
        			if (isNaN(secs)) {
        				secs = 0;
        			} else if (!isNaN(min)) {
        				secs += min * 60;
        			}
        		}
        		href = 'http://www.youtube.com/embed/' + key[1] + '?autoplay=1&wmode=opaque';
    			if (secs) {
    				href += '&start=' + secs;
    			}
    			target = 'class="fancybox fancybox.iframe"'
        	}
        	
        	if (result.indexOf(scheme) == 0) {
        		result = result.substring(scheme.length);
        	}
        	
        	if (result.length > maxlength + 3) {
        		result = result.substring(0, maxlength) + '...';
        	}
        	
        	
        	
        	return '<a ' +
        		target +
        		' href="' +
	            href +
	            '" title="' +
	            href +
	            '">' +
	            result +
	            '</a>';
        });
    }
})();

var smileys = {
		
	// add smileys here in a format like
	//'name': {src:'/path/to/image', width:48, height:48}
}

var smileyify = (function() {
	var smiley = /:([a-z0-9]+):/gi;
	return function(input) {
		return input.replace(smiley, function(result, key) {
			if (key in smileys) {
				var smiley = smileys[key];
				return '<img src="' + 
					smiley.src + 
					'" width="' + 
					smiley.width + 
					'" height="' + 
					smiley.height + 
					'" alt="' +
					key +
					'"/>';
			}
			return result;
		});
	}
})();


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
	var store = {};
	var id = (function() {
		var idBase = "user";
		var idStart = java.lang.System.currentTimeMillis();
		return function() {
			return idBase + (idStart++);
		}
	})();
	return {
		connected: function(user) {
			var userName = processUserName(user.name);
			var userId = user.id || id();
			print('user connected: ' + userName + ', ' + userId);
			if (!(userId in store)) {
				print('did not find the user, creating new');
				store[userId] = {
					id: userId,
					name: userName
				};
				setUserId(userId);
			}
			return store[userId];
		},
		disconnected: function(user) {
			var result = store[user.id];
			delete store[user.id];
			return result;
		},
		forEach: function(func) {
			Object.keys(store).forEach(function(key) {
				func(store[key]);
			});
		},
		maxlength: maxlength
	}
})();

var topic = 'Welcome to JayChat!';

var command = (function() {

	var maxTopicLength = 100;
	
	var commands = {
		'nick' : function(params) {
			var user = clientStorage.user;
			var oldName = user.name;
			user.name = processUserName(params);
			// you must store it again, it's all JSON.stringified in and out
			clientStorage.user = user;
			// and send it out to everyone
			broadcast(userChangedName.bind(null, user));
			// and send an announcement!
			var message = messages.add('/me is now known as ' + user.name, {id: user.id, name: oldName}, true);
			broadcast(showMessage.bind(null, message));
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


// now we define a ready function which will run every time someone requests the page
$(function() {

	// defining a ready function inside a ready function is an error
	// but not one that i am catching yet. just don't do it
	
	print("prerendering the topic");
	$('#topic').html(topic);
	
	// prints to the console on the server. 
	// going to be moved into a console object
	// so you can do console.log, console.warn, console.error
	// print will be retargeted to print to the output?
	// so that responses can be made of whole cloth
	print("prerendering the messages");
	// addMessage is defined in index.shared.js
	// used here and in the index.js definition
	// of showMessage, which also scrolls the 
	// chatbox to the message enter.
	messages.forEach(addMessage);
	
	print("prerendering the list of users");
	users.forEach(function(user) {
		$('#users').append($('<div>', {id: user.id, 'class': 'user'}).html(user.name));
	});
	
	// first we define a function that responds to events from the client
	// this could also be defined in the page scope but as a matter of 
	// style keeping everything scoped as tightly as possible makes sense.
	// much like access in OOP, it's vastly simpler to loosen scope if needed 
	// rather than tighten if desired
	var chat = function() {
		// pretty standard jQuery style here, read the value of the 
		// user input that triggered the event and trim it.
		// you'd never know it by looking here, but that val() 
		// call does A LOT of work.  watch the logs!
		// "getter" methods will actually pause the execution of 
		// this script and send a message to the connected client
		// to retrieve the value.  when the response comes back 
		// from the client, the script is resumed seamlessly
		var content = $('#userInput').val().trim();
		
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
				// browser - here we're reading the stored user name
				var message = messages.add(content, clientStorage.user)
				
				// broadcast is a built-in function that will apply its function argument on each
				// connection to this script - every execution of an event occurs on behalf of
				// some client, which is held as a current client.  broadcast uses magical
				// plumbing (it's declared in jj.ScriptManager if you want to demystify it)
				// to loop through all connected clients and switch context, then execute its
				// argument.
				broadcast(showMessage.bind(null, message));
			}
		}
	}
	

	// and finally, just hook the events up
	// click is what you think it is
	$('#button1').click(chat);
	
	// enter is a special event that fires only on the enter key.
	// we handle this individually because sending every key event over 
	// the wire when all you want is enter.. kinda sucks.
	$('#userInput').enter(chat);
	
	// unfortunately, 

});

// registering for server-side events is best done outside of the ready function
// it will still work if you do it inside, but they will re-register every time
// someone requests the html page... which may be what you want if you have
// something amazingly clever in mind.  bless you if you do. if not, declare
// them globally

// clientConnected is called when a client connects
// within this event we are in the scope of that particular client
clientConnected(function() {

	// getUser is defined in index.js and invoked remotely
	// tell me that's not simple.
	var user = users.connected(getUser());
	clientStorage.user = user;
	// you can broadcast from here as well
	broadcast(function() {
		// this is defined in index.js and called remotely 
		userSignedOn(user);
	});
	
});

// clientDisconnected is called when a client disconnects
clientDisconnected(function() {
	
	// once again, need to save this outside
	// of the broadcast since the clientStorage 
	// context will change on each call to the
	// function being broadcast
	var user = users.disconnected(clientStorage.user);
	
	print('disconnected ' + JSON.stringify(user));
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


// - redo all of this for common js modules.  there is an important limitation in that
//   continuations will not be supported during the module definition phase. this is
//   something that needs to be addressed in any case, as these classes also go out
//   of their way to assume they are in a multithreaded environment which, while not
//   a huge deal since uncontended locks aren't toooo expensive, is still a waste of
//   time given our architecture.
