// this file contains the code that is going to run
// on the client for index.html.  it is automatically
// included by the convention of the file name.  it's
// an enhanced-normal client-side javascript file
// - jQuery 2.0.2 is automatically included, unless you 
// - a support library is included, although at the
//   moment it doesn't expose any functionality directly
//   you can trap socket events if you like.
// - this script will be included after them. the mechanism is
//   not yet in place for this, but the idea is to minify
//   everything before serving it.  right now it's going
//   to be output unmolested

// you can just do normal things in this file
$(function() {
	// just a nicety
	$('#button1').click(function() {
		$('#userInput').focus();
	})
	
	// need to do the do on all the prerendered stuff
	// it might be simpler to render empty and get it all after connection?
	
	// scroll to the bottom
	$('#chatbox').scrollTo('max');
	$('a.fancybox').each(fancyboxit);
	$('#userInput').focus();
	$('#users').on('mouseover', '.user', function(event) {
		$('.' + this.id).parent().stop(true, true).animate({
			backgroundColor: '#00BB22'
		}, 400);
	}).on('mouseout', '.user', function(event) {
		$('.' + this.id).parent().stop(true, true).animate({
			backgroundColor: 'transparent'
		}, 250);
	});
	//powerTipIt($('#users>div, #chatbox>div'));
});

function showUserNameModal() {
	$('#user-name-modal').modal({
		escapeClose: false,
		clickClose: false,
		showClose: false
	});
	$('#user-name').focus();
}

function sorryTryAnother() {
	$('#user-name-form').fadeOut(450);
	$('#user-name').val('');
	$('#user-name-message-try-another').fadeIn(450);
	setTimeout(function() {
		$('#user-name-form').fadeIn(450, function() {
			$('#user-name').focus();
		});
		$('#user-name-message-try-another').fadeOut(450);
	},3500);
	
}

function closeUserNameModal() {
	$.modal.close();
	$('#userInput').focus();
}

var flashTitle = function(title) {
	var state;
	var id = window.setInterval(function() {
		if (state) {
			document.title = state;
			state = false;
		} else {
			state = document.title;
			document.title = title;
		}
	}, 1500);
	return function() {
		window.clearInterval(id);
	}
}

var isOpen = false,
	placeholder,
	enteredText = '';

// there are some socket events exposed on the window
$(window).on('socketopen', function(event) {
	if (isOpen) {
		isOpen();
		isOpen = false;
	}
	$('[disabled]').removeAttr('disabled');
	placeholder = $('#userInput').attr('placeholder');
	$('#userInput').val(enteredText).removeAttr('placeholder').focus();
	
});

$(window).on('socketclose', function(event) {
	isOpen = flashTitle('Disconnected - Reload');
	enteredText = $('#userInput').val();
	$('#userInput')
		.attr('placeholder', placeholder)
		.val('')
		.add('#button1')
			.attr('disabled', 'disabled')
			.blur();
});

(function($) {
	// define a highlight plugin. yay
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

var powerTipIt = function(el) {
//	el.powerTip({
//		mouseOnToPopup: true,
//		placement: 'sw',
//		smartPlacement: true
//	}).data('powertip', 'Please wait...');
	return el;
}

// you can also define functions that you can call remotely
// there's no special set up but there are some rules

// 1 don't throw exceptions. exceptions don't go over the wire, 
//   they just make things stop working, so don't write functions
//   that throw exceptions

// 2 a remote function has to be standalone, it will be invoked
//   against the global context (i.e. window)

// 3 it can only take arguments that can survive a trip over 
//   a JSON transport,

// 4 the function has to be defined all the way at the left
//   margin or it won't be recognized, it must be global
//   or it won't be called correctly, and it must be defined
//   using the syntax you see below.

// 5 if the function is going to return something, there needs
//   to be a return statement on the last line of the function
//   or the rpc system won't notice and will ignore it. this means
//   the last line in a file sense, not a syntactic sense. also
//   the return value must be able to survive a JSON transport.

// 6 these functions cannot be invoked during the rendering phase
//   on the server, only during event handling after a client connection
//   is made

// these rules are going to change in the near future - at the very least, any global
// functions will be treated as RPC callable.  For now just the text pattern is used

// just define them in the client section like so, and
// call them from the server-side event handlers.  they'll just
// work

function showMessage(message) {
	// addMessage is defined in index.shared.js so it can
	// be used on the server as well as on the client
	// here we want to add the fancybox to new lines if needed
	$('a.fancybox', addMessage(message)).each(fancyboxit);
	
	
	$('#chatbox').scrollTo('max');
	if ($('#chatbox > div').length > maxMessages) {
		$('#chatbox > div:first').remove();
	}
}

function userSignedOn(user) {
	// makeUserId is also defined in index.shared.js
	if ($('#' + user.id).length == 0) {
		var div = $("<div>", {id: user.id, 'class': 'user'}).html(user.name);
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
	$('#' + user.id).html(user.name).highlight();
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

function changeTopic(topic) {
	$('#topic').fadeOut(150, function() {
		$('a.fancybox', $(this).html(topic).fadeIn(150)).each(fancyboxit);
	});
	$('#topic-holder div').highlight();
}

function lightsOut() {
	$('#container').fadeOut(1500);
	changeBackground('black', 2500);
}

// all of these functions are now available to call in event handlers
// in index.server.js with normal javascript syntax.  the server does 
// some magic to make it all work seamlessly.
