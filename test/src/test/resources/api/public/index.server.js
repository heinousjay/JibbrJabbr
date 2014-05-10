var broadcast = require('broadcast');

$(function() {
	broadcast(function() {
		$('body').append($('<p></p>').text('nre request'));
	})
});