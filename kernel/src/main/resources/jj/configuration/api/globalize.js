// wraps the require function, copying all of the results
// to the new home (presumably the global 'this' of the including script)

module.exports = function(id, newHome) {
	var m = require(id);
	Object.keys(m).forEach(function(key) {
		newHome[key] = m[key];
	});
}