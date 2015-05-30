var smileys = {
		
	')': 'smile',
	'-)': 'smile'
}


var smiley = /:([a-z0-9]+)[:\b]/gi;

// assigning to module.exports replaces the exports object. you can
// use this for single-function modules
module.exports = function(input) {
	return input.replace(smiley, function(result, key) {
		if (key in smileys) {
			return '<span class="smiley ' + smileys[key] + '">:' + key + ':</span>';
		}
		return result;
	});
}