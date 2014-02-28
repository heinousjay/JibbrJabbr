var maxlength = 30;
// the daring fireball link finder
var http = /\b((?:(https?:\/\/)|www\d{0,3}[.]|[a-z0-9.\-]+[.][a-z]{2,4}\/)(?:[^\s()<>]+|\(([^\s()<>]+|(\([^\s()<>]+\)))*\))+(?:\(([^\s()<>]+|(\([^\s()<>]+\)))*\)|[^\s`!()\[\]{};:'".,<>?«»“”‘’]))/gi;
// is it a pic? we can lightbox them
var pic = /(?:gif|jpg|jpeg|png)$/;
var youtube = /(?:youtube.com\/watch\?v=|youtu.be\/)([\w\d-]+)/;
var youtubeTime = /(?:t=(?:(\d+)m)?(?:([\d.]+)s))/;

/*
 * <iframe width="560" height="315" 
 * 	src="http://www.youtube.com/embed/J91ti_MpdHA" 
 * frameborder="0" allowfullscreen></iframe>
 */

module.exports = function(input) {
	return input.replace(http, function(result, match, scheme) {
		
		var href = result;
		if (!scheme) {
			href = "http://" + result;
		}

		var target = pic.test(href) ?
			'class="fancybox"' :
			'target="_blank"';

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
			href = 'http://www.youtube.com/embed/' + 
				key[1] +
				'?autoplay=1&wmode=opaque';
			
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

		return '<a ' + target + ' href="' + href + '" title="' + href + '">' + 
			result + 
			'</a>';
    });
}