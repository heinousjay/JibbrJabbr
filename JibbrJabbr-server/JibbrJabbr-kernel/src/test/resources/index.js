// this file contains the code that is going to run
// on the client for index.html.  it is automatically
// included by the convention of the file name.  it's
// an enhanced-normal client-side javascript file
// - jQuery 1.8.3 is already included
// - a support library is included, although at the
//   moment it doesn't expose any functionality directly
//   you can trap socket events if you like.
// - this script will be included after them. the mechanism is
//   not yet in place for this, but the idea is to minify
//   everything before serving it.  right now it's going
//   to be output unmolested

// you can just do normal things in this file
$(function() {
	// there might be stuff in there overflowing the bottom
	$('#chatbox').scrollTo('max');
	$('a.fancybox').each(fancyboxit);
});


// there are some socket events exposed on the window
$(window).bind('socketopen', function(event) {
	console.log(event);

	// right now there's nothing interesting passed along, you
	// can just get the notification.  if more is needed we'll
	// make more!
});

$(window).bind('socketclose', function(event) {
	console.log(event);
});

(function($) {
	$.fn.highlight = function(color) {
		return this.animate({
			backgroundColor:color || '#00BB22'
		},200, function() {
			$(this).animate({
				backgroundColor:'transparent'
			}, 150);
		});
	}
})(jQuery);


// you can also define functions that you can call remotely
// there's no special set up but there are some rules

// 1 don't throw exceptions. exceptions don't go over the wire, 
//   they just make things stop working, so don't write functions
//   that throw exceptions

// 2 a remote function has to be standalone, it will be invoked
//   against the global context (i.e. window)

// 3 it can only take arguments that can survive a trip over 
//   a JSON transport,

// 4 the function has to be declared all the way at the left
//   margin or it won't be recognized, and it must be global
//   or it won't be called correctly.

// 5 if the function is going to return something, there needs
//   to be a return statement on the last line of the function
//   or the rpc system won't notice and will ignore it. also the
//   return value must be able to survive a JSON transport.

// 6 these functions cannot be invoked during the rendering phase
//   on the server, only during event handling.

// just define them in the client section like so, and
// call them from the server-side event handlers.  they'll just
// work

var animateLoading = function(flag, element) {
	
	var up = false;
	
	function animate() {
		var color = (up = !up) ? '#FFE47A' : 'transparent';
		element.animate({
			backgroundColor: color
		}, 1350, callback);
	}
	
	function callback() {
		if (flag.on) {
			animate();
		} else {
			element.animate({
				backgroundColor:'transparent'
			}, 25);
		}
	}
	
	animate();
}

var fancyboxit = function() {
	var flag = {on: true};
	$(this).fancybox({
		openEffect: 'elastic',
		closeEffect: 'elastic',
		beforeLoad: function() {
			//animateLoading(flag, this.element);
		},
		onCancel: function () {
			flag.on = false;
		},
		afterLoad: function() {
			flag.on = false;
		}
	});
}



function showMessage(message) {
	// addMessage is defined in index.shared.js so it can
	// be used on the server as well as on the client
	$('a.fancybox', addMessage(message)).each(fancyboxit);
	
	
	$('#chatbox').scrollTo('max');
	if ($('#chatbox > div').length > maxMessages) {
		$('#chatbox > div:first').remove();
	}
}

function userSignedOn(user) {
	// makeUserId is also defined in index.shared.js
	if ($('#' + user.id).length == 0) {
		var div = $("<div>", {id: user.id}).html(user.name);
		$('#users').append(div);
		div.highlight();
	}
}

function userSignedOff(user) {
	$('#' + user.id).removeAttr('id').fadeOut(50, function() {
		$(this).remove();
	});
}

function userChangedName(user) {
	if (user.id === localStorage.userId) {
		localStorage.userName = user.name;
	}
	$('#' + user.id).html(user.name).highlight();
}

function setUserId(userId) {
	localStorage.userId = userId;
}

function changeBackground(newColor, duration) {
	$('body').animate({
		backgroundColor: newColor
	}, duration);
}

function changeTitleColor(newColor, duration) {
	$('#welcome').animate({
		color: newColor
	}, duration);
}
// returns the latest message this client has received,
// used to bring them up to date on connect
function latestMessageId() {
	return $('#chatbox div:last').attr('id');
}

function getUser() {
	if (!('userName' in localStorage)) {
		// clearly this could be nicer
		// and should be, to demonstrate
		// the programming model properly
		// how to get messages to the client script?
		localStorage.userName = prompt("what's your username?");
	}
	var result = {
		name: localStorage.userName,
		id: localStorage.userId
	};
	// the return statement must be on the last line or the
	// RPC mechanism won't pick it up
	return result;
}

function changeTopic(topic) {
	$('#topic').fadeOut(150, function() {
		$('a.fancybox', $(this).html(topic).fadeIn(150)).each(fancyboxit);
	});
	$('#topic-holder div').highlight();
}

// all of these functions are now available to call in event handlers
// in index.server.js with normal javascript syntax.  the server does 
// some magic to make it all work seamlessly.
