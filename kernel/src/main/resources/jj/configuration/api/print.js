// just for testing

module.exports = function() {
	Array.prototype.forEach.call(arguments, function(arg) {
		java.lang.System.out.append(arg).append(" ");
	});
	java.lang.System.out.println();
}